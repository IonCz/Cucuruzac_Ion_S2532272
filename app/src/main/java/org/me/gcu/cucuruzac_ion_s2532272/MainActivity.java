/*  Starter project for Mobile Platform Development - 1st diet 26/27
    You should use this project as the starting point for your assignment.
    This project simply reads the data from the required source and displays the
    raw data in a TextField
*/

//
// Name                 Ion Cucuruzac
// Student ID           S2532272
// Programme of Study   BSc (Hons) Software Development
//

// UPDATE THE PACKAGE NAME to include your Student Identifier
package org.me.gcu.cucuruzac_ion_s2532272;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.HttpURLConnection;

import java.util.ArrayList;
import java.util.Base64;

// holds the fragments and provides with data from viewmodel
public class MainActivity extends AppCompatActivity implements OnClickListener, View.OnTouchListener, SituationListFragment.SituationListListener, MapsFragment.MapsListener {
    private TextView rawDataDisplay;


    private Button startButton;
    private ProgressBar progressBar;
    private View dragHandle;
    private TextView filterLabel;
    private ArrayList<Situation> allSituations;
    // true = use test files from res/raw instead of downloading
    private boolean useTestFiles = false

            ;

    private TrafficViewModel viewModel;

    private SituationListFragment listFragment;

    private DetailsFragment detailsFragment;


