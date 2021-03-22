package com.example.nomads;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    TextView fullnameTv, dobTv, aadharNoTv, addressTv, countryTv, contactTv,  dlNoTv, dlIssuedByTv, dlIssueDateTv, dlValidTillTv;
    AppCompatButton editProfileButton;
    CircleImageView userProfileImage;

    private Toolbar mToolbar;

    FirebaseAuth mAuth;
    DatabaseReference profileUserRef;
    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        profileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);

        mToolbar = (Toolbar)findViewById(R.id.profile_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fullnameTv = (TextView)findViewById(R.id.profile_fullname_display);
        dobTv = (TextView)findViewById(R.id.profile_dob_display);
        aadharNoTv = (TextView)findViewById(R.id.profile_aadharNo_display);
        addressTv = (TextView)findViewById(R.id.profile_address_display);
        countryTv = (TextView)findViewById(R.id.profile_country_display);
        contactTv = (TextView)findViewById(R.id.profile_contact_no_display);
        dlNoTv = (TextView)findViewById(R.id.profile_dlno);
        dlIssuedByTv = (TextView)findViewById(R.id.profile_dl_issuer);
        dlIssueDateTv = (TextView)findViewById(R.id.profile_dl_issue_date);
        dlValidTillTv = (TextView)findViewById(R.id.profile_dl_valid_till);
        editProfileButton = (AppCompatButton) findViewById(R.id.profile_edit_button);

        profileUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String fullname = snapshot.child("fullname").getValue().toString();
                    String dob = snapshot.child("dob").getValue().toString();
                    String aadharNo = snapshot.child("aadharNo").getValue().toString();
                    String address = snapshot.child("address").getValue().toString();
                    String country = snapshot.child("country").getValue().toString();
                    String contact = snapshot.child("phoneNo").getValue().toString();
                    String dlNo = snapshot.child("dlNo").getValue().toString();
                    String dlIssuedBy = snapshot.child("dlIssuedBy").getValue().toString();
                    String dlIssueDate = snapshot.child("dlIssueDate").getValue().toString();
                    String dlValidTill = snapshot.child("dlValidTill").getValue().toString();


                    fullnameTv.setText(fullname);
                    dobTv.setText(dob);
                    aadharNoTv.setText(aadharNo);
                    addressTv.setText(address);
                    countryTv.setText(country);
                    contactTv.setText(contact);
                    dlNoTv.setText(dlNo);
                    dlIssuedByTv.setText(dlIssuedBy);
                    dlIssueDateTv.setText(dlIssueDate);
                    dlValidTillTv.setText(dlValidTill);
                }
                else{
                    Toast.makeText(ProfileActivity.this, "Snapshot doesn't exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent updateProfileIntent = new Intent(ProfileActivity.this, UpdateProfileActivity.class);
                startActivity(updateProfileIntent);
            }
        });
    }
}



