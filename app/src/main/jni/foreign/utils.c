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

#define LOAD_CLASS_POINTER(ENV, IF_FAILED_RETURN) \
FIND_CLASS(ENV, pointer, "net/fred/lua/foreign/Pointer"); \
IF_NULL_RETURN(class_pointer, IF_FAILED_RETURN)

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
    if (obj == NULL || (*env)->IsSameObject(env, obj, NULL)) {
        return NULL;
    }
    LOAD_CLASS_POINTER(env, (void *) -1);
    FIND_INSTANCE_METHOD(env, pointer, get, "get", "()J");
    IF_NULL_RETURN(method_pointer_get, (void *) -1);
    return jlong_to_ptr((*env)->CallLongMethod(env, obj, method_pointer_get), void);
}
