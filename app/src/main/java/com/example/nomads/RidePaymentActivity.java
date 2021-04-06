package com.example.nomads;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RidePaymentActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    double cost = 0.0;

    AppCompatButton successButton, failureButton;
    TextView textView;

    int success = 0;
    String errMssg = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_payment);

        mToolbar = (Toolbar)findViewById(R.id.ride_payment_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Ride Payment Activity");

        String costStr = getIntent().getStringExtra("cost");
        if(!TextUtils.isEmpty(costStr)){
            cost = Double.parseDouble(costStr.substring(costStr.indexOf('.')+1));
        }

        textView = findViewById(R.id.ride_payment_text_view);
        textView.setText("Pay " + costStr + " - Threshold amount");

        successButton = (AppCompatButton) findViewById(R.id.ride_payment_success_button);
        failureButton = (AppCompatButton)findViewById(R.id.ride_payment_failure_button);

        successButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Can add to user history
                DatabaseReference carRef = FirebaseDatabase.getInstance().getReference().child("Cars").child(MainActivity.currentRideCarID);
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(MainActivity.currentUserID);
                /*
                final int[] success = {0};
                final String[] errMssg = {""};
                carRef.child("available").setValue("true").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            success[0]++;
                        }else{
                            errMssg[0] += task.getException().getMessage();
                        }
                    }
                });
                userRef.child("Car booked").child(MainActivity.currentRideCarID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            success[0]++;
                        }else{
                            errMssg[0] += task.getException().getMessage();
                        }
                    }
                });
                if(success[0] == 2){
                    Toast.makeText(RidePaymentActivity.this, "Payment Sucessful! Ride ended successfully.", Toast.LENGTH_SHORT).show();
                    Intent mainIntent = new Intent(RidePaymentActivity.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    finish();
                }else{
                    Toast.makeText(RidePaymentActivity.this, "Payment unsuccessful. Error: "+ errMssg[0] + " TRY AGAIN.", Toast.LENGTH_LONG).show();
                    //finish();
                }

                 //*/
                ///*
                carRef.child("available").setValue("true");
                userRef.child("Car booked").child(MainActivity.currentRideCarID).removeValue();
                Toast.makeText(RidePaymentActivity.this, "Payment Sucessful! Ride ended successfully.", Toast.LENGTH_SHORT).show();
                Intent mainIntent = new Intent(RidePaymentActivity.this, MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainIntent);
                finish();
                //*/
            }
        });
        failureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(RidePaymentActivity.this, "Payment unsuccessful. TRY AGAIN.", Toast.LENGTH_LONG).show();
                //finish();
            }
        });
    }
}