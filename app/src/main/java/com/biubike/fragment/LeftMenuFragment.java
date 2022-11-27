package com.biubike.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.biubike.R;

/**
 * Created by gaolei on 17/1/5.
 */

public class LeftMenuFragment extends Fragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.main_menu,null);
        return view;
    }

}
