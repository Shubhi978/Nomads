package com.example.nomads;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.TimerTask;

public class RideTimerTask extends TimerTask {
    public static int time = 0;
    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(MainActivity.currentUserID);

    @Override
    public void run() {
        String str;
        int tmin = ++time;
        int hr = tmin/60;
        int min = tmin%60;

        if(hr>=10)
            str = String.valueOf(hr);
        else
            str = "0"+hr;
        str = str + ":";
        if(min>=10)
            str= str + min;
        else
            str = str + "0"+ min;

        double cost = MainActivity.rideRate * tmin;
        cost = Math.round(cost*100.0)/100.0;

        userRef.child("Car booked").child(MainActivity.currentRideCarID).child("ride_time").setValue(str);
        userRef.child("Car booked").child(MainActivity.currentRideCarID).child("ride_cost").setValue(String.valueOf(cost));
    }
}
