/*
 * Copyright (C) 2007 The Android Open Source Field
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package aidev.cocis.makerere.org.whiteflycounter.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import aidev.cocis.makerere.org.whiteflycounter.R;
import aidev.cocis.makerere.org.whiteflycounter.common.CommonFunctions;
import aidev.cocis.makerere.org.whiteflycounter.common.Constants;
import aidev.cocis.makerere.org.whiteflycounter.common.MandeUtility;
import aidev.cocis.makerere.org.whiteflycounter.database.WhiteFlyCounterSQLiteOpenHelper;
import aidev.cocis.makerere.org.whiteflycounter.provider.StoryProviderAPI.StoryColumns;

/**
 *
 */
public class StoryProvider extends ContentProvider {

    private static final String t = "StoriesProvider";

    public static final String DATABASE_NAME = "hstories.db";
    private static final int DATABASE_VERSION = 3;
    private static final String STORIES_TABLE_NAME = "hstories";

    private static HashMap<String, String> sStoriesFieldionMap;

    private static final int STORIES = 1;
    private static final int STORY_ID = 2;

    private static final UriMatcher sUriMatcher;

    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends WhiteFlyCounterSQLiteOpenHelper {

        DatabaseHelper(String databaseName) {
            super(Constants.METADATA_PATH, databaseName, null, DATABASE_VERSION);
        }


        @Override
        public void onCreate(SQLiteDatabase db) {
           db.execSQL("CREATE TABLE " + STORIES_TABLE_NAME + " ("
               + StoryColumns._ID + " integer primary key, "
               + StoryColumns.TITLE + " text not null, "
                + StoryColumns.PROJECT_ID + " integer not null, "
               + StoryColumns.STORY_FULLTEXT + " text not null, "
                + StoryColumns.PARTICIPANT_NAME + " text not null, "
                 + StoryColumns.PARTICIPANT_CONTACT + " text not null, "
               + StoryColumns.CAN_EDIT_WHEN_COMPLETE + " text, "
               + StoryColumns.STATUS + " text not null, "
               + StoryColumns.LAST_STATUS_CHANGE_DATE + " date not null, "
               + StoryColumns.DISPLAY_SUBTEXT + " text not null );");
        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        	int initialVersion = oldVersion;
        	if ( oldVersion == 1 ) {
        		db.execSQL("ALTER TABLE " + STORIES_TABLE_NAME + " ADD COLUMN " +
        					StoryColumns.CAN_EDIT_WHEN_COMPLETE + " text;");
        		db.execSQL("UPDATE " + STORIES_TABLE_NAME + " SET " +
        					StoryColumns.CAN_EDIT_WHEN_COMPLETE + " = '" + Boolean.toString(true) + "' WHERE " +
        					StoryColumns.STATUS + " IS NOT NULL AND " +
        					StoryColumns.STATUS + " != '" + StoryProviderAPI.STATUS_INCOMPLETE + "'");
        		oldVersion = 2;
        	}
            Log.w(t, "Successfully upgraded database from version " + initialVersion + " to " + newVersion
                    + ", without destroying all the old data");
        }
    }

    private DatabaseHelper mDbHelper;


    @Override
    public boolean onCreate() {
        // must be at the beginning of any activity that can be called from an external intent
        try {
            CommonFunctions.createWHITEFLYCOUNTERDirs();
        } catch (RuntimeException e) {
            return false;
        }

        mDbHelper = new DatabaseHelper(DATABASE_NAME);
        return true;
    }


    @Override
    public Cursor query(Uri uri, String[] fieldion, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(STORIES_TABLE_NAME);

        switch (sUriMatcher.match(uri)) {
            case STORIES:
                qb.setProjectionMap(sStoriesFieldionMap);
                break;

            case STORY_ID:
                qb.setProjectionMap(sStoriesFieldionMap);
                qb.appendWhere(StoryColumns._ID + "=" + uri.getPathSegments().get(1));
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // Get the database and run the query
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor c = qb.query(db, fieldion, selection, selectionArgs, null, null, sortOrder);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }


    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case STORIES:
                return StoryColumns.CONTENT_TYPE;

            case STORY_ID:
                return StoryColumns.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }


    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        // Validate the requested uri
        if (sUriMatcher.match(uri) != STORIES) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        Long now = Long.valueOf(System.currentTimeMillis());

