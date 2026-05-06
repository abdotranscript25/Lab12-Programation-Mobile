package com.example.localisationtempsreel;

import android.os.Bundle;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private RequestQueue requestQueue;

    // Position reçue depuis MainActivity
    private double currentLat = 0;
    private double currentLon = 0;

    // ⚠️ REMPLACER PAR L'IP DE VOTRE PC
    private final String showUrl = "http://192.168.100.21/geolocalisation/showPositions.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // ✅ Récupérer la position depuis MainActivity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            currentLat = extras.getDouble("current_lat", 0);
            currentLon = extras.getDouble("current_lon", 0);
        }

        requestQueue = Volley.newRequestQueue(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // ✅ Centrer sur la position actuelle (si disponible)
        if (currentLat != 0 && currentLon != 0) {
            LatLng currentPos = new LatLng(currentLat, currentLon);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPos, 15));
            mMap.addMarker(new MarkerOptions()
                    .position(currentPos)
                    .title("📍 Votre position")
                    .snippet("Position actuelle"));
        } else {
            // Fallback : Marrakech
            LatLng defaultPos = new LatLng(31.5442, -8.7709);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultPos, 12));
            Toast.makeText(this, "Position actuelle non disponible", Toast.LENGTH_SHORT).show();
        }

        loadMarkersFromServer();
    }

    private void loadMarkersFromServer() {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                showUrl,
                null,
                response -> {
                    try {
                        JSONArray positions = response.getJSONArray("positions");
                        if (positions.length() == 0) {
                            Toast.makeText(this, "Aucune position en base", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        for (int i = 0; i < positions.length(); i++) {
                            JSONObject pos = positions.getJSONObject(i);
                            double lat = pos.getDouble("latitude");
                            double lon = pos.getDouble("longitude");
                            String date = pos.optString("date_position", "Date inconnue");

                            mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(lat, lon))
                                    .title("Position #" + (i + 1))
                                    .snippet(date));
                        }

                        Toast.makeText(this, positions.length() + " position(s) chargée(s)", Toast.LENGTH_SHORT).show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Erreur lecture données", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "❌ Erreur réseau: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );
        requestQueue.add(request);
    }
}