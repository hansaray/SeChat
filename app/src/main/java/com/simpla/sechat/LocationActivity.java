package com.simpla.sechat;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.simpla.sechat.Adapters.LocationAdapter;
import com.simpla.sechat.Extensions.RunTimePermissions;
import com.simpla.sechat.Objects.LocationObject;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

@SuppressLint("Registered")
@RequiresApi(api = Build.VERSION_CODES.Q)
public class LocationActivity extends RunTimePermissions implements OnMapReadyCallback, LocationListener, PlaceSelectionListener {

    private GoogleMap mMap;
    private RecyclerView recyclerView;
    private double latitude;
    private double longLatitude;
    private static final int ACCESS_FINE_LOCATION_REQUEST_CODE = 100;
    private LocationAdapter adapter;
    private ArrayList<LocationObject> list;
    private String uid;
    private static final int REQUEST_SELECT_PLACE = 1000;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int PERMISSION_CALLBACK_CONSTANT = 105;
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    private LocationCallback locationCallback;
    private SharedPreferences permissionStatus;
    String[] permissionsRequired = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION
            , Manifest.permission.ACCESS_COARSE_LOCATION
            , Manifest.permission.ACCESS_BACKGROUND_LOCATION
    };
    Boolean sentToSettings = false;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        intentInfo();
        createLocationRequest();
        findIds();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.location_map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void intentInfo(){
        Intent intent = getIntent();
        uid =intent.getStringExtra("locationUid");
    }

    private void findIds() {
        ImageView back = findViewById(R.id.location_back);
        recyclerView = findViewById(R.id.location_rw);
        permissionStatus = getSharedPreferences("permissionStatus", MODE_PRIVATE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        list = new ArrayList<>();
        back.setOnClickListener(view -> onBackPressed());
        setupRw();
    }

    private void setupRw() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LocationAdapter(this, list);
        recyclerView.setAdapter(adapter);
        try {
            AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                    .build();
            Intent intent = new PlaceAutocomplete.IntentBuilder
                    (PlaceAutocomplete.MODE_FULLSCREEN)
                    .setFilter(typeFilter)
                    .build(LocationActivity.this);
            startActivityForResult(intent, REQUEST_SELECT_PLACE);
        } catch (GooglePlayServicesRepairableException |
                GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (Build.VERSION.SDK_INT >= 23) {
            checkPermission();
        } else {
            takeLocation();
        }
    }

    @SuppressLint({"ShowToast", "MissingPermission"})
    private void takeLocation() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NotNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    onLocationChanged(location);
                }
            }
        };
        fusedLocationClient.requestLocationUpdates(mLocationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longLatitude = location.getLongitude();
        LatLng myLocation = new LatLng(latitude, longLatitude);
        mMap.addMarker(new MarkerOptions().position(myLocation).title("My Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation,14.90f));
        fusedLocationClient.removeLocationUpdates(locationCallback);
        loadPlaces();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NotNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NotNull String provider) {

    }

    @Override
    public void permissionGranted(int requestCode) {
        if(ACCESS_FINE_LOCATION_REQUEST_CODE == requestCode){
            takeLocation();
        }
    }

    public void loadPlaces(){
        String key = "AIzaSyBaAwk8mNK0OK1fuc44dZUpnaiBAGlriKY";
        String radius = "500";
        final String location = latitude+","+longLatitude;
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+location+"&radius="+radius+"&key="+key;
        @SuppressLint("SetTextI18n") StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray places = jsonObject.getJSONArray("results");
                LocationObject locationObject1 = new LocationObject(getResources().getString(R.string.send_exact_location)
                            ,String.valueOf(latitude),String.valueOf(longLatitude),uid);
                list.add(locationObject1);
                adapter.notifyDataSetChanged();
                for(int i = 0; i<places.length();i++ ){
                    JSONObject p = places.getJSONObject(i);
                    String placeName = p.getString("name");
                    JSONObject geometry = p.getJSONObject("geometry");
                    JSONObject pL = geometry.getJSONObject("location");
                    String lat = pL.getString("lat");
                    String lng = pL.getString("lng");
                    LocationObject locationObject = new LocationObject(placeName,lat,lng,uid);
                    list.add(locationObject);
                    adapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {

        });
        Volley.newRequestQueue(LocationActivity.this).add(stringRequest);
    }

    @Override
    public void onPlaceSelected(@NonNull Place place) {

    }

    @Override
    public void onError(@NonNull Status status) {
        Toast.makeText(this, status.getStatusMessage(), Toast.LENGTH_SHORT).show();
    }

    public void checkPermission() {
        if (ActivityCompat.checkSelfPermission(LocationActivity.this, permissionsRequired[0])
                != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(LocationActivity.this
                , permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(LocationActivity.this, permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(LocationActivity.this, permissionsRequired[1])) {
                boolean foreground = ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                boolean background = ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
                if (foreground || background) {
                    proceedAfterPermission();
                } else {
                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(LocationActivity.this);
                    builder.setTitle("Need App Location Permission");
                    builder.setMessage("App name need location permission to show your current location.");
                    builder.setPositiveButton("Grant", (dialog, which) -> {
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(LocationActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
                    });
                    builder.setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                        finish();
                    });
                    builder.show();
                }
            } else if (permissionStatus.getBoolean(permissionsRequired[0], false) && permissionStatus.getBoolean(permissionsRequired[1], false)) {
                boolean foreground = ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                boolean background = ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
                if (foreground || background) {
                    proceedAfterPermission();
                } else {
                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(LocationActivity.this);
                    builder.setTitle("GPS Not Enabled");
                    builder.setMessage("Enable GPS from your setting");
                    builder.setPositiveButton("Ok", (dialog, which) -> {
                        dialog.cancel();
                        sentToSettings = false;
                    });
                    builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
                    builder.show();
                }
            } else {
                ActivityCompat.requestPermissions(LocationActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
            }

            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(permissionsRequired[0], true);
            editor.apply();
        } else {
            proceedAfterPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        if (requestCode == PERMISSION_CALLBACK_CONSTANT) {
            boolean allgranted = false;
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    allgranted = true;
                } else {
                    allgranted = false;
                    break;
                }
            }

            if (allgranted) {
                proceedAfterPermission();
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(LocationActivity.this, permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(LocationActivity.this, permissionsRequired[1])
            ) {
                boolean foreground = ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                boolean background = ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
                if (foreground || background) {
                    proceedAfterPermission();
                } else {
                    androidx.appcompat.app.AlertDialog.Builder builder = new AlertDialog.Builder(LocationActivity.this);
                    builder.setTitle("Need App Location Permission");
                    builder.setMessage("App name need location permission to show your current location");
                    builder.setPositiveButton("Grant", (dialog, which) -> {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(LocationActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
                    });
                    builder.setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.cancel();
                        finish();
                    });
                    builder.show();
                }
            } else {
                proceedAfterPermission();
            }
        }
    }

    private void proceedAfterPermission() {
        takeLocation();
    }

}