        // Make sure that the fields are all set
        if (values.containsKey(StoryColumns.LAST_STATUS_CHANGE_DATE) == false) {
            values.put(StoryColumns.LAST_STATUS_CHANGE_DATE, now);
        }

        if (values.containsKey(StoryColumns.DISPLAY_SUBTEXT) == false) {
            Date today = new Date();
            String text = getDisplaySubtext(StoryProviderAPI.STATUS_INCOMPLETE, today);
            values.put(StoryColumns.DISPLAY_SUBTEXT, text);
        }

        if (values.containsKey(StoryColumns.STATUS) == false) {
            values.put(StoryColumns.STATUS, StoryProviderAPI.STATUS_INCOMPLETE);
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long rowId = db.insert(STORIES_TABLE_NAME, null, values);
        if (rowId > 0) {
            Uri storyUri = ContentUris.withAppendedId(StoryColumns.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(storyUri, null);
        	MandeUtility.Log(false, "insert"+
        			storyUri.toString()+ values.getAsString(StoryColumns.TITLE));
            return storyUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    private String getDisplaySubtext(String state, Date date) {
        if (state == null) {
        	return new SimpleDateFormat(getContext().getString(R.string.added_on_date_at_time), Locale.getDefault()).format(date);
        } else if (StoryProviderAPI.STATUS_INCOMPLETE.equalsIgnoreCase(state)) {
        	return new SimpleDateFormat(getContext().getString(R.string.saved_on_date_at_time), Locale.getDefault()).format(date);
        } else if (StoryProviderAPI.STATUS_COMPLETE.equalsIgnoreCase(state)) {
        	return new SimpleDateFormat(getContext().getString(R.string.finalized_on_date_at_time), Locale.getDefault()).format(date);
        } else if (StoryProviderAPI.STATUS_FINALIZED.equalsIgnoreCase(state)) {
        	return new SimpleDateFormat(getContext().getString(R.string.finalized_on_date_at_time), Locale.getDefault()).format(date);
        }  else if (StoryProviderAPI.STATUS_SUBMITTED.equalsIgnoreCase(state)) {
        	return new SimpleDateFormat(getContext().getString(R.string.sent_on_date_at_time), Locale.getDefault()).format(date);
        } else if (StoryProviderAPI.STATUS_SUBMISSION_FAILED.equalsIgnoreCase(state)) {
        	return new SimpleDateFormat(getContext().getString(R.string.sending_failed_on_date_at_time), Locale.getDefault()).format(date);
        } else {
        	return new SimpleDateFormat(getContext().getString(R.string.added_on_date_at_time), Locale.getDefault()).format(date);
        }
    }

    private void deleteAllFilesInDirectory(File directory) {
        if (directory.exists()) {
        	// do not delete the directory if it might be an
        	// WHITEFLYCOUNTER Tables story data directory. Let ODK Tables
        	// manage the lifetimes of its filled-in form data
        	// media attachments.
            if (directory.isDirectory() && !CommonFunctions.isWHITEFLYCOUNTERTablesStoryDataDirectory(directory)) {
            	// delete any media entries for files in this directory...
                int images = MediaUtils.deleteImagesInFolderFromMediaProvider(getContext(),directory);
                int audio = MediaUtils.deleteAudioInFolderFromMediaProvider(getContext(),directory);
                int video = MediaUtils.deleteVideoInFolderFromMediaProvider(getContext(),directory);

                Log.i(t, "removed from content providers: " + images
                        + " image files, " + audio + " audio files,"
                        + " and " + video + " video files.");

                // delete all the files in the directory
                File[] files = directory.listFiles();
                for (File f : files) {
                    // should make this recursive if we get worried about
                    // the media directory containing directories
                    f.delete();
                }
            }
            directory.delete();
        }
    }


    /**
     * This method removes the entry from the content provider, and also removes any associated files.
     * files:  form.xml, [formmd5].formdef, formname-media {directory}
     */
    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count;

        switch (sUriMatcher.match(uri)) {
            case STORIES:
                Cursor del = null;
                try {
                	del = this.query(uri, null, where, whereArgs, null);
                	if (del.getCount() > 0) {
                		del.moveToFirst();
                		do {
		                    String storyFile = del.getString(del.getColumnIndex(StoryColumns._ID));
		                    MandeUtility.Log(false, "delete:"+ storyFile);
		                    File storyDir = (new File(storyFile)).getParentFile();
		                    deleteAllFilesInDirectory(storyDir);
                		} while (del.moveToNext());
	                }
                } finally {
                	if ( del != null ) {
                		del.close();
                	}
                }
                count = db.delete(STORIES_TABLE_NAME, where, whereArgs);
                break;

            case STORY_ID:
                String storyId = uri.getPathSegments().get(1);

                Cursor c = null;
                try {
                	c = this.query(uri, null, where, whereArgs, null);
                	if (c.getCount() > 0) {
                		c.moveToFirst();
                		do {
		                    String storyFile = c.getString(c.getColumnIndex(StoryColumns._ID));
		                    MandeUtility.Log(false, "delete:"+ storyFile);
		                    File storyDir = (new File(storyFile)).getParentFile();
		                    deleteAllFilesInDirectory(storyDir);
                		} while (c.moveToNext());
	                }
                } finally {
                	if ( c != null ) {
                		c.close();
                	}
                }

                count =
                    db.delete(STORIES_TABLE_NAME,
                        StoryColumns._ID + "=" + storyId
                                + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""),
                        whereArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }


    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        Long now = Long.valueOf(System.currentTimeMillis());

        // Make sure that the fields are all set
        if (values.containsKey(StoryColumns.LAST_STATUS_CHANGE_DATE) == false) {
            values.put(StoryColumns.LAST_STATUS_CHANGE_DATE, now);
        }

        int count;
        String status = null;
        switch (sUriMatcher.match(uri)) {
            case STORIES:
                if (values.containsKey(StoryColumns.STATUS)) {
                    status = values.getAsString(StoryColumns.STATUS);

                    if (values.containsKey(StoryColumns.DISPLAY_SUBTEXT) == false) {
                        Date today = new Date();
                        String text = getDisplaySubtext(status, today);
                        values.put(StoryColumns.DISPLAY_SUBTEXT, text);
                    }
                }

                count = db.update(STORIES_TABLE_NAME, values, where, whereArgs);
                break;

            case STORY_ID:
                String storyId = uri.getPathSegments().get(1);

                if (values.containsKey(StoryColumns.STATUS)) {
                    status = values.getAsString(StoryColumns.STATUS);

                    if (values.containsKey(StoryColumns.DISPLAY_SUBTEXT) == false) {
                        Date today = new Date();
                        String text = getDisplaySubtext(status, today);
                        values.put(StoryColumns.DISPLAY_SUBTEXT, text);
                    }
                }

                count =
                    db.update(STORIES_TABLE_NAME, values, StoryColumns._ID + "=" + storyId
                            + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(StoryProviderAPI.AUTHORITY, "stories", STORIES);
        sUriMatcher.addURI(StoryProviderAPI.AUTHORITY, "stories/#", STORY_ID);

        sStoriesFieldionMap = new HashMap<String, String>();
        sStoriesFieldionMap.put(StoryColumns._ID, StoryColumns._ID);
        sStoriesFieldionMap.put(StoryColumns.PROJECT_ID, StoryColumns.PROJECT_ID);
        sStoriesFieldionMap.put(StoryColumns.TITLE, StoryColumns.TITLE);
        sStoriesFieldionMap.put(StoryColumns.CAN_EDIT_WHEN_COMPLETE, StoryColumns.CAN_EDIT_WHEN_COMPLETE);
        sStoriesFieldionMap.put(StoryColumns.STATUS, StoryColumns.STATUS);
        sStoriesFieldionMap.put(StoryColumns.STORY_FULLTEXT, StoryColumns.STORY_FULLTEXT);
        sStoriesFieldionMap.put(StoryColumns.PARTICIPANT_NAME, StoryColumns.PARTICIPANT_NAME);
        sStoriesFieldionMap.put(StoryColumns.PARTICIPANT_CONTACT, StoryColumns.PARTICIPANT_CONTACT);
        sStoriesFieldionMap.put(StoryColumns.LAST_STATUS_CHANGE_DATE, StoryColumns.LAST_STATUS_CHANGE_DATE);
        sStoriesFieldionMap.put(StoryColumns.DISPLAY_SUBTEXT, StoryColumns.DISPLAY_SUBTEXT);
    }

}
