package com.biubike.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by gaolei on 17/2/27.
 */

public class RouteDBHelper extends SQLiteOpenHelper {

    public RouteDBHelper(Context context) {
        super(context, "route_history.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
//创建一个数据库
        db.execSQL("CREATE TABLE IF NOT EXISTS  cycle_route (route_id integer primary key autoincrement ," +
                "cycle_date text not null ," +
                "cycle_time text not null ," +
                "cycle_distance text not null ," +
                "cycle_price text not null ," +
                "cycle_points text not null )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
