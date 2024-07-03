package net.fred.lua.editor.text;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Content implements CharSequence {

    public final static int DEFAULT_MAX_UNDO_STACK_SIZE = 500;
    public final static int DEFAULT_LIST_CAPACITY = 1000;

    @Nullable
    private final ReadWriteLock mLock;

    public Content(boolean threadSafe) {
        this.mLock = threadSafe ? new ReentrantReadWriteLock() : null;
    }

    protected void lock(@NonNull LockType type) {
        if (mLock != null) {
            if (type == LockType.READ_LOCK) {
                this.mLock.readLock().lock();
            } else if (type == LockType.WRITE_LOCK) {
                this.mLock.writeLock().lock();
            }
        }
    }

    protected void unlock(@NonNull LockType type) {
        if (mLock != null) {
            if (type == LockType.READ_LOCK) {
                this.mLock.readLock().unlock();
            } else if (type == LockType.WRITE_LOCK) {
                this.mLock.writeLock().unlock();
            }
        }
    }

    @Override
    public int length() {
        return 0;
    }

    @Override
    public char charAt(int index) {
        return 0;
    }

    @NonNull
    @Override
    public CharSequence subSequence(int start, int end) {
        return null;
    }

    protected enum LockType {
        READ_LOCK, WRITE_LOCK
    }

    public interface OnContentChangeListener {

    }

}