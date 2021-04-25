package com.example.nomads;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;

public class ThresholdPaymentActivity extends AppCompatActivity implements PaymentResultListener {

    AppCompatButton successButton, failureButton, razorpayBttn, upiBttn;
    TextView textView;

    private Toolbar mToolbar;

    String carId;
    DatabaseReference carRef;
    FirebaseAuth mAuth;
    DatabaseReference userRef;
    String currentUserID;

    //for Razorpay
    String email = "", phno = "";

    //for UPI
    String TAG ="main";
    final int UPI_PAYMENT = 0;
    String upiReceivername = "Anurag Srivastava";       //"Jyotsana Srivastava"
    String upiReceiverId = "anuragsrivastava9@hdfcbank";      //"8303064565@paytm"
    String upiNote = "Nomads threshold payment to confirm booking.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_threshold_payment);

        Checkout.preload(getApplicationContext());      //Razorpay

        mToolbar = (Toolbar)findViewById(R.id.threshold_payment_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Threshold Payment Activity");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textView = (TextView)findViewById(R.id.threshold_payment_text_view);
        successButton = (AppCompatButton) findViewById(R.id.threshold_payment_success_button);
        failureButton = (AppCompatButton)findViewById(R.id.threshold_payment_failure_button);
        razorpayBttn = (AppCompatButton)findViewById(R.id.threshold_payment_razorpay_button);
        upiBttn = (AppCompatButton)findViewById(R.id.threshold_payment_upi_button);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);

        carId = getIntent().getStringExtra("carId");

        textView.setText("Pay the threshold amount to book "+carId+"\nAmount Payable: Rs."+MainActivity.thresholdAmt);

