package com.example.nomads;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

public class RateActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextView rateText;

    private MaterialEditText etComment;
    private MaterialRatingBar ratingBar;
    private AppCompatButton btnSubmit;

    FirebaseDatabase database;
    DatabaseReference rateDetailRef;

    double ratingStars=0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);

        database=FirebaseDatabase.getInstance();

        mToolbar = (Toolbar)findViewById(R.id.profile_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Review Screen");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ratingBar=(MaterialRatingBar)findViewById(R.id.ratingBar);
        etComment=(MaterialEditText) findViewById(R.id.etComment);

        ratingBar.setOnRatingChangeListener(new MaterialRatingBar.OnRatingChangeListener() {
            @Override
            public void onRatingChanged(MaterialRatingBar ratingBar, float rating) {
                ratingStars=rating;
            }
        });
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitRateDetails();
            }
        });
    }

    private void submitRateDetails() {
    }
}