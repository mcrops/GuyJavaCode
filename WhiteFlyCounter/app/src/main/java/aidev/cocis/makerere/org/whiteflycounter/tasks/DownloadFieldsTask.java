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
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.HashMap;

import aidev.cocis.makerere.org.whiteflycounter.R;
import aidev.cocis.makerere.org.whiteflycounter.common.MandeUtility;
import aidev.cocis.makerere.org.whiteflycounter.listeners.FieldDownloaderListener;
import aidev.cocis.makerere.org.whiteflycounter.provider.FieldsProviderAPI.FieldsColumns;
import aidev.cocis.makerere.org.whiteflycounter.webservices.field.WSField;

/**
 * Background task for downloading a given list of fields. We assume right now that the fields are
 * coming from the same server that presented the field list, but theoretically that won't always be
 * true.
 *
 * @author Acellam Guy
 */
public class DownloadFieldsTask extends
        AsyncTask<ArrayList<WSField>, String, HashMap<WSField, String>> {

    private static final String t = "DownloadFieldsTask";

    private static final String MD5_COLON_PREFIX = "md5:";
    

    private FieldDownloaderListener mStateListener;
    public Context ctx;


    @Override
    protected HashMap<WSField, String> doInBackground(ArrayList<WSField>... values) {
        ArrayList<WSField> toDownload = values[0];

        int total = toDownload.size();
        int count = 1;
    	MandeUtility.Log(false, "downloadFields:"+ String.valueOf(total));

        HashMap<WSField, String> result = new HashMap<WSField, String>();


        for (int i = 0; i < total; i++) {
            WSField fd = toDownload.get(i);
            publishProgress(fd.Name, Integer.valueOf(count).toString(), Integer.valueOf(total)
                    .toString());

            String message = "";

            try {
                Cursor alreadyExists = null;
                Uri uri = null;
                try {
                    String[] fieldion = {
                            FieldsColumns._ID, FieldsColumns.WHITEFLYCOUNTER_PROJECT_ID
                    };
                    String[] selectionArgs = {
                    	String.valueOf(fd.FieldID)
                    };
                    String selection = FieldsColumns.WHITEFLYCOUNTER_PROJECT_ID + "=?";
                    alreadyExists = ctx
                            .getContentResolver()
                            .query(FieldsColumns.CONTENT_URI, fieldion, selection, selectionArgs,
                                null);

	                if (alreadyExists.getCount() <= 0) {
	                    // doesn't exist, so insert it
	                    ContentValues v = new ContentValues();
	                    v.put(FieldsColumns.WHITEFLYCOUNTER_PROJECT_ID, fd.FieldID);

	                    v.put(FieldsColumns.NAME, fd.Name);
	                    v.put(FieldsColumns.WHITEFLYCOUNTER_PROJECT_ID, fd.FieldID);
	                    v.put(FieldsColumns.DESCRIPTION, fd.Description);
	                    uri =
	                        ctx.getContentResolver()
	                                .insert(FieldsColumns.CONTENT_URI, v);
	                	MandeUtility.Log(false, "insert:"+ fd.Name);

	                } else {
	                    alreadyExists.moveToFirst();
	                    uri =
	                        Uri.withAppendedPath(FieldsColumns.CONTENT_URI,
	                            alreadyExists.getString(alreadyExists.getColumnIndex(FieldsColumns._ID)));
	                    MandeUtility.Log(false, "refresh"+ fd.Name);
	                }
                } finally {
                	if ( alreadyExists != null ) {
                		alreadyExists.close();
                	}
                	
                }

            } catch (Exception e) {
                e.printStackTrace();
                if (e.getCause() != null) {
                    message += e.getCause().getMessage();
                } else {
                    message += e.getMessage();
                }
            }
            count++;
            if (message.equalsIgnoreCase("")) {
                message = ctx.getString(R.string.success);
            }
            result.put(fd, message);
        }


        return result;
    }
  
    @Override
    protected void onPostExecute(HashMap<WSField, String> value) {
        synchronized (this) {
            if (mStateListener != null) {
                mStateListener.fieldsDownloadingComplete(value);
            }
        }
    }


    @Override
    protected void onProgressUpdate(String... values) {
        synchronized (this) {
            if (mStateListener != null) {
                // update progress and total
                mStateListener.progressUpdate(values[0],
                	Integer.valueOf(values[1]),
                    Integer.valueOf(values[2]));
            }
        }

    }


    public void setDownloaderListener(FieldDownloaderListener sl) {
        synchronized (this) {
            mStateListener = sl;
        }
    }

}
