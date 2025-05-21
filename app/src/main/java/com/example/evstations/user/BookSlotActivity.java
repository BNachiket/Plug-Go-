package com.example.evstations.user;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.evstations.DBClass;
import com.example.evstations.R;

// Import Razorpay SDK
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BookSlotActivity extends AppCompatActivity implements PaymentResultListener {

    EditText etxtDate, etxtIntime;
    String stationid="", userid="", vehicle="", slotid="", slotname="",b_date="", intime="", outtime="", duration="" , voltage="", amount="", vehicleid="";
    Spinner spnHrs, spnSlots, spnVehicles;
    private Calendar calendar;
    ProgressDialog pDialog;
    private Map<String, String> slotMap;
    private Map<String, String> vehicleMap;
    private Map<String, String> slotPriceMap; // To store price for each slot
    private String razorpayPaymentID = ""; // To store payment ID from Razorpay
    private static final String TAG = "BookSlotActivity";

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_slot);

        // Initialize Razorpay Checkout
        Checkout.preload(getApplicationContext());

        etxtDate = findViewById(R.id.etxtDate);
        etxtIntime = findViewById(R.id.etxtIntime);
        spnHrs = findViewById(R.id.spnHrs);
        spnSlots = findViewById(R.id.spnSlots);
        spnVehicles = findViewById(R.id.spnVehicles);
        stationid = getIntent().getStringExtra("stationid");
        slotMap = new HashMap<>();
        vehicleMap = new HashMap<>();
        slotPriceMap = new HashMap<>(); // Initialize price map
        Button btnSubmit = findViewById(R.id.btnSubmit);
        String query = "SELECT CValue FROM Configuration WHERE CName = 'id'";
        userid = DBClass.getSingleValue(query);

        calendar = Calendar.getInstance();

        etxtDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
        etxtIntime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });
        String[] values = {"1", "1.30", "2", "2.30","3","3.30","4","4.30","5","5.30","6","6.30","7","7.30","8"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spn_item, values);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spnHrs.setAdapter(adapter);
        int textColor = getResources().getColor(R.color.black); // Replace with your desired color resource
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //spnSlots.setPopupBackgroundResource(R.drawable.shape); // Set background resource if needed
        spnHrs.getBackground().setColorFilter(R.color.black, PorterDuff.Mode.SRC_ATOP);

        // Set a listener for item selections
        spnHrs.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Handle the selected item
                String selectedItem = (String) parentView.getItemAtPosition(position);
                updateTotalAmount();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here
            }
        });

        loadSlots();
        loadVehicles();

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateAndInitiatePayment();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Book Slot");
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

    // Method to update total amount based on selected slot and duration
    private void updateTotalAmount() {
        if (spnSlots.getSelectedItem() != null && spnHrs.getSelectedItem() != null) {
            String selectedSlot = spnSlots.getSelectedItem().toString();
            String priceStr = slotPriceMap.get(selectedSlot);

            if (priceStr != null && !priceStr.isEmpty()) {
                try {
                    double basePrice = Double.parseDouble(priceStr);
                    double hours = Double.parseDouble(spnHrs.getSelectedItem().toString());

                    // Calculate total amount (price × hours)
                    double total = basePrice * hours;
                    amount = String.valueOf((int)total); // Convert to integer for Razorpay

                    Log.d(TAG, "Updated total amount: " + amount);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error parsing price: " + e.getMessage());
                    amount = "500"; // Default fallback
                }
            } else {
                amount = "500"; // Default fallback
            }
        }
    }

    private void validateAndInitiatePayment() {
        b_date = etxtDate.getText().toString();
        if (b_date.equals("")) {
            etxtDate.setError("Select Date");
            etxtDate.requestFocus();
            return;
        }

        intime = etxtIntime.getText().toString();
        if (intime.equals("")) {
            etxtIntime.setError("Select Time");
            etxtIntime.requestFocus();
            return;
        }

        duration = spnHrs.getSelectedItem().toString();
        if (duration.equals("")) {
            spnHrs.requestFocus();
            return;
        }

        slotname = spnSlots.getSelectedItem().toString();
        if (slotname.equals("")) {
            spnSlots.requestFocus();
            return;
        }

        vehicle = spnVehicles.getSelectedItem().toString();
        if (vehicle.equals("")) {
            spnVehicles.requestFocus();
            return;
        }

        calculateOutTime();

        String selectedSlot = spnSlots.getSelectedItem().toString();
        slotid = slotMap.get(selectedSlot);
        String selectedVehicle = spnVehicles.getSelectedItem().toString();
        vehicleid = vehicleMap.get(selectedVehicle);

        // Update total amount before payment
        updateTotalAmount();

        // Start Razorpay payment flow
        startRazorpayPayment();
    }

    private void calculateOutTime() {
        String selectedTime = etxtIntime.getText().toString();
        double selectedHours = Double.parseDouble(spnHrs.getSelectedItem().toString());

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        try {
            Date date = sdf.parse(selectedTime);

            // Add selected hours to the time
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            // Handle decimal hours
            int hours = (int) selectedHours;
            int minutes = (int) ((selectedHours - hours) * 60);

            calendar.add(Calendar.HOUR_OF_DAY, hours);
            calendar.add(Calendar.MINUTE, minutes);

            // Format the result time
            outtime = sdf.format(calendar.getTime());

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void startRazorpayPayment() {
        // Show loading dialog
        pDialog = new ProgressDialog(BookSlotActivity.this);
        pDialog.setMessage("Initializing payment...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_AIpc8dFQzZSCf0"); // Replace with your Razorpay test key

        try {
            JSONObject options = new JSONObject();

            // Set payment details
            options.put("name", "EV Charging Station");
            options.put("description", "Booking for " + slotname);
            options.put("currency", "INR");
            options.put("amount", Integer.parseInt(amount) * 100); // Amount in paise

            // Set prefill info
            JSONObject prefill = new JSONObject();
            prefill.put("contact", "9876543210"); // You may fetch this from user profile
            prefill.put("email", "user@example.com"); // You may fetch this from user profile
            options.put("prefill", prefill);

            checkout.open(this, options);

        } catch (Exception e) {
            pDialog.dismiss();
            Log.e(TAG, "Error in starting Razorpay Checkout: " + e.getMessage());
            Toast.makeText(this, "Payment initialization failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPaymentSuccess(String paymentId) {
        // Payment was successful
        razorpayPaymentID = paymentId;
        Log.d(TAG, "Payment successful: " + paymentId);

        // Proceed with booking slot
        bookSlotWithPayment();
    }

    @Override
    public void onPaymentError(int code, String response) {
        // Payment failed
        pDialog.dismiss();
        try {
            Log.e(TAG, "Payment failed: code=" + code + ", response=" + response);
            Toast.makeText(this, "Payment failed: " + response, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "Exception in onPaymentError: " + e.getMessage());
        }
    }

    private void bookSlotWithPayment() {
        if (pDialog == null || !pDialog.isShowing()) {
            pDialog = new ProgressDialog(BookSlotActivity.this);
            pDialog.setMessage("Confirming your booking...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        } else {
            pDialog.setMessage("Confirming your booking...");
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, DBClass.urlBookSlot,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject jsonObject = null;
                        Log.d("Response", ">> " + response);
                        pDialog.dismiss();
                        try {
                            jsonObject = new JSONObject(response);
                            Log.e("onResponse: ", jsonObject.getString("status"));

                            if (jsonObject.getString("status").equals("success")) {
                                Toast.makeText(getApplicationContext(), "Slot booked successfully!", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getApplicationContext(), UserHomeActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "Slot Not Booked ...Failed...", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "Check Internet Connection...", Toast.LENGTH_LONG).show();
                            Log.e("Exception", ">> " + e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pDialog.dismiss();
                        Log.e("Exception", error.toString());
                        Toast.makeText(getApplicationContext(), "Check Internet Connection...", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("stationid", stationid);
                params.put("userid", userid);
                params.put("b_date", b_date);
                params.put("intime", intime);
                params.put("outtime", outtime);
                params.put("duration", duration);
                params.put("slotid", slotid);
                params.put("voltage", "100");
                params.put("amount", amount);
                params.put("slotname", slotname);
                params.put("vehicleid", vehicleid);
                params.put("vehicle", vehicle);
                params.put("payment_id", razorpayPaymentID); // Add payment ID
                Log.e("Params", params.toString());
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                R.style.CustomDatePicker,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        updateEditText();
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        //calendar.add(Calendar.DAY_OF_MONTH, 1);
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());

        datePickerDialog.show();
    }

    private void updateEditText() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        etxtDate.setText(sdf.format(calendar.getTime()));
    }

    private void showTimePickerDialog() {
        // Get the current time
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Create a TimePickerDialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // Handle the selected time
                        String selectedTime = String.format("%02d:%02d", hourOfDay, minute);
                        etxtIntime.setText(selectedTime);
                    }
                },
                hour,
                minute,
                true // 24-hour format
        );

        // Show the TimePickerDialog
        timePickerDialog.show();
    }

    public void loadSlots() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, DBClass.urlStationSlots,
                response -> {
                    JSONObject jsonObject = null;
                    Log.d("Response", ">> " + response);

                    try {
                        jsonObject = new JSONObject(response);
                        JSONArray jsonData = jsonObject.getJSONArray("data");
                        List<String> items = new ArrayList<>();
                        for (int i = 0; i < jsonData.length(); i++) {
                            JSONObject jo = jsonData.getJSONObject(i);
                            if (jo.getString("status").equals("Enable")) {
                                String item = "Voltage : " + jo.getString("voltage") + " Price : " + jo.getString("price");
                                String slotid = jo.getString("id");
                                String voltage = jo.getString("voltage");
                                String price = jo.getString("price");
                                slotMap.put(item, slotid);
                                slotPriceMap.put(item, price); // Store price for calculations
                                items.add(item);
                            }
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(BookSlotActivity.this, R.layout.spn_item, items);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spnSlots.setAdapter(adapter);

                        int textColor = getResources().getColor(R.color.black);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spnSlots.setPopupBackgroundResource(R.drawable.shape);
                        spnSlots.getBackground().setColorFilter(textColor, PorterDuff.Mode.SRC_ATOP);

                        // Set listener for Spinner item selection
                        spnSlots.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                updateTotalAmount();
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                // Handle nothing selected if needed
                            }
                        });

                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), "Check Internet Connection...", Toast.LENGTH_LONG).show();
                        Log.e("Exception", ">> " + e);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Exception", error.toString());
                        Toast.makeText(getApplicationContext(), "Check Internet Connection...", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("stationid", stationid);
                Log.e("Params", params.toString());
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }

    public void loadVehicles() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, DBClass.urlVehicles,
                response -> {
                    JSONObject jsonObject = null;
                    Log.d("Response", ">> " + response);

                    try {
                        jsonObject = new JSONObject(response);
                        JSONArray jsonData = jsonObject.getJSONArray("data");
                        List<String> items = new ArrayList<>();
                        for (int i = 0; i < jsonData.length(); i++) {
                            JSONObject jo = jsonData.getJSONObject(i);
                            String item = jo.getString("veh_name") + " - " + jo.getString("veh_number");
                            String vehicleid = jo.getString("id");
                            vehicleMap.put(item, vehicleid);
                            items.add(item);
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(BookSlotActivity.this, R.layout.spn_item, items);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spnVehicles.setAdapter(adapter);

                        int textColor = getResources().getColor(R.color.black);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spnVehicles.getBackground().setColorFilter(textColor, PorterDuff.Mode.SRC_ATOP);

                        // Set listener for Spinner item selection
                        spnVehicles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                // Nothing special to do when vehicle is selected
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                // Handle nothing selected if needed
                            }
                        });

                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), "Check Internet Connection...", Toast.LENGTH_LONG).show();
                        Log.e("Exception", ">> " + e);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Exception", error.toString());
                        Toast.makeText(getApplicationContext(), "Check Internet Connection...", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userid", userid);
                Log.e("Params", params.toString());
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }
}