package rikka.minidrawer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.design.internal.ForegroundLinearLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Rikka on 2016/5/25.
 */
class DrawerItemView extends ForegroundLinearLayout implements Checkable, View.OnClickListener {
    private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};
    private final int mIconSize;

    private MenuItem mItem;

    private OnDrawerItemClickedListener mOnDrawerItemClickedListener;
    private boolean mChecked;

    private TextView mTextView;
    private ImageView mImageView;

    public interface OnDrawerItemClickedListener {
        void onClick(MenuItem item);
    }

    public DrawerItemView(Context context) {
        this(context, null);
    }

    public DrawerItemView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mIconSize = context.getResources().getDimensionPixelSize(
                R.dimen.design_navigation_icon_size);

        setChecked(false);

        if (getForeground() == null) {
            StateListDrawable drawable = new StateListDrawable();
            drawable.addState(CHECKED_STATE_SET, new ColorDrawable(ContextCompat.getColor(getContext(), R.color.drawer_item_checked_foreground)));
            drawable.addState(View.EMPTY_STATE_SET, new ColorDrawable(Color.TRANSPARENT));

            setForeground(drawable);
        }

        setOnClickListener(this);
    }

    public MenuItem getMenuItem() {
        return mItem;
    }

    public void setMenuItem(MenuItem item) {
        if (item == mItem) {
            return;
        }

        mItem = item;

        setText(item.getTitle());
        setIcon(item.getIcon());
    }

    private void setText(CharSequence title) {
        mTextView.setText(title);
    }

    public void setIcon(Drawable icon) {
        if (icon != null) {
            icon = DrawableCompat.wrap(icon.getConstantState().newDrawable()).mutate();
            icon.setBounds(0, 0, mIconSize, mIconSize);
            DrawableCompat.setTintList(icon, ContextCompat.getColorStateList(getContext(), R.color.drawer_item_icon));
        }
        mImageView.setImageDrawable(icon);
    }

    public void setTextAlpha(float alpha) {
        mTextView.setAlpha(alpha);
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);

        if (child instanceof TextView) {
            mTextView = (TextView) child;
        }

        if (child instanceof ImageView) {
            mImageView = (ImageView) child;
        }
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
            View.mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    @Override
    public void setChecked(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;
            mImageView.setActivated(checked);
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
}
