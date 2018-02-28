package com.w.david.skip.objects;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.net.URL;

/**
 * Created by whcda on 2/9/2018.
 */
@IgnoreExtraProperties
public class Resort implements Serializable{
    public String name="";
    public Address address;
    public String url;
    public Resort()
    {

    }
    public Resort (String name, Address address, String url)
    {
        this.name = name;
        this.address = address;
        this.url= url;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address Address) {
        this.address = Address;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
