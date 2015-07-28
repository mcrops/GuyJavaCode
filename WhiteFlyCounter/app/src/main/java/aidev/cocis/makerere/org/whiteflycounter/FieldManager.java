package aidev.cocis.makerere.org.whiteflycounter;

/**
 * Created by User on 7/7/2015.
 */
import android.app.Activity;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>Manages functionality related to scan history.</p>
 *
 * @author Sean Owen
 */
public final class FieldManager {

    private static final String TAG = FieldManager.class.getSimpleName();

    private static final int MAX_ITEMS = 2000;

    private static final String[] COLUMNS = {
            DBHelper.FIELD_NO_COL,
            DBHelper.DESCRIPTION_COL,
            DBHelper.TIMESTAMP_COL,
    };

    private static final String[] COUNT_COLUMN = { "COUNT(1)" };

    private static final String[] ID_COL_PROJECTION = { DBHelper.ID_COL };
    private static final String[] ID_DETAIL_COL_PROJECTION = { DBHelper.ID_COL, DBHelper.DESCRIPTION_COL };

    private final Activity activity;
    private final boolean enableHistory;

    public FieldManager(Activity activity) {
        this.activity = activity;
        enableHistory = true;
    }

    public boolean hasHistoryItems() {
        SQLiteOpenHelper helper = new DBHelper(activity);
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = helper.getReadableDatabase();
            cursor = db.query(DBHelper.FIELD_TABLE_NAME, COUNT_COLUMN, null, null, null, null, null);
            cursor.moveToFirst();
            return cursor.getInt(0) > 0;
        } finally {
            close(cursor, db);
        }
    }

    public int countHistoryItems() {
        SQLiteOpenHelper helper = new DBHelper(activity);
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = helper.getReadableDatabase();
            cursor = db.query(DBHelper.FIELD_TABLE_NAME, COLUMNS, null, null, null, null, null);
           return cursor.getCount();
        } finally {
            close(cursor, db);
        }
    }

    public List<FieldItem> buildHistoryItems() {
        SQLiteOpenHelper helper = new DBHelper(activity);
        List<FieldItem> items = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = helper.getReadableDatabase();
            cursor = db.query(DBHelper.FIELD_TABLE_NAME, COLUMNS, null, null, null, null, DBHelper.TIMESTAMP_COL + " DESC");
            while (cursor.moveToNext()) {
                String fieldno = cursor.getString(0);
                String summary = cursor.getString(1);
                long timestamp = cursor.getLong(2);
                Field field = new Field(fieldno, summary, timestamp);
                items.add(new FieldItem(field));
            }
        } finally {
            close(cursor, db);
        }
        return items;
    }

    public FieldItem buildHistoryItem(int number) {
        SQLiteOpenHelper helper = new DBHelper(activity);
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = helper.getReadableDatabase();
            cursor = db.query(DBHelper.FIELD_TABLE_NAME, COLUMNS, null, null, null, null, DBHelper.TIMESTAMP_COL + " DESC");
            cursor.move(number + 1);
            String fieldno = cursor.getString(0);
            String summary = cursor.getString(1);
            long timestamp = cursor.getLong(2);
            Field field = new Field(fieldno, summary, timestamp);
            return new FieldItem(field);
        } finally {
            close(cursor, db);
        }
    }

    public void deleteHistoryItem(int number) {
        SQLiteOpenHelper helper = new DBHelper(activity);
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = helper.getWritableDatabase();
            cursor = db.query(DBHelper.FIELD_TABLE_NAME,
                    ID_COL_PROJECTION,
                    null, null, null, null,
                    DBHelper.TIMESTAMP_COL + " DESC");
            cursor.move(number + 1);
            db.delete(DBHelper.FIELD_TABLE_NAME, DBHelper.ID_COL + '=' + cursor.getString(0), null);
        } finally {
            close(cursor, db);
        }
    }

    public void addHistoryItem(Field field, FieldHandler handler) {
        // Do not save this item to the history if the preference is turned off, or the contents are
        // considered secure.
        /*if (!activity.getIntent().getBooleanExtra(Intents.Scan.SAVE_HISTORY, true) ||
                handler.areContentsSecure() || !enableHistory) {
            return;
        }*/

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);

           // deletePrevious(result.getText());


        ContentValues values = new ContentValues();
        values.put(DBHelper.FIELD_NO_COL, field.getFieldno());
        values.put(DBHelper.DESCRIPTION_COL,field.getSummary());
        values.put(DBHelper.TIMESTAMP_COL, System.currentTimeMillis());

        SQLiteOpenHelper helper = new DBHelper(activity);
        SQLiteDatabase db = null;
        try {
            db = helper.getWritableDatabase();
            // Insert the new entry into the DB.
            db.insert(DBHelper.FIELD_TABLE_NAME, DBHelper.TIMESTAMP_COL, values);
        } finally {
            close(null, db);
        }
    }

    public void addHistoryItemDetails(String itemID, String itemDetails) {
        // As we're going to do an update only we don't need need to worry
        // about the preferences; if the item wasn't saved it won't be udpated
        SQLiteOpenHelper helper = new DBHelper(activity);
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = helper.getWritableDatabase();
            cursor = db.query(DBHelper.FIELD_TABLE_NAME,
                    ID_DETAIL_COL_PROJECTION,
                    DBHelper.FIELD_NO_COL + "=?",
                    new String[] { itemID },
                    null,
                    null,
                    DBHelper.TIMESTAMP_COL + " DESC",
                    "1");
            String oldID = null;
            String oldDetails = null;
            if (cursor.moveToNext()) {
                oldID = cursor.getString(0);
                oldDetails = cursor.getString(1);
            }

            if (oldID != null) {
                String newDetails;
                if (oldDetails == null) {
                    newDetails = itemDetails;
                } else if (oldDetails.contains(itemDetails)) {
                    newDetails = null;
                } else {
                    newDetails = oldDetails + " : " + itemDetails;
                }
                if (newDetails != null) {
                    ContentValues values = new ContentValues();
                    values.put(DBHelper.FIELD_NO_COL, newDetails);
                    db.update(DBHelper.FIELD_TABLE_NAME, values, DBHelper.ID_COL + "=?", new String[] { oldID });
                }
            }

        } finally {
            close(cursor, db);
        }
    }

    private void deletePrevious(String text) {
        SQLiteOpenHelper helper = new DBHelper(activity);
        SQLiteDatabase db = null;
        try {
            db = helper.getWritableDatabase();
            db.delete(DBHelper.FIELD_TABLE_NAME, DBHelper.FIELD_NO_COL + "=?", new String[] { text });
        } finally {
            close(null, db);
        }
    }

    public void trimHistory() {
        SQLiteOpenHelper helper = new DBHelper(activity);
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = helper.getWritableDatabase();
            cursor = db.query(DBHelper.FIELD_TABLE_NAME,
                    ID_COL_PROJECTION,
                    null, null, null, null,
                    DBHelper.TIMESTAMP_COL + " DESC");
            cursor.move(MAX_ITEMS);
            while (cursor.moveToNext()) {
                String id = cursor.getString(0);
                Log.i(TAG, "Deleting scan history ID " + id);
                db.delete(DBHelper.FIELD_NO_COL, DBHelper.ID_COL + '=' + id, null);
            }
        } catch (SQLiteException sqle) {
            // We're seeing an error here when called in CaptureActivity.onCreate() in rare cases
            // and don't understand it. First theory is that it's transient so can be safely ignored.
            Log.w(TAG, sqle);
            // continue
        } finally {
            close(cursor, db);
        }
    }

    /**
     * <p>Builds a text representation of the scanning history. Each scan is encoded on one
     * line, terminated by a line break (\r\n). The values in each line are comma-separated,
     * and double-quoted. Double-quotes within values are escaped with a sequence of two
     * double-quotes. The fields output are:</p>
     *
     * <ol>
     *  <li>Raw text</li>
     *  <li>Display text</li>
     *  <li>Format (e.g. QR_CODE)</li>
     *  <li>Unix timestamp (milliseconds since the epoch)</li>
     *  <li>Formatted version of timestamp</li>
     *  <li>Supplemental info (e.g. price info for a product barcode)</li>
     * </ol>
     */
    CharSequence buildHistory() {
        SQLiteOpenHelper helper = new DBHelper(activity);
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = helper.getWritableDatabase();
            cursor = db.query(DBHelper.FIELD_TABLE_NAME,
                    COLUMNS,
                    null, null, null, null,
                    DBHelper.TIMESTAMP_COL + " DESC");

            DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
            StringBuilder historyText = new StringBuilder(1000);
            while (cursor.moveToNext()) {

                historyText.append('"').append(massageHistoryField(cursor.getString(0))).append("\",");
                historyText.append('"').append(massageHistoryField(cursor.getString(1))).append("\",");
                historyText.append('"').append(massageHistoryField(cursor.getString(2))).append("\",");
                historyText.append('"').append(massageHistoryField(cursor.getString(3))).append("\",");

                // Add timestamp again, formatted
                long timestamp = cursor.getLong(3);
                historyText.append('"').append(massageHistoryField(
                        format.format(new Date(timestamp)))).append("\",");

                // Above we're preserving the old ordering of columns which had formatted data in position 5

                historyText.append('"').append(massageHistoryField(cursor.getString(4))).append("\"\r\n");
            }
            return historyText;
        } finally {
            close(cursor, db);
        }
    }

    void clearHistory() {
        SQLiteOpenHelper helper = new DBHelper(activity);
        SQLiteDatabase db = null;
        try {
            db = helper.getWritableDatabase();
            db.delete(DBHelper.FIELD_TABLE_NAME, null, null);
        } finally {
            close(null, db);
        }
    }

    static Uri saveHistory(String history) {
        File historyRoot = new File(CONSTANTS.FOLDER);
        if (!historyRoot.exists() && !historyRoot.mkdirs()) {
            Log.w(TAG, "Couldn't make dir " + historyRoot);
            return null;
        }
        File historyFile = new File(historyRoot, "history-" + System.currentTimeMillis() + ".csv");
        OutputStreamWriter out = null;
        try {
            out = new OutputStreamWriter(new FileOutputStream(historyFile), Charset.forName("UTF-8"));
            out.write(history);
            return Uri.parse("file://" + historyFile.getAbsolutePath());
        } catch (IOException ioe) {
            Log.w(TAG, "Couldn't access file " + historyFile + " due to " + ioe);
            return null;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ioe) {
                    // do nothing
                }
            }
        }
    }

    private static String massageHistoryField(String value) {
        return value == null ? "" : value.replace("\"","\"\"");
    }

    private static void close(Cursor cursor, SQLiteDatabase database) {
        if (cursor != null) {
            cursor.close();
        }
        if (database != null) {
            database.close();
        }
    }

}