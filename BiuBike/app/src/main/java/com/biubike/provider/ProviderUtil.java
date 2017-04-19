package com.biubike.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaolei on 17/4/19.
 */

public class ProviderUtil {
    private ContentResolver cr;
    private ContentValues values;
    Context context;

    public ProviderUtil(Context context) {
        cr = context.getContentResolver();
        values = new ContentValues();
        this.context = context;
    }

    public void updateData() {
        // values.put("USERNAME", "lisi");
        // cr.update(Uri.parse("content://com.lenve.cphost.mycontentprovider/user"),
        // values, "_id=?",
        // new String[] { 4 + "" });
        values.put("USERNAME", "wangwu");
        cr.update(Uri.parse("content://com.lenve.cphost.mycontentprovider/user/3"), values, "USERNAME=?",
                new String[]{"lisi"});
    }

    public void deleteData() {
        // 根据id删除
        // cr.delete(Uri.parse("content://com.lenve.cphost.mycontentprovider/user/1"),
        // "", new String[] {});
        // 根据username删除
        cr.delete(Uri.parse("content://com.lenve.cphost.mycontentprovider/user/zhangsan"), "", new String[]{});
    }

    public void addData(String address, String district,String lattitude, String longitude) {
        values.put("address",address);
        values.put("district",district);
        values.put("lattitude", lattitude);
        values.put("longitude", longitude);
        cr.insert(PoiProvider.CONTENT_URI, values);
    }

    public List<PoiObject> getData() {
        DBHelper helper = new DBHelper(context, "lenve.db", null, 1);
        SQLiteDatabase db = helper.getWritableDatabase();
        List<PoiObject> list = new ArrayList<PoiObject>();
        String sql = "select * from " + DBHelper.USERTABLE + " order by _id DESC" +
                 " limit 15";
        Cursor c = db.rawQuery(sql, null);
        PoiObject u = null;
        while (c.moveToNext()) {
            String address = c.getString(c.getColumnIndex("address"));
            String lattitude = c.getString(c.getColumnIndex("lattitude"));
            String longitude = c.getString(c.getColumnIndex("longitude"));
            String district = c.getString(c.getColumnIndex("district"));
            u = new PoiObject(address, lattitude, longitude,district);
            list.add(u);
        }
        c.close();
        return list;
    }
}
