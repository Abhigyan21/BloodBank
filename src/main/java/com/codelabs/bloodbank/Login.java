package com.codelabs.bloodbank;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class Login extends AppCompatActivity implements View.OnClickListener{

    Button login, register;
    TextView userName, pass;
    UpdateDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        login = (Button) findViewById(R.id.login);
        register = (Button) findViewById(R.id.register);

        userName = (TextView) findViewById(R.id.userName);
        pass = (TextView) findViewById(R.id.pass);

        db = new UpdateDatabase(this);
        login.setOnClickListener(this);
        register.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.login:
                String user = userName.getText().toString();
                String password = pass.getText().toString();
                //System.out.println(user +"," + password);
                /*if(db.check(user, password)){
                    Toast.makeText(Login.this, "Logged in!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Login.this, MapsActivity.class);
                    startActivity(intent);
                }*/
                new CheckLogin().execute(user, password);
                break;

            case R.id.register:
                Intent regIntent = new Intent(Login.this, Register.class);
                startActivity(regIntent);
                break;
        }
    }

    private class CheckLogin extends AsyncTask<String, String, String>{

        String result;
        Socket socket;

        @Override
        protected String doInBackground(String... params) {
            result = params[0] + "," + params[1] + "\n";

            try {
                socket = new Socket("10.42.0.1", 8000);
                if(socket != null){
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                    out.write(result);
                    out.flush();

                    result = in.readLine();
                    result = result.trim();
                    System.out.println(result);

                    if(result != null){
                        in.close();
                        out.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally{
                try {
                    if(socket != null)
                        socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(result.equals("SUCCESS")){
                Toast.makeText(Login.this, "Logged in!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Login.this, MapsActivity.class);
                startActivity(intent);
            }
        }
    }
}
