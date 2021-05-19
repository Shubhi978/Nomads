package com.example.nomads;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.StrictMode;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity{

    private NavigationView navigationView;
    private TextView navProfileUsername;
    private CircleImageView navProfileImage;
    private DrawerLayout drawerLayout;
    private Toolbar mToolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private ValueEventListener userRefValueEventListener;
    Boolean isUserRefListening = true;
    private ValueEventListener currentUserCarValueEventListener;

    static DatabaseReference carsRef = FirebaseDatabase.getInstance().getReference().child("Cars");
    static ValueEventListener carRefValueEventListener;
    static Boolean isCarRefListening = true;
    static String currentUserID;
    static CountDownTimer countDownTimer;
    static Boolean isCountDownTimerRunning = false;
    static Timer rideTimer;
    static String currentRideCarID = "";
    static double rideRate = 0.75;   //Rs. per minute
    static double thresholdAmt = 2.0;   //in Rs.
    //static boolean isRidePaymentPending = false;
    static RideTimerRunnable rideTimerRunnable;
    static Handler timerHandler;
    static boolean handlerIfRunning = false;
    static Runnable timerRunnable;
    static int rideTime = 0;
    static String usersCarStatus = "none";
    static GeoQueryEventListener mainMapGeoQueryEventListener;
    static boolean isMainMapGeoQueryListening = true;
    static GeoQuery mainMapGeoQuery;
    public static boolean isFromSetting=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        isUserRefListening = true;
        isCarRefListening = true;

        mToolbar = (Toolbar)findViewById(R.id.main_activity_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView = (NavigationView)findViewById(R.id.navigation_view);
        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);

        navProfileImage = (CircleImageView)navView.findViewById(R.id.nav_header_profile_image);
        navProfileUsername = (TextView)navView.findViewById(R.id.nav_header_username);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                userMenuSelector(item);
                return false;
            }
        });

    }

    private void getCurrentRideCarID(){
        currentUserID = mAuth.getCurrentUser().getUid();
        userRef.child(currentUserID).addListenerForSingleValueEvent(currentUserCarValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(isUserRefListening){
                    if(snapshot.exists() && snapshot.child("Car booked").exists()){
                        for(DataSnapshot carChild: snapshot.child("Car booked").getChildren()){
                            String carid = carChild.getKey();
                            updateCurrentRideCarID(carid);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateCurrentRideCarID(String carid) {
        currentRideCarID = carid;
    }

    @Override
    protected void onStart() {
        super.onStart();
        isUserRefListening = true;
        isCarRefListening = true;

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null)
            sendUserToLoginActivity();
        else
            checkUserExistence();
    }

    private void checkUserExistence() {
        //final String currentUserID = mAuth.getCurrentUser().getUid();
        currentUserID = mAuth.getCurrentUser().getUid();
        userRef.addValueEventListener(userRefValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(isUserRefListening) {
                    if (snapshot.exists()) {
                        if (!snapshot.hasChild(currentUserID)) {
                            sendUserToSetupActivity();
                        } else {
                            if (snapshot.child(currentUserID).hasChild("fullname")) {
                                String name = snapshot.child(currentUserID).child("fullname").getValue().toString();
                                navProfileUsername.setText(name);
                            }
                            getCurrentRideCarID();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendUserToSetupActivity() {
        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    private void userMenuSelector(MenuItem item) {
        switch(item.getItemId()){

            case R.id.nav_profile:
                Toast.makeText(this, "Your profile", Toast.LENGTH_SHORT).show();
                Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(profileIntent);
            break;
            //Can add a "Driver's Licence" activity to upload licence for authentication
            case R.id.nav_contact_us:
                Toast.makeText(this, "Contact us", Toast.LENGTH_SHORT).show();
                Intent contactUsIntent = new Intent(MainActivity.this, AboutUsActivity.class);
                startActivity(contactUsIntent);
                break;
                /*
            case R.id.nav_add_cars_extra:
                Toast.makeText(this, "AddCar activity", Toast.LENGTH_SHORT).show();
                Intent addCarIntent = new Intent(MainActivity.this, AddCarsExtraMapActivity.class);
                startActivity(addCarIntent);
                break;
            case R.id.nav_add_parkings_extra:
                Toast.makeText(this, "AddParking activity", Toast.LENGTH_SHORT).show();
                Intent addParkingIntent = new Intent(MainActivity.this, AddParkingsExtraMapsActivity.class);
                startActivity(addParkingIntent);
                break;

                 */
            case R.id.nav_logout:
                //Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
                sendUserToLoginActivity();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isFromSetting==true){
            finish();
            startActivity(getIntent());
            isFromSetting=false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isUserRefListening = true;
        isCarRefListening = true;
        isMainMapGeoQueryListening = true;

        if (isFromSetting==true){
            finish();
            startActivity(getIntent());
            isFromSetting=false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isUserRefListening = false;
        if(userRefValueEventListener != null)
            userRef.removeEventListener(userRefValueEventListener);

        if(currentUserCarValueEventListener != null)
            userRef.child(currentUserID).removeEventListener(currentUserCarValueEventListener);


        isMainMapGeoQueryListening = false;
        //mainMapGeoQuery.removeAllListeners();
        /*
        if(mainMapGeoQueryEventListener != null)
            mainMapGeoQuery.removeGeoQueryEventListener(mainMapGeoQueryEventListener);

         //*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isUserRefListening = false;
        if(userRefValueEventListener != null)
            userRef.removeEventListener(userRefValueEventListener);

        if(currentUserCarValueEventListener != null)
            userRef.child(currentUserID).removeEventListener(currentUserCarValueEventListener);

        isMainMapGeoQueryListening = false;
        //mainMapGeoQuery.removeAllListeners();
        /*
        if(mainMapGeoQueryEventListener != null)
            mainMapGeoQuery.removeGeoQueryEventListener(mainMapGeoQueryEventListener);

         */
    }
}