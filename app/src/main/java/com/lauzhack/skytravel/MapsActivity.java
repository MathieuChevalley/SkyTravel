package com.lauzhack.skytravel;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.lauzhack.skytravel.utils.API;
import com.lauzhack.skytravel.utils.Airport;
import com.lauzhack.skytravel.utils.Departure;
import com.lauzhack.skytravel.utils.Flight;
import com.lauzhack.skytravel.utils.ServerResponse;
import com.lauzhack.skytravel.utils.Suggestions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
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
    private List<Integer> priceHistory = new ArrayList<>();

    private ArrayList<Flight> flights = new ArrayList<>();

    private Retrofit retrofit;


    private String firstDeparture;
    private String dateDeparture;

    private SharedPreferences sharedPreferences;

    private Button buttonReservation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = this.getIntent().getExtras();
        firstDeparture = extras.getString("EXTRA_ITEM");
        dateDeparture = extras.getString("EXTRA_DATE");
        setContentView(R.layout.activity_maps);
        buttonReservation = (Button) findViewById(R.id.buttonReservation);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                // open settings
                startActivity(new Intent(this, Settings.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor)
                .readTimeout(20, TimeUnit.SECONDS)
                .connectTimeout(20, TimeUnit.SECONDS).build();

        retrofit = new Retrofit.Builder().baseUrl("https://skytravel-server.herokuapp.com")
                //.client(client)
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


        String duration = sharedPreferences.getString("length", "120");
        String maxPrice = sharedPreferences.getString("price", "1000");
        Log.i("Query", departure + dateDeparture + duration + maxPrice);


        Call<ServerResponse> apiCall = api.getSuggestions(departure, dateDeparture, duration, maxPrice);

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
        LatLngBounds.Builder bounds = LatLngBounds.builder();
        for (int i = 0; i < nextAirports.size(); i++) {
            Suggestions airport = nextAirports.get(i);
            String[] latlng = airport.getLocation().split(",");
            Log.i("addMarker", latlng[1] + "," + latlng[0]);
            LatLng location = new LatLng(Double.parseDouble(latlng[1]), Double.parseDouble(latlng[0]));
            mMap.addMarker(new MarkerOptions().position(location).title(airport.getName())).setTag(i);
            bounds.include(location);

            mMap.addPolyline(new PolylineOptions().add(departure, location).width(4f)
            .geodesic(true));

        }

        CameraUpdate updateFactory = CameraUpdateFactory.newLatLngBounds(bounds.build(), 16);
        mMap.animateCamera(updateFactory);

        for (int i = 0; i < visitedAirports.size() - 1; i++) {
            Departure fromAirport = visitedAirports.get(i);
            Departure toAirport = visitedAirports.get(i + 1);

            String[] latlongFrom = fromAirport.getLocation().split(",");
            String[] latlongTo = toAirport.getLocation().split(",");

            LatLng from = new LatLng(Double.parseDouble(latlongFrom[1]), Double.parseDouble(latlongFrom[0]));
            LatLng to = new LatLng(Double.parseDouble(latlongTo[1]), Double.parseDouble(latlongTo[0]));


            mMap.addPolyline(new PolylineOptions().add(from, to).width(4f).color(Color.RED)
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
        final Suggestions destinationToQuery = nextAirports.get((int) marker.getTag());
        Log.i("destination To query", destinationToQuery.toString());
        API api = retrofit.create(API.class);
                String maxPrice = sharedPreferences.getString("price", "500");
        String duration = sharedPreferences.getString("length", "120");

        String destination = destinationToQuery.getId();
        String origin = current.getId();

        Log.i("current To query", current.toString());

        Call<List<Flight>> apiCall = api.getFlights(maxPrice, duration, origin, destination, dateDeparture);

        apiCall.enqueue(new Callback<List<Flight>>() {

            @Override
            public void onResponse(Call<List<Flight>> call, Response<List<Flight>> response) {
                Log.i("show flight", "on");

                if(response.body() != null) {
                    showFlights(response.body());
                }
                current = new Departure(destinationToQuery.getName(), destinationToQuery.getCityId(),
                        destinationToQuery.getCountryId(), destinationToQuery.getLocation(), destinationToQuery.getId());
                updatePointsToDisplay();

            }

            @Override
            public void onFailure(Call<List<Flight>> call, Throwable t) {
                Log.e("failure", "query failure " + t.getMessage() );
            }
        });



        return true;
    }

    public void showFlights(final List<Flight> proposed) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Choose a flight");
        String[] proposedFlights = new String[proposed.size()];

        for (int i = 0; i < proposed.size(); i++) {
            proposedFlights[i] = proposed.get(i).getCarrier() + " " + proposed.get(i).getPrice() + " " + proposed.get(i).getDepartureTime().split("T")[1];
        }

        alertDialogBuilder.setItems(proposedFlights, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                flights.add(proposed.get(which));
                priceHistory.add(totalPrice);
                totalPrice += Double.parseDouble(proposed.get(which).getPrice());
                if(buttonReservation.getVisibility() == View.INVISIBLE){
                    buttonReservation.setVisibility(View.VISIBLE);
                }
                buttonReservation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    Intent intent = new Intent(MapsActivity.this, BuyActivity.class);
                    intent.putExtra("flights", flights);
                    startActivity(intent);
                    }
                });

            }

        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    public void onBackClicked() {
        current = visitedAirports.remove(visitedAirports.size() - 1);
        totalPrice = priceHistory.remove(priceHistory.size() - 1);
        updatePointsToDisplay();
    }
}
