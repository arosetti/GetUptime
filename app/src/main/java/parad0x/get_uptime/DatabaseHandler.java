package parad0x.get_uptime;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHandler extends SQLiteOpenHelper {
    public final String TAG = this.getClass().getSimpleName();

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "uptime";
    private static final String TABLE_SCORE = "score";

    private static final String KEY_ID_SCORE = "_id";
    private static final String KEY_START = "start";
    private static final String KEY_VAL = "val";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //this.getAll();
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SCORE_TABLE = "CREATE TABLE " + TABLE_SCORE + "("
                + KEY_ID_SCORE + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_START + " INTEGER,"
                + KEY_VAL + " INTEGER)";

        db.execSQL(CREATE_SCORE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCORE);
        onCreate(db);
    }

    public boolean exists(long start) {
        String selectQuery = "SELECT  * FROM " + TABLE_SCORE + " WHERE " + KEY_START + "=" + start;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToNext()) {
            return true;
        }
        cursor.close();
        db.close();

        return false;
    }
    
    public void add(long start, long val) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_START, start);
        values.put(KEY_VAL, val);
        db.insert(TABLE_SCORE, null, values);
        db.close();
    }

    public void update(long start, long val) {
        String updateQuery = "UPDATE " + TABLE_SCORE +
                             " SET " + KEY_VAL + "=" + val +
                             " WHERE " + KEY_START + "=" + start;
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(updateQuery);
        db.close();
    }

    public void remove(long start) {
        String updateQuery = "DELETE FROM " + TABLE_SCORE +
                " WHERE " + KEY_START + "=" + start;
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(updateQuery);
        db.close();
    }
    
    public String[][] getAll() {
        String selectQuery = "SELECT " + KEY_START + "," + KEY_VAL +
                             " FROM " + TABLE_SCORE +
                             " ORDER BY " + KEY_VAL + " DESC";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        String[][] data = new String[cursor.getCount()][2];
        int i = 0;

        while (cursor.moveToNext()) {
            data[i][0] = cursor.getString(0);
            data[i][1] = cursor.getString(1);

            i++;
            
            Log.d(TAG,  cursor.getString(0) + ", " +
                         cursor.getString(1));
        }
        cursor.close();
        db.close();

        return data;
    }
    
    public String[] getBest() {
        String selectQuery = "SELECT " + KEY_START + "," + KEY_VAL + "  " +
                             " FROM " + TABLE_SCORE +
                              " ORDER BY " + KEY_VAL + " DESC LIMIT 1";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        String[] data = new String[2];

        if (cursor.moveToNext()) {
            data[0] = cursor.getString(0);
            data[1] = cursor.getString(1);
            return data;
        }

        cursor.close();
        db.close();

        return null;
    }
}