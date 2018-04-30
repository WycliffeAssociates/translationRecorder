package org.wycliffeassociates.translationrecorder

import android.app.Application
import android.preference.PreferenceManager
import com.door43.tools.reporting.GlobalExceptionHandler
import com.door43.tools.reporting.Logger
import net.gotev.uploadservice.UploadService
import net.gotev.uploadservice.okhttp.OkHttpStack
import okhttp3.OkHttpClient
import org.wycliffeassociates.translationrecorder.FilesPage.DirectoryProvider
import java.io.BufferedInputStream
import java.io.File
import java.io.IOException
import java.security.KeyManagementException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory

/**
 * Created by sarabiaj on 11/28/2017.
 */

class TranslationRecorderApp : Application(), DirectoryProvider {

    private val okHttpClient: OkHttpClient
        @Throws(CertificateException::class, NoSuchAlgorithmException::class, KeyStoreException::class, KeyManagementException::class, IOException::class)
        get() = OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .retryOnConnectionFailure(true)
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .sslSocketFactory(sslSocketFactory)
                .build()

    // Load CAs from an InputStream
    // (could be from a resource or ByteArrayInputStream or ...)
    // From https://www.washington.edu/itconnect/security/ca/load-der.crt
    // Create a KeyStore containing our trusted CAs
    // Create a TrustManager that trusts the CAs in our KeyStore
    // Create an SSLContext that uses our TrustManager
    val sslSocketFactory: SSLSocketFactory
        @Throws(CertificateException::class, IOException::class, KeyStoreException::class, NoSuchAlgorithmException::class, KeyManagementException::class)
        get() {
            val cf = CertificateFactory.getInstance("X.509")
            val caInput = BufferedInputStream(assets.open("rootCA.crt"))
            val ca: Certificate
            try {
                ca = cf.generateCertificate(caInput)
                println("ca=" + (ca as X509Certificate).subjectDN)
            } finally {
                caInput.close()
            }
            val keyStoreType = KeyStore.getDefaultType()
            val keyStore = KeyStore.getInstance(keyStoreType)
            keyStore.load(null, null)
            keyStore.setCertificateEntry("ca", ca)
            val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
            val tmf = TrustManagerFactory.getInstance(tmfAlgorithm)
            tmf.init(keyStore)
            val context = SSLContext.getInstance("TLS")
            context.init(null, tmf.trustManagers, null)
            return context.socketFactory
        }

    private fun configureLogger(minLogLevel: Int, logDir: File) {
        val logFile = File(logDir, "log.txt")
        logFile.createNewFile()
        Logger.configure(logFile, Logger.Level.getLevel(minLogLevel))
        if (logFile.exists()) {
            Logger.w(this.toString(), "SUCCESS: Log file initialized.")
        } else {
            Logger.e(this.toString(), "ERROR: could not initialize log file.")
        }
    }

    override fun onCreate() {
        super.onCreate()
        // setup the broadcast action namespace string which will
        // be used to notify upload status.
        // Gradle automatically generates proper variable as below.
        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID
        UploadService.HTTP_STACK = OkHttpStack(OkHttpClient())


        //configure logger
        val dir = File(externalCacheDir, MainMenu.STACKTRACE_DIR)
        dir.mkdirs()
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        GlobalExceptionHandler.register(dir)
        val minLogLevel = Integer.parseInt(pref.getString(MainMenu.KEY_PREF_LOGGING_LEVEL, MainMenu.PREF_DEFAULT_LOGGING_LEVEL))
        configureLogger(minLogLevel, dir)
    }

    override fun getUploadDirectory(): File {
        return File(this.externalCacheDir, "upload")
    }
}
