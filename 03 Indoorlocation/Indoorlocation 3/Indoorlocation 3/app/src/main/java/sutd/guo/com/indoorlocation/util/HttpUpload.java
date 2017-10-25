package sutd.guo.com.indoorlocation.util;


import android.os.Build;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;




/**
 * Created by Wei Guo
 * on 12/10/15.
 * for http upload
 */
public class HttpUpload {
    private static String WIFIPHP ="insertWifiRawData.php";
    private static String STEPHP ="insertMobileHeadingPlusSteps.php";
    private static String IMUPHP ="insertMobileIMUData.php";

    public HttpUpload(){
        super();
    }


    //parse json
    private String parseJSON(String result){
        String time="";
        try {
            JSONObject jsonObject=new JSONObject(result);
            time=jsonObject.getString("sampling_time");
            return time;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return time;
    }

    //upload wifi info and get sampling_time
    public String HttpWifi(String username, String wifiSth, String ip) throws MalformedURLException {

        String new_time="";

        StringBuffer results= new StringBuffer();
        //String color= URLEncoder.encode(col);
        URL url = new URL(ip+WIFIPHP);

        String postParam="user_id="+username+"&tem_wifi="+wifiSth;

        Log.i("httpwifi", "getWifiData is " + wifiSth);

        HttpURLConnection urlConnection = null;
        BufferedWriter writer = null;
        OutputStream os = null;

        try {

            urlConnection = (HttpURLConnection) url.openConnection();
            if (Build.VERSION.SDK != null && Build.VERSION.SDK_INT > 13) {
                urlConnection.setRequestProperty("Connection", "close");
            }
            urlConnection.setRequestMethod("POST");
            urlConnection.setConnectTimeout(3000);
            urlConnection.setReadTimeout(3000);
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setFixedLengthStreamingMode(postParam.getBytes().length);
            urlConnection.setUseCaches(false);


            os = urlConnection.getOutputStream();
            writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(postParam);
            writer.flush();
            urlConnection.connect();


            String i=urlConnection.getResponseMessage();
            Log.i("httpwifi", "getResponseMessage is " + i + url);

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while((line=reader.readLine())!=null){
                results.append(line);
            }
            Log.i("httpwifi", "respones: " +results);

            new_time= parseJSON(results.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(null!=urlConnection) {
                urlConnection.disconnect();
            }
        }
        return new_time;
    }


    /*IMU */
    public String HttpIMU(String username,String orientation_data,String accelerate_data,
                          String magnetic_data,String barometer_data,String ip) throws MalformedURLException {


        String new_time="";

        StringBuffer results= new StringBuffer();
        //String color= URLEncoder.encode(col);

        URL url = new URL(ip+IMUPHP);
        String postParam="user_id="+username+"&orientation_data="+orientation_data+"&accelerate_data="+accelerate_data+"" +
                "&magnetic_data="+magnetic_data+"&barometer_data="+barometer_data;
        HttpURLConnection urlConnection = null;

        try {

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setConnectTimeout(3000);
            urlConnection.setReadTimeout(3000);
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setFixedLengthStreamingMode(postParam.getBytes().length);
            urlConnection.setUseCaches(false);


            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(postParam);
            writer.flush();
            writer.close();
            os.close();
            urlConnection.connect();


            String i=urlConnection.getResponseMessage();
            Log.d("jiangye", "getResponseMessage is " + i + url);

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while((line=reader.readLine())!=null){
                results.append(line);
            }
            Log.d("jiangye", "respones: " +results);

            new_time= parseJSON(results.toString());


        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(null!=urlConnection) {

                urlConnection.disconnect();
            }
        }
        return new_time;
    }


    /*STEP*/
    public String HttpSTEP(String username,String step_data
            ,String turning_data,String ip) throws MalformedURLException {


        String new_time="";

        StringBuffer results= new StringBuffer();
        //String color= URLEncoder.encode(col);

        URL url = new URL(ip+STEPHP);
        String postParam="user_id="+username+"&steps="+step_data+"&heading="+turning_data;
        HttpURLConnection urlConnection = null;

        try {

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setConnectTimeout(3000);
            urlConnection.setReadTimeout(3000);
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setFixedLengthStreamingMode(postParam.getBytes().length);
            urlConnection.setUseCaches(false);


            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(postParam);
            writer.flush();
            writer.close();
            os.close();
            urlConnection.connect();


            String i=urlConnection.getResponseMessage();
            Log.d("jiangye1", "getResponseMessage is " + i + url);

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while((line=reader.readLine())!=null){
                results.append(line);
            }
            Log.d("jiangye1", "respones: " +results);

            new_time= parseJSON(results.toString());


        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(null!=urlConnection) {

                urlConnection.disconnect();
            }
        }
        return new_time;
    }

}
