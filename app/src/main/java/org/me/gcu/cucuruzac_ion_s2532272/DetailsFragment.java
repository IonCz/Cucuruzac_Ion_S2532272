package org.me.gcu.cucuruzac_ion_s2532272;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

// fragment that shows everything about the situation picked in the list
public class DetailsFragment extends Fragment
{
    private TextView title;
    private TextView description;
    private TextView whenText;
    private TextView whereText;
    private TextView impactText;
    private TextView otherText;
    private LinearLayout content;
    private Situation situation;

    public DetailsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)

    {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_details, container, false);
        title = (TextView) v.findViewById(R.id.detailsTitle);
        description = (TextView) v.findViewById(R.id.detailsDescription);
        whenText = (TextView) v.findViewById(R.id.detailsWhen);
        whereText = (TextView) v.findViewById(R.id.detailsWhere);
        impactText = (TextView) v.findViewById(R.id.detailsImpact);
        otherText = (TextView) v.findViewById(R.id.detailsOther);
        content = (LinearLayout) v.findViewById(R.id.detailsContent);
        showDetails();
        return v;
    }

    // the main activity calls  when a row is clicked
    public void showSituation(Situation s)
    {
        situation = s;
        showDetails();
    }

    private void showDetails()
    {
        // if view not made yet  onCreateView will call  again
        if(title == null)
        {
            return;}

        if(situation == null) // nothing chosen
        {
            title.setText(R.string.select_situation);
            content.setVisibility(View.GONE);
            return;
        }

        title.setText(situation.getCategory() + " - " + Situation.makeReadable(situation.getType()));
        content.setVisibility(View.VISIBLE);

        // description, for roadworks also the place and works split up
        String text = situation.getDescription();
        if(situation.getWorksType() != null)
        {
            text += "\n\nPlace: " + situation.getPlaceDescription() + "\nWorks: " + situation.getWorksType();
        }

        description.setText(text);

        StringBuilder when = new StringBuilder();

        addLine(when, "Start", Situation.formatDate(situation.getStartTime()));
        addLine(when, "End", Situation.formatDate(situation.getEndTime()));

        if(situation.getDurationDays() >= 0)
        {
            addLine(when, "Duration", situation.getDurationDays() + " days");
        }
        addLine(when, "Last updated", Situation.formatDate(situation.getLastUpdated()));

        whenText.setText(when.toString().trim());

        StringBuilder where = new StringBuilder();

        addLine(where, "Road", situation.getRoadName());
        addReadable(where, "Direction", situation.getDirection());
        addLocation(where, "From", situation.getFrom());
        addLocation(where, "To", situation.getTo());

        whereText.setText(where.toString().trim());


        //lanes and delays from the <impact> side of the feed
        Impact impact = situation.getImpact();
        StringBuilder imp = new StringBuilder();

        addLine(imp, "Lanes open", impact.getOperationalLanes());
        addLine(imp, "Lanes restricted", impact.getLanesRestricted());


        addReadable(imp, "Restriction", impact.getConstrictionType());
        addReadable(imp, "Delays", impact.getDelaysType());
        if(impact.getDelayMinutes() >= 0)
        {
            addLine(imp, "Delay time", impact.getDelayMinutes() + " minutes");}
        if(imp.length() == 0)
        {
            imp.append("No impact information");
        }

        impactText.setText(imp.toString().trim());

        StringBuilder other = new StringBuilder();

        addReadable(other, "Details", situation.getSubType());
        addReadable(other, "Severity", situation.getSeverity());
        addReadable(other, "Probability", situation.getProbability());
        addReadable(other, "Status", situation.getValidityStatus());
        addReadable(other, "Source", situation.getSourceType());
        addReadable(other, "Compliance", situation.getComplianceOption());

        if("Roadworks".equals(situation.getCategory()))
        {
            addLine(other, "Urgent", situation.isUrgent() ? "Yes" : "No");
        }


        addLine(other, "ID", situation.getId());

        otherText.setText(other.toString().trim());
    }

    // add row if its a value , no null showing
    private void addLine(StringBuilder sb, String label, String value)
    {
        if(value == null || value.isEmpty()){return;}
        sb.append(label).append(": ").append(value).append("\n");
    }

    //  make "longDelays" into "Long delays"
    private void addReadable(StringBuilder sb, String label, String value)
    {
        if(value == null || value.isEmpty()){return;}
        addLine(sb, label, Situation.makeReadable(value));
    }

    private void addLocation(StringBuilder sb, String label, RoadLocation loc)
    {
        if(loc == null || !loc.hasCoordinates()){return;}
        String name = loc.getPointName();
        if(name == null){name = "";}
        sb.append(label).append(": ").append(name).append(" (").append(loc.getLatitude()).append(", ").append(loc.getLongitude()).append(")\n");
    }
}