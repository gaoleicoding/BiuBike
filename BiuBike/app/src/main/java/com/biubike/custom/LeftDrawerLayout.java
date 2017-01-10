package com.biubike.custom;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.biubike.callback.AllInterface;

/**
 * Created by gaolei on 17/1/5.
 */

public class LeftDrawerLayout extends ViewGroup {
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
    private boolean once;

    public void setListener(AllInterface.OnMenuSlideListener onMenuSlideListener) {
        this.onMenuSlideListener = onMenuSlideListener;

    }


    public LeftDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        //setup drawer's minMargin
        final float density = getResources().getDisplayMetrics().density;
        final float minVel = MIN_FLING_VELOCITY * density;
        mMinDrawerMargin = (int) (MIN_DRAWER_MARGIN * density + 0.5f);

        mHelper = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {
            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                int newLeft = Math.max(-child.getWidth(), Math.min(left, 0));
                return newLeft;
            }

            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                return child == mLeftMenuView;
            }

            @Override
            public void onEdgeDragStarted(int edgeFlags, int pointerId) {
                mHelper.captureChildView(mLeftMenuView, pointerId);
            }

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                final int childWidth = releasedChild.getWidth();
                float offset = (childWidth + releasedChild.getLeft()) * 1.0f / childWidth;
                mHelper.settleCapturedViewAt(xvel > 0 || xvel == 0 && offset > 0.5f ? 0 : -childWidth, releasedChild.getTop());
                invalidate();
            }

            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                final int childWidth = changedView.getWidth();
                float offset = (float) (childWidth + left) / childWidth;
//                Log.d("gaolei", "offset-------------------" + offset);
//                if(offset==0)
//                    closeDrawer();
                mLeftMenuOnScrren = offset;
                //offset can callback here
                onMenuSlideListener.onMenuSlide(offset);
                if (offset == 0)
                    isDrawerOpen = false;
                if (offset == 1)
                    isDrawerOpen = true;

                changedView.setVisibility(offset == 0 ? View.GONE : View.VISIBLE);
                invalidate();
            }

            @Override
            public int getViewHorizontalDragRange(View child) {
                return mLeftMenuView == child ? child.getWidth() : 0;
            }
        });
        //设置edge_left track
        mHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT);
        //设置minVelocity
        mHelper.setMinVelocity(minVel);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        Log.d("gaolei", "onMeasure-------------------");
        setMeasuredDimension(widthSize, heightSize);

        mLeftMenuView = getChildAt(1);
        MarginLayoutParams lp = (MarginLayoutParams)
                mLeftMenuView.getLayoutParams();

        final int drawerWidthSpec = getChildMeasureSpec(widthMeasureSpec,
                mMinDrawerMargin + lp.leftMargin + lp.rightMargin,
                lp.width);
        final int drawerHeightSpec = getChildMeasureSpec(heightMeasureSpec,
                lp.topMargin + lp.bottomMargin,
                lp.height);
        mLeftMenuView.measure(drawerWidthSpec, drawerHeightSpec);


        mContentView = getChildAt(0);
        lp = (MarginLayoutParams) mContentView.getLayoutParams();
        final int contentWidthSpec = MeasureSpec.makeMeasureSpec(
                widthSize - lp.leftMargin - lp.rightMargin, MeasureSpec.EXACTLY);
        final int contentHeightSpec = MeasureSpec.makeMeasureSpec(
                heightSize - lp.topMargin - lp.bottomMargin, MeasureSpec.EXACTLY);
        mContentView.measure(contentWidthSpec, contentHeightSpec);


    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        View menuView = mLeftMenuView;
        View contentView = mContentView;
        Log.d("gaolei", "onLayout-------------------");

        MarginLayoutParams lp = (MarginLayoutParams) contentView.getLayoutParams();
        contentView.layout(lp.leftMargin, lp.topMargin,
                lp.leftMargin + contentView.getMeasuredWidth(),
                lp.topMargin + contentView.getMeasuredHeight());

        lp = (MarginLayoutParams) menuView.getLayoutParams();

        final int menuWidth = menuView.getMeasuredWidth();
        int childLeft = -menuWidth + (int) (menuWidth * mLeftMenuOnScrren);
        menuView.layout(childLeft, lp.topMargin, childLeft + menuWidth,
                lp.topMargin + menuView.getMeasuredHeight());
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
//      if (isDrawerOpen) {
        View menuView = mLeftMenuView;
        mLeftMenuOnScrren = 0.f;
        if (mHelper.smoothSlideViewTo(menuView, -menuView.getWidth(), menuView.getTop())) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
        isDrawerOpen = false;
//      }
    }

    public void openDrawer() {
//      if (!isDrawerOpen) {
        View menuView = mLeftMenuView;
        mLeftMenuOnScrren = 1.0f;
        if (mHelper.smoothSlideViewTo(menuView, 0, menuView.getTop())) {
            // 注意：参数传递根ViewGroup
            ViewCompat.postInvalidateOnAnimation(this);
        }

        isDrawerOpen = true;
//        }
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new MarginLayoutParams(p);
    }

}
