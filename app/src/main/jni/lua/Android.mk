LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := lua
LOCAL_CFLAGS += -std=c99 -O2 -Wall -Wextra -DLUA_USE_DLOPEN
LOCAL_SRC_FILES := lapi.c ldo.c loadlib.c ltablib.c lauxlib.c ldump.c lobject.c ltm.c lbaselib.c lfunc.c lopcodes.c lua.c lbitlib.c lgc.c loslib.c luac.c lcode.c linit.c lparser.c lundump.c lcorolib.c liolib.c lstate.c lutf8lib.c lctype.c llex.c lstring.c lvm.c ldblib.c lmathlib.c lstrlib.c lzio.c ldebug.c lmem.c ltable.c
LOCAL_LDLIBS += -ldl -L$(SYSROOT)/usr/lib -llog
#LOCAL_ARM_MODE := arm
include $(BUILD_SHARED_LIBRARY) 
