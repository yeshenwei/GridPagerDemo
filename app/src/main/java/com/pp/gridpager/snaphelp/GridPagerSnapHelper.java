package com.pp.gridpager.snaphelp;

import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.RecyclerView.State;
import android.support.v7.widget.RecyclerView.SmoothScroller.ScrollVectorProvider;
import android.support.v7.widget.SnapHelper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

public class GridPagerSnapHelper extends SnapHelper {
    @Nullable
    private OrientationHelper mVerticalHelper;
    @Nullable
    private OrientationHelper mHorizontalHelper;
    private RecyclerView mRecyclerView;
    private int mRow = 1;
    private int mColumn = 1;


    public GridPagerSnapHelper(int colum, int row) {
        this.mColumn = colum;
        this.mRow = row;
    }

    @Override
    public void attachToRecyclerView(@Nullable RecyclerView recyclerView) throws IllegalStateException {
        super.attachToRecyclerView(recyclerView);
        this.mRecyclerView = recyclerView;
    }

    /**
     * get the page of position
     *
     * @param position
     * @return
     */
    private int pageIndex(int position) {
        return position / countOfPage();
    }

    /**
     * the total count of per page
     *
     * @return
     */
    private int countOfPage() {
        return mRow * mColumn;
    }

    @Nullable
    @Override
    public int[] calculateDistanceToFinalSnap(@NonNull LayoutManager layoutManager, @NonNull View targetView) {
        int[] out = new int[2];
        if (layoutManager.canScrollHorizontally()) {
            out[0] = distanceToCenter(layoutManager, targetView, this.getHorizontalHelper(layoutManager));
        } else {
            out[0] = 0;
        }

        if (layoutManager.canScrollVertically()) {
            out[1] = distanceToCenter(layoutManager, targetView, this.getVerticalHelper(layoutManager));
        } else {
            out[1] = 0;
        }

        return out;
    }

    @Nullable
    @Override
    public View findSnapView(LayoutManager layoutManager) {
        if (layoutManager.canScrollVertically()) {
            return findCenterView(layoutManager, getVerticalHelper(layoutManager));
        } else if (layoutManager.canScrollHorizontally()) {
            return findCenterView(layoutManager, getHorizontalHelper(layoutManager));
        }
        return null;
    }

    @Override
    public int findTargetSnapPosition(LayoutManager layoutManager, int velocityX, int velocityY) {

        final int itemCount = layoutManager.getItemCount();
        if (itemCount == 0) {
            return RecyclerView.NO_POSITION;
        }

        View mStartMostChildView = null;
        if (layoutManager.canScrollVertically()) {
            mStartMostChildView = this.findStartView(layoutManager, this.getVerticalHelper(layoutManager));
        } else if (layoutManager.canScrollHorizontally()) {
            mStartMostChildView = this.findStartView(layoutManager, this.getHorizontalHelper(layoutManager));
        }


        if (mStartMostChildView == null) {
            return RecyclerView.NO_POSITION;
        }

        final int centerPosition = layoutManager.getPosition(mStartMostChildView);
        if (centerPosition == RecyclerView.NO_POSITION) {
            return RecyclerView.NO_POSITION;
        }

        // 是否滑向下一页
        boolean forwardDirection;
        if (layoutManager.canScrollHorizontally()) {
            forwardDirection = velocityX > 0;
        } else {
            forwardDirection = velocityY > 0;
        }

        //　条目是否是翻转模式
        boolean reverseLayout = false;
        if (layoutManager instanceof ScrollVectorProvider) {
            ScrollVectorProvider vectorProvider = (ScrollVectorProvider) layoutManager;
            PointF vectorForEnd = vectorProvider.computeScrollVectorForPosition(itemCount - 1);
            if (vectorForEnd != null) {
                reverseLayout = vectorForEnd.x < 0.0F || vectorForEnd.y < 0.0F;
            }
        }

        final int pageIndex = pageIndex(centerPosition);
        final int currentPageStart = pageIndex * countOfPage();

        return reverseLayout
                ? (forwardDirection ? currentPageStart - countOfPage() : currentPageStart)
                : (forwardDirection ? currentPageStart + countOfPage() : (currentPageStart + countOfPage() - 1));

    }

