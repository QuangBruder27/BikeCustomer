package com.quangbruder.bikecustomer.data.model;

public class URLs {
    public static final String authService = "8100";
    public static final String bonusService = "8200";
    public static final String rentService = "8300";
    public static final String reportService = "8400";
    public static final String locationService = "8500";


    //private static final String baseGATEWAY = "http://192.168.0.101:8762";
    private static final String baseGATEWAY = "http://178.254.24.192:8762";

    public static final String URL_REGISTER = baseGATEWAY + "/auth/register";
    public static final String URL_LOGIN= baseGATEWAY + "/auth/login";

    public static final String URL_BONUS = baseGATEWAY+"/bonus";

    public static final String URL_GET_BIKE_LOCATION = baseGATEWAY + "/rent/locatebike";
    public static final String URL_RENT_BIKE = baseGATEWAY + "/rent/create";
    public static final String URL_BOOKING_HISTORY =  baseGATEWAY + "/rent/history";
    public static final String URL_CURRENT_BOOKING =  baseGATEWAY + "/rent/now";

    public static final String URL_REPORT =  baseGATEWAY + "/report";









}