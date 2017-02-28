package com.biubike.activity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.biubike.R;
import com.biubike.adapter.MyRouteAdapter;
import com.biubike.adapter.MyRouteDividerDecoration;
import com.biubike.base.BaseActivity;
import com.biubike.bean.RouteRecord;
import com.biubike.database.RouteDBHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaolei on 16/12/29.
 */

public class MyRouteActivity extends BaseActivity implements MyRouteAdapter.OnItemClickListener {

    RecyclerView routeRecyclerView;
    List<RouteRecord> routeList;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_route);
        setStatusBar();
        routeRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_route);
        routeRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        routeList = getAllPoints();
        if(routeList!=null) {
            MyRouteAdapter routeAdapter = new MyRouteAdapter(this, routeList);
            routeRecyclerView.setAdapter(routeAdapter);
            routeRecyclerView.addItemDecoration(new MyRouteDividerDecoration(10));

            routeAdapter.setOnClickListener(this);
        }
    }

    public List<RouteRecord> getAllPoints() {
        String sql = "select * from cycle_route";
        RouteDBHelper helper = new RouteDBHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();
        routeList = new ArrayList<RouteRecord>();
        Cursor cursor = db.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            RouteRecord point = new RouteRecord();
            point.setCycle_date(cursor.getString(cursor
                    .getColumnIndex("cycle_date")));
            point.setCycle_time(cursor.getString(cursor
                    .getColumnIndex("cycle_time")));
            point.setCycle_distance(cursor.getString(cursor
                    .getColumnIndex("cycle_distance")));
            point.setCycle_price(cursor.getString(cursor
                    .getColumnIndex("cycle_price")));
            point.setCycle_points(cursor.getString(cursor
                    .getColumnIndex("cycle_points")));
            routeList.add(point);
        }
        return routeList;
    }


    @Override
    public void onItemClick(View v, int position) {
        Intent intent = new Intent(MyRouteActivity.this, RouteDetailActivity.class);
        RouteRecord routeRecord=routeList.get(position);
//        bundle.putParcelable("routeContent",routeRecord );
        Log.d("gaolei","getCycle_date------pass-------"+routeRecord.getCycle_date());
        Log.d("gaolei","getCycle_points----pass---------"+routeRecord.getCycle_points());
        Bundle bundle = new Bundle();
        bundle.putString("totalTime", routeRecord.getCycle_time());
        bundle.putString("totalDistance", routeRecord.getCycle_distance());
        bundle.putString("totalPrice", routeRecord.getCycle_price());
        bundle.putString("routePoints", routeRecord.getCycle_points());
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
