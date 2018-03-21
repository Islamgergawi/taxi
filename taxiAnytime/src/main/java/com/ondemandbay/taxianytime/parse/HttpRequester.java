package com.ondemandbay.taxianytime.parse;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.ondemandbay.taxianytime.MainActivity;
import com.ondemandbay.taxianytime.utils.AndyUtils;
import com.ondemandbay.taxianytime.utils.AppLog;
import com.ondemandbay.taxianytime.utils.Const;
import com.ondemandbay.taxianytime.utils.PreferenceHelper;
import com.stripe.android.exception.StripeException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * @author Elluminati elluminati.in
 */
public class HttpRequester extends AsyncTask {

    private static final int TIMEOUT_MILLIS = 30000 ;
    private Map<String, String> map;
    private AsyncTaskCompleteListener mAsynclistener;
    private int serviceCode;
    private boolean isGetMethod = false;
    private HttpClient httpclient;
    private Activity activity;
    private AsyncHttpRequest request;
   // private Request req;

    public HttpRequester(Activity activity, Map<String, String> map,
                         int serviceCode, AsyncTaskCompleteListener asyncTaskCompleteListener) {
        this.map = map;
        this.serviceCode = serviceCode;
        this.activity = activity;
        this.isGetMethod = false;
        // is Internet Connection Available...

        if (AndyUtils.isNetworkAvailable(activity)) {
            mAsynclistener = (AsyncTaskCompleteListener) asyncTaskCompleteListener;
            // request = (AsyncHttpRequest) new AsyncHttpRequest().execute(map
            // .get("url"));
           // req = new Request(map.get("url"));
            request = (AsyncHttpRequest) new AsyncHttpRequest()
                  .executeOnExecutor(Executors.newSingleThreadExecutor(),
                        map.get("url"));
        } else {
            showToast("Network is not available!!!");
        }
    }

    public HttpRequester(Activity activity, Map<String, String> map,
                         int serviceCode, boolean isGetMethod,
                         AsyncTaskCompleteListener asyncTaskCompleteListener) {
        this.map = map;
        this.serviceCode = serviceCode;
        this.isGetMethod = isGetMethod;
        this.activity = activity;
        // is Internet Connection Available...

        if (AndyUtils.isNetworkAvailable(activity)) {
            mAsynclistener = (AsyncTaskCompleteListener) asyncTaskCompleteListener;
            // request = (AsyncHttpRequest) new AsyncHttpRequest().execute(map
            // .get("url"));
           // req = new Request(map.get("url"));

            request = (AsyncHttpRequest) new AsyncHttpRequest()
                 .executeOnExecutor(Executors.newSingleThreadExecutor(),
                         map.get("url"));
        } else {
            showToast("Network is not available!!!");
        }
    }

    private void showToast(String msg) {
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
    }

    public void cancelTask() {

      //  request.cancel(true);
        // System.out.println("task is canelled");

    }

    @Override
    protected Object doInBackground(Object[] objects) {
        return null;
    }

    class Request{

