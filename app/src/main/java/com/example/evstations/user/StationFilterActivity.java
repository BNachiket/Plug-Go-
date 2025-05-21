package com.example.evstations.user;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.evstations.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class StationFilterActivity extends AppCompatActivity {

    Double latitude=17.0295 , longitude=74.6078;
    EditText etxtCity;
    String city="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_filter);
        etxtCity = findViewById(R.id.etxtCity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Search Station");
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void btnSearchClick(View view) {
        city = etxtCity.getText().toString();
        if (city.equals(""))
        {
            etxtCity.setError("Please Enter City");
            etxtCity.requestFocus();
            return;
        }
        Intent intent = new Intent(this, FindStationsActivity.class);
        intent.putExtra("city", city);
        startActivity(intent);


    }

    public void btnNearByClick(View view) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission is already granted
            getCurrentLocation();

        } else {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1001);
        }
    }



    private void getCurrentLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();

                                Intent intent = new Intent(StationFilterActivity.this, FindStationsActivity.class);
                                intent.putExtra("lat", latitude);
                                intent.putExtra("lon", longitude);
                                startActivity(intent);
                                // Set the values to your EditText
                                //txtLocation.setText(String.format("%f", latitude) + " , "+ String.format("%f", longitude));
                            } else {
                                Toast.makeText(StationFilterActivity.this, "Location not available", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } catch (SecurityException e) {
            e.printStackTrace();
            // Handle the exception
        }
    }
}