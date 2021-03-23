package com.example.nomads;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import androidx.appcompat.widget.AppCompatButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class CarActivity extends AppCompatActivity {

    private static final String TAG = CarActivity.class.getSimpleName();

    private ImageView carImage;
    private AppCompatButton bookingButton;
    private TextView car_plateNoTv, car_modelNoTv, car_mileageTv, car_ratingTv, car_availabilityTv;

    private Toolbar mToolbar;

    private FirebaseAuth mAuth;
    private DatabaseReference profileUserRef;
    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        profileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);

        mToolbar = (Toolbar)findViewById(R.id.profile_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("CarProfile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        car_plateNoTv = (TextView)findViewById(R.id.car_plateNo);
        car_modelNoTv = (TextView)findViewById(R.id.car_modelNo);
        car_mileageTv = (TextView)findViewById(R.id.car_mileage);
        car_ratingTv = (TextView)findViewById(R.id.car_rating);
        car_availabilityTv = (TextView)findViewById(R.id.car_availability);

        carImage = (ImageView)findViewById(R.id.car_imageView);
        AppCompatButton bookingButton = (AppCompatButton)findViewById(R.id.car_book_button);


        profileUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){

                    String car_plateNo = snapshot.child("car_plateNo").getValue().toString();
                    String car_modelNo = snapshot.child("car_modelNo").getValue().toString();
                    String car_mileage = snapshot.child("car_mileage").getValue().toString();
                    String car_rating = snapshot.child("car_rating").getValue().toString();
                    String car_availability = snapshot.child("car_availability").getValue().toString();

                    car_plateNoTv.setText(car_plateNo);
                    car_modelNoTv.setText(car_modelNo);
                    car_mileageTv.setText(car_mileage);
                    car_ratingTv.setText(car_rating);
                    car_availabilityTv.setText(car_availability);
                }
                else{
                    Toast.makeText(CarActivity.this, "Snapshot doesn't exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        bookingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent bookingIntent = new Intent(CarActivity.this, MainActivity.class);
                startActivity(bookingIntent);
            }
        });
    }

}

