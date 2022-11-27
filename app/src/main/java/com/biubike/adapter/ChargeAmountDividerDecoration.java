package com.biubike.adapter;

/**
 * Created by gaolei on 17/1/18.
 */

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;


/**
 * This class is from the v7 samples of the Android SDK. It's not by me!
 * <p/>
 * See the license above for details.
 */
public class ChargeAmountDividerDecoration extends RecyclerView.ItemDecoration {

    private int space;

    public ChargeAmountDividerDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {

        outRect.set(0, 20, 20, 20);

    }
}
