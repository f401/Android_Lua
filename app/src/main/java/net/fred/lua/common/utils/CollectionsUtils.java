package net.fred.lua.common.utils;

import androidx.annotation.NonNull;

public class CollectionsUtils {

    /**
     * Determine whether @{code src} contains any elements from @{code needle}
     *
     * @param src    See above.
     * @param needle See above.
     * @return If it contains, return true. vice versa.
     */
    public static boolean containsOneOf(Object[] src, @NonNull Object... needle) {
        for (Object item : src) {
            for (Object need : needle) {
                if (need.equals(needle)) {
                    return true;
                }
            }
        }
        return false;
    }
}
