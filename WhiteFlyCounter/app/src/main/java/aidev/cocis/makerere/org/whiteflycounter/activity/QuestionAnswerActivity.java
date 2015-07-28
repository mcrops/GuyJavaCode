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

package aidev.cocis.makerere.org.whiteflycounter.activity;

import java.io.File;
import java.io.IOException;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import aidev.cocis.makerere.org.whiteflycounter.R;
import aidev.cocis.makerere.org.whiteflycounter.common.CommonFunctions;
import aidev.cocis.makerere.org.whiteflycounter.common.CompatibilityUtils;
import aidev.cocis.makerere.org.whiteflycounter.common.Constants;
import aidev.cocis.makerere.org.whiteflycounter.common.MandeUtility;
import aidev.cocis.makerere.org.whiteflycounter.logic.QuestionController;
import aidev.cocis.makerere.org.whiteflycounter.preferences.AdminPreferencesActivity;
import aidev.cocis.makerere.org.whiteflycounter.preferences.PreferencesActivity;
import aidev.cocis.makerere.org.whiteflycounter.provider.FieldsProviderAPI.FieldsColumns;
import aidev.cocis.makerere.org.whiteflycounter.provider.QuestionAnswerProviderAPI.QuestionAnswerColumns;
import aidev.cocis.makerere.org.whiteflycounter.provider.QuestionProviderAPI.QuestionColumns;
import aidev.cocis.makerere.org.whiteflycounter.provider.StoryProviderAPI;
import aidev.cocis.makerere.org.whiteflycounter.provider.StoryProviderAPI.StoryColumns;
import aidev.cocis.makerere.org.whiteflycounter.webservices.field.VectorWSQuestion;
import aidev.cocis.makerere.org.whiteflycounter.webservices.field.WSField;
import aidev.cocis.makerere.org.whiteflycounter.webservices.field.WSQuestion;

/**
 * FormEntryActivity is responsible for displaying questions, animating
 * transitions between questions, and allowing the user to enter data.
 *
 * @author Acellam Guy
 */
public class QuestionAnswerActivity extends Activity {
	private static final String t = "StoryEntryActivity";

	// save with every swipe forward or back. Timings indicate this takes .25
	// seconds.
	// if it ever becomes an issue, this value can be changed to save every n'th
	// screen.
	private static final int SAVEPOINT_INTERVAL = 1;

	// Defines for FormEntryActivity
	private static final boolean EXIT = true;
	private static final boolean DO_NOT_EXIT = false;
	private static final boolean EVALUATE_CONSTRAINTS = true;
	private static final boolean DO_NOT_EVALUATE_CONSTRAINTS = false;

	// Request codes for returning data from specified intent.
	public static final int IMAGE_CAPTURE = 1;
	public static final int BARCODE_CAPTURE = 2;
	public static final int AUDIO_CAPTURE = 3;
	public static final int VIDEO_CAPTURE = 4;
	public static final int LOCATION_CAPTURE = 5;
	public static final int HIERARCHY_ACTIVITY = 6;
	public static final int IMAGE_CHOOSER = 7;
	public static final int AUDIO_CHOOSER = 8;
	public static final int VIDEO_CHOOSER = 9;
	public static final int EX_STRING_CAPTURE = 10;
	public static final int EX_INT_CAPTURE = 11;
	public static final int EX_DECIMAL_CAPTURE = 12;
	public static final int DRAW_IMAGE = 13;
	public static final int SIGNATURE_CAPTURE = 14;
	public static final int ANNOTATE_IMAGE = 15;
	public static final int ALIGNED_IMAGE = 16;
	public static final int BEARING_CAPTURE = 17;

	// Extra returned from gp activity
	public static final String LOCATION_RESULT = "LOCATION_RESULT";
	public static final String BEARING_RESULT = "BEARING_RESULT";

	public static final String KEY_INSTANCES = "instances";
	public static final String KEY_SUCCESS = "success";
	public static final String KEY_ERROR = "error";

	// Identifies the gp of the form used to launch form entry
	public static final String KEY_FORMPATH = "formpath";

	// Identifies whether this is a new form, or reloading a form after a screen
	// rotation (or similar)
	private static final String NEWFORM = "newform";
	// these are only processed if we shut down and are restoring after an
	// external intent fires

