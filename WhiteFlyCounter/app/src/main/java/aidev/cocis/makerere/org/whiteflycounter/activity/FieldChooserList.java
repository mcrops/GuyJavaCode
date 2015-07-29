package aidev.cocis.makerere.org.whiteflycounter.activity;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import aidev.cocis.makerere.org.whiteflycounter.R;
import aidev.cocis.makerere.org.whiteflycounter.common.CommonFunctions;
import aidev.cocis.makerere.org.whiteflycounter.common.IDHidingCursorAdapter;
import aidev.cocis.makerere.org.whiteflycounter.common.MandeUtility;
import aidev.cocis.makerere.org.whiteflycounter.provider.FieldsProviderAPI.FieldsColumns;

/**
 * Responsible for displaying all the valid fields in the fields directory
 * 
 * @author Acellam Guy
 */
public class FieldChooserList extends ListActivity {

	private static final String t = "FieldChooserList";
	private static final boolean EXIT = true;

	private AlertDialog mAlertDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// must be at the beginning of any activity that can be called from an
		// external intent
		try {
			CommonFunctions.createWHITEFLYCOUNTERDirs();
		} catch (RuntimeException e) {
			createErrorDialog(e.getMessage(), EXIT);
			return;
		}

		setContentView(R.layout.chooser_list_layout);
		setTitle(getString(R.string.app_name) + " > "
				+ getString(R.string.select_field));

		String sortOrder = FieldsColumns.NAME + " ASC ";
		Cursor c = managedQuery(FieldsColumns.CONTENT_URI, null, null, null,
				sortOrder);

		String[] data = new String[] { FieldsColumns.NAME,
				FieldsColumns.DISPLAY_SUBTEXT,
				FieldsColumns.WHITEFLYCOUNTER_PROJECT_ID,
				FieldsColumns.DESCRIPTION };
		int[] view = new int[] { R.id.text1, R.id.text2, R.id.text3,R.id.text4 };

		// render total instance view
		SimpleCursorAdapter instances = new IDHidingCursorAdapter(
				FieldsColumns.WHITEFLYCOUNTER_PROJECT_ID, this, R.layout.two_item, c,
				data, view);
		setListAdapter(instances);


	}


	/**
	 * Stores the path of selected field and finishes.
	 */
	@Override
	protected void onListItemClick(ListView listView, View view, int position,
			long id) {
		// get uri to field
		long idFieldsTable = ((SimpleCursorAdapter) getListAdapter())
				.getItemId(position);
		Uri fieldUri = ContentUris.withAppendedId(
				FieldsColumns.CONTENT_URI, idFieldsTable);

		MandeUtility.Log(false, "onListItemClick:" + fieldUri.toString());

		// fieldentryactivity
		startActivity(new Intent(Intent.ACTION_PICK, fieldUri));

		//finish();
	}

	private void createErrorDialog(String errorMsg, final boolean shouldExit) {

		MandeUtility.Log(false, "createErrorDialog:" + "show");

		mAlertDialog = new AlertDialog.Builder(this).create();
		mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
		mAlertDialog.setMessage(errorMsg);
		DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int i) {
				switch (i) {
				case DialogInterface.BUTTON_POSITIVE:
					MandeUtility.Log(false, "createErrorDialog");

					if (shouldExit) {
						finish();
					}
					break;
				}
			}
		};
		mAlertDialog.setCancelable(false);
		mAlertDialog.setButton(getString(R.string.ok), errorListener);
		mAlertDialog.show();
	}

}
