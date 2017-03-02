package com.biubike.custom;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

import com.biubike.R;

/**
 * Created by gaolei on 17/3/2.
 */
public class SelectDialog extends Dialog implements View.OnClickListener {
    private TextView confirm;
    private IDialogOnclickInterface dialogOnclickInterface;
    private Context context;

    public SelectDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_confirm);

        confirm = (TextView) findViewById(R.id.confirm);
        confirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
//        dialogOnclickInterface = (IDialogOnclickInterface) context;
        switch (v.getId()) {
            case R.id.confirm:
//                dialogOnclickInterface.confirmOnclick();
//                turnGPSOn(getContext());
                Intent intent = new Intent(
                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
                cancel();
                break;

            default:
                break;
        }
    }

    public interface IDialogOnclickInterface {
        void confirmOnclick();

    }
}
