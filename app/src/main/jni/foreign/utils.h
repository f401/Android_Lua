//
// Created by root on 7/17/23.
//

#include <jni.h>

#ifndef LUA_UTILS_H
#define LUA_UTILS_H 1

#ifdef __cplusplus
extern "C" {
#endif

void throwNativeException(JNIEnv *, const char *msg);
jobject pointer_create(JNIEnv *, void *);
void *pointer_get_from(JNIEnv *, jobject obj);

#define IF_NULL_RETURN(NEEDLE, expr) if (NEEDLE == NULL) return expr

#define GET_POINTER_PARAM(ENV, OUT, FROM, EXPR)                         \
void* OUT = pointer_get_from(ENV, FROM); if (OUT == (void* ) -1) return EXPR

#ifdef __cplusplus
}
#  define _EXE_ENV_METHOD(ENV, NAME, args...) ENV->NAME(args)
#else
#  define _EXE_ENV_METHOD(ENV, NAME, ...) (*ENV)->NAME(ENV, ##__VA_ARGS__)
#endif


#define FIND_CLASS(ENV, NAME, FULL_NAME) \
if (class_##NAME == NULL || _EXE_ENV_METHOD(ENV, IsSameObject, class_##NAME, NULL)) {                 \
     jclass needleClass = _EXE_ENV_METHOD(ENV, FindClass, FULL_NAME);                               \
     class_##NAME = needleClass == NULL ? NULL : (jclass) _EXE_ENV_METHOD(ENV, NewWeakGlobalRef, needleClass); \
}

#define FIND_STATIC_METHOD(ENV, CLASS, SIMPLE_NAME, NAME, DESC)                                 \
if (method_##CLASS##_##SIMPLE_NAME == NULL) {                                                   \
    method_##CLASS##_##SIMPLE_NAME = _EXE_ENV_METHOD(ENV, GetStaticMethodID, class_##CLASS, NAME, DESC); \
}

#define FIND_INSTANCE_METHOD(ENV, CLASS, SIMPLE_NAME, NAME, DESC) \
if (method_##CLASS##_##SIMPLE_NAME == NULL) {                     \
    method_##CLASS##_##SIMPLE_NAME = _EXE_ENV_METHOD(ENV, GetMethodID, class_##CLASS, NAME, DESC); \
}

#define DEFINE_CRITICAL_FUNCTION_WITH_NON_CRITICAL(FUNC_NAME, DO_IN_NON_CRITICAL, PREFIX,...) \
PREFIX critial_##FUNC_NAME(__VA_ARGS__);   /** Declare first */                                                                                            \
PREFIX nonCritial_##FUNC_NAME                                              \
(JNIEnv *env, jclass clazz, ##__VA_ARGS__) DO_IN_NON_CRITICAL                   \
PREFIX critial_##FUNC_NAME(__VA_ARGS__)

#endif //LUA_UTILS_H
