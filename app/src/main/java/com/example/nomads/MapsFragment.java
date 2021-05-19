package com.example.nomads;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;

import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.collect.Multiset;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.mail.internet.InternetAddress;

import static android.content.Context.LOCATION_SERVICE;

public class MapsFragment extends Fragment implements LocationListener {

    private GoogleMap mMap;
    //GoogleApiClient googleApiClient;
    Location lastLocation;
    SearchView searchView;
    LocationManager locationManager;
    HashMap<Integer, Marker> hashMapMarker;
    HashMap<String, Marker> carsHashMapMarker;
    HashMap<String, String> carsHashMapMarkerTitle;
    LatLng centralLocation;
    int searchRadius = 1;
    boolean iscarFoundNearby = false;
    private AppCompatButton useCurrentLocationButton;

    private int locationErrorCount = 0;

    private LocationRequest locationRequest;
    public static final int LOCATION_REQUEST_CHECK_SETTING = 1001;

    FusedLocationProviderClient mFusedLocationClient;
    Location requestedLocation = null;

    //LocationManager locationManager;
    //LocationListener locationListener;
    android.location.LocationListener locationListener;

    /*
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, locationListener);
                }
            }
        }
    }

     */

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            statusCheck();
            locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);
            ///*
            locationListener = new android.location.LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {
                    statusCheck();
                }
            };

             //*/

            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                return;
            }
            //buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);

            try {
                //Location lastKnownLocation = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
                Location lastKnownLocation = getLastKnownLocation();
                LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 17));
                centralLocation = userLocation;
                isCarPresentNearby();
            } catch (Exception e) {
                locationErrorCount++;
                if(locationErrorCount > 5)
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                //enableLocationService();
                statusCheck();
            }

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    if (carsHashMapMarker.containsValue(marker)) {
                        String carKey = "";
                        for (Map.Entry<String, Marker> entry : carsHashMapMarker.entrySet()) {
                            if (entry.getValue().toString().equals(marker.toString())) {
                                carKey = entry.getKey();
                                break;
                            }
                        }
                        Intent carInfoIntent = new Intent(getContext(), CarActivity.class);
                        carInfoIntent.putExtra("carId", carKey);
                        startActivity(carInfoIntent);
                    }
                    return false;
                }
            });
        }
    };

    public Location getLastKnownLocation() throws InterruptedException {
        Log.i("#############", "getLastKnownLocation() CUSTOM");
        locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return null;
            }
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        ///*
        if(bestLocation == null) {
            final LocationManager manager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            if(manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                ProgressDialog loadingBar = new ProgressDialog(getContext());
                loadingBar.setTitle("Getting user location");
                loadingBar.setMessage("Trying to access user location...");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);

                requestNewLocationData();
                /*
                for (int i = 0; i < Integer.MAX_VALUE; ++i) {
                    Log.i("WAITING", "LOCATION REQUESTED");
                    if (requestedLocation != null) {
                        bestLocation = requestedLocation;
                        break;
                    }
                }
                 */
                Log.i("WAITING", "LOCATION REQUESTED");
                TimeUnit.SECONDS.sleep(1);      //interrupted exception possible
                loadingBar.dismiss();

                if (requestedLocation != null)
                    bestLocation = requestedLocation;
                else
                    Toast.makeText(getContext(), "Unable to access user location!", Toast.LENGTH_SHORT).show();
            }
        }
         //*/
        return bestLocation;
    }

    private void requestNewLocationData() {

        // Initializing LocationRequest
        // object with appropriate methods
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        // setting LocationRequest
        // on FusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            requestedLocation = locationResult.getLastLocation();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);
        searchView = view.findViewById(R.id.map_fragment_search_bar);
        EditText searchEditText = ((EditText) searchView.findViewById(androidx.appcompat.R.id.search_src_text));
        searchEditText.setHintTextColor(ContextCompat.getColor(getContext(), R.color.colorSecondaryText));
        searchEditText.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
        useCurrentLocationButton = view.findViewById(R.id.map_fragment_use_current_location_button);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        MapsInitializer.initialize(getContext());

        hashMapMarker = new HashMap<>();
        carsHashMapMarker = new HashMap<>();
        carsHashMapMarkerTitle = new HashMap<>();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = searchView.getQuery().toString();
                List<Address> addressList = null;

                if (location != null || !location.equals("")) {
                    Geocoder geocoder = new Geocoder(getContext());
                    try {
                        addressList = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //Remove last marker
                    if (hashMapMarker.containsKey(0)) {
                        Marker marker = hashMapMarker.get(0);
                        marker.remove();
                        hashMapMarker.remove(0);
                    }
                    try {
                        Address address = addressList.get(0);
                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                        Marker searchMarker = mMap.addMarker(new MarkerOptions().position(latLng).title(location));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));

                        hashMapMarker.put(0, searchMarker);
                        //Set central location
                        centralLocation = latLng;
                        isCarPresentNearby();
                    }catch (Exception e){
                        Toast.makeText(getContext(), "Address not found. Try Again.", Toast.LENGTH_SHORT).show();
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        mapFragment.getMapAsync(callback);

        MainActivity.carsRef = FirebaseDatabase.getInstance().getReference().child("Cars");
        MainActivity.isCarRefListening = true;
        MainActivity.carsRef.addValueEventListener(MainActivity.carRefValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(MainActivity.isCarRefListening) {
                    if (snapshot.exists()) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            if (ds.exists()) {
                                String currentCarID = ds.getKey();
                                if (ds.hasChild("Details") && ds.child("Details").hasChild("car_modelName")
                                        && ds.hasChild("l") && ds.child("l").hasChild("0")
                                        && ds.child("l").hasChild("1") && ds.hasChild("available")) {
                                    //Store marker title in hashMap
                                    String modelNo = ds.child("Details").child("car_modelName").getValue().toString();
                                    carsHashMapMarkerTitle.put(currentCarID, modelNo);

                                    //Add/Update the marker
                                    double latitude = ds.child("l").child("0").getValue(Double.class);
                                    double longitude = ds.child("l").child("1").getValue(Double.class);
                                    LatLng carLatLng = new LatLng(latitude, longitude);
                                    String available = ds.child("available").getValue().toString();
                                    Marker carMarker;
                                    //Remove last marker
                                    if (carsHashMapMarker.containsKey(currentCarID)) {
                                        Marker rmarker = carsHashMapMarker.get(currentCarID);
                                        rmarker.remove();
                                        carsHashMapMarker.remove(currentCarID);
                                    }
                                    if (available.equals("true")) {
                                        carMarker = mMap.addMarker(new MarkerOptions()
                                                .position(carLatLng)
                                                .title(carsHashMapMarkerTitle.get(currentCarID))
                                                //.icon(BitmapDescriptorFactory.fromResource(R.drawable.green_car_marker)));
                                                //.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_baseline_directions_car_24)));
                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                                    } else {
                                        carMarker = mMap.addMarker(new MarkerOptions()
                                                .position(carLatLng)
                                                .title(carsHashMapMarkerTitle.get(currentCarID))
                                                //.icon(BitmapDescriptorFactory.fromResource(R.drawable.yellow_car_marker)));
                                                //.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_baseline_directions_car_24)));
                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                                    }
                                    carsHashMapMarker.put(currentCarID, carMarker);
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        useCurrentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, locationListener);
                try {
                    //Location lastKnownLocation = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
                    Location lastKnownLocation = getLastKnownLocation();
                        LatLng userLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                    //Remove last marker
                    if (hashMapMarker.containsKey(0)) {
                        Marker marker = hashMapMarker.get(0);
                        marker.remove();
                        hashMapMarker.remove(0);
                    }
                    Marker searchMarker = mMap.addMarker(new MarkerOptions().position(userLatLng).title("Current Location"));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 17));

                    hashMapMarker.put(0, searchMarker);
                    //Set central location
                    centralLocation = userLatLng;
                    isCarPresentNearby();

                }catch (Exception e){
                    locationErrorCount++;
                    if(locationErrorCount > 5)
                    Toast.makeText(getContext(), "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    //enableLocationService();
                    statusCheck();
                }
            }
        });
    }
    
    private void isCarPresentNearby(){
        searchRadius = 1;
        iscarFoundNearby = false;

        MainActivity.isMainMapGeoQueryListening = true;
        getClosestCar();
    }

    private void getClosestCar() {
        MainActivity.carsRef = FirebaseDatabase.getInstance().getReference().child("Cars");

        GeoFire geoFire = new GeoFire(MainActivity.carsRef);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(centralLocation.latitude, centralLocation.longitude), searchRadius);
        MainActivity.mainMapGeoQuery = geoQuery;

        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(MainActivity.mainMapGeoQueryEventListener = new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(MainActivity.isMainMapGeoQueryListening && !iscarFoundNearby) {
                    iscarFoundNearby = true;
                    Toast.makeText(getContext(), "Car found nearby!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(MainActivity.isMainMapGeoQueryListening && !iscarFoundNearby){
                    Toast.makeText(getContext(), "No cars found within 1km of your location.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        //LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
    }

    private void enableLocationService() {
        locationRequest  = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                } catch (ApiException e) {
                    switch (e.getStatusCode()){
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException)e;
                                resolvableApiException.startResolutionForResult(getActivity(),LOCATION_REQUEST_CHECK_SETTING);
                                refreshMapsFragment();
                            } catch (IntentSender.SendIntentException sendIntentException) {
                                //sendIntentException.printStackTrace();
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            Toast.makeText(getContext(), "Your device doesn't support location access.", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        });
    }

    public void refreshMapsFragment(){
        getActivity().getSupportFragmentManager().beginTransaction().detach(this).attach(this).commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == LOCATION_REQUEST_CHECK_SETTING){
            switch (resultCode){
                case Activity.RESULT_OK:
                    Toast.makeText(getContext(), "GPS is turned ON.", Toast.LENGTH_SHORT).show();
                break;

                case Activity.RESULT_CANCELED:
                    Toast.makeText(getContext(), "GPS needs to be turned on.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void statusCheck() {
        final LocationManager manager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Your GPS seems to be disabled, please enable it to access some essential services of the App.")
                .setCancelable(false)
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        MainActivity.isFromSetting = true;
                    }
                })
                .setNegativeButton("No thanks", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
}