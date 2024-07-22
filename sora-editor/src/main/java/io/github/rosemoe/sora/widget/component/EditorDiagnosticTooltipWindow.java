package io.github.rosemoe.sora.widget.component;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.collect.Lists;

import java.util.List;

import io.github.rosemoe.sora.I18nConfig;
import io.github.rosemoe.sora.R;
import io.github.rosemoe.sora.event.ColorSchemeUpdateEvent;
import io.github.rosemoe.sora.event.EditorFocusChangeEvent;
import io.github.rosemoe.sora.event.EditorReleaseEvent;
import io.github.rosemoe.sora.event.EventManager;
import io.github.rosemoe.sora.event.EventReceiver;
import io.github.rosemoe.sora.event.HoverEvent;
import io.github.rosemoe.sora.event.ScrollEvent;
import io.github.rosemoe.sora.event.SelectionChangeEvent;
import io.github.rosemoe.sora.event.Unsubscribe;
import io.github.rosemoe.sora.lang.diagnostic.DiagnosticDetail;
import io.github.rosemoe.sora.lang.diagnostic.DiagnosticRegion;
import io.github.rosemoe.sora.lang.diagnostic.DiagnosticsContainer;
import io.github.rosemoe.sora.lang.diagnostic.Quickfix;
import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.util.IntPair;
import io.github.rosemoe.sora.util.Pair;
import io.github.rosemoe.sora.util.ViewUtils;
import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.base.EditorPopupWindow;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

public class EditorDiagnosticTooltipWindow extends EditorPopupWindow implements EditorBuiltinComponent {
    @NonNull
    private final CodeEditor editor;
    private final EventManager eventManager;
    private final View rootView;
    private final TextView briefMessageText, detailMessageText, quickfixText, moreActionText;
    private final ViewGroup messagePanel, quickfixPanel;
    private final List<DiagnosticRegion> diagnosticList;
    private final float[] buffer;
    private final int[] locationBuffer;
    private final PopupMenu popupMenu;
    private final Pair<Float, Float> lastHoverPos;
    @Nullable
    protected CharPosition memorizedPosition;
    @Nullable
    protected DiagnosticDetail currentDiagnostic;
    protected int maxHeight;
    @Nullable
    private CharPosition hoverPosition;
    private boolean menuShown, popupHovered;

