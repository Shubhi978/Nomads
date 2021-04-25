package com.example.nomads;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.security.auth.Subject;

public class AboutUsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    TextView email_1_tv, phone_1_tv, phone_2_tv;

    private static final int REQUEST_CALL = 1;
    String phone1 = "8303064565", phone2 = "7985923391", email1 = "iit2019163@iiita.ac.in";
    String contact = "";
    int feedbackNo = 0;

    private DatabaseReference feedbackNoRef;
    private ValueEventListener feedbackNoRefValueEventListener;
    Boolean isFeedbackNoRefListening = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        mToolbar = (Toolbar)findViewById(R.id.about_us_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Contact Us");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        email_1_tv = (TextView)findViewById(R.id.about_us_email_1);
        phone_1_tv = (TextView)findViewById(R.id.about_us_phone_1);
        phone_2_tv = (TextView)findViewById(R.id.about_us_phone_2);

        feedbackNoRef = FirebaseDatabase.getInstance().getReference().child("FeedbackEmailNo");
        isFeedbackNoRefListening = true;

        feedbackNoRef.addListenerForSingleValueEvent(feedbackNoRefValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(isFeedbackNoRefListening){
                    String fdbkNo = snapshot.getValue().toString();
                    getFeedbackNo(fdbkNo);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        email_1_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contact = "email1";
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                String currentDateandTime = sdf.format(new Date());
                sendEmail(email1, currentDateandTime);

                feedbackNo++;
                feedbackNoRef.setValue(feedbackNo);
            }
        });

        phone_1_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contact = "phone1";
                makePhoneCall(phone1);
            }
        });

        phone_2_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contact = "phone2";
                makePhoneCall(phone2);
            }
        });
    }

    private void getFeedbackNo(String fdbkNo) {
        feedbackNo = Integer.parseInt(fdbkNo) + 1;
    }

    private void sendEmail(String emailId, String date) {
        String[] recipient = emailId.split(",");
        String subject = "NOMADS user feedback #" + feedbackNo;
        String message = "\n<Type your message>\n\n\n" +
                "----------------DO NOT EDIT THIS----------------" +
                "\nFeedback No: " + feedbackNo +
                "\nDate: " + date +
                "\nFrom\n\tUser: " + MainActivity.currentUserID;

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, recipient);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, message);

        emailIntent.setType("message/rfc822");
        startActivity(Intent.createChooser(emailIntent, "Choose and email client"));
    }

    private void makePhoneCall(String contactNo) {
        if (ContextCompat.checkSelfPermission(AboutUsActivity.this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AboutUsActivity.this,
                    new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
        } else {
            String dial = "tel:" + contactNo;
            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(dial));
            startActivity(callIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(contact.equals("phone1"))
                    makePhoneCall(phone1);
                else
                    makePhoneCall(phone2);
            } else {
                Toast.makeText(this, "Permission needed to make the call.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isFeedbackNoRefListening = true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        isFeedbackNoRefListening = false;
        if(feedbackNoRefValueEventListener != null)
            feedbackNoRef.child("Details").removeEventListener(feedbackNoRefValueEventListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        isFeedbackNoRefListening = false;
        if(feedbackNoRefValueEventListener != null)
            feedbackNoRef.child("Details").removeEventListener(feedbackNoRefValueEventListener);
    }
}