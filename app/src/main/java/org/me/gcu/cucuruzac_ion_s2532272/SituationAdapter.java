package org.me.gcu.cucuruzac_ion_s2532272;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

// custom adapter
public class SituationAdapter extends BaseAdapter
{
    Context context;
    ArrayList<Situation> situations;
    LayoutInflater inflter;

    public SituationAdapter(Context applicationContext, ArrayList<Situation> list) {
        this.context = applicationContext;
        this.situations = list;

        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() { return situations.size(); }

    @Override
    public Object getItem(int i) {
        return situations.get(i); }

    @Override
    public long getItemId(int i) { return i; }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
    {
        WidgetsHolder holder = null;
        if(view == null)
        { //first time, make the row and find the widgets
            view = inflter.inflate(R.layout.list_item, null);

            holder = new WidgetsHolder();
            holder.type = (TextView) view.findViewById(R.id.itemType);
            holder.description = (TextView) view.findViewById(R.id.itemDescription);
            holder.icon = (ImageView) view.findViewById(R.id.icon);
            holder.duration = (TextView) view.findViewById(R.id.itemDuration);
            holder.bar = (View) view.findViewById(R.id.colourBar);

            view.setTag(holder);
        }

        else holder = (WidgetsHolder)view.getTag();

        Situation s = situations.get(i);

        holder.type.setText(s.getCategory() + " - " + Situation.makeReadable(s.getType()));
        holder.description.setText(s.getDescription());
        holder.icon.setImageResource(getIcon(s));

        // roadworks get a colour for how long they last
        if("Roadworks".equals(s.getCategory()) && s.getDurationDays() >= 0)
        {
            long days = s.getDurationDays();
            int colour;
            String text;
            //short= 7 days, medium= 30,  long= more
            if(days <= 7)
            {
                colour = R.color.ForestGreen;
                text = "Short roadworks: ";
            }

            else if(days <= 30)
            {
                colour = R.color.DarkOrange;
                text = "Medium roadworks: "; }
            else
            {
                colour = R.color.FireBrick;
                text = "Long roadworks: ";
            }
            if(days == 0){
                text += "less than 1 day";}
            else {
                text += days + " days"; }

            holder.duration.setText(text);
            holder.duration.setTextColor(context.getResources().getColor(colour));
            holder.duration.setVisibility(View.VISIBLE);


            holder.bar.setBackgroundColor(context.getResources().getColor(colour));
        }
        else
        {
            // incidents dont have the colour
            holder.duration.setVisibility(View.GONE);
            holder.bar.setBackgroundColor(Color.TRANSPARENT);
        }

        return view;
    }

    // which openmoji picture goes with each type
    // static so the map can use the same pictures as the list
    public static int getIcon(Situation s)
    {
        String type = s.getType();
        if(type == null){return R.drawable.warning;}

        if(type.equals("MaintenanceWorks") || type.equals("ConstructionWorks"))
        {
            return getWorksIcon(s.getWorksType());
        }
        else if(type.equals("RoadOrCarriagewayOrLaneManagement")) {

            return R.drawable.close;
        }
        else if(type.equals("AbnormalTraffic"))
        {
            // queuing or slow traffic= lots of cars
            return R.drawable.car;
        }


        else if(type.equals("Accident"))
        {
            return R.drawable.accident;
        }
        else if(type.equals("VehicleObstruction"))
        {
            return R.drawable.car; }
        else if(type.equals("PoorEnvironmentConditions") || type.equals("EnvironmentalObstruction"))
        {
            return R.drawable.rain;
        }
        else {
            return R.drawable.warning;
        }
    }

    // roadworks descriptions end with what the works are
    private static int getWorksIcon(String works)
    {
        if(works == null) {return R.drawable.roadworks;}

        String w = works.toLowerCase();


        if(w.contains("closure")) {return R.drawable.close;
        }
        else if(w.contains("ttl") || w.contains("lights") || w.contains("stop and go") || w.contains("stop/go")) {
            // TTL = temporary traffic lights
            return R.drawable.lights;

        }



        else if(w.contains("sign") || w.contains("vms") || w.contains("speed"))
        {
            // signs, message boards  or speed limit
            return R.drawable.warning;
        }
        else if(w.contains("verge")) {
            // works on the grass next to the road
            return R.drawable.verge;
        }


        else if(w.contains("divers") || w.contains("contraflow")) {
            // "divers" so it also finds "Diverson"
            return R.drawable.diversion;
        }

        else {return R.drawable.roadworks;}
    }



    static class WidgetsHolder { //keeps the widgets so findViewById is only done once
        TextView type;
        TextView description;
        ImageView icon;
        TextView duration;
        View bar;
    }
}