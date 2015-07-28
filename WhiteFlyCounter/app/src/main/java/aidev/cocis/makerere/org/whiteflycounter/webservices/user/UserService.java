package aidev.cocis.makerere.org.whiteflycounter.webservices.user;

import android.os.AsyncTask;

import aidev.cocis.makerere.org.whiteflycounter.webservices.IHTTPEvents;
import aidev.cocis.makerere.org.whiteflycounter.webservices.WS_Enums.SoapProtocolVersion;

public class UserService {
	public String ACTION = "GetUser";
	public String EMAIL = "";
	public String PASSWORD = "";
	public String url = "http://192.168.1.128:8080/ws/";
	public int timeOut = 60000;

	public IHTTPEvents eventHandler;
	public SoapProtocolVersion soapVersion;

	public UserService() {
	}
	public UserService(IHTTPEvents eventHandler, String action,
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

	public void GetAllUsersAsync()
			throws Exception {

		new AsyncTask<Void, Void, VectorWSUser>() {
			@Override
			protected void onPreExecute() {
				eventHandler.HTTPStartedRequest();
			};

			@Override
			protected VectorWSUser doInBackground(Void... params) {
				return GetUser();
			}

			@Override
			protected void onPostExecute(VectorWSUser result) {
				eventHandler.HTTPEndedRequest();
				if (result != null) {
					eventHandler.HTTPFinished(ACTION, result);
				}
			}
		}.execute();
	}

	public void GetUserAuthAsync() throws Exception {

		new AsyncTask<Void, Void, VectorWSUser>() {
			@Override
			protected void onPreExecute() {
				eventHandler.HTTPStartedRequest();
			};

			@Override
			protected VectorWSUser doInBackground(Void... params) {
				return GetUser();
			}

			@Override
			protected void onPostExecute(VectorWSUser result) {
				eventHandler.HTTPEndedRequest();
				if (result != null) {
					eventHandler.HTTPFinished(ACTION, result);
				}
			}
		}.execute();
	}

	public VectorWSUser GetUser() {

		VectorWSUser resultVariable = new VectorWSUser();
		try {

		} catch (Exception e) {
			if (eventHandler != null)
				eventHandler.HTTPFinishedWithException(e);
			e.printStackTrace();
		}
		return resultVariable;
	}

}
