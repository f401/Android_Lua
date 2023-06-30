LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_SHARED_LIBRARIES := liblua
LOCAL_MODULE    := bridge
LOCAL_SRC_FILES := bridge.c cstdouterr.c foreignFuncs.c
include $(BUILD_SHARED_LIBRARY)
