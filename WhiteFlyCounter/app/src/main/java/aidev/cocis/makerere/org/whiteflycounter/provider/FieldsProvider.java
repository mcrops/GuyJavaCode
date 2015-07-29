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
import aidev.cocis.makerere.org.whiteflycounter.provider.FieldsProviderAPI.FieldsColumns;

/**
 *
 */
public class FieldsProvider extends ContentProvider {

	private static final String t = "FieldsProvider";

	public static final String DATABASE_NAME = "bfields.db";
	private static final int DATABASE_VERSION = 1;
	private static final String PROJECTS_TABLE_NAME = "bfields";

	private static HashMap<String, String> sFieldsFieldionMap;

	private static final int PROJECTS = 1;
	private static final int PROJECT_ID = 2;

	private static final UriMatcher sUriMatcher;

	/**
	 * This class helps open, create, and upgrade the database file.
	 */
	private static class DatabaseHelper extends WhiteFlyCounterSQLiteOpenHelper {
	
		private static final String TEMP_PROJECTS_TABLE_NAME = "fields_v1";

		DatabaseHelper(String databaseName) {
			super(Constants.METADATA_PATH, databaseName, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			onCreateNamed(db, PROJECTS_TABLE_NAME);
		}

		private void onCreateNamed(SQLiteDatabase db, String tableName) {
			db.execSQL("CREATE TABLE " + tableName + " (" + FieldsColumns._ID
					+ " integer primary key, " + FieldsColumns.WHITEFLYCOUNTER_PROJECT_ID
					+ " integer null, " + FieldsColumns.NAME
					+ " text null, " + FieldsColumns.DESCRIPTION
					+ " text null, " + FieldsColumns.DISPLAY_SUBTEXT
					+ " text null, "
					+ FieldsColumns.DATE
					+ " integer not null, " // milliseconds  
					+ FieldsColumns.LANGUAGE + " text );");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			int initialVersion = oldVersion;
			if (oldVersion < 2) {
				Log.w(t, "Upgrading database from version " + oldVersion
						+ " to " + newVersion
						+ ", which will destroy all old data");
				db.execSQL("DROP TABLE IF EXISTS " + PROJECTS_TABLE_NAME);
				onCreate(db);
				return;
			} else {
				// adding BASE64_RSA_PUBLIC_KEY and changing type and name of
				// integer MODEL_VERSION to text VERSION
				db.execSQL("DROP TABLE IF EXISTS " + TEMP_PROJECTS_TABLE_NAME);
				onCreateNamed(db, TEMP_PROJECTS_TABLE_NAME);
				db.execSQL("INSERT INTO "
						+ TEMP_PROJECTS_TABLE_NAME
						+ " ("
						+ FieldsColumns._ID
						+ ", "
						+ FieldsColumns.NAME
						+ ", "
						+ FieldsColumns.DISPLAY_SUBTEXT
						+ ", "
						+ FieldsColumns.DESCRIPTION
						+ ", "
						+ FieldsColumns.WHITEFLYCOUNTER_PROJECT_ID
						+ ", "
						+ FieldsColumns.DATE
						+ ", "
						+ FieldsColumns.LANGUAGE
						+ ") SELECT "
						+ FieldsColumns._ID
						+ ", "
						+ FieldsColumns.NAME
						+ ", "
						+ FieldsColumns.DISPLAY_SUBTEXT
						+ ", "
						+ FieldsColumns.DESCRIPTION
						+ ", "
						+ FieldsColumns.WHITEFLYCOUNTER_PROJECT_ID
						+ ", "
						+ FieldsColumns.DATE
						+ ", "
						+ FieldsColumns.LANGUAGE
						+ " FROM "
						+ PROJECTS_TABLE_NAME);

				// risky failures here...
				db.execSQL("DROP TABLE IF EXISTS " + PROJECTS_TABLE_NAME);
				onCreateNamed(db, PROJECTS_TABLE_NAME);
				db.execSQL("INSERT INTO "
						+ PROJECTS_TABLE_NAME
						+ " ("
						+ FieldsColumns._ID
						+ ", "
						+ FieldsColumns.NAME
						+ ", "
						+ FieldsColumns.DISPLAY_SUBTEXT
						+ ", "
						+ FieldsColumns.DESCRIPTION
						+ ", "
						+ FieldsColumns.WHITEFLYCOUNTER_PROJECT_ID
						+ ", "
						+ FieldsColumns.DATE
						+ FieldsColumns.LANGUAGE  + ") SELECT "
						+ FieldsColumns._ID + ", "
						+ FieldsColumns.NAME
						+ ", "
						+ FieldsColumns.DISPLAY_SUBTEXT
						+ ", "
						+ FieldsColumns.DESCRIPTION
						+ ", "
						+ FieldsColumns.WHITEFLYCOUNTER_PROJECT_ID
						+ ", "
						+ FieldsColumns.DATE
						+ FieldsColumns.LANGUAGE + " FROM "
						+ TEMP_PROJECTS_TABLE_NAME);
				db.execSQL("DROP TABLE IF EXISTS " + TEMP_PROJECTS_TABLE_NAME);

				Log.w(t, "Successfully upgraded database from version "
						+ initialVersion + " to " + newVersion
						+ ", without destroying all the old data");
			}
		}
	}

