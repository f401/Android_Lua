//
// Created by root on 2023/7/31.
//

#include <jni.h>
#include <client/linux/handler/exception_handler.h>
#include "utils.h"
#include <android/log.h>

static JavaVM *g_vm;

static bool
dumpCallback(const google_breakpad::MinidumpDescriptor &descriptor, void *, bool succeed) {
    __android_log_print(ANDROID_LOG_ERROR, "Error", "Dump path: %s\n", descriptor.path());
    return succeed;
}

static void init(const char *path) {
    static google_breakpad::MinidumpDescriptor descriptor(path);
    static google_breakpad::ExceptionHandler handler(descriptor,
                                                     nullptr, &dumpCallback,
                                                     nullptr, true, -1);
}

extern "C"
JNIEXPORT void JNICALL
Java_net_fred_lua_foreign_Breakpad_init(JNIEnv *env, jclass clazz, jstring crash_save_path) {
    env->GetJavaVM(&g_vm);
    const char *save_path = env->GetStringUTFChars(crash_save_path, JNI_FALSE);
    init(save_path);
    env->ReleaseStringUTFChars(crash_save_path, save_path);
}

extern "C"
JNIEXPORT void JNICALL
Java_net_fred_lua_foreign_Breakpad_SEND_1SIGNAL_1SEGV(JNIEnv *env, jclass clazz) {
    char *ptr = nullptr;
    *ptr = 89;
}