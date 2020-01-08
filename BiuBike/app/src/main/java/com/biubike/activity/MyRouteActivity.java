package com.biubike.activity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.TextView;

import com.biubike.R;
import com.biubike.adapter.MyRouteAdapter;
import com.biubike.adapter.MyRouteDividerDecoration;
import com.biubike.base.BaseActivity;
import com.biubike.bean.RouteRecord;
import com.biubike.database.RouteDBHelper;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaolei on 16/12/29.
 */

public class MyRouteActivity extends BaseActivity implements MyRouteAdapter.OnItemClickListener {

    private XRecyclerView routeRecyclerView;
    private MyRouteAdapter routeAdapter;
    private List<RouteRecord> routeList;
    private String TABLE_NAME = "cycle_route";
    private int PageId = 0, PageSize = 10;
    private long itemCount = 0;
    private SQLiteDatabase db;
    private TextView no_route;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_route);
        routeRecyclerView = findViewById(R.id.recyclerview_route);
        no_route = findViewById(R.id.no_route);
        routeRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        routeList = new ArrayList<>();

        RouteDBHelper helper = new RouteDBHelper(this);
        db = helper.getWritableDatabase();
        itemCount = getItemCount();
        routeList = loadPage();
        if (routeList != null) {
            routeAdapter = new MyRouteAdapter(this, routeList);
            routeRecyclerView.setAdapter(routeAdapter);
            routeRecyclerView.addItemDecoration(new MyRouteDividerDecoration(10));
            routeAdapter.setOnClickListener(this);
        } else {
            no_route.setVisibility(View.VISIBLE);
        }

        routeRecyclerView.setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader);
        routeRecyclerView.setLoadingMoreProgressStyle(ProgressStyle.BallScale);
        routeRecyclerView.setArrowImageView(R.drawable.iconfont_downgrey);
        routeRecyclerView.setPullRefreshEnabled(false);

        routeRecyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                routeRecyclerView.refreshComplete();
            }

            @Override
            public void onLoadMore() {
                loadPage();
                routeRecyclerView.loadMoreComplete();
                routeAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onItemClick(View v, int position) {
        Intent intent = new Intent(MyRouteActivity.this, RouteDetailActivity.class);
        RouteRecord routeRecord = routeList.get(position);
        intent.putExtra("routeRecord", routeRecord);
        startActivity(intent);
    }


    /*
     * 读取指定ID的分页数据
     * SQL:Select * From TABLE_NAME Limit 9 Offset 10;
     * 表示从TABLE_NAME表获取数据，跳过10行，取9行
     */
    public List<RouteRecord> loadPage() {
        if (routeList.size() >= itemCount) {
            routeRecyclerView.setNoMore(true);
            return null;
        }
        String sql = "select * from " + TABLE_NAME + " order by route_id DESC" +
                " " + "limit " + String.valueOf(PageSize) + " offset " + PageId * PageSize;
        Cursor cursor = db.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            RouteRecord point = new RouteRecord();
            point.setCycleDate(cursor.getString(cursor
                    .getColumnIndex("cycle_date")));
            point.setCycleStartTime(cursor.getLong(cursor
                    .getColumnIndex("cycle_start_time")));
            point.setCycleEndTime(cursor.getLong(cursor
                    .getColumnIndex("cycle_end_time")));
            point.setCycleTime(cursor.getString(cursor
                    .getColumnIndex("cycle_time")));
            point.setCycleDistance(cursor.getString(cursor
                    .getColumnIndex("cycle_distance")));
            point.setCyclePrice(cursor.getString(cursor
                    .getColumnIndex("cycle_price")));

            routeList.add(point);
        }
        PageId++;
        cursor.close();
        return routeList;

    }

    public long getItemCount() {
        String sql = "select count(*) from " + TABLE_NAME;
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        long count = cursor.getLong(0);
        cursor.close();
        return count;
    }

    public void onDestroy() {
        super.onDestroy();

    }
}
