LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := lua
LOCAL_CFLAGS += -std=c99 -O2 -Wall -Wextra -DLUA_USE_DLOPEN
LOCAL_SRC_FILES := macro_injector.c lapi.c lauxlib.c lbaselib.c lcode.c lcorolib.c lctype.c ldblib.c ldebug.c ldo.c ldump.c lfunc.c lgc.c linit.c liolib.c llex.c lmathlib.c lmem.c loadlib.c lobject.c lopcodes.c loslib.c lparser.c lstate.c lstring.c lstrlib.c ltable.c ltablib.c ltm.c lua.c luac.c lundump.c lutf8lib.c lvm.c lzio.c
LOCAL_LDLIBS += -ldl -L$(SYSROOT)/usr/lib -llog
#LOCAL_ARM_MODE := arm
include $(BUILD_SHARED_LIBRARY) 
