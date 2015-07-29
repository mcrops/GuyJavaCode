package aidev.cocis.makerere.org.whiteflycounter.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Set;

import aidev.cocis.makerere.org.whiteflycounter.R;
import aidev.cocis.makerere.org.whiteflycounter.common.CompatibilityUtils;
import aidev.cocis.makerere.org.whiteflycounter.common.MandeUtility;
import aidev.cocis.makerere.org.whiteflycounter.listeners.FieldSettingsDownloaderListener;
import aidev.cocis.makerere.org.whiteflycounter.preferences.PreferencesActivity;
import aidev.cocis.makerere.org.whiteflycounter.provider.FieldsProviderAPI.FieldsColumns;
import aidev.cocis.makerere.org.whiteflycounter.provider.QuestionProviderAPI.QuestionColumns;
import aidev.cocis.makerere.org.whiteflycounter.provider.ReportingPeriodProviderAPI.ReportingPeriodColumns;
import aidev.cocis.makerere.org.whiteflycounter.provider.TemplateProviderAPI.TemplateColumns;
import aidev.cocis.makerere.org.whiteflycounter.tasks.DownloadFieldSettingsTask;
import aidev.cocis.makerere.org.whiteflycounter.webservices.field.WSField;
import aidev.cocis.makerere.org.whiteflycounter.webservices.field.WSFieldSetting;
import aidev.cocis.makerere.org.whiteflycounter.webservices.field.WSQuestion;

/**
 * Responsible for displaying, adding and deleting all the valid fields in the
 * fields directory. If the server requires authentication, a dialog will pop
 * up asking when you request the field list. If somehow you manage to wait
 * long enough and then try to download selected fields and your authorization
 * has timed out, it won't again ask for authentication, it will just throw a
 * 401 and you'll have to hit 'refresh' where it will ask for credentials again.
 * 
 * @author Acellam Guy
 */
