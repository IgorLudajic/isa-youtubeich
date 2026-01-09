package com.team44.isa_youtubeich.domain.model;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class GeoLocation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Getter
    private double latitude;

    @Getter
    private double longitude;

    public GeoLocation() {
    }

    public GeoLocation(Double latitude, Double longitude) {
        setLatitude(latitude);
        setLongitude(longitude);
    }

    public void setLatitude(double latitude) throws IllegalArgumentException {
        if(latitude < -90.0 || latitude > 90.0){
            throw new IllegalArgumentException(String.format("Invalid latitude: %f", latitude));
        }
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        if(longitude < -180.0 || longitude > 180.0){
            throw new IllegalArgumentException(String.format("Invalid longitude: %f", longitude));
        }
        this.longitude = longitude;
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;

        if(o == null || getClass() != o.getClass()) return false;

        GeoLocation location = (GeoLocation)o;

        return latitude == location.getLatitude() && longitude == location.getLongitude();
    }

    @Override
    public int hashCode(){
        return Objects.hash(latitude, longitude);
    }
}
