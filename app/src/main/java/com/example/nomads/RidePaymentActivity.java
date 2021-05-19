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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

import java.util.ArrayList;

public class RidePaymentActivity extends AppCompatActivity implements PaymentResultListener {

    private Toolbar mToolbar;

    AppCompatButton successButton, failureButton, finishBttn, razorpayBttn, upiBttn;
    TextView textView;
    RelativeLayout paymentContainer;

    int success = 0;
    String errMssg = "";
    String email = "", phno = "";

    DatabaseReference userRef;

    //for UPI
    String TAG ="main";
    final int UPI_PAYMENT = 0;
    String upiReceivername = "Anurag Srivastava";       //"Jyotsana Srivastava"
    String upiReceiverId = "anuragsrivastava9@hdfcbank";      //"8303064565@paytm"
    String upiNote = "Nomads ride payment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_payment);

        Checkout.preload(getApplicationContext());      //Razorpay

        mToolbar = (Toolbar)findViewById(R.id.ride_payment_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Ride Payment");

        textView = findViewById(R.id.ride_payment_text_view);
        successButton = (AppCompatButton) findViewById(R.id.ride_payment_success_button);
        failureButton = (AppCompatButton)findViewById(R.id.ride_payment_failure_button);
        finishBttn = (AppCompatButton)findViewById(R.id.ride_payment_finish_bttn);
        razorpayBttn = (AppCompatButton)findViewById(R.id.ride_payment_razorpay_button);
        upiBttn = (AppCompatButton)findViewById(R.id.ride_payment_upi_button);
        paymentContainer = (RelativeLayout)findViewById(R.id.ride_payment_container);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(MainActivity.currentUserID);
        userRef.child("Car booked").child(MainActivity.currentRideCarID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.child("ride_time").exists() && snapshot.child("ride_cost").exists()){
                    String d = snapshot.child("ride_time").getValue().toString();
                    double c = Double.parseDouble(snapshot.child("ride_cost").getValue().toString());
                    double fc = c - MainActivity.thresholdAmt;
                    if(fc<0) {
                        fc = 0.0;
                        finishBttn.setVisibility(View.VISIBLE);
                        paymentContainer.setVisibility(View.INVISIBLE);
                    }else{
                        finishBttn.setVisibility(View.INVISIBLE);
                        paymentContainer.setVisibility(View.VISIBLE);
                    }

                    String txt = "Duration of ride: "+d
                            +"\nCost: Rs."+c
                            +"\nAmount paid (Threshold amount): Rs."+MainActivity.thresholdAmt
                            +"\nFinal amount: Rs." + fc;

                    textView.setText(txt);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        /*
        String costStr = getIntent().getStringExtra("cost");
        if(!TextUtils.isEmpty(costStr)){
            cost = Double.parseDouble(costStr.substring(costStr.indexOf('.')+1));
        }
        textView.setText("Pay " + costStr + " - Threshold amount");

         */

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
                Toast.makeText(RidePaymentActivity.this, "Transaction failed. TRY AGAIN.", Toast.LENGTH_LONG).show();
                //finish();
            }
        });
        finishBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paymentSuccessful();
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
                        upiNote, Double.toString(getCost()));
            }
        });
    }

    private void retrieveUserContactDetails(String e, String pn) {
        email = e;
        phno = pn;
    }

    private void paymentSuccessful(){
        //Can add to user history
        DatabaseReference carRef = FirebaseDatabase.getInstance().getReference().child("Cars").child(MainActivity.currentRideCarID);
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(MainActivity.currentUserID);

        carRef.child("available").setValue("true");
        userRef.child("Car booked").child(MainActivity.currentRideCarID).removeValue();
        Toast.makeText(RidePaymentActivity.this, "Payment Sucessful! Ride ended successfully.", Toast.LENGTH_SHORT).show();

        //MainActivity.isRidePaymentPending = false;

        Intent rateIntent = new Intent(RidePaymentActivity.this, RateActivity.class);
        startActivity(rateIntent);
        finish();
    }

    private double getCost(){
        String txt = textView.getText().toString();
        Double c = Double.parseDouble(txt.substring(txt.lastIndexOf('s')+2).trim());
        //Toast.makeText(this, "Cost: "+c, Toast.LENGTH_SHORT).show();
        return c;
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
            options.put("amount", getCost()*100);//Amt = 500 , then pass 500*100  = 50000
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
        Toast.makeText(this, "Failed due to: "+s+"\nTRY AGAIN!", Toast.LENGTH_LONG).show();
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
            Toast.makeText(RidePaymentActivity.this,"No UPI app found, please install one to continue",Toast.LENGTH_SHORT).show();
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
        if (isConnectionAvailable(RidePaymentActivity.this)) {
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
                Toast.makeText(RidePaymentActivity.this, "Payment cancelled by user.", Toast.LENGTH_SHORT).show();
                //Log.e("UPI", "Cancelled by user: "+approvalRefNo);
            }
            else {
                Toast.makeText(RidePaymentActivity.this, "Transaction failed. TRY AGAIN.", Toast.LENGTH_LONG).show();
                //Log.e("UPI", "failed payment: "+approvalRefNo);
            }
        } else {
            Log.e("UPI", "Internet issue: ");
            Toast.makeText(RidePaymentActivity.this, "Internet connection is not available. Please check and try again", Toast.LENGTH_SHORT).show();
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