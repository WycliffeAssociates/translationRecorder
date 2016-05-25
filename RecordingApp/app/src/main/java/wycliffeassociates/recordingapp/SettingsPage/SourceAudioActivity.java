package wycliffeassociates.recordingapp.SettingsPage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import wycliffeassociates.recordingapp.FilesPage.AudioFiles;
import wycliffeassociates.recordingapp.ProjectManager.Project;
import wycliffeassociates.recordingapp.R;

/**
 * Created by sarabiaj on 5/25/2016.
 */
public class SourceAudioActivity extends Activity{
    private Project mProject;
    private Button btnSourceLanguage;
    private Button btnSourceLocation;
    private Button btnContinue;
    private boolean mSetLocation = false;
    private boolean mSetLanguage = false;
    private final int REQUEST_SOURCE_LOCATION = 42;
    private final int REQUEST_SOURCE_LANGUAGE = 43;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_source_audio);
        Intent i = getIntent();
        mProject = getIntent().getParcelableExtra(Project.PROJECT_EXTRA);


        btnSourceLanguage = (Button) findViewById(R.id.source_language_btn);
        btnSourceLanguage.setOnClickListener(btnClick);
        btnSourceLocation = (Button) findViewById(R.id.source_location_btn);
        btnSourceLocation.setOnClickListener(btnClick);
        btnContinue = (Button) findViewById(R.id.continue_btn);
        btnContinue.setOnClickListener(btnClick);
    }
    public void setSourceLanguage(){
        Intent intent = new Intent(this, LanguageActivity.class);
        intent.putExtra(Project.PROJECT_EXTRA, mProject);
        intent.putExtra("lang_type", "source");
        startActivityForResult(intent, REQUEST_SOURCE_LANGUAGE);
    }

    public void setSourceLocation(){
        startActivityForResult(new Intent(this, SelectSourceDirectory.class), REQUEST_SOURCE_LOCATION);
    }

    public void proceed(){
        Intent intent = new Intent();
        intent.putExtra(Project.PROJECT_EXTRA, mProject);
        setResult(RESULT_OK, intent);
        finish();
    }

    private View.OnClickListener btnClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.source_location_btn: {
                    setSourceLocation();
                    break;
                }
                case R.id.source_language_btn: {
                    setSourceLanguage();
                    break;
                }
                case R.id.continue_btn: {
                    proceed();
                    break;
                }
            }
        }
    };

    public void continueIfBothSet(){
        if(mSetLocation && mSetLanguage){
            btnContinue.setText("Continue");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_SOURCE_LOCATION){
            if(data.hasExtra(SelectSourceDirectory.SOURCE_LOCATION)){
                mProject.setSourceAudioPath(data.getStringExtra(SelectSourceDirectory.SOURCE_LOCATION));
                btnSourceLocation.setText("Source Location: " + mProject.getSourceAudioPath());
                mSetLocation = true;
                continueIfBothSet();
            }
//            if(data.hasExtra(SelectSourceDirectory.SDK_LEVEL)){
//                mProject.setSourceAudioSdkLevel(data.getStringExtra(SelectSourceDirectory.SDK_LEVEL));
//            }
        } else if(requestCode == REQUEST_SOURCE_LANGUAGE){
            if(data.hasExtra(Project.PROJECT_EXTRA)){
                mProject = data.getParcelableExtra(Project.PROJECT_EXTRA);
                btnSourceLanguage.setText("Source Language: " + mProject.getSrcLang());
                mSetLanguage = true;
                continueIfBothSet();
            }
        }
    }
}