    public EditorDiagnosticTooltipWindow(@NonNull final CodeEditor editor) {
        super(editor, FEATURE_HIDE_WHEN_FAST_SCROLL | FEATURE_SHOW_OUTSIDE_VIEW_ALLOWED);
        this.editor = editor;
        this.eventManager = editor.createSubEventManager();
        this.rootView = LayoutInflater.from(editor.getContext()).inflate(R.layout.diagnostic_tooltip_window, null);
        this.briefMessageText = rootView.findViewById(R.id.diagnostic_tooltip_brief_message);
        this.detailMessageText = rootView.findViewById(R.id.diagnostic_tooltip_detailed_message);
        this.quickfixText = rootView.findViewById(R.id.diagnostic_tooltip_preferred_action);
        this.moreActionText = rootView.findViewById(R.id.diagnostic_tooltip_more_actions);
        this.messagePanel = rootView.findViewById(R.id.diagnostic_container_message);
        this.quickfixPanel = rootView.findViewById(R.id.diagnostic_container_quickfix);
        this.maxHeight = (int) (editor.getDpUnit() * 175);
        this.diagnosticList = Lists.newArrayList();
        this.buffer = new float[2];
        this.locationBuffer = new int[2];
        this.popupMenu = new PopupMenu(editor.getContext(), moreActionText);
        this.menuShown = false;
        this.popupHovered = false;
        this.hoverPosition = null;
        this.memorizedPosition = null;
        this.currentDiagnostic = null;
        this.lastHoverPos = new Pair<>();

        super.setContentView(rootView);
        getPopup().setAnimationStyle(R.style.diagnostic_popup_animation);
        rootView.setClipToOutline(true);
        rootView.setOnGenericMotionListener(new View.OnGenericMotionListener() {
            @Override
            public boolean onGenericMotion(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_HOVER_ENTER:
                        popupHovered = true;
                        break;
                    case MotionEvent.ACTION_HOVER_EXIT:
                        popupHovered = false;
                        break;
                }
                return false;
            }
        });
        registerEditorEvents();
        getPopup().setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                currentDiagnostic = null;
                popupHovered = false;
                menuShown = false;
            }
        });
        quickfixText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentDiagnostic != null) {
                    List<Quickfix> quickfixes = currentDiagnostic.getQuickfixes();
                    if (quickfixes != null && !quickfixes.isEmpty()) {
                        quickfixes.get(0).executeQuickfix();
                        dismiss();
                    }
                }
            }
        });
        moreActionText.setText(I18nConfig.getResourceId(R.string.sora_editor_diagnostics_more_actions));
        moreActionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentDiagnostic != null) {
                    final List<Quickfix> quickfixes = currentDiagnostic.getQuickfixes();
                    if (quickfixes != null && quickfixes.size() > 1) {
                        Menu menu = popupMenu.getMenu();
                        menu.clear();
                        for (int i = 0; i < quickfixes.size(); i++) {
                            menu.add(0, i, 0, quickfixes.get(i).resolveTitle(editor.getContext()));
                        }

                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                quickfixes.get(item.getItemId()).executeQuickfix();
                                dismiss();
                                return true;
                            }
                        });

                        popupMenu.show();
                        menuShown = true;
                    }
                }
            }
        });

        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                menuShown = false;
            }
        });
        applyColorScheme();
    }

    @Override
    public boolean isEnabled() {
        return eventManager.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (!isEnabled()) {
            dismiss();
        }
    }

    @Override
    public void dismiss() {
        if (isShowing()) {
            Thread.dumpStack();
            super.dismiss();
        }
    }

    private void registerEditorEvents() {
        eventManager.subscribeEvent(SelectionChangeEvent.class, new EventReceiver<SelectionChangeEvent>() {
            @Override
            public void onReceive(@NonNull SelectionChangeEvent event, @NonNull Unsubscribe unsubscribe) {
                if (!isEnabled() || editor.isInMouseMode()) {
                    return;
                }
                if (event.isSelected() ||
                        (event.getCause() != SelectionChangeEvent.CAUSE_TAP
                                && event.getCause() != SelectionChangeEvent.CAUSE_TEXT_MODIFICATION)) {
                    updateDiagnostic(null, null);
                    return;
                }
                updateDiagnostic(event.getLeft());
            }
        });
        eventManager.subscribeEvent(ScrollEvent.class, new EventReceiver<ScrollEvent>() {
            @Override
            public void onReceive(@NonNull ScrollEvent event, @NonNull Unsubscribe unsubscribe) {
                if (editor.isInMouseMode()) {
                    return;
                }
                if (currentDiagnostic != null && isShowing()) {
                    if (!isSelectionVisible()) {
                        dismiss();
                    } else {
                        updateWindowPosition();
                    }
                }
            }
        });

        final Runnable callback = new Runnable() {
            @Override
            public void run() {
                CharPosition pos = hoverPosition;
                if (getPopup().isShowing()) {
                    if (!(popupHovered || menuShown) && pos != null) {
                        updateDiagnostic(pos);
                    }
                } else {
                    if (pos != null) {
                        updateDiagnostic(pos);
                    }
                }
            }
        };

        eventManager.subscribeAlways(HoverEvent.class, new EventManager.NoUnsubscribeReceiver<HoverEvent>() {
            @Override
            public void onEvent(HoverEvent event) {
                if (editor.isInMouseMode()) {
                    switch (event.getCausingEvent().getAction()) {
                        case MotionEvent.ACTION_HOVER_ENTER:
                            editor.removeCallbacks(callback);
                            updateDiagnostic(null, null);
                            lastHoverPos.first = event.getX();
                            lastHoverPos.second = event.getY();
                            break;
                        case MotionEvent.ACTION_HOVER_EXIT:
                            hoverPosition = null;
                            if (!(popupHovered || menuShown)) {
                                editor.removeCallbacks(callback);
                                editor.postDelayedInLifecycle(callback, ViewUtils.HOVER_TOOLTIP_SHOW_TIMEOUT);

                                lastHoverPos.first = event.getX();
                                lastHoverPos.second = event.getY();
                            }
                            break;
                        case MotionEvent.ACTION_HOVER_MOVE:
                            if (!(popupHovered || menuShown)) {
                                if (editor.isScreenPointOnText(event.getX(), event.getY())) {
                                    if (Math.abs(event.getX() - lastHoverPos.first) > ViewUtils.HOVER_TAP_SLOP ||
                                            Math.abs(event.getY() - lastHoverPos.second) > ViewUtils.HOVER_TAP_SLOP) {
                                        lastHoverPos.first = event.getX();
                                        lastHoverPos.second = event.getY();

                                        long pos = editor.getPointPositionOnScreen(event.getX(), event.getY());
                                        hoverPosition = editor.getText().getIndexer().getCharPosition(
                                                IntPair.getFirst(pos),
                                                IntPair.getSecond(pos)
                                        );

                                        editor.removeCallbacks(callback);
                                        editor.postDelayedInLifecycle(callback, ViewUtils.HOVER_TOOLTIP_SHOW_TIMEOUT);
                                    }
                                } else {
                                    hoverPosition = null;

                                    editor.removeCallbacks(callback);
                                    editor.postDelayedInLifecycle(callback, ViewUtils.HOVER_TOOLTIP_SHOW_TIMEOUT);
                                }
                            }
                    }
                }
            }
        });

        eventManager.subscribeEvent(ColorSchemeUpdateEvent.class, new EventReceiver<ColorSchemeUpdateEvent>() {
            @Override
            public void onReceive(@NonNull ColorSchemeUpdateEvent event, @NonNull Unsubscribe unsubscribe) {
                applyColorScheme();
            }
        });

        eventManager.subscribeEvent(EditorFocusChangeEvent.class, new EventReceiver<EditorFocusChangeEvent>() {
            @Override
            public void onReceive(@NonNull EditorFocusChangeEvent event, @NonNull Unsubscribe unsubscribe) {
                if (!event.isGainFocus()) {
                    dismiss();
                }
            }
        });

        eventManager.subscribeEvent(EditorReleaseEvent.class, new EventReceiver<EditorReleaseEvent>() {
            @Override
            public void onReceive(@NonNull EditorReleaseEvent event, @NonNull Unsubscribe unsubscribe) {
                setEnabled(false);
            }
        });
    }

    protected void applyColorScheme() {
        EditorColorScheme colorScheme = editor.getColorScheme();
        briefMessageText.setTextColor(colorScheme.getColor(EditorColorScheme.DIAGNOSTIC_TOOLTIP_BRIEF_MSG));
        detailMessageText.setTextColor(colorScheme.getColor(EditorColorScheme.DIAGNOSTIC_TOOLTIP_DETAILED_MSG));
        quickfixText.setTextColor(colorScheme.getColor(EditorColorScheme.DIAGNOSTIC_TOOLTIP_ACTION));
        moreActionText.setTextColor(colorScheme.getColor(EditorColorScheme.DIAGNOSTIC_TOOLTIP_ACTION));
        GradientDrawable background = new GradientDrawable();
        background.setCornerRadius(editor.getDpUnit() * 5);
        background.setColor(colorScheme.getColor(EditorColorScheme.DIAGNOSTIC_TOOLTIP_BACKGROUND));
        rootView.setBackground(background);
    }

    protected void updateDiagnostic(CharPosition pos) {
        DiagnosticsContainer diagnostics = editor.getDiagnostics();
        if (diagnostics != null) {
            diagnostics.queryInRegion(
                    diagnosticList,
                    pos.index - 1,
                    pos.index + 1
            );
            if (!diagnosticList.isEmpty()) {
                int minLength = diagnosticList.get(0).endIndex - diagnosticList.get(0).startIndex;
                int minIndex = 0;
                for (int i = 1; i < diagnosticList.size(); ++i) {
                    int length = diagnosticList.get(i).endIndex - diagnosticList.get(i).startIndex;
                    if (length < minLength) {
                        minLength = length;
                        minIndex = i;
                    }
                }
                updateDiagnostic(diagnosticList.get(minIndex).detail, pos);
                if (!editor.getComponent(EditorAutoCompletion.class).isCompletionInProgress())
                    show();
            } else {
                updateDiagnostic(null, null);
            }
            diagnosticList.clear();
        } else {
            updateDiagnostic(null, null);
        }
    }

    protected void updateDiagnostic(DiagnosticDetail diagnostic, CharPosition position) {
        if (!isEnabled()) {
            return;
        }
        if (diagnostic == currentDiagnostic) {
            if (diagnostic != null && !editor.isInMouseMode()) {
                updateWindowPosition();
            }
            return;
        }
        currentDiagnostic = diagnostic;
        memorizedPosition = position;
        if (diagnostic == null) {
            dismiss();
            return;
        }
        CharSequence msg = diagnostic.getBriefMessage();

        briefMessageText.setText(msg.length() != 0 ? msg : "<NULL>");
        CharSequence detailedMessage = diagnostic.getDetailedMessage();
        if (detailedMessage != null) {
            detailMessageText.setText(detailedMessage);
            detailMessageText.setVisibility(View.VISIBLE);
        } else {
            detailMessageText.setVisibility(View.GONE);
        }
        List<Quickfix> quickfixes = diagnostic.getQuickfixes();
        if (quickfixes == null || quickfixes.isEmpty()) {
            detailMessageText.setVisibility(View.GONE);
        } else {
            detailMessageText.setVisibility(View.VISIBLE);
            quickfixText.setText(quickfixes.get(0).resolveTitle(editor.getContext()));
            moreActionText.setVisibility(quickfixes.size() > 1 ? View.VISIBLE : View.GONE);
        }
        updateWindowSize();
        updateWindowPosition();
    }

    protected void updateWindowSize() {
        int width = (int) (editor.getWidth() * 0.9);
        // First, measure the bottom bar
        int bottomBarHeight = 0;
        int bottomBarWidth = 0;
        if (quickfixPanel.getVisibility() == View.VISIBLE) {
            quickfixPanel.measure(
                    View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.AT_MOST),
                    View.MeasureSpec.makeMeasureSpec(114514, View.MeasureSpec.AT_MOST)
            );
            bottomBarHeight = quickfixPanel.getMeasuredHeight();
            bottomBarWidth = Math.min(quickfixPanel.getMeasuredWidth(), width);
        }
        // Then, measure the message region
        int restHeight = Math.max((maxHeight - bottomBarHeight), 1);
        messagePanel.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        messagePanel.measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(restHeight, View.MeasureSpec.AT_MOST)
        );
        int messageHeight = Math.min(messagePanel.getMeasuredHeight(), restHeight);
        int messageWidth = Math.min(messagePanel.getMeasuredWidth(), width);
        messagePanel.getLayoutParams().height = messageHeight;
        int dialogWidth = Math.max(bottomBarWidth, messageWidth);
        int dialogHeight = bottomBarHeight + messageHeight;
        setSize(dialogWidth, dialogHeight);
    }

    protected void updateWindowPosition() {
        CharPosition selection = memorizedPosition;
        if (selection == null) return;
        float charX = editor.getCharOffsetX(selection.line, selection.column);
        float charY = editor.getCharOffsetY(selection.line, selection.column) - editor.getRowHeight();
        editor.getLocationInWindow(locationBuffer);
        float restAbove = charY + locationBuffer[1];
        float restBottom = editor.getHeight() - charY - editor.getRowHeight();
        boolean completionShowing = editor.getComponent(EditorAutoCompletion.class).isShowing();
        float windowY = restAbove > restBottom || completionShowing ?
                charY - getHeight() :
                charY + editor.getRowHeight() * 1.5f;
        if (completionShowing && windowY < 0) {
            dismiss();
            return;
        }
        float windowX = Math.max((charX - (float) getWidth() / 2), 0f);
        setLocationAbsolutely((int) windowX, (int) windowY);
    }

    protected boolean isSelectionVisible() {
        CharPosition selection = editor.getCursor().left();
        editor.getLayout().getCharLayoutOffset(selection.line, selection.column, buffer);
        return buffer[0] >= editor.getOffsetY()
                && buffer[0] - editor.getRowHeight() <= editor.getOffsetY() + editor.getHeight()
                && buffer[1] >= editor.getOffsetX()
                && buffer[1] - 100f /* larger than a single character */ <= editor.getOffsetX() + editor.getWidth();
    }
}
