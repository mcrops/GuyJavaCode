package aidev.cocis.makerere.org.whiteflycounter.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import aidev.cocis.makerere.org.whiteflycounter.R;
import aidev.cocis.makerere.org.whiteflycounter.common.CommonFunctions;
import aidev.cocis.makerere.org.whiteflycounter.common.CompatibilityUtils;
import aidev.cocis.makerere.org.whiteflycounter.common.Constants;
import aidev.cocis.makerere.org.whiteflycounter.common.FileUtils;
import aidev.cocis.makerere.org.whiteflycounter.common.MandeUtility;
import aidev.cocis.makerere.org.whiteflycounter.listeners.FileDeleteListener;
import aidev.cocis.makerere.org.whiteflycounter.listeners.StoryAttachmentListener;
import aidev.cocis.makerere.org.whiteflycounter.preferences.PreferencesActivity;
import aidev.cocis.makerere.org.whiteflycounter.provider.QuestionAnswerProviderAPI.QuestionAnswerColumns;
import aidev.cocis.makerere.org.whiteflycounter.tasks.DeleteFileTask;
import aidev.cocis.makerere.org.whiteflycounter.tasks.LoadAttachmentListTask;

public class AttachmentList extends ListActivity implements StoryAttachmentListener, FileDeleteListener {
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

	private static final String FILENAME = "fieldname";

	private int questionanswerid = 0;
	private int storyid = 0;

	private String mAlertMsg;
	private boolean mAlertShowing = false;
	private String mAlertTitle;

	private AlertDialog mAlertDialog;
	private ProgressDialog mProgressDialog;
	private Button mDownloadButton;

	private Button mToggleButton;
	private ImageButton attachmentButton;
	
	 private HashMap<String, String> mFieldNamesAndURLs = new HashMap<String,String>();

	private SimpleAdapter mFieldListAdapter;
	private ArrayList<HashMap<String, String>> mFieldList;

	private boolean mToggled = false;
	private int mSelectedCount = 0;

	private static final boolean EXIT = true;
	private static final boolean DO_NOT_EXIT = false;
	private boolean mShouldExit;
	private static final String SHOULD_EXIT = "shouldexit";

	public static final int IMAGE_CAPTURE = 1;
	public static final int BARCODE_CAPTURE = 2;
	public static final int AUDIO_CAPTURE = 3;
	public static final int VIDEO_CAPTURE = 4;
	public static final int IMAGE_CHOOSER = 5;
	public static final int AUDIO_CHOOSER = 6;
	public static final int VIDEO_CHOOSER = 7;
	public static final int FILE_CHOOSER = 8;

	private LoadAttachmentListTask mLoadAttachmentTask;
    private DeleteFileTask mDeleteFileTask;

