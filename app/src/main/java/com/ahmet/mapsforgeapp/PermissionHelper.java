package com.ahmet.mapsforgeapp;
import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
/**
 * Description of PermissionHelper
 *
 * @author Ahmet TOPAK
 * @version 1.0
 * @since 1/15/2024
 */

public class PermissionHelper {

    private static final int PERMISSION_REQUEST_CODE = 1;

    private Context context;
    private Activity activity;
    private List<String> permissions;
    private int permissionIndex;

    public PermissionHelper(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
        this.permissions = new ArrayList<>();
        this.permissionIndex = 0;
    }

    public PermissionHelper addPermission(String permission) {
        permissions.add(permission);
        return this;
    }

    public void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestNextPermission();
        }
    }

    private void requestNextPermission() {
        if (permissionIndex < permissions.size()) {
            String permission = permissions.get(permissionIndex);
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{permission}, PERMISSION_REQUEST_CODE);
            } else {
                permissionIndex++;
                requestNextPermission();
            }
        } else {
            showToast("Tüm izinler başarıyla alındı.");
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionIndex++;
                requestNextPermission();
            } else {
                showPermissionExplanationDialog();
            }
        }
    }

    private void showPermissionExplanationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("İzin Gerekli");
        builder.setMessage("Uygulamanın doğru çalışabilmesi için tüm izinleri vermelisiniz.");

        builder.setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Kullanıcıyı ayarlara yönlendirme veya başka bir işlem yapabilirsiniz
                requestPermissions();

            }
        });

        builder.setNegativeButton("İptal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showToast("İzin verilmedi. Uygulama kapatılacak.");
                activity.finish();
            }
        });

        builder.show();
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
