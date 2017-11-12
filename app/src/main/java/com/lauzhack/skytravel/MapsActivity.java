package com.lauzhack.skytravel;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.ViewFlipper;

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

import java.text.ParseException;
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

    private String duration;
    private String maxPrice;
    private String departure;
    private String firstDeparture;
    private String dateDeparture;

    private SharedPreferences sharedPreferences;
    private Calendar cal;

    //previous calendar date (to block the datepicker from this date +1)
    private Button buttonBack;
    private Button buttonReservation;
    private boolean firstTimeInMethod = true;
    private API api;



    private Toolbar toolbar;
    private ListView history;

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

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        buttonBack = (Button) findViewById(R.id.buttonback);
        history = (ListView) findViewById(R.id.history);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setActionBar(toolbar);
        setTitle(sharedPreferences.getString("budget", "500"));
        toolbar.setTitleTextColor(Color.GREEN);

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
            case R.id.menu_history:
                // toggle views in view flipper
                if (history.getVisibility() == View.INVISIBLE) {
                    history.setVisibility(View.VISIBLE);
                    String[] strings =  new String[flights.size()];
                    for (int i = 0; i < flights.size(); i++) {
                        Flight f = flights.get(i);
                        strings[i] = f.getCarrier() + ", " + f.getDepartureTime() + " - " + f.getArrivalTime();
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, strings);
                    history.setAdapter(adapter);
                } else {
                    history.setVisibility(View.INVISIBLE);
                }
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
        api = retrofit.create(API.class);
        departure = "";
        if (current != null) {
            departure = current.getName();
        }
        else {
            departure = firstDeparture;
        }

        duration = sharedPreferences.getString("length", "120");
        maxPrice = sharedPreferences.getString("price", "1000");

        if(firstTimeInMethod){
            firstTimeInMethod = false;
            getAllAirportsAvailable(departure,duration,maxPrice,api);
        } else {
            //ouvrir le date
            Log.i("first time in method", firstTimeInMethod+"");
            cal = Calendar.getInstance();
            DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener(){
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth){
                    cal.set(Calendar.YEAR, year);
                    cal.set(Calendar.MONTH, monthOfYear);
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    dateDeparture = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
                    getAllAirportsAvailable(departure,duration,maxPrice,api);

                }
            };
            Calendar previousCal = Calendar.getInstance();
            Log.i("lastflightdeparturetime", flights.get(flights.size()-1).getDepartureTime());
            String[] dateArray = flights.get(flights.size()-1).getDepartureTime().split("T")[0].split("-");
            Log.i("lastflightdeparturetime", dateArray[0]);
            previousCal.set(Calendar.YEAR, Integer.parseInt(dateArray[0]));
            Log.i("lastflightdeparturetime", dateArray[1]);
            previousCal.set(Calendar.MONTH, Integer.parseInt(dateArray[1]));
            Log.i("lastflightdeparturetime", dateArray[2]);
            previousCal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateArray[2]) + 1);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date dateTest = null;
            long milli = 0;
            try {
                dateTest = sdf.parse(dateArray[0]+"-"+dateArray[1]+"-"+(Integer.parseInt(dateArray[2]) + 1)+"");
                milli = dateTest.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            DatePickerDialog dp = new DatePickerDialog(MapsActivity.this, date, previousCal.get(Calendar.YEAR), previousCal.get(Calendar.MONTH) -1, previousCal.get(Calendar.DAY_OF_MONTH));
            dp.getDatePicker().setMinDate(milli);
            dp.show();
        }

    }

    public void displayAirports() {

        if (!nextAirports.isEmpty()) {
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

            if (nextAirports.size() > 1) {
                CameraUpdate updateFactory = CameraUpdateFactory.newLatLngBounds(bounds.build(), 16);
                mMap.animateCamera(updateFactory);
            }

            for (int i = 0; i < visitedAirports.size() - 1; i++) {
                Log.i("draw red", "");
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

    }

    public void getAllAirportsAvailable(String departure, String duration, String maxPrice, API api){
        Log.i("Query update", departure + dateDeparture + duration + " " + maxPrice);

        Call<ServerResponse> apiCall = api.getSuggestions(departure, dateDeparture, duration, maxPrice);

        apiCall.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                ServerResponse serverResponse = response.body();


                current = serverResponse.getDeparture();
                visitedAirports.add(current);

                Log.i("suggestions length", ""+serverResponse.getSuggestions().size());
                nextAirports = serverResponse.getSuggestions();
                displayAirports();
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                Log.e("ServerRequest", "no suggestions " + t.getMessage());
            }
        });
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
        // move camera on click
        CameraUpdate cu = CameraUpdateFactory.newLatLng(marker.getPosition());
        mMap.animateCamera(cu);

        API api = retrofit.create(API.class);
                String maxPrice = sharedPreferences.getString("price", "500");
        String duration = sharedPreferences.getString("length", "120");

        String destination = destinationToQuery.getId();
        String origin = current.getId();

        Log.i("current To query", current.toString());

        getSuggestionsAsync(destinationToQuery, api, maxPrice, duration, destination, origin);
        return true;
    }

    private void getSuggestionsAsync(final Suggestions destinationToQuery, final API api, final String maxPrice, final String duration, final String destination, final String origin) {
        final Call<List<Flight>> apiCall = api.getFlights(maxPrice, duration, origin, destination, dateDeparture);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Charging");
        progressDialog.show();
        apiCall.enqueue(new Callback<List<Flight>>() {

            @Override
            public void onResponse(Call<List<Flight>> call, Response<List<Flight>> response) {
                Log.i("show flight", "on");
                Departure lastCurrent = current;
                if(response.body() != null) {
                    showFlights(response.body());
                }
                if (lastCurrent == current && current != null) {
                    current = new Departure(destinationToQuery.getName(), destinationToQuery.getCityId(),
                            destinationToQuery.getCountryId(), destinationToQuery.getLocation(), destinationToQuery.getId());
                    progressDialog.dismiss();
                }
                visitedAirports.add(lastCurrent);

            }

            @Override
            public void onFailure(Call<List<Flight>> call, Throwable t) {
                Log.e("failure", "query failure " + t.getMessage() );
                progressDialog.dismiss();
                //getSuggestionsAsync(destinationToQuery, api, maxPrice, duration, destination, origin);
            }
        });
    }

    public void showFlights(final List<Flight> proposed) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        if (proposed.isEmpty()) {
            alertDialogBuilder.setTitle("Sorry, no flights are availble from this point");
        } else {
            alertDialogBuilder.setTitle("Choose a flight");
        }
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
                dateDeparture = proposed.get(which).getArrivalTime();
                if (buttonBack.getVisibility() == View.INVISIBLE) {
                    buttonBack.setVisibility(View.VISIBLE);
                }
                buttonBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackClicked();
                    }
                });
                double cost = Integer.parseInt(sharedPreferences.getString("budget", "0")) - totalPrice;
                setTitle(Double.toString(cost));
                if (cost < 0) {
                    toolbar.setTitleTextColor(Color.RED);
                } else {
                    toolbar.setTitleTextColor(Color.GREEN);
                }

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
                updatePointsToDisplay();


            }

        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    public void onBackClicked() {
        visitedAirports.remove(visitedAirports.size() - 1);
        if(visitedAirports.isEmpty()) {
            current = null;
        }
        else {
            current = visitedAirports.get(visitedAirports.size() - 1);
        }
        totalPrice = priceHistory.remove(priceHistory.size() - 1);
        flights.remove(flights.size() - 1);
        if (visitedAirports.isEmpty()) {
            buttonBack.setVisibility(View.INVISIBLE);
        }
        firstTimeInMethod = true;
        Log.i("go Back", current.toString());
        updatePointsToDisplay();
    }
}
