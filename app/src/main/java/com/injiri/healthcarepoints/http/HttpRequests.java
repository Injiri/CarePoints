package com.injiri.healthcarepoints.http;

import android.util.Log;

import com.injiri.healthcarepoints.views.MainActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpRequests {

    private static final String TAG = HttpRequests.class.getSimpleName();

    public static StringBuffer getAllAvailableCarepoints(double latitude, double longitude) {
        try {
            /** initializing StringBuilder  */
            StringBuilder stringBuilder = new StringBuilder()
                    .append("https://maps.googleapis.com/maps/api/place/search/json?rankby=distance&keyword=hospital&location=")
                    .append(latitude)
                    .append(",")
                    .append(longitude)
                    .append("&key=AIzaSyC6-gwhsbRMAbtSNhR56y2EBV9S16bZhHE&sensor=false&libraries=places");

            /** searching for url */
            URL url = new URL(stringBuilder.toString());

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = connection.getInputStream();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer stringBuffer = new StringBuffer();

            String outputData = "";

            while ((outputData = bufferedReader.readLine()) != null) {
                Log.e(TAG, "getAllAvailableCarepoints: outputData" + outputData);
                stringBuffer.append(outputData);
            }

            Log.e(TAG, "loaded with size of  => " + "Size is " + stringBuffer.length());

            return stringBuffer;

        } catch (Exception e) {
            Log.e(TAG, "onPreExecute excemption" + e);

            return null;

        }

    }
}
