package org.me.gcu.cucuruzac_ion_s2532272;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;

import org.xmlpull.v1.XmlPullParserException;

import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;

import java.io.StringReader;

import java.util.ArrayList;

//PullParser
public class TrafficParser
{
    private Situation aSituation;
    private RoadLocation aLocation;
    private String nameValue;
    private boolean insideComment = false;
    private boolean insideName = false;

    public ArrayList<Situation> parse(String dataToParse, String category)
    {
        ArrayList<Situation> list = new ArrayList<Situation>(); // empty list instead of a crash
        if(dataToParse == null || dataToParse.equals(""))

        {
            Log.e("MyTag","No data for " + category);
            return list;

        } //resets


        aSituation = null;
        aLocation = null;
        nameValue = null;
        insideComment = false;
        insideName = false;


        try
        {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();

            factory.setNamespaceAware(true);

            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput( new StringReader( dataToParse ) );

            int eventType = xpp.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT)
            {
                if(eventType == XmlPullParser.START_TAG)
                {
                    if (xpp.getName().equalsIgnoreCase("situation")) // each situation is a incident or work
                    {
                        aSituation = new Situation();
                        aSituation.setCategory(category);
                        Log.d("MyTag","New situation found!");
                    }


                    else if(aSituation != null)
                    {
                        startTag(xpp);
                    }

                }


                else if(eventType == XmlPullParser.END_TAG)
                {
                    if (xpp.getName().equalsIgnoreCase("situation") && aSituation != null)
                    {

                        list.add(aSituation);
                        Log.d("MyTag", aSituation.toString());
                        aSituation = null;
                    }

                    else {endTag(xpp.getName());}

                }

                eventType = xpp.next();
            }


        }
        catch (XmlPullParserException ae1)
        {
            Log.e("MyTag","Parsing error" + ae1.toString());
        }


        catch (IOException ae1)
        {
            Log.e("MyTag","IO error during parsing");
        }

