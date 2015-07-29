package aidev.cocis.makerere.org.whiteflycounter.common;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import aidev.cocis.makerere.org.whiteflycounter.R;

/**
 * Implementation of cursor adapter that displays the ID of a field if a
 * field has a ID.
 * 
 * @author Acellam Guy
 * 
 */
public class IDHidingCursorAdapter extends SimpleCursorAdapter {

	private final Context ctxt;
	private final String IDColumnName;
	private final ViewBinder originalBinder;

	public IDHidingCursorAdapter(String IDColumnName,
			Context context, int layout, Cursor c, String[] from, int[] to) {
		super(context, layout, c, from, to);
		this.IDColumnName = IDColumnName;
		ctxt = context;
		originalBinder = getViewBinder();
		setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(View view, Cursor cursor,
					int columnIndex) {
				String columnName = cursor.getColumnName(columnIndex);
				if (!columnName
						.equals(IDHidingCursorAdapter.this.IDColumnName)) {
					if (originalBinder != null) {
						return originalBinder.setViewValue(view, cursor,
								columnIndex);
					}
					return false;
				} else {
					String ID = cursor.getString(columnIndex);
					TextView v = (TextView) view;
					if (ID != null) {
						v.setText(ctxt.getString(R.string.id) + " "
								+ ID);
						v.setVisibility(View.VISIBLE);
					} else {
						v.setText(null);
						v.setVisibility(View.GONE);
					}
				}
				return true;
			}
		});
	}

}