package com.biubike.activity;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.biubike.R;
import com.biubike.adapter.ChargeAmountAdapter;
import com.biubike.adapter.ChargeAmountDividerDecoration;
import com.biubike.base.BaseActivity;
import com.biubike.util.Utils;

/**
 * Created by gaolei on 16/12/29.
 */

public class WalletActivity extends BaseActivity implements ChargeAmountAdapter.OnItemClickListener, View.OnClickListener {

    private RecyclerView recyclerview_acount;
    private ChargeAmountAdapter adapter;
    private TextView ballance;
    private ImageView wechat, alipay;
    private RelativeLayout wechat_layout, alipay_layout;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        recyclerview_acount = findViewById(R.id.recyclerview_acount);
        ballance = findViewById(R.id.ballance);
        wechat = findViewById(R.id.wechat);
        alipay = findViewById(R.id.alipay);
        wechat_layout = findViewById(R.id.wechat_layout);
        alipay_layout = findViewById(R.id.alipay_layout);
        wechat_layout.setOnClickListener(this);
        alipay_layout.setOnClickListener(this);

        recyclerview_acount.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new ChargeAmountAdapter(this);
        recyclerview_acount.setAdapter(adapter);
        adapter.setOnClickListener(this);
        recyclerview_acount.addItemDecoration(new ChargeAmountDividerDecoration(10));
        String acount_ballance = getString(R.string.account_ballance);
        Utils.setSpannableStr(ballance, acount_ballance, acount_ballance.length() - 3, acount_ballance.length() - 2, 1.2f);
    }

    @Override
    public void onItemClick(View v, int position) {
        adapter.setSelectPosition(position);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.wechat_layout:
                wechat.setImageResource(R.mipmap.type_select);
                alipay.setImageResource(R.mipmap.type_unselect);
                break;
            case R.id.alipay_layout:
                wechat.setImageResource(R.mipmap.type_unselect);
                alipay.setImageResource(R.mipmap.type_select);
                break;
        }
    }
}
