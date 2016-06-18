package rikka.minidrawer;

import android.content.Context;
import android.support.design.internal.NavigationMenu;
import android.support.design.widget.NavigationView;
import android.support.v7.view.SupportMenuInflater;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Rikka on 2016/5/25.
 */
class DrawerView extends LinearLayout implements DrawerItemView.OnDrawerItemClickedListener {
    private SupportMenuInflater mMenuInflater;

    private NavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener;

    private Menu mMenu;
    private Map<Integer, DrawerItemView> mMap;

    private boolean mMiniMode;
    private int mCheckedMenuId;

    public DrawerView(Context context) {
        this(context, null);
    }

    public DrawerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mMenu = new NavigationMenu(context);
        mMap = new HashMap<>();

        setOrientation(LinearLayout.VERTICAL);
    }

    public boolean isMiniMode() {
        return mMiniMode;
    }

    public void setMiniMode(boolean miniMode) {
        mMiniMode = miniMode;
    }

    public void setCheckedItem(int id) {
        setCheckedItem(mMap.get(id));
    }

    private void setCheckedItem(DrawerItemView item) {
        if (item != null) {
            clickItem(item.getMenuItem(), false);
        }
    }

    private MenuInflater getMenuInflater() {
        if (mMenuInflater == null) {
            mMenuInflater = new SupportMenuInflater(getContext());
        }
        return mMenuInflater;
    }

    public void inflateMenu(int resId) {
        removeAllViews();

        getMenuInflater().inflate(resId, mMenu);

        for (int i = 0; i < mMenu.size(); i++) {
            MenuItem item = mMenu.getItem(i);
            if (item.isVisible()) {
                setMenuItem(i, item);
            }
        }
    }

    private int mLastGroupId;

    private void setMenuItem(int index, MenuItem item) {
        if (index == 0) {
            mLastGroupId = item.getGroupId();
        } else if (mLastGroupId != item.getGroupId()) {
            mLastGroupId = item.getGroupId();

            inflate(getContext(), R.layout.drawer_divider, this/*mLinearLayout*/);
        }

        DrawerItemView drawerItemView = (DrawerItemView) LayoutInflater.from(getContext())
                .inflate(isMiniMode() ? R.layout.drawer_mini_item : R.layout.drawer_item, this/*mLinearLayout*/, false);
        drawerItemView.setMenuItem(item);
        drawerItemView.setOnDrawerItemClickedListener(this);
        /*mLinearLayout.*/addView(drawerItemView);

        mMap.put(item.getItemId(), drawerItemView);
    }

    @Override
    public void onClick(MenuItem item) {
        clickItem(item, true);
    }

    private void clickItem(MenuItem item, boolean listener) {
        if (listener && mOnNavigationItemSelectedListener != null) {
            mOnNavigationItemSelectedListener.onNavigationItemSelected(item);
        }

        mCheckedMenuId = item.getItemId();

        if (!item.isCheckable()) {
            return;
        }

        int id = item.getItemId();
        int groupId = item.getGroupId();

        for (Map.Entry<Integer, DrawerItemView> entry : mMap.entrySet()) {
            if (entry.getValue().getMenuItem().getGroupId() != groupId) {
                continue;
            }

            entry.getValue().setChecked(entry.getKey() == id);
        }
    }

    public void setTextAlpha(float alpha) {
        for (Map.Entry<Integer, DrawerItemView> entry : mMap.entrySet()) {
            entry.getValue().setTextAlpha(alpha);
        }
    }

    public void setNavigationItemSelectedListener(NavigationView.OnNavigationItemSelectedListener onDrawerItemClickedListener) {
        mOnNavigationItemSelectedListener = onDrawerItemClickedListener;
    }

    public int getCheckedItemId() {
        return mCheckedMenuId;
    }
}
