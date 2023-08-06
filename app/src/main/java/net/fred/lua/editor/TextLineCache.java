package net.fred.lua.editor;

import net.fred.lua.common.ArgumentsChecker;
import net.fred.lua.common.Pair;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A class that caches the number of text lines and offsets.
 * The key is the number of rows, and the value is the corresponding offset.
 */
public class TextLineCache {
    private final LinkedHashMap<Integer, Integer> map;
    private final int maxCount;

    public TextLineCache(int maxCount) {
        ArgumentsChecker.checkSize(maxCount);
        this.maxCount = maxCount;
        this.map = new LinkedHashMap<>(maxCount, 0.75f, true);
        // The third parameter is very important.
        this.map.put(0, 0);
    }

    /**
     * Get the closest offset of the number of rows.
     * If a value was returned, it is moved to the head of the queue.
     *
     * @param line Number of rows to obtain.
     * @return The first is the nearest number of rows, and the second is the corresponding nearest offset.
     */
    public Pair<Integer, Integer> getNearestLine(int line) {
        ArgumentsChecker.checkSize(line);
        int nearestDistance = Integer.MAX_VALUE;
        int nearestMatch = 0;
        Set<Integer> keySet = map.keySet();
        for (Integer current : keySet) {
            int distance = Math.abs(current - line);
            if (distance < nearestDistance) {
                nearestMatch = current;
                nearestDistance = distance;
            }
        }
        return Pair.makePair(nearestMatch, map.get(nearestMatch));
    }

    public void put(int line, int off) {
        ArgumentsChecker.checkSize(line);
        ArgumentsChecker.checkSize(off);
        map.put(line, off);
        trimToSize();
    }

    private void trimToSize() {
        if (maxCount < map.size()) {
            Map.Entry<Integer, Integer> toEvict = map.entrySet().iterator().next();
            map.remove(toEvict.getKey());
        }
    }

    public void cleanAll() {
        map.clear();
        map.put(0, 0);
    }

    public void invalidateCache(int fromCharOffset) {
        Set<Map.Entry<Integer, Integer>> entrySet = map.entrySet();
        for (Map.Entry<Integer, Integer> curr : entrySet) {
            if (curr.getValue() >= fromCharOffset) {
                map.remove(curr.getKey());
            }
        }
    }
}
