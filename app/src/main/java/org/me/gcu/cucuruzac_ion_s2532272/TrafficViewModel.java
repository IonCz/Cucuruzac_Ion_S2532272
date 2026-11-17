package org.me.gcu.cucuruzac_ion_s2532272;

import android.app.Application;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// viewmodell keeps the data and download it without losing due rotation
public class TrafficViewModel extends AndroidViewModel
{
    //DOWNLOADING FROM TRAFFIC SCOTLAND
    private String clientId = "";
    private String clientKey = "";
    final String unplannedEventsURL = "https://datex2.trafficscotland.org/rest/2.3/publications/UnplannedEvents/Content.xml";
    final String roadworksURL = "https://datex2.trafficscotland.org/rest/2.3/publications/CurrentRoadworks/Content.xml";

    // 1 hour
    private static final long UPDATE_TIME = 60 * 60 * 1000;

    private final MutableLiveData<ArrayList<Situation>> situations;
    private final MutableLiveData<String> status;
    private final MutableLiveData<Boolean> loading;
    private final MutableLiveData<Integer> progress;
    private final MutableLiveData<Situation> selected;
    private final MutableLiveData<ArrayList<Situation>> shown;
    private final MutableLiveData<String> filter;

    // places for the search near, same order as places in arrays.xml
    // one place for each part of the trunk road network , covering all Scotland
    // coordinates of each place taken from Google Maps
    private final double[] placeLat = {55.8667, 55.9533, 56.1165, 56.0719, 56.3950, 56.4620, 57.1497,
            57.4778, 56.8198, 56.4152, 55.4586, 55.0701, 55.6146};
    private final double[] placeLon = {-4.2508, -3.1883, -3.9369, -3.4393, -3.4308, -2.9707, -2.0943,
            -4.2247, -5.1052, -5.4718, -4.6292, -3.6054, -2.8070};

    // last search,
    // search box  has it next time
    private boolean searching = false;
    private String lastRoad = "";
    private int lastPlace = 0;
    private int lastKm = 20;

    // the list before the incidents or roadworks filter
    private ArrayList<Situation> base = new ArrayList<Situation>();
    private String baseText = "All situations";
    // 0=all,1 =incidents only,2=roadworks only
    private int type = 0;

    // map showing instead of the details, kept here so rotating keeps it
    private boolean mapShown = false;
//thread in the background instead of new Thread + runOnUiThread from the skeleton
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler handler = new Handler(Looper.getMainLooper());//to return to the screen thread
    private boolean useTestFiles = false;
    private boolean started = false;

    // for the progress bar
    private int target = 0;
    private Random random = new Random();
    private ArrayList<Situation> newList;
    private String newMessage;

    public TrafficViewModel(@NonNull Application application)
    {
        super(application);
        situations = new MutableLiveData<>();
        situations.setValue(new ArrayList<Situation>());

        status = new MutableLiveData<>();
        status.setValue("Getting data...");

        loading = new MutableLiveData<>();
        loading.setValue(false);

        progress = new MutableLiveData<>();
        progress.setValue(0);

        selected = new MutableLiveData<>();

        shown = new MutableLiveData<>();
        shown.setValue(new ArrayList<Situation>());

        filter = new MutableLiveData<>();
        filter.setValue("All situations");


    }