	public SharedPreferences preferences;
	
	

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.attachment_manage_list);
		setTitle(getString(R.string.app_name) + " > "
				+ getString(R.string.qa_attachment));

		Intent intent = getIntent();
		if (intent != null) {
			Uri uri = intent.getData();

			if (getContentResolver().getType(uri) == QuestionAnswerColumns.CONTENT_ITEM_TYPE) {
				{
					Cursor questionAnswerCursor = null;
					try {
						questionAnswerCursor = getContentResolver().query(uri,
								null, null, null, null);
						if (questionAnswerCursor.getCount() != 1) {
							this.createErrorDialog("Bad URI: " + uri, EXIT);
							return;
						} else {
							questionAnswerCursor.moveToFirst();

							questionanswerid = questionAnswerCursor
									.getInt(questionAnswerCursor
											.getColumnIndex(QuestionAnswerColumns._ID));

							storyid = questionAnswerCursor
									.getInt(questionAnswerCursor
											.getColumnIndex(QuestionAnswerColumns.STORY_ID));
							

							try {
								CommonFunctions.createStoryDir(String.valueOf(storyid));
								CommonFunctions.createQuestionAnswerDir(
										String.valueOf(storyid),
										String.valueOf(questionanswerid));
							} catch (Exception ex) {

								MandeUtility.Log(true, ex.getMessage());

							}
							

						}
					} finally {
						if (questionAnswerCursor != null) {
							questionAnswerCursor.close();
						}
					}

				}

			} else {
				Log.e(t, "unrecognized URI");
				this.createErrorDialog("unrecognized URI: " + uri, EXIT);
				return;
			}

		}

		mAlertMsg = getString(R.string.please_wait);

		preferences = getSharedPreferences(MandeUtility.PREFS_NAME, 0);
		
		// need white background before load
        getListView().setBackgroundColor(Color.WHITE);
        
        mDownloadButton = (Button) findViewById(R.id.remove_button);
        mDownloadButton.setEnabled(selectedItemCount() > 0);
        mDownloadButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	
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

		attachmentButton = (ImageButton) findViewById(R.id.attachment_button);

		attachmentButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent i = new Intent(Intent.ACTION_GET_CONTENT);
				i.setType("*/*");

				try {

					startActivityForResult(i, AttachmentList.IMAGE_CHOOSER);
				} catch (ActivityNotFoundException e) {
					Toast.makeText(
							getApplicationContext(),
							getApplicationContext()
									.getString(R.string.activity_not_found,
											"choose image"), Toast.LENGTH_SHORT)
							.show();
				}

			}
		});

		if (savedInstanceState != null) {

			// indicating whether or not select-all is on or off.
			if (savedInstanceState.containsKey(BUNDLE_TOGGLED_KEY)) {
				mToggled = savedInstanceState.getBoolean(BUNDLE_TOGGLED_KEY);
			}

			// how many items we've selected
			// Android should keep track of this, but broken on rotate...
			if (savedInstanceState.containsKey(BUNDLE_SELECTED_COUNT)) {
				mSelectedCount = savedInstanceState
						.getInt(BUNDLE_SELECTED_COUNT);
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

		if (savedInstanceState != null
				&& savedInstanceState.containsKey(PROJECTLIST)) {
			mFieldList = (ArrayList<HashMap<String, String>>) savedInstanceState
					.getSerializable(PROJECTLIST);
		} else {
			mFieldList = new ArrayList<HashMap<String, String>>();
		}

		if (getLastNonConfigurationInstance() instanceof LoadAttachmentListTask) {
            mLoadAttachmentTask = (LoadAttachmentListTask) getLastNonConfigurationInstance();
            mLoadAttachmentTask.preferences = preferences;
            mLoadAttachmentTask.storyid = storyid;
            mLoadAttachmentTask.questionid = questionanswerid;
            if (mLoadAttachmentTask.getStatus() == AsyncTask.Status.FINISHED) {
                try {
                    dismissDialog(PROGRESS_DIALOG);
                } catch (IllegalArgumentException e) {
                    Log.i(t, "Attempting to close a dialog that was not previously opened");
                }
                mDeleteFileTask = null;
            }
        } else if (getLastNonConfigurationInstance() instanceof DeleteFileTask) {
        	mDeleteFileTask = (DeleteFileTask) getLastNonConfigurationInstance();
            if (mDeleteFileTask.getStatus() == AsyncTask.Status.FINISHED) {
                try {
                    dismissDialog(PROGRESS_DIALOG);
                } catch (IllegalArgumentException e) {
                    Log.i(t, "Attempting to close a dialog that was not previously opened");
                }
                mDeleteFileTask = null;
            }
        } else if (getLastNonConfigurationInstance() == null) {
			// first time, so get the fieldlist
			downloadFieldList();
		}

		String[] data = new String[] { FILENAME };
		int[] view = new int[] { R.id.text1 };

		 mFieldListAdapter =
		            new SimpleAdapter(this, mFieldList, R.layout.two_item_multiple_choice_attachment, data, view);
		        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		        getListView().setItemsCanFocus(false);
		        setListAdapter(mFieldListAdapter);
	}
	 private void clearChoices() {
	        AttachmentList.this.getListView().clearChoices();
	        mDownloadButton.setEnabled(false);
	    }


	    @Override
	    protected void onListItemClick(ListView l, View v, int position, long id) {
			super.onListItemClick(l, v, position, id);
			mDownloadButton.setEnabled(!(selectedItemCount() == 0));

			Object o = getListAdapter().getItem(position);
			@SuppressWarnings("unchecked")
			HashMap<String, String> item = (HashMap<String, String>) o;
	        String detail = mFieldNamesAndURLs.get(item.get(FILENAME));

	        MandeUtility.Log(false, "onListItemClick");
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
		super.onActivityResult(requestCode, resultCode, intent);

		if (resultCode == RESULT_CANCELED) {

			return;
		}

		switch (requestCode) {

		case IMAGE_CAPTURE:
			/*
			 * We saved the image to the tempfile_path, but we really want it to
			 * be in: /sdcard/mande/stories/storyid/something.jpg so we move it
			 * there before inserting it into the content provider. Once the
			 * android image capture bug gets fixed, (read, we move on from
			 * Android 1.6) we want to handle images the audio and video
			 */
			// The intent is empty, but we know we saved the image to the temp
			// file
			/*
			 * File fi = new File(Constants.TMPFILE_PATH);
			 * 
			 * String s = storyid + File.separator + System.currentTimeMillis()
			 * + ".jpg";
			 * 
			 * File nf = new File(s); if (!fi.renameTo(nf)) { Log.e(t,
			 * "Failed to rename " + fi.getAbsolutePath()); } else { Log.i(t,
			 * "renamed " + fi.getAbsolutePath() + " to " +
			 * nf.getAbsolutePath()); } break;
			 */

		case IMAGE_CHOOSER:
			/*
			 * We saved the image to the tempfile_path, but we really want it to
			 * be in: /sdcard/mande/stories/storyid/something.jpg so we move it
			 * there before inserting it into the content provider. Once the
			 * android image capture bug gets fixed, (read, we move on from
			 * Android 1.6) we want to handle images the audio and video
			 */

			// get gp of chosen file
			String sourceImagePath = null;
			Uri selectedImage = intent.getData();
			if (selectedImage.toString().startsWith("file")) {
				sourceImagePath = selectedImage.toString().substring(6);
			} else {
				String[] fieldion = { Images.Media.DATA };
				Cursor cursor = null;
				try {
					cursor = getContentResolver().query(selectedImage,
							fieldion, null, null, null);
					int column_index = cursor
							.getColumnIndexOrThrow(Images.Media.DATA);
					cursor.moveToFirst();
					sourceImagePath = cursor.getString(column_index);
				} finally {
					if (cursor != null) {
						cursor.close();
					}
				}
			}

			File source = new File(sourceImagePath);

			String filenameArray[] = source.getAbsolutePath().split("\\.");

			String extension = filenameArray[filenameArray.length - 1];

			String destImagePath = Constants.STORIES_PATH + File.separator
					+ storyid + File.separator + questionanswerid
					+ File.separator + System.currentTimeMillis() + "."
					+ extension;


			File newImage = new File(destImagePath);
			FileUtils.copyFile(source, newImage);
			
			downloadFieldList();
			
			break;
		case AUDIO_CAPTURE:
		case VIDEO_CAPTURE:
		case AUDIO_CHOOSER:
		case VIDEO_CHOOSER:
			// For audio/video capture/chooser, we get the URI from the content
			// provider
			// then the widget copies the file and makes a new entry in the
			// content provider.
			break;

		}
	}

    /**
     * starts the task to download the selected fields, also shows progress dialog
     */
    @SuppressWarnings("unchecked")
    private void downloadSelectedFiles() {
        int totalCount = 0;
        ArrayList<String> filesToDownload = new ArrayList<String>();

        SparseBooleanArray sba = getListView().getCheckedItemPositions();
        for (int i = 0; i < getListView().getCount(); i++) {
            if (sba.get(i, false)) {
                HashMap<Integer, String> item =
                    (HashMap<Integer, String>) getListAdapter().getItem(i);
                
                String projID = item.get(FILENAME);
                
                /*String filenameArray[] = projID.split("\\/");
                String fileName = filenameArray[filenameArray.length-1];
                
                
                String proj = mFieldNamesAndURLs.get(fileName);*/
                
                
                
                filesToDownload.add(projID);
                
            }
        }
        totalCount = filesToDownload.size();

       MandeUtility.Log(false, "downloadSelectedFiles:"+Integer.toString(totalCount));

        if (totalCount > 0) {
            // show dialog box
            showDialog(PROGRESS_DIALOG);

            mDeleteFileTask = new DeleteFileTask();
            mDeleteFileTask.ctx = getApplicationContext();
            mDeleteFileTask.setDeleFileListener(this);
            mDeleteFileTask.execute(filesToDownload);
        } else {
            Toast.makeText(getApplicationContext(), R.string.noselect_error, Toast.LENGTH_SHORT)
                    .show();
        }
    }
    
   private void downloadFieldList() {
       ConnectivityManager connectivityManager =
           (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
       NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

       if (ni == null || !ni.isConnected()) {
           Toast.makeText(this, R.string.no_connection, Toast.LENGTH_SHORT).show();
       } else {

           mFieldNamesAndURLs = new HashMap<String, String>();
           if (mProgressDialog != null) {
               // This is needed because onPrepareDialog() is broken in 1.6.
               mProgressDialog.setMessage(getString(R.string.please_wait));
           }
           showDialog(PROGRESS_DIALOG);

           if (mLoadAttachmentTask != null &&
        		   mLoadAttachmentTask.getStatus() != AsyncTask.Status.FINISHED) {
           	return; // we are already doing the download!!!
           } else if (mLoadAttachmentTask != null) {
        	   mLoadAttachmentTask.setLoadAttachmentListener(null);
        	   mLoadAttachmentTask.cancel(true);
        	   mLoadAttachmentTask = null;
           }

           mLoadAttachmentTask = new LoadAttachmentListTask();
           mLoadAttachmentTask.preferences = preferences;
           mLoadAttachmentTask.storyid = storyid;
           mLoadAttachmentTask.questionid = questionanswerid;
           mLoadAttachmentTask.setLoadAttachmentListener(this);
           mLoadAttachmentTask.execute();
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
                         if (mLoadAttachmentTask != null) {
                        	 mLoadAttachmentTask.setLoadAttachmentListener(null);
                        	 mLoadAttachmentTask.cancel(true);
                        	 mLoadAttachmentTask = null;
                         }
                         if (mDeleteFileTask != null) {
                        	 mDeleteFileTask.setDeleFileListener(null);
                        	 mDeleteFileTask.cancel(true);
                        	 mDeleteFileTask = null;
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

		}
		return null;
	}
	
	 @Override
	    public Object onRetainNonConfigurationInstance() {
	        if (mDeleteFileTask != null) {
	            return mDeleteFileTask;
	        } else {
	            return mLoadAttachmentTask;
	        }
	    }


	    @Override
	    protected void onDestroy() {
	        if (mLoadAttachmentTask != null) {
	        	mLoadAttachmentTask.setLoadAttachmentListener(null);
	        }
	        if (mDeleteFileTask != null) {
	        	mDeleteFileTask.setDeleFileListener(null);
	        }
	        super.onDestroy();
	    }


	    @Override
	    protected void onResume() {
	        if (mLoadAttachmentTask != null) {
	        	mLoadAttachmentTask.setLoadAttachmentListener(this);
	        }
	        if (mDeleteFileTask != null) {
	        	mDeleteFileTask.setDeleFileListener(this);
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
						downloadFieldList();
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
	public void attachmentListLoadComplete(HashMap<String, String> result) {
		 dismissDialog(PROGRESS_DIALOG);
	        mLoadAttachmentTask.setLoadAttachmentListener(null);
	        mLoadAttachmentTask = null;

	        if (result == null) {
	            Log.e(t, "Fieldlist Downloading returned null.  That shouldn't happen");
	            // Just displayes "error occured" to the user, but this should never happen.
	            createAlertDialog(getString(R.string.load_remote_field_error),
	                getString(R.string.error_occured), EXIT);
	            return;
	        }

	        if (result.containsKey(LoadAttachmentListTask.DL_AUTH_REQUIRED)) {
	            // need authorization
	            showDialog(AUTH_DIALOG);
	        } else if (result.containsKey(LoadAttachmentListTask.DL_ERROR_MSG)) {
	            // Download failed
	            String dialogMessage =
	                getString(R.string.list_failed_with_error,
	                    result.get(LoadAttachmentListTask.DL_ERROR_MSG));//TODO
	            String dialogTitle = getString(R.string.load_remote_field_error);
	            createAlertDialog(dialogTitle, dialogMessage, DO_NOT_EXIT);
	        } else {
	            // Everything worked. Clear the list and add the results.
	            mFieldNamesAndURLs = result;

	            mFieldList.clear();

	            ArrayList<String> ids = new ArrayList<String>(mFieldNamesAndURLs.keySet());
	            for (int i = 0; i < result.size(); i++) {
	            	String fileName = ids.get(i);
	            	String details = mFieldNamesAndURLs.get(fileName);
	                HashMap<String, String> item = new HashMap<String, String>();
	                item.put(FILENAME, details);
	                
	                    mFieldList.add(item);
	                
	            }
	            mFieldListAdapter.notifyDataSetChanged();
	        }
	}
	@Override
	public void fileDeleteComplete(HashMap<String, String> result) {
		  if (mDeleteFileTask != null) {
			  mDeleteFileTask.setDeleFileListener(null);
	        }

	        if (mProgressDialog.isShowing()) {
	            // should always be true here
	            mProgressDialog.dismiss();
	        }

	        Set<String> keys = result.keySet();
	        
	        StringBuilder b = new StringBuilder();
	        for (String k : keys) {
	            b.append(k);
	            b.append("\n\n");
	        }

	        createAlertDialog(getString(R.string.delete_attachments_result), b.toString().trim(), EXIT);
		
	}
	  @Override
	    public void progressUpdate(String currentFile, int progress, int total) {
	        mAlertMsg = getString(R.string.deleting_attachment, currentFile, progress, total);
	        mProgressDialog.setMessage(mAlertMsg);
	    }

}