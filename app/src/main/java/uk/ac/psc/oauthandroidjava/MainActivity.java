package uk.ac.psc.oauthandroidjava;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;

public class MainActivity extends AppCompatActivity {

    // A constant that we will use to identify the return data from OAuth
    static final int GET_ACCESS_TOKEN = 1;

    Calendar start;

    ListView timetableView;
    TextView loadingLabel;

    String accessToken;
    String refreshToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadingLabel = (TextView)findViewById(R.id.loading);
        timetableView = (ListView)findViewById(R.id.timetable);

        // Work out the start of the week
        start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        start.set(Calendar.DAY_OF_WEEK, start.getFirstDayOfWeek());

        if (accessToken == null) {
            startActivityForResult(
                    new Intent(this,OAuthActivity.class),
                    GET_ACCESS_TOKEN);
        } else {
            new ReadTimetable().execute();
        }
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {
        if (requestCode == GET_ACCESS_TOKEN && resultCode == RESULT_OK) {
            try {
                JSONObject token = new JSONObject(data.getDataString());
                accessToken = token.getString("access_token");
                refreshToken = token.getString("refresh_token");
                new ReadTimetable().execute();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Handles the "previous" button presses
     * @param view Current view
     */
    public void moveBackward(View view) {
        start.add(Calendar.WEEK_OF_YEAR, -1);
        new ReadTimetable().execute();
    }

    /**
     * Handles the "next" button presses
     * @param view Current view
     */
    public void moveForward(View view) {
        start.add(Calendar.WEEK_OF_YEAR, +1);
        new ReadTimetable().execute();
    }

    /**
     *
     * @return The start time to use
     */
    public Calendar getStartTime() {
        return start;
    }

    /**
     * Adds a week to the start time
     * @return The end time to use
     */
    public Calendar getEndTime() {
        Calendar end = (Calendar)start.clone();
        end.add(Calendar.WEEK_OF_YEAR, 1);
        end.add(Calendar.MILLISECOND, -1);
        return end;
    }

    private class ReadTimetable extends AsyncTask<Void, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(Void... params) {
            JSONObject timetable = null;

            // Parameters to send to the API
            Map<String,String> parameters = new HashMap<String,String>();
            parameters.put("start", String.valueOf(getStartTime().getTimeInMillis()/1000));
            parameters.put("end", String.valueOf(getEndTime().getTimeInMillis()/1000));

            try {
                timetable =
                        new ApiHandler(accessToken).call("timetable",parameters);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return timetable;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            try {
                JSONArray timetableItems = jsonObject.getJSONArray("timetable");

                // Set up a string array to hold the item names
                String[] values = new String[timetableItems.length()];

                for (int i=0; i<timetableItems.length(); i++) {
                    JSONObject item = timetableItems.getJSONObject(i);
                    // Do whatever you need to with the data
                    Log.d("PSC",item.getString("Title"));

                    // Combine the start times and titles
                    Calendar start = Calendar.getInstance();
                    start.setTimeInMillis(item.getLong("Start")*1000);
                    values[i] = new SimpleDateFormat("MMM d H:m").format(start.getTime())+" - "
                            +item.getString("Title");
                }

                // Display the timetable
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                        android.R.layout.simple_list_item_1,android.R.id.text1,values);

                loadingLabel.setText("Timetable:");
                timetableView.setAdapter(adapter);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
