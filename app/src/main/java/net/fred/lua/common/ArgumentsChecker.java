package net.fred.lua.common;

import net.fred.lua.common.utils.FileUtils;
import net.fred.lua.common.utils.StringUtils;
import net.fred.lua.io.Logger;


public class ArgumentsChecker {

    /**
     * Ensure {@code cond} is true, otherwise throw an exception with {@code msg}.
     *
     * @param cond Required conditions.
     * @param msg  Information attached when throwing an exception.
     */
    public static void check(boolean cond, String msg) {
        if (!cond) {
            Logger.e(msg);
            throw new IllegalArgumentException(msg);
        }
    }

    public static void checkStringNotNullOrEmpty(String needle, String msg) {
        if (StringUtils.isEmpty(needle)) {
            Logger.e(msg);
            throw new NullPointerException(msg);
        }
    }

    public static void checkNotNull(Object needle, String msg) {
        if (needle == null) {
            throw new NullPointerException(msg);
        }
    }

    public static void checkSize(int value) {
        check(value >= 0, "Value must be greater than or equal to 0.(" + value + ")");
    }

    public static void checkFileExists(String path, String msg) {
        check(FileUtils.exists(path), msg);
    }

    public static void checkIndex(int idx, long size) {
        if (idx < 0 || idx > size - 1) {
            throw new IndexOutOfBoundsException("Size: " + size + ", but index: " + idx);
        }
    }

    public static void checkState(boolean state, String msg) {
        if (!state) {
            throw new IllegalStateException(msg);
        }
    }
}
