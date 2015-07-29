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

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import aidev.cocis.makerere.org.whiteflycounter.common.MandeUtility;
import aidev.cocis.makerere.org.whiteflycounter.listeners.FileDeleteListener;

public class DeleteFileTask extends
        AsyncTask<ArrayList<String>, String, HashMap<String, String>> {

    private static final String t = "DeleteFileTask";

    private static final String MD5_COLON_PREFIX = "md5:";
    

    private FileDeleteListener mStateListener;
    public Context ctx;


    @Override
    protected HashMap<String, String> doInBackground(ArrayList<String>... values) {
        ArrayList<String> toDownload = values[0];

        int total = toDownload.size();
        int count = 1;
    	MandeUtility.Log(false, "deleteFiles:"+ String.valueOf(total));

        HashMap<String, String> result = new HashMap<String, String>();


        for (int i = 0; i < total; i++) {
        	String fd = toDownload.get(i);
            publishProgress(fd, Integer.valueOf(count).toString(), Integer.valueOf(total)
                    .toString());

            String message = "";
            
            File f = new File(fd);
            f.delete();
            
            
            count++;
            result.put(fd, message);
        }


        return result;
    }
  
    @Override
    protected void onPostExecute(HashMap<String, String> value) {
        synchronized (this) {
            if (mStateListener != null) {
                mStateListener.fileDeleteComplete(value);
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


    public void setDeleFileListener(FileDeleteListener sl) {
        synchronized (this) {
            mStateListener = sl;
        }
    }

}
