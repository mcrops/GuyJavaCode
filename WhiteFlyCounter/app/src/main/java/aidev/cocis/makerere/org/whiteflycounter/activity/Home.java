package aidev.cocis.makerere.org.whiteflycounter.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Map.Entry;

import aidev.cocis.makerere.org.whiteflycounter.R;
import aidev.cocis.makerere.org.whiteflycounter.common.Constants;
import aidev.cocis.makerere.org.whiteflycounter.common.MandeUtility;
import aidev.cocis.makerere.org.whiteflycounter.preferences.AdminPreferencesActivity;
import aidev.cocis.makerere.org.whiteflycounter.preferences.PreferencesActivity;
import aidev.cocis.makerere.org.whiteflycounter.provider.FieldsProvider;
import aidev.cocis.makerere.org.whiteflycounter.provider.FieldsProviderAPI.FieldsColumns;
import aidev.cocis.makerere.org.whiteflycounter.provider.QuestionProvider;
import aidev.cocis.makerere.org.whiteflycounter.provider.ReportingPeriodProvider;
import aidev.cocis.makerere.org.whiteflycounter.provider.StoryProvider;
import aidev.cocis.makerere.org.whiteflycounter.provider.TemplateProvider;

public class Home extends Activity {
	// menu options
		private static final int MENU_PREFERENCES = Menu.FIRST;
		private static final int MENU_ADMIN = Menu.FIRST + 1;
		

		private static final int PASSWORD_DIALOG = 1;
		
	// buttons
		private Button mListFieldsButton;
		private Button mManageFieldsButton;
		private Button mResetDatabaseButton;
		private Button mGetFieldsButton;
		private String version;
		private View mReviewSpacer;
		private View mGetFormsSpacer;

		private AlertDialog mAlertDialog;
		private SharedPreferences mAdminPreferences;

		private int mCompletedCount;
		private int mSavedCount;

		private Cursor mFinalizedCursor;
		private Cursor mSavedCursor;
		
		private IncomingHandler mHandler = new IncomingHandler(this);

