package aidev.cocis.makerere.org.whiteflycounter.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import java.util.HashMap;

import aidev.cocis.makerere.org.whiteflycounter.R;
import aidev.cocis.makerere.org.whiteflycounter.common.MandeUtility;
import aidev.cocis.makerere.org.whiteflycounter.listeners.FieldSettingsDownloaderListener;
import aidev.cocis.makerere.org.whiteflycounter.webservices.IHTTPEvents;
import aidev.cocis.makerere.org.whiteflycounter.webservices.field.FieldService;
import aidev.cocis.makerere.org.whiteflycounter.webservices.field.WSField;
import aidev.cocis.makerere.org.whiteflycounter.webservices.field.WSFieldSetting;

/**
 * Background task for downloading a given list of fields. We assume right now that the fields are
 * coming from the same server that presented the field list, but theoretically that won't always be
 * true.
 *
 * @author Acellam Guy
 */
public class DownloadFieldSettingsTask extends
        AsyncTask<WSField, String, HashMap<WSField, String>>  implements IHTTPEvents {

    private static final String t = "DownloadFieldSettingsTask";

    private static final String MD5_COLON_PREFIX = "md5:";

    private FieldSettingsDownloaderListener mStateListener;
    public SharedPreferences preferences;
    public FieldService fieldservice;
    public Context ctx;


    @Override
    protected HashMap<WSField, String> doInBackground(WSField... values) {
        WSField fd = values[0];
        fieldservice = new FieldService(this,"GetFieldSettings",MandeUtility.getEmail(preferences),MandeUtility.getPassword(preferences));
        
       /* int total = toDownload.size();
        int count = 1;
    	MandeUtility.Log(false, "downloadFields:"+ String.valueOf(total));*/

        HashMap<WSField, String> result = new HashMap<WSField, String>();
        
            publishProgress(fd.Name,"Reporting Period", "Searching...",Integer.valueOf(0).toString(), Integer.valueOf(3)
                    .toString());
            
            try {
        	  	
            	fieldservice.GetFieldSettingsAsync(fd);
    	       
    			
    		} catch (Exception e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}    	
            //Get Field Settings
            
            String message = "";

           /* try {
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
            }*/
            if (message.equalsIgnoreCase("")) {
                message = ctx.getString(R.string.success);
            }
            result.put(fd, message);
       


        return result;
    }
  
    @Override
    protected void onPostExecute(HashMap<WSField, String> value) {
        synchronized (this) {
            
        }
    }


    @Override
    protected void onProgressUpdate(String... values) {
        synchronized (this) {
            if (mStateListener != null) {
                // update progress and total
                //mStateListener.progressUpdate(values[0],
                //		values[1],
                //		values[2],
               // 	Integer.valueOf(values[3]),
               //     Integer.valueOf(values[4]));
            }
        }

    }


    public void setDownloaderListener(FieldSettingsDownloaderListener sl) {
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
  		if(methodName.equals("GetFieldSettings")){
  			WSFieldSetting projsetting = (WSFieldSetting) Data;
  			
  			 
  			if(projsetting!=null){
  				 if (mStateListener != null) {

  					 HashMap<WSFieldSetting,String> fieldSetting = new HashMap<WSFieldSetting,String>();
  					 fieldSetting.put(projsetting, "Started Reporting Period");
  					 
  		                mStateListener.fieldSettingsDownloadingComplete(fieldSetting);
  		            
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
