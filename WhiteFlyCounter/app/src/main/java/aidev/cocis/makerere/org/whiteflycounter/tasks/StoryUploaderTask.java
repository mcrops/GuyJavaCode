/*
 * Copyright (C) 2009 University of Washington
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

package aidev.cocis.makerere.org.whiteflycounter.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import java.io.File;
import java.util.HashMap;

import aidev.cocis.makerere.org.whiteflycounter.R;
import aidev.cocis.makerere.org.whiteflycounter.common.Constants;
import aidev.cocis.makerere.org.whiteflycounter.common.FileUtils;
import aidev.cocis.makerere.org.whiteflycounter.common.MandeUtility;
import aidev.cocis.makerere.org.whiteflycounter.listeners.StoryUploaderListener;
import aidev.cocis.makerere.org.whiteflycounter.provider.QuestionAnswerProviderAPI.QuestionAnswerColumns;
import aidev.cocis.makerere.org.whiteflycounter.provider.StoryProviderAPI;
import aidev.cocis.makerere.org.whiteflycounter.provider.StoryProviderAPI.StoryColumns;
import aidev.cocis.makerere.org.whiteflycounter.webservices.IHTTPEvents;
import aidev.cocis.makerere.org.whiteflycounter.webservices.field.StoryService;
import aidev.cocis.makerere.org.whiteflycounter.webservices.field.WSQuestionAnswer;
import aidev.cocis.makerere.org.whiteflycounter.webservices.field.WSStory;

/**
 * Background task for uploading completed stories.
 * 
 * @author Acellam Guy
 */