        Request(final String url){
            map.remove("url");
            if (!isGetMethod) {
                Map<String, List<String>> params = new HashMap<String, List<String>>();

                for (String key : map.keySet()) {
                    AppLog.Log(Const.TAG,
                            key + "  < === >  " + map.get(key));

                    params.put(key, Arrays.asList(map
                            .get(key)));
                }
               Ion.with(activity)
                        .load(url)
                       .setTimeout(30000)
                        .setBodyParameters(params)
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject response) {
                                try {
                                    System.out.println(url);
                                    if(e!=null){
                                        e.printStackTrace();
                                    System.out.println(e.getMessage());
                                    }
                                   // System.out.println(response);
                                    JSONObject jo = new JSONObject(response.toString());
                                    if (jo.getInt("error") == 11) {

                                        new PreferenceHelper(activity).Logout();
                                        Intent i = new Intent(activity, MainActivity.class);
                                        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                        i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                        activity.startActivity(i);
                                        activity.finish();
                                    }

                                    if (mAsynclistener != null) {
                                        mAsynclistener.onTaskCompleted(response.toString(), serviceCode);
                                    }
                                } catch (JSONException ee) {
                                    ee.printStackTrace();
                                } catch (NullPointerException e2) {
                                    e2.printStackTrace();
                                } catch (Exception e3) {
                                }


                            }


                        });
            }
            else {
                Ion.with(activity)
                        .load(url)
                        .setTimeout(30000)
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject response) {


                                try {
                                    System.out.println(url);

                                    if (e != null) {
                                        e.printStackTrace();
                                        System.out.println(e.getMessage());
                                    }

                                    JSONObject jo = new JSONObject(response.toString());
                                    if (jo.getInt("error") == 11) {

                                        new PreferenceHelper(activity).Logout();
                                        Intent i = new Intent(activity, MainActivity.class);
                                        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                        i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                        activity.startActivity(i);
                                        activity.finish();
                                    }
                                    if (mAsynclistener != null) {
                                        mAsynclistener.onTaskCompleted(response.toString(), serviceCode);
                                    }

                                } catch (JSONException ee) {
                                    ee.printStackTrace();
                                } catch (NullPointerException e2) {
                                    e2.printStackTrace();
                                } catch (Exception e3) {
                                }


                            }


                        });

            }
        }
    }


    class AsyncHttpRequest extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            map.remove("url");
            try {
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                if (!isGetMethod) {
                    System.out.println(urls[0]);
                    HttpPost httppost = new HttpPost(urls[0]);
                   httpclient = new DefaultHttpClient();
                    RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(TIMEOUT_MILLIS).build();

                   //  httpclient = HttpClientBuilder.create().build();
                  //  httppost.setConfig(requestConfig);
                //    HttpConnectionParams.setConnectionTimeout(  httpclient.getParams(), TIMEOUT_MILLIS);
                   /* RequestConfig requestConfig = RequestConfig.custom()
                            .setSocketTimeout(TIMEOUT_MILLIS)
                            .setConnectTimeout(TIMEOUT_MILLIS)
                            .setConnectionRequestTimeout(TIMEOUT_MILLIS)
                            .build();
                    httppost.setConfig(requestConfig);*/
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                    for (String key : map.keySet()) {
                        AppLog.Log(Const.TAG,
                                key + "  < === >  " + map.get(key));

                        nameValuePairs.add(new BasicNameValuePair(key, map
                                .get(key)));
                    }

                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    ActivityManager manager = (ActivityManager) activity
                            .getSystemService(Context.ACTIVITY_SERVICE);

                    if (manager.getMemoryClass() < 25) {
                        System.gc();
                    }
                    HttpResponse response = httpclient.execute(httppost);

                    String responseBody = EntityUtils.toString(response
                            .getEntity());

                    return responseBody;

                } else {

                    RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(TIMEOUT_MILLIS).build();

                    // httpclient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();

               httpclient = new DefaultHttpClient();

                    // System.out.println("GET URL " + urls[0]);
                   // HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), 30000);


                    HttpGet httpGet = new HttpGet(urls[0]);
                    //httpGet.setConfig(requestConfig);
                    HttpResponse httpResponse = httpclient.execute(httpGet);
                    HttpEntity httpEntity = httpResponse.getEntity();

                    String responseBody = EntityUtils.toString(httpEntity);
                    return responseBody;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError oume) {
                System.gc();

                Toast.makeText(
                        activity.getParent().getParent(),
                        "Run out of memory please colse the other background apps and try again!",
                        Toast.LENGTH_LONG).show();
            } finally {
                if (httpclient != null)
                    httpclient.getConnectionManager().shutdown();

            }

            return null;

        }

        @Override
        protected void onPostExecute(String response) {

            try {
                System.out.println(response);
                JSONObject jo = new JSONObject(response);
                if(jo.getInt("error")==11)
                {

                    new PreferenceHelper(activity).Logout();
                    Intent i = new Intent(activity, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    activity.startActivity(i);
                    activity.finish();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            catch(NullPointerException e )
            {
                e.printStackTrace();
            }
            catch (Exception e)
            {}

            if (mAsynclistener != null) {
                mAsynclistener.onTaskCompleted(response, serviceCode);
            }

        }
    }


}
