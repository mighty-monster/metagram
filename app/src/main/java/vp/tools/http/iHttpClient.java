package vp.tools.http;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;



import static vp.metagram.general.functions.getSSLContext;


public class iHttpClient
{
    SSLContext sslContext;
    HostnameVerifier hostnameVerifier = (s, sslSession) -> true;

    int timeout = 7500;

    public iHttpClient(Context appContext) throws GeneralSecurityException, IOException
    {
        sslContext = getSSLContext(appContext);

        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException
                    {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException
                    {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers()
                    {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
        };

        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
    }


    public String httpGet(String _url, Map<String, String> _params) throws IOException
    {

        if (_params.size() > 0)
        {_url = _url + "?";}

        _url = "http:/" + _url;
        for (String key:_params.keySet())
        {_url = _url + String.format("%s=%s&",key,_params.get(key));}
        _url = _url.substring(0,_url.length() - 1);

        URL url = new URL(_url);
        HttpURLConnection urlConnection =
                (HttpURLConnection) url.openConnection();

        urlConnection.setConnectTimeout(timeout);

        InputStream in = urlConnection.getInputStream();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null)
        {total.append(line).append('\n');}

        return total.toString();

    }

    public String httpPost(String _url, String _body) throws IOException
    {
        _url = "http://" + _url;
        URL url = new URL(_url);
        HttpURLConnection urlConnection =
                (HttpURLConnection) url.openConnection();
        urlConnection.setConnectTimeout(timeout);

        urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);
        urlConnection.setRequestMethod("POST");
        urlConnection.setUseCaches(false);
        urlConnection.setRequestProperty("Content-Type", "text/plain");

        OutputStream outputStream = urlConnection.getOutputStream();
        outputStream.write(_body.getBytes("UTF-8"));
        outputStream.flush();
        outputStream.close();


        InputStream in = urlConnection.getInputStream();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null)
        {total.append(line).append('\n');}


        return total.toString();

    }


    public String httpsGet(String _url, Map<String,String> _params) throws IOException
    {

        if (_params.size() > 0)
        {_url = _url + "?";}

        _url = "https:/" + _url;
        for (String key:_params.keySet())
        {_url = _url + String.format("%s=%s&",key,_params.get(key));}
        _url = _url.substring(0,_url.length() - 1);

        URL url = new URL(_url);
        HttpsURLConnection urlConnection =
                (HttpsURLConnection) url.openConnection();

        urlConnection.setConnectTimeout(timeout);

        urlConnection.setHostnameVerifier(hostnameVerifier);
        urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
        InputStream in = urlConnection.getInputStream();


        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null)
        {total.append(line).append('\n');}

        return total.toString();

    }

    public String httpsPost(String _url, String _body) throws IOException
    {

        _url = "https://" + _url;


        URL url = new URL(_url);
        HttpsURLConnection urlConnection =
                (HttpsURLConnection) url.openConnection();


        urlConnection.setConnectTimeout(timeout);

        urlConnection.setHostnameVerifier(hostnameVerifier);
        urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
        urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);
        urlConnection.setRequestMethod("POST");
        urlConnection.setUseCaches(false);
        urlConnection.setRequestProperty("Content-Type", "text/plain");


        OutputStream outputStream = urlConnection.getOutputStream();
        outputStream.write(_body.getBytes("UTF-8"));
        outputStream.flush();
        outputStream.close();

        InputStream in = urlConnection.getInputStream();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null)
        {total.append(line).append('\n');}

        return total.toString();

    }




}
