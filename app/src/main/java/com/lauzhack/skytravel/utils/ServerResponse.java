package com.lauzhack.skytravel.utils;

import java.util.List;

/**
 * Created by math on 11.11.2017.
 */

public class ServerResponse {
    private Departure departure;

    private List<Suggestions> suggestions;

    public Departure getDeparture ()
    {
        return departure;
    }

    public void setDeparture (Departure departure)
    {
        this.departure = departure;
    }

    public List<Suggestions> getSuggestions ()
    {
        return suggestions;
    }

    public void setSuggestions (List<Suggestions> suggestions)
    {
        this.suggestions = suggestions;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [departure = "+departure+", suggestions = "+suggestions+"]";
    }
}
