package net.fred.lua.foreign;

import java.io.IOException;

/**
 * @inheritDoc
 */
public class NativeMethodException extends IOException {

    public NativeMethodException(String message) {
        super(message);
    }
}
