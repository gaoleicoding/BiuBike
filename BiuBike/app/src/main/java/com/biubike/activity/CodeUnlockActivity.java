package com.biubike.activity;

import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.biubike.R;
import com.biubike.base.BaseActivity;
import com.biubike.util.Utils;

/**
 * Created by gaolei on 16/12/29.
 */

public class CodeUnlockActivity extends BaseActivity {

    private boolean isFlashOpen = false;
    private EditText editText1, editText2, editText3, editText4, editText5, editText6;
    private int currentEditIndex;

    private RelativeLayout title_layout;
    public static boolean unlockSuccess = false;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_unlock);
        Log.d("gaolei", "CodeUnlockActivity------------onCreate------------------");


//        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dp2px(this, 50));
//        layoutParams.setMargins(0, statusBarHeight, 0, 0);//4个参数按顺序分别是左上右下
//        title_layout.setLayoutParams(layoutParams);
        setStatusBar();

        editText1 = (EditText) findViewById(R.id.editText1);
        editText2 = (EditText) findViewById(R.id.editText2);
        editText3 = (EditText) findViewById(R.id.editText3);
        editText4 = (EditText) findViewById(R.id.editText4);
        editText5 = (EditText) findViewById(R.id.editText5);
        editText6 = (EditText) findViewById(R.id.editText6);
        editText1.setTag(1);
        editText2.setTag(2);
        editText3.setTag(3);
        editText4.setTag(4);
        editText5.setTag(5);
        editText6.setTag(6);
        // 添加 内容change listener ：输入焦点后移 + 密码验证
        editText1.addTextChangedListener(new MyTextChangeWatcher(1));
        editText2.addTextChangedListener(new MyTextChangeWatcher(2));
        editText3.addTextChangedListener(new MyTextChangeWatcher(3));
        editText4.addTextChangedListener(new MyTextChangeWatcher(4));
        editText5.addTextChangedListener(new MyTextChangeWatcher(5));
        editText6.addTextChangedListener(new MyTextChangeWatcher(6));

        // del 监听，输入焦点前移
        editText1.setOnKeyListener(keyListener);
        editText2.setOnKeyListener(keyListener);
        editText3.setOnKeyListener(keyListener);
        editText4.setOnKeyListener(keyListener);
        editText5.setOnKeyListener(keyListener);
        editText6.setOnKeyListener(keyListener);
        new Utils(this).showIMM();
//        ShowKeyboard(editText1);
    }

    public void switchFlashlight(View view) {
        if (!isFlashOpen) {
            Camera camera = Camera.open();
            Camera.Parameters mParameters = camera.getParameters();
            mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);//打开Camera.Parameters.FLASH_MODE_OFF则为关闭
            camera.setParameters(mParameters);
            isFlashOpen = true;
        } else {
            Camera camera = Camera.open();
            Camera.Parameters mParameters = camera.getParameters();
            mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);//打开Camera.Parameters.FLASH_MODE_OFF则为关闭
            camera.setParameters(mParameters);
            camera.release();
            isFlashOpen = false;
        }
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
                if (index < 7) {// 焦点后移

                    Log.d("gaolei", "index------------------" + index);
                    if (index < 6) {
                        EditText editText = getEditTextFromIndex(index);
                        editText.clearFocus();
                        getEditTextFromIndex(index + 1).requestFocusFromTouch();
                    }
                    currentEditIndex = index;
                } else {
                    // TODO 判断
                    // handler.sendEmptyMessage(1);
                }
            } else {
                // 清除 对应 标识位
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

    /**
     * 监听删除键 前移焦点
     */
    private View.OnKeyListener keyListener = new View.OnKeyListener() {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {

            if ((((EditText) v).getText().toString() == null || ((EditText) v)
                    .getText().toString().isEmpty())
                    && keyCode == KeyEvent.KEYCODE_DEL
                    && event.getAction() == KeyEvent.ACTION_DOWN) {

                Log.d("gaolei", "currentEditIndex-----------" + currentEditIndex);
                if (currentEditIndex == 6)
                    currentEditIndex = 5;
                if (currentEditIndex > 0) {
                    EditText editText = getEditTextFromIndex(currentEditIndex);
                    Log.d("gaolei", "editText.getTag()-----------" + editText.getTag());

                    editText.setText("");
                    editText.requestFocusFromTouch();
                    currentEditIndex--;
                }
            }

            return false;
        }
    };

    private EditText getEditTextFromIndex(int index) {
        switch (index) {
            case 1:
                return editText1;
            case 2:
                return editText2;
            case 3:
                return editText3;
            case 4:
                return editText4;
            case 5:
                return editText5;
            case 6:
                return editText6;

            default:
                break;
        }
        return null;
    }

    //显示虚拟键盘
    public static void ShowKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        imm.showSoftInput(v, InputMethodManager.SHOW_FORCED);

    }

    public void onPause() {
        super.onPause();
        Log.d("gaolei", "CodeUnlockActivity------------onPause------------------");
    }

    public void onStop() {
        super.onStop();
        Log.d("gaolei", "CodeUnlockActivity------------onStop------------------");
    }

    public void onDestroy() {
        super.onDestroy();
        unlockSuccess = false;
        Log.d("gaolei", "CodeUnlockActivity------------onDestroy------------------");
    }

    public void finishActivity(View view) {

        unlockSuccess = true;
        finish();
    }

    public void unlockSucess(View view) {
        unlockSuccess = true;
        finish();

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
//            unlockSuccess = true;
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
