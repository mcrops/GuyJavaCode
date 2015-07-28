package aidev.cocis.makerere.org.whiteflycounter.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.Map.Entry;

import aidev.cocis.makerere.org.whiteflycounter.R;
import aidev.cocis.makerere.org.whiteflycounter.common.MandeUtility;
import aidev.cocis.makerere.org.whiteflycounter.preferences.AdminPreferencesActivity;
import aidev.cocis.makerere.org.whiteflycounter.preferences.PreferencesActivity;
import aidev.cocis.makerere.org.whiteflycounter.provider.FieldsProviderAPI.FieldsColumns;
import aidev.cocis.makerere.org.whiteflycounter.provider.QuestionAnswerProviderAPI.QuestionAnswerColumns;
import aidev.cocis.makerere.org.whiteflycounter.provider.StoryProviderAPI;
import aidev.cocis.makerere.org.whiteflycounter.provider.StoryProviderAPI.StoryColumns;

public class StoryEntryActivity extends Activity {
	// menu options
	private static final int MENU_PREFERENCES = Menu.FIRST;
	private static final int MENU_ADMIN = Menu.FIRST + 1;

	private static final int PASSWORD_DIALOG = 1;
	private static final String t = "StoryEntryActivity";
	// buttons
	private String version;
	private View mReviewSpacer;
	private View mGetFormsSpacer;

	private AlertDialog mAlertDialog;
	private SharedPreferences mAdminPreferences;

	int storyid = 0;
	int fieldID = 0;
	int ID = 0;
	private Button mSaveStory;
	EditText storyTitle;
	EditText participantName;
	EditText participantContact;
	EditText storysummary;
	CheckBox storyComplete;

