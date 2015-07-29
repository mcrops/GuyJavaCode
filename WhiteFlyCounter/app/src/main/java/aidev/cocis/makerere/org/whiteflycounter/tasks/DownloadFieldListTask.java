package aidev.cocis.makerere.org.whiteflycounter.tasks;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import java.util.HashMap;

import aidev.cocis.makerere.org.whiteflycounter.common.MandeUtility;
import aidev.cocis.makerere.org.whiteflycounter.listeners.FieldListDownloaderListener;
import aidev.cocis.makerere.org.whiteflycounter.webservices.IHTTPEvents;
import aidev.cocis.makerere.org.whiteflycounter.webservices.field.FieldService;
import aidev.cocis.makerere.org.whiteflycounter.webservices.field.VectorWSField;
import aidev.cocis.makerere.org.whiteflycounter.webservices.field.WSField;

/**
 * Background task for downloading fields from urls or a fieldlist from a url. We overload this task a
 * bit so that we don't have to keep track of two separate downloading tasks and it simplifies
 * interfaces. If LIST_URL is passed to doInBackground(), we fetch a field list. If a hashmap
 * containing field/url pairs is passed, we download those fields.
 *
 * @author Acellam Guy
 */
public class DownloadFieldListTask extends AsyncTask<Void, Integer, HashMap<String, WSField>> implements IHTTPEvents {
    private static final String t = "DownloadFormsTask";

    // used to store error message if one occurs
    public static final String DL_ERROR_MSG = "dlerrormessage";
    public static final String DL_AUTH_REQUIRED = "dlauthrequired";
    public FieldService fieldservice;
    private FieldListDownloaderListener mStateListener;
    public SharedPreferences preferences;
	 


    @Override
    protected HashMap<String, WSField> doInBackground(Void... values) {
    	
    	fieldservice = new FieldService(this,"GetAllFields",MandeUtility.getEmail(preferences),MandeUtility.getPassword(preferences));
    	 
        try {
        	  	
        	fieldservice.GetAllFieldsAsync();
	       
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    	

        // We populate this with available fields from the server.
        HashMap<String, WSField> fieldList = new HashMap<String, WSField>();

         
        return fieldList;
    }


    @Override
    protected void onPostExecute(HashMap<String, WSField> value) {
        synchronized (this) {
           
        }
    }


    public void setDownloaderListener(FieldListDownloaderListener sl) {
        synchronized (this) {
            mStateListener = sl;
        }
    }
    
    @Override
	public void HTTPStartedRequest() {
		MandeUtility.Log(false,"HTTPStartedRequest");
		
	}


	@Override
	public void HTTPFinished(String methodName, Object Data) {
		 synchronized (this) {
		if(methodName.equals("GetAllFields")){
			VectorWSField fields = (VectorWSField) Data;
			
			 
			if(fields!=null){
				 if (mStateListener != null) {

					 HashMap<Integer, WSField> fieldList = new HashMap<Integer, WSField>();
					 
					    for (WSField wsField : fields) {
					    	fieldList.put(wsField.FieldID, wsField);
						}
		                mStateListener.fieldListDownloadingComplete(fieldList);
		            }
			}
		}
		MandeUtility.Log(false, "HTTPFinished");
		MandeUtility.Log(false,methodName);
		 }
	}

	@Override
	public void HTTPFinishedWithException(Exception ex) {
		MandeUtility.Log(true,"HTTPFinishedWithException");
		
	}
	@Override
	public void HTTPEndedRequest() {
		MandeUtility.Log(true,"sWsdl2CodeEndedRequest");
	}


}