    private MapsFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;});



        //application bar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        setTitle(R.string.toolbar_title);



        // Set up the raw links to the graphical components
        rawDataDisplay = (TextView)findViewById(R.id.rawDataDisplay);
        startButton = (Button)findViewById(R.id.startButton);
        startButton.setOnClickListener(this);

        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        progressBar.setMax(100);

        dragHandle = findViewById(R.id.dragHandle);
        dragHandle.setOnTouchListener(this);

        filterLabel = (TextView)findViewById(R.id.filterLabel);

        // More Code goes here
        allSituations = new ArrayList<Situation>();
        viewModel = new ViewModelProvider(this).get(TrafficViewModel.class);

        // list goes in the listHolder
        listFragment = new SituationListFragment();

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();


        transaction.replace(R.id.listHolder, listFragment);
        transaction.commit();

        // details or the map go in the detailsHolder
        detailsFragment = new DetailsFragment();
        mapFragment = new MapsFragment();


        showMap(viewModel.isMapShown());
        // updates the screen if the data is viewmodel changes

        viewModel.getSituations().observe(this, new Observer<ArrayList<Situation>>() {
            @Override
            public void onChanged(ArrayList<Situation> list) {
                allSituations = list;
                showSituations();}});



        //while downloading , disables the button
        viewModel.getLoading().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                if(isLoading){
                    progressBar.setVisibility(View.VISIBLE);
                    startButton.setEnabled(false);}

                else {
                    progressBar.setVisibility(View.GONE);
                    startButton.setEnabled(true);
                }
            }});

        // fills the bar, the numbers come from the view model
        viewModel.getProgress().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer value) {
                progressBar.setProgress(value);
                if(viewModel.getLoading().getValue())
                {
                    rawDataDisplay.setText("Getting data... " + value + "%");}
            }
        });

        viewModel.getShown().observe(this, new Observer<ArrayList<Situation>>() {
            @Override
            public void onChanged(ArrayList<Situation> list) {
                listFragment.setSituations(list);
                mapFragment.setSituations(list, viewModel.getSelected().getValue());}});



        viewModel.getFilter().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String text) {
                showFilter(text);
            }
        });

        // the clicked situation is kept in the view model
        viewModel.getSelected().observe(this, new Observer<Situation>() {
            @Override
            public void onChanged(Situation situation) {
                detailsFragment.showSituation(situation);
                mapFragment.setSituations(viewModel.getShown().getValue(), situation);
            }
        });
        String clientId = "";
        String clientKey = "";
        Bundle metaData = null;
        try {
            metaData = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData;
            clientId = metaData.getString(getPackageName() + ".TS_CLIENT_ID");
            clientKey = metaData.getString(getPackageName() + ".TS_CLIENT_KEY");
        } catch (PackageManager.NameNotFoundException e) { throw new RuntimeException(e); }
        viewModel.setKeys(clientId, clientKey);

        // first downloading now , after in an hour and so on
        viewModel.startAutoUpdate(useTestFiles);
    }



    private void showSituations() {
        String text = viewModel.getStatus().getValue();
        if(allSituations.size() > 0){
            text += " - " + allSituations.size() + " situations";}
        rawDataDisplay.setText(text);
    }



    // label over the list says what is shown, orange when its a search or filtered so it stands out
    private void showFilter(String text)
    {
        if(viewModel.isFiltered())
        {
            // second line tells the user how to go back to all of them
            filterLabel.setText(text + "\n" + getString(R.string.clear_hint));
            filterLabel.setBackgroundColor(getResources().getColor(R.color.DarkOrange));
            filterLabel.setTextColor(getResources().getColor(R.color.black));
        }
        else {
            filterLabel.setText(text);
            filterLabel.setBackgroundColor(getResources().getColor(R.color.DimGray));
            filterLabel.setTextColor(getResources().getColor(R.color.white));
        }
    }



    // called from the list fragment when a row is clicked
    @Override
    public void onSituationSelected(Situation situation)
    {
        Log.d("MyTag", "Selected: " + situation.getCategory() + " " + situation.getId());
        viewModel.selectSituation(situation);
    }

    // tapped the box over a marker on the map,  show its details instead of the map
    @Override
    public void onMarkerClicked(Situation situation)
    {
        viewModel.selectSituation(situation);
        showMap(false);
    }

    // map and details take turns in the same place
    private void showMap(boolean show)
    {
        viewModel.setMapShown(show);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(show)
        {
            transaction.replace(R.id.detailsHolder, mapFragment);
        }
        else
        {
            transaction.replace(R.id.detailsHolder, detailsFragment);
        }
        transaction.commit();
    }


    // the menu from res/menu/main_menu.xml placed on the toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getItemId() == R.id.refresh)
        {
            startProgress();
        }
        else if(item.getItemId() == R.id.search) {
            showSearchDialog();
        }
        else if(item.getItemId() == R.id.map)
        {
            // opens the map, pick it again go back to the details
            showMap(!viewModel.isMapShown());
        }
        else if(item.getItemId() == R.id.incidents)
        {
            // works on all of them or on the last search
            viewModel.setType(1);
        }
        else if(item.getItemId() == R.id.roadworks)
        {
            viewModel.setType(2);
        }
        else if(item.getItemId() == R.id.legend)
        {
            showLegend();
        }
        else if(item.getItemId() == R.id.list)
        {

            // resets the filter and search and show all the situations again
            viewModel.showAll();
            Toast.makeText(this, "Showing all " + allSituations.size() + " situations", Toast.LENGTH_SHORT).show();
        }
        else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    //search dialog with layout
    private void showSearchDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_search, null);

        builder.setView(dialogView);
        builder.setTitle(R.string.menu_search);

        final EditText roadInput = (EditText) dialogView.findViewById(R.id.searchRoad);
        final Spinner placeInput = (Spinner) dialogView.findViewById(R.id.searchPlace);

        final EditText distanceInput = (EditText) dialogView.findViewById(R.id.searchDistance);

        // places come from arrays.xml
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.places, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        placeInput.setAdapter(adapter);

        // put back what was searched last time
        roadInput.setText(viewModel.getLastRoad());
        placeInput.setSelection(viewModel.getLastPlace());
        distanceInput.setText(String.valueOf(viewModel.getLastKm()));


        //buttons: search by road, search nearby, cancel
        builder.setPositiveButton(R.string.search_road_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String road = roadInput.getText().toString().trim();
                if(road.isEmpty())
                {
                    Toast.makeText(MainActivity.this, "Type a road name first", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    viewModel.searchByRoad(road);
                }
            }});



        builder.setNeutralButton(R.string.search_near_button, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                String km = distanceInput.getText().toString().trim();
                if(km.isEmpty()){
                    Toast.makeText(MainActivity.this, "Type a distance first", Toast.LENGTH_SHORT).show();}
                else {
                    viewModel.searchNear((String) placeInput.getSelectedItem(), placeInput.getSelectedItemPosition(), Double.parseDouble(km));
                } //only digits can be entered without causing a parseDouble error
            }});
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });


        AlertDialog alert = builder.create();
        alert.show();
    }

    // meaning of  the icons and colours
    private void showLegend()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(getLayoutInflater().inflate(R.layout.dialog_legend, null));


        builder.setTitle(R.string.legend_title);
        builder.setPositiveButton(R.string.ok, null);

        AlertDialog alert = builder.create();
        alert.show();
    }

    // drag the blue bar to make the list or the details bigger
    // works the same in portrait (up/down) and landscape (left/right)
    @Override
    public boolean onTouch(View v, MotionEvent ev)
    {
        boolean status = false;
        if(v == dragHandle)
        {
            status = true;
            if(ev.getAction() == MotionEvent.ACTION_MOVE)
            {
                // the 2 parts are the views  before and after the bar
                LinearLayout parent = (LinearLayout) v.getParent();
                int index = parent.indexOfChild(v);

                View before = parent.getChildAt(index - 1);
                View after = parent.getChildAt(index + 1);

                int[] start = new int[2];
                int[] end = new int[2];

                before.getLocationOnScreen(start);
                after.getLocationOnScreen(end);

                float position;
                float size;
                if(parent.getOrientation() == LinearLayout.VERTICAL)
                {
                    position = ev.getRawY() - start[1];
                    size = end[1] + after.getHeight() - start[1];
                }
                else
                {
                    position = ev.getRawX() - start[0];
                    size = end[0] + after.getWidth() - start[0];
                }


                // the bar to be dragged from 0 to 1
                float part = position / size;
                if(parent.getOrientation() == LinearLayout.VERTICAL)
                {
                    // portrait - dont let one side disappear
                    if(part < 0.15f){
                        part = 0.15f;}
                    if(part > 0.85f){
                        part = 0.85f;}
                }
                else

                {
                    // for landscape mode
                    //the list is long enough to be readable
                    //otherwise, it closes like a curtain
                    if(part < 0.20f)
                    {
                        part = 0;}
                    else if(part < 0.35f)
                    {
                        part = 0.35f;}
                    if(part > 0.65f)
                    {
                        part = 0.65f;}
                }

                // changing the proportion
                LinearLayout.LayoutParams p1 = (LinearLayout.LayoutParams) before.getLayoutParams();
                LinearLayout.LayoutParams p2 = (LinearLayout.LayoutParams) after.getLayoutParams();

                p1.weight = part;
                p2.weight = 1 - part;


                before.setLayoutParams(p1);
                after.setLayoutParams(p2);
            }
        }
        return status;
    }
    // refresh button now
    public void onClick(View aview)
    {
        startProgress();

    }

    public void startProgress()
    {
        // button with insta refresh, the auto-update is done in view model
        viewModel.startProgress();


    }//

}