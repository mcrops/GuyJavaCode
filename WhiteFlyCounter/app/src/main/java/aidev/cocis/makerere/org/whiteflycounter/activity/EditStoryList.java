package aidev.cocis.makerere.org.whiteflycounter.activity;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;

import aidev.cocis.makerere.org.whiteflycounter.R;
import aidev.cocis.makerere.org.whiteflycounter.common.CompatibilityUtils;
import aidev.cocis.makerere.org.whiteflycounter.common.MandeUtility;
import aidev.cocis.makerere.org.whiteflycounter.preferences.PreferencesActivity;
import aidev.cocis.makerere.org.whiteflycounter.provider.FieldsProviderAPI.FieldsColumns;
import aidev.cocis.makerere.org.whiteflycounter.provider.StoryProviderAPI;
import aidev.cocis.makerere.org.whiteflycounter.provider.StoryProviderAPI.StoryColumns;

public class EditStoryList extends ListActivity implements
		OnLongClickListener {

	private static final String BUNDLE_SELECTED_ITEMS_KEY = "selected_items";
	private static final String BUNDLE_TOGGLED_KEY = "toggled";

	private static final int MENU_PREFERENCES = Menu.FIRST;
	private static final int MENU_SHOW_UNSENT = Menu.FIRST + 1;
	private static final int INSTANCE_UPLOADER = 0;


	private boolean mShowUnsent = true;
	private SimpleCursorAdapter mInstances;
	private ArrayList<Long> mSelected = new ArrayList<Long>();
	private boolean mRestored = false;

	int fieldID = 0;
	int ID = 0;
	
	private AlertDialog mAlertDialog;
	private SharedPreferences mAdminPreferences;
	
	private static final boolean EXIT = true;
	private static final boolean DO_NOT_EXIT = false;

	private static final String t = "StoryUploaderList";

	public Cursor getUnsentCursor() {
		// get all complete or failed submission instances
		String selection = "("+StoryColumns.STATUS + "=? ) AND "+StoryColumns.PROJECT_ID+ "=?";
		String selectionArgs[] = { StoryProviderAPI.STATUS_COMPLETE,String.valueOf(fieldID) };
		String sortOrder = StoryColumns.TITLE + " ASC";
		Cursor c = managedQuery(StoryColumns.CONTENT_URI, null, selection,
				selectionArgs, sortOrder);
		return c;
	}

	public Cursor getAllCursor() {
		// get all complete or failed submission instances
		String selection = "("+StoryColumns.STATUS + "=? " + ") AND "+StoryColumns.PROJECT_ID+ "=?";
		String selectionArgs[] = { StoryProviderAPI.STATUS_COMPLETE,String.valueOf(fieldID) };
		String sortOrder = StoryColumns.TITLE + " ASC";
		Cursor c = managedQuery(StoryColumns.CONTENT_URI, null, selection,
				selectionArgs, sortOrder);
		return c;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_story_list);

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
	

		Cursor c = mShowUnsent ? getUnsentCursor() : getAllCursor();

		String[] data = new String[] { StoryColumns.TITLE,
				StoryColumns.DISPLAY_SUBTEXT };
		int[] view = new int[] { R.id.text1, R.id.text2 };

		// render total instance view
		mInstances = new SimpleCursorAdapter(this,
				R.layout.two_item, c, data, view);

		setListAdapter(mInstances);
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		getListView().setItemsCanFocus(false);

		// set title
		setTitle(getString(R.string.app_name) + " > "
				+ getString(R.string.edit_data));

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
		int k = c.getInt(c.getColumnIndex(StoryColumns._ID));

		MandeUtility.Log(false, "onListItemClick:"+ Long.toString(k));
		
		Uri storyUri = ContentUris.withAppendedId(
				StoryColumns.CONTENT_URI, k);
		startActivity(new Intent("aidev.cocis.makerere.org.whiteflycounter.editstoryactivity", storyUri));



	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		long[] selectedArray = savedInstanceState
				.getLongArray(BUNDLE_SELECTED_ITEMS_KEY);
		for (int i = 0; i < selectedArray.length; i++)
			mSelected.add(selectedArray[i]);
		mRestored = true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		long[] selectedArray = new long[mSelected.size()];
		for (int i = 0; i < mSelected.size(); i++)
			selectedArray[i] = mSelected.get(i);
		outState.putLongArray(BUNDLE_SELECTED_ITEMS_KEY, selectedArray);
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
							EditStoryList.this.showUnsent();
							break;

						case 1: // show all
							MandeUtility.Log(false, "changeView:"+ "showAll");
							EditStoryList.this.showAll();
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
