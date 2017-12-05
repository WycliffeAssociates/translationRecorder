package org.wycliffeassociates.translationrecorder.FilesPage.Export;

import net.gotev.uploadservice.http.HttpConnection;
import net.gotev.uploadservice.http.impl.HurlStack;
import net.gotev.uploadservice.http.impl.HurlStackConnection;

import java.io.IOException;
import java.lang.reflect.Field;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

/**
 * Implementation of the OkHttp Stack.
 * @author Aleksandar Gotev
 */
public class SSLHttpStack extends HurlStack {

  SSLSocketFactory sslSocketFactory;

  public SSLHttpStack(SSLSocketFactory sslSocketFactory) {
    super();
    this.sslSocketFactory = sslSocketFactory;
  }

  @Override
  public HttpConnection createNewConnection(String method, String url) throws IOException {
    HurlStackConnection hsc = (HurlStackConnection) super.createNewConnection(method, url);
    try {
      Field field = hsc.getClass().getDeclaredField("mConnection");
      field.setAccessible(true);
      ((HttpsURLConnection)field.get(hsc)).setSSLSocketFactory(sslSocketFactory);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return hsc;
  }
}