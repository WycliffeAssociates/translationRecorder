package org.wycliffeassociates.translationrecorder.TestUtils;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.wycliffeassociates.translationrecorder.R;

/**
 * Created by sarabiaj on 8/30/2017.
 */

public class FragmentTestActivity extends AppCompatActivity {
    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_test_activity);
    }
}