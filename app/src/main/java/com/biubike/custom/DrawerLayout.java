package com.biubike.custom;

import android.content.Context;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Scroller;

import com.biubike.callback.AllInterface;

/**
 * Created by gaolei on 17/1/5.
 */

public class DrawerLayout extends LinearLayout {
    private static final int MIN_DRAWER_MARGIN = 64; // dp
    /**
     * Minimum velocity that will be detected as a fling
     */
    private static final int MIN_FLING_VELOCITY = 400; // dips per second

    /**
     * drawer离父容器右边的最小外边距
     */
    private int mMinDrawerMargin;

    private View mLeftMenuView;
    private View mContentView;

    private ViewDragHelper mHelper;
    /**
     * drawer显示出来的占自身的百分比
     */
    public float mLeftMenuOnScrren;
    public static boolean isDrawerOpen = false;
    AllInterface.OnMenuSlideListener onMenuSlideListener;
    Scroller mScroller;

    public void setListener(AllInterface.OnMenuSlideListener onMenuSlideListener) {
        this.onMenuSlideListener = onMenuSlideListener;
    }


    public DrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        //setup drawer's minMargin
        mScroller = new Scroller(context);

        final float density = getResources().getDisplayMetrics().density;
        final float minVel = MIN_FLING_VELOCITY * density;
        mMinDrawerMargin = (int) (MIN_DRAWER_MARGIN * density + 0.5f);


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(widthSize, heightSize);

        View leftMenuView = getChildAt(1);
        MarginLayoutParams lp = (MarginLayoutParams)
                leftMenuView.getLayoutParams();

        final int drawerWidthSpec = getChildMeasureSpec(widthMeasureSpec,
                mMinDrawerMargin + lp.leftMargin + lp.rightMargin,
                lp.width);
        final int drawerHeightSpec = getChildMeasureSpec(heightMeasureSpec,
                lp.topMargin + lp.bottomMargin,
                lp.height);
        leftMenuView.measure(drawerWidthSpec, drawerHeightSpec);


        View contentView = getChildAt(0);
        lp = (MarginLayoutParams) contentView.getLayoutParams();
        final int contentWidthSpec = MeasureSpec.makeMeasureSpec(
                widthSize - lp.leftMargin - lp.rightMargin, MeasureSpec.EXACTLY);
        final int contentHeightSpec = MeasureSpec.makeMeasureSpec(
                heightSize - lp.topMargin - lp.bottomMargin, MeasureSpec.EXACTLY);
        contentView.measure(contentWidthSpec, contentHeightSpec);

        mLeftMenuView = leftMenuView;
        mContentView = contentView;


    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean shouldInterceptTouchEvent = mHelper.shouldInterceptTouchEvent(ev);
        return shouldInterceptTouchEvent;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mHelper.processTouchEvent(event);
        return true;
    }


    @Override
    public void computeScroll() {
        if (mHelper.continueSettling(true)) {
            invalidate();
        }
    }

    public void closeDrawer() {
        View menuView = mLeftMenuView;

        menuView.scrollTo(-menuView.getWidth(), menuView.getTop());
        invalidate();
        mLeftMenuOnScrren = 0.0f;
    }

    public void openDrawer() {

        View menuView = mLeftMenuView;
        menuView.scrollTo(0, menuView.getTop());
        mLeftMenuOnScrren = 1.0f;

        invalidate();
        isDrawerOpen = true;
    }

}
