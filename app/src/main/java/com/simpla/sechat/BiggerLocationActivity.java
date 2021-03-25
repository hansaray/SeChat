package com.simpla.sechat;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.simpla.sechat.Extensions.RunTimePermissions;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.Q)
public class BiggerLocationActivity extends RunTimePermissions implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private String locationName;
    private double latitude, longLatitude;
    private ImageView back;
    private static final int ACCESS_FINE_LOCATION_REQUEST_CODE = 100;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int PERMISSION_CALLBACK_CONSTANT = 105;
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    private SharedPreferences permissionStatus;
    private LocationCallback locationCallback;
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
        setContentView(R.layout.activity_bigger_location);
        intentInfo();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.bg_map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        permissionStatus = getSharedPreferences("permissionStatus", MODE_PRIVATE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void permissionGranted(int requestCode) {
        if (ACCESS_FINE_LOCATION_REQUEST_CODE == requestCode) {
            takeLocation();
        }
    }

    private void intentInfo() {
        Intent intent = getIntent();
        latitude = Double.parseDouble(Objects.requireNonNull(intent.getStringExtra("biggerLocationLat")));
        longLatitude = Double.parseDouble(Objects.requireNonNull(intent.getStringExtra("biggerLocationLng")));
        locationName = intent.getStringExtra("biggerLocationName");
        findIds();
    }

    private void findIds() {
        back = findViewById(R.id.bg_back);
        setupListener();
    }

    private void setupListener() {
        back.setOnClickListener(v -> onBackPressed());
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
        double myLatitude = location.getLatitude();
        double myLongLatitude = location.getLongitude();
        LatLng myLocation = new LatLng(myLatitude, myLongLatitude);
        mMap.addMarker(new MarkerOptions().position(myLocation).title("My Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation,14.90f));
        fusedLocationClient.removeLocationUpdates(locationCallback);
        showPlace();
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

    private void showPlace(){
        LatLng myLocation = new LatLng(latitude, longLatitude);
        mMap.addMarker(new MarkerOptions().position(myLocation).title(locationName));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation,14.90f));
    }

    public void checkPermission() {
        if (ActivityCompat.checkSelfPermission(BiggerLocationActivity.this, permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(BiggerLocationActivity.this, permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(BiggerLocationActivity.this, permissionsRequired[0]) || ActivityCompat.shouldShowRequestPermissionRationale(BiggerLocationActivity.this, permissionsRequired[1])) {
                boolean foreground = ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                boolean background = ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
                if (foreground || background) {
                    proceedAfterPermission();
                } else {
                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(BiggerLocationActivity.this);
                    builder.setTitle("Need App Location Permission");
                    builder.setMessage("App name need location permission to show your current location.");
                    builder.setPositiveButton("Grant", (dialog, which) -> {
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(BiggerLocationActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
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
                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(BiggerLocationActivity.this);
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
                ActivityCompat.requestPermissions(BiggerLocationActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
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
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(BiggerLocationActivity.this, permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(BiggerLocationActivity.this, permissionsRequired[1])
            ) {
                boolean foreground = ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                boolean background = ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
                if (foreground || background) {
                    proceedAfterPermission();
                } else {
                    androidx.appcompat.app.AlertDialog.Builder builder = new AlertDialog.Builder(BiggerLocationActivity.this);
                    builder.setTitle("Need App Location Permission");
                    builder.setMessage("App name need location permission to show your current location");
                    builder.setPositiveButton("Grant", (dialog, which) -> {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(BiggerLocationActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
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