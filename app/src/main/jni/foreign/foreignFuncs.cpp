#include <jni.h>
#include <string>
#include <android/log.h>
#include "MemoryAccessor.h"

extern "C" {
#include <stdlib.h>
#include <dlfcn.h>
#include <string.h>
#include <errno.h>
#include "../common.h"
#include "utils.h"
}

/*
 * These functions may be called multiple times, so dynamic registration is used.
 */
#define ERROR_DL_OPEN_FAILED "DLL open failed: "
#define ERROR_DL_LOAD_SYMBOL_FAILED "Can't find symbol: "

static void FreePointer(JNIEnv *env, jclass clazz, jobject ptr) {
    GET_POINTER_PARAM(env, dst, ptr,);
    free(dst);
}

static jobject AllocateSegment(JNIEnv *env, jclass clazz, jlong size) {
    void *handle = calloc(size, 1);
    if (handle == nullptr) {
        std::string msg("Failed to alloc ");
        msg.append(std::to_string(size));
        msg.append(" data, reason: ");
        msg.append(strerror(errno));
        throwNativeException(env, msg.data());
        return nullptr;
    }
    return pointer_create(env, handle);
}

static jobject OpenDll(JNIEnv *env, jclass clazz, jstring jpath, jint flag) {
    const char *path = env->GetStringUTFChars(jpath, JNI_FALSE);

    void *handle = dlopen(path, flag);

    if (handle == nullptr) {
        std::string msg(ERROR_DL_OPEN_FAILED);
        msg.append(dlerror());
        throwNativeException(env, msg.data());
        env->ReleaseStringUTFChars(jpath, path);
        return nullptr;
    }

    env->ReleaseStringUTFChars(jpath, path);
    return pointer_create(env, handle);
}

static void CopyMemory(JNIEnv *env, jclass clazz, jobject _dest, jobject _src, jlong length) {
    GET_POINTER_PARAM(env, dest, _dest,);
    GET_POINTER_PARAM(env, src, _src,);
    memcpy(dest, src, length);
}

static jint CloseDll(JNIEnv *env, jclass clazz, jobject handle) {
    GET_POINTER_PARAM(env, dst, handle, -1);
    return dlclose(dst);
}

static jobject LookUpSymbol(JNIEnv *env, jclass clazz, jobject handle, jstring name) {
    const char *sym = env->GetStringUTFChars(name, JNI_FALSE);
    GET_POINTER_PARAM(env, dst, handle, nullptr);
    void *result = dlsym(dst, sym);

    if (result == nullptr) {
        std::string msg(ERROR_DL_LOAD_SYMBOL_FAILED);
        msg.append(sym).append(".Reason: ").append(dlerror());
        throwNativeException(env, msg.data());
        return nullptr;
    }

    env->ReleaseStringUTFChars(name, sym);
    return pointer_create(env, result);
}

//----------------------------------------------------------value handles-----------------------------------------------//



const static JNINativeMethod dllMethods[] = {
        {"dlopen",  "(Ljava/lang/String;I)Lnet/fred/lua/foreign/Pointer;",                              (void *) &OpenDll},
        {"dlclose", "(Lnet/fred/lua/foreign/Pointer;)I",                                                (void *) &CloseDll},
        {"dlsym",
                    "(Lnet/fred/lua/foreign/Pointer;Ljava/lang/String;)Lnet/fred/lua/foreign/Pointer;", (void *) &LookUpSymbol}
};

const static JNINativeMethod seg_methods[] = {
        {"alloc", "(J)Lnet/fred/lua/foreign/Pointer;", (void *) &AllocateSegment},
        {"free", "(Lnet/fred/lua/foreign/Pointer;)V", (void *) &FreePointer},
        {"memcpy", "(Lnet/fred/lua/foreign/Pointer;Lnet/fred/lua/foreign/Pointer;J)V",
         (void *) &CopyMemory},
};

static int registerMethods(JNIEnv *env) {
    jclass dll = env->FindClass("net/fred/lua/foreign/core/DynamicLoadingLibrary");
    jclass seg = env->FindClass("net/fred/lua/foreign/internal/MemorySegment");
    if (registerMemoryAccessorFunctions(env) == JNI_ERR ||
        env->RegisterNatives(dll, dllMethods, sizeof(dllMethods) / sizeof(dllMethods[0])) ==
        JNI_ERR ||
        env->RegisterNatives(seg, seg_methods, sizeof(seg_methods) / sizeof(seg_methods[0])) ==
        JNI_ERR) {
        throwNativeException(env, "Failed to register natives.");
        return JNI_ERR;
    }
    return JNI_OK;
}

extern "C"
jint JNI_OnLoad(JavaVM *vm, void *reversed) {
    jint version = JNI_ERR;
    JNIEnv *env = nullptr;

    if (vm->GetEnv((void **) &env, JNI_VERSION_1_1) == JNI_OK &&
        registerMethods(env) == JNI_OK) {
        version = JNI_VERSION_1_6;
    }

    return version;
}
