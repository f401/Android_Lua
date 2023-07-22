//
// Created by root on 7/17/23.
//

#include "utils.h"
#include "../common.h"

static jclass class_logger;
static jclass class_pointer;
static jmethodID method_logger_info;
static jmethodID method_logger_error;
static jmethodID method_pointer_from;
static jmethodID method_pointer_get;

#define FIND_CLASS(ENV, NAME, FULL_NAME) \
if (class_##NAME == NULL || (*ENV)->IsSameObject(ENV, class_##NAME, NULL)) { \
     jclass needleClass = (*ENV)->FindClass(ENV, FULL_NAME);                 \
     if (needleClass == NULL) {                                              \
        class_##NAME = nullptr;        \
     } else {                                                                \
        class_##NAME = (*ENV)->NewWeakGlobalRef(ENV, needleClass);           \
     }                                                                       \
}

#define FIND_STATIC_METHOD(ENV, CLASS, SIMPLE_NAME, NAME, DESC) \
if (method_##CLASS##_##SIMPLE_NAME == NULL || (*ENV)->IsSameObject(ENV, method_##CLASS##_##SIMPLE_NAME, NULL)) { \
    jmethodID __needleMethod = (*ENV)->GetStaticMethodID(ENV, class_##CLASS, NAME, DESC);                        \
    if (__needleMethod == NULL) {                            \
        method_##CLASS##_##SIMPLE_NAME = nullptr;\
    } else {\
        method_##CLASS##_##SIMPLE_NAME = (*ENV)->NewWeakGlobalRef(ENV, __needleMethod);                              \
    }                                                 \
}


#define LOAD_CLASS_LOGGER(ENV, IF_FAILED_RETURN) \
FIND_CLASS(env, logger, "net/fred/lua/common/Logger"); \
IF_NULL_RETURN(class_logger, IF_FAILED_RETURN)

#define LOAD_CLASS_POINTER(ENV, IF_FAILED_RETURN) \
FIND_CLASS(ENV, pointer, "net/fred/lua/foreign/Pointer"); \
IF_NULL_RETURN(class_pointer, IF_FAILED_RETURN)

void logger_info(JNIEnv *env, const char *msg) {
    LOAD_CLASS_LOGGER(env,);
    FIND_STATIC_METHOD(env, logger, info, "i", "(Ljava/lang/String;)V");
    IF_NULL_RETURN(method_logger_info,);
    (*env)->CallStaticVoidMethod(env, class_logger, method_logger_info,
                                 (*env)->NewStringUTF(env, msg));
}

void logger_error(JNIEnv *env, const char *msg) {
    LOAD_CLASS_LOGGER(env,);
    FIND_STATIC_METHOD(env, logger, error, "e", "(Ljava/lang/String;)V");
    IF_NULL_RETURN(method_logger_error,);
    (*env)->CallStaticVoidMethod(env, class_logger, method_logger_error,
                                 (*env)->NewStringUTF(env, msg));
}

void throwNativeException(JNIEnv *env, const char *msg) {
    jclass e = (*env)->FindClass(env, "net/fred/lua/foreign/NativeMethodException");
    (*env)->ThrowNew(env, e, msg);
}

jobject pointer_create(JNIEnv *env, void *needle) {
    LOAD_CLASS_POINTER(env, NULL);
    FIND_STATIC_METHOD(env, pointer, from, "from", "(J)Lnet/fred/lua/foreign/Pointer;");
    IF_NULL_RETURN(method_pointer_from, NULL);
    return (*env)->CallStaticObjectMethod(env, class_pointer, method_pointer_from,
                                          ptr_to_jlong(needle));
}

void *pointer_get_from(JNIEnv *env, jobject obj) {
    LOAD_CLASS_POINTER(env, NULL);
    if (method_pointer_get == NULL || (*env)->IsSameObject(env, method_pointer_get, NULL)) {
        jmethodID id = (*env)->GetMethodID(env, class_pointer, "get", "()J");
        if (id == NULL) {
            throwNativeException(env, "Failed to init `get`");
            return NULL;
        }
        method_pointer_get = (*env)->NewWeakGlobalRef(env, id);
    }
    return jlong_to_ptr((*env)->CallLongMethod(env, obj, method_pointer_get), void);
}
