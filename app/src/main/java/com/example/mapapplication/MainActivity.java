package com.example.mapapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // ─── UI ───────────────────────────────────────────────────────────────────
    private Button btnMap;
    private TextView tvStatus, tvLat, tvLng, tvAlt, tvAccuracy, tvServer;

    // ─── Localisation ─────────────────────────────────────────────────────────
    private double latitude, longitude, altitude;
    private float  accuracy;
    private LocationManager locationManager;

    // ─── Réseau ───────────────────────────────────────────────────────────────
    private RequestQueue requestQueue;
    private static final String INSERT_URL = "http://10.0.2.2/map_project/createPosition.php";

    // ─── Permissions ──────────────────────────────────────────────────────────
    private static final int PERMISSION_REQUEST_CODE = 100;

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();

        requestQueue   = Volley.newRequestQueue(getApplicationContext());
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        btnMap.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, MapActivity.class)));

        checkAndRequestPermissions();
    }

    // ─── Binding ──────────────────────────────────────────────────────────────
    private void bindViews() {
        btnMap      = findViewById(R.id.btnMap);
        tvStatus    = findViewById(R.id.tvStatus);
        tvLat       = findViewById(R.id.tvLat);
        tvLng       = findViewById(R.id.tvLng);
        tvAlt       = findViewById(R.id.tvAlt);
        tvAccuracy  = findViewById(R.id.tvAccuracy);
        tvServer    = findViewById(R.id.tvServer);
    }

    // ─── Permissions ──────────────────────────────────────────────────────────
    private void checkAndRequestPermissions() {
        boolean fineOk  = checkPerm(Manifest.permission.ACCESS_FINE_LOCATION);
        boolean coarseOk= checkPerm(Manifest.permission.ACCESS_COARSE_LOCATION);

        if (fineOk && coarseOk) {
            startLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    }, PERMISSION_REQUEST_CODE);
        }
    }

    private boolean checkPerm(String perm) {
        return ActivityCompat.checkSelfPermission(this, perm)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this,
                        getString(R.string.permission_denied),
                        Toast.LENGTH_LONG).show();
                tvStatus.setText("⚠ Permission refusée");
            }
        }
    }

    // ─── Localisation ─────────────────────────────────────────────────────────
    private void startLocationUpdates() {
        if (!checkPerm(Manifest.permission.ACCESS_FINE_LOCATION)
                && !checkPerm(Manifest.permission.ACCESS_COARSE_LOCATION)) return;

        tvStatus.setText("🛰 GPS actif — en attente de signal…");

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                60_000,   // 1 minute
                150,      // 150 mètres
                locationListener);
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location loc) {
            latitude  = loc.getLatitude();
            longitude = loc.getLongitude();
            altitude  = loc.getAltitude();
            accuracy  = loc.getAccuracy();

            // Mise à jour UI
            tvStatus.setText("✅ Position reçue");
            tvLat.setText(String.format(Locale.getDefault(), "%.6f°", latitude));
            tvLng.setText(String.format(Locale.getDefault(), "%.6f°", longitude));
            tvAlt.setText(String.format(Locale.getDefault(), "%.1f m", altitude));
            tvAccuracy.setText(String.format(Locale.getDefault(), "± %.1f m", accuracy));

            tvServer.setText("⏳ Envoi au serveur…");
            sendPosition(latitude, longitude);
        }

        @Override public void onProviderEnabled(@NonNull String provider)  {}
        @Override public void onProviderDisabled(@NonNull String provider) {
            tvStatus.setText("⚠ GPS désactivé");
        }
        @Override public void onStatusChanged(String p, int s, Bundle e)   {}
    };

    // ─── Réseau ───────────────────────────────────────────────────────────────
    private void sendPosition(final double lat, final double lon) {
        StringRequest req = new StringRequest(
                Request.Method.POST,
                INSERT_URL,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        boolean ok = json.optBoolean("success", false);
                        tvServer.setText(ok
                                ? "✅ " + json.optString("message", "Enregistré")
                                : "❌ " + json.optString("message", "Erreur serveur"));
                    } catch (JSONException e) {
                        tvServer.setText("✅ Position envoyée");
                    }
                },
                error -> tvServer.setText("❌ Erreur réseau : " + error.getMessage())
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String androidId = Settings.Secure.getString(
                        getContentResolver(), Settings.Secure.ANDROID_ID);

                params.put("latitude",  String.valueOf(lat));
                params.put("longitude", String.valueOf(lon));
                params.put("date",      sdf.format(new Date()));
                params.put("imei",      androidId);
                return params;
            }
        };
        requestQueue.add(req);
    }

    // ─── Cycle de vie ─────────────────────────────────────────────────────────
    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
    }
}
