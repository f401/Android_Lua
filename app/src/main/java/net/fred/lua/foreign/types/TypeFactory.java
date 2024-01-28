package net.fred.lua.foreign.types;

public interface TypeFactory<T extends Type<?>> {

    T create(int feature);
}
