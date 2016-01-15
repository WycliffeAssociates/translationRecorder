package wycliffeassociates.recordingapp.SettingsPage;

import android.content.Context;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/**
 * Created by leongv on 1/14/2016.
 */
public class CustomPrefCategory extends PreferenceCategory {
    public CustomPrefCategory(Context context) {
        super(context);
    }

    public CustomPrefCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomPrefCategory(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        TextView title = (TextView) view.findViewById(android.R.id.title);
        if (title != null) {
            title.setTextSize(16 * view.getResources().getDisplayMetrics().density);
        }
    }
}
