package net.fred.lua.common;

import net.fred.lua.common.utils.StringUtils;


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

    public static void checkNotEmpty(String needle, String msg) {
        if (StringUtils.isEmpty(needle)) {
            Logger.e(msg);
            throw new NullPointerException(msg);
        }
    }

    public static void checkNotLessZero(int value) {
        check(value >= 0, "Value must be greater than or equal to 0.(" + value + ")");
    }

    /**
     * See also {@see ArgumentsChecker#check}.
     * The difference is that this method uses @{link Logger#w} instead of throwing an exception.
     */
    public static void checkOrWarning(boolean cond, String msg) {
        if (!cond) {
            Logger.w(msg);
        }
    }
}
