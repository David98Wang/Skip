package com.w.david.skip.objects;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

/**
 * Created by whcda on 2/9/2018.
 */
@IgnoreExtraProperties
public class Address implements Serializable{
    public int streetNumber = 0;
    public String streetName = "";
    public String cityName = "";
    public String stateName = "";
    public String countryName = "";
    public String postalCode = "";  

    public Address() {

    }

    public Address(int streetNumber, String streetName, String cityName, String stateName, String countryName, String postalCode) {
        this.streetNumber = streetNumber;
        this.streetName = streetName;
        this.cityName = cityName;
        this.stateName = stateName;
        this.countryName = countryName;
        this.postalCode = postalCode;
    }

    public String getLocationString() {
        return streetNumber + " " + streetName + ", " + cityName + ", " + stateName + ", " + countryName;
    }

    public int getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(int streetNumber) {
        this.streetNumber = streetNumber;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
}
