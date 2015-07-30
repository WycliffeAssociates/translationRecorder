package wycliffeassociates.recordingapp;

/**
 * Created by Emmanuel on 7/29/2015.
 */

import java.io.File;
import java.io.FileInputStream;

/**
 * Executes the file upload in a background thread and updates progress to
 * listeners that implement the java.beans.PropertyChangeListener interface.
 * @author www.codejava.net
 *
 */
public class UploadTask{
    private static final int BUFFER_SIZE = 4096;

    private String host;
    private int port;
    private String username;
    private String password;

    private String destDir;
    private File uploadFile;

    public UploadTask(String host, int port, String username, String password,
                      String destDir, File uploadFile) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.destDir = destDir;
        this.uploadFile = uploadFile;
    }

    /**
     * Executed in background thread
     */
    protected Void doInBackground() throws Exception {
        FTPUtility util = new FTPUtility(host, port, username, password);
        try {
            util.connect();
            util.uploadFile(uploadFile, destDir);

            FileInputStream inputStream = new FileInputStream(uploadFile);
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = -1;
            long totalBytesRead = 0;
            int percentCompleted = 0;
            long fileSize = uploadFile.length();

            inputStream.close();

            util.finish();
        } catch (FTPException ex) {
            ex.printStackTrace();
        } finally {
            util.disconnect();
        }

        return null;
    }
}
