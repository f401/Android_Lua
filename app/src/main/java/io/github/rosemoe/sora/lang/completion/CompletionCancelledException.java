package io.github.rosemoe.sora.lang.completion;

public class CompletionCancelledException extends RuntimeException {
    public CompletionCancelledException() {
    }

    public CompletionCancelledException(String message) {
        super(message);
    }
}
