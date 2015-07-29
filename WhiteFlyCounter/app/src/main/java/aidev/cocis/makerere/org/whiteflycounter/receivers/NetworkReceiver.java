package aidev.cocis.makerere.org.whiteflycounter.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.HashMap;

import aidev.cocis.makerere.org.whiteflycounter.R;
import aidev.cocis.makerere.org.whiteflycounter.listeners.StoryUploaderListener;
import aidev.cocis.makerere.org.whiteflycounter.preferences.PreferencesActivity;
import aidev.cocis.makerere.org.whiteflycounter.provider.StoryProviderAPI;
import aidev.cocis.makerere.org.whiteflycounter.provider.StoryProviderAPI.StoryColumns;
import aidev.cocis.makerere.org.whiteflycounter.tasks.StoryUploaderTask;

public class NetworkReceiver extends BroadcastReceiver implements StoryUploaderListener {

    // turning on wifi often gets two CONNECTED events. we only want to run one thread at a time
    public static boolean running = false;
    StoryUploaderTask mUploaderTask;


   @Override
	public void onReceive(Context context, Intent intent) {
        // make sure sd card is ready, if not don't try to send
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return;
        }
        
		String action = intent.getAction();

		NetworkInfo currentNetworkInfo = (NetworkInfo) intent
				.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);

		if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
			if (currentNetworkInfo.getState() == NetworkInfo.State.CONNECTED) {
				if (interfaceIsEnabled(context, currentNetworkInfo)) {
					uploadStories(context);
				}
			}
		} else if (action.equals("org.odk.collect.android.FormSaved")) {
			ConnectivityManager connectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

			if (ni == null || !ni.isConnected()) {
				// not connected, do nothing
			} else {
				if (interfaceIsEnabled(context, ni)) {
					uploadStories(context);
				}
			}
		}
	}

	private boolean interfaceIsEnabled(Context context,
			NetworkInfo currentNetworkInfo) {
		// make sure autosend is enabled on the given connected interface
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		boolean sendwifi = sharedPreferences.getBoolean(
				PreferencesActivity.KEY_AUTOSEND_WIFI, false);
		boolean sendnetwork = sharedPreferences.getBoolean(
				PreferencesActivity.KEY_AUTOSEND_NETWORK, false);

		return (currentNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI
				&& sendwifi || currentNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE
				&& sendnetwork);
	}


    private void uploadStories(Context context) {
        if (!running) {
            running = true;

            String selection = StoryColumns.STATUS + "=? or " + StoryColumns.STATUS + "=?";
            String selectionArgs[] =
                {
                        StoryProviderAPI.STATUS_COMPLETE,
                        StoryProviderAPI.STATUS_SUBMISSION_FAILED
                };

            Cursor c =
                context.getContentResolver().query(StoryColumns.CONTENT_URI, null, selection,
                    selectionArgs, null);

            ArrayList<Long> toUpload = new ArrayList<Long>();
            if (c != null && c.getCount() > 0) {
                c.move(-1);
                while (c.moveToNext()) {
                    Long l = c.getLong(c.getColumnIndex(StoryColumns._ID));
                    toUpload.add(Long.valueOf(l));
                }
                
                // get the username, password, and server from preferences
                SharedPreferences settings =
                        PreferenceManager.getDefaultSharedPreferences(context);

                String storedUsername = settings.getString(PreferencesActivity.KEY_USERNAME, null);
                String storedPassword = settings.getString(PreferencesActivity.KEY_PASSWORD, null);
                String server = settings.getString(PreferencesActivity.KEY_SERVER_URL,
                        context.getString(R.string.default_server_url));
                String url = server
                        + settings.getString(PreferencesActivity.KEY_FORMLIST_URL,
                                context.getString(R.string.default_mande_storylist));

                Uri u = Uri.parse(url);
                //WebUtils.addCredentials(storedUsername, storedPassword, u.getHost());

                mUploaderTask = new StoryUploaderTask();
                mUploaderTask.setUploaderListener(this);

                Long[] toSendArray = new Long[toUpload.size()];
                toUpload.toArray(toSendArray);
                mUploaderTask.execute(toSendArray);
            } else {
                running = false;
            }
        }
    }


    @Override
    public void uploadingComplete(HashMap<String, String> result) {
        // task is done
        mUploaderTask.setUploaderListener(null);
        running = false;
    }


    @Override
    public void progressUpdate(int progress, int total) {
        // do nothing
    }


    @Override
    public void authRequest(Uri url, HashMap<String, String> doneSoFar) {
        // if we get an auth request, just fail
        mUploaderTask.setUploaderListener(null);
        running = false;
    }

}