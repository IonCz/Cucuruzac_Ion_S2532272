package org.me.gcu.cucuruzac_ion_s2532272;

import android.util.Log;

// holds the <impact> part of a situation (lanes and delays)
public class Impact
{
    private String operationalLanes;
    private String lanesRestricted;
    private String constrictionType;
    private String delaysType;
    private int delayMinutes = -1;

    public Impact() {
    }

    public String getOperationalLanes() {
        return operationalLanes;
    }

    public void setOperationalLanes(String operationalLanes) {
        this.operationalLanes = operationalLanes;
    }

    public String getLanesRestricted() {
        return lanesRestricted;
    }

    public void setLanesRestricted(String lanesRestricted) {
        this.lanesRestricted = lanesRestricted;
    }

    public String getConstrictionType() {
        return constrictionType;
    }

    public void setConstrictionType(String constrictionType) {
        this.constrictionType = constrictionType;
    }

    public String getDelaysType() {
        return delaysType;
    }

    public void setDelaysType(String delaysType) {
        this.delaysType = delaysType;
    }

    public int getDelayMinutes() {
        return delayMinutes;
    }

    // the feed gives the delay in seconds, minutes are easier to read
    public void setDelayTime(String seconds) {
        try
        {
            delayMinutes = (int) Double.parseDouble(seconds) / 60;
        } catch (NumberFormatException e)
        {
            Log.e("Impact", "Error parsing delay time");
        }
    }

    @Override
    public String toString()
    {
        return "Impact{" +
                "operationalLanes='" + operationalLanes + '\'' +
                ", lanesRestricted='" + lanesRestricted + '\'' +
                ", constriction='" + constrictionType + '\'' +
                ", delays='" + delaysType + '\'' +
                ", delayMinutes=" + delayMinutes +
                '}';
    }
}
