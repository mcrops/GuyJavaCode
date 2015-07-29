package aidev.cocis.makerere.org.whiteflycounter;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Created by User on 7/7/2015.
 */
public final class FieldListActivity extends ListActivity {

    private static final String TAG = FieldListActivity.class.getSimpleName();

    private FieldManager fieldManager;
    private ArrayAdapter<FieldItem> adapter;
    private CharSequence originalTitle;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.fieldManager = new FieldManager(this);
        adapter = new FieldItemAdapter(this);
        setListAdapter(adapter);
        View listview = getListView();
        registerForContextMenu(listview);
        originalTitle = getTitle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadHistoryItems();
    }

    private void reloadHistoryItems() {
        Iterable<FieldItem> items = fieldManager.buildHistoryItems();
        adapter.clear();
        for (FieldItem item : items) {
            adapter.add(item);
        }
        setTitle(originalTitle + " (" + adapter.getCount() + ')');
        if (adapter.isEmpty()) {
            adapter.add(new FieldItem(null));
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        if (adapter.getItem(position).getField() != null) {
            /*Intent intent = new Intent(this, CaptureActivity.class);
            intent.putExtra(Intents.History.ITEM_NUMBER, position);
            setResult(Activity.RESULT_OK, intent);
            finish();*/
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu,
                                    View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        int position = ((AdapterView.AdapterContextMenuInfo) menuInfo).position;
        if (position >= adapter.getCount() || adapter.getItem(position).getField() != null) {
            menu.add(Menu.NONE, position, position, "Clear one history");
        } // else it's just that dummy "Empty" message
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int position = item.getItemId();
        fieldManager.deleteHistoryItem(position);
        reloadHistoryItems();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (fieldManager.hasHistoryItems()) {
            MenuInflater menuInflater = getMenuInflater();
            menuInflater.inflate(R.menu.history, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_history_send:
                CharSequence history = fieldManager.buildHistory();
                Parcelable historyFile = FieldManager.saveHistory(history.toString());
                if (historyFile == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Unmount USB");
                    builder.setPositiveButton("OK", null);
                    builder.show();
                } else {
                    Intent intent = new Intent(Intent.ACTION_SEND, Uri.parse("mailto:"));
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    String subject = "Subject";
                    intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                    intent.putExtra(Intent.EXTRA_TEXT, subject);
                    intent.putExtra(Intent.EXTRA_STREAM, historyFile);
                    intent.setType("text/csv");
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException anfe) {
                        Log.w(TAG, anfe.toString());
                    }
                }
                break;
            case R.id.menu_history_clear_text:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Sure");
                builder.setCancelable(true);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i2) {
                        fieldManager.clearHistory();
                        dialog.dismiss();
                        finish();
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

}
