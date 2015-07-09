package aidev.cocis.makerere.org.whiteflycounter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Created by User on 7/3/2015.
 */
public class ShowAnalysis extends Activity implements View.OnClickListener {

    ImageView imageView;
    TextView textViewTitle;
    private Button btnRecorderControl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showanalysis);

        imageView = (ImageView) findViewById(R.id.imageView);
        textViewTitle = (TextView) findViewById(R.id.textViewTitle);
        btnRecorderControl = (Button) findViewById(R.id.recorder_control);

        btnRecorderControl.setText("Again");
        btnRecorderControl.setBackgroundResource(R.drawable.btn_shutter_pressed);
        btnRecorderControl.setOnClickListener(this);


        // getIntent() is a method from the started activity
        Intent detectFramesIntent = getIntent(); // gets the previously created intent

        File imgFile = new File(detectFramesIntent.getStringExtra("imagePath"));
        Bitmap bmp = BitmapFactory.decodeFile(imgFile.getAbsolutePath());


        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

        imageView.setImageBitmap(bmp);
        textViewTitle.setText("Analysis Results:"+detectFramesIntent.getStringExtra("count")+" Detected.");

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.recorder_control)
        {
            Intent frameIntent = new Intent(this, FFmpegRecorderActivity.class);
            startActivity(frameIntent);

            finish();
        }

    }
}
