/*
 * written by sqall
 * Twitter: https://twitter.com/sqall01
 * Blog: https://h4des.org
 * Github: https://github.com/sqall01
 * Github Repository: https://github.com/sqall01/chasr-android-map
 *
 * This file is part of Chasr Android Logger.
 * Licensed under GPL, either version 3, or any later.
 * See <http://www.gnu.org/licenses/>
 */

package de.alertr.chasrmap;

import android.location.Location;

public class GpsLocation {

    public final static double KM_MILE = 0.621371;
    public final static double KM_NMILE = 0.5399568;

    private double lat;
    private double lon;
    private double alt;
    private double speed;
    private long utctime;

    public GpsLocation(double lat, double lon, double alt, double speed, long utctime) {
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
        this.speed = speed;
        this.utctime = utctime;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public double getAlt() {
        return alt;
    }

    public double getSpeed() {
        return speed;
    }

    public long getUtctime() {
        return utctime;
    }

    /**
     * Calculates the approximate distance to the given target location in meters.
     * @param target location
     * @return approximate distance in meter
     */
    public float distanceTo(GpsLocation target) {
        Location start = new Location("");
        start.setLatitude(lat);
        start.setLongitude(lon);
        Location end = new Location("");
        end.setLatitude(target.getLat());
        end.setLongitude(target.getLon());
        return start.distanceTo(end);
    }
}
