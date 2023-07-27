#include <jni.h>
#include <string>

extern "C" {
#include <stdlib.h>
#include <dlfcn.h>
#include <string.h>
#include <errno.h>
#include "../common.h"
#include "utils.h"
}

/*
 * These functions may be called multiple times, so dynamic registration is used.
 */
#define ERROR_DL_OPEN_FAILED "DLL open failed: "
#define ERROR_DL_LOAD_SYMBOL_FAILED "Can't find symbol: "

static void FreePointer(JNIEnv *env, jclass clazz, jobject ptr) {
    GET_POINTER_PARAM(env, dst, ptr,);
    free(dst);
}

static jobject AllocateSegment(JNIEnv *env, jclass clazz, jlong size) {
    void *handle = calloc(size, 1);
    if (handle == nullptr) {
        std::string msg("Failed to alloc ");
        msg.append(std::to_string(size));
        msg.append(" data, reason: ");
        msg.append(strerror(errno));
        throwNativeException(env, msg.data());
        return nullptr;
    }
    return pointer_create(env, handle);
}

static jstring GetSystemError(JNIEnv *env, jclass clazz) {
    return env->NewStringUTF(strerror(errno));
}

static jobject OpenDll(JNIEnv *env, jclass clazz, jstring jpath, jint flag) {
    const char *path = env->GetStringUTFChars(jpath, JNI_FALSE);

    void *handle = dlopen(path, flag);

    if (handle == nullptr) {
        std::string msg(ERROR_DL_OPEN_FAILED);
        msg.append(dlerror());
        throwNativeException(env, msg.data());
        env->ReleaseStringUTFChars(jpath, path);
        return nullptr;
    }

    env->ReleaseStringUTFChars(jpath, path);
    return pointer_create(env, handle);
}

static void CopyMemory(JNIEnv *env, jclass clazz, jobject _dest, jobject _src, jlong length) {
    GET_POINTER_PARAM(env, dest, _dest,);
    GET_POINTER_PARAM(env, src, _src,);
    memcpy(dest, src, length);
}

static jint CloseDll(JNIEnv *env, jclass clazz, jobject handle) {
    GET_POINTER_PARAM(env, dst, handle, -1);
    return dlclose(dst);
}

static jobject LookUpSymbol(JNIEnv *env, jclass clazz, jobject handle, jstring name) {
    const char *sym = env->GetStringUTFChars(name, JNI_FALSE);
    GET_POINTER_PARAM(env, dst, handle, nullptr);
    void *result = dlsym(dst, sym);

    if (result == nullptr) {
        std::string msg(ERROR_DL_LOAD_SYMBOL_FAILED);
        msg.append(sym).append(".Reason: ").append(dlerror());
        throwNativeException(env, msg.data());
        return nullptr;
    }

    env->ReleaseStringUTFChars(name, sym);
    return pointer_create(env, result);
}

static jlong ObtainStringLen(JNIEnv *env, jclass clazz, jobject dest) {
    GET_POINTER_PARAM(env, dst, dest, -1);
    return strlen((char *) dst);
}

//----------------------------------------------------------value handles-----------------------------------------------//

//Macro is a good thing.
#define DEF_PUT_FUNCS(TYPE)                                                          \
static void putJava##TYPE(JNIEnv *env, jclass clazz, jobject ptr, j##TYPE value) {   \
    GET_POINTER_PARAM(env, dst, ptr, );                                              \
    memcpy(dst, &value, sizeof(j##TYPE));                                            \
}                                                                                    \
static void putJava##TYPE(JNIEnv *env, jclass clazz, jobject ptr, j##TYPE value)

DEF_PUT_FUNCS(byte);

DEF_PUT_FUNCS(int);

DEF_PUT_FUNCS(short);

DEF_PUT_FUNCS(char);

DEF_PUT_FUNCS(long);


static void PutString(JNIEnv *env, jclass clazz, jobject handle, jstring string) {
    const char *str = env->GetStringUTFChars(string, JNI_FALSE);
    size_t string_length = env->GetStringUTFLength(string);
    GET_POINTER_PARAM(env, dst, handle,);
    memcpy(dst, str, string_length + 1);
    env->ReleaseStringUTFChars(string, str);
}

static void putJavaPointer(JNIEnv *env, jclass clazz, jobject _dest, jobject _source) {
    GET_POINTER_PARAM(env, source, _source,);
    GET_POINTER_PARAM(env, dst, _dest,);
    memcpy(dst, &source, sizeof(void *));
}

#undef DEF_PUT_FUNCS

#define DEF_PEEK_FUNCS(TYPE)                                               \
static j##TYPE peekJava##TYPE(JNIEnv *env, jclass clazz, jobject ptr) {    \
    j##TYPE result = 0;                                                    \
    GET_POINTER_PARAM(env, dst, ptr, 0);                                   \
    memcpy(&result, dst, sizeof(result));                                  \
    return result;                                                         \
}                                                                          \
static j##TYPE peekJava##TYPE(JNIEnv *env, jclass clazz, jobject ptr)

