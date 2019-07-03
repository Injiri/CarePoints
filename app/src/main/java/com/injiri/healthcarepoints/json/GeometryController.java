package com.injiri.healthcarepoints.json;

import android.util.Log;

import com.injiri.healthcarepoints.model.Carepoint;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class GeometryController {

    private static final String TAG = GeometryController.class.getSimpleName();


    /**
     * deserializeCarepointData method to manipulate data through from JSON format
     *
     * @param carepointsStringBuffer
     */
    public static ArrayList<Carepoint> deserializeCarepointData(StringBuffer carepointsStringBuffer) {

        ArrayList<Carepoint> carePointArrayList = new ArrayList();

        try {

            JSONObject jsonpObject = new JSONObject(carepointsStringBuffer.toString());
            JSONArray jsonArray = jsonpObject.getJSONArray("results");

            Carepoint carepoint = new Carepoint();
            for (int index = 0; index < jsonArray.length(); index++) {
                try {
                    JSONObject jsonObject = jsonArray.getJSONObject(index);


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
                    Log.e(TAG, "deserializeCarepointData: " + e);
                }

                carePointArrayList.add(carepoint);
            }

            Log.e(TAG, "deserializeCarepointData: Carepoints Arrays List Data " + carePointArrayList);

            return carePointArrayList;

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "deserializeCarepointData: " + e);

            return new ArrayList<>();
        }

    }
}
