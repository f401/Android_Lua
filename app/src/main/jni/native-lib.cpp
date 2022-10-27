#include <jni.h>
#include <string>
extern "C" {
#include "lua/lua.h"
#include "lua/lauxlib.h"
}
extern "C" JNIEXPORT jstring JNICALL
Java_com_mycompany_myapp_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    lua_State* state = luaL_newstate();
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
