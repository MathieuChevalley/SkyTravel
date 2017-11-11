package com.lauzhack.skytravel.utils;

/**
 * Created by math on 11.11.2017.
 */

public class ServerResponse {
    private Departure departure;

    private Suggestions[] suggestions;

    public Departure getDeparture ()
    {
        return departure;
    }

    public void setDeparture (Departure departure)
    {
        this.departure = departure;
    }

    public Suggestions[] getSuggestions ()
    {
        return suggestions;
    }

    public void setSuggestions (Suggestions[] suggestions)
    {
        this.suggestions = suggestions;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [departure = "+departure+", suggestions = "+suggestions+"]";
    }
}
