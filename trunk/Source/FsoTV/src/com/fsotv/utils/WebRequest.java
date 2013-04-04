package com.fsotv.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.util.Log;

public class WebRequest {
  public enum PostType{
    GET, POST;
  }

  public static InputStream GetStream(String url, PostType postType) {
    HttpClient httpclient = new DefaultHttpClient();
    CookieStore cookieStore = new BasicCookieStore();
    InputStream is = null;
    Log.e("URL", url);
    try {
      // Create local HTTP context
      HttpContext localContext = new BasicHttpContext();

      // Bind custom cookie store to the local context
      localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

      HttpResponse httpresponse;
      if (postType == PostType.POST)
      {
        HttpPost httppost = new HttpPost(url);
        httpresponse = httpclient.execute(httppost, localContext);
      }
      else
      {
        HttpGet httpget = new HttpGet(url);
        httpresponse = httpclient.execute(httpget, localContext);
      }
      is = httpresponse.getEntity().getContent();
      //StringBuilder responseString = inputStreamToString(is);
      //response = responseString.toString();
    }
    catch (UnknownHostException e) {
      e.printStackTrace();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    
    return is;
  }

  private static StringBuilder inputStreamToString(InputStream is) throws IOException {
    String line = "";
    StringBuilder total = new StringBuilder();
    // Wrap a BufferedReader around the InputStream
    BufferedReader rd = new BufferedReader(new InputStreamReader(is,Charset.forName("iso-8859-9")));
    // Read response until the end
    while ((line = rd.readLine()) != null) {
      total.append(line);
    }

    // Return full string
    return total;
  }
}
