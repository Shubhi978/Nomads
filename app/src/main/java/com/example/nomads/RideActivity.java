package com.example.nomads;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;
import java.util.TimerTask;

public class RideActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    TextView carNoTv, durationTv, costTv;
    AppCompatButton contactUsBttn, endRideBttn;

    DatabaseReference carRef;

    FirebaseAuth mAuth;
    DatabaseReference userRef;
    String currentUserID;

    String carId = MainActivity.currentRideCarID;

    private ValueEventListener carDetailsValueEventListener, userCarValueEventListener;
    boolean isCarDetailsListening = true, isUserCarListening = true;


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
                ///*
                MainActivity.rideTimer.cancel();
                MainActivity.rideTimer.purge();
                 //*/
                /*
                MainActivity.handlerIfRunning = false;
                MainActivity.timerHandler.removeCallbacks(MainActivity.rideTimerRunnable);
                //MainActivity.timerHandler.removeCallbacks(MainActivity.timerRunnable);
                MainActivity.rideTime = 0;
                 */
                //Can add to user history
                Intent ridePaymentIntent = new Intent(RideActivity.this, RidePaymentActivity.class);
                ridePaymentIntent.putExtra("cost", costTv.getText().toString());
                startActivity(ridePaymentIntent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        isCarDetailsListening = true;
        isUserCarListening = true;
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
    }
}