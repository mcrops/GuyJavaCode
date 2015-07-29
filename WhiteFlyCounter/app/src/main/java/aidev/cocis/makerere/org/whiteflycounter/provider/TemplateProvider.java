/*
 * Copyright (C) 2007 The Android Open Source Template
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
import aidev.cocis.makerere.org.whiteflycounter.provider.TemplateProviderAPI.TemplateColumns;

/**
 *
 */
public class TemplateProvider extends ContentProvider {

	private static final String t = "TemplateProvider";

	public static final String DATABASE_NAME = "btemplate.db";
	private static final int DATABASE_VERSION = 1;
	private static final String TEMPLATES_TABLE_NAME = "btemplate";

	private static HashMap<String, String> sTemplatesTemplateionMap;

	private static final int TEMPLATES = 1;
	private static final int TEMPLATE_ID = 2;

	private static final UriMatcher sUriMatcher;

	/**
	 * This class helps open, create, and upgrade the database file.
	 */
	private static class DatabaseHelper extends WhiteFlyCounterSQLiteOpenHelper {
	
		private static final String TEMP_TEMPLATES_TABLE_NAME = "templates_v1";

		DatabaseHelper(String databaseName) {
			super(Constants.METADATA_PATH, databaseName, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			onCreateNamed(db, TEMPLATES_TABLE_NAME);
		}

		private void onCreateNamed(SQLiteDatabase db, String tableName) {
			db.execSQL("CREATE TABLE " + tableName + " (" + TemplateColumns._ID
					+ " integer primary key, " + TemplateColumns.PROJECT_ID
					+ " integer null, " + TemplateColumns.NAME
					+ " text null, " + TemplateColumns.DISPLAY_SUBTEXT
					+ " text null, "
					+ TemplateColumns.DATE
					+ " integer not null );");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			int initialVersion = oldVersion;
			if (oldVersion < 2) {
				Log.w(t, "Upgrading database from version " + oldVersion
						+ " to " + newVersion
						+ ", which will destroy all old data");
				db.execSQL("DROP TABLE IF EXISTS " + TEMPLATES_TABLE_NAME);
				onCreate(db);
				return;
			} else {
				// adding BASE64_RSA_PUBLIC_KEY and changing type and name of
				// integer MODEL_VERSION to text VERSION
				db.execSQL("DROP TABLE IF EXISTS " + TEMP_TEMPLATES_TABLE_NAME);
				onCreateNamed(db, TEMP_TEMPLATES_TABLE_NAME);
				db.execSQL("INSERT INTO "
						+ TEMP_TEMPLATES_TABLE_NAME
						+ " ("
						+ TemplateColumns._ID
						+ ", "
						+ TemplateColumns.PROJECT_ID
						+ ", "
						+ TemplateColumns.NAME
						+ ", "
						+ TemplateColumns.DISPLAY_SUBTEXT
						+ ", "
						+ TemplateColumns.DATE
						+ ") SELECT "
						+ TemplateColumns._ID
						+ ", "
						+ TemplateColumns.PROJECT_ID
						+ ", "
						+ TemplateColumns.NAME
						+ ", "
						+ TemplateColumns.DISPLAY_SUBTEXT
						+ ", "
						+ TemplateColumns.DATE
						+ " FROM "
						+ TEMPLATES_TABLE_NAME);

				// risky failures here...
				db.execSQL("DROP TABLE IF EXISTS " + TEMPLATES_TABLE_NAME);
				onCreateNamed(db, TEMPLATES_TABLE_NAME);
				db.execSQL("INSERT INTO "
						+ TEMPLATES_TABLE_NAME
						+ " ("
						+ TemplateColumns._ID
						+ ", "
						+ TemplateColumns.PROJECT_ID
						+ ", "
						+ TemplateColumns.NAME
						+ ", "
						+ TemplateColumns.DISPLAY_SUBTEXT
						+ ", "
						+ TemplateColumns.DATE  + ") SELECT "
						+ TemplateColumns._ID
						+ ", "
						+ TemplateColumns.PROJECT_ID
						+ ", "
						+ TemplateColumns.NAME
						+ ", "
						+ TemplateColumns.DISPLAY_SUBTEXT
						+ ", "
						+ TemplateColumns.DATE+ " FROM "
						+ TEMP_TEMPLATES_TABLE_NAME);
				db.execSQL("DROP TABLE IF EXISTS " + TEMP_TEMPLATES_TABLE_NAME);

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
	public Cursor query(Uri uri, String[] templateion, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(TEMPLATES_TABLE_NAME);

		switch (sUriMatcher.match(uri)) {
		case TEMPLATES:
			qb.setProjectionMap(sTemplatesTemplateionMap);
			break;

		case TEMPLATE_ID:
			qb.setProjectionMap(sTemplatesTemplateionMap);
			qb.appendWhere(TemplateColumns._ID + "="
					+ uri.getPathSegments().get(1));
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// Get the database and run the query
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor c = qb.query(db, templateion, selection, selectionArgs, null,
				null, sortOrder);

		// Tell the cursor what uri to watch, so it knows when its source data
		// changes
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case TEMPLATES:
			return TemplateColumns.CONTENT_TYPE;

		case TEMPLATE_ID:
			return TemplateColumns.CONTENT_ITEM_TYPE;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public synchronized Uri insert(Uri uri, ContentValues initialValues) {
		// Validate the requested uri
		if (sUriMatcher.match(uri) != TEMPLATES) {
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
		if (values.containsKey(TemplateColumns.DATE) == false) {
			values.put(TemplateColumns.DATE, now);
		}

		if (values.containsKey(TemplateColumns.DISPLAY_SUBTEXT) == false) {
			Date today = new Date();
			String ts = new SimpleDateFormat(getContext().getString(
					R.string.added_on_date_at_time), Locale.getDefault())
					.format(today);
			values.put(TemplateColumns.DISPLAY_SUBTEXT, ts);
		}

		if (values.containsKey(TemplateColumns.PROJECT_ID) == false) {
			values.put(TemplateColumns.PROJECT_ID, values.getAsString(TemplateColumns.PROJECT_ID));
		}
		if (values.containsKey(TemplateColumns.NAME) == false) {
			values.put(TemplateColumns.NAME, values.getAsString(TemplateColumns.NAME));
		}
		
		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		// first try to see if a record with this id already exists...
		String[] templateion = { TemplateColumns.PROJECT_ID, TemplateColumns.PROJECT_ID };
		String[] selectionArgs = { values.getAsString(TemplateColumns.PROJECT_ID) };
		String selection = TemplateColumns.PROJECT_ID + "=?";
		Cursor c = null;
		try {
			c = db.query(TEMPLATES_TABLE_NAME, templateion, selection,
					selectionArgs, null, null, null);
			if (c.getCount() > 0) {
				// already exists
				throw new SQLException("FAILED Insert into " + uri
						+ " -- row already exists for template definition: "
						+ values.getAsString(TemplateColumns.PROJECT_ID));
			}
		} finally {
			if (c != null) {
				c.close();
			}
		}

		long rowId = db.insert(TEMPLATES_TABLE_NAME, null, values);
		if (rowId > 0) {
			Uri templateUri = ContentUris.withAppendedId(TemplateColumns.CONTENT_URI,
					rowId);
			getContext().getContentResolver().notifyChange(templateUri, null);
			MandeUtility.Log(false,"insert"+ templateUri.toString()+
							values.getAsString(TemplateColumns.PROJECT_ID));
			return templateUri;
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
	 * any associated files. files: template.xml, [templatemd5].templatedef, templatename-media
	 * {directory}
	 */
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		int count;

		switch (sUriMatcher.match(uri)) {
		case TEMPLATES:
			Cursor del = null;
			try {
				del = this.query(uri, null, where, whereArgs, null);
				if (del.getCount() > 0) {
					del.moveToFirst();
					do {
						String templateFilePath = del.getString(del
								.getColumnIndex(TemplateColumns.PROJECT_ID));
						MandeUtility.Log(false, "delete"+templateFilePath);
						deleteFileOrDir(templateFilePath);
					} while (del.moveToNext());
				}
			} finally {
				if (del != null) {
					del.close();
				}
			}
			count = db.delete(TEMPLATES_TABLE_NAME, where, whereArgs);
			break;

		case TEMPLATE_ID:
			String templateId = uri.getPathSegments().get(1);

			Cursor c = null;
			try {
				c = this.query(uri, null, where, whereArgs, null);
				// This should only ever return 1 record.
				if (c.getCount() > 0) {
					c.moveToFirst();
					do {
						
						String templateFilePath = c.getString(c
								.getColumnIndex(TemplateColumns.PROJECT_ID));
						MandeUtility.Log(false,"delete:"+ templateFilePath);
						deleteFileOrDir(templateFilePath);
					
						
					
						 
					} while (c.moveToNext());
				}
			} finally {
				if (c != null) {
					c.close();
				}
			}

			count = db.delete(
					TEMPLATES_TABLE_NAME,
					TemplateColumns._ID
							+ "="
							+ templateId
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
		case TEMPLATES:
			

			Cursor c = null;
			try {
				c = this.query(uri, null, where, whereArgs, null);

				if (c.getCount() > 0) {
					c.moveToPosition(-1);
					while (c.moveToNext()) {
						// before updating the paths, delete all the files
						if (values.containsKey(TemplateColumns.PROJECT_ID)) {
							String newFile = values
									.getAsString(TemplateColumns.PROJECT_ID);
							String delFile = c
									.getString(c
											.getColumnIndex(TemplateColumns.PROJECT_ID));
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
			if (values.containsKey(TemplateColumns.DATE) == true) {
				Date today = new Date();
				String ts = new SimpleDateFormat(getContext().getString(
						R.string.added_on_date_at_time), Locale.getDefault())
						.format(today);
				values.put(TemplateColumns.DISPLAY_SUBTEXT, ts);
			}

			count = db.update(TEMPLATES_TABLE_NAME, values, where, whereArgs);
			break;

		case TEMPLATE_ID:
			String templateId = uri.getPathSegments().get(1);
			// Whenever file paths are updated, delete the old files.

			Cursor update = null;
			try {
				update = this.query(uri, null, where, whereArgs, null);

				// This should only ever return 1 record.
				if (update.getCount() > 0) {
					update.moveToFirst();

					// the order here is important (jrcache needs to be before
					// template file)
					// because we update the jrcache file if there's a new template
					// file
					

					if (values.containsKey(TemplateColumns.PROJECT_ID)) {
						String templateFile = values
								.getAsString(TemplateColumns.PROJECT_ID);
						String oldFile = update.getString(update
								.getColumnIndex(TemplateColumns.PROJECT_ID));

						if (templateFile != null
								&& templateFile.equalsIgnoreCase(oldFile)) {
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
					if (values.containsKey(TemplateColumns.DATE) == true) {
						Date today = new Date();
						String ts = new SimpleDateFormat(getContext()
								.getString(R.string.added_on_date_at_time),
								Locale.getDefault()).format(today);
						values.put(TemplateColumns.DISPLAY_SUBTEXT, ts);
					}

					count = db.update(
							TEMPLATES_TABLE_NAME,
							values,
							TemplateColumns._ID
									+ "="
									+ templateId
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
		sUriMatcher.addURI(TemplateProviderAPI.AUTHORITY, "templates", TEMPLATES);
		sUriMatcher.addURI(TemplateProviderAPI.AUTHORITY, "templates/#", TEMPLATE_ID);

		sTemplatesTemplateionMap = new HashMap<String, String>();
		sTemplatesTemplateionMap.put(TemplateColumns._ID, TemplateColumns._ID);
		sTemplatesTemplateionMap.put(TemplateColumns.PROJECT_ID,
				TemplateColumns.PROJECT_ID);
		sTemplatesTemplateionMap.put(TemplateColumns.NAME,
				TemplateColumns.NAME);
		
	}

}
