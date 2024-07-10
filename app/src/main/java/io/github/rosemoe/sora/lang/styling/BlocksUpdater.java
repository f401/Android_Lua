package io.github.rosemoe.sora.lang.styling;

import java.util.Iterator;
import java.util.List;

/**
 * Update block line positions on edit
 */
public class BlocksUpdater {
    /**
     * Update blocks
     *
     * @param blocks   Block lines to update
     * @param restrict Min line to update
     * @param delta    Delta for line index
     */
    public static void update(List<CodeBlock> blocks, int restrict, int delta) {
        if (delta == 0) {
            return;
        }
        Iterator<CodeBlock> itr = blocks.iterator();
        while (itr.hasNext()) {
            CodeBlock block = itr.next();
            if (block.startLine >= restrict) {
                block.startLine += delta;
            }
            if (block.endLine >= restrict) {
                block.endLine += delta;
            }
            if (block.startLine >= block.endLine) {
                itr.remove();
            }
        }
    }
}
