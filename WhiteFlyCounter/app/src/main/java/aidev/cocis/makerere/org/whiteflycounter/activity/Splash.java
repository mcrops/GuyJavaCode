package aidev.cocis.makerere.org.whiteflycounter.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import aidev.cocis.makerere.org.whiteflycounter.R;

public class Splash extends Activity {
 TextView textViewFOBUrl;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		textViewFOBUrl = (TextView) findViewById(R.id.textViewFOBUrl);
		//textViewFOBUrl.setTypeface(FontManager.getFonts(getApplicationContext(), "Dosis-Medium.ttf"));
		
		/**
		 * Take 3 seconds before showing login
		 */
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent i = new Intent(Splash.this, Home.class);
				startActivity(i);
				// close this activity
				finish();
			}
		}, 3000);
	}

}
