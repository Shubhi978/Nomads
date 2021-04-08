package com.example.nomads;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

public class ThresholdPaymentActivity extends AppCompatActivity {

    AppCompatButton successButton, failureButton;
    TextView textView;

    private Toolbar mToolbar;

    String carId;
    DatabaseReference carRef;
    FirebaseAuth mAuth;
    DatabaseReference userRef;
    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_threshold_payment);

        mToolbar = (Toolbar)findViewById(R.id.threshold_payment_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Threshold Payment Activity");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textView = (TextView)findViewById(R.id.threshold_payment_text_view);
        successButton = (AppCompatButton) findViewById(R.id.threshold_payment_success_button);
        failureButton = (AppCompatButton)findViewById(R.id.threshold_payment_failure_button);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);

        carId = getIntent().getStringExtra("carId");

        textView.setText("Pay the threshold amount to book "+carId);

        carRef = FirebaseDatabase.getInstance().getReference().child("Cars").child(carId);

        successButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                carRef.child("available").setValue("false").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            MainActivity.currentRideCarID = carId;
                            MainActivity.usersCarStatus = "booked";
                            userRef.child("Car booked").child(carId).child("status").setValue("waiting");
                            Toast.makeText(ThresholdPaymentActivity.this, "Successfully booked!", Toast.LENGTH_SHORT).show();
                            //finish();
                            sendUserToCarActivityWithSuccess();
                        }else{
                            String message = task.getException().getMessage();
                            Toast.makeText(ThresholdPaymentActivity.this, "Error while booking: "+message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        failureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ThresholdPaymentActivity.this, "Booking unsuccessful", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    ///*
    private void sendUserToCarActivityWithSuccess() {
        Intent carBookedIntent = new Intent(ThresholdPaymentActivity.this, CarActivity.class);
        carBookedIntent.putExtra("carId", carId);
        carBookedIntent.putExtra("bookedSuccess", 1);
        startActivity(carBookedIntent);
        finish();
    }

     //*/
}