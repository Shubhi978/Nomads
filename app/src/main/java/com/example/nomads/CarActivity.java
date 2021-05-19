package com.example.nomads;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class CarActivity extends AppCompatActivity {

    String carId, bookedCar = "",carAvailable = "false";
    int isThreshPaymentSuccessfull = 0;
    AppCompatButton bookButton, cancelBookingButton, unlockCarButton;
    TextView availableTv, waitingTv;
    private Toolbar mToolbar;

    DatabaseReference carRef;

    FirebaseAuth mAuth;
    DatabaseReference userRef;
    String currentUserID;
    private ValueEventListener carDetailsValueEventListener, carsValueEventListener, userRefValueEventListener, userCarValueEventListener;
    boolean isCarDetailsListening = true, isCarsListening = true, isUserRefListening = true, isUserCarListening = true;

    //private static final String TAG = CarActivity.class.getSimpleName();
    //private ImageView carImage;
    private TextView car_plateNoTv, car_modelNoTv, car_mileageTv, car_ratingTv, car_availabilityTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car);

        //if(MainActivity.usersCarStatus.equals("unlocked"))
        isCarDetailsListening = true;
        isCarsListening = true;
        isUserRefListening = true;
        isUserCarListening = true;

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);

        mToolbar = (Toolbar)findViewById(R.id.car_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Car Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //carImage = (ImageView)findViewById(R.id.car_imageView);
        bookButton = (AppCompatButton)findViewById(R.id.car_book_button);
        availableTv = (TextView)findViewById(R.id.car_available_text_view);
        waitingTv = (TextView)findViewById(R.id.car_waiting_text_view);
        unlockCarButton = (AppCompatButton)findViewById(R.id.car_unlock_button);
        cancelBookingButton = (AppCompatButton)findViewById(R.id.car_cancel_booking_button);
        car_plateNoTv = (TextView)findViewById(R.id.car_plateNo);
        car_modelNoTv = (TextView)findViewById(R.id.car_modelNo);
        car_mileageTv = (TextView)findViewById(R.id.car_mileage);
        car_ratingTv = (TextView)findViewById(R.id.car_rating);
        car_availabilityTv = (TextView)findViewById(R.id.car_availability);

        carId = getIntent().getStringExtra("carId");
        isThreshPaymentSuccessfull = getIntent().getIntExtra("bookedSuccess", 0);

        carRef = FirebaseDatabase.getInstance().getReference().child("Cars").child(carId);
        //Display details
        carRef.child("Details").addValueEventListener(carDetailsValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isCarDetailsListening) {
                    String car_plateNo = snapshot.child("car_plateNo").getValue().toString();
                    String car_modelNo = snapshot.child("car_modelNo").getValue().toString();
                    String car_mileage = snapshot.child("car_mileage").getValue().toString();
                    String car_rating = snapshot.child("car_rating").getValue().toString();

                    car_plateNoTv.setText(car_plateNo);
                    car_modelNoTv.setText(car_modelNo);
                    car_mileageTv.setText(car_mileage);
                    car_ratingTv.setText(car_rating);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //groupCreatorAndKeys.put(carRef.child("Details"), carDetailsValueEventListener);

        carRef.addValueEventListener(carsValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(isCarsListening) {
                    carAvailable = snapshot.child("available").getValue().toString();
                    if (carAvailable.equals("true")) {
                        bookButton.setEnabled(true);
                        bookButton.setBackgroundResource(R.drawable.button);
                        //availableTv.setText("Car Unavailable");
                        availableTv.setVisibility(View.INVISIBLE);
                        car_availabilityTv.setText("Available");
                    } else if (carAvailable.equals("false")) {
                        bookButton.setEnabled(false);
                        bookButton.setBackgroundResource(R.drawable.disabled_button);
                        availableTv.setText("Car Unavailable");
                        availableTv.setVisibility(View.VISIBLE);
                        car_availabilityTv.setText("Unavailable");
                    }/*else{
                    bookButton.setEnabled(false);
                    bookButton.setBackgroundResource(R.drawable.disabled_button);
                    //availableTv.setText("Car Unavailable");
                    availableTv.setVisibility(View.INVISIBLE);
                    car_availabilityTv.setText("Unavailable");
                    }
                    */
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //groupCreatorAndKeys.put(carRef, carsValueEventListener);

        bookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent thresholdPaymentIntent = new Intent(CarActivity.this, ThresholdPaymentActivity.class);
                thresholdPaymentIntent.putExtra("carId", carId);
                startActivity(thresholdPaymentIntent);
                finish();
            }
        });

        ///*
        if(isThreshPaymentSuccessfull == 1){
            //Toast.makeText(this, "Returned Threshold payment successfull!", Toast.LENGTH_SHORT).show();
            userRef.child("Car booked").child(carId).child("status").setValue("waiting");
            setTimer();
        }

         //*/
        userRef.addValueEventListener(userRefValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(isUserRefListening) {
                    if (snapshot.exists() && snapshot.hasChild("Car booked")) {
                        int bookedFlag = 0;
                        for (DataSnapshot ds : snapshot.child("Car booked").getChildren()) {
                            if (ds.exists()) {
                                bookedCar = ds.getKey();
                                bookedFlag = 1;
                                break;
                            }
                        }
                        if (bookedFlag == 1) {
                            if (carId.equals(bookedCar))
                                carBookedByTheCurrentUser();
                                //else if(carAvailable.equals("true")){
                            else {
                                bookButton.setEnabled(false);
                                bookButton.setBackgroundResource(R.drawable.disabled_button);
                                availableTv.setText("Can't book more than one car.");
                                availableTv.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //groupCreatorAndKeys.put(userRef, userRefValueEventListener);

        unlockCarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.usersCarStatus = "unlocked";
                ///*
                userRef.child("Car booked").child(carId).child("status").setValue("unlocked");
                userRef.child("Car booked").child(carId).child("waiting time").removeValue();
                MainActivity.countDownTimer.cancel();

                MainActivity.currentRideCarID = carId;
                MainActivity.currentUserID = currentUserID;
                userRef.child("Car booked").child(MainActivity.currentRideCarID).child("ride_time").setValue("00");
                userRef.child("Car booked").child(MainActivity.currentRideCarID).child("ride_cost").setValue("00");

                ///*
                RideTimerTask.time = 0;
                MainActivity.rideTimer = new Timer();
                TimerTask task = new RideTimerTask();
                //MainActivity.rideTimer.schedule(task, 60000, 60000);
                //For demo: 5 sec = 1 min
                MainActivity.rideTimer.schedule(task, 5000, 5000);
                 //*/
                /*
                MainActivity.handlerIfRunning = true;
                MainActivity.rideTime = 0;
                MainActivity.timerHandler = new Handler();
                MainActivity.rideTimerRunnable = new RideTimerRunnable();

                 */
                /*
                try {
                    //MainActivity.timerHandler.postDelayed(MainActivity.rideTimerRunnable, 5000);    //Should be 60000 in place of 1000
                    //sendUserToRideActivity();

                }catch(Exception e){
                    Toast.makeText(CarActivity.this, "BadTokenException... Trying again", Toast.LENGTH_SHORT).show();
                    //sendUserToRideActivity();
                }

                     //*/
            }
        });
    }

    ///*
    private void sendUserToRideActivity(){
        Intent rideIntent = new Intent(CarActivity.this, RideActivity.class);
        startActivity(rideIntent);
        finish();
    }

    private void sendUserToRidePaymentActivity(){
        Intent paymentIntent = new Intent(CarActivity.this, RidePaymentActivity.class);
        startActivity(paymentIntent);
        finish();
    }

     //*/
    private void killActivity(){
        //Toast.makeText(this, "killActivity() called", Toast.LENGTH_SHORT).show();
        isCarDetailsListening = false;
        isCarsListening = false;
        isUserRefListening = false;
        isUserCarListening = false;

        if(carDetailsValueEventListener != null)
            carRef.child("Details").removeEventListener(carDetailsValueEventListener);
        if(carsValueEventListener != null)
            carRef.removeEventListener(carsValueEventListener);
        if(userRefValueEventListener != null)
            userRef.removeEventListener(userRefValueEventListener);
        if(userCarValueEventListener != null)
            userRef.child("Car booked").child(carId).removeEventListener(userCarValueEventListener);

        finish();
    }

    private void carBookedByTheCurrentUser() {
        userRef.child("Car booked").child(carId).addValueEventListener(userCarValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(isUserCarListening) {
                    if (snapshot.exists()) {
                        if (snapshot.child("status").exists() && snapshot.child("status").getValue().toString().equals("unlocked")) {
                            sendUserToRideActivity();
                            //Toast.makeText(CarActivity.this, "isUserCarListening: "+isUserCarListening, Toast.LENGTH_SHORT).show();
                            //Toast.makeText(CarActivity.this, "Should go to RideActivity", Toast.LENGTH_SHORT).show();
                            //userRef.child("Car booked").child(carId).removeEventListener(this);
                            killActivity();
                        }else if(snapshot.child("status").exists() && snapshot.child("status").getValue().toString().equals("payment_pending")){
                            sendUserToRidePaymentActivity();
                            killActivity();
                        }
                        if (snapshot.child("waiting time").exists()) {
                            String str = snapshot.child("waiting time").getValue().toString();
                            waitingTv.setVisibility(View.VISIBLE);
                            waitingTv.setText("Reach the car in " + str);
                            unlockCarButton.setVisibility(View.VISIBLE);
                            cancelBookingButton.setVisibility(View.VISIBLE);
                            bookButton.setVisibility(View.INVISIBLE);
                            availableTv.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //groupCreatorAndKeys.put(userRef.child("Car booked").child(carId), userCarValueEventListener);

        cancelBookingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CarActivity.this, "Booking cancelled", Toast.LENGTH_SHORT).show();
                if(MainActivity.countDownTimer != null)
                MainActivity.countDownTimer.cancel();
                //MainActivity.countDownTimer.onFinish();
                carRef.child("available").setValue("true");
                userRef.child("Car booked").child(carId).removeValue();
                waitingTv.setVisibility(View.INVISIBLE);
                unlockCarButton.setVisibility(View.INVISIBLE);
                cancelBookingButton.setVisibility(View.INVISIBLE);
                bookButton.setVisibility(View.VISIBLE);

                MainActivity.isCountDownTimerRunning = false;
            }
        });
    }

    private void setTimer() {
        MainActivity.isCountDownTimerRunning = true;
        MainActivity.countDownTimer = new CountDownTimer(60*60* 1000 + 100, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateTime((int) millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                Toast.makeText(CarActivity.this, "Unable to reach the car in time. Booking cancelled!", Toast.LENGTH_SHORT).show();
                carRef.child("available").setValue("true");
                userRef.child("Car booked").child(carId).removeValue();
                waitingTv.setVisibility(View.INVISIBLE);
                unlockCarButton.setVisibility(View.INVISIBLE);
                cancelBookingButton.setVisibility(View.INVISIBLE);
                bookButton.setVisibility(View.VISIBLE);

                MainActivity.isCountDownTimerRunning = false;
            }
        }.start();
    }
    public void updateTime(int time) {
        String str;
        int min=time/60, sec = time%60;
        if(min>=10)
            str=String.valueOf(min);
        else
            str = "0"+ min;
        str = str + ":";
        if(sec<10)
            str = str + "0" + sec;
        else
            str = str + sec;

        userRef.child("Car booked").child(carId).child("waiting time").setValue(str);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("RESUME", "onResume() of CARACTIVITY reached!!!!$%");
        System.out.println("onResume() of CARACTIVITY reached!!!!$%");
        //Toast.makeText(this, "onResume() reached", Toast.LENGTH_SHORT).show();
        isCarDetailsListening = true;
        isCarsListening = true;
        isUserRefListening = true;
        isUserCarListening = true;

        /*
        if(MainActivity.currentRideCarID.equals(carId) && MainActivity.usersCarStatus.equals("booked") && !MainActivity.isCountDownTimerRunning){
            setTimer();
        }

         //*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("PAUSE", "onPause() of CARACTIVITY reached!!!!$%");
        //Toast.makeText(this, "onPause() reached", Toast.LENGTH_SHORT).show();

        isCarDetailsListening = false;
        isCarsListening = false;
        isUserRefListening = false;
        isUserCarListening = false;

        if(carDetailsValueEventListener != null)
            carRef.child("Details").removeEventListener(carDetailsValueEventListener);
        if(carsValueEventListener != null)
            carRef.removeEventListener(carsValueEventListener);
        if(userRefValueEventListener != null)
            userRef.removeEventListener(userRefValueEventListener);
        if(userCarValueEventListener != null)
            userRef.child("Car booked").child(carId).removeEventListener(userCarValueEventListener);
        //Log.i("DDDDDDDDDDDDDDDDDDDDDDD", userCarValueEventListener.toString());
    }

    ///*
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("DESTROY", "onDestroy() of CARACTIVITY reached!!!!$%");
        //Toast.makeText(this, "onDestroy() reached", Toast.LENGTH_LONG).show();

        ///*
        isCarDetailsListening = false;
        isCarsListening = false;
        isUserRefListening = false;
        isUserCarListening = false;

        if(carDetailsValueEventListener != null)
        carRef.child("Details").removeEventListener(carDetailsValueEventListener);
        if(carsValueEventListener != null)
        carRef.removeEventListener(carsValueEventListener);
        if(userRefValueEventListener != null)
        userRef.removeEventListener(userRefValueEventListener);
        if(userCarValueEventListener != null)
        userRef.child("Car booked").child(carId).removeEventListener(userCarValueEventListener);

         //*/
    }

}