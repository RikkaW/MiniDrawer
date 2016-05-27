package rikka.minidrawer;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

/**
 * Created by Rikka on 2016/5/25.
 */
public class MiniDrawerLayout extends FrameLayout {
    private final ViewConfiguration mConfiguration;

    private View mFrame;
    private DrawerContainer mDrawerContainer;

    private int mMenuResourceId;
    private int mDrawerWidthMini;
    private int mDrawerWidthExpanded;
    private int mDrawerPadding;

    private boolean mExpanded;

    private NavigationView.OnNavigationItemSelectedListener mNavigationItemSelectedListener;

    public MiniDrawerLayout(Context context) {
        this(context, null);
    }

    public MiniDrawerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MiniDrawerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mConfiguration = ViewConfiguration.get(context);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MiniDrawerLayout, defStyleAttr, 0);

        mMenuResourceId = a.getResourceId(R.styleable.MiniDrawerLayout_drawer_menu, -1);
        mDrawerWidthMini = a.getDimensionPixelSize(R.styleable.MiniDrawerLayout_drawer_width, 0);
        mDrawerWidthExpanded = a.getDimensionPixelSize(R.styleable.MiniDrawerLayout_drawer_width_expanded, 0);
        mDrawerPadding = a.getDimensionPixelSize(R.styleable.MiniDrawerLayout_drawer_padding, 0);

        a.recycle();
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);

        if (!(child instanceof DrawerContainer) && mFrame == null) {
            mFrame = child;
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mDrawerContainer = new DrawerContainer(getContext());
        mDrawerContainer.init(mMenuResourceId, mDrawerWidthMini, mDrawerWidthExpanded, mDrawerPadding);
        mDrawerContainer.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                if (mNavigationItemSelectedListener != null) {
                    mNavigationItemSelectedListener.onNavigationItemSelected(item);
                }
                return false;
            }
        });
        addView(mDrawerContainer);

        setExpanded(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mFrame.getLayoutParams().width = widthMeasureSpec - mDrawerWidthMini;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        mFrame.layout(mTransX, 0, right, mFrame.getMeasuredHeight());
    }

    private void setTransX(int transX) {
        int drawerWidth = mDrawerWidthExpanded;
        int miniDrawerWidth = mDrawerWidthMini;

        if (transX > drawerWidth) {
            transX = drawerWidth;
        }

        if (transX < miniDrawerWidth) {
            transX = miniDrawerWidth;
        }

        mTransX = transX;
        //setDrawerByTransX(transX - miniDrawerWidth, drawerWidth - miniDrawerWidth);
        mDrawerContainer.setDrawerByTransX(transX - miniDrawerWidth, drawerWidth - miniDrawerWidth);

        requestLayout();
    }

    private void cancelAnimator() {
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
        }
    }

    private void startAnimator() {
        int start = mTransX;
        int end = isExpanded() ? mDrawerWidthExpanded : mDrawerWidthMini;
        int time = 100 + (int) (200 * (Math.abs(end - start) / (float) (mDrawerWidthExpanded - mDrawerWidthMini)));

        mAnimator = ValueAnimator.ofInt(start, end);
        mAnimator.setDuration(time);
        mAnimator.setInterpolator(new FastOutSlowInInterpolator());
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setTransX((int) animation.getAnimatedValue());
            }
        });
        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mDrawerContainer.animationStart(isExpanded());
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                requestLayout();
                mDrawerContainer.animationEnd(isExpanded());
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mAnimator.start();
    }

    private ValueAnimator mAnimator;

    public void toggle() {
        setExpanded(!mExpanded);
    }

    final public boolean isExpanded() {
        return mExpanded;
    }

    public void setExpanded(boolean expanded) {
        mExpanded = expanded;

        cancelAnimator();
        startAnimator();
    }

    private float mXDown;
    private float mYDown;

    private int mTransX;
    private long mDownTime;

    private int mSavedTransX;
    private int mSavedScrollY;

    private boolean mInArea;
    private boolean mInterceptTouch;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                //Log.d("QAQ", "ACTION_DOWN");

                cancelAnimator();

                mInterceptTouch = false;

                mDownTime = SystemClock.elapsedRealtime();
                mXDown = ev.getX();
                mYDown = ev.getY();
                mSavedTransX = mTransX;
                //mSavedScrollY = mDrawer.getScrollY();

                float left, right;
                if (isExpanded()) {
                    left = mFrame.getX();
                    right = left + mFrame.getWidth();
                } else {
                    left = mDrawerContainer.getX();
                    right = left + mDrawerWidthMini;
                }

                float x = ev.getX();
                mInArea = x >= left && x <= right;

                break;
            case MotionEvent.ACTION_MOVE:
                if (mInArea) {
                    setTransX(mSavedTransX + (int) (ev.getX() - mXDown));

                    final long timeDelta = SystemClock.elapsedRealtime() - mDownTime;
                    final float xDelta = ev.getX() - mXDown;
                    final float yDelta = ev.getY() - mYDown;

                    //Log.d("QAQ", "ACTION_MOVE " + timeDelta + " " + xDelta + " " + yDelta + " " + mConfiguration.getScaledTouchSlop());

                    // if not tap
                    if (timeDelta > 10
                            && Math.abs(xDelta) > mConfiguration.getScaledTouchSlop()) {
                        mInterceptTouch = true;

                        mDrawerContainer.dragStarted(isExpanded());
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                dragFinished();
                break;
        }

        return mInArea && mInterceptTouch;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent ev) {
        //Log.d("onTouchEvent", "" + ev.getActionMasked());

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                //Log.d("QAQ", "ACTION_MOVE " + mInArea);
                if (mInArea) {
                    setTransX(mSavedTransX + (int) (ev.getX() - mXDown));
                }
                break;
            case MotionEvent.ACTION_UP:
                dragFinished();
                break;
        }
        return mInArea && mInterceptTouch;
    }

    private void dragFinished() {
        if (Math.abs(mSavedTransX - mTransX) > 100) {
            toggle();
            return;
        }

        cancelAnimator();
        startAnimator();
    }

    public void setNavigationItemSelectedListener(NavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener) {
        mNavigationItemSelectedListener = navigationItemSelectedListener;
    }

    /**
     * Set drawer checked item by id
     *
     * <p><strong>Note:</strong> this will not call listener
     *
     * @param id menu id
     */

    public void setCheckedItem(int id) {
        mDrawerContainer.setCheckedItem(id);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState state = new SavedState(superState);
        state.drawerState = new Bundle();
        state.drawerState.putInt("MenuId", mDrawerContainer.getCheckedItemId());
        state.drawerState.putInt("ScrollY", mDrawerContainer.getScrollY());
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable savedState) {
        if (!(savedState instanceof SavedState)) {
            super.onRestoreInstanceState(savedState);
            return;
        }
        SavedState state = (SavedState) savedState;
        super.onRestoreInstanceState(state.getSuperState());
        int id = state.drawerState.getInt("MenuId", -1);
        if (id != -1) {
            mDrawerContainer.setCheckedItem(id);
        }
        mDrawerContainer.setScrollY(state.drawerState.getInt("ScrollY", 0));
    }

    public static class SavedState extends BaseSavedState {
        public Bundle drawerState;

        public SavedState(Parcel in, ClassLoader loader) {
            super(in);
            drawerState = in.readBundle(loader);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeBundle(drawerState);
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel parcel, ClassLoader loader) {
                return new SavedState(parcel, loader);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        });
    }
}
