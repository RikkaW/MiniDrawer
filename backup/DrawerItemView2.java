package rikka.minidrawerdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Checkable;
import android.widget.TextView;

/**
 * Created by Rikka on 2016/5/25.
 */
class DrawerItemView2 extends TextView implements Checkable, View.OnClickListener {
    private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};
    private final int mIconSize;

    private MenuItem mItem;

    private OnDrawerItemClickedListener mOnDrawerItemClickedListener;
    private boolean mChecked;

    public interface OnDrawerItemClickedListener {
        void onClick(MenuItem item);
    }

    public DrawerItemView2(Context context) {
        this(context, null);
    }

    public DrawerItemView2(Context context, AttributeSet attrs) {
        super(context, attrs);

        mIconSize = context.getResources().getDimensionPixelSize(
                R.dimen.design_navigation_icon_size);

        setChecked(false);

        if (getForeground() == null) {
            StateListDrawable drawable = new StateListDrawable();
            drawable.addState(CHECKED_STATE_SET, new ColorDrawable(Color.parseColor("#18000000")));
            drawable.addState(EMPTY_STATE_SET, new ColorDrawable(Color.TRANSPARENT));

            setForeground(drawable);
        }

        setOnClickListener(this);
    }

    public MenuItem getMenuItem() {
        return mItem;
    }

    public void setIcon(Drawable icon) {
        if (icon != null) {
            icon = DrawableCompat.wrap(icon.getConstantState().newDrawable()).mutate();
            icon.setBounds(0, 0, mIconSize, mIconSize);
            DrawableCompat.setTintList(icon, ContextCompat.getColorStateList(getContext(), R.color.drawer_item));
        }
        //TextViewCompat.setCompoundDrawablesRelative(this, icon, null, null, null);
        setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
    }

    public void setMenuItem(MenuItem item) {
        if (item == mItem) {
            return;
        }

        mItem = item;

        setText(item.getTitle());
        setIcon(item.getIcon());
    }

    public void setOnDrawerItemClickedListener(OnDrawerItemClickedListener onDrawerItemClickedListener) {
        mOnDrawerItemClickedListener = onDrawerItemClickedListener;
    }

    @Override
    public void onClick(View v) {
        if (mOnDrawerItemClickedListener != null) {
            mOnDrawerItemClickedListener.onClick(mItem);
        }
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    @Override
    public void setChecked(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;
            refreshDrawableState();
        }
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void toggle() {
        setChecked(!isChecked());
    }


    private Drawable mForeground;

    private final Rect mSelfBounds = new Rect();

    private final Rect mOverlayBounds = new Rect();

    private int mForegroundGravity = Gravity.FILL;

    protected boolean mForegroundInPadding = true;

    boolean mForegroundBoundsChanged = false;

    /**
     * Describes how the foreground is positioned.
     *
     * @return foreground gravity.
     * @see #setForegroundGravity(int)
     */
    public int getForegroundGravity() {
        return mForegroundGravity;
    }

    /**
     * Describes how the foreground is positioned. Defaults to START and TOP.
     *
     * @param foregroundGravity See {@link android.view.Gravity}
     * @see #getForegroundGravity()
     */
    public void setForegroundGravity(int foregroundGravity) {
        if (mForegroundGravity != foregroundGravity) {
            if ((foregroundGravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) == 0) {
                foregroundGravity |= Gravity.START;
            }

            if ((foregroundGravity & Gravity.VERTICAL_GRAVITY_MASK) == 0) {
                foregroundGravity |= Gravity.TOP;
            }

            mForegroundGravity = foregroundGravity;

            if (mForegroundGravity == Gravity.FILL && mForeground != null) {
                Rect padding = new Rect();
                mForeground.getPadding(padding);
            }

            requestLayout();
        }
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || (who == mForeground);
    }

    @Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (mForeground != null) {
            mForeground.jumpToCurrentState();
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mForeground != null && mForeground.isStateful()) {
            mForeground.setState(getDrawableState());
        }
    }

    /**
     * Supply a Drawable that is to be rendered on top of all of the child
     * views in the frame layout.  Any padding in the Drawable will be taken
     * into account by ensuring that the children are inset to be placed
     * inside of the padding area.
     *
     * @param drawable The Drawable to be drawn on top of the children.
     */
    public void setForeground(Drawable drawable) {
        if (mForeground != drawable) {
            if (mForeground != null) {
                mForeground.setCallback(null);
                unscheduleDrawable(mForeground);
            }

            mForeground = drawable;

            if (drawable != null) {
                setWillNotDraw(false);
                drawable.setCallback(this);
                if (drawable.isStateful()) {
                    drawable.setState(getDrawableState());
                }
                if (mForegroundGravity == Gravity.FILL) {
                    Rect padding = new Rect();
                    drawable.getPadding(padding);
                }
            } else {
                setWillNotDraw(true);
            }
            requestLayout();
            invalidate();
        }
    }

    /**
     * Returns the drawable used as the foreground of this FrameLayout. The
     * foreground drawable, if non-null, is always drawn on top of the children.
     *
     * @return A Drawable or null if no foreground was set.
     */
    public Drawable getForeground() {
        return mForeground;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mForegroundBoundsChanged |= changed;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mForegroundBoundsChanged = true;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);

        if (mForeground != null) {
            final Drawable foreground = mForeground;

            if (mForegroundBoundsChanged) {
                mForegroundBoundsChanged = false;
                final Rect selfBounds = mSelfBounds;
                final Rect overlayBounds = mOverlayBounds;

                final int w = getRight() - getLeft();
                final int h = getBottom() - getTop();

                if (mForegroundInPadding) {
                    selfBounds.set(0, 0, w, h);
                } else {
                    selfBounds.set(getPaddingLeft(), getPaddingTop(),
                            w - getPaddingRight(), h - getPaddingBottom());
                }

                Gravity.apply(mForegroundGravity, foreground.getIntrinsicWidth(),
                        foreground.getIntrinsicHeight(), selfBounds, overlayBounds);
                foreground.setBounds(overlayBounds);
            }

            foreground.draw(canvas);
        }
    }

    @Override
    public void drawableHotspotChanged(float x, float y) {
        super.drawableHotspotChanged(x, y);
        if (mForeground != null) {
            DrawableCompat.setHotspot(mForeground, x, y);
        }
    }
}
