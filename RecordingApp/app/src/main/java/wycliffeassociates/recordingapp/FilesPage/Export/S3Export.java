package wycliffeassociates.recordingapp.FilesPage.Export;

import android.provider.Settings;
import android.widget.Toast;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;

import java.io.File;
import java.util.ArrayList;

import wycliffeassociates.recordingapp.FileManagerUtils.FileItem;
import wycliffeassociates.recordingapp.FilesPage.AudioFilesAdapter;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.Reporting.Logger;

/**
 * Created by sarabiaj on 12/10/2015.
 */
public class S3Export extends Export {
    BasicAWSCredentials mCredentialsProvider;
    AmazonS3 mS3;
    TransferUtility mTransferUtility;
    TransferListener mListener;
    Export mExp;

    /**
     * Creates an Export object to target AmazonS3
     * @param fileItemList
     * @param adapter
     * @param currentDir
     */
    public S3Export(ArrayList<FileItem> fileItemList, AudioFilesAdapter adapter, String currentDir){
        super(fileItemList, adapter, currentDir);
        mExp = this;
    }

    /**
     * Initializes credentials to export to Door43
     */
    private void init(){

        mCredentialsProvider = new BasicAWSCredentials(mCtx.getResources().getString(R.string.door43_key), mCtx.getResources().getString(R.string.door43_secret_access));
        // Create an S3 client
        ClientConfiguration cc = new ClientConfiguration();
        cc.setSocketTimeout(0);
        mS3 = new AmazonS3Client(mCredentialsProvider);
        // Set the region of your S3 bucket
        mS3.setRegion(Region.getRegion(Regions.US_WEST_2));
        mTransferUtility = new TransferUtility(mS3, mCtx.getActivity().getApplicationContext());
        mListener = new TransferListener() {

            @Override
            protected void finalize() throws Throwable {
                Logger.e(this.toString(), "finalized called");
                super.finalize();
            }

            @Override
            public void onStateChanged(int id, TransferState state) {
                if(state.equals(TransferState.COMPLETED)){
                    Toast.makeText(mCtx.getActivity(), "Uploaded file successfully.", Toast.LENGTH_SHORT).show();
                    Logger.w(this.toString(), "successfully sent file to s3");
                    cleanUp();
                    mCtx.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressCallback.dismissProgress();
                        }
                    });
                    mProgressCallback.setExporting(false);
                }
                Logger.e(this.toString(), "state is " + state.toString());
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                if(bytesTotal > 0) {
                    int percentage = (int) (bytesCurrent / (float)bytesTotal * 100);
                    mProgressCallback.setUploadProgress(percentage);
                }
            }

            @Override
            public void onError(int id, Exception ex) {
                Logger.e(this.toString(), "Failed Something S3 Related, ID" + id + " EX: " + ex.toString());
                Toast.makeText(mCtx.getActivity(), "ERROR: Upload file failed!", Toast.LENGTH_SHORT).show();
                mProgressCallback.setExporting(false);
            }
        };
    }

    /**
     * Uploads the selected files to AmazonS3 using the credentials set up in init()
     */
    @Override
    public void export() {
        if (Export.shouldZip(mExportList)) {
            zipFiles(this);
        } else {
            handleUserInput();
        }
    }

    @Override
    protected void handleUserInput() {
        upload();
    }

    protected void upload(){
        mProgressCallback.setExporting(true);
        init();
        String name = null;
        if (mExportList.size() > 0) {
            if (mZipPath == null) {
                name = mExportList.get(0);
            } else {
                name = mZipPath;
            }

            File fileToUpload = new File(name);
            name = name.substring(name.lastIndexOf('/'), name.length());
            name = Settings.Secure.getString(mCtx.getActivity().getContentResolver(),
                    Settings.Secure.ANDROID_ID) + name;

            mProgressCallback.showProgress(ProgressUpdateCallback.UPLOAD);

            TransferObserver observer = mTransferUtility.upload(
                    mCtx.getResources().getString(R.string.door43_bucket),     /* The bucket to upload to */
                    name,    /* The key for the uploaded object */
                    fileToUpload        /* The file where the data to upload exists */
            );
            observer.setTransferListener(mListener);
        }
    }
}
