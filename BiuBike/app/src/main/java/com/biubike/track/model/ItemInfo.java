package com.biubike.track.model;


import android.app.Activity;

/**
 * Created by baidu on 17/1/13.
 */

public class ItemInfo {
    public int titleIconId;
    public int titleId;
    public int descId;
    public Class<? extends Activity> clazz;

    public ItemInfo(int titleIconId, int titleId, int descId, Class<? extends Activity> clazz) {
        this.titleIconId = titleIconId;
        this.titleId = titleId;
        this.descId = descId;
        this.clazz = clazz;
    }
}
