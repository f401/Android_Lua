#include <jni.h>
#include <stdlib.h>
#include <dlfcn.h>
#include <string.h>
#include <errno.h>
#include "../common.h"
#include <ffi.h>


/*
 * These functions may be called multiple times, so dynamic registration is used.
 */

static void freePointer(JNIEnv *env, jclass clazz, jlong ptr) {
    free(jlong_to_ptr(ptr, void));
}

static jlong alloc(JNIEnv* env, jclass clazz, jlong size) {
    return ptr_to_jlong(calloc(size, 1));
}

static jstring getSystemError(JNIEnv* env, jclass clazz) {
    return (*env)->NewStringUTF(env, strerror(errno));
}

static jlong dllOpen(JNIEnv* env, jclass clazz, jstring jpath, jint flag) {
    const char* path = (*env)->GetStringUTFChars(env, jpath, 0);
    void* handle = dlopen(path, flag);
    (*env)->ReleaseStringUTFChars(env, jpath, path);
    return ptr_to_jlong(handle);
}

static jstring dllError(JNIEnv* env, jclass clazz) {
    return (*env)->NewStringUTF(env, dlerror());
}

static jint dllClose(JNIEnv* env, jclass clazz, jlong handle) {
    return dlclose(jlong_to_ptr(handle, void));
}

static jlong dllSymbolLookup(JNIEnv* env, jclass clazz, jlong handle, jstring name) {
    const char* sym = (*env)->GetStringUTFChars(env, name, 0);
    void *result = dlsym(jlong_to_ptr(handle, void), sym);
    (*env)->ReleaseStringUTFChars(env, name, sym);
    return ptr_to_jlong(result);
}

static void duplicateStringTo(JNIEnv *env, jclass clazz, jlong handle, jstring string) {
    const char *str = (*env)->GetStringUTFChars(env, string, 0);
    size_t string_length = (*env)->GetStringUTFLength(env, string);
    memcpy(jlong_to_ptr(handle, void), str, string_length + 1);
    (*env)->ReleaseStringUTFChars(env, string, str);
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
static void putJava##TYPE(JNIEnv *env, jclass clazz, jlong ptr, j##TYPE value) {    \
    memcpy(jlong_to_ptr(ptr, void), &value, sizeof(j##TYPE));                       \
}                                                                                   \
static void putJava##TYPE(JNIEnv *env, jclass clazz, jlong ptr, j##TYPE value)

DEF_PUT_FUNCS(byte);
DEF_PUT_FUNCS(int);
DEF_PUT_FUNCS(short);
DEF_PUT_FUNCS(char);
DEF_PUT_FUNCS(long);
#undef DEF_PUT_FUNCS

#define DEF_PEEK_FUNCS(TYPE)                                                         \
static j##TYPE peekJava##TYPE(JNIEnv *env, jclass clazz, jlong ptr) {                \
    j##TYPE result = 0;                                                                  \
    memcpy(jlong_to_ptr(ptr, void), &result, sizeof(result));                        \
    return result;                                                                   \
}                                                                                    \
static j##TYPE peekJava##TYPE(JNIEnv *env, jclass clazz, jlong ptr)
DEF_PEEK_FUNCS(byte);
DEF_PEEK_FUNCS(int);
DEF_PEEK_FUNCS(short);
DEF_PEEK_FUNCS(char);
DEF_PEEK_FUNCS(long);
#undef DEF_PEEK_FUNCS

const static JNINativeMethod methods[] = {
        {"alloc",             "(J)J",                   &alloc},
        {"free",              "(J)V",                   &freePointer},
        {"strerror",          "()Ljava/lang/String;",   &getSystemError},
        {"dlopen",            "(Ljava/lang/String;I)J", &dllOpen},
        {"dlerror",           "()Ljava/lang/String;",   &dllError},
        {"dlclose",           "(J)I",                   &dllClose},
        {"dlsym",             "(JLjava/lang/String;)J", &dllSymbolLookup},

        {"putByte",           "(JB)V",                  &putJavabyte},
        {"putChar",           "(JC)V",                  &putJavachar},
        {"putShort",          "(JS)V",                  &putJavashort},
        {"putInt",            "(JI)V",                  &putJavaint},
        {"putLong",           "(JJ)V",                  &putJavalong},

        {"peekByte",          "(J)B",                   &peekJavabyte},
        {"peekChar",          "(J)C",                   &peekJavachar},
        {"peekShort",         "(J)S",                   &peekJavashort},
        {"peekInt",           "(J)I",                   &peekJavaint},
        {"peekLong",          "(J)J",                   &peekJavalong},
        {"duplicateStringTo", "(JLjava/lang/String;)V", &duplicateStringTo},

        {"ffi_prep_cif",      "(JIJJ)I",                &ffiPrepareCIF}
};

static int registerMethods(JNIEnv* env) {
    jclass clazz = (*env)->FindClass(env, "net/fred/lua/foreign/internal/ForeignFunctions");
    if (clazz != NULL && (*env)->
        RegisterNatives(env, clazz, methods, sizeof(methods) / sizeof(methods[0]))
            == JNI_OK) {
        return JNI_OK;
    }
    return JNI_ERR;
}

jint JNI_OnLoad(JavaVM* vm, void* reversed) {
    jint version = JNI_ERR;
    JNIEnv *env = NULL;

    if ((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_1) == JNI_OK) {
        if (registerMethods(env) == JNI_OK) {
            version = JNI_VERSION_1_4;
        }
    }

    return version;
}

