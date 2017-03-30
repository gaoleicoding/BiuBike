package com.biubike.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baidu.mapapi.search.sug.SuggestionResult.SuggestionInfo;
import com.biubike.R;

import java.util.List;

/**
 * Created by gaolei on 17/1/18.
 */

public class PoiSuggestionAdapter extends RecyclerView.Adapter<PoiSuggestionAdapter.MyViewHolder> {

    public Context context;
    OnItemClickListener listener;
    private List<SuggestionInfo> list;
    String from;

    public PoiSuggestionAdapter(Context context, List<SuggestionInfo> list,String from) {
        this.context = context;
        this.list = list;
        this.from=from;
    }

    public void changeData(List<SuggestionInfo> list) {
        if (list == null)
            this.list.clear();
        else
            this.list = list;
        notifyDataSetChanged();
    }

    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.poi_suggestion_item, null);
        MyViewHolder holder = new MyViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = (int) view.getTag();
                if (listener != null) {
                    listener.onItemClick(view, position,from,list.get(position));
                }
            }
        });
        return holder;
    }

    public void onBindViewHolder(MyViewHolder holder, int position) {
        SuggestionInfo info = list.get(position);
        holder.place.setText(info.key);
        holder.district.setText(info.city + " " + info.district);
        holder.itemView.setTag(position);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView place, district;

        public MyViewHolder(View view) {
            super(view);
            place = (TextView) view.findViewById(R.id.place);
            district = (TextView) view.findViewById(R.id.district);
        }
    }

    public interface OnItemClickListener {
        public void onItemClick(View v, int position,String flag,SuggestionInfo info);
    }

    public void setOnClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}