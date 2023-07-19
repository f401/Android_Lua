//
// Created by root on 7/17/23.
//

#include <jni.h>

#ifndef LUA_UTILS_H
#define LUA_UTILS_H 1

#ifdef __cplusplus
extern "C" {
#endif

void init_logger(JNIEnv *env);

void logger_info(JNIEnv *, const char *msg);

void logger_error(JNIEnv *, const char *msg);

void throwNativeException(JNIEnv *, const char *msg);

#ifdef __cplusplus
}
#endif

#endif //LUA_UTILS_H