	public static final String KEY_INSTANCEPATH = "instancepath";
	public static final String KEY_XPATH = "xpath";
	public static final String KEY_XPATH_WAITING_FOR_DATA = "xpathwaiting";

	private static final int MENU_LANGUAGES = Menu.FIRST;
	private static final int MENU_HIERARCHY_VIEW = Menu.FIRST + 1;
	private static final int MENU_SAVE = Menu.FIRST + 2;
	private static final int MENU_PREFERENCES = Menu.FIRST + 3;

	private static final int PROGRESS_DIALOG = 1;
	private static final int SAVING_DIALOG = 2;

	// Random ID
	private static final int DELETE_REPEAT = 654321;

	private String mFormPath;

	private Animation mInAnimation;
	private Animation mOutAnimation;
	private View mStaleView = null;

	private LinearLayout mQuestionHolder;
	private View mCurrentView;

	private AlertDialog mAlertDialog;
	private ProgressDialog mProgressDialog;
	private String mErrorMessage;

	// used to limit forward/backward swipes to one per question
	private boolean mBeenSwiped = false;

	private int viewCount = 0;

	int storyid=0;
	int questionanswerid=0;
	

	private ImageButton mNextButton;
	private ImageButton mBackButton;
	TextView txtQuestion;
	EditText editTextAnswer;
	
	
	QuestionController questionController;
	WSField field;
	int ID =0;
	enum AnimationType {
		LEFT, RIGHT, FADE
	}
	public SharedPreferences preferences;
	private SharedPreferences mAdminPreferences;
	
	private RecordButton mRecordButton = null;
    private MediaRecorder mRecorder = null;

    private PlayButton   mPlayButton = null;
    private MediaPlayer   mPlayer = null;
    
    private static final String LOG_TAG = "AudioRecord";
    private static String mFileName = null;
    
    boolean recordBtnPressed = false;
    boolean recordPlayBtnPressed = false;

    boolean mStartPlaying = true;

    boolean mStartRecording = true;

    
	private android.widget.LinearLayout.LayoutParams mLayout;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		field = new WSField();
		// must be at the beginning of any activity that can be called from an
		// external intent
		try {
			CommonFunctions.createWHITEFLYCOUNTERDirs();
		} catch (RuntimeException e) {
			return;
		}
		
		preferences = getSharedPreferences(MandeUtility.PREFS_NAME, 0);
		
		Intent intent = getIntent();
		if (intent != null) {
			Uri uri = intent.getData();

			if (getContentResolver().getType(uri) == FieldsColumns.CONTENT_ITEM_TYPE) {
				// get the field details

				{
					Cursor fieldCursor = null;
					try {
						fieldCursor = getContentResolver().query(uri, null,
								null, null, null);
						if (fieldCursor.getCount() != 1) {
							this.createErrorDialog("Bad URI: " + uri, EXIT);
							return;
						} else {
							fieldCursor.moveToFirst();

							field.FieldID = fieldCursor
									.getInt(fieldCursor
											.getColumnIndex(FieldsColumns.WHITEFLYCOUNTER_PROJECT_ID));
							field.Name = fieldCursor
									.getString(fieldCursor
											.getColumnIndex(FieldsColumns.NAME));

							field.Description = String
									.valueOf(fieldCursor.getString(fieldCursor
											.getColumnIndex(FieldsColumns.DESCRIPTION)));
							ID = fieldCursor
									.getInt(fieldCursor
											.getColumnIndex(FieldsColumns._ID));
						}
					} finally {
						if (fieldCursor != null) {
							fieldCursor.close();
						}
					}

				}

			} else {
				Log.e(t, "unrecognized URI");
				this.createErrorDialog("unrecognized URI: " + uri, EXIT);
				return;
			}

		}
		
		String sortOrder = QuestionColumns.QUESTION_ORDER + " ASC ";
		Cursor cursor = getApplicationContext()
				.getContentResolver().query(QuestionColumns.CONTENT_URI, null,  QuestionColumns.PROJECT_ID + " ="+field.FieldID, null,
				sortOrder);
		
	
		
