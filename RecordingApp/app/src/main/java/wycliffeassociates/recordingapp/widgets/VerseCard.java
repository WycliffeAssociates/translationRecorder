package wycliffeassociates.recordingapp.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import wycliffeassociates.recordingapp.FilesPage.FileNameExtractor;
import wycliffeassociates.recordingapp.R;

/**
 * Created by sarabiaj on 6/30/2016.
 */
public class VerseCard extends FrameLayout {
    public VerseCard(Context context) {
        this(context, null);
    }

    public VerseCard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerseCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(getContext(), R.layout.verse_card_widget, this);

        findViewById(R.id.expanded_card).setVisibility(View.GONE);
        findViewById(R.id.base_card).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                View expanded = findViewById(R.id.expanded_card);
                if(expanded.getVisibility() == GONE){
                    expanded.setVisibility(VISIBLE);
                } else {
                    expanded.setVisibility(GONE);
                }
            }
        });
    }
}
