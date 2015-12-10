package wycliffeassociates.recordingapp.FilesPage;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import android.net.Uri;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


import wycliffeassociates.recordingapp.AudioInfo;
import wycliffeassociates.recordingapp.SettingsPage.PreferencesManager;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.FileManagerUtils.AudioItem;

public class AudioFiles extends Activity {

    private DrawerLayout mDrawerLayout;

    private CheckBox btnCheckAll;

    private ImageButton
                btnSortName, btnSortDuration, btnSortDate, btnDelete,
            btnExport,
                btnExportApp, btnExportFTP, btnExportFolder, btnExportS3;

    private Menu mMenu;
    private ListView audioFileView;
    // private TextView file_path;
    private static String currentDir;
    private File file[];

    private ArrayList<AudioItem> audioItemList;
    private ArrayList<AudioItem> tempItemList;
    static ArrayList<String> exportList;

    private boolean checkAll = true;

    // 0: Z-A
    // 1: A-Z
    // 2: 0:00 - 9:99
    // 3: 9:99 - 0:00
    // 4: Oldest First
    // 5: Recent First
    int sort = 5;

    public AudioFilesAdapter adapter;

    Hashtable<Date, String> audioHash;

    /**
     * URI of the document in storage where it was exported by the user
     */
    private Uri currentUri;

    /**
     * The path to the zipfile
     */
    private String zipPath = null;

    /**
     * the current filepath being exported
     */
    private String thisPath;

    /**
     * the total number of files being exported
     */
    private int totalFiles = 0;

    /**
     * the number of the current file being exported (corresponds with allMoving)
     */
    private int fileNum =0;