	private DatabaseHelper mDbHelper;

	@Override
	public boolean onCreate() {

		// must be at the beginning of any activity that can be called from an
		// external intent
		try {
			CommonFunctions.createWHITEFLYCOUNTERDirs();
		} catch (RuntimeException e) {
			return false;
		}

		mDbHelper = new DatabaseHelper(DATABASE_NAME);
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] fieldion, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(PROJECTS_TABLE_NAME);

		switch (sUriMatcher.match(uri)) {
		case PROJECTS:
			qb.setProjectionMap(sFieldsFieldionMap);
			break;

		case PROJECT_ID:
			qb.setProjectionMap(sFieldsFieldionMap);
			qb.appendWhere(FieldsColumns._ID + "="
					+ uri.getPathSegments().get(1));
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// Get the database and run the query
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor c = qb.query(db, fieldion, selection, selectionArgs, null,
				null, sortOrder);

		// Tell the cursor what uri to watch, so it knows when its source data
		// changes
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case PROJECTS:
			return FieldsColumns.CONTENT_TYPE;

		case PROJECT_ID:
			return FieldsColumns.CONTENT_ITEM_TYPE;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public synchronized Uri insert(Uri uri, ContentValues initialValues) {
		// Validate the requested uri
		if (sUriMatcher.match(uri) != PROJECTS) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}


		

		Long now = Long.valueOf(System.currentTimeMillis());

		// Make sure that the necessary fields are all set
		if (values.containsKey(FieldsColumns.DATE) == false) {
			values.put(FieldsColumns.DATE, now);
		}

		if (values.containsKey(FieldsColumns.DISPLAY_SUBTEXT) == false) {
			Date today = new Date();
			String ts = new SimpleDateFormat(getContext().getString(
					R.string.added_on_date_at_time), Locale.getDefault())
					.format(today);
			values.put(FieldsColumns.DISPLAY_SUBTEXT, ts);
		}

		if (values.containsKey(FieldsColumns.NAME) == false) {
			values.put(FieldsColumns.NAME, values.getAsString(FieldsColumns.NAME));
		}
		
		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		// first try to see if a record with this id already exists...
		String[] fieldion = { FieldsColumns._ID, FieldsColumns.WHITEFLYCOUNTER_PROJECT_ID };
		String[] selectionArgs = { values.getAsString(FieldsColumns.WHITEFLYCOUNTER_PROJECT_ID) };
		String selection = FieldsColumns.WHITEFLYCOUNTER_PROJECT_ID + "=?";
		Cursor c = null;
		try {
			c = db.query(PROJECTS_TABLE_NAME, fieldion, selection,
					selectionArgs, null, null, null);
			if (c.getCount() > 0) {
				// already exists
				throw new SQLException("FAILED Insert into " + uri
						+ " -- row already exists for field definition: "
						+ values.getAsString(FieldsColumns.WHITEFLYCOUNTER_PROJECT_ID));
			}
		} finally {
			if (c != null) {
				c.close();
			}
		}

		long rowId = db.insert(PROJECTS_TABLE_NAME, null, values);
		if (rowId > 0) {
			Uri fieldUri = ContentUris.withAppendedId(FieldsColumns.CONTENT_URI,
					rowId);
			getContext().getContentResolver().notifyChange(fieldUri, null);
			MandeUtility.Log(false,"insert"+ fieldUri.toString()+
							values.getAsString(FieldsColumns.WHITEFLYCOUNTER_PROJECT_ID));
			return fieldUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	private void deleteFileOrDir(String fileName) {
		File file = new File(fileName);
		if (file.exists()) {
			if (file.isDirectory()) {
				// delete any media entries for files in this directory...
				int images = MediaUtils
						.deleteImagesInFolderFromMediaProvider(getContext(),file);
				int audio = MediaUtils
						.deleteAudioInFolderFromMediaProvider(getContext(), file);
				int video = MediaUtils
						.deleteVideoInFolderFromMediaProvider(getContext(),file);

				Log.i(t, "removed from content providers: " + images
						+ " image files, " + audio + " audio files," + " and "
						+ video + " video files.");

				// delete all the containing files
				File[] files = file.listFiles();
				for (File f : files) {
					// should make this recursive if we get worried about
					// the media directory containing directories
					Log.i(t,
							"attempting to delete file: " + f.getAbsolutePath());
					f.delete();
				}
			}
			file.delete();
			Log.i(t, "attempting to delete file: " + file.getAbsolutePath());
		}
	}

	/**
	 * This method removes the entry from the content provider, and also removes
	 * any associated files. files: field.xml, [fieldmd5].fielddef, fieldname-media
	 * {directory}
	 */
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		int count;

		switch (sUriMatcher.match(uri)) {
		case PROJECTS:
			Cursor del = null;
			try {
				del = this.query(uri, null, where, whereArgs, null);
				if (del.getCount() > 0) {
					del.moveToFirst();
					do {
						String fieldFilePath = del.getString(del
								.getColumnIndex(FieldsColumns.WHITEFLYCOUNTER_PROJECT_ID));
						MandeUtility.Log(false, "delete"+fieldFilePath);
						deleteFileOrDir(fieldFilePath);
					} while (del.moveToNext());
				}
			} finally {
				if (del != null) {
					del.close();
				}
			}
			count = db.delete(PROJECTS_TABLE_NAME, where, whereArgs);
			break;

		case PROJECT_ID:
			String fieldId = uri.getPathSegments().get(1);

			Cursor c = null;
			try {
				c = this.query(uri, null, where, whereArgs, null);
				// This should only ever return 1 record.
				if (c.getCount() > 0) {
					c.moveToFirst();
					do {
						
						String fieldFilePath = c.getString(c
								.getColumnIndex(FieldsColumns.WHITEFLYCOUNTER_PROJECT_ID));
						MandeUtility.Log(false,"delete:"+ fieldFilePath);
						deleteFileOrDir(fieldFilePath);
					
						
					
						 
					} while (c.moveToNext());
				}
			} finally {
				if (c != null) {
					c.close();
				}
			}

			count = db.delete(
					PROJECTS_TABLE_NAME,
					FieldsColumns._ID
							+ "="
							+ fieldId
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		int count = 0;
		switch (sUriMatcher.match(uri)) {
		case PROJECTS:
			

			Cursor c = null;
			try {
				c = this.query(uri, null, where, whereArgs, null);

				if (c.getCount() > 0) {
					c.moveToPosition(-1);
					while (c.moveToNext()) {
						// before updating the paths, delete all the files
						if (values.containsKey(FieldsColumns.WHITEFLYCOUNTER_PROJECT_ID)) {
							String newFile = values
									.getAsString(FieldsColumns.WHITEFLYCOUNTER_PROJECT_ID);
							String delFile = c
									.getString(c
											.getColumnIndex(FieldsColumns.WHITEFLYCOUNTER_PROJECT_ID));
							if (newFile.equalsIgnoreCase(delFile)) {
								// same file, so don't delete anything
							} else {
								// different files, delete the old one
								deleteFileOrDir(delFile);
							}

							
						}
					}
				}
			} finally {
				if (c != null) {
					c.close();
				}
			}

			// Make sure that the necessary fields are all set
			if (values.containsKey(FieldsColumns.DATE) == true) {
				Date today = new Date();
				String ts = new SimpleDateFormat(getContext().getString(
						R.string.added_on_date_at_time), Locale.getDefault())
						.format(today);
				values.put(FieldsColumns.DISPLAY_SUBTEXT, ts);
			}

			count = db.update(PROJECTS_TABLE_NAME, values, where, whereArgs);
			break;

		case PROJECT_ID:
			String fieldId = uri.getPathSegments().get(1);
			// Whenever file paths are updated, delete the old files.

			Cursor update = null;
			try {
				update = this.query(uri, null, where, whereArgs, null);

				// This should only ever return 1 record.
				if (update.getCount() > 0) {
					update.moveToFirst();

					// the order here is important (jrcache needs to be before
					// field file)
					// because we update the jrcache file if there's a new field
					// file
					

					if (values.containsKey(FieldsColumns.WHITEFLYCOUNTER_PROJECT_ID)) {
						String fieldFile = values
								.getAsString(FieldsColumns.WHITEFLYCOUNTER_PROJECT_ID);
						String oldFile = update.getString(update
								.getColumnIndex(FieldsColumns.WHITEFLYCOUNTER_PROJECT_ID));

						if (fieldFile != null
								&& fieldFile.equalsIgnoreCase(oldFile)) {
							// Files are the same, so we may have just copied
							// over something we had
							// already
						} else {
							// New file name. This probably won't ever happen,
							// though.
							deleteFileOrDir(oldFile);
						}

						
					}

					// Make sure that the necessary fields are all set
					if (values.containsKey(FieldsColumns.DATE) == true) {
						Date today = new Date();
						String ts = new SimpleDateFormat(getContext()
								.getString(R.string.added_on_date_at_time),
								Locale.getDefault()).format(today);
						values.put(FieldsColumns.DISPLAY_SUBTEXT, ts);
					}

					count = db.update(
							PROJECTS_TABLE_NAME,
							values,
							FieldsColumns._ID
									+ "="
									+ fieldId
									+ (!TextUtils.isEmpty(where) ? " AND ("
											+ where + ')' : ""), whereArgs);
				} else {
					Log.e(t, "Attempting to update row that does not exist");
				}
			} finally {
				if (update != null) {
					update.close();
				}
			}
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(FieldsProviderAPI.AUTHORITY, "fields", PROJECTS);
		sUriMatcher.addURI(FieldsProviderAPI.AUTHORITY, "fields/#", PROJECT_ID);

		sFieldsFieldionMap = new HashMap<String, String>();
		sFieldsFieldionMap.put(FieldsColumns._ID, FieldsColumns._ID);
		sFieldsFieldionMap.put(FieldsColumns.NAME,
				FieldsColumns.NAME);
		sFieldsFieldionMap.put(FieldsColumns.DISPLAY_SUBTEXT,
				FieldsColumns.DISPLAY_SUBTEXT);
		sFieldsFieldionMap.put(FieldsColumns.DESCRIPTION,
				FieldsColumns.DESCRIPTION);
		sFieldsFieldionMap.put(FieldsColumns.WHITEFLYCOUNTER_PROJECT_ID,
				FieldsColumns.WHITEFLYCOUNTER_PROJECT_ID);
		sFieldsFieldionMap.put(FieldsColumns.DATE, FieldsColumns.DATE);
		sFieldsFieldionMap.put(FieldsColumns.WHITEFLYCOUNTER_PROJECT_ID,
				FieldsColumns.WHITEFLYCOUNTER_PROJECT_ID);
		sFieldsFieldionMap.put(FieldsColumns.LANGUAGE, FieldsColumns.LANGUAGE);
	}

}
