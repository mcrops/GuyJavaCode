package aidev.cocis.makerere.org.whiteflycounter;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by User on 7/3/2015.
 */
public class FinishSurvey extends Activity implements View.OnClickListener {

    ImageView imageView;
    TextView textViewTitle;
    private Button btnNext;
    private Button btnFinishSurvey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_entry_end);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){


        }
    }
}
