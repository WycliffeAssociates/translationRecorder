package wycliffeassociates.recordingapp.project;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.app.FragmentManager;
import android.view.View;
import android.widget.Button;

import wycliffeassociates.recordingapp.ProjectManager.Project;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.project.adapters.TargetLanguageAdapter;

/**
 * Created by sarabiaj on 5/25/2016.
 */
public class SourceAudioActivity extends Activity implements ScrollableListFragment.OnItemClickListener{
    private Project mProject;
    private Button btnSourceLanguage;
    private Button btnSourceLocation;
    private Button btnContinue;
    private boolean mSetLocation = false;
    private boolean mSetLanguage = false;
    private final int REQUEST_SOURCE_LOCATION = 42;
    private final int REQUEST_SOURCE_LANGUAGE = 43;
    private Fragment mFragment;
    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_source_audio);
        Intent i = getIntent();
        mProject = getIntent().getParcelableExtra(Project.PROJECT_EXTRA);
        mFragmentManager = getFragmentManager();

        btnSourceLanguage = (Button) findViewById(R.id.language_btn);
        btnSourceLanguage.setOnClickListener(btnClick);
        btnSourceLocation = (Button) findViewById(R.id.location_btn);
        btnSourceLocation.setOnClickListener(btnClick);
        btnContinue = (Button) findViewById(R.id.continue_btn);
        btnContinue.setOnClickListener(btnClick);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void setSourceLanguage(){
        mFragment = new ScrollableListFragment.Builder(new TargetLanguageAdapter(ParseJSON.getLanguages(this), this)).setSearchHint("Choose Target Language:").build();
        mFragmentManager.beginTransaction().add(R.id.fragment_container, mFragment).commit();
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
                case R.id.location_btn: {
                    setSourceLocation();
                    break;
                }
                case R.id.language_btn: {
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
        }
    }

    @Override
    public void onItemClick(Object result) {
        mProject.setSourceLanguage(((Language)result).getCode());
        btnSourceLanguage.setText("Source Language: " + mProject.getSrcLang());
        mSetLanguage = true;
        mFragmentManager.beginTransaction().remove(mFragment).commit();
        continueIfBothSet();
    }
}
