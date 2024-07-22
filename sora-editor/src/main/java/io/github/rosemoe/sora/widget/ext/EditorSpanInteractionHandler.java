package io.github.rosemoe.sora.widget.ext;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.common.base.Predicate;
import com.google.errorprone.annotations.ForOverride;
import com.google.errorprone.annotations.concurrent.LazyInit;

import io.github.rosemoe.sora.event.ClickEvent;
import io.github.rosemoe.sora.event.DoubleClickEvent;
import io.github.rosemoe.sora.event.EditorMotionEvent;
import io.github.rosemoe.sora.event.EventManager;
import io.github.rosemoe.sora.event.LongPressEvent;
import io.github.rosemoe.sora.lang.styling.Span;
import io.github.rosemoe.sora.lang.styling.span.SpanClickableUrl;
import io.github.rosemoe.sora.lang.styling.span.SpanExtAttrs;
import io.github.rosemoe.sora.lang.styling.span.SpanInteractionInfo;
import io.github.rosemoe.sora.text.TextRange;
import io.github.rosemoe.sora.util.IntPair;
import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.RegionResolver;

/**
 * Handle span interaction for editor. This is a optional part of editor currently.
 * If you need to handle span interaction,
 * create this handler with the target editor.
 * <p>
 * Note that do not create multiple handler of the same type for editor.
 * Otherwise, single span interaction event will
 * be handled multiple times.
 *
 * @author Rosemoe
 */
public class EditorSpanInteractionHandler {
    @NonNull
    private final CodeEditor editor;
    @LazyInit
    private EventManager mEventManager;

    public EditorSpanInteractionHandler(@NonNull CodeEditor editor) {
        this.editor = editor;

        getEventManager().subscribeAlways(ClickEvent.class, new EventManager.NoUnsubscribeReceiver<ClickEvent>() {
            @Override
            public void onEvent(ClickEvent event) {
                if (!event.isFromMouse() || (event.isFromMouse() && editor.getKeyMetaStates().isCtrlPressed())) {
                    handleInteractionEvent(
                            event,
                            new Predicate<SpanInteractionInfo>() {
                                @Override
                                public boolean apply(SpanInteractionInfo input) {
                                    return input.isClickable();
                                }
                            },
                            new EventInteractionHandler() {
                                @Override
                                public boolean handle(Span span, SpanInteractionInfo info, TextRange range) {
                                    return handleSpanClick(span, info, range);
                                }
                            },
                            !event.isFromMouse()
                    );
                }
            }
        });

        getEventManager().subscribeAlways(DoubleClickEvent.class, new EventManager.NoUnsubscribeReceiver<DoubleClickEvent>() {
            @Override
            public void onEvent(DoubleClickEvent event) {
                handleInteractionEvent(
                        event,
                        new Predicate<SpanInteractionInfo>() {
                            @Override
                            public boolean apply(SpanInteractionInfo input) {
                                return input.isDoubleClickable();
                            }
                        },
                        new EventInteractionHandler() {
                            @Override
                            public boolean handle(Span span, SpanInteractionInfo info, TextRange range) {
                                return handleSpanDoubleClick(span, info, range);
                            }
                        },
                        !event.isFromMouse()
                );
            }
        });

        getEventManager().subscribeAlways(LongPressEvent.class, new EventManager.NoUnsubscribeReceiver<LongPressEvent>() {
            @Override
            public void onEvent(LongPressEvent event) {
                handleInteractionEvent(
                        event,
                        new Predicate<SpanInteractionInfo>() {
                            @Override
                            public boolean apply(SpanInteractionInfo input) {
                                return input.isLongClickable();
                            }
                        },
                        new EventInteractionHandler() {
                            @Override
                            public boolean handle(Span span, SpanInteractionInfo info, TextRange range) {
                                return handleSpanLongClick(span, info, range);
                            }
                        },
                        !event.isFromMouse()
                );
            }
        });
    }

    @NonNull
    public EventManager getEventManager() {
        if (mEventManager == null) {
            mEventManager = editor.createSubEventManager();
        }
        return mEventManager;
    }

    private void handleInteractionEvent(EditorMotionEvent event, Predicate<SpanInteractionInfo> predicate,
                                        EventInteractionHandler handler, boolean checkCursorRange) {
        long regionInfo = RegionResolver.resolveTouchRegion(editor, event.getCausingEvent());
        Span span = event.getSpan();
        TextRange spanRange = event.getSpanRange();

        if (IntPair.getFirst(regionInfo) == RegionResolver.REGION_TEXT &&
                IntPair.getSecond(regionInfo) == RegionResolver.IN_BOUND &&
                span != null && spanRange != null
        ) {
            if (!checkCursorRange || spanRange.isPositionInside(editor.getCursor().left())) {
                SpanInteractionInfo it = span.<SpanInteractionInfo>getSpanExt(SpanExtAttrs.EXT_INTERACTION_INFO);
                if (predicate.apply(it)) {
                    if (handler.handle(span, it, spanRange)) {
                        event.intercept();
                    }
                }
            }
        }
    }

    @ForOverride
    public boolean handleSpanClick(@NonNull Span span, @NonNull SpanInteractionInfo interactionInfo, @NonNull TextRange spanRange) {
        return false;
    }

    public boolean handleSpanDoubleClick(@NonNull Span span, @NonNull SpanInteractionInfo interactionInfo, @NonNull TextRange spanRange) {
        if (interactionInfo instanceof SpanClickableUrl) {
            SpanClickableUrl spanClickableUrl = (SpanClickableUrl) interactionInfo;
            String url = spanClickableUrl.getData();
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                editor.getContext().startActivity(intent);
            } catch (Throwable e) {
                Log.e("SpanInteractionHandler", "Failed to open url", e);
            }
            return true;
        }
        return false;
    }

    public boolean handleSpanLongClick(@NonNull Span span, @NonNull SpanInteractionInfo interactionInfo, @NonNull TextRange spanRange) {
        return false;
    }

    public interface EventInteractionHandler {
        boolean handle(Span span, SpanInteractionInfo info, TextRange range);
    }
}
