package com.lauzhack.skytravel.utils;


import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by math on 11.11.2017.
 */

public interface API {


    @GET("/airports/search/{name}")
    Call<List<Airport>> getAirports(@Path("name") String s);

    @GET("/flight/{departure}/{date}/{duration}/{maxPrice}")
    Call<ServerResponse> getSuggestions(@Path("departure") String location,
                                        @Path("date") String date,
                                        @Path("duration") String duration,
                                        @Path("maxPrice") String maxPrice);

    @GET("/session")
    Call<List<Flight>> getFlights(@Query("maxPrice") String maxPrice,
                                  @Query("maxDuration") String maxDuration,
                                  @Query("originPlace") String origin,
                                  @Query("destinationPlace") String destination,
                                  @Query("outBound") String outBound);
}
