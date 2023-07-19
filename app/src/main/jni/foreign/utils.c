//
// Created by root on 7/17/23.
//

#include "utils.h"

static jclass class_logger;
static jmethodID method_logger_info;
static jmethodID method_logger_error;

#define GET_CLASS(ENV, NAME, FULL_NAME) \
if (class_##NAME == NULL || (*ENV)->IsSameObject(ENV, class_##NAME, NULL)) { \
    class_##NAME = (*ENV)->NewWeakGlobalRef(ENV, (*ENV)->FindClass(ENV, FULL_NAME));                           \
}

#define GET_STATIC_METHOD(ENV, CLASS, SIMPLE_NAME, NAME, DESC) \
if (method_##CLASS##_##SIMPLE_NAME == NULL || (*ENV)->IsSameObject(ENV, method_##CLASS##_##SIMPLE_NAME, NULL)) { \
    method_##CLASS##_##SIMPLE_NAME = (*ENV)->NewWeakGlobalRef(ENV, (*ENV)->GetStaticMethodID(ENV, class_##CLASS, NAME, DESC));                           \
}

void init_logger(JNIEnv *env) {
    GET_CLASS(env, logger, "net/fred/lua/common/Logger");
    GET_STATIC_METHOD(env, logger, info, "i", "(Ljava/lang/String;)V")
    GET_STATIC_METHOD(env, logger, error, "e", "(Ljava/lang/String;)V")
}

void logger_info(JNIEnv *env, const char *msg) {
    init_logger(env);
    (*env)->CallStaticVoidMethod(env, class_logger, method_logger_info,
                                 (*env)->NewStringUTF(env, msg));
}

void logger_error(JNIEnv *env, const char *msg) {
    init_logger(env);
    (*env)->CallStaticVoidMethod(env, class_logger, method_logger_error,
                                 (*env)->NewStringUTF(env, msg));
}

void throwNativeException(JNIEnv *env, const char *msg) {
    jclass e = (*env)->FindClass(env, "net/fred/lua/foreign/NativeMethodException");
    (*env)->ThrowNew(env, e, msg);
}
