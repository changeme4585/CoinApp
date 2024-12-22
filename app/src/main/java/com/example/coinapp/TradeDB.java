package com.example.coinapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class TradeDB extends SQLiteOpenHelper {
    public static final String TABLE_NAME = "voca";
    public static final String state = "state";
    public static final String coinName = "coinName";
    public static final String amount = "amount"; //매매 수량
    public static final String coinPrice = "coinPrice";  //매매 가격
    public TradeDB( Context context) {
        super(context, "TradeDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                state + " TEXT , " +
                coinName + " TEXT, " +
                coinPrice + " TEXT, " +
                amount + " TEXT);";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    } // 거래기록을 저장하는 db 테이블
}
