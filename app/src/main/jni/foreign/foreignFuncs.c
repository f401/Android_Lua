#include <jni.h>
#include <stdlib.h>
#include <dlfcn.h>
#include <string.h>
#include <errno.h>
#include "../common.h"

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

const static JNINativeMethod methods[] = {
        {"alloc", "(J)J", &alloc},
        {"free", "(J)V", &freePointer},
        {"strerror", "()Ljava/lang/String;", &getSystemError},
        {"dlopen", "(Ljava/lang/String;I)J", &dllOpen},
        {"dlerror", "()Ljava/lang/String;", &dllError},
        {"dlclose", "(J)I", &dllClose},
        {"dlsym", "(JLjava/lang/String;)J", &dllSymbolLookup}
};

static int registerMethods(JNIEnv* env) {
    jclass clazz = (*env)->FindClass(env, "net/fred/lua/foreign/ForeignFunctions");
    if (clazz != NULL && (*env)->
        RegisterNatives(env, clazz, methods, sizeof(methods) / sizeof(methods[0]))
            == JNI_OK) {
        return JNI_OK;
    }
    return JNI_ERR;
}

jint JNI_OnLoad(JavaVM* vm, void* reversed) {
    int version = JNI_ERR;
    JNIEnv *env = NULL;

    if ((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_1) == JNI_OK) {
        if (registerMethods(env) == JNI_OK) {
            version = JNI_VERSION_1_4;
        }
    }
    return version;
}

