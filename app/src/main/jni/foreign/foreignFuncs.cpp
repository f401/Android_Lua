#include <jni.h>
#include <string>

extern "C" {
#include <stdlib.h>
#include <dlfcn.h>
#include <string.h>
#include <errno.h>
#include "../common.h"
#include <ffi.h>
#include "utils.h"
}

/*
 * These functions may be called multiple times, so dynamic registration is used.
 */
#define ERROR_DL_OPEN_FAILED "DLL open failed: "
#define ERROR_DL_LOAD_SYMBOL_FAILED "Can't find symbol: "

static void freePointer(JNIEnv *env, jclass clazz, jobject ptr) {
    free(pointer_get_from(env, ptr));
}

static jobject alloc(JNIEnv *env, jclass clazz, jlong size) {
    void *handle = calloc(size, 1);
    if (handle == nullptr) {
        std::string msg("Failed to alloc ");
        msg.append(std::to_string(size));
        msg.append(" data, reason: ");
        msg.append(strerror(errno));
        throwNativeException(env, msg.data());
    }
    return pointer_create(env, handle);
}

static jstring getSystemError(JNIEnv *env, jclass clazz) {
    return env->NewStringUTF(strerror(errno));
}

static jobject dllOpen(JNIEnv *env, jclass clazz, jstring jpath, jint flag) {
    const char *path = env->GetStringUTFChars(jpath, JNI_FALSE);

    void *handle = dlopen(path, flag);

    if (handle == nullptr) {
        std::string msg(ERROR_DL_OPEN_FAILED);
        msg.append(dlerror());
        throwNativeException(env, msg.data());
    }

    env->ReleaseStringUTFChars(jpath, path);
    return pointer_create(env, handle);
}

static jint dllClose(JNIEnv *env, jclass clazz, jobject handle) {
    return dlclose(pointer_get_from(env, handle));
}

static jobject dllSymbolLookup(JNIEnv *env, jclass clazz, jobject handle, jstring name) {
    const char *sym = env->GetStringUTFChars(name, JNI_FALSE);
    void *result = dlsym(pointer_get_from(env, handle), sym);

    if (result == nullptr) {
        std::string msg(ERROR_DL_LOAD_SYMBOL_FAILED);
        msg.append(sym).append(".Reason: ").append(dlerror());
        throwNativeException(env, msg.data());
    }

    env->ReleaseStringUTFChars(name, sym);
    return pointer_create(env, result);
}

static void duplicateStringTo(JNIEnv *env, jclass clazz, jobject handle, jstring string) {
    const char *str = env->GetStringUTFChars(string, JNI_FALSE);
    size_t string_length = env->GetStringUTFLength(string);
    memcpy(pointer_get_from(env, handle), str, string_length + 1);
    env->ReleaseStringUTFChars(string, str);
}

static jint ffiPrepareCIF(JNIEnv *env, jclass clazz, jlong cif, jint argsCount, jlong returnType,
                          jlong paramsType) {
    return ffi_prep_cif(jlong_to_ptr(cif, ffi_cif),
                        FFI_DEFAULT_ABI, argsCount, jlong_to_ptr(returnType, ffi_type),
                        jlong_to_ptr(paramsType, ffi_type*));
}

//----------------------------------------------------------value handles-----------------------------------------------//

//Macro is a good thing.
#define DEF_PUT_FUNCS(TYPE)                                                         \
static void putJava##TYPE(JNIEnv *env, jclass clazz, jobject ptr, j##TYPE value) {    \
    memcpy(pointer_get_from(env, ptr), &value, sizeof(j##TYPE));                       \
}                                                                                   \
static void putJava##TYPE(JNIEnv *env, jclass clazz, jobject ptr, j##TYPE value)

DEF_PUT_FUNCS(byte);

DEF_PUT_FUNCS(int);

DEF_PUT_FUNCS(short);

DEF_PUT_FUNCS(char);

DEF_PUT_FUNCS(long);

#undef DEF_PUT_FUNCS

#define DEF_PEEK_FUNCS(TYPE)                                                         \
static j##TYPE peekJava##TYPE(JNIEnv *env, jclass clazz, jobject ptr) {                \
    j##TYPE result = 0;                                                                  \
    memcpy(pointer_get_from(env, ptr), &result, sizeof(result));                        \
    return result;                                                                   \
}                                                                                    \
static j##TYPE peekJava##TYPE(JNIEnv *env, jclass clazz, jobject ptr)