    @Override
    protected LinearSmoothScroller createSnapScroller(LayoutManager layoutManager) {
        return !(layoutManager instanceof ScrollVectorProvider) ? null : new LinearSmoothScroller(this.mRecyclerView.getContext()) {
            protected void onTargetFound(View targetView, State state, Action action) {
                int[] snapDistances = calculateDistanceToFinalSnap(mRecyclerView.getLayoutManager(), targetView);
                int dx = snapDistances[0];
                int dy = snapDistances[1];
                int time = this.calculateTimeForDeceleration(Math.max(Math.abs(dx), Math.abs(dy)));
                if (time > 0) {
                    action.update(dx, dy, time, this.mDecelerateInterpolator);
                }

            }

            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return 100.0F / (float) displayMetrics.densityDpi;
            }

            protected int calculateTimeForScrolling(int dx) {
                return Math.min(100, super.calculateTimeForScrolling(dx));
            }
        };
    }

    private int distanceToCenter(@NonNull LayoutManager layoutManager, @NonNull View targetView, OrientationHelper helper) {


        if (layoutManager.canScrollHorizontally()) {
            int totalWidth = mRecyclerView.getWidth();
            //单个item的宽度
            int columnWidth = totalWidth / mColumn;

            int position = layoutManager.getPosition(targetView);
            int pageIndex = pageIndex(position);
            int currentPageStart = pageIndex * countOfPage();

            int distance = ((position - currentPageStart) / mRow) * columnWidth;
            final int childStart = helper.getDecoratedStart(targetView);

            Log.d("troy","position:" + position
                    + " currentPageStart:" + currentPageStart
                    + " distance:" + distance
                    + " childStart:" + childStart);

            return childStart - distance;
        } else {//数值方向
            int totalHeight = mRecyclerView.getHeight();

            int rowHeight = totalHeight / mRow;

            int position = layoutManager.getPosition(targetView);
            int pageIndex = pageIndex(position);

            int currentPageStart = pageIndex * countOfPage();

            int distance = ((position - currentPageStart) / mColumn) * rowHeight;

            final int childStart = helper.getDecoratedStart(targetView);
            return childStart - distance;
        }

    }

    @Nullable
    private View findCenterView(LayoutManager layoutManager, OrientationHelper helper) {
        int childCount = layoutManager.getChildCount();
        if (childCount == 0) {
            return null;
        }

        View closestChild = null;
        final int center;
        if (layoutManager.getClipToPadding()) {
            center = helper.getStartAfterPadding() + helper.getTotalSpace() / 2;
        } else {
            center = helper.getEnd() / 2;
        }
        int absClosest = Integer.MAX_VALUE;

        for (int i = 0; i < childCount; i++) {
            final View child = layoutManager.getChildAt(i);
            int childCenter = helper.getDecoratedStart(child)
                    + (helper.getDecoratedMeasurement(child) / 2);
            int absDistance = Math.abs(childCenter - center);

            /** if child center is closer than previous closest, set it as closest  **/
            if (absDistance < absClosest) {
                absClosest = absDistance;
                closestChild = child;
            }
        }

        return closestChild;
        }


    @Nullable
    private View findStartView(LayoutManager layoutManager, OrientationHelper helper) {
        int childCount = layoutManager.getChildCount();
        if (childCount == 0) {
            return null;
        } else {
            View closestChild = null;
            int startest = 2147483647;

            for (int i = 0; i < childCount; ++i) {
                View child = layoutManager.getChildAt(i);
                int childStart = helper.getDecoratedStart(child);
                if (childStart < startest) {
                    startest = childStart;
                    closestChild = child;
                }
            }

            return closestChild;
        }
    }

    @NonNull
    private OrientationHelper getVerticalHelper(@NonNull LayoutManager layoutManager) {
        if (this.mVerticalHelper == null || this.mVerticalHelper.getLayoutManager() != layoutManager) {
            this.mVerticalHelper = OrientationHelper.createVerticalHelper(layoutManager);
        }

        return this.mVerticalHelper;
    }

    @NonNull
    private OrientationHelper getHorizontalHelper(@NonNull LayoutManager layoutManager) {
        if (this.mHorizontalHelper == null || this.mHorizontalHelper.getLayoutManager() != layoutManager) {
            this.mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager);
        }
        return this.mHorizontalHelper;
    }
}