public class FieldSettingsDownload extends Activity implements
		FieldSettingsDownloaderListener {
	private static final String t = "RemoveFileManageList";

	private static final int PROGRESS_DIALOG = 1;
	private static final int AUTH_DIALOG = 2;
	private static final int MENU_PREFERENCES = Menu.FIRST;

	private static final String BUNDLE_TOGGLED_KEY = "toggled";
	private static final String BUNDLE_SELECTED_COUNT = "selectedcount";
	private static final String BUNDLE_PROJECT_MAP = "fieldmap";
	private static final String DIALOG_TITLE = "dialogtitle";
	private static final String DIALOG_MSG = "dialogmsg";
	private static final String DIALOG_SHOWING = "dialogshowing";
	private static final String PROJECTLIST = "fieldlist";

	public static final String LIST_URL = "listurl";

	private static final String PROJECTNAME = "fieldname";
	private static final String PROJECTDETAIL_ID = "fielddetailid";
	private static final String PROJECTID_DISPLAY = "fieldiddisplay";

	private String mAlertMsg;
	private boolean mAlertShowing = false;
	private String mAlertTitle;

	private AlertDialog mAlertDialog;
	private ProgressDialog mProgressDialog;

	private DownloadFieldSettingsTask mDownloadFieldSettingsTask;
	private static final boolean EXIT = true;
	private static final boolean DO_NOT_EXIT = false;
	private boolean mShouldExit;
	private static final String SHOULD_EXIT = "shouldexit";

	public SharedPreferences preferences;
	WSField field;

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fields_download_settings);
		setTitle(getString(R.string.app_name) + " > "
				+ getString(R.string.get_fields));
		mAlertMsg = getString(R.string.please_wait);
		field = new WSField();

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
		downloadFieldSettings();

		if (savedInstanceState != null) {
			// If the screen has rotated, the hashmap with the field ids and
			// urls is passed here.
			if (savedInstanceState.containsKey(BUNDLE_PROJECT_MAP)) {
				field = (WSField) savedInstanceState
						.getSerializable(BUNDLE_PROJECT_MAP);
			}

			// to restore alert dialog.
			if (savedInstanceState.containsKey(DIALOG_TITLE)) {
				mAlertTitle = savedInstanceState.getString(DIALOG_TITLE);
			}
			if (savedInstanceState.containsKey(DIALOG_MSG)) {
				mAlertMsg = savedInstanceState.getString(DIALOG_MSG);
			}
			if (savedInstanceState.containsKey(DIALOG_SHOWING)) {
				mAlertShowing = savedInstanceState.getBoolean(DIALOG_SHOWING);
			}
			if (savedInstanceState.containsKey(SHOULD_EXIT)) {
				mShouldExit = savedInstanceState.getBoolean(SHOULD_EXIT);
			}
		}

		if (getLastNonConfigurationInstance() instanceof FieldSettingsDownload) {
			mDownloadFieldSettingsTask = (DownloadFieldSettingsTask) getLastNonConfigurationInstance();
			mDownloadFieldSettingsTask.preferences = preferences;
			if (mDownloadFieldSettingsTask.getStatus() == AsyncTask.Status.FINISHED) {
				try {
					dismissDialog(PROGRESS_DIALOG);
				} catch (IllegalArgumentException e) {
					Log.i(t,
							"Attempting to close a dialog that was not previously opened");
				}
				mDownloadFieldSettingsTask = null;
			}
		} else if (getLastNonConfigurationInstance() == null) {
			// first time, so get the fieldlist
			downloadFieldSettings();
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

	/**
	 * Starts the download task and shows the progress dialog.
	 */
	private void downloadFieldSettings() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

		if (ni == null || !ni.isConnected()) {
			Toast.makeText(this, R.string.no_connection, Toast.LENGTH_SHORT)
					.show();
		} else {

			if (mProgressDialog != null) {
				// This is needed because onPrepareDialog() is broken in 1.6.
				mProgressDialog.setMessage(getString(R.string.please_wait));
			}

			showDialog(PROGRESS_DIALOG);

			if (mDownloadFieldSettingsTask != null
					&& mDownloadFieldSettingsTask.getStatus() != AsyncTask.Status.FINISHED) {
				return; // we are already doing the download!!!
			} else if (mDownloadFieldSettingsTask != null) {
				mDownloadFieldSettingsTask.setDownloaderListener(null);
				mDownloadFieldSettingsTask.cancel(true);
				mDownloadFieldSettingsTask = null;
			}

			mDownloadFieldSettingsTask = new DownloadFieldSettingsTask();
			mDownloadFieldSettingsTask.ctx = this.getApplicationContext();
			mDownloadFieldSettingsTask.preferences = preferences;
			mDownloadFieldSettingsTask.setDownloaderListener(this);
			mDownloadFieldSettingsTask.execute(field);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// outState.putSerializable(BUNDLE_PROJECT_MAP, (Serializable) field);
		outState.putString(DIALOG_TITLE, mAlertTitle);
		outState.putString(DIALOG_MSG, mAlertMsg);
		outState.putBoolean(DIALOG_SHOWING, mAlertShowing);
		outState.putBoolean(SHOULD_EXIT, mShouldExit);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MandeUtility.Log(false, "onCreateOptionsMenu:show");
		super.onCreateOptionsMenu(menu);

		CompatibilityUtils.setShowAsAction(
				menu.add(0, MENU_PREFERENCES, 0, R.string.general_preferences)
						.setIcon(R.drawable.ic_menu_preferences),
				MenuItem.SHOW_AS_ACTION_NEVER);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case MENU_PREFERENCES:
			MandeUtility.Log(false, "onMenuItemSelected:MENU_PREFERENCES");
			Intent i = new Intent(this, PreferencesActivity.class);
			startActivity(i);
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case PROGRESS_DIALOG:
			MandeUtility.Log(false, "onCreateDialog.PROGRESS_DIALOG:show");
			mProgressDialog = new ProgressDialog(this);
			DialogInterface.OnClickListener loadingButtonListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					MandeUtility
							.Log(false, "onCreateDialog.PROGRESS_DIALOG:OK");
					dialog.dismiss();
					// we use the same progress dialog for both
					// so whatever isn't null is running
					if (mDownloadFieldSettingsTask != null) {
						mDownloadFieldSettingsTask
								.setDownloaderListener(null);
						mDownloadFieldSettingsTask.cancel(true);
						mDownloadFieldSettingsTask = null;
					}
				}
			};
			mProgressDialog.setTitle(getString(R.string.downloading_data));
			mProgressDialog.setMessage(mAlertMsg);
			mProgressDialog.setIcon(android.R.drawable.ic_dialog_info);
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.setCancelable(false);
			mProgressDialog.setButton(getString(R.string.cancel),
					loadingButtonListener);
			return mProgressDialog;
		case AUTH_DIALOG:
			/*
			 * MandeUtility.Log(false,"onCreateDialog.AUTH_DIALOG:show");
			 * AlertDialog.Builder b = new AlertDialog.Builder(this);
			 * 
			 * LayoutInflater factory = LayoutInflater.from(this); final View
			 * dialogView = factory.inflate(R.layout.server_auth_dialog, null);
			 * 
			 * // Get the server, username, and password from the settings
			 * SharedPreferences settings =
			 * PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			 * String server =
			 * settings.getString(PreferencesActivity.KEY_SERVER_URL,
			 * getString(R.string.default_server_url));
			 * 
			 * String fieldListUrl =
			 * getString(R.string.default_odk_fieldlist); final String url =
			 * server +
			 * settings.getString(PreferencesActivity.KEY_PROJECTLIST_URL,
			 * fieldListUrl); Log.i(t, "Trying to get fieldList from: " +
			 * url);
			 * 
			 * EditText username = (EditText)
			 * dialogView.findViewById(R.id.username_edit); String
			 * storedUsername =
			 * settings.getString(PreferencesActivity.KEY_USERNAME, null);
			 * username.setText(storedUsername);
			 * 
			 * EditText password = (EditText)
			 * dialogView.findViewById(R.id.password_edit); String
			 * storedPassword =
			 * settings.getString(PreferencesActivity.KEY_PASSWORD, null);
			 * password.setText(storedPassword);
			 * 
			 * b.setTitle(getString(R.string.server_requires_auth));
			 * b.setMessage(getString(R.string.server_auth_credentials, url));
			 * b.setView(dialogView);
			 * b.setPositiveButton(getString(R.string.ok), new
			 * DialogInterface.OnClickListener() {
			 * 
			 * @Override public void onClick(DialogInterface dialog, int which)
			 * { Collect.getInstance().getActivityLogger().logAction(this,
			 * "onCreateDialog.AUTH_DIALOG", "OK");
			 * 
			 * EditText username = (EditText)
			 * dialogView.findViewById(R.id.username_edit); EditText password =
			 * (EditText) dialogView.findViewById(R.id.password_edit);
			 * 
			 * Uri u = Uri.parse(url);
			 * 
			 * WebUtils.addCredentials(username.getText().toString(),
			 * password.getText() .toString(), u.getHost());
			 * downloadFieldList(); } });
			 * b.setNegativeButton(getString(R.string.cancel), new
			 * DialogInterface.OnClickListener() {
			 * 
			 * @Override public void onClick(DialogInterface dialog, int which)
			 * { Collect.getInstance().getActivityLogger().logAction(this,
			 * "onCreateDialog.AUTH_DIALOG", "Cancel"); finish(); } });
			 * 
			 * b.setCancelable(false); mAlertShowing = false; return b.create();
			 */
		}
		return null;
	}

	@Override
	public Object onRetainNonConfigurationInstance() {

		return mDownloadFieldSettingsTask;

	}

	@Override
	protected void onDestroy() {
		if (mDownloadFieldSettingsTask != null) {
			mDownloadFieldSettingsTask.setDownloaderListener(null);
		}
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		if (mDownloadFieldSettingsTask != null) {
			mDownloadFieldSettingsTask.setDownloaderListener(this);
		}
		if (mAlertShowing) {
			createAlertDialog(mAlertTitle, mAlertMsg, mShouldExit);
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (mAlertDialog != null && mAlertDialog.isShowing()) {
			mAlertDialog.dismiss();
		}
		super.onPause();
	}

	/**
	 * Creates an alert dialog with the given tite and message. If shouldExit is
	 * set to true, the activity will exit when the user clicks "ok".
	 * 
	 * @param title
	 * @param message
	 * @param shouldExit
	 */
	private void createAlertDialog(String title, String message,
			final boolean shouldExit) {
		MandeUtility.Log(false, "createAlertDialog:show");
		mAlertDialog = new AlertDialog.Builder(this).create();
		mAlertDialog.setTitle(title);
		mAlertDialog.setMessage(message);
		DialogInterface.OnClickListener quitListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int i) {
				switch (i) {
				case DialogInterface.BUTTON_POSITIVE: // ok
					MandeUtility.Log(false, "createAlertDialog:OK");
					// just close the dialog
					mAlertShowing = false;
					// successful download, so quit
					if (shouldExit) {
						finish();
					}
					break;
				}
			}
		};
		mAlertDialog.setCancelable(false);
		mAlertDialog.setButton(getString(R.string.ok), quitListener);
		mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
		mAlertMsg = message;
		mAlertTitle = title;
		mAlertShowing = true;
		mShouldExit = shouldExit;
		mAlertDialog.show();
	}

	/*
	 * @Override public void progressUpdate(String field,String
	 * currentSetting,String msg, int progress, int total) { mAlertMsg =
	 * getString(R.string.fetching_field, field,currentSetting,msg,
	 * progress, total); mProgressDialog.setMessage(mAlertMsg); }
	 */

	@Override
	public void fieldSettingsDownloadingComplete(
			HashMap<WSFieldSetting, String> result) {

		// if (mProgressDialog.isShowing()) {
		// should always be true here
		// mProgressDialog.dismiss();
		// }

		/*
		 * Set<WSFieldSetting> keys = result.keySet();
		 * 
		 * StringBuilder b = new StringBuilder(); for (WSFieldSetting k :
		 * keys) { b.append(k.field.Name); b.append("\n\n"); }
		 * 
		 * createAlertDialog(getString(R.string.download_fields_result),
		 * b.toString().trim(), EXIT);
		 */

		this.FieldReportingPeriodDownloadingComplete(result);
	}

	@Override
	public void FieldReportingPeriodTemplateQuestionDownloadingComplete(
			HashMap<WSFieldSetting, String> value) {
		mAlertMsg = getString(R.string.fetching_field_settings, "Questions",
				"Syncing", 3, 3);
		mProgressDialog.setMessage(mAlertMsg);

		Set<WSFieldSetting> keys = value.keySet();

		StringBuilder b = new StringBuilder();
		for (WSFieldSetting fd : keys) {

			mAlertMsg = getString(R.string.fetching_field_settings,
					"Questions", "Creating Database", 3, 3);
			mProgressDialog.setMessage(mAlertMsg);
			for (WSQuestion question : fd.questions) {
				try {
					Cursor alreadyExists = null;
					Uri uri = null;
					try {
						/*String[] fieldion = { QuestionColumns.QUESTION_ORDER,
								QuestionColumns.QUESTION_ORDER };
						String[] selectionArgs = { String
								.valueOf(question.QuestionOrder) };
						String selection = QuestionColumns.QUESTION_ORDER
								+ "=?";*/
						alreadyExists = getApplicationContext()
								.getContentResolver().query(
										QuestionColumns.CONTENT_URI,
										null,  QuestionColumns.PROJECT_ID + " ="+fd.field.FieldID +" AND "+ QuestionColumns.QUESTION_ID + " ="+question.QuestionID, null,
										null);

						if (alreadyExists.getCount() <= 0) {
							mAlertMsg = getString(
									R.string.fetching_field_settings,
									"Questions", "Inserting in Database", 3, 3);
							mProgressDialog.setMessage(mAlertMsg);
							// doesn't exist, so insert it
							ContentValues v = new ContentValues();
							v.put(QuestionColumns.PROJECT_ID,
									fd.field.FieldID);

							v.put(QuestionColumns.QUESTION, question.Question);
							
							v.put(QuestionColumns.QUESTION_ID, question.QuestionID);
							

							v.put(QuestionColumns.QUESTION_ORDER,
									question.QuestionOrder);

							uri = getApplicationContext().getContentResolver()
									.insert(QuestionColumns.CONTENT_URI, v);
							MandeUtility.Log(false, "insert:"
									+ fd.reportingPeriod.PeriodDescription);

							mAlertMsg = getString(
									R.string.fetching_field_settings,
									"Questions",
									"Questions Successfully synced!", 3, 3);
							mProgressDialog.setMessage(mAlertMsg);

							if (mProgressDialog.isShowing()) {
								// should always be true here
								mProgressDialog.dismiss();
							}
							createAlertDialog(
									getString(R.string.download_fields_result),
									"Sync Finished", EXIT);

						} else {
							alreadyExists.moveToFirst();
							mAlertMsg = getString(
									R.string.fetching_field_settings,
									"Questions", "Questions found", 3, 3);
							;
							mProgressDialog.setMessage(mAlertMsg);
							
							if (mProgressDialog.isShowing()) {
								// should always be true here
								mProgressDialog.dismiss();
							}
							createAlertDialog(
									getString(R.string.download_fields_result),
									"Sync Finished", EXIT);
							
							uri = Uri
									.withAppendedPath(
											TemplateColumns.CONTENT_URI,
											alreadyExists.getString(alreadyExists
													.getColumnIndex(TemplateColumns.PROJECT_ID)));
							
							ContentValues v = new ContentValues();
							v.put(QuestionColumns.PROJECT_ID,
									fd.field.FieldID);

							v.put(QuestionColumns.QUESTION, question.Question);
							
							
							v.put(QuestionColumns.QUESTION_ID, question.QuestionID);

							v.put(QuestionColumns.QUESTION_ORDER,
									question.QuestionOrder);
	 						/*uri = Uri
	 								.withAppendedPath(
	 										QuestionAnswerColumns.CONTENT_URI,
	 										alreadyExists.getString(alreadyExists
	 												.getColumnIndex(QuestionAnswerColumns.QUESTION_ID)));*/
	 						
	 						getApplicationContext().getContentResolver()
	 								.update(QuestionColumns.CONTENT_URI, v, null, null);
	 						MandeUtility.Log(false, "update: Question");
	 						
							MandeUtility.Log(false, "refresh"
									+ fd.reportingPeriod.PeriodDescription);
						}
					} finally {
						if (alreadyExists != null) {
							alreadyExists.close();
						}

					}

				} catch (Exception e) {
					e.printStackTrace();
					if (e.getCause() != null) {
						mAlertMsg = getString(
								R.string.fetching_field_settings,
								"Questions", e.getCause().getMessage(), 3, 3);
						mProgressDialog.setMessage(mAlertMsg);
					} else {
						mAlertMsg = getString(
								R.string.fetching_field_settings,
								"Questions", e.getMessage(), 3, 3);
						mProgressDialog.setMessage(mAlertMsg);
					}
				}
			}

		}

	}

	@Override
	public void FieldReportingPeriodTemplateDownloadingComplete(
			HashMap<WSFieldSetting, String> value) {
		mAlertMsg = getString(R.string.fetching_field_settings,
				"Questionnaire", "Syncing", 2, 3);
		mProgressDialog.setMessage(mAlertMsg);

		Set<WSFieldSetting> keys = value.keySet();

		StringBuilder b = new StringBuilder();
		for (WSFieldSetting fd : keys) {

			mAlertMsg = getString(R.string.fetching_field_settings,
					"Questionnaire", "Creating Database", 2, 3);
			mProgressDialog.setMessage(mAlertMsg);

			try {
				Cursor alreadyExists = null;
				Uri uri = null;
				try {
					String[] fieldion = { TemplateColumns.PROJECT_ID,
							TemplateColumns.PROJECT_ID };
					String[] selectionArgs = { String
							.valueOf(fd.field.FieldID) };
					String selection = TemplateColumns.PROJECT_ID + "=?";
					alreadyExists = getApplicationContext()
							.getContentResolver().query(
									TemplateColumns.CONTENT_URI, fieldion,
									selection, selectionArgs, null);

					if (alreadyExists.getCount() <= 0) {
						mAlertMsg = getString(
								R.string.fetching_field_settings,
								"Questionnaire", "Inserting in Database", 2, 3);
						mProgressDialog.setMessage(mAlertMsg);
						// doesn't exist, so insert it
						ContentValues v = new ContentValues();
						v.put(TemplateColumns.PROJECT_ID, fd.field.FieldID);

						v.put(TemplateColumns.NAME, fd.field.Name);
						uri = getApplicationContext().getContentResolver()
								.insert(TemplateColumns.CONTENT_URI, v);
						MandeUtility.Log(false, "insert:"
								+ fd.reportingPeriod.PeriodDescription);

						mAlertMsg = getString(
								R.string.fetching_field_settings,
								"Questionnaire",
								"Reporting Period Successfully synced!", 2, 3);
						mProgressDialog.setMessage(mAlertMsg);

						this.FieldReportingPeriodTemplateQuestionDownloadingComplete(value);

					} else {
						alreadyExists.moveToFirst();
						mAlertMsg = getString(
								R.string.fetching_field_settings,
								"Questionnaire", "Questionnaires found", 2, 3);
						;
						mProgressDialog.setMessage(mAlertMsg);
						this.FieldReportingPeriodTemplateQuestionDownloadingComplete(value);
						uri = Uri
								.withAppendedPath(
										TemplateColumns.CONTENT_URI,
										alreadyExists.getString(alreadyExists
												.getColumnIndex(TemplateColumns.PROJECT_ID)));
						MandeUtility.Log(false, "refresh"
								+ fd.reportingPeriod.PeriodDescription);
					}
				} finally {
					if (alreadyExists != null) {
						alreadyExists.close();
					}

				}

			} catch (Exception e) {
				e.printStackTrace();
				if (e.getCause() != null) {
					mAlertMsg = getString(R.string.fetching_field_settings,
							"Reporting Period", e.getCause().getMessage(), 1, 3);
					mProgressDialog.setMessage(mAlertMsg);
				} else {
					mAlertMsg = getString(R.string.fetching_field_settings,
							"Reporting Period", e.getMessage(), 1, 3);
					mProgressDialog.setMessage(mAlertMsg);
				}
			}

		}
	}

	@Override
	public void FieldReportingPeriodDownloadingComplete(
			HashMap<WSFieldSetting, String> value) {
		mAlertMsg = getString(R.string.fetching_field_settings,
				"Reporting Period", "Syncing", 1, 3);
		mProgressDialog.setMessage(mAlertMsg);

		Set<WSFieldSetting> keys = value.keySet();

		StringBuilder b = new StringBuilder();
		for (WSFieldSetting fd : keys) {

			mAlertMsg = getString(R.string.fetching_field_settings,
					"Reporting Period", "Creating Database", 1, 3);
			mProgressDialog.setMessage(mAlertMsg);

			try {
				Cursor alreadyExists = null;
				Uri uri = null;
				try {
					String[] fieldion = { ReportingPeriodColumns.PROJECT_ID,
							ReportingPeriodColumns.PROJECT_ID };
					String[] selectionArgs = { String
							.valueOf(fd.field.FieldID) };
					String selection = ReportingPeriodColumns.PROJECT_ID + "=?";
					alreadyExists = getApplicationContext()
							.getContentResolver().query(
									ReportingPeriodColumns.CONTENT_URI,
									fieldion, selection, selectionArgs, null);

					if (alreadyExists.getCount() <= 0) {
						mAlertMsg = getString(
								R.string.fetching_field_settings,
								"Reporting Period", "Inserting in Database", 1,
								3);
						mProgressDialog.setMessage(mAlertMsg);
						// doesn't exist, so insert it
						ContentValues v = new ContentValues();
						v.put(ReportingPeriodColumns.PROJECT_ID,
								fd.field.FieldID);

						v.put(ReportingPeriodColumns.RESEARCH_START_DATE,
								fd.reportingPeriod.ResearchStartDate
										.toLocaleString());
						v.put(ReportingPeriodColumns.RESEARCH_END_DATE,
								fd.reportingPeriod.ResearchEndDate
										.toLocaleString());
						v.put(ReportingPeriodColumns.STORY_INPUT_DEADLINE,
								fd.reportingPeriod.StoryInputDeadline
										.toLocaleString());
						v.put(ReportingPeriodColumns.REPORT_PERIOD_CLOSE_DATE,
								fd.reportingPeriod.ReportPeriodCloseDate
										.toLocaleString());
						v.put(ReportingPeriodColumns.PERIOD_DESCRIPTION,
								fd.reportingPeriod.PeriodDescription);
						v.put(ReportingPeriodColumns.PERIOD_ACTIVE,
								fd.reportingPeriod.Active);
						v.put(ReportingPeriodColumns.PERIOD_STATUS,
								fd.reportingPeriod.Status);
						uri = getApplicationContext().getContentResolver()
								.insert(ReportingPeriodColumns.CONTENT_URI, v);
						MandeUtility.Log(false, "insert:"
								+ fd.reportingPeriod.PeriodDescription);

						mAlertMsg = getString(
								R.string.fetching_field_settings,
								"Reporting Period",
								"Reporting Period Successfully synced!", 1, 3);
						mProgressDialog.setMessage(mAlertMsg);

						this.FieldReportingPeriodTemplateDownloadingComplete(value);

					} else {
						alreadyExists.moveToFirst();
						mAlertMsg = getString(
								R.string.fetching_field_settings,
								"Reporting Period", "Found!", 1, 3);
						mProgressDialog.setMessage(mAlertMsg);
						this.FieldReportingPeriodTemplateDownloadingComplete(value);
						uri = Uri
								.withAppendedPath(
										ReportingPeriodColumns.CONTENT_URI,
										alreadyExists.getString(alreadyExists
												.getColumnIndex(ReportingPeriodColumns.PROJECT_ID)));
						MandeUtility.Log(false, "refresh"
								+ fd.reportingPeriod.PeriodDescription);
					}
				} finally {
					if (alreadyExists != null) {
						alreadyExists.close();
					}

				}

			} catch (Exception e) {
				e.printStackTrace();
				if (e.getCause() != null) {
					mAlertMsg = getString(R.string.fetching_field_settings,
							"Reporting Period", e.getCause().getMessage(), 1, 3);
					mProgressDialog.setMessage(mAlertMsg);
				} else {
					mAlertMsg = getString(R.string.fetching_field_settings,
							"Reporting Period", e.getMessage(), 1, 3);
					mProgressDialog.setMessage(mAlertMsg);
				}
			}

		}

	}

}
