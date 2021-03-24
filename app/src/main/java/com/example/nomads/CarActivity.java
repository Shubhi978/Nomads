package com.example.nomads;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CarActivity extends AppCompatActivity {

    String carId, bookedCar = "",carAvailable = "false";
    int isThreshPaymentSuccessfull = 0;
    AppCompatButton bookButton, cancelBookingButton, unlockCarButton;
    TextView detailsTv, availableTv, waitingTv;
    private Toolbar mToolbar;

    DatabaseReference carRef;

    FirebaseAuth mAuth;
    DatabaseReference userRef;
    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);

        mToolbar = (Toolbar)findViewById(R.id.car_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Car Info");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bookButton = (AppCompatButton)findViewById(R.id.car_book_button);
        detailsTv = (TextView)findViewById(R.id.car_details_text_view);
        availableTv = (TextView)findViewById(R.id.car_available_text_view);
        waitingTv = (TextView)findViewById(R.id.car_waiting_text_view);
        unlockCarButton = (AppCompatButton)findViewById(R.id.car_unlock_button);
        cancelBookingButton = (AppCompatButton)findViewById(R.id.car_cancel_booking_button);

        carId = getIntent().getStringExtra("carId");
        isThreshPaymentSuccessfull = getIntent().getIntExtra("bookedSuccess", 0);
        //Toast.makeText(this, "String extra from intent: " + carId, Toast.LENGTH_SHORT).show();

        detailsTv.setText(carId + " Car details");

        carRef = FirebaseDatabase.getInstance().getReference().child("Cars").child(carId);

        carRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                carAvailable = snapshot.child("available").getValue().toString();
                //Toast.makeText(CarActivity.this, "Available read from firebase: "+available, Toast.LENGTH_SHORT).show();
                if(carAvailable.equals("true")){
                    bookButton.setEnabled(true);
                    //availableTv.setText("Car Unavailable");
                    availableTv.setVisibility(View.INVISIBLE);
                }else{
                    bookButton.setEnabled(false);
                    availableTv.setText("Car Unavailable");
                    availableTv.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        bookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent thresholdPaymentIntent = new Intent(CarActivity.this, ThresholdPaymentActivity.class);
                thresholdPaymentIntent.putExtra("carId", carId);
                startActivity(thresholdPaymentIntent);
            }
        });

        if(isThreshPaymentSuccessfull == 1){
            Toast.makeText(this, "Returned Threshold payment successfull!", Toast.LENGTH_SHORT).show();
            userRef.child("Car booked").child(carId).child("status").setValue("waiting");
            setTimer();
        }
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild("Car booked")){
                    int bookedFlag = 0;
                    for(DataSnapshot ds : snapshot.child("Car booked").getChildren()){
                        if(ds.exists()){
                            bookedCar = ds.getKey();
                            bookedFlag = 1;
                            break;
                        }
                    }
                    if(bookedFlag == 1){
                        if(carId.equals(bookedCar))
                            carBookedByTheCurrentUser();
                        else if(carAvailable.equals("true")){
                            bookButton.setEnabled(false);
                            availableTv.setText("Can't book more than one car.");
                            availableTv.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void carBookedByTheCurrentUser() {
        userRef.child("Car booked").child(carId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("waiting time").exists()){
                    String str = snapshot.child("waiting time").getValue().toString();
                    waitingTv.setVisibility(View.VISIBLE);
                    waitingTv.setText("Reach the car in "+str);
                    unlockCarButton.setVisibility(View.VISIBLE);
                    cancelBookingButton.setVisibility(View.VISIBLE);
                    bookButton.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        cancelBookingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.countDownTimer.cancel();
                carRef.child("available").setValue("true");
                userRef.child("Car booked").child(carId).removeValue();
                waitingTv.setVisibility(View.INVISIBLE);
                unlockCarButton.setVisibility(View.INVISIBLE);
                cancelBookingButton.setVisibility(View.INVISIBLE);
                bookButton.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setTimer() {
        MainActivity.countDownTimer = new CountDownTimer(60*60* 1000 + 100, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                //Toast.makeText(CarActivity.this, "Timer ticked", Toast.LENGTH_SHORT).show();
                updateTime((int) millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                carRef.child("available").setValue("true");
                userRef.child("Car booked").child(carId).removeValue();
                waitingTv.setVisibility(View.INVISIBLE);
                unlockCarButton.setVisibility(View.INVISIBLE);
                cancelBookingButton.setVisibility(View.INVISIBLE);
                bookButton.setVisibility(View.VISIBLE);
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
}