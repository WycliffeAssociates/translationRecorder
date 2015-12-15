package wycliffeassociates.recordingapp.FilesPage.Export;

import android.app.Fragment;
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

import java.io.File;
import java.util.ArrayList;

import wycliffeassociates.recordingapp.FileManagerUtils.AudioItem;
import wycliffeassociates.recordingapp.FilesPage.AudioFilesAdapter;

/**
 * Created by sarabiaj on 12/10/2015.
 */
public class S3Export extends Export {
    CognitoCachingCredentialsProvider mCredentialsProvider;
    CognitoSyncManager mSyncClient;
    AmazonS3 mS3;
    TransferUtility mTransferUtility;

    /**
     * Creates an Export object to target AmazonS3
     * @param audioItemList
     * @param adapter
     * @param currentDir
     * @param ctx
     */
    public S3Export(ArrayList<AudioItem> audioItemList, AudioFilesAdapter adapter, String currentDir, Fragment ctx){
        super(audioItemList, adapter, currentDir, ctx);
        init();
    }

    /**
     * Initializes credentials to export to Door43
     */
    //TODO: set up credentials for door43
    //TODO: store credentials in a text file that has a git ignore
    private void init(){
        mCredentialsProvider = new CognitoCachingCredentialsProvider(
                mCtx.getActivity().getApplicationContext(),
                "us-east-1:9930710e-a037-4432-b1dd-e95087fc6bdc", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );

        // Initialize the Cognito Sync client
        mSyncClient = new CognitoSyncManager(
                mCtx.getActivity().getApplicationContext(),
                Regions.US_EAST_1, // Region
                mCredentialsProvider);

        // Create an S3 client
        mS3 = new AmazonS3Client(mCredentialsProvider);

        // Set the region of your S3 bucket
        mS3.setRegion(Region.getRegion(Regions.US_EAST_1));

        mTransferUtility = new TransferUtility(mS3, mCtx.getActivity().getApplicationContext());

    }

    /**
     * Uploads the selected files to AmazonS3 using the credentials set up in init()
     */
    @Override
    public void export(){
        String name = null;
        if(mExportList.size() > 0){
            if(mZipPath == null){
                name = mExportList.get(0);
            } else {
                name = mZipPath;
            }

            File fileToUpload = new File(name);
            TransferObserver observer = mTransferUtility.upload(
                    "translationrecorderbucket",     /* The bucket to upload to */
                    name,    /* The key for the uploaded object */
                    fileToUpload        /* The file where the data to upload exists */
            );
            observer.setTransferListener(new TransferListener(){

                @Override
                public void onStateChanged(int id, TransferState state) {
                    // do something
                }

                //TODO: progress bar
                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                    int percentage = (int) (bytesCurrent/bytesTotal * 100);
                    System.out.println( new Integer(percentage).toString());
                    //Display percentage transfered to user
                    if(percentage == 100){
//                        if(mMenu != null)
//                            mMenu.close();
                    }
                }

                @Override
                public void onError(int id, Exception ex) {
                    System.out.println( "Failed Something S3 Related, ID" + id + " EX: " + ex.toString());
                }

            });
        }
    }
}
