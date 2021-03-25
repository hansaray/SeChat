package com.simpla.sechat.Extensions;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.simpla.sechat.R;

public abstract class RunTimePermissions extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public void askPermission(final String[] requiredPermissions, final int requestCode) {

        int checkPermission = PackageManager.PERMISSION_GRANTED;
        boolean showExcuse = false;

        for (String permission : requiredPermissions) {
            checkPermission = checkPermission + ContextCompat.checkSelfPermission(this, permission);
            showExcuse = showExcuse || ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
        }

        if (checkPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(RunTimePermissions.this, requiredPermissions, requestCode);
        } else permissionGranted(requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        int checkPermission = PackageManager.PERMISSION_GRANTED;

        //if checkPermission=0, all the permissions are granted
        for (int permissionStatus : grantResults) {
            checkPermission = checkPermission + permissionStatus;
        }

        if ((grantResults.length > 0) && checkPermission == PackageManager.PERMISSION_GRANTED) {
            permissionGranted(requestCode);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.permission));
            builder.setMessage(getResources().getString(R.string.permission_exp));
            builder.setNegativeButton(getResources().getString(R.string.no), (dialog, which) -> dialog.cancel());
            builder.setPositiveButton(getResources().getString(R.string.yes), (dialog, which) -> {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            });
            builder.show();
        }
    }
    public abstract void permissionGranted(int requestCode);
}