    PreferencesManager pref;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_list);

        // Hide the fragment to start with
        hideFragment(R.id.file_actions);


        // Pull file directory and sorting preferences
        pref = new PreferencesManager(this);
        currentDir = (String) pref.getPreferences("fileDirectory");
        AudioInfo.fileDir = currentDir;
        sort = (int) pref.getPreferences("displaySort");

        audioFileView = (ListView) findViewById(R.id.main_content);

        // Initialization
        audioItemList = new ArrayList<AudioItem>();
        tempItemList = new ArrayList<AudioItem>();
        audioHash = new Hashtable<Date, String>();

        // Cleanup any leftover visualization files
        removeUnusedVisualizationFiles(currentDir);

        //get files in the directory
        File f = new File(currentDir);
        file = f.listFiles();
        // No files
        if (file == null) {
            Toast.makeText(AudioFiles.this, "No Audio Files in Folder", Toast.LENGTH_SHORT).show();
        }
        // Get audio files
        else {
            for (int i = 0; i < file.length; i++) {
                int len = file[i].getName().length();
                if (len > 3) {
                    String sub = file[i].getName().substring(len - 4);

                    if (sub.equalsIgnoreCase(".3gp") || sub.equalsIgnoreCase(".wav")
                            || sub.equalsIgnoreCase(".mp3")) {
                        // Add file names
                        Date lastModDate = new Date(file[i].lastModified());

                        File tFile = new File(currentDir + "/" + file[i].getName());
                        Uri uri = Uri.fromFile(tFile);

                        //String mediaPath = Uri.parse("android.resource://<your-package-name>/raw/filename").getPath();

                        //TODO : DURATION
                        /*MediaMetadataRetriever mmr = new MediaMetadataRetriever();

                        mmr.setDataSource(this, uri);
                        String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                        //System.out.println(duration);
                        int time = (Integer.parseInt(duration) / 1000);
                        mmr.release();*/

                        long time = (((tFile.length() - 44) / 2) / 44100);
                        //System.out.println("pppp" + time);

                        //create an Audio Item
                        tempItemList.add(new AudioItem(file[i].getName(), lastModDate, (int) time));
                    }

                }

                //audioFileView.setAdapter(
                generateAdapterView(tempItemList, sort);
                //);

            }
//            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

            //move this to AudioFilesAdapter -- ultimately to AudioFilesListener

//            btnExportFTP = (ImageButton) findViewById(R.id.btnExportFTP);
//            btnExportFTP.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    exportList = new ArrayList<String>();
//                    if ((file == null)) {
//                        Toast.makeText(AudioFiles.this, "Failed", Toast.LENGTH_SHORT).show();
//                    } else {
//                        for (int i = 0; i < adapter.checkBoxState.length; i++) {
//                            if (adapter.checkBoxState[i]) {
//                                exportList.add(currentDir + "/" + audioItemList.get(i).getName());
//                            }
//                        }
//                        if (exportList.size() > 0) {
//                            Intent intent = new Intent(v.getContext(), FTPActivity.class);
//                            startActivityForResult(intent, 0);
//                        } else {
//                            Toast.makeText(AudioFiles.this, "Failed", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                }
//            });

//            btnExportFolder = (ImageButton) findViewById(R.id.share_dir);
//            btnExportFolder.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    String append = "";
//                    //(String) pref.getPreferences("appName") + "/" + pref.getPreferences("deviceUUID") + "/";
//                    exportList = new ArrayList<String>();
//                    if ((file == null)) {
//                        Toast.makeText(AudioFiles.this, "Failed", Toast.LENGTH_SHORT).show();
//                    } else {
//                        for (int i = 0; i < adapter.checkBoxState.length; i++) {
//                            if (adapter.checkBoxState[i] == true) {
//                                exportList.add(currentDir + "/" + audioItemList.get(i).getName());
//                            }
//                        }
//                        if (exportList.size() > 0) {
//                            totalFiles = exportList.size();
//                            thisPath = exportList.get(0);
//                            if (exportList.size() > 1) {
//                                // We want a zip file since there are multiple files
//                                zipPath = thisPath.replaceAll("(\\.)([A-Za-z0-9]{3}$|[A-Za-z0-9]{4}$)", ".zip");
//                                // Files to zip
//                                String[] toZip = new String[totalFiles];
//                                for (int i = 0; i < totalFiles; i++) {
//                                    toZip[i] = exportList.get(i);
//                                }
//                                try {
//                                    zip(toZip, zipPath);
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                                createFile("application/zip", append + getNameFromPath(zipPath));
//                            } else//export single file over
//                                createFile("audio/*", append + getNameFromPath(thisPath));
//                        } else {
//                            Toast.makeText(AudioFiles.this, "Failed", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                }
//            });
//
//            btnExportS3 = (ImageButton) findViewById(R.id.share_amazon);
//            btnExportS3.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    // Initialize the Amazon Cognito credentials provider
//                    CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
//                            getApplicationContext(),
//                            "us-east-1:9930710e-a037-4432-b1dd-e95087fc6bdc", // Identity Pool ID
//                            Regions.US_EAST_1 // Region
//                    );
//
//                    // Initialize the Cognito Sync client
//                    CognitoSyncManager syncClient = new CognitoSyncManager(
//                            getApplicationContext(),
//                            Regions.US_EAST_1, // Region
//                            credentialsProvider);
//
//                    // Create an S3 client
//                    AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
//
//                    // Set the region of your S3 bucket
//                    s3.setRegion(Region.getRegion(Regions.US_EAST_1));
//
//                    TransferUtility transferUtility = new TransferUtility(s3, getApplicationContext());
//
//                    String append = "";
//                    File newFile = null;
//                    String name = "";
//                    exportList = new ArrayList<String>();
//                    if ((file == null)) {
//                        Toast.makeText(AudioFiles.this, "Failed", Toast.LENGTH_SHORT).show();
//                    } else {
//                        for (int i = 0; i < adapter.checkBoxState.length; i++) {
//                            if (adapter.checkBoxState[i] == true) {
//                                exportList.add(currentDir + "/" + audioItemList.get(i).getName());
//                            }
//                        }
//                        if (exportList.size() > 0) {
//                            totalFiles = exportList.size();
//                            thisPath = exportList.get(0);
//                            if (exportList.size() > 1) {
//                                //we want a zip file since there are multiple files
//                                zipPath = thisPath.replaceAll("(\\.)([A-Za-z0-9]{3}$|[A-Za-z0-9]{4}$)", ".zip");
//                                //files to zip
//                                String[] toZip = new String[totalFiles];
//                                for (int i = 0; i < totalFiles; i++) {
//                                    toZip[i] = exportList.get(i);
//                                }
//                                try {
//                                    zip(toZip, zipPath);
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                                name = zipPath;
//                                newFile = new File(name);
//                            } else {//export single file over
//
//                                name = thisPath;
//                                newFile = new File(name);
//                            }
//
//                            System.out.println("file is " + thisPath+  " and it is " + newFile.exists());
//
//                            TransferObserver observer = transferUtility.upload(
//                                    "translationrecorderbucket",     /* The bucket to upload to */
//                                    name,    /* The key for the uploaded object */
//                                    newFile        /* The file where the data to upload exists */
//                            );
//                            observer.setTransferListener(new TransferListener(){
//
//                                @Override
//                                public void onStateChanged(int id, TransferState state) {
//                                    // do something
//                                }
//
//                                @Override
//                                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
//                                    int percentage = (int) (bytesCurrent/bytesTotal * 100);
//                                    System.out.println( new Integer(percentage).toString());
//                                    //Display percentage transfered to user
//                                    if(percentage == 100){
//                                        if(mMenu != null)
//                                            mMenu.close();
//                                    }
//                                }
//
//                                @Override
//                                public void onError(int id, Exception ex) {
//                                   System.out.println( "Failed Something S3 Related, ID" + id + " EX: " + ex.toString());
//                                }
//
//                            });
//                        } else {
//                            Toast.makeText(AudioFiles.this, "Failed", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                }
//            });


            btnCheckAll = (CheckBox) findViewById(R.id.btnCheckAll);
            btnCheckAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (file == null) {
                    }
                    else {
                        for (int i = 0; i < audioFileView.getCount(); i++) {
                            adapter.checkBoxState[i] = checkAll;
                            adapter.notifyDataSetChanged();
                        }
                        if (!checkAll) {
                            exportList = new ArrayList<String>();
                            if(adapter != null && adapter.checkBoxState != null){
                                Arrays.fill(adapter.checkBoxState, Boolean.FALSE);
                                adapter.notifyDataSetChanged();
                            }
                            btnCheckAll.setButtonDrawable(R.drawable.ic_select_all_empty);
                            hideFragment(R.id.file_actions);
                        } else {
                            btnCheckAll.setButtonDrawable(R.drawable.ic_select_all_selected);
                            showFragment(R.id.file_actions);
                        }
                        checkAll = !checkAll;
                    }
                }
            });

            btnSortName = (ImageButton) findViewById(R.id.btnSortName);
            btnSortName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (sort == 1) {
                        pref.setPreferences("displaySort", 0);
                    } else {
                        pref.setPreferences("displaySort", 1);
                    }
                    sort = (int) pref.getPreferences("displaySort");
                    generateAdapterView(tempItemList, sort);
                }
            });

            btnSortDuration = (ImageButton) findViewById(R.id.btnSortDuration);
            btnSortDuration.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (sort == 3) {
                        pref.setPreferences("displaySort", 2);
                    } else {
                        pref.setPreferences("displaySort", 3);
                    }
                    sort = (int) pref.getPreferences("displaySort");
                    generateAdapterView(tempItemList, sort);
                }
            });

            btnSortDate = (ImageButton) findViewById(R.id.btnSortDate);
            btnSortDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (sort == 5) {
                        pref.setPreferences("displaySort", 4);
                    } else {
                        pref.setPreferences("displaySort", 5);
                    }
                    sort = (int) pref.getPreferences("displaySort");
                    generateAdapterView(tempItemList, sort);
                }
            });
        }
    }

    public void showShareDialog(View v){
        FragmentManager fm = getFragmentManager();
        FragmentShareDialog d = new FragmentShareDialog();
        d.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        d.show(fm, "Share Dialog");
    }

    public void showDeleteConfirmDialog(View v) {
        FragmentManager fm = getFragmentManager();
        FragmentDeleteDialog d = new FragmentDeleteDialog();
        d.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        d.show(fm, "Delete Confirm Dialog");
    }

    public void confirmDelete() {
        exportList = new ArrayList<String>();
        for (int i = 0; i < adapter.checkBoxState.length; i++) {
            if (adapter.checkBoxState[i]) {
                exportList.add(currentDir + "/" + audioItemList.get(i).getName());
            }
        }
        if (exportList.size() > 0) {
            deleteFiles(exportList);
            Toast.makeText(AudioFiles.this, "File has been deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(AudioFiles.this, "Select a file to delete", Toast.LENGTH_SHORT).show();
        }
        sort = (int) pref.getPreferences("displaySort");
        generateAdapterView(tempItemList, sort);
        hideFragment(R.id.file_actions);
    }

    private void removeUnusedVisualizationFiles(String filesDir){
        File audioFilesLocation = new File(filesDir);
        File visFilesLocation = new File(AudioInfo.pathToVisFile);
        File[] visFiles = visFilesLocation.listFiles();
        File[] audioFiles = audioFilesLocation.listFiles();
        for(File v : visFiles){
            boolean found = false;
            for(File a : audioFiles){
                //check if the names match up; exclude the path to get to them or the file extention
                if(extractFilename(a).equals(extractFilename(v))){
                    found = true;
                    break;
                }
            }
            if(!found){
                System.out.println("Removing " + v.getName());
                v.delete();
            }
        }
    }

    private String extractFilename(File a){
        if(a.isDirectory()){
            return "";
        }
        String nameWithExtention = a.getName();
        if(nameWithExtention.lastIndexOf('.') < 0 || nameWithExtention.lastIndexOf('.') > nameWithExtention.length()){
            return "";
        }
        String filename = nameWithExtention.substring(0, nameWithExtention.lastIndexOf('.'));
        return filename;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mMenu = menu;
        return true;
    }

    /**
     *Clears Check Box State when the back button is pressed
     */
    public void onBackPressed(){
        if (file == null) {}
        else {
            exportList = new ArrayList<String>();
            if(adapter != null && adapter.checkBoxState != null)
            Arrays.fill(adapter.checkBoxState, Boolean.FALSE);
        }
        finish();
    }

    private void generateAdapterView(ArrayList<AudioItem> tempItemList, int sort){
        //
        ArrayList<AudioItem> cleanList = new ArrayList<AudioItem>();
        for (int a = 0; a < tempItemList.size(); a++){
            cleanList.add(tempItemList.get(a));
        }

        // Clear list
        audioItemList = new ArrayList<AudioItem>();
        audioItemList = sortAudioItem(cleanList, sort);

        // Temp array for Adapter
        AudioItem[] tempArr = new AudioItem[audioItemList.size()];
        for(int a = 0; a < audioItemList.size(); a++){
            tempArr[a] = audioItemList.get(a);

        }

        // Set Adapter view
        adapter = (new AudioFilesAdapter(this, tempArr));
        audioFileView.setAdapter(adapter);
    }

    // TODO : after merge, ezpz implement
    private void deleteFiles(ArrayList<String> exportList){
        //int count = 0;

        for (int i = 0; i < exportList.size(); i++) {
            File file = new File(exportList.get(i));
//            boolean deleted = file.delete();
//            if (deleted){
            if (file.delete()) {
                String value = exportList.get(i).replace(currentDir + "/", "");
                for (int a = 0; a < tempItemList.size(); a++) {
                    if (tempItemList.get(a).getName().equals(value)){
                        tempItemList.remove(a);
                        a = tempItemList.size() + 2;
                    }
                }
                //tempItemList.remove(i - count);
                //System.out.println("========" + (i - count));
                //count++;
            }
        }
    }

    private ArrayList<AudioItem> sortAudioItem(ArrayList<AudioItem> nList, int sort) {
        //
        ArrayList<AudioItem> outputList = new ArrayList<AudioItem>();
        if (nList.size() > 0) {
            boolean flag = false;
            switch (sort) {
                case 0:
                case 2:
                case 4:
                    //false
                    break;
                case 1:
                case 3:
                case 5:
                default:
                    flag = true;
                    break;
            }

            int val = 0;
            int size = nList.size() - 1;

            if (sort == 0 || sort == 1) {
                String cmp = "";

                // As long as there are items...
                do {
                    //
                    size = nList.size() - 1;
                    cmp = nList.get(size).getName().toLowerCase();
                    val = size;

                    // Compare with other items
                    for (int x = 0; x < size; x++) {
                        if (cmp.compareTo(nList.get(x).getName().toLowerCase()) < 0) {
                            if (flag) {
                                //A-Z
                            } else {
                                //Z-A
                                val = x;
                                cmp = nList.get(x).getName();
                            }
                        } else {
                            if (flag) {
                                //A-Z
                                val = x;
                                cmp = nList.get(x).getName();
                            } else {
                                //Z-A
                            }
                        }
                    }

                    //
                    outputList.add(nList.get(val));
                    nList.remove(val);

                } while (size > 0);

            } else if (sort == 2 || sort == 3) {
                //
                Integer cmp = 0;

                // As long as there are items...
                do {
                    size = nList.size() - 1;
                    cmp = nList.get(size).getDuration();
                    val = size;

                    // Compare with other items
                    for (int x = 0; x < size; x++) {
                        if (cmp > nList.get(x).getDuration()) {
                            if (flag) {
                                //A-Z
                            } else {
                                //Z-A
                                val = x;
                                cmp = nList.get(x).getDuration();
                            }
                        } else {
                            if (flag) {
                                //A-Z
                                val = x;
                                cmp = nList.get(x).getDuration();
                            } else {
                                //Z-A
                            }
                        }
                    }

                    //
                    outputList.add(nList.get(val));
                    nList.remove(val);

                } while (size > 0);

            } else {
                //
                ArrayList<Date> tempList = new ArrayList<Date>();
                Date cmp = new Date();

                // As long as there are items
                do {
                    //
                    size = nList.size() - 1;
                    cmp = nList.get(size).getDate();
                    val = size;

                    // Compare with other items
                    for (int x = 0; x < size; x++) {
                        if (cmp.after(nList.get(x).getDate())) {
                            if (flag) {
                                //A-Z
                            } else {
                                //Z-A
                                val = x;
                                cmp = nList.get(x).getDate();
                            }
                        } else {
                            if (flag) {
                                // A-Z
                                val = x;
                                cmp = nList.get(x).getDate();
                            } else {
                                // Z-A
                            }
                        }
                    }

                    //
                    outputList.add(nList.get(val));
                    nList.remove(val);

                } while (size > 0);
            }

        } else {
            System.out.println("empty");
        }

        return outputList;
    }

    //==================================
    //      Export to Applications
    //==================================

    /**
     *  Passes URIs to relevant audio applications.
     *
     *      @param exportList
     *          a list of filenames to be exported
     */
    private void exportApplications(ArrayList<String> exportList, String append){

        Intent sendIntent = new Intent();
        sendIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        //share it implementation
        File tFile;

        //individual file
        if(exportList.size() < 2){
            Uri audioUri;

            tFile = new File (exportList.get(0));
            audioUri = Uri.fromFile(tFile);
            sendIntent.setAction(Intent.ACTION_SEND);
            String filename = exportList.get(0).replace(currentDir + "/", "");
            sendIntent.putExtra(Intent.EXTRA_TITLE, append + filename);

            //send individual URI
            sendIntent.putExtra(Intent.EXTRA_STREAM, audioUri);

            //multiple files
        }else{

            ArrayList<Uri> audioUris = new ArrayList<Uri>();
            for(int i=0; i<exportList.size(); i++){
                tFile = new File(exportList.get(i));
                audioUris.add(Uri.fromFile(tFile));
            }
            sendIntent.setAction(Intent.ACTION_SEND_MULTIPLE);

            //send multiple arrayList of URIs
            sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, audioUris);
        }

        //open
        sendIntent.setType("audio/*");
        startActivity(Intent.createChooser(sendIntent, "Export Audio"));
    }

    /**
     *  Passes zip file URI to relevant audio applications.
     *      @param path
     *      a list of filenames to be exported
     */
    private void exportZipApplications(String path){

        Intent sendIntent = new Intent();
        sendIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        //share it implementation
        File tFile;

        Uri audioUri;

        tFile = new File (path);
        audioUri = Uri.fromFile(tFile);
        sendIntent.setAction(Intent.ACTION_SEND);

        //send individual URI
        sendIntent.putExtra(Intent.EXTRA_STREAM, audioUri);

        //open
        sendIntent.setType("application/zip");
        startActivityForResult(Intent.createChooser(sendIntent, "Export Zip"), 3);
    }

    //==================================
    //         Export to Folder
    //==================================

    /**
     * Iterates the file number that is being looked a
     * @return Returns true if iteration worked, false if the end has been reached
     */
    public boolean iteratePath(){
        if(fileNum + 1 < totalFiles) {
            fileNum++;
            return true;
        }
        if(fileNum + 1 == totalFiles){
            fileNum++;
            return false;
        }
        return false;
    }

    /**
     * A method to extract filename from the path
     * @param path The paths to the files
     * @return The simple filename of the file
     */
    public String getNameFromPath(String path){
        String[] temp = path.split("/");
        return temp[temp.length-1];
    }

    /**
     * Copies a file from a path to a uri
     * @param destUri The destination of the file
     * @param path The original path to the file
     */
    public void savefile(Uri destUri, String path)
    {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            String sourceFilename = path;
            ParcelFileDescriptor destinationFilename = getContentResolver().
                    openFileDescriptor(destUri, "w");
            bis = new BufferedInputStream(new FileInputStream(sourceFilename));
            bos = new BufferedOutputStream(new FileOutputStream(destinationFilename.getFileDescriptor()));
            byte[] buf = new byte[1024];
            bis.read(buf);
            do {
                bos.write(buf);
            } while(bis.read(buf) != -1);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) bis.close();
                if (bos != null) bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //not very well abstracted, but if we are working with non-zip-files
            //keep saving files
            if(!path.contains(".zip")) {
                iteratePath();
                if (fileNum < totalFiles) {
                    thisPath = exportList.get(fileNum);
                    createFile("audio/*", getNameFromPath(thisPath));
                }
            }
            else//we just transferred a zip file, the old file needs to be deleted
            {
                File toDelete = new File(path);
                try {
                    toDelete.getCanonicalFile().delete();
                }
                catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Creates a file in folder selected by user
     * @param mimeType Typically going to be "audio/*" for this app
     * @param fileName The name of the file selected.
     */
    private void createFile(String mimeType, String fileName) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT );
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        this.startActivityForResult(intent, 43);
    }

    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        currentUri = null;
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 43) {
                currentUri = resultData.getData();
                if(null!= zipPath){
                    savefile(currentUri, zipPath);
                    zipPath = null;//reset
                }//
                else
                    savefile(currentUri, thisPath);
            }

            if(requestCode ==3){//delete zip file, needs to be done after upload
                zipPath = null;//set null for next time
            }
        }
    }

    //==================================
    //         Zipping files
    //==================================

    /**
     * Zips files into a single folder
     * @param files A String array of the paths to the files to be zipped
     * @param zipFile The location of the zip file as a String
     * @throws IOException
     */
    public static void zip(String[] files, String zipFile) throws IOException {
        BufferedInputStream origin = null;
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
        try {
            byte data[] = new byte[1024];

            for (int i = 0; i < files.length; i++) {
                FileInputStream fi = new FileInputStream(files[i]);
                origin = new BufferedInputStream(fi, 1024);
                try {
                    ZipEntry entry = new ZipEntry(files[i].substring(files[i].lastIndexOf("/") + 1));
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, 1024)) != -1) {
                        out.write(data, 0, count);
                    }
                }
                finally {
                    origin.close();
                }
            }
        }
        finally {
            out.close();
        }
    }



    public void hideFragment(int view) {
        View fragment = findViewById(view);
        if (fragment.getVisibility() == View.VISIBLE) {
            fragment.setVisibility(View.GONE);
        }
    }

    public void showFragment(int view) {
        View fragment = findViewById(view);
        if (fragment.getVisibility() == View.GONE) {
            fragment.setVisibility(View.VISIBLE);
        }
    }

}
