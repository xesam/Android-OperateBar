package io.github.xesam.android.operatebar;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class OperateBarLayout extends FrameLayout {

    private View decorView;

    public OperateBarLayout(@NonNull Context context) {
        this(context, null);
    }

    public OperateBarLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OperateBarLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setClipChildren(false);
        setClipToPadding(false);

        try (TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OperateBarLayout, defStyleAttr, 0)) {
            int decorLayoutRes = a.getResourceId(R.styleable.OperateBarLayout_oblDividerLayout, 0);
            if (decorLayoutRes != 0) {
                decorView = LayoutInflater.from(context).inflate(decorLayoutRes, this, false);
                decorView.setClickable(false);
                decorView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                super.addView(decorView, 0);
            } else {
                decorView = null;
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();

        int contentLeft = paddingLeft;
        int contentRight = Math.max(contentLeft, widthSize - paddingRight);
        int contentWidth = Math.max(0, contentRight - contentLeft);

        int visibleChildCount = countVisibleChildren();

        int tallestChild = measureChildren(widthMeasureSpec, heightMeasureSpec,
                widthMode, heightMode, heightSize, contentLeft, contentWidth, visibleChildCount);

        int desiredWidth = computeDesiredWidth(widthMode, widthSize, paddingLeft, paddingRight);
        int desiredHeight = tallestChild + paddingTop + paddingBottom;

        int measuredWidth = resolveSize(desiredWidth, widthMeasureSpec);
        int measuredHeight = resolveSize(desiredHeight, heightMeasureSpec);
        setMeasuredDimension(measuredWidth, measuredHeight);

        measureDecorView(measuredWidth, heightMode, heightSize);
    }

    private int countVisibleChildren() {
        int count = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child != decorView && child.getVisibility() != View.GONE) {
                count++;
            }
        }
        return count;
    }

    private int measureChildren(int widthMeasureSpec, int heightMeasureSpec,
            int widthMode, int heightMode, int heightSize,
            int contentLeft, int contentWidth, int visibleChildCount) {
        int tallestChild = 0;
        int childSlotIndex = 0;

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == decorView || child.getVisibility() == View.GONE) {
                continue;
            }

            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) child.getLayoutParams();
            int childWidthSpec = createChildWidthSpec(widthMeasureSpec, widthMode, heightMode,
                    contentLeft, contentWidth, visibleChildCount, lp, childSlotIndex);

            int childHeightSpec = createChildHeightSpec(lp, heightMode, heightSize);
            child.measure(childWidthSpec, childHeightSpec);

            int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            if (childHeight > tallestChild) {
                tallestChild = childHeight;
            }
            childSlotIndex++;
        }
        return tallestChild;
    }

    private int createChildWidthSpec(int widthMeasureSpec, int widthMode, int heightMode,
            int contentLeft, int contentWidth, int visibleChildCount,
            FrameLayout.LayoutParams lp, int childSlotIndex) {
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();

        if (widthMode == MeasureSpec.UNSPECIFIED || visibleChildCount == 0) {
            return getChildMeasureSpec(widthMeasureSpec,
                    paddingLeft + paddingRight + lp.leftMargin + lp.rightMargin, lp.width);
        }

        int slotLeft = getSlotLeft(contentLeft, contentWidth, visibleChildCount, childSlotIndex);
        int slotRight = getSlotRight(contentLeft, contentWidth, visibleChildCount, childSlotIndex);
        int slotWidth = Math.max(0, slotRight - slotLeft);
        int availableInSlot = Math.max(0, slotWidth - lp.leftMargin - lp.rightMargin);
        return makeChildWidthSpec(lp.width, availableInSlot);
    }

    private int computeDesiredWidth(int widthMode, int widthSize, int paddingLeft, int paddingRight) {
        if (widthMode != MeasureSpec.UNSPECIFIED) {
            return widthSize;
        }

        int totalChildWidth = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == decorView || child.getVisibility() == View.GONE) {
                continue;
            }
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) child.getLayoutParams();
            totalChildWidth += child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
        }
        return paddingLeft + paddingRight + totalChildWidth;
    }

    private void measureDecorView(int measuredWidth, int heightMode, int heightSize) {
        if (decorView == null) {
            return;
        }

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) decorView.getLayoutParams();
        int decorWidthSpec = MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY);
        int decorHeightSpec = createChildHeightSpec(lp, heightMode, heightSize);
        decorView.measure(decorWidthSpec, decorHeightSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        layoutChildrenEvenly();
        layoutDecorView();
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        ensureBottomAligned(child);
    }

    private void ensureBottomAligned(@NonNull View child) {
        LayoutParams params = asLayoutParams(child.getLayoutParams());
        if (params.gravity == Gravity.NO_GRAVITY) {
            params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        } else {
            params.gravity = (params.gravity & ~Gravity.VERTICAL_GRAVITY_MASK) | Gravity.BOTTOM;
        }
        child.setLayoutParams(params);
    }

    private void layoutDecorView() {
        if (decorView == null) {
            return;
        }

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) decorView.getLayoutParams();
        int contentBottom = getHeight() - getPaddingBottom();
        int decorHeight = decorView.getMeasuredHeight();
        int decorBottom = contentBottom - lp.bottomMargin;
        int decorTop = decorBottom - decorHeight;
        decorView.layout(0, decorTop, getWidth(), decorBottom);
    }

    private void layoutChildrenEvenly() {
        int visibleChildCount = countVisibleChildren();
        if (visibleChildCount == 0) {
            return;
        }

        int contentLeft = getPaddingLeft();
        int contentRight = getWidth() - getPaddingRight();
        int contentBottom = getHeight() - getPaddingBottom();
        int contentWidth = Math.max(0, contentRight - contentLeft);

        int slotIndex = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == decorView || child.getVisibility() == View.GONE) {
                continue;
            }

            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) child.getLayoutParams();
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            int slotLeft = getSlotLeft(contentLeft, contentWidth, visibleChildCount, slotIndex);
            int slotRight = getSlotRight(contentLeft, contentWidth, visibleChildCount, slotIndex);
            float slotCenter = (slotLeft + slotRight) / 2f;
            int childLeft = Math.round(slotCenter - (childWidth / 2f) + ((lp.leftMargin - lp.rightMargin) / 2f));

            int minLeft = contentLeft + lp.leftMargin;
            int maxLeft = contentRight - lp.rightMargin - childWidth;
            if (maxLeft >= minLeft) {
                childLeft = Math.max(minLeft, Math.min(childLeft, maxLeft));
            } else {
                childLeft = minLeft;
            }

            int childBottom = contentBottom - lp.bottomMargin;
            int childTop = childBottom - childHeight;
            child.layout(childLeft, childTop, childLeft + childWidth, childBottom);
            slotIndex++;
        }
    }

    @Override
    protected void dispatchDraw(android.graphics.Canvas canvas) {
        long drawingTime = getDrawingTime();

        drawNonPrimaryChildren(canvas, drawingTime);
        drawDecorView(canvas, drawingTime);
        drawPrimaryChildren(canvas, drawingTime);
    }

    private void drawNonPrimaryChildren(android.graphics.Canvas canvas, long drawingTime) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == decorView || child.getVisibility() != View.VISIBLE || isPrimary(child)) {
                continue;
            }
            drawChild(canvas, child, drawingTime);
        }
    }

    private void drawDecorView(android.graphics.Canvas canvas, long drawingTime) {
        if (decorView != null && decorView.getVisibility() == View.VISIBLE) {
            drawChild(canvas, decorView, drawingTime);
        }
    }

    private void drawPrimaryChildren(android.graphics.Canvas canvas, long drawingTime) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == decorView || child.getVisibility() != View.VISIBLE || !isPrimary(child)) {
                continue;
            }
            drawChild(canvas, child, drawingTime);
        }
    }

    private boolean isPrimary(@NonNull View child) {
        LayoutParams params = asLayoutParams(child.getLayoutParams());
        return params.isPrimary;
    }

    @NonNull
    private LayoutParams asLayoutParams(ViewGroup.LayoutParams params) {
        if (params instanceof LayoutParams) {
            return (LayoutParams) params;
        }
        if (params instanceof FrameLayout.LayoutParams) {
            return new LayoutParams((FrameLayout.LayoutParams) params);
        }
        return new LayoutParams(params);
    }

    private int getSlotLeft(int contentLeft, int contentWidth, int totalSlots, int slotIndex) {
        return contentLeft + (contentWidth * slotIndex) / totalSlots;
    }

    private int getSlotRight(int contentLeft, int contentWidth, int totalSlots, int slotIndex) {
        return contentLeft + (contentWidth * (slotIndex + 1)) / totalSlots;
    }

    private int makeChildWidthSpec(int childLayoutWidth, int availableInSlot) {
        if (childLayoutWidth == LayoutParams.MATCH_PARENT) {
            return MeasureSpec.makeMeasureSpec(availableInSlot, MeasureSpec.EXACTLY);
        }
        if (childLayoutWidth >= 0) {
            return MeasureSpec.makeMeasureSpec(Math.min(childLayoutWidth, availableInSlot), MeasureSpec.EXACTLY);
        }
        return MeasureSpec.makeMeasureSpec(availableInSlot, MeasureSpec.AT_MOST);
    }

    private int createChildHeightSpec(FrameLayout.LayoutParams lp, int parentHeightMode, int parentHeightSize) {
        if (lp.height >= 0) {
            return MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY);
        }
        if (parentHeightMode == MeasureSpec.UNSPECIFIED) {
            return MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        int availableHeight = Math.max(0,
                parentHeightSize - getPaddingTop() - getPaddingBottom() - lp.topMargin - lp.bottomMargin);
        if (lp.height == LayoutParams.MATCH_PARENT) {
            return MeasureSpec.makeMeasureSpec(availableHeight, MeasureSpec.EXACTLY);
        }
        return MeasureSpec.makeMeasureSpec(availableHeight, MeasureSpec.AT_MOST);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    public static class LayoutParams extends FrameLayout.LayoutParams {
        public boolean isPrimary;

        public LayoutParams(@NonNull Context c, @Nullable AttributeSet attrs) {
            super(c, attrs);
            try (TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.OperateBarLayout_Layout)) {
                isPrimary = a.getBoolean(R.styleable.OperateBarLayout_Layout_layout_oblIsPrimary, false);
            }
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(@NonNull ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(@NonNull FrameLayout.LayoutParams source) {
            super(source);
            gravity = source.gravity;
        }
    }
}
