package aidev.cocis.makerere.org.whiteflycounter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by User on 7/3/2015.
 */
public class StartSurvey extends Activity implements View.OnClickListener {

    ImageView imageView;
    TextView textViewTitle;
    private Button btnStart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_entry_start);

        btnStart = (Button) findViewById(R.id.startSurvey);
        btnStart.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.startSurvey:
                Intent startSurveyIntent = new Intent(this, FFmpegRecorderActivity.class);
                startActivity(startSurveyIntent);
                break;

        }
    }
}
