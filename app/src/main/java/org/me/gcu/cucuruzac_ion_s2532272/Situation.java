package org.me.gcu.cucuruzac_ion_s2532272;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

//an incident or a work item from the feed
public class Situation
{
    private String category;// Incident or roadworks
    private String id;
    private String type;
    private String subType;
    private String description;
    private String placeDescription;
    private String worksType;
    private String probability;
    private String validityStatus;
    private String sourceType;
    private String complianceOption;
    private String severity;
    private String locationType;
    private String direction;
    private boolean urgent;
    private Date startTime;
    private Date endTime;
    private Date lastUpdated;
    private Impact impact;
    private RoadLocation from;
    private RoadLocation to;

    public Situation() {
        impact = new Impact();
        from = new RoadLocation();

        to = new RoadLocation();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }


    public void setType(String type) {
        this.type = type;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }



    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {this.description = description;

        breakDownDescription();
    }

    public String getPlaceDescription() {
        return placeDescription;
    }


    public String getWorksType() {
        return worksType;
    }

    public String getProbability() {
        return probability;
    }

    public void setProbability(String probability) {
        this.probability = probability;
    }


    public String getValidityStatus() {
        return validityStatus;
    }

    public void setValidityStatus(String validityStatus) {
        this.validityStatus = validityStatus;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }


    public String getComplianceOption() {
        return complianceOption;
    }

    public void setComplianceOption(String complianceOption) {
        this.complianceOption = complianceOption;
    }

    public String getSeverity() {
        return severity;
    }


    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }


    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public boolean isUrgent() {
        return urgent;
    }


    public void setUrgent(String urgent) {this.urgent = Boolean.parseBoolean(urgent);//  in feed is true/false
    }

    public Date getStartTime() {return startTime;
    }

    public void setStartTime(String startTime) {this.startTime = parseDate(startTime);
    }




    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = parseDate(endTime);
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = parseDate(lastUpdated);
    }

    public Impact getImpact() {
        return impact;
    }

    public RoadLocation getFrom() {
        return from;
    }



    public RoadLocation getTo() {
        return to;
    }

// road from location , if no, only the first description word
    public String getRoadName() {
        if (from.getRoadName() != null)
        {
            return from.getRoadName();
        }

        if (to.getRoadName() != null)
        {
            return to.getRoadName();
        }


        if (description != null && !description.isEmpty())
        {
            return description.split(" ")[0];
        }


        return null;
    }

    private void breakDownDescription()
    {
        placeDescription = description;

        worksType = null;

        if (description == null || !"Roadworks".equals(category))
        {
            return;
        }

        if (description.contains(" - "))
        {
            String[] parts = description.split(" - ");

            worksType = parts[parts.length - 1].trim();
            StringBuilder place = new StringBuilder();
            for (int i = 0; i < parts.length - 1; i++) {if (i > 0) {place.append(" - ");}



                place.append(parts[i].trim());
            }

            placeDescription = place.toString();
        }
    }
//0 no delays, 1 delays, 2 long delays
    public int getDelayLevel() { String delays = impact.getDelaysType();
        if (delays == null) {return 0;
        }

        if (delays.toLowerCase().contains("long")) {return 2;
        }

        return 1;
    }

    // how many days will be working
    //-1 if missing data
    public long getDurationDays() {if (startTime == null || endTime == null)
    {
            return -1;
        }


        long difference = endTime.getTime() - startTime.getTime();
        return difference / (1000L * 60 * 60 * 24);
    }

    // reads the date, only the first 19 characters are needed
    private Date parseDate(String text) {
        try
        {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.UK);


            return format.parse(text.substring(0, 19));
        }

        catch (ParseException | IndexOutOfBoundsException | NullPointerException e)
        {
            Log.e("Situation", "Error parsing date: " + text);


            return null;
        }
    }




    //how the date is shown on screen
    public static String formatDate(Date date) {
        if (date == null) {return "Not specified";
        }

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.UK);

        return format.format(date);
    }

    //"MaintenanceWorks" becomes "Maintenance works"
    public static String makeReadable(String text) {
        if (text == null || text.isEmpty()) {return "Not specified";
        }

        StringBuilder result = new StringBuilder();


        for (int i = 0; i < text.length(); i++) {char c = text.charAt(i);

            if (i > 0 && Character.isUpperCase(c))
            {
                result.append(' ');
                result.append(Character.toLowerCase(c));
            }
            else {result.append(c);}
        }

        result.setCharAt(0, Character.toUpperCase(result.charAt(0)));

        return result.toString();
    }
// for LOg.d
    @Override
    public String toString() {
        return "Situation{" +
                "category='" + category + '\'' +
                ", id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", subType='" + subType + '\'' +
                ", road='" + getRoadName() + '\'' +
                ", description='" + description + '\'' +
                ", place='" + placeDescription + '\'' +
                ", works='" + worksType + '\'' +
                ", start='" + formatDate(startTime) + '\'' +
                ", end='" + formatDate(endTime) + '\'' +
                ", durationDays=" + getDurationDays() +
                ", lastUpdated='" + formatDate(lastUpdated) + '\'' +
                ", delayLevel=" + getDelayLevel() +
                ", urgent=" + urgent +
                ", probability='" + probability + '\'' +
                ", severity='" + severity + '\'' +
                ", validity='" + validityStatus + '\'' +
                ", source='" + sourceType + '\'' +
                ", compliance='" + complianceOption + '\'' +
                ", direction='" + direction + '\'' +
                ", locationType='" + locationType + '\'' +
                ", " + impact +
                ", from=" + from +
                ", to=" + to +
                '}';
    }
}