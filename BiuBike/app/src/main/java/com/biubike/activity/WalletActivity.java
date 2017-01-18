package com.biubike.activity;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.biubike.R;
import com.biubike.adapter.ChargeAmountAdapter;
import com.biubike.adapter.ChargeAmountDividerDecoration;
import com.biubike.base.BaseActivity;
import com.biubike.util.Utils;

/**
 * Created by gaolei on 16/12/29.
 */

public class WalletActivity extends BaseActivity implements ChargeAmountAdapter.OnItemClickListener{

    RecyclerView recyclerview_acount;
    ChargeAmountAdapter adapter;
    TextView ballance;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        setStatusBar();

        recyclerview_acount = (RecyclerView) findViewById(R.id.recyclerview_acount);
        ballance = (TextView) findViewById(R.id.ballance);
        recyclerview_acount.setLayoutManager(new GridLayoutManager(this,3));
        adapter=new ChargeAmountAdapter(this);
        recyclerview_acount.setAdapter(adapter);
        adapter.setOnClickListener(this);
        recyclerview_acount.addItemDecoration(new ChargeAmountDividerDecoration(10));
        String acount_ballance=getString(R.string.account_ballance);
        Utils.setSpannableStr(ballance,acount_ballance,acount_ballance.length()-2,acount_ballance.length()-1,1.2f);
    }

    @Override
    public void onItemClick(View v, int position) {
        adapter.setSelectPosition(position);
    }
}