DEF_PEEK_FUNCS(byte);

DEF_PEEK_FUNCS(int);

DEF_PEEK_FUNCS(short);

DEF_PEEK_FUNCS(char);

DEF_PEEK_FUNCS(long);

#undef DEF_PEEK_FUNCS

const static JNINativeMethod methods[] = {
        {"alloc",             "(J)Lnet/fred/lua/foreign/Pointer;",                                                (void *) &alloc},
        {"free",              "(Lnet/fred/lua/foreign/Pointer;)V",                                                (void *) &freePointer},
        {"strerror",          "()Ljava/lang/String;",                                                             (void *) &getSystemError},
        {"dlopen",            "(Ljava/lang/String;I)Lnet/fred/lua/foreign/Pointer;",                              (void *) &dllOpen},
        {"dlclose",           "(Lnet/fred/lua/foreign/Pointer;)I",                                                (void *) &dllClose},
        {"dlsym",             "(Lnet/fred/lua/foreign/Pointer;Ljava/lang/String;)Lnet/fred/lua/foreign/Pointer;", (void *) &dllSymbolLookup},

        {"putByte",           "(Lnet/fred/lua/foreign/Pointer;B)V",                                               (void *) &putJavabyte},
        {"putChar",           "(Lnet/fred/lua/foreign/Pointer;C)V",                                               (void *) &putJavachar},
        {"putShort",          "(Lnet/fred/lua/foreign/Pointer;S)V",                                               (void *) &putJavashort},
        {"putInt",            "(Lnet/fred/lua/foreign/Pointer;I)V",                                               (void *) &putJavaint},
        {"putLong",           "(Lnet/fred/lua/foreign/Pointer;J)V",                                               (void *) &putJavalong},

        {"peekByte",          "(Lnet/fred/lua/foreign/Pointer;)B",                                                (void *) &peekJavabyte},
        {"peekChar",          "(Lnet/fred/lua/foreign/Pointer;)C",                                                (void *) &peekJavachar},
        {"peekShort",         "(Lnet/fred/lua/foreign/Pointer;)S",                                                (void *) &peekJavashort},
        {"peekInt",           "(Lnet/fred/lua/foreign/Pointer;)I",                                                (void *) &peekJavaint},
        {"peekLong",          "(Lnet/fred/lua/foreign/Pointer;)J",                                                (void *) &peekJavalong},
        {"duplicateStringTo", "(Lnet/fred/lua/foreign/Pointer;Ljava/lang/String;)V",                              (void *) &duplicateStringTo},

        {"ffi_prep_cif",      "(JIJJ)I",                                                                          (void *) &ffiPrepareCIF},
};

static int registerMethods(JNIEnv *env) {
    jclass clazz = env->FindClass("net/fred/lua/foreign/internal/ForeignFunctions");
    if (clazz != nullptr &&
        env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(methods[0])) == JNI_OK) {
        return JNI_OK;
    }
    return JNI_ERR;
}

extern "C"
jint JNI_OnLoad(JavaVM *vm, void *reversed) {
    jint version = JNI_ERR;
    JNIEnv *env = nullptr;

    if (vm->GetEnv((void **) &env, JNI_VERSION_1_1) == JNI_OK) {
        if (registerMethods(env) == JNI_OK) {
            version = JNI_VERSION_1_4;
        }
    }

    return version;
}