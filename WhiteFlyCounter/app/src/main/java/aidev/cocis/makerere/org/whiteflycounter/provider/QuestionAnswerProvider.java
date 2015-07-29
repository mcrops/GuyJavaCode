/*
 * Copyright (C) 2007 The Android Open Source QuestionAnswer
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import aidev.cocis.makerere.org.whiteflycounter.R;
import aidev.cocis.makerere.org.whiteflycounter.common.CommonFunctions;
import aidev.cocis.makerere.org.whiteflycounter.common.Constants;
import aidev.cocis.makerere.org.whiteflycounter.common.MandeUtility;
import aidev.cocis.makerere.org.whiteflycounter.database.WhiteFlyCounterSQLiteOpenHelper;
import aidev.cocis.makerere.org.whiteflycounter.provider.QuestionAnswerProviderAPI.QuestionAnswerColumns;

/**
 *
 */
public class QuestionAnswerProvider extends ContentProvider {

	private static final String t = "QuestionAnswerProvider";

	public static final String DATABASE_NAME = "hquestionanswer.db";
	private static final int DATABASE_VERSION = 1;
	private static final String QUESTIONS_TABLE_NAME = "hquestionanswer";

	private static HashMap<String, String> sQuestionAnswersQuestionAnswerionMap;

	private static final int QUESTIONS = 1;
	private static final int QUESTION_ID = 2;

	private static final UriMatcher sUriMatcher;

	/**
	 * This class helps open, create, and upgrade the database file.
	 */
	private static class DatabaseHelper extends WhiteFlyCounterSQLiteOpenHelper {
	
		private static final String TEMP_QUESTIONS_TABLE_NAME = "questionanswers_v1";

