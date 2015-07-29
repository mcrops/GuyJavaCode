package aidev.cocis.makerere.org.whiteflycounter;

/**
 * Created by User on 7/7/2015.
 */

import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.content.Context;

/**
 * @author Sean Owen
 */
final class DBHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 5;
    private static final String DB_NAME = "whiteflyhistory.db";

    static final String PLANT_TABLE_NAME = "plant";
    static final String ID_COL = "id";
    static final String PATH_COL = "path";
    static final String COUNT_COL = "count";
    static final String FIELD_NO_COL = "fieldno";
    static final String TIMESTAMP_COL = "timestamp";

    static final String FIELD_TABLE_NAME = "field";
    static final String DESCRIPTION_COL = "description";

    DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(
                "CREATE TABLE " + PLANT_TABLE_NAME + " (" +
                        ID_COL + " INTEGER PRIMARY KEY, " +
                        PATH_COL + " TEXT, " +
                        COUNT_COL + " INTEGER, " +
                        FIELD_NO_COL + " TEXT, " +
                        TIMESTAMP_COL + " INTEGER);");

        sqLiteDatabase.execSQL(
                "CREATE TABLE " + FIELD_TABLE_NAME + " (" +
                        ID_COL + " INTEGER PRIMARY KEY, " +
                        DESCRIPTION_COL + " TEXT, " +
                        FIELD_NO_COL + " TEXT, " +
                        TIMESTAMP_COL + " INTEGER);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PLANT_TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FIELD_TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

}