    public LiveData<ArrayList<Situation>> getSituations() { return situations; }
    public LiveData<String> getStatus() { return status; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<Integer> getProgress() { return progress; }
    public LiveData<Situation> getSelected() { return selected; }
    public LiveData<ArrayList<Situation>> getShown() { return shown; }
    public LiveData<String> getFilter() { return filter; }
    public boolean isSearching() { return searching; }
    public String getLastRoad() { return lastRoad; }
    public int getLastPlace() { return lastPlace; }
    public int getLastKm() { return lastKm; }
    public boolean isMapShown() { return mapShown; }
    // label goes orange when its a search or only incidents/roadworks are shown
    public boolean isFiltered() { return searching || type != 0; }
    public void setMapShown(boolean show) { mapShown = show; }

    public void setKeys(String id, String key) { clientId = id; clientKey = key; }

    // keeps the clicked situation here
    public void selectSituation(Situation s)
    {
        selected.setValue(s);
    }

//deletes the search and filter if was pressed show all situations in menu
    public void showAll()
    {
        searching = false;
        type = 0;

        base = situations.getValue();
        baseText = "All situations";
        applyFilter();

    }

    // show incidents only or show roadworks only in the menu
    public void setType(int t)
    {
        type = t;
        applyFilter();
    }

    // keeps only the incidents or the roadworks from the base list
    private void applyFilter()
    {

        ArrayList<Situation> results = new ArrayList<Situation>();
        for(Situation s : base)
        {
            if(type == 0
                    || (type == 1 && "Incident".equals(s.getCategory()))
                    || (type == 2 && "Roadworks".equals(s.getCategory()))) {
                results.add(s);
            }


        }

        String text = baseText;
        if(type == 1){text += " (incidents only)";}

        else if(type == 2) {text += " (roadworks only)"; }



        // the selected situation is cleared to prevent the map and details from displaying information from the previous one.
        if(selected.getValue() != null && !results.contains(selected.getValue())){selected.setValue(null);}


        shown.setValue(results);
        filter.setValue(text + ": " + results.size() + " found");
    }

    // road search
    public void searchByRoad(String road)
    {
        searching = true;
        lastRoad = road;
        ArrayList<Situation> results = new ArrayList<Situation>();
        for(Situation s : situations.getValue())
        {
            if(road.equalsIgnoreCase(s.getRoadName()))
            {
                results.add(s);
            }
            else if(s.getDescription() != null)
            {
                String[] words = s.getDescription().split(" ");
                for(String w : words) {
                    w = w.replace(",", "").replace(".", "");
                    if(w.equalsIgnoreCase(road)){
                        results.add(s);
                        break;
                    }
                }




            }
        }
        base = results;
        baseText = "Road " + road.toUpperCase();

        applyFilter();
    }

    // distance search from one of the fixed places
    public void searchNear(String placeName, int place, double km)
    {
        searching = true;
        lastPlace = place;
        lastKm = (int) km;
        ArrayList<Situation> results = new ArrayList<Situation>();
        for(Situation s : situations.getValue())
        {
            double d = getDistance(s, placeLat[place], placeLon[place]);
            if(d >= 0 && d <= km){
                results.add(s);}
        }
        base = results;
        baseText = "Within " + (int) km + " km of " + placeName;


        applyFilter();
    }

    // km to the closest end  of the situation
    // -1 if  no coordinates
    private double getDistance(Situation s, double lat, double lon)
    {
        double best = -1;
        float[] result = new float[1];
        if(s.getFrom().hasCoordinates())
        {
            Location.distanceBetween(lat, lon, s.getFrom().getLatitude(), s.getFrom().getLongitude(), result);

            best = result[0] / 1000;
        }
        if(s.getTo().hasCoordinates())
        {
            Location.distanceBetween(lat, lon, s.getTo().getLatitude(), s.getTo().getLongitude(), result);

            double d = result[0] / 1000;
            if(best < 0 || d < best){best = d; }


        }


        return best;
    }

    // only starts once, preventing the rotation downlaoding again
    public void startAutoUpdate(boolean useTestFiles)
    {
        if(started){return;}


        started = true;

        this.useTestFiles = useTestFiles;
        handler.post(autoUpdate);
    }
//run now and then again after UPDATE_TIME
    private Runnable autoUpdate = new Runnable()
    {
        @Override
        public void run()
        {
            startProgress();
            handler.postDelayed(this, UPDATE_TIME);
        }
    };
// start the bar and downlaoding the data in background
    public void startProgress()
    {
        // already downloading
        if(loading.getValue()){
            return;}
        loading.setValue(true);
        progress.setValue(0);
        target = 20;
        handler.removeCallbacks(fillBar);
        handler.post(fillBar);


        if (useTestFiles) {executor.execute(new TestTask());}
        else


        {
            // Run network access on a separate thread;
            executor.execute(new Task());
        }

    }

     // reads a test file from res/raw into a textstring
    private String readTestFile(int fileId)
    {
        StringBuilder text = new StringBuilder();
        try

        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getApplication().getResources().openRawResource(fileId)));
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line);
            }
            reader.close();
        } catch (IOException e) {
            Log.e("MyTag","Error reading test file");
        }


        return text.toString();
    }




   // download code from skeleton, moved here , returns the xml as text
    private String downloadData(String myurl)
    {
        StringBuilder result = new StringBuilder();
        BufferedReader in = null;
        String inputLine = "";
        try
        {
            URL url = new URL(myurl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();


            //ADD AUTHORISATION (string with clientIID:clientKey)
            urlConnection.setRequestProperty("Authorization",Base64.getEncoder().encodeToString((clientId + ":" + clientKey).getBytes()));
            try
            {
                in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                while ((inputLine = in.readLine()) != null){
                    result.append(inputLine);
                }

                in.close();
            }
            catch (IOException e)
            {
                Log.e("urlConnection", "ioexception");
            }

            finally
            {
                urlConnection.disconnect();
            }
        }

        catch (IOException e)
        {
            Log.e("URL", "ioexception");
        }

        //Clean up any leading garbage characters
        String data = result.toString();
        int i = data.indexOf("<?"); //initial tag
        if (i >= 0) {
            data = data.substring(i);
        }
        return data;
    }

    // saves the last xml so the app still has something without internet
    private void saveFile(String fileName, String data)
    {
        try
        {
            File f = new File(getApplication().getFilesDir(), fileName);
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(data.getBytes());
            fos.close();
        }
        catch (IOException e)
        {
            Log.e("MyTag","Error saving " + fileName);
        }
    }

    //read the saved Xml, if its not , then return ""
    private String readFile(String fileName)
    {
        File f = new File(getApplication().getFilesDir(), fileName);
        if(!f.exists()) {

            return ""; }
        StringBuilder text = new StringBuilder();
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line);
            }

            reader.close();
        } catch (IOException e)
        {
            Log.e("MyTag","Error reading " + fileName);
        }


        return text.toString();
    }
