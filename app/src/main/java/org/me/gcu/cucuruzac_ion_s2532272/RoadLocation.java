package org.me.gcu.cucuruzac_ion_s2532272;

import android.util.Log;

public class RoadLocation
{
    private double latitude;
    private double longitude;
    private String roadName;
    private String pointName;

    public RoadLocation() {
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        try
        {
            this.latitude = Double.parseDouble(latitude);
        } catch (NumberFormatException e)
        {
            Log.e("RoadLocation","Error parsing latitude");
        }
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude)
    {
        try
        {
            this.longitude = Double.parseDouble(longitude);
        } catch (NumberFormatException e)

        {
            Log.e("RoadLocation","Error parsing longitude");
        }
    }

    public String getRoadName() {
        return roadName;
    }

    public void setRoadName(String roadName) {
        this.roadName = roadName;
    }

    public String getPointName() {
        return pointName;
    }

    public void setPointName(String pointName) {
        this.pointName = pointName;
    }


    public boolean hasCoordinates() {
        return latitude != 0 && longitude != 0;
    }



    @Override
    public String toString() {
        return "RoadLocation{" +
                "roadName='" + roadName + '\'' +
                ", pointName='" + pointName + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
