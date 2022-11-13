#include <jni.h>
#include <stdio.h>
#include "../common.h"

//redirect stdout
JNIEXPORT void JNICALL Java_net_fred_lua_io_CStandardOutputInput_redirectStandardOutTo
(JNIEnv *env, jobject thiz, jstring path) {
	const char* cpath = (*env)->GetStringUTFChars(env, path, 0);
	freopen(cpath, "w", stdout);
	(*env)->ReleaseStringUTFChars(env, path, cpath);
}

//redirect stderr
JNIEXPORT void JNICALL Java_net_fred_lua_io_CStandardOutputInput_redirectStandardErrTo
  (JNIEnv *env, jobject thiz, jstring path) {
      const char* cpath = (*env)->GetStringUTFChars(env, path, 0);
      freopen(cpath, "w", stderr);
      (*env)->ReleaseStringUTFChars(env, path, cpath);
  }

//redirect stdin
JNIEXPORT void JNICALL Java_net_fred_lua_io_CStandardOutputInput_redirectStandardInTo
  (JNIEnv *env, jobject thiz, jstring path) {
      const char* cpath = (*env)->GetStringUTFChars(env, path, 0);
      freopen(cpath, "r", stdin);
      (*env)->ReleaseStringUTFChars(env, path, cpath);
  }

