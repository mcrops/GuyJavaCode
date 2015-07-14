package aidev.cocis.makerere.org.whiteflycounter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

/**
 * Created by User on 7/3/2015.
 */
public class ShowAnalysis extends Activity implements View.OnClickListener {

    ImageView imageView;
    TextView textViewTitle;
    private Button btnNext;
    private Button btnFinishSurvey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showanalysis);

        imageView = (ImageView) findViewById(R.id.imageView);
        textViewTitle = (TextView) findViewById(R.id.countTextView);
        btnNext = (Button) findViewById(R.id.form_forward_button);
        btnNext.setOnClickListener(this);

        btnFinishSurvey = (Button) findViewById(R.id.form_back_button);
        btnFinishSurvey.setOnClickListener(this);


        // getIntent() is a method from the started activity
        Intent detectFramesIntent = getIntent(); // gets the previously created intent

        File imgFile = new File(detectFramesIntent.getStringExtra("imagePath"));
        Bitmap bmp = BitmapFactory.decodeFile(imgFile.getAbsolutePath());


        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

        imageView.setImageBitmap(bmp);
        textViewTitle.setText(detectFramesIntent.getStringExtra("count")+" Detected.");

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.form_forward_button:
                Intent frameIntent = new Intent(this, FFmpegRecorderActivity.class);
                startActivity(frameIntent);

                finish();
                break;
            case R.id.form_back_button:
                Intent finishSurveyIntent = new Intent(this, FinishSurvey.class);
                startActivity(finishSurveyIntent);
                break;

        }
    }
}
