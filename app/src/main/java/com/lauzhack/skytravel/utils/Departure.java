package com.lauzhack.skytravel.utils;

/**
 * Created by math on 11.11.2017.
 */

public class Departure {
    private String Name;

    private String CityId;

    private String CountryId;

    private String Location;

    private String Id;

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public String getCityId() {
        return CityId;
    }

    public void setCityId(String CityId) {
        this.CityId = CityId;
    }

    public String getCountryId() {
        return CountryId;
    }

    public void setCountryId(String CountryId) {
        this.CountryId = CountryId;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String Location) {
        this.Location = Location;
    }

    public String getId() {
        return Id;
    }

    public void setId(String Id) {
        this.Id = Id;
    }

    @Override
    public String toString() {
        return "ClassPojo [Name = " + Name + ", CityId = " + CityId + ", CountryId = " + CountryId + ", Location = " + Location + ", Id = " + Id + "]";

    }
}
