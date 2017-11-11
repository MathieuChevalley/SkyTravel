package com.lauzhack.skytravel.utils;

/**
 * Created by math on 11.11.2017.
 */

public class Airport {

    private String CountryName;



    private String CityId;



    private String PlaceId;



    private String RegionId;



    private String CountryId;



    private String PlaceName;



    public String getCountryName ()

    {

        return CountryName;

    }



    public void setCountryName (String CountryName)

    {

        this.CountryName = CountryName;

    }



    public String getCityId ()

    {

        return CityId;

    }



    public void setCityId (String CityId)

    {

        this.CityId = CityId;

    }



    public String getPlaceId ()

    {

        return PlaceId;

    }



    public void setPlaceId (String PlaceId)

    {

        this.PlaceId = PlaceId;

    }



    public String getRegionId ()

    {

        return RegionId;

    }



    public void setRegionId (String RegionId)

    {

        this.RegionId = RegionId;

    }



    public String getCountryId ()

    {

        return CountryId;

    }



    public void setCountryId (String CountryId)

    {

        this.CountryId = CountryId;

    }



    public String getPlaceName ()

    {

        return PlaceName;

    }



    public void setPlaceName (String PlaceName)

    {

        this.PlaceName = PlaceName;

    }



    @Override

    public String toString()

    {

        return "ClassPojo [CountryName = "+CountryName+", CityId = "+CityId+", PlaceId = "+PlaceId+", RegionId = "+RegionId+", CountryId = "+CountryId+", PlaceName = "+PlaceName+"]";

    }
}


