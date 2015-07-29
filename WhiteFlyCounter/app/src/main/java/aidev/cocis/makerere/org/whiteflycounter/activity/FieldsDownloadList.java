package aidev.cocis.makerere.org.whiteflycounter.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import aidev.cocis.makerere.org.whiteflycounter.R;
import aidev.cocis.makerere.org.whiteflycounter.common.CompatibilityUtils;
import aidev.cocis.makerere.org.whiteflycounter.common.MandeUtility;
import aidev.cocis.makerere.org.whiteflycounter.listeners.FieldDownloaderListener;
import aidev.cocis.makerere.org.whiteflycounter.listeners.FieldListDownloaderListener;
import aidev.cocis.makerere.org.whiteflycounter.preferences.PreferencesActivity;
import aidev.cocis.makerere.org.whiteflycounter.tasks.DownloadFieldListTask;
import aidev.cocis.makerere.org.whiteflycounter.tasks.DownloadFieldsTask;
import aidev.cocis.makerere.org.whiteflycounter.webservices.field.WSField;

/**
 * 
 * @author Acellam Guy
 */
public class FieldsDownloadList extends ListActivity implements FieldListDownloaderListener,
        FieldDownloaderListener {
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
    private Button mDownloadButton;

    private DownloadFieldListTask mDownloadFieldListTask;
    private DownloadFieldsTask mDownloadFieldsTask;
    private Button mToggleButton;
    private Button mRefreshButton;

    private HashMap<Integer, WSField> mFieldNamesAndURLs = new HashMap<Integer,WSField>();
    private SimpleAdapter mFieldListAdapter;
    private ArrayList<HashMap<String, String>> mFieldList;

    private boolean mToggled = false;
    private int mSelectedCount = 0;

    private static final boolean EXIT = true;
    private static final boolean DO_NOT_EXIT = false;
    private boolean mShouldExit;
    private static final String SHOULD_EXIT = "shouldexit";

    public SharedPreferences preferences;

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remote_file_manage_list);
        setTitle(getString(R.string.app_name) + " > " + getString(R.string.get_fields));
        mAlertMsg = getString(R.string.please_wait);

        
    	preferences = getSharedPreferences(MandeUtility.PREFS_NAME, 0);
    	
    	
        // need white background before load
        getListView().setBackgroundColor(Color.WHITE);

        mDownloadButton = (Button) findViewById(R.id.add_button);
        mDownloadButton.setEnabled(selectedItemCount() > 0);
        mDownloadButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	// this is callled in downloadSelectedFiles():
            	//    Collect.getInstance().getActivityLogger().logAction(this, "downloadSelectedFiles", ...);
                downloadSelectedFiles();
                mToggled = false;
                clearChoices();
            }
        });

        mToggleButton = (Button) findViewById(R.id.toggle_button);
        mToggleButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // toggle selections of items to all or none
                ListView ls = getListView();
                mToggled = !mToggled;

                MandeUtility.Log(false, "toggleFieldCheckbox:"+ Boolean.toString(mToggled));

                for (int pos = 0; pos < ls.getCount(); pos++) {
                    ls.setItemChecked(pos, mToggled);
                }

                mDownloadButton.setEnabled(!(selectedItemCount() == 0));
            }
        });

        mRefreshButton = (Button) findViewById(R.id.refresh_button);
        mRefreshButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	 MandeUtility.Log(false, "refreshFields");

                mToggled = false;
                downloadFieldList();
                FieldsDownloadList.this.getListView().clearChoices();
                clearChoices();
            }
        });

        if (savedInstanceState != null) {
            // If the screen has rotated, the hashmap with the field ids and urls is passed here.
            if (savedInstanceState.containsKey(BUNDLE_PROJECT_MAP)) {
                mFieldNamesAndURLs =
                    (HashMap<Integer, WSField>) savedInstanceState
                            .getSerializable(BUNDLE_PROJECT_MAP);
            }

            // indicating whether or not select-all is on or off.
            if (savedInstanceState.containsKey(BUNDLE_TOGGLED_KEY)) {
                mToggled = savedInstanceState.getBoolean(BUNDLE_TOGGLED_KEY);
            }

            // how many items we've selected
            // Android should keep track of this, but broken on rotate...
            if (savedInstanceState.containsKey(BUNDLE_SELECTED_COUNT)) {
                mSelectedCount = savedInstanceState.getInt(BUNDLE_SELECTED_COUNT);
                mDownloadButton.setEnabled(!(mSelectedCount == 0));
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

        if (savedInstanceState != null && savedInstanceState.containsKey(PROJECTLIST)) {
            mFieldList =
                (ArrayList<HashMap<String, String>>) savedInstanceState.getSerializable(PROJECTLIST);
        } else {
            mFieldList = new ArrayList<HashMap<String, String>>();
        }

        if (getLastNonConfigurationInstance() instanceof DownloadFieldListTask) {
            mDownloadFieldListTask = (DownloadFieldListTask) getLastNonConfigurationInstance();
            mDownloadFieldListTask.preferences = preferences;
            if (mDownloadFieldListTask.getStatus() == AsyncTask.Status.FINISHED) {
                try {
                    dismissDialog(PROGRESS_DIALOG);
                } catch (IllegalArgumentException e) {
                    Log.i(t, "Attempting to close a dialog that was not previously opened");
                }
                mDownloadFieldsTask = null;
            }
        } else if (getLastNonConfigurationInstance() instanceof DownloadFieldsTask) {
            mDownloadFieldsTask = (DownloadFieldsTask) getLastNonConfigurationInstance();
            if (mDownloadFieldsTask.getStatus() == AsyncTask.Status.FINISHED) {
                try {
                    dismissDialog(PROGRESS_DIALOG);
                } catch (IllegalArgumentException e) {
                    Log.i(t, "Attempting to close a dialog that was not previously opened");
                }
                mDownloadFieldsTask = null;
            }
        } else if (getLastNonConfigurationInstance() == null) {
            // first time, so get the fieldlist
            downloadFieldList();
        }

        String[] data = new String[] {
                PROJECTNAME,PROJECTID_DISPLAY,PROJECTDETAIL_ID 
        };
        int[] view = new int[] {
               R.id.text1, R.id.text2,R.id.text3
        };

        mFieldListAdapter =
            new SimpleAdapter(this, mFieldList, R.layout.two_item_multiple_choice, data, view);
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        getListView().setItemsCanFocus(false);
        setListAdapter(mFieldListAdapter);
    }


   
    private void clearChoices() {
        FieldsDownloadList.this.getListView().clearChoices();
        mDownloadButton.setEnabled(false);
    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		mDownloadButton.setEnabled(!(selectedItemCount() == 0));

		Object o = getListAdapter().getItem(position);
		@SuppressWarnings("unchecked")
		HashMap<String, String> item = (HashMap<String, String>) o;
        WSField detail = mFieldNamesAndURLs.get(item.get(PROJECTDETAIL_ID));

        MandeUtility.Log(false, "onListItemClick");
    }


    /**
     * Starts the download task and shows the progress dialog.
     */
    private void downloadFieldList() {
        ConnectivityManager connectivityManager =
            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

        if (ni == null || !ni.isConnected()) {
            Toast.makeText(this, R.string.no_connection, Toast.LENGTH_SHORT).show();
        } else {

            mFieldNamesAndURLs = new HashMap<Integer, WSField>();
            if (mProgressDialog != null) {
                // This is needed because onPrepareDialog() is broken in 1.6.
                mProgressDialog.setMessage(getString(R.string.please_wait));
            }
            showDialog(PROGRESS_DIALOG);

            if (mDownloadFieldListTask != null &&
            	mDownloadFieldListTask.getStatus() != AsyncTask.Status.FINISHED) {
            	return; // we are already doing the download!!!
            } else if (mDownloadFieldListTask != null) {
            	mDownloadFieldListTask.setDownloaderListener(null);
            	mDownloadFieldListTask.cancel(true);
            	mDownloadFieldListTask = null;
            }

            mDownloadFieldListTask = new DownloadFieldListTask();
            mDownloadFieldListTask.preferences = preferences;
            mDownloadFieldListTask.setDownloaderListener(this);
            mDownloadFieldListTask.execute();
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(BUNDLE_TOGGLED_KEY, mToggled);
        outState.putInt(BUNDLE_SELECTED_COUNT, selectedItemCount());
        outState.putSerializable(BUNDLE_PROJECT_MAP, mFieldNamesAndURLs);
        outState.putString(DIALOG_TITLE, mAlertTitle);
        outState.putString(DIALOG_MSG, mAlertMsg);
        outState.putBoolean(DIALOG_SHOWING, mAlertShowing);
        outState.putBoolean(SHOULD_EXIT, mShouldExit);
        outState.putSerializable(PROJECTLIST, mFieldList);
    }


    /**
     * returns the number of items currently selected in the list.
     *
     * @return
     */
    private int selectedItemCount() {
        int count = 0;
        SparseBooleanArray sba = getListView().getCheckedItemPositions();
        for (int i = 0; i < getListView().getCount(); i++) {
            if (sba.get(i, false)) {
                count++;
            }
        }
        return count;
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
            	 MandeUtility.Log(false,"onCreateDialog.PROGRESS_DIALOG:show");
                mProgressDialog = new ProgressDialog(this);
                DialogInterface.OnClickListener loadingButtonListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        	 MandeUtility.Log(false, "onCreateDialog.PROGRESS_DIALOG:OK");
                            dialog.dismiss();
                            // we use the same progress dialog for both
                            // so whatever isn't null is running
                            if (mDownloadFieldListTask != null) {
                                mDownloadFieldListTask.setDownloaderListener(null);
                                mDownloadFieldListTask.cancel(true);
                                mDownloadFieldListTask = null;
                            }
                            if (mDownloadFieldsTask != null) {
                                mDownloadFieldsTask.setDownloaderListener(null);
                                mDownloadFieldsTask.cancel(true);
                                mDownloadFieldsTask = null;
                            }
                        }
                    };
                mProgressDialog.setTitle(getString(R.string.downloading_data));
                mProgressDialog.setMessage(mAlertMsg);
                mProgressDialog.setIcon(android.R.drawable.ic_dialog_info);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setButton(getString(R.string.cancel), loadingButtonListener);
                return mProgressDialog;
            case AUTH_DIALOG:
                /*MandeUtility.Log(false,"onCreateDialog.AUTH_DIALOG:show");
                AlertDialog.Builder b = new AlertDialog.Builder(this);

                LayoutInflater factory = LayoutInflater.from(this);
                final View dialogView = factory.inflate(R.layout.server_auth_dialog, null);

                // Get the server, username, and password from the settings
                SharedPreferences settings =
                    PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                String server =
                    settings.getString(PreferencesActivity.KEY_SERVER_URL,
                        getString(R.string.default_server_url));

                String fieldListUrl = getString(R.string.default_odk_fieldlist);
                final String url =
                    server + settings.getString(PreferencesActivity.KEY_PROJECTLIST_URL, fieldListUrl);
                Log.i(t, "Trying to get fieldList from: " + url);

                EditText username = (EditText) dialogView.findViewById(R.id.username_edit);
                String storedUsername = settings.getString(PreferencesActivity.KEY_USERNAME, null);
                username.setText(storedUsername);

                EditText password = (EditText) dialogView.findViewById(R.id.password_edit);
                String storedPassword = settings.getString(PreferencesActivity.KEY_PASSWORD, null);
                password.setText(storedPassword);

                b.setTitle(getString(R.string.server_requires_auth));
                b.setMessage(getString(R.string.server_auth_credentials, url));
                b.setView(dialogView);
                b.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Collect.getInstance().getActivityLogger().logAction(this, "onCreateDialog.AUTH_DIALOG", "OK");

                        EditText username = (EditText) dialogView.findViewById(R.id.username_edit);
                        EditText password = (EditText) dialogView.findViewById(R.id.password_edit);

                        Uri u = Uri.parse(url);

                        WebUtils.addCredentials(username.getText().toString(), password.getText()
                                .toString(), u.getHost());
                        downloadFieldList();
                    }
                });
                b.setNegativeButton(getString(R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Collect.getInstance().getActivityLogger().logAction(this, "onCreateDialog.AUTH_DIALOG", "Cancel");
                            finish();
                        }
                    });

                b.setCancelable(false);
                mAlertShowing = false;
                return b.create();*/
        }
        return null;
    }


    /**
     * starts the task to download the selected fields, also shows progress dialog
     */
    @SuppressWarnings("unchecked")
    private void downloadSelectedFiles() {
        int totalCount = 0;
        ArrayList<WSField> filesToDownload = new ArrayList<WSField>();

        SparseBooleanArray sba = getListView().getCheckedItemPositions();
        for (int i = 0; i < getListView().getCount(); i++) {
            if (sba.get(i, false)) {
                HashMap<Integer, String> item =
                    (HashMap<Integer, String>) getListAdapter().getItem(i);
                
                String projID = item.get(PROJECTDETAIL_ID);
                String IDSubStr =projID.substring(3, projID.length());
                WSField proj = mFieldNamesAndURLs.get(Integer.parseInt(IDSubStr));
                
                
                filesToDownload.add(proj);
                
            }
        }
        totalCount = filesToDownload.size();

       MandeUtility.Log(false, "downloadSelectedFiles:"+Integer.toString(totalCount));

        if (totalCount > 0) {
            // show dialog box
            showDialog(PROGRESS_DIALOG);

            mDownloadFieldsTask = new DownloadFieldsTask();
            mDownloadFieldsTask.ctx = getApplicationContext();
            mDownloadFieldsTask.setDownloaderListener(this);
            mDownloadFieldsTask.execute(filesToDownload);
        } else {
            Toast.makeText(getApplicationContext(), R.string.noselect_error, Toast.LENGTH_SHORT)
                    .show();
        }
    }


    @Override
    public Object onRetainNonConfigurationInstance() {
        if (mDownloadFieldsTask != null) {
            return mDownloadFieldsTask;
        } else {
            return mDownloadFieldListTask;
        }
    }


    @Override
    protected void onDestroy() {
        if (mDownloadFieldListTask != null) {
            mDownloadFieldListTask.setDownloaderListener(null);
        }
        if (mDownloadFieldsTask != null) {
            mDownloadFieldsTask.setDownloaderListener(null);
        }
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        if (mDownloadFieldListTask != null) {
            mDownloadFieldListTask.setDownloaderListener(this);
        }
        if (mDownloadFieldsTask != null) {
            mDownloadFieldsTask.setDownloaderListener(this);
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
     * Called when the field list has finished downloading. results will either contain a set of
     * <fieldname, fielddetails> tuples, or one tuple of DL.ERROR.MSG and the associated message.
     *
     * @param result
     */
    public void fieldListDownloadingComplete(HashMap<Integer, WSField> result) {
        dismissDialog(PROGRESS_DIALOG);
        mDownloadFieldListTask.setDownloaderListener(null);
        mDownloadFieldListTask = null;

        if (result == null) {
            Log.e(t, "Fieldlist Downloading returned null.  That shouldn't happen");
            // Just displayes "error occured" to the user, but this should never happen.
            createAlertDialog(getString(R.string.load_remote_field_error),
                getString(R.string.error_occured), EXIT);
            return;
        }

        if (result.containsKey(DownloadFieldListTask.DL_AUTH_REQUIRED)) {
            // need authorization
            showDialog(AUTH_DIALOG);
        } else if (result.containsKey(DownloadFieldListTask.DL_ERROR_MSG)) {
            // Download failed
            String dialogMessage =
                getString(R.string.list_failed_with_error,
                    result.get(DownloadFieldListTask.DL_ERROR_MSG).Name);//TODO
            String dialogTitle = getString(R.string.load_remote_field_error);
            createAlertDialog(dialogTitle, dialogMessage, DO_NOT_EXIT);
        } else {
            // Everything worked. Clear the list and add the results.
            mFieldNamesAndURLs = result;

            mFieldList.clear();

            ArrayList<Integer> ids = new ArrayList<Integer>(mFieldNamesAndURLs.keySet());
            for (int i = 0; i < result.size(); i++) {
            	Integer fieldID = ids.get(i);
            	WSField details = mFieldNamesAndURLs.get(fieldID);
                HashMap<String, String> item = new HashMap<String, String>();
                item.put(PROJECTNAME, details.Name);
                item.put(PROJECTID_DISPLAY, details.Description);
                item.put(PROJECTDETAIL_ID, "ID:"+fieldID.toString());

                // Insert the new field in alphabetical order.
                if (mFieldList.size() == 0) {
                    mFieldList.add(item);
                } else {
                    int j;
                    for (j = 0; j < mFieldList.size(); j++) {
                        HashMap<String, String> compareMe = mFieldList.get(j);
                        String name = compareMe.get(PROJECTNAME);
                        if (name.compareTo(mFieldNamesAndURLs.get(ids.get(i)).Name) > 0) {
                            break;
                        }
                    }
                    mFieldList.add(j, item);
                }
            }
            mFieldListAdapter.notifyDataSetChanged();
        }
    }


    /**
     * Creates an alert dialog with the given tite and message. If shouldExit is set to true, the
     * activity will exit when the user clicks "ok".
     *
     * @param title
     * @param message
     * @param shouldExit
     */
    private void createAlertDialog(String title, String message, final boolean shouldExit) {
        MandeUtility.Log(false,"createAlertDialog:show");
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


    @Override
    public void progressUpdate(String currentFile, int progress, int total) {
        mAlertMsg = getString(R.string.fetching_field, currentFile, progress, total);
        mProgressDialog.setMessage(mAlertMsg);
    }


    @Override
    public void fieldsDownloadingComplete(HashMap<WSField, String> result) {
        if (mDownloadFieldsTask != null) {
            mDownloadFieldsTask.setDownloaderListener(null);
        }

        if (mProgressDialog.isShowing()) {
            // should always be true here
            mProgressDialog.dismiss();
        }

        Set<WSField> keys = result.keySet();
        
        StringBuilder b = new StringBuilder();
        for (WSField k : keys) {
            b.append(k.Name);
            b.append("\n\n");
        }

        createAlertDialog(getString(R.string.download_fields_result), b.toString().trim(), EXIT);
    }
    
	
}
