/*
 * Copyright (C) 2007 The Android Open Source ReportingPeriod
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
import aidev.cocis.makerere.org.whiteflycounter.provider.ReportingPeriodProviderAPI.ReportingPeriodColumns;

/**
 *
 */
public class ReportingPeriodProvider extends ContentProvider {

	private static final String t = "ReportingPeriodProvider";

	public static final String DATABASE_NAME = "breportingperiod.db";
	private static final int DATABASE_VERSION = 1;
	private static final String REPORTING_PERIODS_TABLE_NAME = "breportingperiod";

	private static HashMap<String, String> sReportingPeriodsReportingPeriodionMap;

	private static final int REPORTING_PERIODS = 1;
	private static final int REPORTING_PERIOD_ID = 2;

	private static final UriMatcher sUriMatcher;

	/**
	 * This class helps open, create, and upgrade the database file.
	 */
	private static class DatabaseHelper extends WhiteFlyCounterSQLiteOpenHelper {
	
		private static final String TEMP_REPORTING_PERIODS_TABLE_NAME = "reportingperiods_v1";

		DatabaseHelper(String databaseName) {
			super(Constants.METADATA_PATH, databaseName, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			onCreateNamed(db, REPORTING_PERIODS_TABLE_NAME);
		}

		private void onCreateNamed(SQLiteDatabase db, String tableName) {
			db.execSQL("CREATE TABLE " + tableName + " (" + ReportingPeriodColumns._ID
					+ " integer primary key, " + ReportingPeriodColumns.PROJECT_ID
					+ " integer null, " + ReportingPeriodColumns.RESEARCH_START_DATE
					+ " text null, " + ReportingPeriodColumns.RESEARCH_END_DATE
					+ " text null, " + ReportingPeriodColumns.STORY_INPUT_DEADLINE
					+ " text null, " + ReportingPeriodColumns.REPORT_PERIOD_CLOSE_DATE
					+ " text null, " + ReportingPeriodColumns.PERIOD_DESCRIPTION
					+ " text null, " + ReportingPeriodColumns.PERIOD_ACTIVE
					+ " text null, " + ReportingPeriodColumns.PERIOD_STATUS
					+ " text null, " + ReportingPeriodColumns.DISPLAY_SUBTEXT
					+ " text null, "
					+ ReportingPeriodColumns.DATE
					+ " integer not null );");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			int initialVersion = oldVersion;
			if (oldVersion < 2) {
				Log.w(t, "Upgrading database from version " + oldVersion
						+ " to " + newVersion
						+ ", which will destroy all old data");
				db.execSQL("DROP TABLE IF EXISTS " + REPORTING_PERIODS_TABLE_NAME);
				onCreate(db);
				return;
			} else {
				// adding BASE64_RSA_PUBLIC_KEY and changing type and name of
				// integer MODEL_VERSION to text VERSION
				db.execSQL("DROP TABLE IF EXISTS " + TEMP_REPORTING_PERIODS_TABLE_NAME);
				onCreateNamed(db, TEMP_REPORTING_PERIODS_TABLE_NAME);
				db.execSQL("INSERT INTO "
						+ TEMP_REPORTING_PERIODS_TABLE_NAME
						+ " ("
						+ ReportingPeriodColumns._ID
						+ ", "
						+ ReportingPeriodColumns.PROJECT_ID
						+ ", "
						+ ReportingPeriodColumns.REPORT_PERIOD_CLOSE_DATE
						+ ", "
						+ ReportingPeriodColumns.PERIOD_DESCRIPTION
						+ ", "
						+ ReportingPeriodColumns.PERIOD_ACTIVE
						+ ", "
						+ ReportingPeriodColumns.PERIOD_STATUS
						+ ", "
						+ ReportingPeriodColumns.DISPLAY_SUBTEXT
						+ ", "
						+ ReportingPeriodColumns.RESEARCH_START_DATE
						+ ", "
						+ ReportingPeriodColumns.RESEARCH_END_DATE
						+ ", "
						+ ReportingPeriodColumns.DATE
						+ ", "
						+ ReportingPeriodColumns.STORY_INPUT_DEADLINE
						+ ") SELECT "
						+ ReportingPeriodColumns._ID
						+ ", "
						+ ReportingPeriodColumns.PROJECT_ID
						+ ", "
						+ ReportingPeriodColumns.REPORT_PERIOD_CLOSE_DATE
						+ ", "
						+ ReportingPeriodColumns.PERIOD_DESCRIPTION
						+ ", "
						+ ReportingPeriodColumns.PERIOD_ACTIVE
						+ ", "
						+ ReportingPeriodColumns.PERIOD_STATUS
						+ ", "
						+ ReportingPeriodColumns.DISPLAY_SUBTEXT
						+ ", "
						+ ReportingPeriodColumns.RESEARCH_START_DATE
						+ ", "
						+ ReportingPeriodColumns.RESEARCH_END_DATE
						+ ", "
						+ ReportingPeriodColumns.DATE
						+ ", "
						+ ReportingPeriodColumns.STORY_INPUT_DEADLINE
						+ " FROM "
						+ REPORTING_PERIODS_TABLE_NAME);

				// risky failures here...
				db.execSQL("DROP TABLE IF EXISTS " + REPORTING_PERIODS_TABLE_NAME);
				onCreateNamed(db, REPORTING_PERIODS_TABLE_NAME);
				db.execSQL("INSERT INTO "
						+ REPORTING_PERIODS_TABLE_NAME
						+ " ("
						+ ReportingPeriodColumns._ID
						+ ", "
						+ ReportingPeriodColumns.PROJECT_ID
						+ ", "
						+ ReportingPeriodColumns.REPORT_PERIOD_CLOSE_DATE
						+ ", "
						+ ReportingPeriodColumns.PERIOD_DESCRIPTION
						+ ", "
						+ ReportingPeriodColumns.PERIOD_ACTIVE
						+ ", "
						+ ReportingPeriodColumns.PERIOD_STATUS
						+ ", "
						+ ReportingPeriodColumns.DISPLAY_SUBTEXT
						+ ", "
						+ ReportingPeriodColumns.RESEARCH_START_DATE
						+ ", "
						+ ReportingPeriodColumns.RESEARCH_END_DATE
						+ ", "
						+ ReportingPeriodColumns.DATE
						+ ", "
						+ ReportingPeriodColumns.STORY_INPUT_DEADLINE  + ") SELECT "
						+ ReportingPeriodColumns._ID
						+ ", "
						+ ReportingPeriodColumns.PROJECT_ID
						+ ", "
						+ ReportingPeriodColumns.REPORT_PERIOD_CLOSE_DATE
						+ ", "
						+ ReportingPeriodColumns.PERIOD_DESCRIPTION
						+ ", "
						+ ReportingPeriodColumns.PERIOD_ACTIVE
						+ ", "
						+ ReportingPeriodColumns.PERIOD_STATUS
						+ ", "
						+ ReportingPeriodColumns.DISPLAY_SUBTEXT
						+ ", "
						+ ReportingPeriodColumns.RESEARCH_START_DATE
						+ ", "
						+ ReportingPeriodColumns.RESEARCH_END_DATE
						+ ", "
						+ ReportingPeriodColumns.DATE
						+ ", "
						+ ReportingPeriodColumns.STORY_INPUT_DEADLINE+ " FROM "
						+ TEMP_REPORTING_PERIODS_TABLE_NAME);
				db.execSQL("DROP TABLE IF EXISTS " + TEMP_REPORTING_PERIODS_TABLE_NAME);

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
	public Cursor query(Uri uri, String[] reportingperiodion, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(REPORTING_PERIODS_TABLE_NAME);

		switch (sUriMatcher.match(uri)) {
		case REPORTING_PERIODS:
			qb.setProjectionMap(sReportingPeriodsReportingPeriodionMap);
			break;

		case REPORTING_PERIOD_ID:
			qb.setProjectionMap(sReportingPeriodsReportingPeriodionMap);
			qb.appendWhere(ReportingPeriodColumns._ID + "="
					+ uri.getPathSegments().get(1));
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// Get the database and run the query
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor c = qb.query(db, reportingperiodion, selection, selectionArgs, null,
				null, sortOrder);

		// Tell the cursor what uri to watch, so it knows when its source data
		// changes
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case REPORTING_PERIODS:
			return ReportingPeriodColumns.CONTENT_TYPE;

		case REPORTING_PERIOD_ID:
			return ReportingPeriodColumns.CONTENT_ITEM_TYPE;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public synchronized Uri insert(Uri uri, ContentValues initialValues) {
		// Validate the requested uri
		if (sUriMatcher.match(uri) != REPORTING_PERIODS) {
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
		if (values.containsKey(ReportingPeriodColumns.DATE) == false) {
			values.put(ReportingPeriodColumns.DATE, now);
		}

		if (values.containsKey(ReportingPeriodColumns.DISPLAY_SUBTEXT) == false) {
			Date today = new Date();
			String ts = new SimpleDateFormat(getContext().getString(
					R.string.added_on_date_at_time), Locale.getDefault())
					.format(today);
			values.put(ReportingPeriodColumns.DISPLAY_SUBTEXT, ts);
		}

		if (values.containsKey(ReportingPeriodColumns.PROJECT_ID) == false) {
			values.put(ReportingPeriodColumns.PROJECT_ID, values.getAsString(ReportingPeriodColumns.PROJECT_ID));
		}
		if (values.containsKey(ReportingPeriodColumns.RESEARCH_START_DATE) == false) {
			values.put(ReportingPeriodColumns.RESEARCH_START_DATE, values.getAsString(ReportingPeriodColumns.RESEARCH_START_DATE));
		}
		if (values.containsKey(ReportingPeriodColumns.RESEARCH_END_DATE) == false) {
			values.put(ReportingPeriodColumns.RESEARCH_END_DATE, values.getAsString(ReportingPeriodColumns.RESEARCH_END_DATE));
		}
		if (values.containsKey(ReportingPeriodColumns.STORY_INPUT_DEADLINE) == false) {
			values.put(ReportingPeriodColumns.STORY_INPUT_DEADLINE, values.getAsString(ReportingPeriodColumns.STORY_INPUT_DEADLINE));
		}
		if (values.containsKey(ReportingPeriodColumns.REPORT_PERIOD_CLOSE_DATE) == false) {
			values.put(ReportingPeriodColumns.REPORT_PERIOD_CLOSE_DATE, values.getAsString(ReportingPeriodColumns.REPORT_PERIOD_CLOSE_DATE));
		}
		if (values.containsKey(ReportingPeriodColumns.PERIOD_DESCRIPTION) == false) {
			values.put(ReportingPeriodColumns.PERIOD_DESCRIPTION, values.getAsString(ReportingPeriodColumns.PERIOD_DESCRIPTION));
		}
		if (values.containsKey(ReportingPeriodColumns.PERIOD_ACTIVE) == false) {
			values.put(ReportingPeriodColumns.PERIOD_ACTIVE, values.getAsString(ReportingPeriodColumns.PERIOD_ACTIVE));
		}
		if (values.containsKey(ReportingPeriodColumns.PERIOD_STATUS) == false) {
			values.put(ReportingPeriodColumns.PERIOD_STATUS, values.getAsString(ReportingPeriodColumns.PERIOD_STATUS));
		}
		
		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		// first try to see if a record with this id already exists...
		String[] reportingperiodion = { ReportingPeriodColumns.PROJECT_ID, ReportingPeriodColumns.PROJECT_ID };
		String[] selectionArgs = { values.getAsString(ReportingPeriodColumns.PROJECT_ID) };
		String selection = ReportingPeriodColumns.PROJECT_ID + "=?";
		Cursor c = null;
		try {
			c = db.query(REPORTING_PERIODS_TABLE_NAME, reportingperiodion, selection,
					selectionArgs, null, null, null);
			if (c.getCount() > 0) {
				// already exists
				throw new SQLException("FAILED Insert into " + uri
						+ " -- row already exists for reportingperiod definition: "
						+ values.getAsString(ReportingPeriodColumns.PROJECT_ID));
			}
		} finally {
			if (c != null) {
				c.close();
			}
		}

		long rowId = db.insert(REPORTING_PERIODS_TABLE_NAME, null, values);
		if (rowId > 0) {
			Uri reportingperiodUri = ContentUris.withAppendedId(ReportingPeriodColumns.CONTENT_URI,
					rowId);
			getContext().getContentResolver().notifyChange(reportingperiodUri, null);
			MandeUtility.Log(false,"insert"+ reportingperiodUri.toString()+
							values.getAsString(ReportingPeriodColumns.PROJECT_ID));
			return reportingperiodUri;
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
	 * any associated files. files: reportingperiod.xml, [reportingperiodmd5].reportingperioddef, reportingperiodname-media
	 * {directory}
	 */
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		int count;

		switch (sUriMatcher.match(uri)) {
		case REPORTING_PERIODS:
			Cursor del = null;
			try {
				del = this.query(uri, null, where, whereArgs, null);
				if (del.getCount() > 0) {
					del.moveToFirst();
					do {
						String reportingperiodFilePath = del.getString(del
								.getColumnIndex(ReportingPeriodColumns.PROJECT_ID));
						MandeUtility.Log(false, "delete"+reportingperiodFilePath);
						deleteFileOrDir(reportingperiodFilePath);
					} while (del.moveToNext());
				}
			} finally {
				if (del != null) {
					del.close();
				}
			}
			count = db.delete(REPORTING_PERIODS_TABLE_NAME, where, whereArgs);
			break;

		case REPORTING_PERIOD_ID:
			String reportingperiodId = uri.getPathSegments().get(1);

			Cursor c = null;
			try {
				c = this.query(uri, null, where, whereArgs, null);
				// This should only ever return 1 record.
				if (c.getCount() > 0) {
					c.moveToFirst();
					do {
						
						String reportingperiodFilePath = c.getString(c
								.getColumnIndex(ReportingPeriodColumns.PROJECT_ID));
						MandeUtility.Log(false,"delete:"+ reportingperiodFilePath);
						deleteFileOrDir(reportingperiodFilePath);
					
						
					
						 
					} while (c.moveToNext());
				}
			} finally {
				if (c != null) {
					c.close();
				}
			}

			count = db.delete(
					REPORTING_PERIODS_TABLE_NAME,
					ReportingPeriodColumns._ID
							+ "="
							+ reportingperiodId
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
		case REPORTING_PERIODS:
			

			Cursor c = null;
			try {
				c = this.query(uri, null, where, whereArgs, null);

				if (c.getCount() > 0) {
					c.moveToPosition(-1);
					while (c.moveToNext()) {
						// before updating the paths, delete all the files
						if (values.containsKey(ReportingPeriodColumns.PROJECT_ID)) {
							String newFile = values
									.getAsString(ReportingPeriodColumns.PROJECT_ID);
							String delFile = c
									.getString(c
											.getColumnIndex(ReportingPeriodColumns.PROJECT_ID));
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
			if (values.containsKey(ReportingPeriodColumns.DATE) == true) {
				Date today = new Date();
				String ts = new SimpleDateFormat(getContext().getString(
						R.string.added_on_date_at_time), Locale.getDefault())
						.format(today);
				values.put(ReportingPeriodColumns.DISPLAY_SUBTEXT, ts);
			}

			count = db.update(REPORTING_PERIODS_TABLE_NAME, values, where, whereArgs);
			break;

		case REPORTING_PERIOD_ID:
			String reportingperiodId = uri.getPathSegments().get(1);
			// Whenever file paths are updated, delete the old files.

			Cursor update = null;
			try {
				update = this.query(uri, null, where, whereArgs, null);

				// This should only ever return 1 record.
				if (update.getCount() > 0) {
					update.moveToFirst();

					// the order here is important (jrcache needs to be before
					// reportingperiod file)
					// because we update the jrcache file if there's a new reportingperiod
					// file
					

					if (values.containsKey(ReportingPeriodColumns.PROJECT_ID)) {
						String reportingperiodFile = values
								.getAsString(ReportingPeriodColumns.PROJECT_ID);
						String oldFile = update.getString(update
								.getColumnIndex(ReportingPeriodColumns.PROJECT_ID));

						if (reportingperiodFile != null
								&& reportingperiodFile.equalsIgnoreCase(oldFile)) {
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
					if (values.containsKey(ReportingPeriodColumns.DATE) == true) {
						Date today = new Date();
						String ts = new SimpleDateFormat(getContext()
								.getString(R.string.added_on_date_at_time),
								Locale.getDefault()).format(today);
						values.put(ReportingPeriodColumns.DISPLAY_SUBTEXT, ts);
					}

					count = db.update(
							REPORTING_PERIODS_TABLE_NAME,
							values,
							ReportingPeriodColumns._ID
									+ "="
									+ reportingperiodId
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
		sUriMatcher.addURI(ReportingPeriodProviderAPI.AUTHORITY, "reportingperiods", REPORTING_PERIODS);
		sUriMatcher.addURI(ReportingPeriodProviderAPI.AUTHORITY, "reportingperiods/#", REPORTING_PERIOD_ID);

		sReportingPeriodsReportingPeriodionMap = new HashMap<String, String>();
		sReportingPeriodsReportingPeriodionMap.put(ReportingPeriodColumns._ID, ReportingPeriodColumns._ID);
		sReportingPeriodsReportingPeriodionMap.put(ReportingPeriodColumns.PROJECT_ID,
				ReportingPeriodColumns.PROJECT_ID);
		sReportingPeriodsReportingPeriodionMap.put(ReportingPeriodColumns.RESEARCH_START_DATE,
				ReportingPeriodColumns.RESEARCH_START_DATE);
		sReportingPeriodsReportingPeriodionMap.put(ReportingPeriodColumns.RESEARCH_END_DATE,
				ReportingPeriodColumns.RESEARCH_END_DATE);
		sReportingPeriodsReportingPeriodionMap.put(ReportingPeriodColumns.STORY_INPUT_DEADLINE,
				ReportingPeriodColumns.STORY_INPUT_DEADLINE);
		sReportingPeriodsReportingPeriodionMap.put(ReportingPeriodColumns.REPORT_PERIOD_CLOSE_DATE,
				ReportingPeriodColumns.REPORT_PERIOD_CLOSE_DATE);
		sReportingPeriodsReportingPeriodionMap.put(ReportingPeriodColumns.PERIOD_DESCRIPTION,
				ReportingPeriodColumns.PERIOD_DESCRIPTION);
		sReportingPeriodsReportingPeriodionMap.put(ReportingPeriodColumns.PERIOD_ACTIVE,
				ReportingPeriodColumns.PERIOD_ACTIVE);
		sReportingPeriodsReportingPeriodionMap.put(ReportingPeriodColumns.PERIOD_STATUS,
				ReportingPeriodColumns.PERIOD_STATUS);
		
	}

}
