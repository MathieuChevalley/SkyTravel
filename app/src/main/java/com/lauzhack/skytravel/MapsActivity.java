package com.lauzhack.skytravel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.lauzhack.skytravel.utils.API;
import com.lauzhack.skytravel.utils.Airport;
import com.lauzhack.skytravel.utils.Departure;
import com.lauzhack.skytravel.utils.ServerResponse;
import com.lauzhack.skytravel.utils.Suggestions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        SharedPreferences.OnSharedPreferenceChangeListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private List<Suggestions> nextAirports;
    private Departure current;
    private List<Departure> visitedAirports = new ArrayList<>();
    private int totalPrice = 0;

    private Retrofit retrofit;

    private Date currentDate = new Date();

    private String firstDeparture;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firstDeparture = this.getIntent().getStringExtra(Intent.EXTRA_TEXT);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Calendar cal = Calendar.getInstance();
        currentDate = cal.getTime();

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
        Log.i("ok", "oj");
        mMap = googleMap;


        retrofit = new Retrofit.Builder().baseUrl("https://skytravel-server.herokuapp.com")
                .addConverterFactory(GsonConverterFactory.create()).build();

        updatePointsToDisplay();

        mMap.setOnMarkerClickListener(this);

    }

    public void updatePointsToDisplay() {
        API api = retrofit.create(API.class);
        SimpleDateFormat ft =
                new SimpleDateFormat("yyyy-MM-dd");
        String departure = "";
        if (current != null) {
            departure = current.getName();
        }
        else {
            departure = firstDeparture;
        }
        String date = ft.format(currentDate);
        String duration = sharedPreferences.getString("length", "120");
        String maxPrice = sharedPreferences.getString("price", "1000");
        Log.i("Query", departure + date + duration + maxPrice);


        Call<ServerResponse> apiCall = api.getSuggestions(departure, date, duration, maxPrice);

        apiCall.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                ServerResponse serverResponse = response.body();

                if (current != null) {
                    visitedAirports.add(current);
                }
                current = serverResponse.getDeparture();

                Log.i("suggestions length", ""+serverResponse.getSuggestions().size());
                nextAirports = serverResponse.getSuggestions();
                displayAirports();
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                Log.e("ServerRequest", "no suggestions");
            }
        });
    }

    public void displayAirports() {

        mMap.clear();

        String[] latlongDeparture = current.getLocation().split(",");
        LatLng departure = new LatLng(Double.parseDouble(latlongDeparture[1]),
                Double.parseDouble(latlongDeparture[0]));
        Log.i("goingtoaddmarker", "go" + nextAirports.size());
        for (int i = 0; i < nextAirports.size(); i++) {
            Suggestions airport = nextAirports.get(i);
            String[] latlng = airport.getLocation().split(",");
            Log.i("addMarker", latlng[1] + "," + latlng[0]);
            LatLng location = new LatLng(Double.parseDouble(latlng[1]), Double.parseDouble(latlng[0]));
            mMap.addMarker(new MarkerOptions().position(location).title(airport.getName())).setTag(i);

            mMap.addPolyline(new PolylineOptions().add(departure, location)
            .geodesic(true));

        }

        for (int i = 0; i < visitedAirports.size() - 1; i++) {
            Departure fromAirport = visitedAirports.get(i);
            Departure toAirport = visitedAirports.get(i + 1);

            String[] latlongFrom = fromAirport.getLocation().split(",");
            String[] latlongTo = toAirport.getLocation().split(",");

            LatLng from = new LatLng(Double.parseDouble(latlongFrom[1]), Double.parseDouble(latlongFrom[0]));
            LatLng to = new LatLng(Double.parseDouble(latlongTo[1]), Double.parseDouble(latlongTo[0]));


            mMap.addPolyline(new PolylineOptions().add(from, to).width(0.5f)
                    .geodesic(true));
        }


    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        this.sharedPreferences = sharedPreferences;
        updatePointsToDisplay();

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Suggestions destinationToQuery = nextAirports.get((int) marker.getTag());

        return false;
    }

    public void showFlights() {

    }
}
