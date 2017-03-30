package com.biubike.custom;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.biubike.R;

import java.util.List;


/**
 * 可扩展的单选对话框
 *
 * @author guo
 */
public class NaviSelectDialog extends Dialog implements View.OnClickListener {

    protected List<String> mItems;// 数据源，选项长度由它的长度决定
    protected String mCurValue;// 当前值（选定状态）
    protected OnDlgItemClickListener mOnItemClickListener;// 回调
    protected ListView navi_listview;
    private boolean hasCheckbox;
    private LinearLayout checkBoxLayout;
    private TextView tv_ensure;
    private TextView tv_cancle;
    private CheckBox cb_remeber;
    private SingleChoiceAdapter adapter;

    public NaviSelectDialog(Context context) {
        super(context, R.style.transparentCommonDialogStyle);// 设置样式
        init(context);
    }

    protected void init(Context context) {
        mCurValue=context.getString(R.string.inner_navi);
        setContentView(R.layout.dlg_navi_select_layout);
        navi_listview = (ListView) findViewById(R.id.navi_listview);
        checkBoxLayout = (LinearLayout) findViewById(R.id.layout_remeber_myChoice);
        tv_ensure = (TextView) findViewById(R.id.tv_dlg_redio_checkbox_ensure);
        tv_cancle = (TextView) findViewById(R.id.tv_dlg_redio_checkbox_cancle);
        tv_ensure.setOnClickListener(this);
        tv_cancle.setOnClickListener(this);
        cb_remeber = (CheckBox) findViewById(R.id.checkbox_remeber_my_choice);
    }
    /**
     * 设置内容
     */
    public Dialog setItems(List<String> itmes,
                           OnDlgItemClickListener l, boolean hasCheckbox) {
        mItems = itmes;
        mOnItemClickListener = l;
        createItems();
        return this;
    }

    private void createItems() {
        adapter = new SingleChoiceAdapter(getContext());
        navi_listview.setAdapter(adapter);
        navi_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                adapter.changeUI(position);
                mCurValue=mItems.get(position);
            }
        });
    }

    protected class SingleChoiceAdapter extends BaseAdapter {
        LayoutInflater mLayoutInflater;
        int position;

        public SingleChoiceAdapter(Context context) {
            super();
            mLayoutInflater = LayoutInflater.from(context);
        }

        public void changeUI(int position) {
            this.position = position;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        ImageView mSelectTagIV;
        TextView mTextTV;

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String curValue = mItems.get(position);
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(
                        R.layout.layout_item_redio_checkbox_dlg, null);
            }
            mTextTV = (TextView) convertView
                    .findViewById(R.id.single_choice_dlg_text_tv);
            mTextTV.setText(curValue);
            mSelectTagIV = (ImageView) convertView
                    .findViewById(R.id.single_choice_dlg_select_tag_iv);
            if (this.position == position) {
                mSelectTagIV
                        .setImageResource(R.mipmap.radio_selected);
            } else {
                mSelectTagIV
                        .setImageResource(R.mipmap.radio_unselected);
            }
            return convertView;
        }
    }

    public interface OnDlgItemClickListener {

        public void onEnsureClicked(Dialog dialog, String value, boolean isChecked);
        public void onCancleClicked(Dialog dialog);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_dlg_redio_checkbox_ensure:
                boolean checked = hasCheckbox;
                if (hasCheckbox) {
                    checked = cb_remeber.isChecked();
                }
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onEnsureClicked(NaviSelectDialog.this, mCurValue, checked);
                }
                break;
            case R.id.tv_dlg_redio_checkbox_cancle:
                mOnItemClickListener.onCancleClicked(NaviSelectDialog.this);
                break;
        }
    }

}
