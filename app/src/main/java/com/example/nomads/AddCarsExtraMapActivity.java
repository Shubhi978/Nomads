package com.example.nomads;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddCarsExtraMapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private Toolbar mToolbar;

    private GoogleMap mMap;

    LocationManager locationManager;
    LocationListener locationListener;
    EditText customCarID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_cars_extra_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        customCarID = (EditText)findViewById(R.id.custom_car_id);

        mToolbar = (Toolbar)findViewById(R.id.add_car_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Add Cars (extra activity)");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, locationListener);
                }
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);

        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(location != null) {
                    //mMap.clear();
                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(userLocation).title("Current Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 17));
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else{
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, locationListener);
            Location lastKnownLocation = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);

            if(lastKnownLocation != null) {
                //mMap.clear();
                LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 17));
            }
        }
    }


    @Override
    public void onMapLongClick(LatLng latLng) {
        /*
        Location placeLocation = new Location(locationManager.GPS_PROVIDER);
        placeLocation.setLatitude(latLng.latitude);
        placeLocation.setLongitude(latLng.longitude);

         */

        mMap.addMarker(new MarkerOptions().position(latLng).title("New Car"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));

        String carID = customCarID.getText().toString();
        if(TextUtils.isEmpty(carID)){
            Toast.makeText(this, "UNIQUE Custom car id needed!", Toast.LENGTH_SHORT).show();
        }else{
            DatabaseReference carRef = FirebaseDatabase.getInstance().getReference().child("Cars").child(carID);
            /*
            //GeoFire geoFire = new GeoFire(carRef.child("location"));
            GeoFire geoFire = new GeoFire(carRef);
            geoFire.setLocation("location", new GeoLocation(latLng.latitude , latLng.longitude));
             */
            //Alternative location in Firebase
            carRef.child("Location").child("latitude").setValue(latLng.latitude);
            carRef.child("Location").child("longitude").setValue(latLng.longitude);

            carRef.child("available").setValue("true");
            carRef.child("Details").child("car_modelName").setValue("Model name");
            carRef.child("Details").child("car_plateNo").setValue("Vehicle Identification Number");
            carRef.child("Details").child("car_modelNo").setValue("model no");
            carRef.child("Details").child("car_mileage").setValue("mileage");
            carRef.child("Details").child("car_rating").setValue("rating");
            //Other details
        }

    }
}