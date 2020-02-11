package com.weatherapp2019;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SavedCitiesHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 13;
    public static final String DATABASE_NAME = "SavedCities.db";
    public static final String TABLE_NAME = "SavedCities";
    public static final String COLUMN_1 = "_id";
    public static final String COLUMN_2 = "CityID";
    public SavedCitiesHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    // String used as Parameter in onCreate
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + "(" + COLUMN_1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_2 + " INTEGER UNIQUE )";

    // String used as parameter in onUpgrade
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    // Method you use to add data
    public boolean insertData(long cityID) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_2, cityID);
        long result = db.insert(TABLE_NAME, null, contentValues);
        //Insert method inserts data and returns a -1 if it doesn't work
        return result != -1;
    }

}
