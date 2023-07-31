LOCAL_PATH := $(call my-dir)

include $(FFI)

include $(CLEAR_VARS)
LOCAL_SRC_FILES        := foreignFuncs.cpp foreignVals.c utils.c libffi.cpp breakpad.cpp
LOCAL_MODULE           := foreign
LOCAL_SHARED_LIBRARIES := libffi
LOCAL_STATIC_LIBRARIES += breakpad_client
LOCAL_LDLIBS           += -ldl -llog
LOCAL_CFLAGS           += $(FFI_INCLUDES)
LOCAL_C_INCLUDES       += $(FFI_C_INCLUDES) $(BREAKPAD_INCLUDES)
include $(BUILD_SHARED_LIBRARY)