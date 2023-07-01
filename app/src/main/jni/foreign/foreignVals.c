#include <jni.h>
#include <stdlib.h>
#include <dlfcn.h>
#include "../common.h"

JNIEXPORT jlong JNICALL
Java_net_fred_lua_foreign_ForeignValues_getNULL(JNIEnv *env, jclass clazz) {
    return ptr_to_jlong(NULL);
}

JNIEXPORT jint JNICALL
Java_net_fred_lua_foreign_ForeignValues_getRTLD_1LAZY(JNIEnv *env, jclass clazz) {
    return RTLD_LAZY;
}

JNIEXPORT jint JNICALL
Java_net_fred_lua_foreign_ForeignValues_getRTLD_1GLOBAL(JNIEnv *env, jclass clazz) {
    return RTLD_GLOBAL;
}

JNIEXPORT jint JNICALL
Java_net_fred_lua_foreign_ForeignValues_getRTLD_1NOW(JNIEnv *env, jclass clazz) {
    return RTLD_NOW;
}

JNIEXPORT jint JNICALL
Java_net_fred_lua_foreign_ForeignValues_getRTLD_1LOCAL(JNIEnv *env, jclass clazz) {
    return RTLD_LOCAL;
}