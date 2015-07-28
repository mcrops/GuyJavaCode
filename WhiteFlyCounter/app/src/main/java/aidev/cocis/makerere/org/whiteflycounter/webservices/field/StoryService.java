package aidev.cocis.makerere.org.whiteflycounter.webservices.field;

import android.os.AsyncTask;

import aidev.cocis.makerere.org.whiteflycounter.webservices.IHTTPEvents;
import aidev.cocis.makerere.org.whiteflycounter.webservices.WS_Enums.SoapProtocolVersion;

public class StoryService {
	public String NAMESPACE = "http://localhost:8080/";
	public String ACTION = "GetFieldSettings";
	public String EMAIL = "";
	public String PASSWORD = "";
	public String url = "http://192.168.1.128:8081/ws/";
	public int timeOut = 60000;

	public IHTTPEvents eventHandler;
	public SoapProtocolVersion soapVersion;

	public StoryService(IHTTPEvents eventHandler, String action,
			String email, String password) {
		this.eventHandler = eventHandler;
		this.url = this.url + action;
		this.ACTION = action;
		this.EMAIL = email;
		this.PASSWORD = password;
	}

	public void setTimeOut(int seconds) {
		this.timeOut = seconds * 1000;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void uploadStoryAsync(
			final int FieldID, final String StoryTitle,
			final String StoryFullText, final String StorySummary) throws Exception {

		new AsyncTask<Void, Void, WSStory>() {
			@Override
			protected void onPreExecute() {
				eventHandler.HTTPStartedRequest();
			};

			@Override
			protected WSStory doInBackground(Void... params) {
				return GetStory(FieldID, StoryTitle, StoryFullText,
						StorySummary);
			}

			@Override
			protected void onPostExecute(WSStory result) {
				eventHandler.HTTPEndedRequest();
				if (result != null) {
					eventHandler.HTTPFinished(ACTION, result);
				}
			}
		}.execute();
	}

	public WSStory GetStory( int FieldID,
			String StoryTitle, String StoryFullText, String StorySummary) {


		WSStory resultVariable = new WSStory();
		try {

		} catch (Exception e) {
			if (eventHandler != null)
				eventHandler.HTTPFinishedWithException(e);
			e.printStackTrace();
		}
		return resultVariable;
	}

}
