package wycliffeassociates.recordingapp;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import java.io.File;

import wycliffeassociates.recordingapp.FilesPage.FileNameExtractor;
import wycliffeassociates.recordingapp.Recording.RecordingScreen;
import wycliffeassociates.recordingapp.SettingsPage.Settings;

/**
 * Created by sarabiaj on 3/16/2016.
 */
public class RerecordDialog extends ExitDialog {

    public static RerecordDialog Build(Activity activity, int theme, boolean loadedFile, boolean isPlaying, String filename){
        RerecordDialog exit = new RerecordDialog(activity, theme);
        exit.setFilename(filename);
        exit.setIsPlaying(isPlaying);
        exit.setLoadedFile(loadedFile);

        File file = new File(filename);
        FileNameExtractor fne = new FileNameExtractor(file);
        if(fne.matched()) {
            Settings.updateFilename(activity, fne.getLang(), fne.getSource(), fne.getBook(),
                    fne.getChapter(), fne.getChunk());
        }
        if(loadedFile){
            Intent intent = new Intent(activity, RecordingScreen.class);
            activity.startActivity(intent);
            activity.finish();
            return null;
        }
        return exit;
    }

    public RerecordDialog(Activity a, int theme) {
        super(a, theme);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()) {
            case R.id.btnSave:
                dismiss();
                break;
            case R.id.btnDelete: {
                if (filename != null){
                    File file = new File(filename);
                    file.delete();
                    Intent intent = new Intent(activity, RecordingScreen.class);
                    activity.startActivity(intent);
                    activity.finish();
                }
                break;
            }
            default:{
                System.out.println("Exit dialog hit the default statement.");
                break;
            }
        }
        dismiss();
    }
}
