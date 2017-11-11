package com.lauzhack.skytravel;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.lauzhack.skytravel.utils.API;
import com.lauzhack.skytravel.utils.Airport;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private ListView mListViewResultDisplay;
    private EditText mEditTextSearchQuery;
    private ArrayAdapter mAdapter;
    private List<String> resultArray;
    private Retrofit retrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        retrofit = new Retrofit.Builder().baseUrl("https://skytravel-server.herokuapp.com")
                .addConverterFactory(GsonConverterFactory.create()).build();

        //Get text view
        mEditTextSearchQuery = (EditText) findViewById(R.id.editTextCityQuery);

        //Get List View
        mListViewResultDisplay = (ListView) findViewById(R.id.listViewResults);

        //Add change listener for search query
        mEditTextSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.toString().length() >= 3) {
                    API api = retrofit.create(API.class);
                    Call<List<Airport>> apiCall = api.getAirports(s.toString());
                    apiCall.enqueue(new Callback<List<Airport>>() {
                        @Override
                        public void onResponse(Call<List<Airport>> call, Response<List<Airport>> response) {
                            List<Airport> airports = response.body();
                            resultArray.clear();
                            for(int i = 0; i < airports.size(); i++) {
                                resultArray.add(airports.get(i).getPlaceName());
                            }
                            mAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure(Call<List<Airport>> call, Throwable t) {
                            Log.e("failure", "search failed, " + t.getMessage());
                        }
                    });

                }
                else {
                    resultArray.clear();
                    mAdapter.notifyDataSetChanged();
                }
            }
        });

        resultArray = new ArrayList<>();

        mAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, resultArray);
        mListViewResultDisplay.setAdapter(mAdapter);



        mListViewResultDisplay.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = resultArray.get(position);

                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, item);
                //based on item add info to intent
                startActivity(intent);
            }
        });


    }

}
