package com.example.evstations;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.evstations.user.VehiclesActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    EditText etxtEmail, etxtPassword , etxtName, etxtMobileno;

    String id="", name="", mobileno="", email="", password="", usertype="";
    ProgressDialog pDialog;
    JSONArray jsonData = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        etxtEmail = findViewById(R.id.etxtEmail);
        etxtPassword = findViewById(R.id.etxtPassword);
        etxtName = findViewById(R.id.etxtName);
        etxtMobileno = findViewById(R.id.etxtMobileno);
    }

//    public void btnRegisterClick(View view) {
//        name = etxtName.getText().toString();
//        mobileno = etxtMobileno.getText().toString();
//        if (name.equals("")) {
//            etxtName.setError("Please Enter  Name.");
//            etxtName.requestFocus();
//            return;
//        }
//        if (mobileno.equals("")) {
//            etxtMobileno.setError("Please Enter  Mobileno.");
//            etxtMobileno.requestFocus();
//            return;
//        }
//        email = etxtEmail.getText().toString();
//        password = etxtPassword.getText().toString();
//        if (email.equals("")) {
//            etxtEmail.setError("Please Enter  Email.");
//            etxtEmail.requestFocus();
//            return;
//        }
//        if (password.equals("")) {
//            etxtPassword.setError("Please Enter  Password.");
//            etxtPassword.requestFocus();
//            return;
//        }
//
//        pDialog = new ProgressDialog(RegistrationActivity.this);
//        pDialog.setMessage("validating your details, please wait...");
//        pDialog.setIndeterminate(false);
//        pDialog.setCancelable(true);
//        pDialog.show();
//
//        StringRequest stringRequest = new StringRequest(Request.Method.POST, DBClass.urlRegistration,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        JSONObject jsonObject = null;
//                        Log.d("Response", ">> "+response);
//                        pDialog.dismiss();
//                        try {
//                            jsonObject = new JSONObject(response);
//
//                            Log.e( "onResponse: ", jsonObject.getString("status") );
//
//                            if(jsonObject.getString("status").equals("success")) {
//
//                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
//                                startActivity(intent);
//                                finish();
//                                Toast.makeText(getApplicationContext(), "Registration Success...", Toast.LENGTH_LONG).show();
//
//                            }
//                            else
//                            {
//                                Toast.makeText(getApplicationContext(), "Registration Failed...", Toast.LENGTH_LONG).show();
//                            }
//                        } catch (JSONException e) {
//                            Toast.makeText(getApplicationContext(), "Check Internet Connection...", Toast.LENGTH_LONG).show();
//                            Log.e("Exception", ">> "+e);
//                        }
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        pDialog.dismiss();
//                        Log.e("Exception", error.toString());
//                        Toast.makeText(getApplicationContext(), "Check Internet Connection...", Toast.LENGTH_LONG).show();
//                    }
//                }){
//            @Override
//            protected Map<String, String> getParams()
//            {
//                Map<String, String> params = new HashMap<>();
//                params.put("name", name);
//                params.put("email", email);
//                params.put("mobileno", mobileno);
//                params.put("password", password);
//                Log.e("Params", params.toString());
//                return params;
//            }
//        };
//        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
//        requestQueue.add(stringRequest);
//
//    }
public void btnRegisterClick(View view) {
    name = etxtName.getText().toString().trim();
    mobileno = etxtMobileno.getText().toString().trim();
    email = etxtEmail.getText().toString().trim();
    password = etxtPassword.getText().toString().trim();

    // Validation
    if (name.isEmpty()) {
        etxtName.setError("Enter your name");
        etxtName.requestFocus();
        return;
    }

    if (!mobileno.matches("\\d{10}")) {
        etxtMobileno.setError("Enter valid 10-digit mobile number");
        etxtMobileno.requestFocus();
        return;
    }

    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        etxtEmail.setError("Invalid email");
        etxtEmail.requestFocus();
        return;
    }

    if (password.length() < 8) {
        etxtPassword.setError("Password must be at least 6 characters");
        etxtPassword.requestFocus();
        return;
    }

    // Progress Dialog
    pDialog = new ProgressDialog(RegistrationActivity.this);
    pDialog.setMessage("Registering...");
    pDialog.setIndeterminate(false);
    pDialog.setCancelable(false);
    pDialog.show();

    StringRequest stringRequest = new StringRequest(Request.Method.POST, DBClass.urlRegistration,
            response -> {
                pDialog.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    String message = jsonObject.optString("message");

                    if (status.equals("success")) {
                        Toast.makeText(getApplicationContext(), "Registration successful", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Invalid server response", Toast.LENGTH_LONG).show();
                    Log.e("JSONException", e.toString());
                }
            },
            error -> {
                pDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Network error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("VolleyError", error.toString());
            }) {
        @Override
        protected Map<String, String> getParams() {
            Map<String, String> params = new HashMap<>();
            params.put("name", name);
            params.put("email", email);
            params.put("mobileno", mobileno);
            params.put("password", password);
            return params;
        }
    };

    Volley.newRequestQueue(this).add(stringRequest);
}


    public void btnLoginClick(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}