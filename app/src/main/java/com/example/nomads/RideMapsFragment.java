package com.example.nomads;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.SearchRecentSuggestionsProvider;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RideMapsFragment extends Fragment{

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    
    AppCompatButton searchParkingBttn;
    Marker parkingMarker;

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

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            //Toast.makeText(getContext(), "Map is ready!", Toast.LENGTH_SHORT).show();
            mMap = googleMap;
            locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            ///*
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Toast.makeText(getContext(), "User Location Changed!", Toast.LENGTH_SHORT).show();
                    mMap.clear();
                    LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(userLatLng).title("Your Location"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(userLatLng));

                    RideActivity.userLocation = location;
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
            //*/
            if (Build.VERSION.SDK_INT < 23) {
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
                locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, locationListener);
            } else {
                if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                } else{
                    ///*
                    locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, locationListener);
                    Location lastKnownLocation = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
                    mMap.clear();
                    LatLng userLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(userLatLng).title("Your Location"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 17));

                    RideActivity.userLocation = lastKnownLocation;

                     //*/
                }
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //Toast.makeText(getContext(), "onCreateView", Toast.LENGTH_SHORT).show();
        View view = inflater.inflate(R.layout.fragment_ride_maps, container, false);
        searchParkingBttn = view.findViewById(R.id.ride_map_fragment_closest_parking_button);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.ride_map);
        //Toast.makeText(getContext(), "onViewCreated", Toast.LENGTH_SHORT).show();
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        searchParkingBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RideActivity.searchRadius = 1;
                RideActivity.isParkingFound = false;
                getClosestParking();
            }
        });
    }

    private void getClosestParking() {
        RideActivity.parkingRef = FirebaseDatabase.getInstance().getReference().child("ParkingLots");

        GeoFire geoFire = new GeoFire(RideActivity.parkingRef);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(RideActivity.userLocation.getLatitude(), RideActivity.userLocation.getLongitude()), RideActivity.searchRadius);

        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!RideActivity.isParkingFound){
                    RideActivity.isParkingFound = true;
                    RideActivity.parkingFoundId = key;

                    markTheFoundParkingLot();
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
                if(!RideActivity.isParkingFound){
                    RideActivity.searchRadius++;
                    getClosestParking();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void markTheFoundParkingLot() {
        RideActivity.parkingRef.child(RideActivity.parkingFoundId).child("l").addValueEventListener(RideActivity.parkingRefValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(RideActivity.isParkingRefListening) {
                    if (snapshot.exists() && snapshot.child("0").exists() && snapshot.child("1").exists()) {
                        double lat = Double.parseDouble(snapshot.child("0").getValue().toString());
                        double lon = Double.parseDouble(snapshot.child("1").getValue().toString());

                        LatLng markerLatLng = new LatLng(lat, lon);
                        if (parkingMarker != null) {
                            parkingMarker.remove();
                        }
                        parkingMarker = mMap.addMarker(new MarkerOptions().position(markerLatLng).title(RideActivity.parkingFoundId)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng, 17));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if(locationListener!=null)
        locationManager.removeUpdates(locationListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationListener!=null)
        locationManager.removeUpdates(locationListener);
    }
}