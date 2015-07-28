package aidev.cocis.makerere.org.whiteflycounter.activity;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.ArrayList;

import aidev.cocis.makerere.org.whiteflycounter.R;
import aidev.cocis.makerere.org.whiteflycounter.common.CompatibilityUtils;
import aidev.cocis.makerere.org.whiteflycounter.common.MandeUtility;
import aidev.cocis.makerere.org.whiteflycounter.preferences.PreferencesActivity;
import aidev.cocis.makerere.org.whiteflycounter.provider.FieldsProviderAPI.FieldsColumns;
import aidev.cocis.makerere.org.whiteflycounter.provider.StoryProviderAPI;
import aidev.cocis.makerere.org.whiteflycounter.provider.StoryProviderAPI.StoryColumns;
import aidev.cocis.makerere.org.whiteflycounter.receivers.NetworkReceiver;

public class StoryUploaderList extends ListActivity implements
		OnLongClickListener {

	private static final String BUNDLE_SELECTED_ITEMS_KEY = "selected_items";
	private static final String BUNDLE_TOGGLED_KEY = "toggled";

	private static final int MENU_PREFERENCES = Menu.FIRST;
	private static final int MENU_SHOW_UNSENT = Menu.FIRST + 1;
	private static final int INSTANCE_UPLOADER = 0;

	private Button mUploadButton;
	private Button mToggleButton;

	private boolean mShowUnsent = true;
	private SimpleCursorAdapter mInstances;
	private ArrayList<Long> mSelected = new ArrayList<Long>();
	private boolean mRestored = false;
	private boolean mToggled = false;

	int fieldID = 0;
	int ID = 0;
	
	private AlertDialog mAlertDialog;
	private SharedPreferences mAdminPreferences;
	
	private static final boolean EXIT = true;
	private static final boolean DO_NOT_EXIT = false;

	private static final String t = "StoryUploaderList";

	public Cursor getUnsentCursor() {
		// get all complete or failed submission instances
		String selection = "("+StoryColumns.STATUS + "=? or " + StoryColumns.STATUS
				+ "=? ) AND "+StoryColumns.PROJECT_ID+ "=?";
		String selectionArgs[] = { StoryProviderAPI.STATUS_FINALIZED,
				StoryProviderAPI.STATUS_SUBMISSION_FAILED,String.valueOf(fieldID) };
		String sortOrder = StoryColumns.TITLE + " ASC";
		Cursor c = managedQuery(StoryColumns.CONTENT_URI, null, selection,
				selectionArgs, sortOrder);
		return c;
	}

	public Cursor getAllCursor() {
		// get all complete or failed submission instances
		String selection = "("+StoryColumns.STATUS + "=? or " + StoryColumns.STATUS
				+ "=? or " + StoryColumns.STATUS+ "=? ) AND "+StoryColumns.PROJECT_ID+ "=?";
		String selectionArgs[] = { StoryProviderAPI.STATUS_FINALIZED,
				StoryProviderAPI.STATUS_SUBMISSION_FAILED,
				StoryProviderAPI.STATUS_SUBMITTED,String.valueOf(fieldID) };
		String sortOrder = StoryColumns.TITLE + " ASC";
		Cursor c = managedQuery(StoryColumns.CONTENT_URI, null, selection,
				selectionArgs, sortOrder);
		return c;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.instance_uploader_list);

      try{
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

  							ID = fieldCursor.getInt(fieldCursor
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
      }catch(Exception ex){
    	  
      }
		// set up long click listener

		mUploadButton = (Button) findViewById(R.id.upload_button);
		mUploadButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

				if (NetworkReceiver.running == true) {
					Toast.makeText(
							StoryUploaderList.this,
							"Background send running, please try again shortly",
							Toast.LENGTH_SHORT).show();
				} else if (ni == null || !ni.isConnected()) {
					MandeUtility.Log(true, "uploadButton:noConnection");

					Toast.makeText(StoryUploaderList.this,
							R.string.no_connection, Toast.LENGTH_SHORT).show();
				} else {
					MandeUtility.Log(true, "uploadButton:"+
									Integer.toString(mSelected.size()));

					if (mSelected.size() > 0) {
						// items selected
						uploadSelectedFiles();
						mToggled = false;
						mSelected.clear();
						StoryUploaderList.this.getListView().clearChoices();
						mUploadButton.setEnabled(false);
					} else {
						// no items selected
						Toast.makeText(getApplicationContext(),
								getString(R.string.noselect_error),
								Toast.LENGTH_SHORT).show();
					}
				}
			}
		});

		mToggleButton = (Button) findViewById(R.id.toggle_button);
		mToggleButton.setLongClickable(true);
		mToggleButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// toggle selections of items to all or none
				ListView ls = getListView();
				mToggled = !mToggled;

				MandeUtility.Log(true, "toggleButton:"+
								Boolean.toString(mToggled));
				// remove all items from selected list
				mSelected.clear();
				for (int pos = 0; pos < ls.getCount(); pos++) {
					ls.setItemChecked(pos, mToggled);
					// add all items if mToggled sets to select all
					if (mToggled)
						mSelected.add(ls.getItemIdAtPosition(pos));
				}
				mUploadButton.setEnabled(!(mSelected.size() == 0));

			}
		});
		mToggleButton.setOnLongClickListener(this);

		Cursor c = mShowUnsent ? getUnsentCursor() : getAllCursor();

		String[] data = new String[] { StoryColumns.TITLE,
				StoryColumns.DISPLAY_SUBTEXT };
		int[] view = new int[] { R.id.text1, R.id.text2 };

		// render total instance view
		mInstances = new SimpleCursorAdapter(this,
				R.layout.two_item_multiple_choice, c, data, view);

		setListAdapter(mInstances);
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		getListView().setItemsCanFocus(false);
		mUploadButton.setEnabled(!(mSelected.size() == 0));

		// set title
		setTitle(getString(R.string.app_name) + " > "
				+ getString(R.string.send_data));

		// if current activity is being reinitialized due to changing
		// orientation restore all check
		// marks for ones selected
		if (mRestored) {
			ListView ls = getListView();
			for (long id : mSelected) {
				for (int pos = 0; pos < ls.getCount(); pos++) {
					if (id == ls.getItemIdAtPosition(pos)) {
						ls.setItemChecked(pos, true);
						break;
					}
				}

			}
			mRestored = false;
		}
	}


	private void uploadSelectedFiles() {
		// send list of _IDs.
		long[] instanceIDs = new long[mSelected.size()];
		for (int i = 0; i < mSelected.size(); i++) {
			instanceIDs[i] = mSelected.get(i);
		}

		Intent i = new Intent(this, StoryUploaderActivity.class);
		i.putExtra("instances", instanceIDs);
		startActivityForResult(i, INSTANCE_UPLOADER);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MandeUtility.Log(false, "onCreateOptionsMenu:show");
		super.onCreateOptionsMenu(menu);

		CompatibilityUtils.setShowAsAction(
				menu.add(0, MENU_PREFERENCES, 0, R.string.general_preferences)
						.setIcon(R.drawable.ic_menu_preferences),
				MenuItem.SHOW_AS_ACTION_NEVER);
		CompatibilityUtils.setShowAsAction(
				menu.add(0, MENU_SHOW_UNSENT, 1, R.string.change_view).setIcon(
						R.drawable.ic_menu_manage),
				MenuItem.SHOW_AS_ACTION_NEVER);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case MENU_PREFERENCES:
			MandeUtility.Log(false, "onMenuItemSelected:MENU_PREFERENCES");
			createPreferencesMenu();
			return true;
		case MENU_SHOW_UNSENT:
			MandeUtility.Log(false, "onMenuItemSelected:MENU_SHOW_UNSENT");
			showSentAndUnsentChoices();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	private void createPreferencesMenu() {
		Intent i = new Intent(this, PreferencesActivity.class);
		startActivity(i);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		// get row id from db
		Cursor c = (Cursor) getListAdapter().getItem(position);
		long k = c.getLong(c.getColumnIndex(StoryColumns._ID));

		MandeUtility.Log(false, "onListItemClick:"+ Long.toString(k));

		// add/remove from selected list
		if (mSelected.contains(k))
			mSelected.remove(k);
		else
			mSelected.add(k);

		mUploadButton.setEnabled(!(mSelected.size() == 0));

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		long[] selectedArray = savedInstanceState
				.getLongArray(BUNDLE_SELECTED_ITEMS_KEY);
		for (int i = 0; i < selectedArray.length; i++)
			mSelected.add(selectedArray[i]);
		mToggled = savedInstanceState.getBoolean(BUNDLE_TOGGLED_KEY);
		mRestored = true;
		mUploadButton.setEnabled(selectedArray.length > 0);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		long[] selectedArray = new long[mSelected.size()];
		for (int i = 0; i < mSelected.size(); i++)
			selectedArray[i] = mSelected.get(i);
		outState.putLongArray(BUNDLE_SELECTED_ITEMS_KEY, selectedArray);
		outState.putBoolean(BUNDLE_TOGGLED_KEY, mToggled);
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
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		if (resultCode == RESULT_CANCELED) {
			return;
		}
		switch (requestCode) {
		// returns with a form path, start entry
		case INSTANCE_UPLOADER:
			if (intent.getBooleanExtra("instances", false)) {
				mSelected.clear();
				getListView().clearChoices();
				if (mInstances.isEmpty()) {
					finish();
				}
			}
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, intent);
	}

	private void showUnsent() {
		mShowUnsent = true;
		Cursor c = mShowUnsent ? getUnsentCursor() : getAllCursor();
		Cursor old = mInstances.getCursor();
		try {
			mInstances.changeCursor(c);
		} finally {
			if (old != null) {
				old.close();
				this.stopManagingCursor(old);
			}
		}
		getListView().invalidate();
	}

	private void showAll() {
		mShowUnsent = false;
		Cursor c = mShowUnsent ? getUnsentCursor() : getAllCursor();
		Cursor old = mInstances.getCursor();
		try {
			mInstances.changeCursor(c);
		} finally {
			if (old != null) {
				old.close();
				this.stopManagingCursor(old);
			}
		}
		getListView().invalidate();
	}

	@Override
	public boolean onLongClick(View v) {
		MandeUtility.Log(false, "toggleButton.longClick:"+
						Boolean.toString(mToggled));
		return showSentAndUnsentChoices();
	}

	private boolean showSentAndUnsentChoices() {
		/**
		 * Create a dialog with options to save and exit, save, or quit without
		 * saving
		 */
		String[] items = { getString(R.string.show_unsent_stories),
				getString(R.string.show_sent_and_unsent_stories) };

		MandeUtility.Log(false, "changeView:"+ "show");

		AlertDialog alertDialog = new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setTitle(getString(R.string.change_view))
				.setNeutralButton(getString(R.string.cancel),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								MandeUtility.Log(false, "changeView:cancel");
								dialog.cancel();
							}
						})
				.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {

						case 0: // show unsent
							MandeUtility.Log(false, "changeView:showUnsent");
							StoryUploaderList.this.showUnsent();
							break;

						case 1: // show all
							MandeUtility.Log(false, "changeView:"+ "showAll");
							StoryUploaderList.this.showAll();
							break;

						case 2:// do nothing
							break;
						}
					}
				}).create();
		alertDialog.show();
		return true;
	}

}
