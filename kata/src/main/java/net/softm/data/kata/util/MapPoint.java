package net.softm.data.kata.util;


import net.softm.data.kata.common.NaviDestination;

public class MapPoint {
    long created;
    String id;
    String key;

    NaviDestination destination;
    String name;

    float lat;
    float lon;
    public MapPoint(String name, float lat, float lon) {
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.destination = NaviDestination.BASIC;
    }

    public MapPoint(NaviDestination destination,String name, float lat, float lon) {
        this.destination = destination;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
    }

    public NaviDestination getDestination() {
        return destination;
    }

    public void setDestination(NaviDestination destination) {
        this.destination = destination;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public float getLon() {
        return lon;
    }

    public void setLon(float lon) {
        this.lon = lon;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}