		DatabaseHelper(String databaseName) {
			super(Constants.METADATA_PATH, databaseName, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			onCreateNamed(db, QUESTIONS_TABLE_NAME);
		}

		private void onCreateNamed(SQLiteDatabase db, String tableName) {
			db.execSQL("CREATE TABLE " + tableName + " (" + QuestionAnswerColumns._ID
					+ " integer primary key, " + QuestionAnswerColumns.PROJECT_ID
					+ " integer  null, " + QuestionAnswerColumns.QUESTION_ID
					+ " integer  null, " + QuestionAnswerColumns.STORY_ID
					+ " integer  null, " + QuestionAnswerColumns.QUESTION
					+ " text null, " + QuestionAnswerColumns.QUESTION_ANSWER
					+ " text null, " + QuestionAnswerColumns.STATUS
					+ " integer null, " + QuestionAnswerColumns.DISPLAY_SUBTEXT
					+ " text null, "
					+ QuestionAnswerColumns.DATE
					+ " integer not null );");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			int initialVersion = oldVersion;
			if (oldVersion < 2) {
				Log.w(t, "Upgrading database from version " + oldVersion
						+ " to " + newVersion
						+ ", which will destroy all old data");
				db.execSQL("DROP TABLE IF EXISTS " + QUESTIONS_TABLE_NAME);
				onCreate(db);
				return;
			} else {
				// adding BASE64_RSA_PUBLIC_KEY and changing type and name of
				// integer MODEL_VERSION to text VERSION
				db.execSQL("DROP TABLE IF EXISTS " + TEMP_QUESTIONS_TABLE_NAME);
				onCreateNamed(db, TEMP_QUESTIONS_TABLE_NAME);
				db.execSQL("INSERT INTO "
						+ TEMP_QUESTIONS_TABLE_NAME
						+ " ("
						+ QuestionAnswerColumns._ID
						+ ", "
						+ QuestionAnswerColumns.PROJECT_ID
						+ ", "
						+ QuestionAnswerColumns.QUESTION_ID
						+ ", "
						+ QuestionAnswerColumns.STORY_ID
						+ ", "
						+ QuestionAnswerColumns.STATUS
						+ ", "
						+ QuestionAnswerColumns.QUESTION
						+ ", "
						+ QuestionAnswerColumns.QUESTION_ANSWER
						+ ", "
						+ QuestionAnswerColumns.DISPLAY_SUBTEXT
						+ ", "
						+ QuestionAnswerColumns.DATE
						+ ") SELECT "
						+ QuestionAnswerColumns._ID
						+ ", "
						+ QuestionAnswerColumns.PROJECT_ID
						+ ", "
						+ QuestionAnswerColumns.QUESTION_ID
						+ ", "
						+ QuestionAnswerColumns.STORY_ID
						+ ", "
						+ QuestionAnswerColumns.STATUS
						+ ", "
						+ QuestionAnswerColumns.QUESTION
						+ ", "						
						+ QuestionAnswerColumns.QUESTION_ANSWER
						+ ", "
						+ QuestionAnswerColumns.DISPLAY_SUBTEXT
						+ ", "
						+ QuestionAnswerColumns.DATE
						+ " FROM "
						+ QUESTIONS_TABLE_NAME);

				// risky failures here...
				db.execSQL("DROP TABLE IF EXISTS " + QUESTIONS_TABLE_NAME);
				onCreateNamed(db, QUESTIONS_TABLE_NAME);
				db.execSQL("INSERT INTO "
						+ QUESTIONS_TABLE_NAME
						+ " ("
						+ QuestionAnswerColumns._ID
						+ ", "
						+ QuestionAnswerColumns.PROJECT_ID
						+ ", "
						+ QuestionAnswerColumns.QUESTION_ID
						+ ", "
						+ QuestionAnswerColumns.STORY_ID
						+ ", "
						+ QuestionAnswerColumns.STATUS
						+ ", "
						+ QuestionAnswerColumns.QUESTION
						+ ", "						
						+ QuestionAnswerColumns.QUESTION_ANSWER
						+ ", "
						+ QuestionAnswerColumns.DISPLAY_SUBTEXT
						+ ", "
						+ QuestionAnswerColumns.DATE  + ") SELECT "
						+ QuestionAnswerColumns._ID
						+ ", "
						+ QuestionAnswerColumns.PROJECT_ID
						+ ", "
						+ QuestionAnswerColumns.QUESTION_ID
						+ ", "
						+ QuestionAnswerColumns.STORY_ID
						+ ", "
						+ QuestionAnswerColumns.STATUS
						+ ", "
						+ QuestionAnswerColumns.QUESTION
						+ ", "						
						+ QuestionAnswerColumns.QUESTION_ANSWER
						+ ", "
						+ QuestionAnswerColumns.DISPLAY_SUBTEXT
						+ ", "
						+ QuestionAnswerColumns.DATE+ " FROM "
						+ TEMP_QUESTIONS_TABLE_NAME);
				db.execSQL("DROP TABLE IF EXISTS " + TEMP_QUESTIONS_TABLE_NAME);

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
	public Cursor query(Uri uri, String[] questionanswerion, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(QUESTIONS_TABLE_NAME);

		switch (sUriMatcher.match(uri)) {
		case QUESTIONS:
			qb.setProjectionMap(sQuestionAnswersQuestionAnswerionMap);
			break;

		case QUESTION_ID:
			qb.setProjectionMap(sQuestionAnswersQuestionAnswerionMap);
			qb.appendWhere(QuestionAnswerColumns._ID + "="
					+ uri.getPathSegments().get(1));
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// Get the database and run the query
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor c = qb.query(db, questionanswerion, selection, selectionArgs, null,
				null, sortOrder);

		// Tell the cursor what uri to watch, so it knows when its source data
		// changes
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case QUESTIONS:
			return QuestionAnswerColumns.CONTENT_TYPE;

		case QUESTION_ID:
			return QuestionAnswerColumns.CONTENT_ITEM_TYPE;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public synchronized Uri insert(Uri uri, ContentValues initialValues) {
		// Validate the requested uri
		if (sUriMatcher.match(uri) != QUESTIONS) {
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
		if (values.containsKey(QuestionAnswerColumns.DATE) == false) {
			values.put(QuestionAnswerColumns.DATE, now);
		}

		if (values.containsKey(QuestionAnswerColumns.DISPLAY_SUBTEXT) == false) {
			Date today = new Date();
			String ts = new SimpleDateFormat(getContext().getString(
					R.string.added_on_date_at_time), Locale.getDefault())
					.format(today);
			values.put(QuestionAnswerColumns.DISPLAY_SUBTEXT, ts);
		}

		if (values.containsKey(QuestionAnswerColumns.PROJECT_ID) == false) {
			values.put(QuestionAnswerColumns.PROJECT_ID, values.getAsString(QuestionAnswerColumns.PROJECT_ID));
		}
		if (values.containsKey(QuestionAnswerColumns.QUESTION_ID) == false) {
			values.put(QuestionAnswerColumns.QUESTION_ID, values.getAsString(QuestionAnswerColumns.QUESTION_ID));
		}
		if (values.containsKey(QuestionAnswerColumns.QUESTION) == false) {
			values.put(QuestionAnswerColumns.QUESTION, values.getAsString(QuestionAnswerColumns.QUESTION));
		}
		if (values.containsKey(QuestionAnswerColumns.QUESTION_ANSWER) == false) {
			values.put(QuestionAnswerColumns.QUESTION_ANSWER, values.getAsString(QuestionAnswerColumns.QUESTION_ANSWER));
		}
		if (values.containsKey(QuestionAnswerColumns.STORY_ID) == false) {
			values.put(QuestionAnswerColumns.STORY_ID, values.getAsString(QuestionAnswerColumns.STORY_ID));
		}
		if (values.containsKey(QuestionAnswerColumns.STATUS) == false) {
			values.put(QuestionAnswerColumns.STATUS, values.getAsString(QuestionAnswerColumns.STATUS));
		}
		
		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		// first try to see if a record with this id already exists...
		String[] questionanswerion = { QuestionAnswerColumns.QUESTION_ID, QuestionAnswerColumns.PROJECT_ID,QuestionAnswerColumns.STATUS };
		String[] selectionArgs = { values.getAsString(QuestionAnswerColumns.QUESTION_ID),values.getAsString(QuestionAnswerColumns.PROJECT_ID),values.getAsString(QuestionAnswerColumns.STATUS) };
		String selection = QuestionAnswerColumns.QUESTION_ID + "=? AND "+QuestionAnswerColumns.PROJECT_ID+"=? AND "+QuestionAnswerColumns.STATUS+"=?";
		Cursor c = null;
		try {
			c = db.query(QUESTIONS_TABLE_NAME, questionanswerion, selection,
					selectionArgs, null, null, null);
			if (c.getCount() > 0) {
				// already exists
				throw new SQLException("FAILED Insert into " + uri
						+ " -- row already exists for questionanswer definition: "
						+ values.getAsString(QuestionAnswerColumns.QUESTION_ID));
			}
		} finally {
			if (c != null) {
				c.close();
			}
		}

		long rowId = db.insert(QUESTIONS_TABLE_NAME, null, values);
		if (rowId > 0) {
			Uri questionanswerUri = ContentUris.withAppendedId(QuestionAnswerColumns.CONTENT_URI,
					rowId);
			getContext().getContentResolver().notifyChange(questionanswerUri, null);
			MandeUtility.Log(false,"insert"+ questionanswerUri.toString()+
							values.getAsString(QuestionAnswerColumns.QUESTION_ID));
			return questionanswerUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}



	/**
	 * This method removes the entry from the content provider, and also removes
	 * any associated files. files: questionanswer.xml, [questionanswermd5].questionanswerdef, questionanswername-media
	 * {directory}
	 */
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		int count;

		switch (sUriMatcher.match(uri)) {
		case QUESTIONS:
			Cursor del = null;
			try {
				del = this.query(uri, null, where, whereArgs, null);
				if (del.getCount() > 0) {
					del.moveToFirst();
					do {
						String questionanswerFilePath = del.getString(del
								.getColumnIndex(QuestionAnswerColumns.PROJECT_ID));
						MandeUtility.Log(false, "delete"+questionanswerFilePath);
					} while (del.moveToNext());
				}
			} finally {
				if (del != null) {
					del.close();
				}
			}
			count = db.delete(QUESTIONS_TABLE_NAME, where, whereArgs);
			break;

		case QUESTION_ID:
			String questionanswerId = uri.getPathSegments().get(1);

			Cursor c = null;
			try {
				c = this.query(uri, null, where, whereArgs, null);
				// This should only ever return 1 record.
				if (c.getCount() > 0) {
					c.moveToFirst();
					do {
						
						String questionanswerFilePath = c.getString(c
								.getColumnIndex(QuestionAnswerColumns._ID));
						MandeUtility.Log(false,"delete:"+ questionanswerFilePath);
						
					
						 
					} while (c.moveToNext());
				}
			} finally {
				if (c != null) {
					c.close();
				}
			}

			count = db.delete(
					QUESTIONS_TABLE_NAME,
					QuestionAnswerColumns._ID
							+ "="
							+ questionanswerId
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
		case QUESTIONS:
			

			Cursor c = null;
			try {
				c = this.query(uri, null, where, whereArgs, null);

				if (c.getCount() > 0) {
					c.moveToPosition(-1);
					while (c.moveToNext()) {
						// before updating the paths, delete all the files
						if (values.containsKey(QuestionAnswerColumns._ID)) {
							String newFile = values
									.getAsString(QuestionAnswerColumns._ID);
							String delFile = c
									.getString(c
											.getColumnIndex(QuestionAnswerColumns._ID));
							if (newFile.equalsIgnoreCase(delFile)) {
								// same file, so don't delete anything
							} else {
								// different files, delete the old one
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
			if (values.containsKey(QuestionAnswerColumns.DATE) == true) {
				Date today = new Date();
				String ts = new SimpleDateFormat(getContext().getString(
						R.string.added_on_date_at_time), Locale.getDefault())
						.format(today);
				values.put(QuestionAnswerColumns.DISPLAY_SUBTEXT, ts);
			}

			count = db.update(QUESTIONS_TABLE_NAME, values, where, whereArgs);
			break;

		case QUESTION_ID:
			String questionanswerId = uri.getPathSegments().get(1);
			// Whenever file paths are updated, delete the old files.

			Cursor update = null;
			try {
				update = this.query(uri, null, where, whereArgs, null);

				// This should only ever return 1 record.
				if (update.getCount() > 0) {
					update.moveToFirst();

					// the order here is important (jrcache needs to be before
					// questionanswer file)
					// because we update the jrcache file if there's a new questionanswer
					// file
					

					if (values.containsKey(QuestionAnswerColumns._ID)) {
						String questionanswerFile = values
								.getAsString(QuestionAnswerColumns._ID);
						String oldFile = update.getString(update
								.getColumnIndex(QuestionAnswerColumns._ID));

						if (questionanswerFile != null
								&& questionanswerFile.equalsIgnoreCase(oldFile)) {
							// Files are the same, so we may have just copied
							// over something we had
							// already
						} else {
							// New file name. This probably won't ever happen,
							// though.
						}

						
					}

					// Make sure that the necessary fields are all set
					if (values.containsKey(QuestionAnswerColumns.DATE) == true) {
						Date today = new Date();
						String ts = new SimpleDateFormat(getContext()
								.getString(R.string.added_on_date_at_time),
								Locale.getDefault()).format(today);
						values.put(QuestionAnswerColumns.DISPLAY_SUBTEXT, ts);
					}

					count = db.update(
							QUESTIONS_TABLE_NAME,
							values,
							QuestionAnswerColumns._ID
									+ "="
									+ questionanswerId
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
		sUriMatcher.addURI(QuestionAnswerProviderAPI.AUTHORITY, "questionanswers", QUESTIONS);
		sUriMatcher.addURI(QuestionAnswerProviderAPI.AUTHORITY, "questionanswers/#", QUESTION_ID);

		sQuestionAnswersQuestionAnswerionMap = new HashMap<String, String>();
		sQuestionAnswersQuestionAnswerionMap.put(QuestionAnswerColumns._ID, QuestionAnswerColumns._ID);
		sQuestionAnswersQuestionAnswerionMap.put(QuestionAnswerColumns.PROJECT_ID,
				QuestionAnswerColumns.PROJECT_ID);
		sQuestionAnswersQuestionAnswerionMap.put(QuestionAnswerColumns.QUESTION_ID,
				QuestionAnswerColumns.QUESTION_ID);
		sQuestionAnswersQuestionAnswerionMap.put(QuestionAnswerColumns.QUESTION,
				QuestionAnswerColumns.QUESTION);
		sQuestionAnswersQuestionAnswerionMap.put(QuestionAnswerColumns.QUESTION_ANSWER,
				QuestionAnswerColumns.QUESTION_ANSWER);
		sQuestionAnswersQuestionAnswerionMap.put(QuestionAnswerColumns.STORY_ID,
				QuestionAnswerColumns.STORY_ID);
		sQuestionAnswersQuestionAnswerionMap.put(QuestionAnswerColumns.STATUS,
				QuestionAnswerColumns.STATUS);
	}

}
