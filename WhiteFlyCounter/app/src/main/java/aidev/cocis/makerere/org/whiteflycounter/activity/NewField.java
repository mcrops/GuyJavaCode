package aidev.cocis.makerere.org.whiteflycounter.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import aidev.cocis.makerere.org.whiteflycounter.FieldListActivity;
import aidev.cocis.makerere.org.whiteflycounter.FieldManager;
import aidev.cocis.makerere.org.whiteflycounter.R;
import aidev.cocis.makerere.org.whiteflycounter.Util;

/**
 * Created by User on 7/3/2015.
 */

public class NewField extends Activity implements View.OnClickListener {

    EditText fieldNoEditText;
    EditText fiedlDescriptionEditText;
    private Button btnSave;
    private FieldManager fieldManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_field);

        btnSave = (Button) findViewById(R.id.save);
        btnSave.setOnClickListener(this);

        fieldNoEditText = (EditText) findViewById(R.id.field_no_edit_text);
        fiedlDescriptionEditText=(EditText) findViewById(R.id.field_description);


    }
    @Override
    protected void onResume() {
        super.onResume();

        // historyManager must be initialized here to update the history preference
        fieldManager = new FieldManager(this);
        fieldManager.trimHistory();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.save:
                /*Field field = new Field(fieldNoEditText.getText().toString(),fiedlDescriptionEditText.getText().toString());
                FieldHandler fieldHandler = new FieldHandler(this,field);
                fieldManager.addHistoryItem(field,fieldHandler);*/

                Util.LogI("New Field:Save");
                Intent i = new Intent(getApplicationContext(),
                        FieldListActivity.class);
                startActivity(i);
                break;

        }
    }
}
