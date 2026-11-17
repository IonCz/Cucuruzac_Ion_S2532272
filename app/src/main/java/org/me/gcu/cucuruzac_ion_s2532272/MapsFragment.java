package org.me.gcu.cucuruzac_ion_s2532272;

import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsFragment extends SupportMapFragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<Situation> situations = new ArrayList<Situation>();
    private Situation selected;
    private MapsListener listener;

    // tells the main activity which marker was tapped
    public interface MapsListener
    {
        void onMarkerClicked(Situation situation);
    }

    public MapsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we add the situations that are in the list.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        // + and - buttons
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // tap the box over a marker to see that situation details
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                listener.onMarkerClicked((Situation) marker.getTag());
            }});



        showOnMap();
    }
    // the activity sends what is in the list
    public void setSituations(ArrayList<Situation> list, Situation sel)
    {
        if(list != null) {situations = list;}
        selected = sel;
        // the old selected one is not in a new search
        if(selected != null && !situations.contains(selected)){selected = null;}


        showOnMap();
    }

    private void showOnMap()
    {
        // if map not ready,  onMapReady calls
        if(mMap == null){return;}

        mMap.clear();

        LatLngBounds.Builder bounds = new LatLngBounds.Builder();

        int count = 0;

        // one marker for each situation, same picture as in the list

        for(Situation s : situations)
        {
            RoadLocation from = s.getFrom();
            if(!from.hasCoordinates() || s == selected) {continue; }

            // incidents go on top
            float layer = 1;


            if("Roadworks".equals(s.getCategory())) {layer = 0; }

            LatLng start = new LatLng(from.getLatitude(), from.getLongitude());
            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(SituationAdapter.getIcon(s));
            addMarker(s, start, icon, s.getCategory() + " - " + Situation.makeReadable(s.getType()), layer);
            bounds.include(start);
            count++;

        }

        if(selected != null && selected.getFrom().hasCoordinates()) {showSelected();
        }
        else if(count > 0)
        {
            // zoom to fit all  markers
            try
            {
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 80));
            }
            catch (IllegalStateException e) {
                // map has no size , just show scotland
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(56.8, -4.2), 6));
            }
        }
    }

    // the situaytion is a blue mark
    // maps zoom on it
    private void showSelected()
    {
        RoadLocation from = selected.getFrom();
        LatLng start = new LatLng(from.getLatitude(), from.getLongitude());


        BitmapDescriptor blue = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);

        addMarker(selected, start, blue, selected.getCategory() + " - " + Situation.makeReadable(selected.getType()), 2);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 12));

    }

    // the tag remember the situation
    private void addMarker(Situation s, LatLng pos, BitmapDescriptor icon, String title, float layer)
    {
        Marker marker = mMap.addMarker(new MarkerOptions().position(pos)
                .title(title)
                .snippet(s.getDescription())
                .icon(icon)
                .zIndex(layer));
        if(marker != null){marker.setTag(s);}}

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // the map goes away with the view, onMapReady gives a new one next time
        mMap = null;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof MapsListener)
        {
            listener = (MapsListener) context;
        }
        else
        {
            throw new RuntimeException(context.toString()
                    + " must implement MapsListener");


        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        listener = null;

    }
}