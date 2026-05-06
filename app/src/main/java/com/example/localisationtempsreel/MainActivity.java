package com.example.localisationtempsreel;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_LOC = 100;

    private TextView tvLat, tvLon;
    private RequestQueue requestQueue;
    private LocationManager locationManager;

    // Dernière position capturée (pour l'envoyer à MapsActivity)
    private double lastLatitude = 0;
    private double lastLongitude = 0;

    // ⚠️ REMPLACER PAR L'IP DE VOTRE PC (même réseau Wi-Fi)
    private final String insertUrl = "http://192.168.100.21/geolocalisation/createPosition.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLat = findViewById(R.id.tvLat);
        tvLon = findViewById(R.id.tvLon);
        Button btnMap = findViewById(R.id.btnMap);

        requestQueue = Volley.newRequestQueue(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // ✅ MODIFICATION : Envoi de la position à MapsActivity
        btnMap.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);

            // Passer la dernière position connue
            if (lastLatitude != 0 && lastLongitude != 0) {
                intent.putExtra("current_lat", lastLatitude);
                intent.putExtra("current_lon", lastLongitude);
            }

            startActivity(intent);
        });

        askLocationPermissionAndStart();
    }

    private void askLocationPermissionAndStart() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQ_LOC);
        } else {
            startGpsUpdates();
        }
    }

    @SuppressLint("MissingPermission")
    private void startGpsUpdates() {
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                60000,   // 60 secondes
                150,     // 150 mètres
                locationListener
        );
        Toast.makeText(this, "Recherche GPS...", Toast.LENGTH_SHORT).show();
    }

    private final LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(@NonNull Location location) {
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            double alt = location.getAltitude();
            float acc = location.getAccuracy();

            // ✅ Stocker la dernière position
            lastLatitude = lat;
            lastLongitude = lon;

            tvLat.setText("Latitude: " + lat);
            tvLon.setText("Longitude: " + lon);

            String msg = String.format("📍 Nouvelle position\nLat: %.6f\nLon: %.6f\nAlt: %.1fm\nPrécision: %.1fm",
                    lat, lon, alt, acc);
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();

            addPosition(lat, lon);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            String statusText;
            switch (status) {
                case LocationProvider.AVAILABLE:
                    statusText = "Disponible";
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    statusText = "Temporairement indisponible";
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    statusText = "Hors service";
                    break;
                default:
                    statusText = "Inconnu";
            }
            Toast.makeText(MainActivity.this, provider + " : " + statusText, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
            Toast.makeText(MainActivity.this, provider + " activé", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            Toast.makeText(MainActivity.this, provider + " désactivé", Toast.LENGTH_SHORT).show();
        }
    };

    private void addPosition(final double lat, final double lon) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                insertUrl,
                response -> {
                    // Succès (optionnel : log)
                },
                (VolleyError error) -> Toast.makeText(MainActivity.this,
                        "❌ Erreur envoi: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Map<String, String> params = new HashMap<>();
                params.put("latitude", String.valueOf(lat));
                params.put("longitude", String.valueOf(lon));
                params.put("date_position", sdf.format(new Date()));
                params.put("imei", getDeviceIdentifier());
                return params;
            }
        };
        requestQueue.add(request);
    }

    private String getDeviceIdentifier() {
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (androidId != null && !androidId.trim().isEmpty()) return androidId;

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                    == PackageManager.PERMISSION_GRANTED) {
                TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                if (tm != null) {
                    String id = tm.getDeviceId();
                    if (id != null && !id.trim().isEmpty()) return id;
                }
            }
        } catch (Exception ignored) {}

        return "UNKNOWN_DEVICE_" + System.currentTimeMillis();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOC && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startGpsUpdates();
        } else {
            Toast.makeText(this, "Permission refusée", Toast.LENGTH_LONG).show();
        }
    }
}