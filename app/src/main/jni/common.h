#ifndef __COMMON_H_
#define __COMMON_H_

#define ptr_to_jlong(ptr) ((jlong)(unsigned long long)(ptr))
#define jlong_to_ptr(ptr, type) ((type*)(unsigned long long)(ptr))

#endif
