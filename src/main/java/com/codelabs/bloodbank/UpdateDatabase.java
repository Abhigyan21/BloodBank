package com.codelabs.bloodbank;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by abhigyan on 18/11/16.
 */
public class UpdateDatabase extends SQLiteOpenHelper {

    private static String DB = "Info.db";
    private static String TABLE_LOGIN = "LOGIN";
    private static String TABLE_REGISTER = "REGISTER";
    private static String COLUMN_NAME = "USERNAME";
    private static String COLUMN_PASS = "PASS";
    private static String COLUMN_EMAIL = "EMAIL";
    private static String COLUMN_NUM = "NUMBER";
    private static String COLUMN_ADD = "ADDRESS";
    private static String COLUMN_DONOR = "DONOR";

    public UpdateDatabase(Context context) {
        super(context, DB, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
         db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_LOGIN + "(" + COLUMN_NAME + " VARCHAR(20)," +
         COLUMN_PASS + " VARCHAR(20));");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_REGISTER + "(" + COLUMN_NAME + " VARCHAR(20)," +
                COLUMN_PASS + " VARCHAR(20)," + COLUMN_EMAIL + " VARCHAR(20)," + COLUMN_NUM + " VARCHAR(20)," + COLUMN_ADD
                + " VARCHAR(20)," + COLUMN_DONOR + " VARCHAR(20));");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGIN);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REGISTER);
        onCreate(db);
    }

    public void insert(String name, String pass, String email, String num, String add, String donor) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_NAME, name);
        contentValues.put(COLUMN_PASS, pass);
        contentValues.put(COLUMN_EMAIL, email);
        contentValues.put(COLUMN_NUM, num);
        contentValues.put(COLUMN_ADD, add);
        contentValues.put(COLUMN_DONOR, donor);

        db.insert(TABLE_REGISTER, null, contentValues);
        db.close();
    }

    public boolean check(String name, String pass){
        Cursor c;
        SQLiteDatabase db;
        try {
            db = this.getReadableDatabase();
            c = db.rawQuery("SELECT * FROM REGISTER", null);

            while (c.moveToNext()) {
                try {
                    int nameID = c.getColumnIndex(COLUMN_NAME);
                    String username = c.getString(nameID);

                    int passID = c.getColumnIndex(COLUMN_PASS);
                    String pwd = c.getString(passID);
                    System.out.println(username +"," + pwd);
                    if(name.equals(username) && pass.equals(pwd)){
                        return true;
                    }
                } catch (Exception e) {
                    Log.e("UpdateDB", e.getMessage());
                }
            }
        } catch (SQLException e) {
            Log.e("Update", e.getMessage());
        }
        return false;
    }
}