        Log.d("MyTag","End of document reached, " + category + " found: " + list.size());
        return list;
    }

    //an if statement for each tag
    private void startTag(XmlPullParser xpp) throws XmlPullParserException, IOException
    {
        if (xpp.getName().equalsIgnoreCase("situationRecord"))
        {
            // type is in the attribute xsi:type
            String temp = xpp.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type");
            aSituation.setType(temp);

            Log.d("MyTag","Situation type: " + temp);
        }

        else if (xpp.getName().equalsIgnoreCase("situationRecordCreationReference"))
        {
            String temp = xpp.nextText();
            aSituation.setId(temp);
        }


        else if (xpp.getName().equalsIgnoreCase("situationRecordVersionTime"))
        {
            String temp = xpp.nextText();
            aSituation.setLastUpdated(temp);
        }

        else if (xpp.getName().equalsIgnoreCase("probabilityOfOccurrence"))
        {
            String temp = xpp.nextText();
            aSituation.setProbability(temp);
        }



        else if (xpp.getName().equalsIgnoreCase("severity"))
        {
            String temp = xpp.nextText();
            aSituation.setSeverity(temp);
        }

        else if (xpp.getName().equalsIgnoreCase("sourceType"))
        {
            String temp = xpp.nextText();
            aSituation.setSourceType(temp);
        }
        else if (xpp.getName().equalsIgnoreCase("validityStatus"))
        {
            String temp = xpp.nextText();
            aSituation.setValidityStatus(temp);
        }



        else if (xpp.getName().equalsIgnoreCase("overallStartTime"))
        {
            String temp = xpp.nextText();
            aSituation.setStartTime(temp);
            Log.d("MyTag","Start time: " + temp);
        }

        else if (xpp.getName().equalsIgnoreCase("overallEndTime"))
        {
            String temp = xpp.nextText();
            aSituation.setEndTime(temp);
            Log.d("MyTag","End time: " + temp);
        }


        else if (xpp.getName().equalsIgnoreCase("numberOfOperationalLanes"))
        {
            String temp = xpp.nextText();
            aSituation.getImpact().setOperationalLanes(temp);
        }

        else if (xpp.getName().equalsIgnoreCase("numberOfLanesRestricted"))
        {
            String temp = xpp.nextText();
            aSituation.getImpact().setLanesRestricted(temp);
        }



        else if (xpp.getName().equalsIgnoreCase("trafficConstrictionType"))
        {
            String temp = xpp.nextText();
            aSituation.getImpact().setConstrictionType(temp);
        }



        else if (xpp.getName().equalsIgnoreCase("delaysType"))
        {
            String temp = xpp.nextText();
            aSituation.getImpact().setDelaysType(temp);
            Log.d("MyTag","Delays: " + temp);
        }

        else if (xpp.getName().equalsIgnoreCase("delayTimeValue"))
        {
            String temp = xpp.nextText();
            aSituation.getImpact().setDelayTime(temp);
        }


        else if (xpp.getName().equalsIgnoreCase("urgentRoadworks"))
        {
            String temp = xpp.nextText();
            aSituation.setUrgent(temp);
        }

        else if (xpp.getName().equalsIgnoreCase("complianceOption"))
        {
            String temp = xpp.nextText();
            aSituation.setComplianceOption(temp);
        }



        //the description is in a value within generalPublicComment
        else if (xpp.getName().equalsIgnoreCase("generalPublicComment"))
        {
            insideComment = true;
        }

        else if (xpp.getName().equalsIgnoreCase("tpegDirection"))
        {
            String temp = xpp.nextText();
            aSituation.setDirection(temp);
        }



        else if (xpp.getName().equalsIgnoreCase("tpegLinearLocationType"))
        {
            String temp = xpp.nextText();
            aSituation.setLocationType(temp);
        }

        else if (xpp.getName().equalsIgnoreCase("from") || xpp.getName().equalsIgnoreCase("point"))
        {
            // goes into from
            aLocation = aSituation.getFrom();
        }

        else if (xpp.getName().equalsIgnoreCase("to"))
        {
            aLocation = aSituation.getTo();
        }


        else if (xpp.getName().equalsIgnoreCase("latitude"))
        {
            String temp = xpp.nextText();

            if(aLocation != null){aLocation.setLatitude(temp);}
        }


        else if (xpp.getName().equalsIgnoreCase("longitude"))
        {
            String temp = xpp.nextText();
            if(aLocation != null){aLocation.setLongitude(temp);}


        }

        //road or place is in a value
        else if (xpp.getName().equalsIgnoreCase("name")
                || xpp.getName().equalsIgnoreCase("ilc")
                || xpp.getName().equalsIgnoreCase("otherName"))
        {
            insideName = true;
        }
        else if (xpp.getName().equalsIgnoreCase("value"))
        {
            String temp = xpp.nextText();
            if(insideComment){
                aSituation.setDescription(temp);
                Log.d("MyTag","Description: " + temp);
            }
            else if(insideName){
                nameValue = temp;
            }


        }



        //tells if  its road or place
        else if (xpp.getName().equalsIgnoreCase("tpegOtherPointDescriptorType")
                || xpp.getName().equalsIgnoreCase("tpegJunctionPointDescriptorType")
                || xpp.getName().equalsIgnoreCase("tpegIlcPointDescriptorType"))
        {
            String temp = xpp.nextText();
            if(aLocation != null && nameValue != null)
            {
                if(temp.equalsIgnoreCase("linkName") || temp.equalsIgnoreCase("tpegIlcName1")){
                    aLocation.setRoadName(nameValue);
                    Log.d("MyTag","Road: " + nameValue);
                }


                else if(temp.equalsIgnoreCase("nonLinkedPointName")
                        || temp.equalsIgnoreCase("junctionName")
                        || temp.equalsIgnoreCase("intersectionName")){
                    aLocation.setPointName(nameValue);
                }
            }
            nameValue = null;
        }// the tag with the subtype has a different name for each type of situation
        else if (xpp.getName().equalsIgnoreCase("roadOrCarriagewayOrLaneManagementType")
                || xpp.getName().equalsIgnoreCase("abnormalTrafficType")
                || xpp.getName().equalsIgnoreCase("roadMaintenanceType")
                || xpp.getName().equalsIgnoreCase("constructionWorkType")
                || xpp.getName().equalsIgnoreCase("accidentType")
                || xpp.getName().equalsIgnoreCase("vehicleObstructionType")
                || xpp.getName().equalsIgnoreCase("obstructionType")
                || xpp.getName().equalsIgnoreCase("environmentalObstructionType")
                || xpp.getName().equalsIgnoreCase("poorEnvironmentType")
                || xpp.getName().equalsIgnoreCase("equipmentOrSystemFaultType"))
        {
            String temp = xpp.nextText();
            aSituation.setSubType(temp);
        }


        else if (xpp.getName().endsWith("Type"))
        {
            Log.d("MyTag","Tag not used yet: " + xpp.getName());
        }
    }


//closing tags stop the flags
    private void endTag(String name)
    {
        if (name.equalsIgnoreCase("generalPublicComment"))
        {
            insideComment = false;
        }

        else if (name.equalsIgnoreCase("from") || name.equalsIgnoreCase("to") || name.equalsIgnoreCase("point"))
        {
            aLocation = null;
        }

        else if (name.equalsIgnoreCase("name") || name.equalsIgnoreCase("ilc") || name.equalsIgnoreCase("otherName"))
        {
            insideName = false;
        }
    }
}
