package org.me.gcu.cucuruzac_ion_s2532272;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;

// fragment that shows all the situations in a list
public class SituationListFragment extends Fragment implements AdapterView.OnItemClickListener
{
    private ListView listView;
    private ArrayList<Situation> situations = new ArrayList<Situation>();
    private SituationListListener listener;

    // tells the main activity which one was clicked
    public interface SituationListListener
    {
        void onSituationSelected(Situation situation);
    }

    public SituationListFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_list, container, false);
        listView = (ListView) v.findViewById(R.id.situationList);
        listView.setOnItemClickListener(this);
        showList();
        return v;
    }

    // main activity calls this when the view model has new data
    public void setSituations(ArrayList<Situation> list)
    {
        situations = list;

        showList();
    }

    private void showList()
    {
        // if the view is not  made,then onCreateView does it
        if(listView == null){return;}


        // new adapter every time the list changes
        SituationAdapter adapter = new SituationAdapter(getContext(), situations);


        listView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Situation s = situations.get(position);
        Log.d("MyTag", "Clicked " + s.getId());

        listener.onSituationSelected(s);
    } // notify the activity and puts in viewmodel

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof SituationListListener)
        {
            listener = (SituationListListener) context;
        }
        else
        {
            throw new RuntimeException(context.toString()
                    + " must implement SituationListListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        listener = null;
    }
}
