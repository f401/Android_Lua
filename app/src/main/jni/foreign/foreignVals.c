#include <jni.h>
#include <stdlib.h>
#include <dlfcn.h>
#include "../common.h"
#include <ffi.h>

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

JNIEXPORT jlong JNICALL
Java_net_fred_lua_foreign_ForeignValues_getFFI_1TYPE_1INT16(JNIEnv *env, jclass clazz) {
    return ptr_to_jlong(&ffi_type_sint16);
}

JNIEXPORT jlong JNICALL
Java_net_fred_lua_foreign_ForeignValues_getFFI_1TYPE_1INT32(JNIEnv *env, jclass clazz) {
    return ptr_to_jlong(&ffi_type_sint32);
}

JNIEXPORT jlong JNICALL
Java_net_fred_lua_foreign_ForeignValues_getFFI_1TYPE_1INT64(JNIEnv *env, jclass clazz) {
    return ptr_to_jlong(&ffi_type_sint64);
}

JNIEXPORT jlong JNICALL
Java_net_fred_lua_foreign_ForeignValues_getFFI_1TYPE_1INT8(JNIEnv *env, jclass clazz) {
    return ptr_to_jlong(&ffi_type_sint8);
}

JNIEXPORT jlong JNICALL
Java_net_fred_lua_foreign_ForeignValues_getFFI_1TYPE_1POINTER(JNIEnv *env, jclass clazz) {
    return ptr_to_jlong(&ffi_type_pointer);
}

JNIEXPORT jlong JNICALL
Java_net_fred_lua_foreign_ForeignValues_getFFI_1TYPE_1VOID(JNIEnv *env, jclass clazz) {
    return ptr_to_jlong(&ffi_type_void);
}

JNIEXPORT jlong JNICALL
Java_net_fred_lua_foreign_ForeignValues_sizeOfFFI_1CIF(JNIEnv *env, jclass clazz) {
    return sizeof(ffi_cif);
}

JNIEXPORT jlong JNICALL
Java_net_fred_lua_foreign_ForeignValues_sizeOfPointer(JNIEnv *env, jclass clazz) {
    return sizeof(void *);
}

JNIEXPORT jint JNICALL
Java_net_fred_lua_foreign_ForeignValues_getFFI_1STATUS_1OK(JNIEnv *env, jclass clazz) {
    return FFI_OK;
}