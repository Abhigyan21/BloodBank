package com.codelabs.bloodbank;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Created by abhigyan on 30/1/17.
 */

public class JSONParser {
    static JSONObject jsonObj = null;

    public JSONObject getUsers(String url){
        try{
            URL urlObj = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection) urlObj.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Accept-Charset", "UTF-8");
            urlConnection.connect();

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            StringBuilder result = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null){
                result.append(line);
            }

            urlConnection.disconnect();
            jsonObj = new JSONObject(result.toString());

        }catch(IOException | JSONException e){
            e.printStackTrace();
        }

        return jsonObj;
    }

    public JSONObject registerUser(String url, HashMap<String, String> params){
        StringBuilder sbparams = new StringBuilder();
        int i = 0;

        try{
            for(String key : params.keySet()){
                if(i != 0){
                    sbparams.append("&");
                }
                sbparams.append(key).append("=").append(URLEncoder.encode(params.get(key), "UTF-8"));
                i++;
            }

            URL urlObj = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection)urlObj.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Accept-Charset", "UTF-8");
            urlConnection.connect();

            String paramsString = sbparams.toString();
            DataOutputStream out = new DataOutputStream(urlConnection.getOutputStream());
            out.writeBytes(paramsString);
            out.flush();
            out.close();

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            StringBuilder result = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null){
                result.append(line);
            }

            urlConnection.disconnect();
            jsonObj = new JSONObject(result.toString());

        }catch(IOException  | JSONException e){
            e.printStackTrace();
        }

        return jsonObj;
    }
}
