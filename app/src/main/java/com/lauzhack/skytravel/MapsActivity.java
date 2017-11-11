package com.lauzhack.skytravel;

import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.lauzhack.skytravel.utils.Airport;
import com.lauzhack.skytravel.utils.Departure;
import com.lauzhack.skytravel.utils.Suggestions;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        SharedPreferences.OnSharedPreferenceChangeListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private List<Suggestions> nextAirports;
    private Departure current;
    private List<Departure> visitedAirports;
    private int totalPrice = 0;



    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMarkerClickListener(this);
    }

    public void updatePointsToDisplay() {

    }

    public void displayAirports() {

        mMap.clear();

        String[] latlongDeparture = current.getLocation().split(",");
        LatLng departure = new LatLng(Double.parseDouble(latlongDeparture[0]),
                Double.parseDouble(latlongDeparture[1]));
        for (int i = 0; i < nextAirports.size(); i++) {
            Suggestions airport = nextAirports.get(i);
            String[] latlng = airport.getLocation().split(",");
            LatLng location = new LatLng(Double.parseDouble(latlng[0]), Double.parseDouble(latlng[1]));
            mMap.addMarker(new MarkerOptions().position(location).title(airport.getName())).setTag(i);

            mMap.addPolyline(new PolylineOptions().add(departure, location)
            .geodesic(true));

        }

        for (int i = 0; i < visitedAirports.size() - 1; i++) {
            Departure fromAirport = visitedAirports.get(i);
            Departure toAirport = visitedAirports.get(i + 1);

            String[] latlongFrom = fromAirport.getLocation().split(",");
            String[] latlongTo = toAirport.getLocation().split(",");

            LatLng from = new LatLng(Double.parseDouble(latlongFrom[0]), Double.parseDouble(latlongFrom[1]));
            LatLng to = new LatLng(Double.parseDouble(latlongTo[0]), Double.parseDouble(latlongTo[1]));


            mMap.addPolyline(new PolylineOptions().add(from, to)
                    .geodesic(true));
        }


    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        this.sharedPreferences = sharedPreferences;
        updatePointsToDisplay();
        displayAirports();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Suggestions destinationToQuery = nextAirports.get((int) marker.getTag());

        return false;
    }

    public void showFlights() {

    }
}
