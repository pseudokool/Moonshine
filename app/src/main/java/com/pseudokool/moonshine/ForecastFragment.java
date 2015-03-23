package com.pseudokool.moonshine;

import android.app.Fragment;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by carlyle on 17/03/15.
 */
/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    private ArrayAdapter<String> mForecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater){
        menuInflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){

        switch(menuItem.getItemId()){
            case R.id.action_refresh:
                FetchWeatherTask fetchWeatherTask = new FetchWeatherTask();
                fetchWeatherTask.execute("400054,IN");
                break;

            default:

        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        String[] forecastArray = {
                "Today - Smoggy -- 15/37",
                "Today - Hail -- 45/37",
                "Today - Smoggy -- 65/37",
                "Today - Rain -- 25/37",
                "Today - Smoggy -- 25/37",
                "Today - Smoggy -- 25/37",
                "Today - Sun-- 25/37",
                "Today - Smoggy -- 25/37",
                "Today - Mist-- 25/37",
                "Today - Smoggy -- 25/37",
                "Today - Smoggy -- 25/37",
                "Today - Smoggy -- 25/37",
                "Today - Smoggy -- 25/37",
                "Today - Smoggy -- 25/37",
                "Today - Smoggy -- 25/37",
                "Today - Smoggy -- 25/37",
                "Today - Smoggy -- 25/37",
                "Today - Smoggy -- 25/37",
                "Today - Smoggy -- 25/37",
                "Today - Smoggy -- 25/37",
                "Today - Smoggy -- 25/37",
                "Today - Smoggy -- 25/37",
                "Today - Smoggy -- 25/37",
                "Tomorrow - Hail -- 31/34"
        };


        ArrayList<String> weekForecast = new ArrayList<String>(Arrays.asList(forecastArray));

        mForecastAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                weekForecast
        );


        ListView listView = (ListView) rootView.findViewById(R.id.listView_forecast);
        listView.setAdapter(mForecastAdapter);

        return rootView;
    }


    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... params){
            String forecastJsonStr = null;
            HttpURLConnection urlConnection = null;
            BufferedReader bufferedReader = null;
            String format = "json";
            String units = "metric";
            int numDays = 7;

            try{

                //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=Mumbai&units=metric&cnt=7&mode=json");
                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";

                Uri builtURI = Uri.parse(FORECAST_BASE_URL).buildUpon()
                                .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays)).build();

                URL url = new URL(builtURI.toString());
                Log.v(LOG_TAG, url.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer stringBuffer = new StringBuffer();
                if( inputStream==null){
                    return null;
                }
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while( (line = bufferedReader.readLine())!= null ){
                    stringBuffer.append(line + "\n");
                }
                if(stringBuffer.length()==0){
                    return null;
                }
                forecastJsonStr = stringBuffer.toString();
                Log.v(LOG_TAG, "JSON: " + forecastJsonStr);



            } catch(IOException ioex){
                //Log.e("PHFrag", "Error", ioex);
            } finally{
                if(urlConnection!=null){
                    urlConnection.disconnect();
                }

                if(bufferedReader!=null){
                    try{
                        bufferedReader.close();
                    } catch (final IOException fioex){

                        Log.e("PHFrag", "StreamCloseError", fioex);
                    }
                }

                try{
                    return getWeatherDataFromJson(forecastJsonStr, numDays);
                } catch(JSONException jsex) {
                    Log.e(LOG_TAG, jsex.getMessage(), jsex);
                }

            }

            return null;
        }

        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
            throws JSONException {

            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMP = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DT = "dt";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            String resultStrs[] = new String[numDays];
            for(int i=0; i<weatherArray.length();i++) {
                /*
                    "dt":1426795200,
                    "temp":{
                        "day":9.47,
                        "min":9.47,
                        "max":9.47,
                        "night":9.47,
                        "eve":9.47,
                        "morn":9.47
                        },
                    "pressure":993.17,
                    "humidity":90,
                    "weather":[
                    {
                        "id":800,
                        "main":"Clear",
                        "description":"sky is clear",
                        "icon":"01n"
                    }
                    ],
                    "speed":0.87,
                    "deg":285,
                    "clouds":0
                */
                String day;
                String desc;
                String highLow;
                double max;
                double min;

                JSONObject dayForecast = weatherArray.getJSONObject(i);

                long dt = dayForecast.getLong(OWM_DT);
                Date date = new Date(dt * 1000);
                SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
                day = format.format(date).toString();

                JSONObject weather = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                desc = weather.getString(OWM_DESCRIPTION);

                JSONObject temp = dayForecast.getJSONObject(OWM_TEMP);
                max = temp.getDouble(OWM_MAX);
                min = temp.getDouble(OWM_MIN);

                highLow = Math.round(min) + "/" + Math.round(max);

                resultStrs[i] = day + " " + desc + " " + highLow;
                Log.v(LOG_TAG, resultStrs[i]);

            }   // for

            return resultStrs;
        }

        @Override
        protected void onPostExecute(String[] results) {
            if( results!=null ) {
                mForecastAdapter.clear();

                for(String dayForecastStr : results ) {
                    mForecastAdapter.add(dayForecastStr);
                }
            }
        }

    }
}
