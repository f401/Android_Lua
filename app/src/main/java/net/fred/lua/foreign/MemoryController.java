package net.fred.lua.foreign;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.ForOverride;

import net.fred.lua.foreign.child.IChildPolicy;
import net.fred.lua.foreign.child.SimpleChildHolder;

import java.io.Closeable;
import java.util.concurrent.atomic.AtomicBoolean;

public class MemoryController implements Closeable {
    private static final String TAG = "MemoryController";
    private final AtomicBoolean mIsDisposed;

    /*** Contains objects that need to be released together when this object is released.} */
    private IChildPolicy mChildPolicy;
    @Nullable
    private MemoryController mParent;

    public MemoryController() {
        this.mIsDisposed = new AtomicBoolean(false);
        this.mChildPolicy = new SimpleChildHolder(this);
    }

    /*** Release this object. After releasing it, you cannot touch it at all. */
    @Override
    public final void close() throws NativeMethodException {
        if (askParentToAllowChildRelease(this)
                && this.mIsDisposed.compareAndSet(false, true)) {
                doClose(false);
        } else {
            Log.e(TAG, "Pointer freed twice " + this);
        }
    }

    private void doClose(boolean finalized) throws NativeMethodException {
        dispose(finalized);
        mChildPolicy.closeAllChild();
        if (hasParent()) {
            // Unbind us from the parent
            mParent.removeChild(this);
            mParent = null;
        }
    }

    /**
     * Determine if it has been disposed.
     *
     * @return Has it been disposed.
     */
    public final boolean isClosed() {
        return this.mIsDisposed.get();
    }

    /**
     * Obtain the son based on the provided index.
     * @param idx The index.
     * @return The son.
     * @see IChildPolicy#childAt(int)
     * @exception IndexOutOfBoundsException Throw when the provided quantity is greater than the maximum number of sub items.
     */
    public final AutoCloseable childAt(int idx) {
        return mChildPolicy.childAt(idx);
    }

    /**
     * Returns whether the current object has children.
     * @return Whether the current object has children.
     * @see IChildPolicy#hasChild() 
     */
    public final boolean hasChild() {
        return mChildPolicy.hasChild();
    }

    /**
     * Returns whether the current object has parent.
     * @return Whether the current object has parent.
     */
    public final boolean hasParent() {
        return mParent != null;
    }

    /**
     * Bind segment as a child of the current object.
     * The index will increase as the number of children increases, starting from {@code 0}.
     * @param segment The child you want to add.
     * @see IChildPolicy#addChild(AutoCloseable)
     */
    public final void addChild(@Nullable AutoCloseable segment) {
        if (segment == null) {
            Log.w("MemoryController", "Try to add child with null value. Ignoring.");
            return;
        }
        mChildPolicy.addChild(segment);
    }

    /**
     * This method will be called when the current object "FINDS" its father.
     * The meaning is that when the current object is bound to {@code parent}, it will be called.
     * @param parent The father you want to bind.
     * @exception IllegalStateException Thrown when the current object already has a father.
     */
    public final void attachParent(@NonNull MemoryController parent) {
        Preconditions.checkState(this.mParent == null,
                "The current object already has a father. If you want to replace it, please call 'detachParent' first`.");
        synchronized (this) {
            this.mParent = parent;
        }
    }

    /**
     * Called when unbinding the current object from the parent
     * This method will call {@link #onDetachParent()}
     */
    public synchronized final void detachParent() {
        onDetachParent();
        this.mParent = null;
    }

    /**
     * Called when unbinding the current object from the parent
     * @see SharedResource#onDetachParent()
     */
    @ForOverride
    protected void onDetachParent() {
    }

    /**
     * Determine if the given {@code needle} is a paternal relative of the current object.
     * @param needle The object you want to check.
     */
    public final boolean checkIsParent(@NonNull MemoryController needle) {
        return this == needle || (mParent != null && mParent.checkIsParent(needle));
    }

    /**
     * Called from child.
     * Determine whether the parent allows child release.
     *
     * @return Returning true indicates permission, while false does not allow.
     * @see SharedResource
     */
    protected boolean askParentToAllowChildRelease(MemoryController controller) {
        if (hasParent()) {
            return mParent.askParentToAllowChildRelease(controller);
        }
        return true;
    }


    /**
     * Remove {@code child} from the current object.
     * If successful, the {@link #detachParent} of {@code child} will be automatically called.
     *
     * @param child The object you want to delete.
     * @see IChildPolicy#removeChild(AutoCloseable)
     */
    public final void removeChild(@NonNull MemoryController child) {
        mChildPolicy.removeChild(child);
    }

    /**
     * Called when releasing the current object (including garbage collector collecting the current object).
     * <p>
     * Rewriting this method normally is to free up resources.
     */
    public void dispose(boolean finalized) throws NativeMethodException {
        if (finalized) {
            Log.e(TAG, "Memory hasn't released yet!, Object " + this);
        }
    }

    /**
     * Get the current object's child management method.
     * @see IChildPolicy
     */
    public final IChildPolicy getChildPolicy() {
        return mChildPolicy;
    }

    /**
     * Set the current object's child management method.
     *
     * @param policy The method you want to set.
     * @see IChildPolicy
     */
    public final void setChildPolicy(@NonNull IChildPolicy policy) {
        this.mChildPolicy = policy;
    }

    /**
     * Get the father of the current object.
     * @return If the current object does not have a father, return null.
     */
    @Nullable
    public final MemoryController getParent() {
        return mParent;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (this.mIsDisposed.compareAndSet(false, true)) {
            doClose(true);
        }
    }
}
