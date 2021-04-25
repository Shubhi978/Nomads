package com.example.nomads;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RateActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private AppCompatButton submitBttn, skipBttn;
    private RatingBar ratingBar;
    private EditText feedbackText;

    DatabaseReference carDetailsRef;

    double rating=0.0, currentRating = 0.0;
    int currentRatingCount = 0;
    String feedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);

        mToolbar = (Toolbar)findViewById(R.id.rate_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Review");

        submitBttn = (AppCompatButton)findViewById(R.id.rate_submit_button);
        skipBttn = (AppCompatButton)findViewById(R.id.rate_skip_button);
        ratingBar = (RatingBar) findViewById(R.id.rate_rating_bar);
        feedbackText = (EditText) findViewById(R.id.rate_feedback_edittext);

        skipBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rebuildAndGoToMainScreen();
            }
        });

        submitBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rating  = ratingBar.getRating();
                if(rating == 0.0){
                    Toast.makeText(RateActivity.this, "Please give the rating", Toast.LENGTH_SHORT).show();
                }else{
                    carDetailsRef = FirebaseDatabase.getInstance().getReference().child("Cars").child(MainActivity.currentRideCarID).child("Details");
                    carDetailsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists() && snapshot.hasChild("car_rating") && snapshot.hasChild("car_ratingCount")){
                                currentRating = Double.parseDouble(snapshot.child("car_rating").getValue().toString());
                                currentRatingCount = Integer.parseInt(snapshot.child("car_ratingCount").getValue().toString());

                                //Toast.makeText(RateActivity.this, "Current rating: "+currentRating, Toast.LENGTH_SHORT).show();
                                rating = (currentRating*currentRatingCount + rating)/(currentRatingCount + 1);
                                rating = Math.round (rating*100.0)/100.0;
                                ++currentRatingCount;
                                carDetailsRef.child("car_rating").setValue(rating);
                                carDetailsRef.child("car_ratingCount").setValue(currentRatingCount);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    feedback = feedbackText.getText().toString();
                    if(!TextUtils.isEmpty(feedback)){
                        DatabaseReference feedbackRef = FirebaseDatabase.getInstance().getReference().child("Feedback");
                        feedback = "Car: " + MainActivity.currentRideCarID
                                + "\nUser: " + MainActivity.currentUserID
                                + "\nReview:\n" + feedback;
                        feedbackRef.push().setValue(feedback);

                    }

                    Toast.makeText(RateActivity.this, "Thank you!", Toast.LENGTH_SHORT).show();
                    rebuildAndGoToMainScreen();
                }
            }
        });
    }

    private void rebuildAndGoToMainScreen() {
        Intent mainIntent = new Intent(RateActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}