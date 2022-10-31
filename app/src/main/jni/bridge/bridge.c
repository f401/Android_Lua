#include "../lua/lua.h"
#include "../lua/lauxlib.h"
#include "../lua/lualib.h"
#include <jni.h>
#include "../common.h"

#define jlong_to_luastate(ptr) ((lua_State*)ptr)

JNIEXPORT jlong JNICALL Java_net_fred_lua_jni_lua_LuaJavaBridge_nativeNewState
  (JNIEnv * env, jobject thiz) {
	  lua_State* state = luaL_newstate();
	  return ptr_to_jlong(state);
  }

JNIEXPORT void JNICALL Java_net_fred_lua_jni_lua_LuaJavaBridge_nativeClose
  (JNIEnv *env, jobject thiz, jlong ptr) {
	  lua_close(jlong_to_luastate(ptr));
  }

JNIEXPORT jint JNICALL Java_net_fred_lua_jni_lua_LuaJavaBridge_nativeGetGlobal
  (JNIEnv *env, jobject thiz, jlong ptr, jstring name) {
	const char* n = (*env)->GetStringUTFChars(env, name, 0);
	int result = lua_getglobal(jlong_to_luastate(ptr), n);
	(*env)->ReleaseStringUTFChars(env, name, n);
    return result;
  }

JNIEXPORT void JNICALL Java_net_fred_lua_jni_lua_LuaJavaBridge_nativePushNumber
  (JNIEnv *env, jobject thiz, jlong ptr, jint num) {
	  lua_pushnumber(jlong_to_luastate(ptr), num);
  }

JNIEXPORT jint JNICALL Java_net_fred_lua_jni_lua_LuaJavaBridge_nativeToInteger
  (JNIEnv *env, jobject thiz, jlong ptr, jint stack) {
	return lua_tointeger(jlong_to_luastate(ptr), stack);
  }

JNIEXPORT void JNICALL Java_net_fred_lua_jni_lua_LuaJavaBridge_nativeCall
  (JNIEnv *env, jobject thiz, jlong ptr, jint par, jint ret) {
	  lua_call(jlong_to_luastate(ptr), par, ret);
  }

JNIEXPORT void JNICALL Java_net_fred_lua_jni_lua_LuaJavaBridge_nativePop
  (JNIEnv *env, jobject thiz, jlong ptr, jint stack) {
	lua_pop(jlong_to_luastate(ptr), stack);

  }

JNIEXPORT void JNICALL Java_net_fred_lua_jni_lua_LuaJavaBridge_nativeOpenlibs
  (JNIEnv *env, jobject thiz, jlong ptr) {
	luaL_openlibs(jlong_to_luastate(ptr));
  }

JNIEXPORT int JNICALL Java_net_fred_lua_jni_lua_LuaJavaBridge_nativeDofile
  (JNIEnv * env, jobject thiz, jlong ptr, jstring name) {
	  const char* n = (*env)->GetStringUTFChars(env, name, 0);
	  int result = luaL_dofile(jlong_to_luastate(ptr), n);
	  (*env)->ReleaseStringUTFChars(env, name, n);
      return result;
  }
