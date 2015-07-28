package aidev.cocis.makerere.org.whiteflycounter.webservices.field;

import android.os.AsyncTask;

import aidev.cocis.makerere.org.whiteflycounter.webservices.IHTTPEvents;
import aidev.cocis.makerere.org.whiteflycounter.webservices.WS_Enums.SoapProtocolVersion;


public class FieldService {
	public String NAMESPACE ="http://localhost:8080/";
	public String ACTION ="GetFieldSettings";
	public String EMAIL ="";
	public String PASSWORD ="";
    public String url="http://192.168.1.128:8080/ws/";
    public int timeOut = 60000;
    
    public IHTTPEvents eventHandler;
    public SoapProtocolVersion soapVersion;
   
    public FieldService(IHTTPEvents eventHandler, String action,
			String email, String password) {
		this.eventHandler = eventHandler;
		this.url = this.url + action;
		this.ACTION = action;
		this.EMAIL = email;
		this.PASSWORD = password;
	}
    public void setTimeOut(int seconds){
        this.timeOut = seconds * 1000;
    }
    public void setUrl(String url){
        this.url = url;
    }
    
    public void GetAllFieldsAsync() throws Exception{
        
        new AsyncTask<Void, Void, VectorWSField>(){
            @Override
            protected void onPreExecute() {
                eventHandler.HTTPStartedRequest();
            };
            @Override
            protected VectorWSField doInBackground(Void... params) {
                return GetAllFields();
            }
            @Override
            protected void onPostExecute(VectorWSField result)
            {
                eventHandler.HTTPEndedRequest();
                if (result != null){
                    eventHandler.HTTPFinished(ACTION, result);
                }
            }
        }.execute();
    }
 public void GetFieldSettingsAsync(final WSField field) throws Exception{
        
        new AsyncTask<Void, Void, WSFieldSetting>(){
            @Override
            protected void onPreExecute() {
                eventHandler.HTTPStartedRequest();
            };
            @Override
            protected WSFieldSetting doInBackground(Void... params) {
                return GetFieldSetting(field);
            }
            @Override
            protected void onPostExecute(WSFieldSetting result)
            {
                eventHandler.HTTPEndedRequest();
                if (result != null){
                    eventHandler.HTTPFinished(ACTION, result);
                }
            }
        }.execute();
    }
 public WSFieldSetting GetFieldSetting(WSField field){

     
		WSFieldSetting resultVariable = new WSFieldSetting();
     try{

     }catch (Exception e) {
         if (eventHandler != null)
             eventHandler.HTTPFinishedWithException(e);
         e.printStackTrace();
     }
     return resultVariable;
 }
    public VectorWSField GetAllFields(){
        
        VectorWSField resultVariable = new VectorWSField();
        try{

        }catch (Exception e) {
            if (eventHandler != null)
                eventHandler.HTTPFinishedWithException(e);
            e.printStackTrace();
        }
        return resultVariable;
    }
    
}
