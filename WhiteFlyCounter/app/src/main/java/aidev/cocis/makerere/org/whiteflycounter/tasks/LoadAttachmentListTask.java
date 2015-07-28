package aidev.cocis.makerere.org.whiteflycounter.tasks;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import java.io.File;
import java.util.HashMap;

import aidev.cocis.makerere.org.whiteflycounter.common.Constants;
import aidev.cocis.makerere.org.whiteflycounter.listeners.StoryAttachmentListener;
import aidev.cocis.makerere.org.whiteflycounter.webservices.field.FieldService;

/**
 * Background task for downloading fields from urls or a fieldlist from a url. We overload this task a
 * bit so that we don't have to keep track of two separate downloading tasks and it simplifies
 * interfaces. If LIST_URL is passed to doInBackground(), we fetch a field list. If a hashmap
 * containing field/url pairs is passed, we download those fields.
 *
 * @author Acellam Guy
 */
public class LoadAttachmentListTask extends AsyncTask<Void, Integer, HashMap<String, String>> {
    private static final String t = "LoadAttachmentListTask";

    // used to store error message if one occurs
    public static final String DL_ERROR_MSG = "dlerrormessage";
    public static final String DL_AUTH_REQUIRED = "dlauthrequired";
    public FieldService fieldservice;
    private StoryAttachmentListener mStateListener;
    public SharedPreferences preferences;
	public int storyid;
	public int questionid;


    @Override
    protected HashMap<String, String> doInBackground(Void... values) {
    	
    	
        // We populate this with available fields from the server.
        HashMap<String, String> attachmentList = new HashMap<String, String>();

       File dir = new File(Constants.STORIES_PATH+ File.separator+String.valueOf(storyid)+ File.separator+String.valueOf(questionid));
		
		File[] filelist = dir.listFiles();
		String[] theNamesOfFiles = new String[filelist.length];
		for (int i = 0; i < theNamesOfFiles.length; i++) {
		   theNamesOfFiles[i] = filelist[i].getName();

			attachmentList.put(filelist[i].getName().toString(), filelist[i].toString());
			
		}
		
		return attachmentList;
    }


    @Override
    protected void onPostExecute(HashMap<String, String> value) {
        synchronized (this) {
        	 if (mStateListener != null) {

                    mStateListener.attachmentListLoadComplete(value);
                }
        }
    }


    public void setLoadAttachmentListener(StoryAttachmentListener sl) {
        synchronized (this) {
            mStateListener = sl;
       	 
        }
    }
 


}
