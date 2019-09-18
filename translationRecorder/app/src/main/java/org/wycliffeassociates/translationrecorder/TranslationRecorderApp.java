package org.wycliffeassociates.translationrecorder;

import android.app.Application;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.okhttp.OkHttpStack;

import org.wycliffeassociates.translationrecorder.FilesPage.DirectoryProvider;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import okhttp3.OkHttpClient;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;

/**
 * Created by sarabiaj on 11/28/2017.
 */

public class TranslationRecorderApp extends Application implements DirectoryProvider {

    private static Context context;

    //some arbitrarily larger buffer
    int minBufferSize = 10 * AudioTrack.getMinBufferSize(AudioInfo.SAMPLERATE,
    AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

    AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, AudioInfo.SAMPLERATE,
                            AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                            minBufferSize, AudioTrack.MODE_STREAM);

    ProjectDatabaseHelper database = new ProjectDatabaseHelper(this);

    @Override
    public void onCreate() {
        super.onCreate();
        // setup the broadcast action namespace string which will
        // be used to notify upload status.
        // Gradle automatically generates proper variable as below.
        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;
        UploadService.HTTP_STACK = new OkHttpStack(getOkHttpClient());

        context = this;
    }

    private OkHttpClient getOkHttpClient() {
        return new OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .retryOnConnectionFailure(true)
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(0, TimeUnit.SECONDS) // unlimited timeout
                .readTimeout(30, TimeUnit.SECONDS)
                //.sslSocketFactory(getSSLSocketFactory())
                .build();
    }

    public SSLSocketFactory getSSLSocketFactory() throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        // Load CAs from an InputStream
        // (could be from a resource or ByteArrayInputStream or ...)
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        // From https://www.washington.edu/itconnect/security/ca/load-der.crt
        InputStream caInput = new BufferedInputStream(getAssets().open("rootCA.crt"));
        Certificate ca;
        try {
            ca = cf.generateCertificate(caInput);
            System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
        } finally {
            caInput.close();
        }

        // Create a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        // Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        // Create an SSLContext that uses our TrustManager
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, tmf.getTrustManagers(), null);
        return context.getSocketFactory();
    }

    public File getUploadDirectory() {
        return new File(this.getExternalCacheDir(), "upload");
    }

    public AudioTrack getAudioTrack() {
        return audioTrack;
    }

    public int getTrackBufferSize() {
        return minBufferSize;
    }

    public ProjectDatabaseHelper getDatabase() {
        return database;
    }

    public static Context getContext() {
        return context;
    }
}
