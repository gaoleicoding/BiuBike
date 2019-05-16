package com.biubike.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.biubike.R;
import com.biubike.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class IdentityCodeView extends RelativeLayout {


    // 输入框的宽度
    private int mEtWidth;
    //是否是密码模式
    private boolean mEtPwd;


    private EditText editText1, editText2, editText3, editText4, editText5, editText6;
    private int currentEditIndex;
    List<EditText> editTextList = new ArrayList<>();


    public IdentityCodeView(Context context) {
        this(context, null);
    }

    public IdentityCodeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IdentityCodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    //初始化 布局和属性
    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.layout_identity_code, this);
        editText1 = findViewById(R.id.editText1);
        editText2 = findViewById(R.id.editText2);
        editText3 = findViewById(R.id.editText3);
        editText4 = findViewById(R.id.editText4);
        editText5 = findViewById(R.id.editText5);
        editText6 = findViewById(R.id.editText6);
        editTextList.add(editText1);
        editTextList.add(editText2);
        editTextList.add(editText3);
        editTextList.add(editText4);
        editTextList.add(editText5);
        editTextList.add(editText6);

        editText1.addTextChangedListener(new MyTextChangeWatcher(0));
        editText2.addTextChangedListener(new MyTextChangeWatcher(1));
        editText3.addTextChangedListener(new MyTextChangeWatcher(2));
        editText4.addTextChangedListener(new MyTextChangeWatcher(3));
        editText5.addTextChangedListener(new MyTextChangeWatcher(4));
        editText6.addTextChangedListener(new MyTextChangeWatcher(5));

        editText1.setOnFocusChangeListener(new MyFoucusChangeListener(0));
        editText2.setOnFocusChangeListener(new MyFoucusChangeListener(1));
        editText3.setOnFocusChangeListener(new MyFoucusChangeListener(2));
        editText4.setOnFocusChangeListener(new MyFoucusChangeListener(3));
        editText5.setOnFocusChangeListener(new MyFoucusChangeListener(4));
        editText6.setOnFocusChangeListener(new MyFoucusChangeListener(5));

        // del 监听，输入焦点前移
        editText1.setOnKeyListener(keyListener);
        editText2.setOnKeyListener(keyListener);
        editText3.setOnKeyListener(keyListener);
        editText4.setOnKeyListener(keyListener);
        editText5.setOnKeyListener(keyListener);
        editText6.setOnKeyListener(keyListener);
        editText1.setBackgroundResource(R.drawable.shape_icv_et_bg_focus);

        int dividerWidth = Utils.dp2px(context, 12);
        int paddingWidth = Utils.dp2px(context, 56);
        mEtWidth = (Utils.getScreenWidth(context) - dividerWidth * 5 - paddingWidth) / 6;
        editText1.setWidth(mEtWidth);
        editText1.setHeight(mEtWidth);
        editText2.setWidth(mEtWidth);
        editText2.setHeight(mEtWidth);
        editText3.setWidth(mEtWidth);
        editText3.setHeight(mEtWidth);
        editText4.setWidth(mEtWidth);
        editText4.setHeight(mEtWidth);
        editText5.setWidth(mEtWidth);
        editText5.setHeight(mEtWidth);
        editText6.setWidth(mEtWidth);
        editText6.setHeight(mEtWidth);

    }

    class MyTextChangeWatcher implements TextWatcher {
        // 标示 绑定的EditText
        private int index;

        public MyTextChangeWatcher(int index) {
            super();
            this.index = index;
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s != null && s.length() == 1) {
                // 焦点后移
                if (index < 5) {
                    EditText editText = editTextList.get(index);
                    editText.clearFocus();
                    editTextList.get(index + 1).requestFocusFromTouch();
                }

                if (getInputNum() == 6)
                    inputCompleteListener.inputComplete();
            } else {


            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
        }
    }

    class MyFoucusChangeListener implements OnFocusChangeListener {
        private int index;

        public MyFoucusChangeListener(int index) {
            super();
            this.index = index;
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {


            if (hasFocus) {
                v.setBackgroundResource(R.drawable.shape_icv_et_bg_focus);
                currentEditIndex = index;

                clearOtherFocus(index);
            }
        }
    }

    private void clearOtherFocus(int position) {
        for (int i = 0; i < editTextList.size(); i++) {
            if (i != position) {
                if (TextUtils.isEmpty(editTextList.get(i).getText().toString())) {
                    editTextList.get(i).clearFocus();
                    editTextList.get(i).setBackgroundResource(R.drawable.shape_icv_et_bg_normal);
                }
            }
        }
    }


    /**
     * 监听删除键 前移焦点
     */
    private View.OnKeyListener keyListener = new View.OnKeyListener() {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_DEL
                    && event.getAction() == KeyEvent.ACTION_DOWN) {
                EditText editText = editTextList.get(currentEditIndex);
                //如果有焦点的输入框有内容或者是第一位则只删除内容，如果是没内容则取消焦点和删除上一个输入框文字
                if (!TextUtils.isEmpty(editTextList.get(currentEditIndex).getText().toString()) || currentEditIndex == 0) {
                    editText.setText("");
                } else {
                    editTextList.get(currentEditIndex - 1).setText("");
                    editTextList.get(currentEditIndex).setBackgroundResource(R.drawable.shape_icv_et_bg_normal);
                    if (currentEditIndex > 0)
                        editTextList.get(currentEditIndex - 1).requestFocusFromTouch();
                }
            }

            return false;
        }
    };

    int getInputNum() {
        return isEtInput(editText1) + isEtInput(editText2) + isEtInput(editText3) + isEtInput(editText4) + isEtInput(editText5) + isEtInput(editText6);
    }

    int isEtInput(EditText editText) {
        return !TextUtils.isEmpty(editText.getText().toString().trim()) ? 1 : 0;
    }


    public String getInputContent() {
        return getEtText(editText1) + getEtText(editText2) + getEtText(editText3) + getEtText(editText4) + getEtText(editText5) + getEtText(editText6);
    }

    private String getEtText(EditText editText) {
        return editText.getText().toString().trim();
    }

    public interface InputCompleteListener {
        void inputComplete();
    }

    InputCompleteListener inputCompleteListener;

    public void setInputCompleteListener(InputCompleteListener inputCompleteListener) {
        this.inputCompleteListener = inputCompleteListener;
    }
}
