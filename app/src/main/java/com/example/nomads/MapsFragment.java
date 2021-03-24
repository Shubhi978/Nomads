package com.example.nomads;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import com.firebase.geofire.GeoFire;
import com.google.android.gms.location.LocationListener;

import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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

public class MapsFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;
    SearchView searchView;
    LocationManager locationManager;
    HashMap<Integer, Marker> hashMapMarker;
    HashMap<String, Marker> carsHashMapMarker;
    LatLng centralLocation;
    GeoFire geoFire;
    private AppCompatButton useCurrentLocationButton;

    //LocationManager locationManager;
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
            locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

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
            //buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);

            Location lastKnownLocation = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
            LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            //mMap.clear();
            //mMap.addMarker(new MarkerOptions().position(userLocation).title("Your location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 17));
            centralLocation = userLocation;

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    if(carsHashMapMarker.containsValue(marker)){
                        String carKey = "";
                        for(Map.Entry<String, Marker> entry: carsHashMapMarker.entrySet()) {
                            //if(entry.getValue() == marker) {
                            if(entry.getValue().toString().equals(marker.toString())) {
                                carKey = entry.getKey();
                                break;
                            }
                        }
                        //Toast.makeText(getContext(), "Clicked marker key: "+carKey, Toast.LENGTH_SHORT).show();
                        Intent carInfoIntent = new Intent(getContext(), CarActivity.class);
                        carInfoIntent.putExtra("carId", carKey);
                        startActivity(carInfoIntent);
                    }
                    /*
                    if(hashMapMarker.containsKey(0)){
                        if(marker != hashMapMarker.get(0)){
                            sendUserToCarActivity();
                        }
                    }else
                        sendUserToCarActivity();

                     */
                    return false;
                }
            });
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        googleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        //geoFire = new GeoFire(userRef);
        /*
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

         */

    }

    @Override
    public void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    /*
    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

     */

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);
        searchView = view.findViewById(R.id.map_fragment_search_bar);
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

        hashMapMarker = new HashMap<>();
        carsHashMapMarker = new HashMap<>();

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

                    Address address = addressList.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    Marker searchMarker = mMap.addMarker(new MarkerOptions().position(latLng).title(location));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));

                    hashMapMarker.put(0, searchMarker);
                    //Set central location
                    centralLocation = latLng;
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        mapFragment.getMapAsync(callback);

        DatabaseReference carsRef = FirebaseDatabase.getInstance().getReference().child("Cars");
        carsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    if(ds.exists()){
                        String currentCarID = ds.getKey();
                        if(ds.child("Location").hasChild("latitude") && ds.child("Location").hasChild("longitude") && ds.hasChild("available")){
                            double latitude = ds.child("Location").child("latitude").getValue(Double.class);
                            double longitude = ds.child("Location").child("longitude").getValue(Double.class);
                            LatLng carLatLng = new LatLng(latitude, longitude);
                            String available = ds.child("available").getValue().toString();
                            Marker carMarker;
                            //Remove last marker
                            if (carsHashMapMarker.containsKey(currentCarID)) {
                                Marker rmarker = carsHashMapMarker.get(currentCarID);
                                rmarker.remove();
                                carsHashMapMarker.remove(currentCarID);
                            }
                            if(available.equals("true")){
                                carMarker = mMap.addMarker(new MarkerOptions()
                                        .position(carLatLng)
                                        .title("car model number")
                                        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.green_car_marker)));
                                        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_baseline_directions_car_24)));
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                            }else{
                                carMarker = mMap.addMarker(new MarkerOptions()
                                        .position(carLatLng)
                                        .title("car model number")
                                        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.yellow_car_marker)));
                                        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_baseline_directions_car_24)));
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                            }
                            carsHashMapMarker.put(currentCarID, carMarker);
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
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                Location lastKnownLocation = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
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
            }
        });
    }

    private void sendUserToCarActivity() {
        Intent carInfoIntent = new Intent(getContext(), CarActivity.class);
        startActivity(carInfoIntent);
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);

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
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, (com.google.android.gms.location.LocationListener) this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onStop() {
        super.onStop();
    }

}