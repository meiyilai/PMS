package com.gzmelife.app.tools;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GoodFoodSQLiteOpenHelper extends SQLiteOpenHelper {
	private static String name = "search_hos_food.db";
	private static Integer version = 1;

	public GoodFoodSQLiteOpenHelper(Context context) {
		super(context, name, null, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table records(_id integer primary key autoincrement,name varchar(200))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
}
