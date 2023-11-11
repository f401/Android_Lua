//
// Created by root on 2023/7/31.
//

#include <jni.h>
#include <client/linux/handler/exception_handler.h>
#include <sys/prctl.h>
#include "utils.h"
#include "client/linux/minidump_writer/linux_ptrace_dumper.h"
#include <android/log.h>
#include <unistd.h>

static JavaVM *g_vm;

static bool
dumpCallback(const google_breakpad::MinidumpDescriptor &descriptor, void *, bool succeed) {
    __android_log_print(ANDROID_LOG_ERROR, "Error", "Dump path: %s\n", descriptor.path());
    int latest = open((descriptor.directory() + "/latest").data(), O_WRONLY | O_CREAT, 0666);
    write(latest, descriptor.path(), strlen(descriptor.path()));
    close(latest);
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

    sigset_t unblocked;
    sigprocmask(SIG_UNBLOCK, nullptr, &unblocked);
    sigaddset(&unblocked, SIGSEGV);
    sigprocmask(SIG_UNBLOCK, &unblocked, nullptr);

    init(save_path);
    env->ReleaseStringUTFChars(crash_save_path, save_path);
}

extern "C"
JNIEXPORT void JNICALL
Java_net_fred_lua_foreign_Breakpad_SEND_1SIGNAL_1SEGV(JNIEnv *env, jclass clazz) {
    char *c = nullptr;
    *c = 'a';
}