	private static final boolean EXIT = true;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.form_entry_end);
		
		setTitle(getString(R.string.app_name) + " > "
				+ getString(R.string.save_story));
		
		((TextView) findViewById(R.id.description))
		.setText(R.string.story_end);
		
		storyTitle = (EditText) findViewById(R.id.save_title);
		participantName = (EditText) findViewById(R.id.save_person_name);
		participantContact = (EditText) findViewById(R.id.save_person_contact);
		storysummary = (EditText) findViewById(R.id.storysummary);
		// checkbox for if finished or ready to send
		storyComplete = ((CheckBox) findViewById(R.id.mark_finished));
		
		Intent intent = getIntent();
		if (intent != null) {
			Uri uri = intent.getData();

			if (getContentResolver().getType(uri) == FieldsColumns.CONTENT_ITEM_TYPE) {
				// get the field details

				String Name = null;
				String Description = null;
				String SubText = null;
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

							fieldID = fieldCursor
									.getInt(fieldCursor
											.getColumnIndex(FieldsColumns.WHITEFLYCOUNTER_PROJECT_ID));

							ID = fieldCursor
									.getInt(fieldCursor
											.getColumnIndex(FieldsColumns._ID));

							Name = fieldCursor.getString(fieldCursor
									.getColumnIndex(FieldsColumns.NAME));

							Description = fieldCursor
									.getString(fieldCursor
											.getColumnIndex(FieldsColumns.DESCRIPTION));

							SubText = fieldCursor
									.getString(fieldCursor
											.getColumnIndex(FieldsColumns.DISPLAY_SUBTEXT));
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
		
		Cursor alreadyExists = null;
		Uri uri = null;
		try {
													
			alreadyExists = getApplicationContext()
						.getContentResolver().query(
								StoryColumns.CONTENT_URI,
								null,  StoryColumns.STATUS + " = '"+StoryProviderAPI.STATUS_INCOMPLETE+"' AND "+StoryColumns.PROJECT_ID+" = "+fieldID, null,
								null);
			

			if (alreadyExists.getCount() <= 0) {
			

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
		
		
		//storyComplete.setChecked(true);
		
				
		
		// send data button. expects a result.
		mSaveStory = (Button) findViewById(R.id.save_exit_button);
		mSaveStory.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						MandeUtility.Log(false, "Save Story:click");
						
						ContentValues val = new ContentValues();
						val.put(StoryColumns.TITLE,storyTitle.getText().toString()
								);
						val.put(StoryColumns.PROJECT_ID,
								fieldID);
						val.put(StoryColumns.STORY_FULLTEXT,storysummary.getText().toString());
						
						val.put(StoryColumns.PARTICIPANT_NAME, participantName.getText().toString());
						
						val.put(StoryColumns.PARTICIPANT_CONTACT, participantContact.getText().toString());
						
						if(storyComplete.isChecked()){
							val.put(StoryColumns.STATUS, StoryProviderAPI.STATUS_FINALIZED);
						}else{
						val.put(StoryColumns.STATUS, StoryProviderAPI.STATUS_COMPLETE);
						}

						
 						Uri uri = Uri
 								.withAppendedPath(
 										StoryColumns.CONTENT_URI, String.valueOf(storyid));
 						
 						getApplicationContext().getContentResolver()
 								.update(uri, val, null, null);
 						MandeUtility.Log(false, "update: story answer");
 						
 						ContentValues qval = new ContentValues();
 						qval.put(QuestionAnswerColumns.STATUS,
 								StoryProviderAPI.STATUS_COMPLETE);
												
 						
 						
 						getApplicationContext().getContentResolver()
 								.update(QuestionAnswerColumns.CONTENT_URI, qval, QuestionAnswerColumns.STORY_ID + " = ?",
 							            new String[] { String.valueOf(storyid) });
 						MandeUtility.Log(false, "update: question  answer");
 						
 						MandeUtility.Log(false, "List Fields");
 						Intent i = new Intent(getApplicationContext(),
 								FieldChooserList.class);
 						startActivity(i);
 						
 						finish();
 						
					}
				});
		

		
		String saveName="";
		// no meta/instanceName field in the form -- see if we have a
		// name for this instance from a previous save attempt...
		if (getContentResolver().getType(getIntent().getData()) == StoryColumns.CONTENT_ITEM_TYPE) {
			Uri instanceUri = getIntent().getData();
			Cursor instance = null;
			try {
				instance = getContentResolver().query(instanceUri,
						null, null, null, null);
				if (instance.getCount() == 1) {
					instance.moveToFirst();
					saveName = instance
							.getString(instance
									.getColumnIndex(StoryColumns.PARTICIPANT_CONTACT));
				}
			} finally {
				if (instance != null) {
					instance.close();
				}
			}
		}
		if (saveName == null) {
			// last resort, default generated name
			
		}
		
		mSaveStory.setEnabled(true);
		mSaveStory.setVisibility(View.VISIBLE);
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_PREFERENCES:
			MandeUtility.Log(false, "onOptionsItemSelected:	MENU_PREFERENCES");
			Intent ig = new Intent(this, PreferencesActivity.class);
			startActivity(ig);
			return true;
		case MENU_ADMIN:
			MandeUtility.Log(false, "onOptionsItemSelected:MENU_ADMIN");
			String pw = mAdminPreferences.getString(
					AdminPreferencesActivity.KEY_ADMIN_PW, "");
			if ("".equalsIgnoreCase(pw)) {
				Intent i = new Intent(getApplicationContext(),
						AdminPreferencesActivity.class);
				startActivity(i);
			} else {
				showDialog(PASSWORD_DIALOG);
				MandeUtility.Log(false, "createAdminPasswordDialog:show");
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
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
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case PASSWORD_DIALOG:

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			final AlertDialog passwordDialog = builder.create();

			passwordDialog.setTitle(getString(R.string.enter_admin_password));
			final EditText input = new EditText(this);
			input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
			input.setTransformationMethod(PasswordTransformationMethod
					.getInstance());
			passwordDialog.setView(input, 20, 10, 20, 10);

			passwordDialog.setButton(AlertDialog.BUTTON_POSITIVE,
					getString(R.string.ok),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							String value = input.getText().toString();
							String pw = mAdminPreferences.getString(
									AdminPreferencesActivity.KEY_ADMIN_PW, "");
							if (pw.compareTo(value) == 0) {
								Intent i = new Intent(getApplicationContext(),
										AdminPreferencesActivity.class);
								startActivity(i);
								input.setText("");
								passwordDialog.dismiss();
							} else {
								Toast.makeText(
										StoryEntryActivity.this,
										getString(R.string.admin_password_incorrect),
										Toast.LENGTH_SHORT).show();
								MandeUtility.Log(false, "adminPasswordDialog:"
										+ "PASSWORD_INCORRECT");
							}
						}
					});

			passwordDialog.setButton(AlertDialog.BUTTON_NEGATIVE,
					getString(R.string.cancel),
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							MandeUtility.Log(false, "adminPasswordDialog:"
									+ "cancel");
							input.setText("");
							return;
						}
					});

			passwordDialog.getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
			return passwordDialog;

		}
		return null;
	}

	private boolean loadSharedPreferencesFromFile(File src) {
		// this should probably be in a thread if it ever gets big
		boolean res = false;
		ObjectInputStream input = null;
		try {
			input = new ObjectInputStream(new FileInputStream(src));
			Editor prefEdit = PreferenceManager.getDefaultSharedPreferences(
					this).edit();
			prefEdit.clear();
			// first object is preferences
			Map<String, ?> entries = (Map<String, ?>) input.readObject();
			for (Entry<String, ?> entry : entries.entrySet()) {
				Object v = entry.getValue();
				String key = entry.getKey();

				if (v instanceof Boolean)
					prefEdit.putBoolean(key, ((Boolean) v).booleanValue());
				else if (v instanceof Float)
					prefEdit.putFloat(key, ((Float) v).floatValue());
				else if (v instanceof Integer)
					prefEdit.putInt(key, ((Integer) v).intValue());
				else if (v instanceof Long)
					prefEdit.putLong(key, ((Long) v).longValue());
				else if (v instanceof String)
					prefEdit.putString(key, ((String) v));
			}
			prefEdit.commit();

			// second object is admin options
			Editor adminEdit = getSharedPreferences(
					AdminPreferencesActivity.ADMIN_PREFERENCES, 0).edit();
			adminEdit.clear();
			// first object is preferences
			Map<String, ?> adminEntries = (Map<String, ?>) input.readObject();
			for (Entry<String, ?> entry : adminEntries.entrySet()) {
				Object v = entry.getValue();
				String key = entry.getKey();

				if (v instanceof Boolean)
					adminEdit.putBoolean(key, ((Boolean) v).booleanValue());
				else if (v instanceof Float)
					adminEdit.putFloat(key, ((Float) v).floatValue());
				else if (v instanceof Integer)
					adminEdit.putInt(key, ((Integer) v).intValue());
				else if (v instanceof Long)
					adminEdit.putLong(key, ((Long) v).longValue());
				else if (v instanceof String)
					adminEdit.putString(key, ((String) v));
			}
			adminEdit.commit();

			res = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (input != null) {
					input.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return res;
	}

}