DEF_PEEK_FUNCS(byte);
DEF_PEEK_FUNCS(int);
DEF_PEEK_FUNCS(short);

DEF_PEEK_FUNCS(char);

DEF_PEEK_FUNCS(long);

static jobject peekJavaPointer(JNIEnv *env, jclass clazz, jobject dst) {
    GET_POINTER_PARAM(env, dest, dst, nullptr);
    void *result;
    memcpy(&result, dest, sizeof(void *));
    return pointer_create(env, result);
}

static jobject ReadString(JNIEnv *env, jclass clazz, jobject dest) {
    GET_POINTER_PARAM(env, dst, dest, nullptr);
    return env->NewStringUTF((char *) dst);
}

#undef DEF_PEEK_FUNCS

const static JNINativeMethod methods[] = {
        {"alloc",              "(J)Lnet/fred/lua/foreign/Pointer;",                                                              (void *) &AllocateSegment},
        {"free",               "(Lnet/fred/lua/foreign/Pointer;)V",                                                              (void *) &FreePointer},
        {"strerror",           "()Ljava/lang/String;",                                                                           (void *) &GetSystemError},
        {"memcpy",             "(Lnet/fred/lua/foreign/Pointer;Lnet/fred/lua/foreign/Pointer;J)V",                               (void *) &CopyMemory},
        {"dlopen",             "(Ljava/lang/String;I)Lnet/fred/lua/foreign/Pointer;",                                            (void *) &OpenDll},
        {"dlclose",            "(Lnet/fred/lua/foreign/Pointer;)I",                                                              (void *) &CloseDll},
        {"dlsym",
                               "(Lnet/fred/lua/foreign/Pointer;Ljava/lang/String;)Lnet/fred/lua/foreign/Pointer;",
                                                                                                                                 (void *) &LookUpSymbol},
        {"putByte",            "(Lnet/fred/lua/foreign/Pointer;B)V",                                                             (void *) &putJavabyte},
        {"putChar",            "(Lnet/fred/lua/foreign/Pointer;C)V",                                                             (void *) &putJavachar},
        {"putShort",           "(Lnet/fred/lua/foreign/Pointer;S)V",                                                             (void *) &putJavashort},
        {"putInt",             "(Lnet/fred/lua/foreign/Pointer;I)V",                                                             (void *) &putJavaint},
        {"putLong",            "(Lnet/fred/lua/foreign/Pointer;J)V",                                                             (void *) &putJavalong},
        {"putPointer",         "(Lnet/fred/lua/foreign/Pointer;Lnet/fred/lua/foreign/Pointer;)V",
                                                                                                                                 (void *) &putJavaPointer},

        {"peekByte",           "(Lnet/fred/lua/foreign/Pointer;)B",                                                              (void *) &peekJavabyte},
        {"peekChar",           "(Lnet/fred/lua/foreign/Pointer;)C",                                                              (void *) &peekJavachar},
        {"peekShort",          "(Lnet/fred/lua/foreign/Pointer;)S",                                                              (void *) &peekJavashort},
        {"peekInt",            "(Lnet/fred/lua/foreign/Pointer;)I",                                                              (void *) &peekJavaint},
        {"peekLong",           "(Lnet/fred/lua/foreign/Pointer;)J",                                                              (void *) &peekJavalong},
        {"peekPointer",        "(Lnet/fred/lua/foreign/Pointer;)Lnet/fred/lua/foreign/Pointer;",
                                                                                                                                 (void *) &peekJavaPointer},

        {"putString",          "(Lnet/fred/lua/foreign/Pointer;Ljava/lang/String;)V",
                                                                                                                                 (void *) &PutString},
        {"peekString",         "(Lnet/fred/lua/foreign/Pointer;)Ljava/lang/String;",                                             (void *) &ReadString},
        {"obtainStringLength", "(Lnet/fred/lua/foreign/Pointer;)J",                                                              (void *) &ObtainStringLen},
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
            version = JNI_VERSION_1_6;
        }
    }

    return version;
}
