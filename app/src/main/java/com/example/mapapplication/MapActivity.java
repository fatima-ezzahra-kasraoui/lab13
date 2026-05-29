package com.example.mapapplication;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MapActivity extends AppCompatActivity {

    // ─── UI ───────────────────────────────────────────────────────────────────
    private MapView   map;
    private TextView  tvMarkerCount;
    private Button    btnRefresh;

    // ─── Réseau ───────────────────────────────────────────────────────────────
    private RequestQueue requestQueue;
    private static final String POSITIONS_URL = "http://10.0.2.2/map_project/getPosition.php";

    // ─── Coordonnées par défaut (Casablanca) ──────────────────────────────────
    private static final double DEFAULT_LAT = 33.5731;
    private static final double DEFAULT_LNG = -7.5898;

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialisation OSMDroid AVANT setContentView
        Configuration.getInstance().load(
                getApplicationContext(),
                getSharedPreferences("osm_prefs", MODE_PRIVATE));

        setContentView(R.layout.activity_map);

        // Vues
        map            = findViewById(R.id.map);
        tvMarkerCount  = findViewById(R.id.tvMarkerCount);
        btnRefresh     = findViewById(R.id.btnRefresh);

        setupMap();

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        btnRefresh.setOnClickListener(v -> {
            clearMarkers();
            loadPositions();
        });

        loadPositions();
    }

    // ─── Carte ────────────────────────────────────────────────────────────────
    private void setupMap() {
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.getController().setZoom(13.0);
        map.getController().setCenter(new GeoPoint(DEFAULT_LAT, DEFAULT_LNG));
    }

    private void clearMarkers() {
        map.getOverlays().clear();
        map.invalidate();
        tvMarkerCount.setText("Chargement…");
    }

    // ─── Chargement des positions ─────────────────────────────────────────────
    private void loadPositions() {
        tvMarkerCount.setText("⏳ Chargement des positions…");

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST,
                POSITIONS_URL,
                null,
                response -> {
                    try {
                        JSONArray positions = response.getJSONArray("positions");
                        int count = positions.length();

                        if (count == 0) {
                            tvMarkerCount.setText("Aucune position enregistrée");
                            return;
                        }

                        Drawable markerIcon = ContextCompat.getDrawable(this, R.drawable.marker);
                        GeoPoint firstPoint = null;

                        for (int i = 0; i < count; i++) {
                            JSONObject pos = positions.getJSONObject(i);
                            double lat = pos.getDouble("latitude");
                            double lng = pos.getDouble("longitude");
                            String date = pos.optString("date", "");

                            Marker marker = new Marker(map);
                            marker.setPosition(new GeoPoint(lat, lng));
                            marker.setTitle("Position " + (i + 1));
                            marker.setSnippet(date.isEmpty() ? "" : "🕐 " + date);
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

                            if (markerIcon != null) {
                                marker.setIcon(markerIcon);
                            }

                            map.getOverlays().add(marker);

                            if (i == 0) firstPoint = new GeoPoint(lat, lng);
                        }

                        // Centrer sur la première position
                        if (firstPoint != null) {
                            map.getController().animateTo(firstPoint);
                        }

                        map.invalidate();
                        tvMarkerCount.setText("📍 " + count + " position(s) affichée(s)");

                    } catch (JSONException e) {
                        e.printStackTrace();
                        tvMarkerCount.setText("❌ Erreur de parsing JSON");
                        Toast.makeText(this, "Erreur JSON : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    tvMarkerCount.setText("❌ Erreur réseau");
                    Toast.makeText(this, "Erreur réseau : " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );

        requestQueue.add(req);
    }

    // ─── Cycle de vie OSMDroid ────────────────────────────────────────────────
    @Override protected void onResume()  { super.onResume();  map.onResume(); }
    @Override protected void onPause()   { super.onPause();   map.onPause(); }
    @Override protected void onDestroy() { super.onDestroy(); map.onDetach(); }
}
