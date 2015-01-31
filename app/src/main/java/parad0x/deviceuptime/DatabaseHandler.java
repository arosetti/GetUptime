package parad0x.deviceuptime;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "uptime";
    private static final String TABLE_SCORE = "score";

    private static final String KEY_ID_SCORE = "_id";
    private static final String KEY_SCORE = "value";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SCORE_TABLE = "CREATE TABLE " + TABLE_SCORE + "("
                + KEY_ID_SCORE + " INTEGER PRIMARY KEY,"
                + KEY_SCORE + " TEXT" + ")";

        db.execSQL(CREATE_SCORE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCORE);
        onCreate(db);
    }

    public void addScore(int score) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_SCORE, score);
        db.insert(TABLE_SCORE, null, values);
        db.close();
    }

    public void updateScore(int id, int score) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_SCORE, score);
        String strFilter = KEY_ID_SCORE + id;

        db.update(TABLE_SCORE, values, strFilter, null);
        db.close();
    }

    public String[] getAllScores() {
        String selectQuery = "SELECT  * FROM " + TABLE_SCORE;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        String[] data = new String[cursor.getCount()];
        int i = 0;

        while (cursor.moveToNext()) {
            data[i] = cursor.getString(1);
            i = i++;
        }
        cursor.close();
        db.close();

        return data;
    }
}