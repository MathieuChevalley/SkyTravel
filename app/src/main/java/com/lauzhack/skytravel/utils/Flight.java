package com.lauzhack.skytravel.utils;

/**
 * Created by math on 12.11.2017.
 */

public class Flight {
    private String duration;

    private String price;

    private String arrivalTime;

    private String ticketLink;

    private String departureTime;

    private String carrier;

    public String getDuration ()
    {
        return duration;
    }

    public void setDuration (String duration)
    {
        this.duration = duration;
    }

    public String getPrice ()
    {
        return price;
    }

    public void setPrice (String price)
    {
        this.price = price;
    }

    public String getArrivalTime ()
    {
        return arrivalTime;
    }

    public void setArrivalTime (String arrivalTime)
    {
        this.arrivalTime = arrivalTime;
    }

    public String getTicketLink ()
    {
        return ticketLink;
    }

    public void setTicketLink (String ticketLink)
    {
        this.ticketLink = ticketLink;
    }

    public String getDepartureTime ()
    {
        return departureTime;
    }

    public void setDepartureTime (String departureTime)
    {
        this.departureTime = departureTime;
    }

    public String getCarrier ()
    {
        return carrier;
    }

    public void setCarrier (String carrier)
    {
        this.carrier = carrier;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [duration = "+duration+", price = "+price+", arrivalTime = "+arrivalTime+", ticketLink = "+ticketLink+", departureTime = "+departureTime+", carrier = "+carrier+"]";
    }
}
