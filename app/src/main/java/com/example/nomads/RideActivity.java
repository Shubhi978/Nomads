package com.example.nomads;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class RideActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    TextView carNoTv, durationTv, costTv;
    AppCompatButton contactUsBttn, endRideBttn;
    Button dialogCancelBttn, dialogEmergencyEndBttn;

    static DatabaseReference carRef;

    FirebaseAuth mAuth;
    DatabaseReference userRef;
    String currentUserID;

    String carId = MainActivity.currentRideCarID;

    private ValueEventListener carDetailsValueEventListener, userCarValueEventListener;
    boolean isCarDetailsListening = true, isUserCarListening = true;

    private static final int REQUEST_CALL = 1;
    String emergencyNo = "7037777342";      //7985923391  9451179809
    String emergencyRecipientEmail = "jyotsana.srivastava99@gmail.com,iit2019171@iiita.ac.in,iit2019163@iiita.ac.in,iit2019175@iiita.ac.in";
    //String emergencyRecipientEmail = "jyotsana.srivastava99@gmail.com";
    String emergencySenderEmail = "iit2019174@iiita.ac.in";
    String emergencySenderPassword = "sharedPassword";

    Dialog endConfirmationDialog;

    static DatabaseReference parkingRef, carLocationRef;
    static Location carLocation;
    static String parkingFoundId = "";
    static int searchRadius = 1;
    static boolean isParkingFound = false;

    //static GeoQueryEventListener geoQueryEventListener;
    static boolean isGeoQueryListening = true;

    static ValueEventListener parkingRefValueEventListener, carLocationValueEventListener;
    static boolean isParkingRefListening = true, isCarLocationRefListening = true;

    public static boolean ridesIsFromSetting=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride);

        /*
        MainActivity.handlerIfRunning = true;
        MainActivity.rideTime = 0;
        MainActivity.timerHandler = new Handler();
        MainActivity.rideTimerRunnable = new RideTimerRunnable();
        MainActivity.timerHandler.postDelayed(MainActivity.rideTimerRunnable, 1000);    //Should be 60000 in place of 1000
        /*
        MainActivity.timerRunnable = new Runnable() {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(MainActivity.currentUserID);
            @Override
            public void run() {
                if(!MainActivity.handlerIfRunning)
                    return;
                else{
                    String str;
                    int tmin = MainActivity.rideTime++;
                    int hr = tmin / 60;
                    int min = tmin % 60;

                    if (hr >= 10)
                        str = String.valueOf(hr);
                    else
                        str = "0" + hr;
                    str = str + ":";
                    if (min >= 10)
                        str = str + min;
                    else
                        str = str + "0" + min;

                    double cost = MainActivity.rideRate * tmin;
                    cost = Math.round(cost * 100.0) / 100.0;

                    userRef.child("Car booked").child(MainActivity.currentRideCarID).child("ride_time").setValue(str);
                    userRef.child("Car booked").child(MainActivity.currentRideCarID).child("ride_cost").setValue(String.valueOf(cost));

                    MainActivity.timerHandler.postDelayed(MainActivity.timerRunnable, 1000);    //Should be 60000 in place of 1000
                }
            }
        };
        MainActivity.timerHandler.post(MainActivity.timerRunnable);

         */

        mToolbar = (Toolbar)findViewById(R.id.ride_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Your Ride");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        carNoTv = (TextView)findViewById(R.id.ride_heading_car_no);
        durationTv = (TextView)findViewById(R.id.ride_duration);
        costTv = (TextView)findViewById(R.id.ride_cost);

        contactUsBttn = (AppCompatButton)findViewById(R.id.ride_contact_us_button);
        endRideBttn = (AppCompatButton)findViewById(R.id.end_ride_button);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        isUserCarListening = true;

        carRef = FirebaseDatabase.getInstance().getReference().child("Cars").child(carId);
        isCarDetailsListening = true;

        carRef.child("Details").addValueEventListener(carDetailsValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(isCarDetailsListening) {
                    if (snapshot.exists() && snapshot.child("car_plateNo").exists()) {
                        String car_plateNo = snapshot.child("car_plateNo").getValue().toString();
                        carNoTv.setText("Car No: " + car_plateNo);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ///*
        userRef.child("Car booked").child(carId).addValueEventListener(userCarValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(isUserCarListening) {
                    if (snapshot.exists()) {
                        if (snapshot.child("ride_time").exists()) {
                            String str = snapshot.child("ride_time").getValue().toString();
                            durationTv.setText("Duration: " + str);
                        }
                        if (snapshot.child("ride_cost").exists()) {
                            String str = snapshot.child("ride_cost").getValue().toString();
                            costTv.setText("Cost: Rs." + str);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

         //*/

        endRideBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkIfParkingSafely();
                //endTheRide();
            }
        });

        contactUsBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makePhoneCall();
            }
        });

        ///*
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

         //*/
    }

    private void checkIfParkingSafely() {
        searchRadius = 1;
        isParkingFound = false;
        isGeoQueryListening = true;
        getClosestParking();
    }

    private void getClosestParking() {
        parkingRef = FirebaseDatabase.getInstance().getReference().child("ParkingLots");

        GeoFire geoFire = new GeoFire(RideActivity.parkingRef);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(carLocation.getLatitude(), carLocation.getLongitude()), searchRadius);

        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(isGeoQueryListening && !isParkingFound){
                    isParkingFound = true;
                    parkingFoundId = key;

                    Location parkingLocation = new Location("");
                    parkingLocation.setLatitude(location.latitude);
                    parkingLocation.setLongitude(location.longitude);

                    double distance = carLocation.distanceTo(parkingLocation);
                    Toast.makeText(RideActivity.this, "distance found: "+distance + "m", Toast.LENGTH_SHORT).show();

                    if(distance < 10){
                        endTheRide();
                    }else{
                        displayConfirmationDialog();
                    }

                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(isGeoQueryListening && !isParkingFound){
                    Toast.makeText(RideActivity.this, "Parking lot not found in 1 query (1 Km)!", Toast.LENGTH_SHORT).show();

                    displayConfirmationDialog();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void displayConfirmationDialog() {
        endConfirmationDialog = new Dialog(RideActivity.this);
        endConfirmationDialog.setContentView(R.layout.end_ride_dialog);
        endConfirmationDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.end_ride_dialog_background));
        endConfirmationDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        endConfirmationDialog.setCancelable(false);
        endConfirmationDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;

        dialogCancelBttn = (Button)endConfirmationDialog.findViewById(R.id.dialog_cancel_button);
        dialogEmergencyEndBttn = (Button)endConfirmationDialog.findViewById(R.id.dialog_emergency_end_button);

        dialogCancelBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endConfirmationDialog.dismiss();
            }
        });

        dialogEmergencyEndBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyTheAdminAboutEmergencyEnd();
                endConfirmationDialog.dismiss();
                endTheRide();
            }
        });

        endConfirmationDialog.show();
    }

    private void notifyTheAdminAboutEmergencyEnd() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        String currentDateandTime = sdf.format(new Date());

        Random rand = new Random();
        int rno = rand.nextInt(100)+1;
        String subject = "Nomads ## EMERGENCY END #" + rno + " ## - Unsafe parking by user";
        String body = "Time: " + currentDateandTime +
                "\n\nUser: " + MainActivity.currentUserID +
                "\n\nUser location: " + carLocation.toString() +
                "\n\nNearest Parking: " + parkingFoundId;

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        Session session = Session.getInstance(properties,
                new javax.mail.Authenticator(){
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(emergencySenderEmail, emergencySenderPassword);
                    }
                });
        try{
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emergencySenderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emergencyRecipientEmail));
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
            Toast.makeText(this, "Notification sent to admin", Toast.LENGTH_SHORT).show();
        }catch(MessagingException e){
            //String errMsg = e.getMessage();
            //Toast.makeText(this, "Error in sending mail: "+errMsg, Toast.LENGTH_SHORT).show();
        }
    }

    private void endTheRide(){
        ///*
        if(MainActivity.rideTimer != null) {
            MainActivity.rideTimer.cancel();
            MainActivity.rideTimer.purge();
        }
        //*/
        //MainActivity.isRidePaymentPending = true;
        userRef.child("Car booked").child(carId).child("status").setValue("payment_pending");
                /*
                MainActivity.handlerIfRunning = false;
                MainActivity.timerHandler.removeCallbacks(MainActivity.rideTimerRunnable);
                //MainActivity.timerHandler.removeCallbacks(MainActivity.timerRunnable);
                MainActivity.rideTime = 0;
                 */
        //Can add to user history
        Intent ridePaymentIntent = new Intent(RideActivity.this, RidePaymentActivity.class);
        //ridePaymentIntent.putExtra("cost", costTv.getText().toString());
        startActivity(ridePaymentIntent);
        finish();
    }

    private void makePhoneCall() {
        if (ContextCompat.checkSelfPermission(RideActivity.this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(RideActivity.this,
                        new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
        } else {
            String dial = "tel:" + emergencyNo;
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall();
            } else {
                Toast.makeText(this, "Permission needed to make the call.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (ridesIsFromSetting){
            finish();
            startActivity(getIntent());
            ridesIsFromSetting=false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isCarDetailsListening = true;
        isUserCarListening = true;
        isParkingRefListening = true;
        isCarLocationRefListening = true;
        isGeoQueryListening = true;

        if (ridesIsFromSetting){
            finish();
            startActivity(getIntent());
            ridesIsFromSetting=false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        isCarDetailsListening = false;
        if(carDetailsValueEventListener != null)
            carRef.child("Details").removeEventListener(carDetailsValueEventListener);

        isUserCarListening = false;
        if(userCarValueEventListener != null)
            userRef.child("Car booked").child(carId).removeEventListener(userCarValueEventListener);

        isParkingRefListening = false;
        if(parkingRefValueEventListener != null && !parkingFoundId.equals(""))
            parkingRef.child(parkingFoundId).child("l").removeEventListener(parkingRefValueEventListener);

        isCarLocationRefListening = false;
        if(carLocationValueEventListener != null)
            carLocationRef.removeEventListener(carLocationValueEventListener);

        isGeoQueryListening = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        isCarDetailsListening = false;
        if(carDetailsValueEventListener != null)
            carRef.child("Details").removeEventListener(carDetailsValueEventListener);

        isUserCarListening = false;
        if(userCarValueEventListener != null)
            userRef.child("Car booked").child(carId).removeEventListener(userCarValueEventListener);

        isParkingRefListening = false;
        if(parkingRefValueEventListener != null && !parkingFoundId.equals(""))
            parkingRef.child(parkingFoundId).child("l").removeEventListener(parkingRefValueEventListener);

        isCarLocationRefListening = false;
        if(carLocationValueEventListener != null)
            carLocationRef.removeEventListener(carLocationValueEventListener);

        isGeoQueryListening = false;
    }
}