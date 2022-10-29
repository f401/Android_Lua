#include "CStandardOutputInput.h"
#include <stdio.h>
#include "../common.h"


JNIEXPORT jlong JNICALL Java_net_fred_lua_jni_CStandardOutputInput_getStandardOutPointer
  (JNIEnv *env, jobject thiz) {
	return ptr_to_jlong(stdout);
  }

JNIEXPORT jlong JNICALL Java_net_fred_lua_jni_CStandardOutputInput_getStandardErrPointer
  (JNIEnv *env, jobject thiz) {
	  return ptr_to_jlong(stderr);
  }
JNIEXPORT jlong JNICALL Java_net_fred_lua_jni_CStandardOutputInput_getStandardInPointer
  (JNIEnv *env, jobject thiz) {
	  return ptr_to_jlong(stdin);
  }
  
  //set stdout ptr
JNIEXPORT void JNICALL Java_net_fred_lua_jni_CStandardOutputInput_setStandardOutPointer
  (JNIEnv *env, jobject thiz, jlong ptr) {
	  stdout = jlong_to_ptr(ptr, FILE);
  }
  
//set stderr ptr
JNIEXPORT void JNICALL Java_net_fred_lua_jni_CStandardOutputInput_setStandardErrPointer
  (JNIEnv *env, jobject thiz, jlong ptr) {
      stderr = jlong_to_ptr(ptr, FILE);
  }
  
//set stdin ptr
JNIEXPORT void JNICALL Java_net_fred_lua_jni_CStandardOutputInput_setStandardInPointer
  (JNIEnv *env, jobject thiz, jlong ptr) {
      stdin = jlong_to_ptr(ptr, FILE);
  }

//redirect stdout
JNIEXPORT void JNICALL Java_net_fred_lua_jni_CStandardOutputInput_redirectStandardOutTo
(JNIEnv *env, jobject thiz, jstring path) {
    
}

//redirect stderr
JNIEXPORT void JNICALL Java_net_fred_lua_jni_CStandardOutputInput_redirectStandardErrTo
  (JNIEnv *env, jobject thiz, jstring path) {
      
  }

//redirect stdin
JNIEXPORT void JNICALL Java_net_fred_lua_jni_CStandardOutputInput_redirectStandardInTo
  (JNIEnv *env, jobject thiz, jstring path) {
      
  }

JNIEXPORT void JNICALL Java_net_fred_lua_jni_CStandardOutputInput_closePointer
  (JNIEnv *env, jobject thiz, jlong ptr) {
      fclose(jlong_to_ptr(ptr, FILE));
  }
