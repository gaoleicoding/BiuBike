package com.biubike.custom;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.nineoldandroids.view.ViewHelper;

public class SlideLayout extends ViewGroup {
	private static String TAG = "SlideMenuLayout";
	
	private Context mContext;
	private Scroller mScroller;    //Android 提供的滑动辅助类
	private int mTouchSlop = 0 ;    //在被判定为滚动之前用户手指可以移动的最大值
	private VelocityTracker mVelocityTracker;    //用于计算手指滑动的速度
	public static final int SNAP_VELOCITY = 200;    //滚动显示和隐藏左侧布局时，手指滑动需要达到的速度：每秒200个像素点
	private int mMaxScrollX = 0;    //最大滚动距离，等于menu的宽度
	private int menuWidth=0;
	private int scrollX=0;
	private int oldX;
	public static boolean clickMenuToClose=false;
	
	public void setMaxScrollX(int maxScrollX) {
		this.mMaxScrollX = maxScrollX;
	}

	private float mDownX;    //一次按下抬起的动作中，按下时的X坐标，用于和抬起时的X比较，判断移动距离。少于mTouchSlop则判定为原地点击
	private float mLastX;    //记录滑动过程中的X坐标
	
	private boolean isMenuOpen = false;    //菜单界面是否被打开，只有完全打开才为true
	public boolean isMenuOpen() {
		return isMenuOpen;
	}

	private boolean isTouchFinished = true;
	
	private View mContent;
	
	public SlideLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}
	
	private void init() {
		Log.v(TAG, "init start");
		mScroller = new Scroller(mContext);
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			scrollX=mScroller.getCurrX();
//			这里是判断打开菜单时即向右滑动菜单向右滑动 实现视觉差滑动效果，scrollX就是监听startScroll开始滚动程X的值，因为getScrollX()只是监听手在屏幕上滑动的过程，手离开后就要在这里监听，0.7是可以根据需求改动
			if(scrollX<oldX){
//			Log.d("gaolei", "scrollX2--------------------------------|"+scrollX);
			ViewHelper.setTranslationX(getChildAt(0), (menuWidth+scrollX)* 0.7f);
			}
//			这里是判断点击菜单按钮关闭时，对菜单做处理，已达到视觉差效果，不然的话没有
			if(clickMenuToClose){
				ViewHelper.setTranslationX(getChildAt(0), (menuWidth+scrollX)* 0.7f);
			}
			oldX=scrollX;
			postInvalidate();
			
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		setMeasuredDimension(width, height);

		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			child.measure(widthMeasureSpec, heightMeasureSpec);
		}
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		mContent = getChildAt(1);
		if (mContent != null) {
			mContent.layout(l, t, l + mContent.getMeasuredWidth(), t + mContent.getMeasuredHeight());
		}
		
		 menuWidth = 0;
		View menu = getChildAt(0);
		if (menu != null) {
			ViewGroup.LayoutParams layoutParams = menu.getLayoutParams();
			menuWidth = layoutParams.width;
			menu.layout(l - menuWidth, t, l, t + menu.getMeasuredHeight());
		}
	
		
	}
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		createVelocityTracker(event);
		int curScrollX = getScrollX();
		// 检查触摸点是否在滑动布局(内容content)中，如果不是则返回false，即本View不处理该事件
		if (mContent != null && isTouchFinished) {
			Rect rect = new Rect();
			mContent.getHitRect(rect);
			if (!rect.contains((int)event.getX() + curScrollX, (int)event.getY())) {
				return false;
			}
		}

		float x = event.getX();    //取得本次event的X坐标
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mDownX = x;
			mLastX = x;
			isTouchFinished = false;
			break;
		case MotionEvent.ACTION_MOVE:
			int deltaX = (int)(mLastX - x);
			if((curScrollX + deltaX) < -mMaxScrollX) {
				deltaX = -mMaxScrollX - curScrollX;
			}
			if((curScrollX + deltaX) > 0){
				deltaX = -curScrollX;
			}
//			这里是判断打手势滑动开菜单时即向右滑动菜单向右滑动 实现视觉差滑动效果，menuWidth+getScrollX()是菜单滑动时X的变化，手离开后就要在这里监听，0.7是可以根据需求改动
		
			
			if (deltaX != 0) {
				scrollBy(deltaX, 0);
			}
			//这句话一定要放到scrollBy后面 不然你快速滑动左边会有空白闪现
			ViewHelper.setTranslationX(getChildAt(0), (menuWidth+getScrollX())* 0.7f);
			mLastX = x;
			
			break;
		case MotionEvent.ACTION_UP:
			int velocityX = getScrollVelocity();
			int offsetX = (int) (x - mDownX);
			
			//成立表明移动距离已经达到被判断为滑动的最低标准
			//不成立表明不被判断为滑动，则认为是单一的点击，则关闭menu
			if(Math.abs(offsetX) >= mTouchSlop) {
				
				//成立表明手指移动速度达标，则进行自动滑动；
				//不成立表明速度不达标，但仍然需要判断当前SlideLayout的位置
				//如果已经超过一半，则继续自动完成剩下的滑动，如果没有超过一半，则反向滑动
				if(Math.abs(velocityX) >= SNAP_VELOCITY) {
					if(velocityX > 0){
						openMenu();
					} else if(velocityX < 0) {
						closeMenu();
					}
				} else {
					if (curScrollX >= -mMaxScrollX / 2) {
						closeMenu();
					} else {
						openMenu();
					}
				}
			} else {
				closeMenu();
			}

			recycleVelocityTracker();
			isTouchFinished = true;
			break;
		}
		return true;
	}
	
	private void createVelocityTracker(MotionEvent event) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);
	}
	
	//获取手指在View上的滑动速度,以每秒钟移动了多少像素值为单位
	private int getScrollVelocity() {
		mVelocityTracker.computeCurrentVelocity(1000);
		return (int) mVelocityTracker.getXVelocity();
	}
	
	private void recycleVelocityTracker() {
		mVelocityTracker.recycle();
		mVelocityTracker = null;
	}
	
	//打开Menu布局
	public void openMenu() {
		int curScrollX = getScrollX();
		
		scrollToDestination(-mMaxScrollX - curScrollX);
		isMenuOpen = true;
	}
	
	//关闭Menu布局
	public void closeMenu() {
		int curScrollX = getScrollX();
		scrollToDestination(-curScrollX);
		isMenuOpen = false;
	}
	
	private void scrollToDestination(int x) {
		if (x == 0)
			return;
		mScroller.startScroll(getScrollX(), 0, x, 0, Math.abs(x));
		invalidate();
	}
}