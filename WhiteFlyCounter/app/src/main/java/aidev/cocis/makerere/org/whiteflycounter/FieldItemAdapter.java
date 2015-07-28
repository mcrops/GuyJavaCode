package aidev.cocis.makerere.org.whiteflycounter;

/**
 * Created by User on 7/7/2015.
 */
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

final class FieldItemAdapter extends ArrayAdapter<FieldItem> {

    private final Context activity;

    FieldItemAdapter(Context activity) {
        super(activity, R.layout.history_list_item, new ArrayList<FieldItem>());
        this.activity = activity;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        View layout;
        if (view instanceof LinearLayout) {
            layout = view;
        } else {
            LayoutInflater factory = LayoutInflater.from(activity);
            layout = factory.inflate(R.layout.history_list_item, viewGroup, false);
        }

        FieldItem item = getItem(position);
        Field field = item.getField();

        CharSequence title;
        CharSequence detail;
        if (field != null) {
            title = field.getFieldno();
            detail = item.getDisplayAndDetails();
        } else {
            Resources resources = getContext().getResources();
            title ="Title";
            detail = "Empty detail";
        }

        ((TextView) layout.findViewById(R.id.history_title)).setText(title);
        ((TextView) layout.findViewById(R.id.history_detail)).setText(detail);

        return layout;
    }

}