package com.codelabs.bloodbank;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;

public class Register extends AppCompatActivity {

    Button register;
    EditText name, email, number;
    //UpdateDatabase db;
    double latitude = 0, longitude = 0;
    LocationManager locationManager;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        register = (Button) findViewById(R.id.register);

        name = (EditText) findViewById(R.id.nameText);
        email = (EditText) findViewById(R.id.emailText);
        number = (EditText) findViewById(R.id.phoneText);

        // db = new UpdateDatabase(this);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationManager = (LocationManager) Register.this.getSystemService(Context.LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(Register.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Register.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};
                    ActivityCompat.requestPermissions(Register.this, permissions, 1);
                    //return;
                }

                Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location != null) {
                    Log.e("TAG", "GPS is on");
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                } else
                    Toast.makeText(Register.this, "Location not found", Toast.LENGTH_SHORT).show();

                String username = name.getText().toString();
                String mail = email.getText().toString();
                String num = number.getText().toString();
                String lat = String.valueOf(latitude);
                String lang = String.valueOf(longitude);
                /*db.insert(username, password, mail, num, add, isDonor);
                Toast.makeText(Register.this, "Registered", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Register.this, Login.class);
                startActivity(intent);*/

                new CheckRegister().execute(username, mail, num, lat, lang);
            }


        });


    }

    private class CheckRegister extends AsyncTask<String, String, String> {

        String result;
        JSONParser parser = new JSONParser();
        final String URL = "http://192.168.0.18/bank/v1/register";
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(Register.this);
            dialog.setMessage("Creating account... Please wait.");
            dialog.setIndeterminate(false);
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            HashMap<String, String> param = new HashMap<>();
            param.put("name", params[0]);
            param.put("email", params[1]);
            param.put("number", params[2]);
            param.put("lat", params[3]);
            param.put("lang", params[4]);

            JSONObject jsonObject = parser.registerUser(URL, param);

            try{
                if(jsonObject != null){
                    result = jsonObject.getString("error");
                }
            }catch(JSONException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(result.equals("false")){
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }

                Toast.makeText(Register.this, "Registered", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Register.this, MapsActivity.class);
                startActivity(intent);
            }
        }
    }
}
