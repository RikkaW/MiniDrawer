package rikka.minidrawer;

import android.content.Context;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * Created by Rikka on 2016/5/27.
 */
class DrawerContainer extends NestedScrollView implements NavigationView.OnNavigationItemSelectedListener {
    private FrameLayout mFrameLayout;

    private DrawerView mMiniDrawer;
    private DrawerView mDrawer;

    private NavigationView.OnNavigationItemSelectedListener mNavigationItemSelectedListener;

    private int mWidthMini;
    private int mWidthExpanded;

    public DrawerContainer(Context context) {
        this(context, null);
    }

    public DrawerContainer(Context context, AttributeSet attrs) {
        super(context, attrs);

        mFrameLayout = new FrameLayout(getContext());
        mFrameLayout.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        addView(mFrameLayout);
    }

    public void init(int menuResourceId, int width, int widthExpanded, int padding) {
        mWidthMini = width;
        mWidthExpanded = widthExpanded;

        mMiniDrawer = new DrawerView(getContext());
        mMiniDrawer.setLayoutParams(new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT));
        mMiniDrawer.setPadding(0, padding, 0, padding);
        mMiniDrawer.setClipToPadding(false);
        mMiniDrawer.setMiniMode(true);
        if (menuResourceId != -1) {
            mMiniDrawer.inflateMenu(menuResourceId);
        }

        mDrawer = new DrawerView(getContext());
        mDrawer.setLayoutParams(new LinearLayout.LayoutParams(widthExpanded, ViewGroup.LayoutParams.WRAP_CONTENT));
        mDrawer.setPadding(0, padding, 0, padding);
        mDrawer.setClipToPadding(false);
        if (menuResourceId != -1) {
            mDrawer.inflateMenu(menuResourceId);
        }

        mDrawer.setNavigationItemSelectedListener(this);
        mMiniDrawer.setNavigationItemSelectedListener(this);

        mFrameLayout.addView(mMiniDrawer);
        mFrameLayout.addView(mDrawer);

        setLayoutParams(new NestedScrollView.LayoutParams(mWidthMini, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    public boolean onNavigationItemSelected(MenuItem item) {
        setCheckedItem(item.getItemId());

        if (mNavigationItemSelectedListener != null) {
            mNavigationItemSelectedListener.onNavigationItemSelected(item);
        }
        return false;
    }

    public void setCheckedItem(int id) {
        mMiniDrawer.setCheckedItem(id);
        mDrawer.setCheckedItem(id);
    }

    public void setDrawerByTransX(int x, int width) {
        float percent = (float) x / width;

        if (percent < 0.4 && percent >= 0.05) {
            mDrawer.setTextAlpha((percent - 0.05f) * 1 / 0.35f);
        } else {
            mDrawer.setTextAlpha(1);
        }

        mDrawer.setAlpha(1);
        mMiniDrawer.setAlpha(1);

        if (percent < 0.05) {
            mMiniDrawer.setVisibility(VISIBLE);
            mDrawer.setVisibility(GONE);
        } else {
            mMiniDrawer.setVisibility(GONE);
            mDrawer.setVisibility(VISIBLE);
        }
    }

    public void dragStarted(boolean expanded) {
        if (!expanded) {
            mMiniDrawer.setVisibility(GONE);
            mDrawer.setVisibility(VISIBLE);
            mDrawer.setTextAlpha(0);

            getLayoutParams().width = mWidthExpanded;
        }
    }

    public void animationStart(boolean expanded) {
        if (expanded) {
            getLayoutParams().width = mWidthExpanded;
        }
    }

    public void animationEnd(boolean expanded) {
        if (!expanded) {
            mMiniDrawer.setVisibility(VISIBLE);
            mDrawer.setVisibility(GONE);
            //mDrawer.setScrollY(mSavedScrollY);
        }

        getLayoutParams().width = expanded ? mWidthExpanded : mWidthMini;
    }

    public void setNavigationItemSelectedListener(NavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener) {
        mNavigationItemSelectedListener = navigationItemSelectedListener;
    }
}
