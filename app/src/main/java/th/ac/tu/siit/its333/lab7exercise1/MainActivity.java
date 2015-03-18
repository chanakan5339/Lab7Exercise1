package th.ac.tu.siit.its333.lab7exercise1;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;


public class MainActivity extends ActionBarActivity {

    long OtimeBkk = 0;
    long OtimeNon = 0;
    long OtimePT = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        WeatherTask w = new WeatherTask();
        w.execute("http://ict.siit.tu.ac.th/~cholwich/bangkok.json", "Bangkok Weather");
    }

    public void buttonClicked(View v) {
        int id = v.getId();
        WeatherTask w = new WeatherTask();


        long timeMillis = System.currentTimeMillis();
        long timeMin = TimeUnit.MILLISECONDS.toMinutes(timeMillis);

        switch (id) {
            case R.id.btBangkok:
                if(timeMin - OtimeBkk > 1 ) {
                    w.execute("http://ict.siit.tu.ac.th/~cholwich/bangkok.json", "Bangkok Weather");
                    OtimeBkk = timeMin;
                    OtimeNon = 0;
                    OtimePT = 0;
                }
                break;
            case R.id.btNon:
                if(timeMin - OtimeNon > 1 ) {
                    w.execute("http://ict.siit.tu.ac.th/~cholwich/nonthaburi.json", "Nonthaburi Weather");
                    OtimeNon = timeMin;
                    OtimeBkk = 0;
                    OtimePT = 0;

                }
                break;
            case R.id.btPathum:
                if(timeMin - OtimePT > 1 ) {
                    w.execute("http://ict.siit.tu.ac.th/~cholwich/pathumthani.json", "Pathumthani Weather");
                    OtimePT = timeMin;
                    OtimeBkk = 0;
                    OtimeNon = 0;
                }
                break;
            default: break;
        }



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class WeatherTask extends AsyncTask<String, Void, Boolean> {
        String errorMsg = "";
        ProgressDialog pDialog;
        String title;

        double windSpeed;
        double humidity;
        double tempMin;
        double tempMax;
        double temp;
        String weather;

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Loading weather data ...");
            pDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            BufferedReader reader;
            StringBuilder buffer = new StringBuilder();
            String line;
            try {
                title = params[1];
                URL u = new URL(params[0]);
                HttpURLConnection h = (HttpURLConnection)u.openConnection();
                h.setRequestMethod("GET");
                h.setDoInput(true);
                h.connect();

                int response = h.getResponseCode();
                if (response == 200) {
                    reader = new BufferedReader(new InputStreamReader(h.getInputStream()));
                    while((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }
                    //Start parsing JSON
                    JSONObject jWeather = new JSONObject(buffer.toString());
                    JSONObject jWind = jWeather.getJSONObject("wind");
                    windSpeed = jWind.getDouble("speed");
                    JSONObject jMain = jWeather.getJSONObject("main");
                    humidity = jMain.getDouble("humidity");
                    temp = jMain.getDouble("temp");
                    tempMin = jMain.getDouble("temp_min");
                    tempMax = jMain.getDouble("temp_max");
                    JSONArray jweather2 = jWeather.getJSONArray("weather");
                    weather = jweather2.getJSONObject(0).getString("main");
                    temp = temp - 272.15;
                    tempMax = tempMax - 272.15;
                    tempMin = tempMin - 272.15;
                    errorMsg = "";
                    return true;
                }
                else {
                    errorMsg = "HTTP Error";
                }
            } catch (MalformedURLException e) {
                Log.e("WeatherTask", "URL Error");
                errorMsg = "URL Error";
            } catch (IOException e) {
                Log.e("WeatherTask", "I/O Error");
                errorMsg = "I/O Error";
            } catch (JSONException e) {
                Log.e("WeatherTask", "JSON Error");
                errorMsg = "JSON Error";
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            TextView tvTitle, tvWeather, tvWind , tvHumid, tvTemp;
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }

            tvTitle = (TextView)findViewById(R.id.tvTitle);
            tvWeather = (TextView)findViewById(R.id.tvWeather);
            tvWind = (TextView)findViewById(R.id.tvWind);
            tvHumid = (TextView)findViewById(R.id.tvHumid);
            tvTemp = (TextView)findViewById(R.id.tvTemp);


            if (result) {
                tvTitle.setText(title);
                tvWeather.setText(weather);
                tvWind.setText(String.format("%.1f", windSpeed));
                tvHumid.setText(String.format("%.1f", humidity));
                tvTemp.setText(String.format("%.1f(max %.1f,min %.1f)",temp,tempMax,tempMin));
            }
            else {
                tvTitle.setText(errorMsg);
                tvWeather.setText("");
                tvWind.setText("");
                tvHumid.setText("");
                tvTemp.setText("");

             }

            }
        }
}

