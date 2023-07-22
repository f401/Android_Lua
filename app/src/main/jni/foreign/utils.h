//
// Created by root on 7/17/23.
//

#include <jni.h>

#ifndef LUA_UTILS_H
#define LUA_UTILS_H 1

#ifdef __cplusplus
extern "C" {
#endif

void logger_info(JNIEnv *, const char *msg);

void logger_error(JNIEnv *, const char *msg);

void throwNativeException(JNIEnv *, const char *msg);

jobject pointer_create(JNIEnv *, void *);

void *pointer_get_from(JNIEnv *, jobject obj);

#define IF_NULL_RETURN(NEEDLE, expr) if (NEEDLE == NULL) return expr

#ifdef __cplusplus
}
#endif

#endif //LUA_UTILS_H