		private MyContentObserver mContentObserver = new MyContentObserver();
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		{
			// dynamically construct the title
			PackageInfo pInfo;
			try {
				pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
				version = pInfo.versionName;
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			TextView mainMenuMessageLabel = (TextView) findViewById(R.id.main_menu_header);
			mainMenuMessageLabel.setText( getResources().getString(R.string.app_name)+" "+ version);
		}

		setTitle(getString(R.string.app_name) + " > "
				+ getString(R.string.main_menu));
		
		File f = new File(Constants.WHITEFLYCOUNTER_ROOT + "/mande.settings");
		if (f.exists()) {
			boolean success = loadSharedPreferencesFromFile(f);
			if (success) {
				Toast.makeText(this,
						"Settings successfully loaded from file",
						Toast.LENGTH_LONG).show();
				f.delete();
			} else {
				Toast.makeText(
						this,
						"Sorry, settings file is corrupt and should be deleted or replaced",
						Toast.LENGTH_LONG).show();
			}
		}
		
		mReviewSpacer = findViewById(R.id.review_spacer);
		mGetFormsSpacer = findViewById(R.id.get_forms_spacer);

		mAdminPreferences = this.getSharedPreferences(
				AdminPreferencesActivity.ADMIN_PREFERENCES, 0);

		// enter data button. expects a result.
		mListFieldsButton = (Button) findViewById(R.id.list_fields);
		mListFieldsButton.setText(getString(R.string.list_fields_button));
		mListFieldsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MandeUtility.Log(false, "List Fields:Click");
				Intent i = new Intent(getApplicationContext(),
						FieldChooserList.class);
				startActivity(i);
			}
		});

		// review data button. expects a result.
		mGetFieldsButton = (Button) findViewById(R.id.get_new_fields);
		mGetFieldsButton.setText(getString(R.string.get_fields_button));
		mGetFieldsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MandeUtility.Log(false, "Get Fields:click");
				Intent i = new Intent(getApplicationContext(),
						NewField.class);
				startActivity(i);
			}
		});

		// send data button. expects a result.
		mManageFieldsButton = (Button) findViewById(R.id.manage_fields);
		mManageFieldsButton.setText(getString(R.string.manage_fields_button));
		mManageFieldsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MandeUtility.Log(false, "Delete Fields:click");
				/*Intent i = new Intent(getApplicationContext(),
						SendStories.class);
				startActivity(i);*/
			}
		});

		// send data button. expects a result.
				mResetDatabaseButton= (Button) findViewById(R.id.reset_database);
				mResetDatabaseButton.setText(getString(R.string.reset_database_button));
				mResetDatabaseButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						MandeUtility.Log(false, "Reset datbase:click");
						getApplicationContext().deleteDatabase(FieldsProvider.DATABASE_NAME);
						getApplicationContext().deleteDatabase(ReportingPeriodProvider.DATABASE_NAME);
						getApplicationContext().deleteDatabase(QuestionProvider.DATABASE_NAME);
						getApplicationContext().deleteDatabase(StoryProvider.DATABASE_NAME);
						getApplicationContext().deleteDatabase(TemplateProvider.DATABASE_NAME);
						
						//getContentResolver().query(FieldsColumns.CONTENT_URI, null, null, null, null).;
					}
				});

		// count for finalized instances
				try{

		mFinalizedCursor = managedQuery(FieldsColumns.CONTENT_URI, null,
				null, null, null);
		startManagingCursor(mFinalizedCursor);
		mCompletedCount = mFinalizedCursor.getCount();
		mFinalizedCursor.registerContentObserver(mContentObserver);
		
		mSavedCount = mFinalizedCursor.getCount();
		// don't need to set a content observer because it can't change in the
		// background

		
			updateButtons();	
		}catch(Exception ex){}
	

	}
	private void updateButtons() {
		//mFinalizedCursor = managedQuery(FieldsColumns.CONTENT_URI, null,
		//		null, null, null);
		String[] fieldion = { FieldsColumns._ID, FieldsColumns.WHITEFLYCOUNTER_PROJECT_ID };
		
		mFinalizedCursor = getContentResolver().query(FieldsColumns.CONTENT_URI, fieldion, null, null, null);
		//startManagingCursor(mFinalizedCursor);
		mCompletedCount = mFinalizedCursor.getCount();
		mFinalizedCursor.registerContentObserver(mContentObserver);
		
		mCompletedCount = mFinalizedCursor.getCount();
		if (mCompletedCount > 0) {
			mListFieldsButton.setText(getString(R.string.select_field_button,
					mCompletedCount));
		} else {
			mListFieldsButton.setText(getString(R.string.select_field));
		}


	}
	
	 /* notifies us that something changed
	 *
	 */
	private class MyContentObserver extends ContentObserver {

		public MyContentObserver() {
			super(null);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			mHandler.sendEmptyMessage(0);
		}
	}

	/*
	 * Used to prevent memory leaks
	 */
	static class IncomingHandler extends Handler {
		private final WeakReference<Home> mTarget;

		IncomingHandler(Home target) {
			mTarget = new WeakReference<Home>(target);
		}

		@Override
		public void handleMessage(Message msg) {
			Home target = mTarget.get();
			if (target != null) {
				target.updateButtons();
			}
		}
	}
	@Override
	protected void onPause() {
		super.onPause();
		if (mAlertDialog != null && mAlertDialog.isShowing()) {
			mAlertDialog.dismiss();
		}
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
			MandeUtility.Log(false,"onOptionsItemSelected:MENU_ADMIN");
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
										Home.this,
										getString(R.string.admin_password_incorrect),
										Toast.LENGTH_SHORT).show();
								MandeUtility.Log(false, "adminPasswordDialog:" +
										"PASSWORD_INCORRECT");
							}
						}
					});

			passwordDialog.setButton(AlertDialog.BUTTON_NEGATIVE,
					getString(R.string.cancel),
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							MandeUtility.Log(false, "adminPasswordDialog:"+
											"cancel");
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
			Editor adminEdit = getSharedPreferences(AdminPreferencesActivity.ADMIN_PREFERENCES, 0).edit();
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