public class StoryUploaderTask extends
		AsyncTask<Long, Integer, StoryUploaderTask.Outcome> implements
		IHTTPEvents {

	private static final String t = "UploaderTask";
	// it can take up to 27 seconds to spin up Aggregate
	private static final int CONNECTION_TIMEOUT = 60000;
	private static final String fail = "Error: ";

	private StoryUploaderListener mStateListener;

	public SharedPreferences preferences;
	public Context ctx;

	public StoryService storyService;

	String storyid;
	Outcome outcome;

	public static class Outcome {
		public Uri mAuthRequestingServer = null;
		public HashMap<String, String> mResults = new HashMap<String, String>();
	}

	/**
	 * Uploads to urlString the submission identified by id with story id
	 *
	 *            - Instance URL for recording status update.
	 * @return false if credentials are required and we should terminate
	 *         immediately.
	 */
	/*private boolean uploadOneSubmission(String id, int FieldID,
			String StoryTitle, String StoryFullText, String StorySummary,
			SoapObject QuestionAnswerList) {

		try {
			storyService.uploadStoryAsync(null, FieldID, StoryTitle,
					StoryFullText, StorySummary, QuestionAnswerList);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}*/

	// TODO: This method is like 350 lines long, down from 400.
	// still. ridiculous. make it smaller.
	protected Outcome doInBackground(Long... values) {

		storyService = new StoryService(this, "SaveStory",
				MandeUtility.getEmail(preferences),
				MandeUtility.getPassword(preferences));

		outcome = new Outcome();

		String selection = StoryColumns._ID + "=?";
		String[] selectionArgs = new String[(values == null) ? 0
				: values.length];
		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				if (i != values.length - 1) {
					selection += " or " + StoryColumns._ID + "=?";
				}
				selectionArgs[i] = values[i].toString();
			}
		}

		Cursor c = null;
		try {
			c = ctx.getContentResolver().query(StoryColumns.CONTENT_URI, null,
					selection, selectionArgs, null);

			if (c.getCount() > 0) {
				c.moveToPosition(-1);
				while (c.moveToNext()) {
					if (isCancelled()) {
						return outcome;
					}
					publishProgress(c.getPosition() + 1, c.getCount());
					String title = c.getString(c
							.getColumnIndex(StoryColumns.TITLE));
					storyid = c.getString(c.getColumnIndex(StoryColumns._ID));
					int fieldID = c.getInt(c
							.getColumnIndex(StoryColumns.PROJECT_ID));
					String fulltext = "";
					String summary = c.getString(c
							.getColumnIndex(StoryColumns.STORY_FULLTEXT));

					//SoapObject questionlist = new SoapObject();

					String sortOrder = QuestionAnswerColumns.QUESTION_ID
							+ " ASC ";
					Cursor cursor = ctx.getContentResolver().query(
							QuestionAnswerColumns.CONTENT_URI, null,
							QuestionAnswerColumns.STORY_ID + " =" + storyid,
							null, sortOrder);

					while (cursor.moveToNext()) {

						WSQuestionAnswer qa = new WSQuestionAnswer();

						qa.Question = cursor
								.getString(cursor
										.getColumnIndex(QuestionAnswerColumns.QUESTION));
						qa.Answer = cursor
								.getString(cursor
										.getColumnIndex(QuestionAnswerColumns.QUESTION_ANSWER));
						qa.NewQA = false;

						

						fulltext = fulltext + "<p><b>" + qa.Question
								+ "</b></p>";
						fulltext = fulltext + "<p>" + qa.Answer + "</p>";
						try {


							File dir = new File(
									Constants.STORIES_PATH
											+ File.separator
											+ String.valueOf(cursor.getInt(cursor
													.getColumnIndex(QuestionAnswerColumns.STORY_ID)))
											+ File.separator
											+ String.valueOf(cursor.getInt(cursor
													.getColumnIndex(QuestionAnswerColumns._ID))));

							File[] filelist = dir.listFiles();
							if(filelist!=null){

								//SoapObject qadoc = new SoapObject();
								
								String[] theNamesOfFiles = new String[filelist.length];
								for (int i = 0; i < theNamesOfFiles.length; i++) {
									theNamesOfFiles[i] = filelist[i].getName();

									//attachmentList.put(filelist[i].getName()
									//		.toString(), );
									
									/*AssetManager assetManager = ctx.getAssets();

									ByteArrayOutputStream out = new ByteArrayOutputStream();

									byte[] raw = out.toByteArray();

									String s = Base64.encode(raw);

									InputStream in = null;
									ByteArrayOutputStream out1 = new ByteArrayOutputStream();
									in = assetManager.open(filelist[i].toString());
									byte[] buffer = new byte[1024];
									int read;
									while ((read = in.read(buffer)) != -1) {
										out1.write(buffer, 0, read);
									}
									in.close();
									out1.close();*/
									File f = new File(filelist[i].toString());
									
									byte[] raw = FileUtils.getFileAsBytes(f);									
									/*String s = Base64.encode(raw);
									SoapObject scmp3 = new SoapObject();
									scmp3.addProperty("Name", filelist[i].getName());
									scmp3.addProperty("Contents", s);
									qadoc.addProperty("QuestionFileDocument", scmp3);*/

								}
								//qa.QuestionFileDocument_QuestionAnswer = qadoc;
								
								//questionlist.addProperty("QuestionAnswer", qa);
								
							}else{
								//questionlist.addProperty("QuestionAnswer", qa);
							}

							

						} catch (Exception ex) {
							MandeUtility.Log(true, ex.getMessage());
						}

					}

					/*if (!uploadOneSubmission(storyid, fieldID, title,
							fulltext, summary, questionlist)) {
						return outcome; // get credentials...
					}*/
				}
			}
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return outcome;
	}

	@Override
	protected void onPostExecute(Outcome outcome) {
		synchronized (this) {
			if (mStateListener != null) {

			}
		}
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		synchronized (this) {
			if (mStateListener != null) {
				// update progress and total
				mStateListener.progressUpdate(values[0].intValue(),
						values[1].intValue());
			}
		}
	}

	public void setUploaderListener(StoryUploaderListener sl) {
		synchronized (this) {
			mStateListener = sl;
		}
	}

	@Override
	public void HTTPStartedRequest() {
		MandeUtility.Log(false, "HTTPStartedRequest");

	}

	@Override
	public void HTTPFinished(String methodName, Object Data) {
		synchronized (this) {
			if (methodName.equals("SaveStory")) {

				WSStory story = (WSStory) Data;

				if (story != null) {
					if (mStateListener != null) {

						if (outcome.mAuthRequestingServer != null) {
							mStateListener.authRequest(
									outcome.mAuthRequestingServer,
									outcome.mResults);
						} else {

							ContentValues cv = new ContentValues();

							// if it got here, it must have worked
							cv.put(StoryColumns.STATUS,
									StoryProviderAPI.STATUS_SUBMITTED);

							Uri toUpdate = Uri.withAppendedPath(
									StoryColumns.CONTENT_URI, storyid);
							outcome.mResults.put(storyid,
									ctx.getString(R.string.success));
							ctx.getContentResolver().update(toUpdate, cv, null,
									null);

							mStateListener.uploadingComplete(outcome.mResults);
						}

					}
				}
			}
			MandeUtility.Log(false, "HTTPFinished");
			MandeUtility.Log(false, methodName);
		}
	}

	@Override
	public void HTTPFinishedWithException(Exception ex) {
		MandeUtility.Log(true, "HTTPFinishedWithException");

	}

	@Override
	public void HTTPEndedRequest() {
		MandeUtility.Log(true, "sWsdl2CodeEndedRequest");
	}
}
