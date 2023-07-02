package net.fred.lua;

//LuaState
@Deprecated
public class LuaState implements AutoCloseable {

    private long ptr;
    private boolean opened;

    public LuaState() {
        this(true);
    }

    public LuaState(boolean newState) {
        this(newState, true);
    }

    public LuaState(boolean newState, boolean openLibs) {
        if (newState) newState();
        if (openLibs) openlibs();
    }

    @Override
    public void close() {
        nativeClose(ptr);
    }

    public void newState() {
        if (opened)
            nativeClose(ptr);
        ptr = nativeNewState();
        opened = true;
    }

    public int getGlobal(String name) {
        return nativeGetGlobal(ptr, name);
    }

    public void pushNumber(int number) {
        nativePushNumber(ptr, number);
    }

    public void call(int paramsNums, int returnNums) {
        nativeCall(ptr, paramsNums, returnNums);
    }

    public int toInteger(int stack) {
        return nativeToInteger(ptr, stack);
    }

    public void pop(int stack) {
        nativePop(ptr, stack);
    }

    public void openlibs() {
        nativeOpenlibs(ptr);
    }

    public int dofile(String file) {
        return nativeDofile(ptr, file);
    }

    private native void nativeClose(long ptr);

    private native long nativeNewState();

    private native int nativeGetGlobal(long ptr, String name);

    private native void nativePushNumber(long ptr, int number);

    private native int nativeToInteger(long ptr, int stack);

    private native void nativeCall(long ptr, int paramsNums, int returnNums);

    private native void nativePop(long ptr, int stack);

    private native void nativeOpenlibs(long ptr);

    private native int nativeDofile(long ptr, String path);

    static {
        System.loadLibrary("bridge");
    }
}
