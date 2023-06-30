#include <jni.h>
#include <stdlib.h>
#include "../common.h"

static void freePointer(JNIEnv *env, jclass clazz, jlong ptr) {
    free(jlong_to_ptr(ptr, void));
}

static jlong alloc(JNIEnv* env, jclass clazz, jlong size) {
    return ptr_to_jlong(calloc(size, 1));
}

const static JNINativeMethod methods[] = {
        {"alloc", "(J)J", &alloc},
        {"free", "(J)V", &freePointer}
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