		VectorWSQuestion questions = new VectorWSQuestion();	
		int qns=0;
		while (cursor.moveToNext()) {
			
			WSQuestion question = new  WSQuestion();
			
		   question.Question =	cursor.getString(cursor
					.getColumnIndex(QuestionColumns.QUESTION));
		   question.QuestionOrder =	cursor.getInt(cursor
					.getColumnIndex(QuestionColumns.QUESTION_ORDER));
		   question.QuestionID =	cursor.getInt(cursor
					.getColumnIndex(QuestionColumns.QUESTION_ID));
		   question.FieldID =	cursor.getInt(cursor
					.getColumnIndex(QuestionColumns.PROJECT_ID));
		   questions.add(question);
		   
		}
		questionController = new QuestionController(questions);
		questionController.questionCount = cursor.getCount();

		cursor.close();
		
		setContentView(R.layout.form_entry);
		
		
		setTitle(getString(R.string.app_name) + " > "
				+ getString(R.string.adding_stories));

		txtQuestion  = (TextView) findViewById(R.id.txtquestionholder);
		editTextAnswer  = (EditText) findViewById(R.id.editTextAnswer);

		mBeenSwiped = false;
		mAlertDialog = null;
		mCurrentView = null;
		mInAnimation = null;
		mOutAnimation = null;
		mQuestionHolder = (LinearLayout) findViewById(R.id.questionholder);

		// get admin preference settings
		mAdminPreferences = getSharedPreferences(
				AdminPreferencesActivity.ADMIN_PREFERENCES, 0);

		mNextButton = (ImageButton) findViewById(R.id.form_forward_button);
		mNextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mBeenSwiped = true;
				showNextView();
				
				if(recordBtnPressed){
					stopRecording();
					mRecordButton.setText("Start recording");
					recordBtnPressed =false;					
					mStartRecording = true;
				}
				
