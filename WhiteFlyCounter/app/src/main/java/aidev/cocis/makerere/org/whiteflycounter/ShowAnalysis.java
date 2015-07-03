package aidev.cocis.makerere.org.whiteflycounter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

/**
 * Created by User on 7/3/2015.
 */
public class ShowAnalysis extends Activity {

    ImageView imageView;
    TextView textViewTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showanalysis);

        imageView = (ImageView) findViewById(R.id.imageView);
        textViewTitle = (TextView) findViewById(R.id.textViewTitle);

        // getIntent() is a method from the started activity
        Intent detectFramesIntent = getIntent(); // gets the previously created intent

        imageView.setImageURI(Uri.fromFile(new File(detectFramesIntent.getStringExtra("imagePath"))));
        textViewTitle.setText("Analysis Results:"+detectFramesIntent.getStringExtra("count")+" Detected.");

    }
}
