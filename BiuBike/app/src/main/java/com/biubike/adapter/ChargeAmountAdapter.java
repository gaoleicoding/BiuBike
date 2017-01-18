package com.biubike.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.biubike.R;

/**
 * Created by gaolei on 17/1/18.
 */

public class ChargeAmountAdapter extends RecyclerView.Adapter<ChargeAmountAdapter.MyViewHolder> {

    public Context context;
    String[] accountArr;
    int selectPosition = 0;
    OnItemClickListener listener;

    public ChargeAmountAdapter(Context context) {
        this.context = context;
        accountArr = context.getResources().getStringArray(R.array.amount);
    }

    public void setSelectPosition(int selectPosition) {
        this.selectPosition = selectPosition;
        notifyDataSetChanged();
    }

    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.charge_amount_item, null);
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
        holder.textView.setText(accountArr[position]);
        holder.itemView.setTag(position);
        if (position == selectPosition) {
            holder.textView.setTextColor(Color.parseColor("#343333"));
            holder.textView.setBackgroundResource(R.drawable.charge_amount_select);
        } else {
            holder.textView.setTextColor(Color.parseColor("#989898"));
            holder.textView.setBackgroundResource(R.drawable.charge_amount_unselect);
        }
    }

    @Override
    public int getItemCount() {
        return accountArr.length;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public MyViewHolder(View view) {
            super(view);
            textView = (TextView) view.findViewById(R.id.acount);
        }
    }

    public interface OnItemClickListener {
        public void onItemClick(View v, int position);
    }

    public void setOnClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}