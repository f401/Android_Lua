//
// Created by root on 7/27/23.
//
#include "utils.h"

extern "C" {
#include "../common.h"
#include <ffi.h>
#include <stdlib.h>
}

static jclass class_type;

#define EQUAL_TO_NULL(ENV, NEEDLE) (NEEDLE == nullptr || env->IsSameObject(NEEDLE, nullptr))

#define LOAD_CLASS_TYPE(ENV, EXPR) \
FIND_CLASS(ENV, type, "net/fred/lua/foreign/types/Type"); \
IF_NULL_RETURN(class_type, EXPR)

static void *get_pointer_from_type(JNIEnv *env, jobject type) {
    LOAD_CLASS_TYPE(env, nullptr);

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
    if (!(EQUAL_TO_NULL(env, _params))) {
        params_len = env->GetArrayLength(_params);
        if (params_len != 0) {
            params = jlong_to_ptr(env->CallLongMethod(thiz, method_desc_request,
                                                      (jlong) params_len * sizeof(void *)),
                                  ffi_type *);
            for (jsize i = 0; i < params_len; ++i) {
                jobject curr = env->GetObjectArrayElement(_params, i);

                void *curr_ptr = get_pointer_from_type(env, curr);
                IF_NULL_RETURN(curr_ptr, -2);
                env->DeleteLocalRef(curr);

                params[i] = (ffi_type *) curr_ptr;
            }
        }
    }
    return ffi_prep_cif(cif, FFI_DEFAULT_ABI, params_len, returnType, params);
}
extern "C"
JNIEXPORT void JNICALL
Java_net_fred_lua_foreign_ffi_FunctionCaller_ffi_1call(JNIEnv *env, jobject thiz, jobject _cif,
                                                       jobject _func_address,
                                                       jobject _return_segment,
                                                       jobjectArray _typed_params,// describer.getParams()
                                                       jobjectArray _params) {
    jclass class_functionCaller = env->GetObjectClass(thiz);
    static jmethodID method_functionCaller_evalTotalSize;
    FIND_INSTANCE_METHOD(env, functionCaller, evalTotalSize, "evalTotalSize",
                         "([Ljava/lang/Object;)J");
    IF_NULL_RETURN(method_functionCaller_evalTotalSize,);

    void **params_ptr = nullptr;
    jsize length = 0;
    //assign params data
    if (!(EQUAL_TO_NULL(env, _params) || (length = env->GetArrayLength(_typed_params)) == 0)) {
        char *params_data = (char *) alloca(
                env->CallLongMethod(thiz, method_functionCaller_evalTotalSize, _params));
        params_ptr = (void **) alloca(sizeof(void *) * length);
        jint off = 0;

        LOAD_CLASS_TYPE(env,);
        static jmethodID method_type_write, method_type_getSize;
        FIND_INSTANCE_METHOD(env, type, getSize, "getSize", "(Ljava/lang/Object;)I");
        IF_NULL_RETURN(method_type_getSize,);
        FIND_INSTANCE_METHOD(env, type, write, "write",
                             "(Lnet/fred/lua/foreign/Pointer;Ljava/lang/Object;)V");
        IF_NULL_RETURN(method_type_write,);
        for (jsize i = 0; i < length; ++i) {
            jobject typed = env->GetObjectArrayElement(_typed_params, i);
            jobject param = env->GetObjectArrayElement(_params, i);

            void *dest_ptr = params_data + off;
            params_ptr[i] = dest_ptr;

            jobject dest = pointer_create(env, dest_ptr);
            env->CallVoidMethod(typed, method_type_write, dest, param);
            env->DeleteLocalRef(dest);

            off += env->CallIntMethod(typed, method_type_getSize, param);

            env->DeleteLocalRef(typed);
            env->DeleteLocalRef(param);
        }
    }
    GET_POINTER_PARAM(env, cif, _cif,);
    GET_POINTER_PARAM(env, func_addr, _func_address,);
    GET_POINTER_PARAM(env, ret_seg, _return_segment,);
    ffi_call((ffi_cif *) cif, (void (*)()) func_addr, ret_seg, params_ptr);
}