        carRef = FirebaseDatabase.getInstance().getReference().child("Cars").child(carId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.child("email").exists() && snapshot.child("phoneNo").exists()){
                    String e = snapshot.child("email").getValue().toString();
                    String pn = snapshot.child("phoneNo").getValue().toString();
                    retrieveUserContactDetails(e, pn);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        successButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paymentSuccessful();
            }
        });
        failureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paymentFailed();
            }
        });
        razorpayBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRazorpayPayment();
            }
        });
        upiBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                payUsingUpi(upiReceivername, upiReceiverId,
                        upiNote, Double.toString(MainActivity.thresholdAmt));
            }
        });
    }

    private void retrieveUserContactDetails(String e, String pn) {
        email = e;
        phno = pn;
    }

    private void paymentSuccessful(){
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

    private void paymentFailed(){
        Toast.makeText(ThresholdPaymentActivity.this, "Transaction failed. Booking unsuccessful.", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void sendUserToCarActivityWithSuccess() {
        Intent carBookedIntent = new Intent(ThresholdPaymentActivity.this, CarActivity.class);
        carBookedIntent.putExtra("carId", carId);
        carBookedIntent.putExtra("bookedSuccess", 1);
        startActivity(carBookedIntent);
        finish();
    }

    //Razorpay Payment
    private void startRazorpayPayment() {
        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_FGiXB0VQZZpxOY");

        checkout.setImage(R.drawable.logo);

        final Activity activity = this;

        try {
            JSONObject options = new JSONObject();

            options.put("name", "Nomads Car Service");
            options.put("description", "Reference No. #123456");
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png");
            //options.put("order_id", "order_DBJOWzybf0sJbb");//from response of step 3.
            options.put("theme.color", "#3399cc");
            options.put("currency", "INR");
            options.put("amount", MainActivity.thresholdAmt*100);//Amt = 500 , then pass 500*100  = 50000
            options.put("prefill.email", email);
            options.put("prefill.contact",phno);
            JSONObject retryObj = new JSONObject();
            retryObj.put("enabled", true);
            retryObj.put("max_count", 4);
            options.put("retry", retryObj);

            checkout.open(activity, options);

        } catch(Exception e) {
            Log.e("TAG", "Error in starting Razorpay Checkout", e);
        }
    }

    @Override
    public void onPaymentSuccess(String s) {
        paymentSuccessful();
    }

    @Override
    public void onPaymentError(int i, String s) {
        paymentFailed();
    }

    //UPI Payment
    void payUsingUpi(String name,String upiId, String note, String amount) {
        Log.e("main ", "name "+name +"--up--"+upiId+"--"+ note+"--"+amount);
        Uri uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", upiId)
                .appendQueryParameter("pn", name)
                //.appendQueryParameter("mc", "")       //uncomment with business account
                //.appendQueryParameter("tid", "02125412")
                //.appendQueryParameter("tr", "25584584")       //uncomment with business account
                .appendQueryParameter("tn", note)
                .appendQueryParameter("am", amount)
                .appendQueryParameter("cu", "INR")
                //.appendQueryParameter("refUrl", "blueapp")
                .build();
        Intent upiPayIntent = new Intent(Intent.ACTION_VIEW);
        upiPayIntent.setData(uri);
        // will always show a dialog to user to choose an app
        Intent chooser = Intent.createChooser(upiPayIntent, "Pay with");
        // check if intent resolves
        if(null != chooser.resolveActivity(getPackageManager())) {
            startActivityForResult(chooser, UPI_PAYMENT);
        } else {
            Toast.makeText(ThresholdPaymentActivity.this,"No UPI app found, please install one to continue",Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("main ", "response "+resultCode );
        /*
       E/main: response -1
       E/UPI: onActivityResult: txnId=AXI4a3428ee58654a938811812c72c0df45&responseCode=00&Status=SUCCESS&txnRef=922118921612
       E/UPIPAY: upiPaymentDataOperation: txnId=AXI4a3428ee58654a938811812c72c0df45&responseCode=00&Status=SUCCESS&txnRef=922118921612
       E/UPI: payment successfull: 922118921612
         */
        switch (requestCode) {
            case UPI_PAYMENT:
                if ((RESULT_OK == resultCode) || (resultCode == 11)) {
                    if (data != null) {
                        String trxt = data.getStringExtra("response");
                        Log.e("UPI", "onActivityResult: " + trxt);
                        ArrayList<String> dataList = new ArrayList<>();
                        dataList.add(trxt);
                        upiPaymentDataOperation(dataList);
                    } else {
                        Log.e("UPI", "onActivityResult: " + "Return data is null");
                        ArrayList<String> dataList = new ArrayList<>();
                        dataList.add("nothing");
                        upiPaymentDataOperation(dataList);
                    }
                } else {
                    //when user simply back without payment
                    Log.e("UPI", "onActivityResult: " + "Return data is null");
                    ArrayList<String> dataList = new ArrayList<>();
                    dataList.add("nothing");
                    upiPaymentDataOperation(dataList);
                }
                break;
        }
    }
    private void upiPaymentDataOperation(ArrayList<String> data) {
        if (isConnectionAvailable(ThresholdPaymentActivity.this)) {
            String str = data.get(0);
            Log.e("UPIPAY", "upiPaymentDataOperation: "+str);
            String paymentCancel = "";
            if(str == null) str = "discard";
            String status = "";
            String approvalRefNo = "";
            String response[] = str.split("&");
            for (int i = 0; i < response.length; i++) {
                String equalStr[] = response[i].split("=");
                if(equalStr.length >= 2) {
                    if (equalStr[0].toLowerCase().equals("Status".toLowerCase())) {
                        status = equalStr[1].toLowerCase();
                    }
                    else if (equalStr[0].toLowerCase().equals("ApprovalRefNo".toLowerCase()) || equalStr[0].toLowerCase().equals("txnRef".toLowerCase())) {
                        approvalRefNo = equalStr[1];
                    }
                }
                else {
                    paymentCancel = "Payment cancelled by user.";
                }
            }
            if (status.equals("success")) {
                //Code to handle successful transaction here.
                //Toast.makeText(RidePaymentActivity.this, "Transaction successful.", Toast.LENGTH_SHORT).show();
                //Log.e("UPI", "payment successfull: "+approvalRefNo);
                paymentSuccessful();
            }
            else if("Payment cancelled by user.".equals(paymentCancel)) {
                Toast.makeText(ThresholdPaymentActivity.this, "Payment cancelled by user.", Toast.LENGTH_SHORT).show();
                //Log.e("UPI", "Cancelled by user: "+approvalRefNo);
            }
            else {
                paymentFailed();
            }
        } else {
            Log.e("UPI", "Internet issue: ");
            Toast.makeText(ThresholdPaymentActivity.this, "Internet connection is not available. Please check and try again", Toast.LENGTH_SHORT).show();
        }
    }
    public static boolean isConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()
                    && netInfo.isConnectedOrConnecting()
                    && netInfo.isAvailable()) {
                return true;
            }
        }
        return false;
    }
}