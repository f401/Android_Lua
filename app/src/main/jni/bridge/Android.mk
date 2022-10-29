LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
#LOCAL_STATIC_LIBRARIES := liblua
LOCAL_SHARED_LIBRARIES := liblua
LOCAL_MODULE    := bridge
LOCAL_SRC_FILES := bridge.c
include $(BUILD_SHARED_LIBRARY)