//time for the status row
    private String getTime(long millis)

    {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.UK);

        return format.format(new Date(millis));
    }


    // call from background
    // handler moves it to the UI thread
    private void showProgress(final int value)
    {
        handler.post(new Runnable() {
            @Override
            public void run() {
                target = value;
            }
        });
    }

    // bar goes up 5-10% at a time but never past the step the download is at
    private Runnable fillBar = new Runnable()
    {
        @Override
        public void run()
        {
            int value = progress.getValue();
            if(value < target)
            {value = value + 5 + random.nextInt(6);

                if(value > target)
                {value = target; }

                progress.setValue(value);
            }

            if(value >= 100)
            {
                // if bar is full show the new data
                status.setValue(newMessage);
                loading.setValue(false);
                situations.setValue(newList);
                searching = false;
                base = newList;
                baseText = "All situations";
                applyFilter();
            }


            else
            {
                handler.postDelayed(this, 200);
            }
        }
    };

// after downlaoding keeps the  new list until the bar reaches 100
    private void showResult(final ArrayList<Situation> list, final String message)
    {
        handler.post(new Runnable() {
            @Override
            public void run() {
                newList = list;
                newMessage = message;
                target = 100;
            }
        });}




    //download and read  the feeds , in the background thread
    private class Task implements Runnable
    {
        @Override
        public void run(){
            Log.d("MyTask","in run");
            String incidents = downloadData(unplannedEventsURL);
            showProgress(45);// bars steps
            String roadworks = downloadData(roadworksURL);
            showProgress(70);
            String message;

            if(incidents.equals("") || roadworks.equals(""))
            {
                // no internet- use the last saved data
                incidents = readFile("incidents.xml");
                roadworks = readFile("roadworks.xml");
                if(incidents.equals(""))
                {
                    // if didnt download its using the res and raw files
                    incidents = readTestFile(R.raw.unplanned_example);
                    roadworks = readTestFile(R.raw.roadworks_example);
                    message = "No internet connection - showing sample data";
                }
                else
                {
                    long saved = new File(getApplication().getFilesDir(), "incidents.xml").lastModified();
                    message = "No internet connection - showing data from " + getTime(saved);
                }
            }

            else
            {
                saveFile("incidents.xml", incidents); saveFile("roadworks.xml", roadworks);
                message = "Last updated: " + getTime(System.currentTimeMillis());
            }

            TrafficParser parser = new TrafficParser();

            ArrayList<Situation> list = new ArrayList<Situation>();
            list.addAll(parser.parse(incidents, "Incident"));

            showProgress(90);

            list.addAll(parser.parse(roadworks,"Roadworks"));

            showResult(list, message);


        }
    }

    private class TestTask implements Runnable
    {
        @Override
        public void run(){
            Log.d("MyTask","reading test files");
            TrafficParser parser = new TrafficParser();

            ArrayList<Situation> list = new ArrayList<Situation>();

            list.addAll(parser.parse(readTestFile(R.raw.unplanned_example), "Incident"));
            showProgress(60);

            list.addAll(parser.parse(readTestFile(R.raw.roadworks_example), "Roadworks"));
            showResult(list, "Showing test files");
        }
    }
//stops the times and thread when app is closed
    @Override
    protected void onCleared() {
        super.onCleared();
        handler.removeCallbacks(autoUpdate);
        handler.removeCallbacks(fillBar);
        executor.shutdown();
    }
}