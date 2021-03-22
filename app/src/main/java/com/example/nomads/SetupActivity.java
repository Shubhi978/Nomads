package com.example.nomads;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText fullname, country, phoneNo, aadharNo, dlNo, dlIssuedBy, dlIssueDate, dlValidTill;
    private Button saveInformationButton;
    private CircleImageView profileImage;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);

        fullname = (EditText)findViewById(R.id.setup_full_name);
        country = (EditText)findViewById(R.id.setup_country);
        phoneNo = (EditText)findViewById(R.id.setup_phone_no);
        aadharNo = (EditText)findViewById(R.id.setup_aadhar_no);
        dlNo = (EditText)findViewById(R.id.setup_dlno);
        dlIssuedBy = (EditText)findViewById(R.id.setup_dl_issuer);
        dlIssueDate = (EditText)findViewById(R.id.setup_dl_issue_date);
        dlValidTill = (EditText)findViewById(R.id.setup_dl_valid_till);
        saveInformationButton = (Button)findViewById(R.id.setup_information_button);
        profileImage = (CircleImageView)findViewById(R.id.setup_profile_image);
        loadingBar = new ProgressDialog(this);

        saveInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAccountSetupInformation();
            }
        });
    }

    private void saveAccountSetupInformation() {
        String fullnameStr = fullname.getText().toString();
        String countryStr = country.getText().toString();
        String phoneNoStr = phoneNo.getText().toString();
        String aadharNoStr = aadharNo.getText().toString();
        String dlNoStr = dlNo.getText().toString();
        String dlIssuedByStr = dlIssuedBy.getText().toString();
        String dlIssueDateStr = dlIssueDate.getText().toString();
        String dlValidTillStr = dlValidTill.getText().toString();

        if(TextUtils.isEmpty(fullnameStr))
            Toast.makeText(this, "Enter full name", Toast.LENGTH_SHORT).show();
        else if(TextUtils.isEmpty(countryStr))
            Toast.makeText(this, "Enter country", Toast.LENGTH_SHORT).show();
        else if(TextUtils.isEmpty(phoneNoStr))
            Toast.makeText(this, "Enter a valid Phone number", Toast.LENGTH_SHORT).show();
        else if(TextUtils.isEmpty(aadharNoStr))
            Toast.makeText(this, "Enter your Aadhar number", Toast.LENGTH_SHORT).show();
        else if(TextUtils.isEmpty(dlNoStr))
            Toast.makeText(this, "Enter your Driving Licence number", Toast.LENGTH_SHORT).show();
        else if(TextUtils.isEmpty(dlIssuedByStr))
            Toast.makeText(this, "Enter name of the agency that issued the DL", Toast.LENGTH_SHORT).show();
        else if(TextUtils.isEmpty(dlIssueDateStr))
            Toast.makeText(this, "Enter Issue date (DOI)", Toast.LENGTH_SHORT).show();
        else if(TextUtils.isEmpty(dlValidTillStr))
            Toast.makeText(this, "Enter validity date", Toast.LENGTH_SHORT).show();
        else{
            loadingBar.setTitle("Saving Information");
            loadingBar.setMessage("Please wait while we are creating your account...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            HashMap userMap = new HashMap();
            userMap.put("fullname", fullnameStr);
            userMap.put("country", countryStr);
            userMap.put("phoneNo", phoneNoStr);
            userMap.put("aadharNo", aadharNoStr);
            userMap.put("dlNo", dlNoStr);
            userMap.put("dlIssuedBy", dlIssuedByStr);
            userMap.put("dlIssueDate", dlIssueDateStr);
            userMap.put("dlValidTill", dlValidTillStr);
            userMap.put("gender", "N/A");
            userMap.put("dob", "N/A");
            userMap.put("address", "N/A");
            userMap.put("addressL1", "N/A");
            userMap.put("addressL2", "N/A");
            userMap.put("city", "N/A");
            userMap.put("pincode", "N/A");

            userRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()) {
                        Toast.makeText(SetupActivity.this, "Account created successfully", Toast.LENGTH_LONG).show();
                        sendUserToMainActivity();
                        loadingBar.dismiss();
                    }else{
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }
    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}