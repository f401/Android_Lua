//
// Created by root on 7/27/23.
//
#include "utils.h"

extern "C" {
#include "../common.h"
#include <ffi.h>
}

static void *get_pointer_from_type(JNIEnv *env, jobject type) {
    static jclass class_type;
    FIND_CLASS(env, type, "net/fred/lua/foreign/types/Type");
    IF_NULL_RETURN(class_type, nullptr);

    static jmethodID method_type_get_pointer;
    FIND_INSTANCE_METHOD(env, type, get_pointer, "getFFIPointer",
                         "()Lnet/fred/lua/foreign/Pointer;");
    IF_NULL_RETURN(method_type_get_pointer, nullptr);

    return pointer_get_from(env, env->CallObjectMethod(type, method_type_get_pointer));
}

extern "C"
JNIEXPORT jint JNICALL
Java_net_fred_lua_foreign_ffi_FunctionDescriber_prep_1cif(JNIEnv *env, jobject thiz,
                                                          jobject __cif,
                                                          jobject return_type,
                                                          jobjectArray _params) {
    auto *returnType = static_cast<ffi_type *>(get_pointer_from_type(env, return_type));
    IF_NULL_RETURN(returnType, -1);

    static jmethodID method_desc_request;
    jclass class_desc = env->GetObjectClass(thiz);
    FIND_INSTANCE_METHOD(env, desc, request, "requestMemory", "(J)J");
    IF_NULL_RETURN(method_desc_request, -1);

    GET_POINTER_PARAM(env, _cif, __cif, -1);
    auto *cif = (ffi_cif *) _cif;

    //solve params
    ffi_type **params = nullptr;
    size_t params_len = 0;
    if (!(_params == nullptr || env->IsSameObject(_params, nullptr))) {
        params_len = env->GetArrayLength(_params);
        if (params_len != 0) {
            params = jlong_to_ptr(env->CallLongMethod(thiz, method_desc_request,
                                                      (jlong) params_len * sizeof(void *)),
                                  ffi_type *);
            for (jsize i = 0; i < params_len; ++i) {
                jobject curr = env->GetObjectArrayElement(_params, i);

                void *curr_ptr = get_pointer_from_type(env, curr);
                IF_NULL_RETURN(curr_ptr, -2);

                params[i] = (ffi_type *) curr_ptr;
            }
        }
    }
    return ffi_prep_cif(cif, FFI_DEFAULT_ABI, params_len, returnType, params);
}