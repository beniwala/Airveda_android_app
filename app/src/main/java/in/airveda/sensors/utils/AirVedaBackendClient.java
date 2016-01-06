package in.airveda.sensors.utils;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.entity.StringEntity;

/**
 * Created by siddhartha on 03/11/15.
 */
public class AirVedaBackendClient {
//    private static final String BASE_URL = "https://www.google.co.in/#q=hi";
    private static final String BASE_URL = "http://sashooj.me:8000/";


    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void setAuth(String username, String pass){
        client.setBasicAuth(username, pass);
    }

    public static void clearCredentials(){
        client.clearCredentialsProvider();
    }

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void getByURL(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(url, params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void postJSON(Context context, String url, JSONObject jsonObject, AsyncHttpResponseHandler responseHandler){
        try {
            StringEntity entity = new StringEntity(jsonObject.toString());
            client.post(context, getAbsoluteUrl(url) , entity, "application/json", responseHandler);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
