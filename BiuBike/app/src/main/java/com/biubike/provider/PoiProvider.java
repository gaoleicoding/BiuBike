package com.biubike.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class PoiProvider extends ContentProvider {

	private SQLiteOpenHelper helper;
	private SQLiteDatabase db;
	private static UriMatcher matcher;
	private static final String AUTHORITY = "com.biubike.provider.poiprovider";
	public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY);

	static {

		matcher = new UriMatcher(UriMatcher.NO_MATCH);
		matcher.addURI(AUTHORITY, "user", 1);// 配置表
		matcher.addURI(AUTHORITY, "user/#", 2);// 匹配任何数字
		matcher.addURI(AUTHORITY, "user/*", 3);// 匹配任何文本

	}

	@Override
	public boolean onCreate() {
		helper = new DBHelper(getContext(), "lenve.db", null, 1);
		db = helper.getWritableDatabase();
		Log.d("qf", "MyContentProvider--->onCreate()");
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		return null;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		db.insert(DBHelper.USERTABLE, null, values);
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int code = matcher.match(uri);
		int result = 0;
		switch (code) {
		case UriMatcher.NO_MATCH:
			break;
		case 1:
			// 删除所有
			result = db.delete(DBHelper.USERTABLE, null, null);
			Log.d("qf", "删除所有数据！");
			break;
		case 2:
			// content://com.lenve.cphost.mycontentprovider/user/10
			// 按条件删除，id
			result = db.delete(DBHelper.USERTABLE, "_id=?", new String[] { ContentUris.parseId(uri) + "" });
			Log.d("qf", "根据删除一条数据");
			break;
		case 3:
			// content://com.lenve.cphost.mycontentprovider/user/zhangsan
			// uri.getPathSegments()拿到一个List<String>，里边的值分别是0-->user、1-->zhangsan
			result = db.delete(DBHelper.USERTABLE, "USERNAME=?", new String[] { uri.getPathSegments().get(1) });
			break;
		default:
			break;
		}
		return result;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int code = matcher.match(uri);
		int result = 0;
		switch (code) {
		case 1:
			result = db.update(DBHelper.USERTABLE, values, selection, selectionArgs);
			break;
		case 2:
			result = db.update(DBHelper.USERTABLE, values, "_id=" + ContentUris.parseId(uri) + " AND " + selection,
					selectionArgs);
			break;
		// 根据手动传参id来更新
		case 3:
			result = db.update(DBHelper.USERTABLE, values, selection, selectionArgs);
			break;
		}
		return result;
	}

}
