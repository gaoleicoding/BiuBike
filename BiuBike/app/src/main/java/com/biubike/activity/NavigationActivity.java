package com.biubike.activity;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.biubike.R;
import com.biubike.adapter.PoiSuggestionAdapter;
import com.biubike.adapter.RecyclerViewDivider;
import com.biubike.base.BaseActivity;
import com.biubike.util.Utils;

import java.util.List;

/**
 * Created by gaolei on 16/12/29.
 */

public class NavigationActivity extends BaseActivity implements
        OnGetSuggestionResultListener, PoiSuggestionAdapter.OnItemClickListener {

    LinearLayout place_search_layout;
    RelativeLayout title_content_layout;
    EditText start_place_edit, destination_edit, place_edit;
    RecyclerView recyclerview_position;
    private List<SuggestionResult.SuggestionInfo> suggestionInfoList;
    private SuggestionSearch mSuggestionSearch = null;
    PoiSuggestionAdapter sugAdapter;
    boolean firstSetAdapter = true, isStartPoi = true;
    String start_place,destination;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        setStatusBar();
        place_search_layout = (LinearLayout) findViewById(R.id.place_search_layout);
        title_content_layout = (RelativeLayout) findViewById(R.id.title_content_layout);
        start_place_edit = (EditText) findViewById(R.id.start_place_edit);
        destination_edit = (EditText) findViewById(R.id.destination_edit);
        place_edit = (EditText) findViewById(R.id.place_edit);
        recyclerview_position = (RecyclerView) findViewById(R.id.recyclerview_position_history);


        recyclerview_position.setLayoutManager(new LinearLayoutManager(this));

        recyclerview_position.addItemDecoration(new RecyclerViewDivider(
                this, LinearLayoutManager.HORIZONTAL, 1, ContextCompat.getColor(this, R.color.color_c8cacc)));
        // 初始化建议搜索模块，注册建议搜索事件监听
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(this);
        /**
         * 当输入关键字变化时，动态更新建议列表
         */
        place_edit.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2,
                                      int arg3) {
                if (cs.length() <= 0) {
                    return;
                }
                /**
                 * 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新
                 */
                mSuggestionSearch
                        .requestSuggestion((new SuggestionSearchOption())
                                .keyword(cs.toString()).city("北京"));
            }
        });
    }
    public void switchPoi(View view) {
        start_place=start_place_edit.getText().toString();
        destination=destination_edit.getText().toString();
        if(!destination_edit.getText().toString().equals(getString(R.string.input_destination)))
            start_place_edit.setText(destination);
        else
            start_place_edit.setText(getString(R.string.input_start_place));

            destination_edit.setText(start_place);
    }
    public void showInputStart(View view) {
        place_edit.requestFocus();
        new Utils(this).showIMM();
        setStatusBarLayout();
        title_content_layout.setVisibility(View.GONE);
        place_search_layout.setVisibility(View.VISIBLE);
        place_edit.setHint(getString(R.string.input_start_place));
        isStartPoi = true;
    }

    public void showInputDestination(View view) {
        place_edit.requestFocus();
        new Utils(this).showIMM();
        setStatusBarLayout();
        title_content_layout.setVisibility(View.GONE);
        place_search_layout.setVisibility(View.VISIBLE);
        place_edit.setHint(getString(R.string.input_destination));
        isStartPoi = false;
    }

    public void backFromSearchPlace(View view) {
        new Utils(this).hideIMM();
        place_edit.setText("");
        if(sugAdapter!=null)
        sugAdapter.changeData(null);
        title_content_layout.setVisibility(View.VISIBLE);
        place_search_layout.setVisibility(View.GONE);
        setStatusBar();
    }

    public void confirmPoi(View view) {
        if (isStartPoi)
            start_place_edit.setText(place_edit.getText().toString());
        else
            destination_edit.setText(place_edit.getText().toString());
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (place_search_layout.getVisibility() == View.VISIBLE) {
                backFromSearchPlace(place_search_layout);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onGetSuggestionResult(SuggestionResult res) {
        if (res == null || res.getAllSuggestions() == null) {
            return;
        }
        suggestionInfoList = res.getAllSuggestions();
        if (firstSetAdapter) {
            sugAdapter = new PoiSuggestionAdapter(this, suggestionInfoList);
            recyclerview_position.setAdapter(sugAdapter);
            sugAdapter.setOnClickListener(this);
            firstSetAdapter = false;
        } else {
            sugAdapter.changeData(suggestionInfoList);
        }
    }

    @Override
    public void onItemClick(View v, int position) {
        TextView poi = (TextView) v.findViewById(R.id.place);
        if (isStartPoi)
            start_place_edit.setText(poi.getText().toString());
        else
            destination_edit.setText(poi.getText().toString());
        backFromSearchPlace(place_search_layout);
    }
}
