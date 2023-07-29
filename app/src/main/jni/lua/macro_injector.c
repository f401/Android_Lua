//
// Created by root on 7/28/23.
//

#include "lauxlib.h"

LUA_API int J_luaL_dofile(lua_State *state, const char *fileName) {
    return luaL_dofile(state, fileName);
}

LUA_API int J_luaL_dostring(lua_State *state, const char *str) {
    return luaL_dostring(state, str);
}