				if(recordPlayBtnPressed)
				{
					mPlayButton.setText("Start playing");
					
					mStartPlaying =true;
					
					recordPlayBtnPressed = false;
				}
			}
		});

		mBackButton = (ImageButton) findViewById(R.id.form_back_button);
		mBackButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mBeenSwiped = true;
				showPreviousView();
				
				if(recordBtnPressed){
					stopRecording();
					mRecordButton.setText("Start recording");
					recordBtnPressed=false;
					mStartRecording = true;
				}
				
				if(recordPlayBtnPressed)
				{
					mPlayButton.setText("Start playing");
					mStartPlaying = true;
					recordPlayBtnPressed = false;
				}
			}
		});

		checkStory(field.FieldID);
		showNextView();
		
		 LinearLayout ll = (LinearLayout) findViewById(R.id.sound_buttons_listview);
	        mRecordButton = new RecordButton(this);
	        ll.addView(mRecordButton,
	            new LinearLayout.LayoutParams(
	                ViewGroup.LayoutParams.WRAP_CONTENT,
	                ViewGroup.LayoutParams.WRAP_CONTENT,
	                0));
	        mPlayButton = new PlayButton(this);
	        ll.addView(mPlayButton,
	            new LinearLayout.LayoutParams(
	                ViewGroup.LayoutParams.WRAP_CONTENT,
	                ViewGroup.LayoutParams.WRAP_CONTENT,
	                0));
	        
	}
	   @Override
	    public void onPause() {
	        super.onPause();
	        if (mRecorder != null) {
	            mRecorder.release();
	            mRecorder = null;
	        }

	        if (mPlayer != null) {
	            mPlayer.release();
	            mPlayer = null;
	        }
	    }
	 
	    
	private void createErrorDialog(String errorMsg, final boolean shouldExit) {
		MandeUtility.Log(false, "createErrorDialog:show");
		mAlertDialog = new AlertDialog.Builder(this).create();
		mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
		mAlertDialog.setMessage(errorMsg);
		DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int i) {
				switch (i) {
				case DialogInterface.BUTTON_POSITIVE:

					if (shouldExit) {
						finish();
					}
					break;
				}
			}
		};
		mAlertDialog.setCancelable(false);
		mAlertDialog.setButton(getString(R.string.ok), errorListener);
		mAlertDialog.show();
	}
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(KEY_FORMPATH, mFormPath);
		
		outState.putBoolean(NEWFORM, false);
		outState.putString(KEY_ERROR, mErrorMessage);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		

		if (resultCode == RESULT_CANCELED) {
			// request was canceled...
			if (requestCode != HIERARCHY_ACTIVITY) {
				
			}
			return;
		}

	
		refreshCurrentView();
	}

	/**
	 * Refreshes the current view. the controller and the displayed view can get
	 * out of sync due to dialogs and restarts caused by screen orientation
	 * changes, so they're resynchronized here.
	 */
	public void refreshCurrentView() {
	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MandeUtility.Log(false, "onCreateOptionsMenu:"+"show");
		super.onCreateOptionsMenu(menu);

		CompatibilityUtils.setShowAsAction(
				menu.add(0, MENU_SAVE, 0, R.string.save_all_answers).setIcon(
						R.drawable.attachment_add),
				MenuItem.SHOW_AS_ACTION_IF_ROOM);

		CompatibilityUtils.setShowAsAction(
				menu.add(0, MENU_HIERARCHY_VIEW, 0, R.string.view_hierarchy)
						.setIcon(R.drawable.ic_menu_goto),
				MenuItem.SHOW_AS_ACTION_IF_ROOM);

		CompatibilityUtils.setShowAsAction(
				menu.add(0, MENU_LANGUAGES, 0, R.string.change_language)
						.setIcon(R.drawable.ic_menu_start_conversation),
				MenuItem.SHOW_AS_ACTION_NEVER);

		CompatibilityUtils.setShowAsAction(
				menu.add(0, MENU_PREFERENCES, 0, R.string.general_preferences)
						.setIcon(R.drawable.ic_menu_preferences),
				MenuItem.SHOW_AS_ACTION_NEVER);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		boolean useability;
		useability = mAdminPreferences.getBoolean(
				AdminPreferencesActivity.KEY_SAVE_MID, true);

		menu.findItem(MENU_SAVE).setVisible(useability).setEnabled(useability);

		useability = mAdminPreferences.getBoolean(
				AdminPreferencesActivity.KEY_JUMP_TO, true);

		menu.findItem(MENU_HIERARCHY_VIEW).setVisible(useability)
				.setEnabled(useability);

		
		menu.findItem(MENU_LANGUAGES).setVisible(useability)
				.setEnabled(useability);

		useability = mAdminPreferences.getBoolean(
				AdminPreferencesActivity.KEY_ACCESS_SETTINGS, true);

		menu.findItem(MENU_PREFERENCES).setVisible(useability)
				.setEnabled(useability);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		case MENU_LANGUAGES:
			MandeUtility.Log(false, "onOptionsItemSelected"+
							"MENU_LANGUAGES");
			
			return true;
		case MENU_SAVE:
			MandeUtility.Log(false, "onOptionsItemSelected"+
							"MENU_SAVE");
			
			Uri questionAnswerUri = ContentUris.withAppendedId(
					QuestionAnswerColumns.CONTENT_URI, questionanswerid);
			startActivity(new Intent("aidev.cocis.makerere.org.whiteflycounter.addAttachment", questionAnswerUri));
			
			
			return true;
		case MENU_HIERARCHY_VIEW:
			MandeUtility.Log(false, "onOptionsItemSelected"+
							"MENU_HIERARCHY_VIEW");
			
			return true;
		case MENU_PREFERENCES:
			MandeUtility.Log(false, "onOptionsItemSelected"+
							"MENU_PREFERENCES");
			Intent pref = new Intent(this, PreferencesActivity.class);
			startActivity(pref);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Attempt to save the answer(s) in the current screen to into the data
	 * model.
	 *
	 * @return false if any error occurs while saving (constraint violated,
	 *         etc...), true otherwise.
	 */
	private boolean saveAnswersForCurrentScreen() {
		
		return true;
	}


	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		menu.add(0, v.getId(), 0, getString(R.string.clear_answer));
		
		menu.setHeaderTitle(getString(R.string.edit_prompt));
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		

		return super.onContextItemSelected(item);
	}

	/**
	 * If we're loading, then we pass the loading thread to our next instance.
	 */
	@Override
	public Object onRetainNonConfigurationInstance() {
		
		return null;
	}


	private int checkStory(int fieldID){
		
		
		 try {
				Cursor alreadyExists = null;
				Uri uri = null;
				try {
															
					alreadyExists = getApplicationContext()
 							.getContentResolver().query(
 									StoryColumns.CONTENT_URI,
 									null,  StoryColumns.STATUS + " = '"+StoryProviderAPI.STATUS_INCOMPLETE+"' AND "+StoryColumns.PROJECT_ID+" = "+fieldID, null,
 									null);
					

					if (alreadyExists.getCount() <= 0) {
						// doesn't exist, so insert it
						ContentValues v = new ContentValues();
						v.put(StoryColumns.TITLE,
								"");
						v.put(StoryColumns.PROJECT_ID,
								fieldID);
						
						v.put(StoryColumns.STORY_FULLTEXT,
								"");
						
						v.put(StoryColumns.PARTICIPANT_NAME, "");
						
						v.put(StoryColumns.PARTICIPANT_CONTACT, "");
						
						v.put(StoryColumns.STATUS, StoryProviderAPI.STATUS_INCOMPLETE);

						uri = getApplicationContext().getContentResolver()
								.insert(StoryColumns.CONTENT_URI, v);
						MandeUtility.Log(false, "init: Story");
						
						Cursor newstory = getApplicationContext()
								.getContentResolver().query(
										StoryColumns.CONTENT_URI,
										null,  StoryColumns.STATUS + " ='"+StoryProviderAPI.STATUS_INCOMPLETE+"' AND "+StoryColumns.PROJECT_ID+" = "+fieldID, null,
										null);
						newstory.moveToFirst();
						
						  storyid = newstory.getInt(newstory
										.getColumnIndex(StoryColumns._ID));
						  
						  newstory.close();

					} else {
						alreadyExists.moveToFirst();
						
					  storyid = alreadyExists.getInt(alreadyExists
									.getColumnIndex(StoryColumns._ID));
						
						
					}
				} finally {
					if (alreadyExists != null) {
						alreadyExists.close();
					}

				}

			} catch (Exception e) {
		         MandeUtility.Log(true, e.getCause().getMessage());
			}
		return storyid;
	}
	/**
	 * Determines what should be displayed on the screen. Possible options are:
	 * a question, an ask repeat dialog, or the submit screen. Also saves
	 * answers to the data model after checking constraints.
	 */
	private void showNextView() {
	
		
		try{

	        WSQuestion question = questionController.getNxtQuestion();	        
	        txtQuestion.setText(question.Question);
	        

				String answ =editTextAnswer.getText().toString();
			
			 try {
	 				Cursor alreadyExists = null;
	 				Uri uri = null;
	 				try {
	 										
	 					alreadyExists = getApplicationContext()
	 							.getContentResolver().query(
	 									QuestionAnswerColumns.CONTENT_URI,
	 									null,  QuestionAnswerColumns.QUESTION_ID + " ="+question.QuestionID+" AND "+QuestionAnswerColumns.PROJECT_ID+" = "+question.FieldID+" AND "+QuestionAnswerColumns.STATUS+" = '"+StoryProviderAPI.STATUS_INCOMPLETE+"'" , null,
	 									null);

	 					if (alreadyExists.getCount() <= 0) {
	 						// doesn't exist, so insert it
	 						ContentValues v = new ContentValues();
	 						v.put(QuestionAnswerColumns.QUESTION_ANSWER,
	 								"");

	 						v.put(QuestionAnswerColumns.QUESTION, question.Question);
	 						
	 						v.put(QuestionAnswerColumns.PROJECT_ID, question.FieldID);
	 						
	 						v.put(QuestionAnswerColumns.QUESTION_ID, question.QuestionID);
	 						
	 						v.put(QuestionAnswerColumns.STATUS, StoryProviderAPI.STATUS_INCOMPLETE);
	 						
	 						v.put(QuestionAnswerColumns.STORY_ID, storyid);

	 						uri = getApplicationContext().getContentResolver()
	 								.insert(QuestionAnswerColumns.CONTENT_URI, v);
	 						MandeUtility.Log(false, "insert: Question answer");
	 						
	 						editTextAnswer.setText("");
	 						
	 						Cursor newquestionanswercursor =  getApplicationContext()
		 							.getContentResolver().query(
		 									QuestionAnswerColumns.CONTENT_URI,
		 									null,  QuestionAnswerColumns.QUESTION_ID + " ="+question.QuestionID+" AND "+QuestionAnswerColumns.PROJECT_ID+" = "+question.FieldID+" AND "+QuestionAnswerColumns.STATUS+" = '"+StoryProviderAPI.STATUS_INCOMPLETE+"'" , null,
		 									null);
	 						newquestionanswercursor.moveToFirst();
							
							  questionanswerid = newquestionanswercursor.getInt(newquestionanswercursor
											.getColumnIndex(QuestionAnswerColumns._ID));
							  
							  newquestionanswercursor.close();
							  

	 					} else {
	 						alreadyExists.moveToFirst();
	 						
	 						editTextAnswer.setText(alreadyExists.getString(alreadyExists
	 								.getColumnIndex(QuestionAnswerColumns.QUESTION_ANSWER)));
	 						
	 						
	 						
							
							  questionanswerid = alreadyExists.getInt(alreadyExists
											.getColumnIndex(QuestionAnswerColumns._ID));
	 						
	 						
	 					}
	 				} finally {
	 					if (alreadyExists != null) {
	 						alreadyExists.close();
	 					}

	 				}

	 			} catch (Exception e) {
	 		         MandeUtility.Log(true, e.getCause().getMessage());
	 			}
	 	      
	 	        //update previous question
	 	       
	 	        try{
	 	        	if(questionController.QUESTION_COUNTER>0){
	 	        		
	 	        		 WSQuestion ques = questionController.getQuestion(questionController.QUESTION_COUNTER-1);
	 	 	        	Cursor alreadyExists = null;
	 	 				Uri uri = null;
	 	 				try {
	 	 					alreadyExists = getApplicationContext()
	 	 							.getContentResolver().query(
	 	 									QuestionAnswerColumns.CONTENT_URI,
	 	 									null,  QuestionAnswerColumns.QUESTION_ID + " ="+ques.QuestionID+" AND "+QuestionAnswerColumns.PROJECT_ID+" = "+ques.FieldID+" AND "+QuestionAnswerColumns.STATUS+" = '"+StoryProviderAPI.STATUS_INCOMPLETE+"'", null,
	 	 									null);

	 	 					if (alreadyExists.getCount() <= 0) {
	 	 					

	 	 					} else {
	 	 					
	 	 					alreadyExists.moveToFirst();
	 	 					
	 	 						// doesn't exist, so insert it
	 	 						ContentValues v = new ContentValues();
	 	 						v.put(QuestionAnswerColumns.QUESTION_ANSWER,
	 	 								answ);
	 	 						v.put(QuestionAnswerColumns.PROJECT_ID, question.FieldID);
	 	 						
	 	 						v.put(QuestionAnswerColumns.STATUS, StoryProviderAPI.STATUS_INCOMPLETE);
	 	 						
	 	 						v.put(QuestionAnswerColumns.QUESTION, ques.Question);
	 	 						
	 	 						v.put(QuestionAnswerColumns.QUESTION_ID, ques.QuestionID);
	 	 						
	 	 						v.put(QuestionAnswerColumns.STORY_ID, storyid);
	 	 						
	 	 						uri = Uri
	 	 								.withAppendedPath(
	 	 										QuestionAnswerColumns.CONTENT_URI,
	 	 										alreadyExists.getString(alreadyExists
	 	 												.getColumnIndex(QuestionAnswerColumns._ID)));
	 	 						
	 	 						getApplicationContext().getContentResolver()
	 	 								.update(uri, v, null, null);
	 	 						MandeUtility.Log(false, "update: Question answer");
	 	 						
	 	 						Cursor newquestionanswercursor =  getApplicationContext()
			 							.getContentResolver().query(
			 									QuestionAnswerColumns.CONTENT_URI,
			 									null,  QuestionAnswerColumns.QUESTION_ID + " ="+question.QuestionID+" AND "+QuestionAnswerColumns.PROJECT_ID+" = "+question.FieldID+" AND "+QuestionAnswerColumns.STATUS+" = '"+StoryProviderAPI.STATUS_INCOMPLETE+"'" , null,
			 									null);
		 						newquestionanswercursor.moveToFirst();
								
								  questionanswerid = newquestionanswercursor.getInt(newquestionanswercursor
												.getColumnIndex(QuestionAnswerColumns._ID));
								  
								  newquestionanswercursor.close();
	 	 						
	 	 						
	 	 					}
	 	 				} finally {
	 	 					if (alreadyExists != null) {
	 	 						alreadyExists.close();
	 	 					}

	 	 				}
	 	 				
	 	        	}
	 	        } catch (Exception e) {
	 		         MandeUtility.Log(true, e.getCause().getMessage());
	 			}
	 	        
		}catch(Exception ex){
			 if(questionController.QUESTION_COUNTER==questionController.questionCount){
		        	
		        	//show story form
		        	
		        	Uri fieldUri = ContentUris.withAppendedId(
							FieldsColumns.CONTENT_URI, ID);
					startActivity(new Intent(Intent.ACTION_CREATE_DOCUMENT, fieldUri));
					
		        	
					
		        	
		        }
		}
		

		try {
			CommonFunctions.createStoryDir(String.valueOf(storyid));
			CommonFunctions.createQuestionAnswerDir(
					String.valueOf(storyid),
					String.valueOf(questionanswerid));
		} catch (Exception ex) {

			MandeUtility.Log(true, ex.getMessage());

		}
		
		
		mFileName = Constants.STORIES_PATH+ File.separator+String.valueOf(storyid)+ File.separator+String.valueOf(questionanswerid)+File.separator+"audiorecord.3gp";    
	       
	}

	/**
	 * Determines what should be displayed between a question, or the start
	 * screen and displays the appropriate view. Also saves answers to the data
	 * model without checking constraints.
	 */
	private void showPreviousView() {
		 WSQuestion question = questionController.getPrevQuestion();	        
	        txtQuestion.setText(question.Question);
	        
	        String answ =editTextAnswer.getText().toString();
	        
	        try {
				Cursor alreadyExists = null;
				Uri uri = null;
				try {
										
					alreadyExists = getApplicationContext()
							.getContentResolver().query(
									QuestionAnswerColumns.CONTENT_URI,
									null,  QuestionAnswerColumns.QUESTION_ID + " ="+question.QuestionID+" AND "+QuestionAnswerColumns.PROJECT_ID+" = "+question.FieldID+" AND "+QuestionAnswerColumns.STATUS+" = '"+StoryProviderAPI.STATUS_INCOMPLETE+"'", null,
									null);

					if (alreadyExists.getCount() <= 0) {
						// doesn't exist, so insert it
						ContentValues v = new ContentValues();
						v.put(QuestionAnswerColumns.QUESTION_ANSWER,
								"");

						v.put(QuestionAnswerColumns.QUESTION, question.Question);
						
						v.put(QuestionAnswerColumns.STATUS, StoryProviderAPI.STATUS_INCOMPLETE);
						
						v.put(QuestionAnswerColumns.QUESTION_ID, question.QuestionID);
						

						uri = getApplicationContext().getContentResolver()
								.insert(QuestionAnswerColumns.CONTENT_URI, v);
						MandeUtility.Log(false, "insert: Question answer");
						
						Cursor newquestionanswercursor =  getApplicationContext()
	 							.getContentResolver().query(
	 									QuestionAnswerColumns.CONTENT_URI,
	 									null,  QuestionAnswerColumns.QUESTION_ID + " ="+question.QuestionID+" AND "+QuestionAnswerColumns.PROJECT_ID+" = "+question.FieldID+" AND "+QuestionAnswerColumns.STATUS+" = '"+StoryProviderAPI.STATUS_INCOMPLETE+"'" , null,
	 									null);
 						newquestionanswercursor.moveToFirst();
						
						  questionanswerid = newquestionanswercursor.getInt(newquestionanswercursor
										.getColumnIndex(QuestionAnswerColumns._ID));
						  
						  newquestionanswercursor.close();


					} else {
						alreadyExists.moveToFirst();
						
						editTextAnswer.setText(alreadyExists.getString(alreadyExists
								.getColumnIndex(QuestionAnswerColumns.QUESTION_ANSWER)));
						
						questionanswerid = alreadyExists.getInt(alreadyExists
								.getColumnIndex(QuestionAnswerColumns._ID));
						
						
					}
				} finally {
					if (alreadyExists != null) {
						alreadyExists.close();
					}

				}

			} catch (Exception e) {
		         MandeUtility.Log(true, e.getCause().getMessage());
			}
	      
	        //update previous question
	       
	        try{
	        	if(questionController.QUESTION_COUNTER<questionController.questionCount-1){
	        		
	        		 WSQuestion ques = questionController.getQuestion(questionController.QUESTION_COUNTER+1);
	 	        	Cursor alreadyExists = null;
	 				Uri uri = null;
	 				try {
	 					alreadyExists = getApplicationContext()
	 							.getContentResolver().query(
	 									QuestionAnswerColumns.CONTENT_URI,
	 									null,  QuestionAnswerColumns.QUESTION_ID + " ="+ques.QuestionID+" AND "+QuestionAnswerColumns.PROJECT_ID+" = "+ques.FieldID+" AND "+QuestionAnswerColumns.STATUS+" = '"+StoryProviderAPI.STATUS_INCOMPLETE+"'", null,
	 									null);

	 					if (alreadyExists.getCount() <= 0) {
	 					

	 					} else {
	 					
	 					alreadyExists.moveToFirst();
	 					
	 						// doesn't exist, so insert it
	 						ContentValues v = new ContentValues();
	 						v.put(QuestionAnswerColumns.QUESTION_ANSWER,
	 								answ);

	 						v.put(QuestionAnswerColumns.QUESTION, ques.Question);
	 						
	 						v.put(QuestionAnswerColumns.STATUS, StoryProviderAPI.STATUS_INCOMPLETE);
	 						
	 						v.put(QuestionAnswerColumns.QUESTION_ID, ques.QuestionID);
	 						uri = Uri
	 								.withAppendedPath(
	 										QuestionAnswerColumns.CONTENT_URI,
	 										alreadyExists.getString(alreadyExists
	 												.getColumnIndex(QuestionAnswerColumns._ID)));
	 						
	 						getApplicationContext().getContentResolver()
	 								.update(uri, v, null, null);
	 						MandeUtility.Log(false, "update: Question answer");
	 						
	 						Cursor newquestionanswercursor =  getApplicationContext()
		 							.getContentResolver().query(
		 									QuestionAnswerColumns.CONTENT_URI,
		 									null,  QuestionAnswerColumns.QUESTION_ID + " ="+question.QuestionID+" AND "+QuestionAnswerColumns.PROJECT_ID+" = "+question.FieldID+" AND "+QuestionAnswerColumns.STATUS+" = '"+StoryProviderAPI.STATUS_INCOMPLETE+"'" , null,
		 									null);
	 						newquestionanswercursor.moveToFirst();
							
							  questionanswerid = newquestionanswercursor.getInt(newquestionanswercursor
											.getColumnIndex(QuestionAnswerColumns._ID));
							  
							  newquestionanswercursor.close();
	 						
	 						
	 					}
	 				} finally {
	 					if (alreadyExists != null) {
	 						alreadyExists.close();
	 					}

	 				}
	 				
	        	}
	        } catch (Exception e) {
		         MandeUtility.Log(true, e.getCause().getMessage());
			}
	        

			try {
				CommonFunctions.createStoryDir(String.valueOf(storyid));
				CommonFunctions.createQuestionAnswerDir(
						String.valueOf(storyid),
						String.valueOf(questionanswerid));
			} catch (Exception ex) {

				MandeUtility.Log(true, ex.getMessage());

			}
			
	        	    	
	    	
	        mFileName = Constants.STORIES_PATH+ File.separator+String.valueOf(storyid)+ File.separator+String.valueOf(questionanswerid)+File.separator+"audiorecord.3gp";    
	    	
	}

	private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    class RecordButton extends Button {

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    setText("Stop recording");
                    recordBtnPressed = true;
                } else {
                    setText("Start recording");
                    recordBtnPressed = false;
                }
                mStartRecording = !mStartRecording;
            }
        };

        public RecordButton(Context ctx) {
            super(ctx);
            setText("Start recording");
            setOnClickListener(clicker);
        }
    }

    class PlayButton extends Button {

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    setText("Stop playing");
                    recordPlayBtnPressed = true;
                } else {
                    setText("Start playing");
                    recordPlayBtnPressed = false;
                }
                mStartPlaying = !mStartPlaying;
            }
        };

        public PlayButton(Context ctx) {
            super(ctx);
            setText("Start playing");
            setOnClickListener(clicker);
        }
    }
}
