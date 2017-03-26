package com.biubike.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.biubike.R;
import com.biubike.bean.RouteRecord;

import java.util.List;

/**
 * Created by gaolei on 17/1/18.
 */

public class MyRouteAdapter extends RecyclerView.Adapter<MyRouteAdapter.MyViewHolder> {

    public Context context;
    int selectPosition = 0;
    OnItemClickListener listener;
    List<RouteRecord> list;

    public MyRouteAdapter(Context context, List<RouteRecord> list) {
        this.context = context;
        this.list = list;
    }

    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.my_route_item, null);
        MyViewHolder holder = new MyViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int position = (int) view.getTag();
                if (listener != null) {
                    listener.onItemClick(view, position);
                }
            }
        });
        return holder;
    }

    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.itemView.setTag(position);
        RouteRecord routeRecord=list.get(position);
        holder.bike_time.setText(routeRecord.getCycle_time()+"分钟");
        holder.bike_distance.setText(routeRecord.getCycle_distance()+"米");
        holder.bike_price.setText(routeRecord.getCycle_price()+"元");
        holder.bike_date.setText(routeRecord.getCycle_date());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView bike_time,bike_distance,bike_price,bike_date;

        public MyViewHolder(View view) {
            super(view);
            bike_time = (TextView) view.findViewById(R.id.bike_time);
            bike_distance = (TextView) view.findViewById(R.id.bike_distance);
            bike_price = (TextView) view.findViewById(R.id.bike_price);
            bike_date = (TextView) view.findViewById(R.id.bike_date);
        }
    }

    public interface OnItemClickListener {
        public void onItemClick(View v, int position);
    }

    public void setOnClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}