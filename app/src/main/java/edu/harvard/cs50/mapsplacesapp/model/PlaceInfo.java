package edu.harvard.cs50.mapsplacesapp.model;

import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Mykyta Andreiev
 */

public class PlaceInfo {

    private String id;
    private String name;
    private String address;
    private String phoneNumber;
    private Uri websiteUri;
    private LatLng latLng;
    private float rating;
    private String attributions;

    public PlaceInfo() {
    }

    public PlaceInfo(String id, String name, String address, String phoneNumber, Uri websiteUri, LatLng latLng, float rating, String attributions) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.websiteUri = websiteUri;
        this.latLng = latLng;
        this.rating = rating;
        this.attributions = attributions;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Uri getWebsiteUri() {
        return websiteUri;
    }

    public void setWebsiteUri(Uri websiteUri) {
        this.websiteUri = websiteUri;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "PlaceInfo{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", websiteUri=" + websiteUri +
                ", latLng=" + latLng +
                ", rating=" + rating +
                '}';
    }
}
