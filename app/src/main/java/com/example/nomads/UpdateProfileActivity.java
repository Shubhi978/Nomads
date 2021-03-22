package com.example.nomads;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateProfileActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private EditText fullnameTv, dobTv, aadharNoTv, addressL1Tv, addressL2Tv, cityTv, pincodeTv, countryTv, contactTv, dlNoTv, dlIssuedByTv, dlIssueDateTv, dlValidTillTv;
    private AppCompatButton saveButton;
    private CircleImageView userProfileImage;

    private FirebaseAuth mAuth;
    private DatabaseReference updateProfileUserRef;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        updateProfileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);

        mToolbar = (Toolbar)findViewById(R.id.update_profile_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Update Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fullnameTv = (EditText)findViewById(R.id.update_profile_fullname_display);
        dobTv = (EditText)findViewById(R.id.update_profile_dob_display);
        aadharNoTv = (EditText)findViewById(R.id.update_profile_aadharNo_display);
        addressL1Tv = (EditText)findViewById(R.id.update_profile_address_line1_display);
        addressL2Tv = (EditText)findViewById(R.id.update_profile_address_line2_display);
        cityTv = (EditText)findViewById(R.id.update_profile_address_city_display);
        pincodeTv = (EditText)findViewById(R.id.update_profile_address_pincode_display);
        countryTv = (EditText)findViewById(R.id.update_profile_country_display);
        contactTv = (EditText)findViewById(R.id.update_profile_contact_no_display);
        dlNoTv = (EditText)findViewById(R.id.update_profile_dlno);
        dlIssuedByTv = (EditText)findViewById(R.id.update_profile_dl_issuer);
        dlIssueDateTv = (EditText)findViewById(R.id.update_profile_dl_issue_date);
        dlValidTillTv = (EditText)findViewById(R.id.update_profile_dl_valid_till);
        saveButton = (AppCompatButton) findViewById(R.id.update_profile_save_button);

        updateProfileUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String fullname = snapshot.child("fullname").getValue().toString();
                    String dob = snapshot.child("dob").getValue().toString();
                    String aadharNo = snapshot.child("aadharNo").getValue().toString();
                    String addressl1 = snapshot.child("addressL1").getValue().toString();
                    String addressl2 = snapshot.child("addressL2").getValue().toString();
                    String city = snapshot.child("city").getValue().toString();
                    String pincode = snapshot.child("pincode").getValue().toString();
                    String country = snapshot.child("country").getValue().toString();
                    String contact = snapshot.child("phoneNo").getValue().toString();
                    String dlNo = snapshot.child("dlNo").getValue().toString();
                    String dlIssuedBy = snapshot.child("dlIssuedBy").getValue().toString();
                    String dlIssueDate = snapshot.child("dlIssueDate").getValue().toString();
                    String dlValidTill = snapshot.child("dlValidTill").getValue().toString();

                    fullnameTv.setText(fullname);
                    dobTv.setText(dob);
                    aadharNoTv.setText(aadharNo);
                    addressL1Tv.setText(addressl1);
                    addressL2Tv.setText(addressl2);
                    cityTv.setText(city);
                    pincodeTv.setText(pincode);
                    countryTv.setText(country);
                    contactTv.setText(contact);
                    dlNoTv.setText(dlNo);
                    dlIssuedByTv.setText(dlIssuedBy);
                    dlIssueDateTv.setText(dlIssueDate);
                    dlValidTillTv.setText(dlValidTill);
                }
                else{
                    Toast.makeText(UpdateProfileActivity.this, "Snapshot doesn't exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAccountInfo();
            }
        });
    }

    private void validateAccountInfo() {
        String fullname = fullnameTv.getText().toString();
        String dob = dobTv.getText().toString();
        String aadharNo = aadharNoTv.getText().toString();
        String addressl1 = addressL1Tv.getText().toString();
        String addressl2 = addressL2Tv.getText().toString();
        String city = cityTv.getText().toString();
        String pincode = pincodeTv.getText().toString();
        String country = countryTv.getText().toString();
        String contact = contactTv.getText().toString();
        String dlNo = dlNoTv.getText().toString();
        String dlIssuedBy = dlIssuedByTv.getText().toString();
        String dlIssueDate = dlIssueDateTv.getText().toString();
        String dlValidTill = dlValidTillTv.getText().toString();

        if(TextUtils.isEmpty(fullname))
            Toast.makeText(this, "fullname field empty", Toast.LENGTH_SHORT).show();
        else if(TextUtils.isEmpty(dob))
            Toast.makeText(this, "dob field empty", Toast.LENGTH_SHORT).show();
        else if(TextUtils.isEmpty(aadharNo))
            Toast.makeText(this, "aadharNo field empty", Toast.LENGTH_SHORT).show();
        else if(TextUtils.isEmpty(addressl1))
            Toast.makeText(this, "addressl1 field empty", Toast.LENGTH_SHORT).show();
        else if(TextUtils.isEmpty(addressl2))
            Toast.makeText(this, "addressl2 field empty", Toast.LENGTH_SHORT).show();
        else if(TextUtils.isEmpty(city))
            Toast.makeText(this, "city field empty", Toast.LENGTH_SHORT).show();
        else if(TextUtils.isEmpty(pincode))
            Toast.makeText(this, "pincode field empty", Toast.LENGTH_SHORT).show();
        else if(TextUtils.isEmpty(country))
            Toast.makeText(this, "country field empty", Toast.LENGTH_SHORT).show();
        else if(TextUtils.isEmpty(contact))
            Toast.makeText(this, "contact field empty", Toast.LENGTH_SHORT).show();
        else if(TextUtils.isEmpty(dlNo))
            Toast.makeText(this, "Driving Licence number field empty", Toast.LENGTH_SHORT).show();
        else if(TextUtils.isEmpty(dlIssuedBy))
            Toast.makeText(this, "name of the agency that issued the DL field empty", Toast.LENGTH_SHORT).show();
        else if(TextUtils.isEmpty(dlIssueDate))
            Toast.makeText(this, "Issue date field empty", Toast.LENGTH_SHORT).show();
        else if(TextUtils.isEmpty(dlValidTill))
            Toast.makeText(this, "validity date field empty", Toast.LENGTH_SHORT).show();
        else{
            HashMap userMap = new HashMap();
            userMap.put("fullname", fullname);
            userMap.put("dob", dob);
            userMap.put("aadharNo", aadharNo);
            userMap.put("addressL1", addressl1);
            userMap.put("addressL2", addressl2);
            userMap.put("city", city);
            userMap.put("pincode", pincode);
            userMap.put("country", country);
            userMap.put("phoneNo", contact);
            userMap.put("dlNo", dlNo);
            userMap.put("dlIssuedBy", dlIssuedBy);
            userMap.put("dlIssueDate", dlIssueDate);
            userMap.put("dlValidTill", dlValidTill);

            String address = addressl1 + ",\n" + addressl2 + ",\n" + city + ".";
            userMap.put("address", address);

            updateProfileUserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Toast.makeText(UpdateProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        sendUserToProfileActivity();
                    }else{
                        String message = task.getException().getMessage().toString();
                        Toast.makeText(UpdateProfileActivity.this, "Error occured: "+message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void sendUserToProfileActivity() {
        Intent profileIntent = new Intent(UpdateProfileActivity.this, ProfileActivity.class);
        //profileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(profileIntent);
        finish();
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(UpdateProfileActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}