package com.injiri.healthcarepoints;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class GeometryController {
    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * boolean variable for check loading
     */
    public static boolean loading;
    /**
     * initializing arrayList to to carry nearBy carepoints
     */
    public static ArrayList<Carepoint> carePointArrayList = new ArrayList();

    /**
     * manipulateData method to manipulate data through from JSON format
     *
     * @param buffer
     */
    public static void manipulateData(StringBuffer buffer) {
        loading = true;
        try {
            carePointArrayList.clear();

            JSONObject jsonpObject = new JSONObject(buffer.toString());
            JSONArray array = jsonpObject.getJSONArray("results");

            Carepoint carepoint = new Carepoint();
            for (int i = 0; i < array.length(); i++) {
                try {
                    JSONObject jsonObject = array.getJSONObject(i);


                    if (jsonObject.getString("name") != null) {
                        carepoint.setName(jsonObject.getString("name"));

                    } else carepoint.setName("Not Available");

                    try {
                        if (jsonObject.getJSONObject("opening_hours").getBoolean("open_now"))
                            carepoint.setOpeningHours("Opened");
                        else carepoint.setOpeningHours("closed");
                    } catch (Exception e) {
                        carepoint.setOpeningHours("Not Available");
                    }
                    carepoint.setContact(jsonObject.getString("vicinity"));
                    carepoint.setGeometry(new double[]{jsonObject.getJSONObject("geometry").getJSONObject("location").getDouble("lat"),
                            jsonObject.getJSONObject("geometry").getJSONObject("location").getDouble("lng")});


                } catch (Exception e) {
                    Log.e(TAG, "manipulateData: " + e);
                }
                carePointArrayList.add(carepoint);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "manipulateData: " + e);
        }

        Log.d("Array Loaded with size ", "Size of------" + carePointArrayList.size() + "+++++++++++++++++++++++");
        loading = false;
    }
}
