LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_SRC_FILES        := foreignFuncs.cpp foreignVals.c utils.c libffi.cpp breakpad.cpp MemoryAccessor.cpp
LOCAL_MODULE           := foreign
LOCAL_SHARED_LIBRARIES := ffi breakpad_client
LOCAL_LDLIBS            :=  -llog -ldl
include $(BUILD_SHARED_LIBRARY)
