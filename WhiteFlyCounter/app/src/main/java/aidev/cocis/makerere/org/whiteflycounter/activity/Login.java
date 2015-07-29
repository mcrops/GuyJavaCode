package aidev.cocis.makerere.org.whiteflycounter.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import aidev.cocis.makerere.org.whiteflycounter.R;
import aidev.cocis.makerere.org.whiteflycounter.common.MandeUtility;
import aidev.cocis.makerere.org.whiteflycounter.webservices.IHTTPEvents;
import aidev.cocis.makerere.org.whiteflycounter.webservices.user.UserService;
import aidev.cocis.makerere.org.whiteflycounter.webservices.user.VectorWSUser;
import aidev.cocis.makerere.org.whiteflycounter.webservices.user.WSUser;

public class Login extends Activity  implements IHTTPEvents {
	// Declare UI variables
	EditText editTextEmail;
	EditText editTextPassword;
	TextView textViewFOBUrl;
	Button buttonLogin;
	 UserService userservice;
	 SharedPreferences preferences;
	 
	public void callWebService(){
         userservice = new UserService(this,"GetUser",editTextEmail.getText().toString(),editTextPassword.getText().toString());
        
        try {
        	  	
        	userservice.GetUserAuthAsync();
	       
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
    }
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		preferences = getSharedPreferences(MandeUtility.PREFS_NAME, 0);
		
		if(!MandeUtility.getEmail(preferences).equals("")){
			gotoHome();
		}else{
			
		setContentView(R.layout.activity_login);
		// instantiate UI variables from XML
		editTextEmail = (EditText) findViewById(R.id.editTextEmail);
		editTextPassword = (EditText) findViewById(R.id.editTextPassword);
		textViewFOBUrl = (TextView) findViewById(R.id.textViewFOBUrl);
		buttonLogin = (Button) findViewById(R.id.buttonLogin);
		// set fonts
		/*textViewFOBUrl.setTypeface(FontManager.getFonts(
				getApplicationContext(), "Dosis-Medium.ttf"));
		editTextEmail.setTypeface(FontManager.getFonts(getApplicationContext(),
				"Dosis-Medium.ttf"));
		editTextPassword.setTypeface(FontManager.getFonts(
				getApplicationContext(), "Dosis-Medium.ttf"));
		buttonLogin.setTypeface(FontManager.getFonts(getApplicationContext(),
				"Dosis-Medium.ttf"));*/
		}
	}

	public void onClick(View view) {

		switch (view.getId()) {
		case R.id.buttonLogin:
			 new AsyncTask<Void, Void, Void>() {
		        	@Override
		        	protected Void doInBackground(Void... params) {
		        		callWebService();
						return null;
		        	}
				}.execute();
			break;

		default:
			break;
		}

	}
	void gotoHome(){
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				
					Intent i = new Intent(Login.this, Home.class);
					startActivity(i);						
				// close this activity
				finish();
			}
		}, 2000);
	}
	 @Override
		public void HTTPStartedRequest() {
			MandeUtility.Log(false,"HTTPStartedRequest");
			
		}


		@Override
		public void HTTPFinished(String methodName, Object Data) {
			if(methodName.equals("GetUser")){	
				VectorWSUser users = (VectorWSUser) Data;
				
				//save customer finished
				WSUser user = users.firstElement();
				
				if(user!=null){
					//save password to preferences
					MandeUtility.saveEmail(userservice.EMAIL, preferences);
					MandeUtility.savePassword(userservice.PASSWORD, preferences);
					gotoHome();
				}
			}
			MandeUtility.Log(false, "HTTPFinished");
			MandeUtility.Log(false,methodName);
			
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
