package com.injiri.healthcarepoints.views;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.snackbar.Snackbar;
import com.injiri.healthcarepoints.BuildConfig;
import com.injiri.healthcarepoints.R;
import com.injiri.healthcarepoints.adapters.CarepointAdapter;
import com.injiri.healthcarepoints.http.HttpRequests;
import com.injiri.healthcarepoints.json.GeometryController;
import com.injiri.healthcarepoints.model.Carepoint;
import com.injiri.healthcarepoints.services.UserlocationService;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    RecyclerView carepointsRecyclerView;
    ArrayList<Carepoint> carepoints;
    ProgressBar carepointsrogressBar;
    Button carepointsbtm;
    CarepointAdapter adapter;
    public double latitude;
    public double longitude;
    private boolean userservice_running = false;
    public static java.lang.StringBuffer stringBuffer = new StringBuffer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        carepointsRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        carepointsbtm = (Button) findViewById(R.id.carepoints_btn);
        carepointsrogressBar = (ProgressBar) findViewById(R.id.carepoints_progress_bar);
        carepoints = new ArrayList<>();

        carepoints.add(new Carepoint("Lurambi", "Open now", "+254703474326", new double[7]));
        adapter = new CarepointAdapter(carepoints, getApplicationContext());
        carepointsRecyclerView.setAdapter(adapter);
        carepointsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        latitude = 0.2844924;
        longitude = 34.7673467;

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String lat = intent.getStringExtra(UserlocationService.USER_LONGITUDE);
                final String lng = intent.getStringExtra(UserlocationService.USER_LATITUDE);
                if (lat != null && lng != null) {
                    Toast.makeText(getApplicationContext(), "Latitude " + lat + "\n Longitude:" + lng, Toast.LENGTH_LONG).show();

//                    latitude = Double.valueOf(lat);
//                    longitude = Double.valueOf(lng);

                } else {
                    Toast.makeText(getApplicationContext(), "Latitude " + "null" + "\n Longitude:" + lng, Toast.LENGTH_LONG).show();
                }
            }
        }, new IntentFilter(UserlocationService.LOCATION_BROADCAST_ACTION));

        carepointsbtm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "onClick: care poinnts button");
                FetchCarepointsTask fetchCarepointsTask = new FetchCarepointsTask();
                fetchCarepointsTask.execute();

            }
        });

    }


    public class FetchCarepointsTask extends AsyncTask<Void, Void, Void> {

        @SuppressLint("LongLogTag")
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            carepointsrogressBar.setVisibility(View.VISIBLE);

        }

        @Override
        protected Void doInBackground(Void... voids) {

            StringBuffer carePointsStringBuffer = HttpRequests.getAllAvailableCarepoints(latitude, longitude);
            ArrayList<Carepoint> ca = new ArrayList<>();


            if (carePointsStringBuffer != null) {
                carepoints = GeometryController.deserializeCarepointData(carePointsStringBuffer);
                for (Carepoint carepoint : carepoints) {
                    Log.e(TAG, "doInBackground: carepoint detail :" + carepoint.getName());
                }
            } else {
                Log.e(TAG, "doInBackground: buffer null");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            super.onPostExecute(aVoid);

            carepointsrogressBar.setVisibility(View.INVISIBLE);
            adapter.notifyDataSetChanged();

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkgoogle_services();
    }

    public void checkgoogle_services() {

        if (isgoogleservices_available()) {
            checkinternet_connectivity(null);
        } else {
            Toast.makeText(getApplicationContext(), "google services not available", Toast.LENGTH_LONG).show();

        }
    }

    public Boolean checkinternet_connectivity(DialogInterface dialogInterface) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo active_networkinfo = connectivityManager.getActiveNetworkInfo();
        if (active_networkinfo == null || !active_networkinfo.isConnected()) {
            connecttoInternet_dialog();
            return false;
        }
        if (dialogInterface != null) {
            dialogInterface.dismiss();

        }
        // if connection is active, proceed and check the permissions
        if (permission_granted()) {
            start_userlocation_service();
        } else {
            request_permisions();
        }
        return true;

    }

    public void connecttoInternet_dialog() {
        AlertDialog.Builder allert_builder = new AlertDialog.Builder(MainActivity.this);
        allert_builder.setTitle("No Internet");
        allert_builder.setMessage("sorry refresh connection and try agaain");
        String refresh_btnText = "Reconnect";
        allert_builder.setPositiveButton(refresh_btnText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //proceed only after  a user grrants all the permissions needed
                if (checkinternet_connectivity(dialog)) {
                    // now is location permission granted?
                    if (permission_granted()) {
                        //all resources are available to start the service
                        start_userlocation_service();
                    }


                }
            }
        });
        AlertDialog dialog = allert_builder.create();
        dialog.show();
    }

    public void start_userlocation_service() {
//        it runs untill the activity is stoped/clossed
        if (!userservice_running) {
            Toast.makeText(this, "user service running", Toast.LENGTH_LONG).show();
            //start broadcast sharering
            Intent intent = new Intent(this, UserlocationService.class);
            startService(intent);
            userservice_running = true;
        }
    }

    public boolean permission_granted() {
        int state1 = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int state2 = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);


        return state1 == PackageManager.PERMISSION_GRANTED && state2 == PackageManager.PERMISSION_GRANTED;


    }

    public void request_permisions() {
        boolean provide_rationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION);
        boolean provide_rationale2 = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (provide_rationale || provide_rationale2) {
            display_snackbar(R.string.rationale_permission, android.R.string.ok, new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 34);
                }
            });
        } else {
//    Log.i(TAG,"Requesting permission");
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 34);
        }
    }


    public void display_snackbar(final int textstring_id, final int actionStringid, View.OnClickListener listener) {
        Snackbar.make(findViewById(R.id.content), getString(actionStringid), Snackbar.LENGTH_INDEFINITE).setAction(getString(actionStringid), listener).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 34) {
            if (grantResults.length <= 0) {
                Log.i("SNACKBAR", "user Interaction canceled");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i("SNACKBAR", "permissions succesfully granted");
                start_userlocation_service();
            } else {
                //all permissions have been denied
                display_snackbar(R.string.deniedpermission_exp, R.string.settings, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //display the app setting intent
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                        intent.setData(uri);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }

                });
            }
        }
    }

    public boolean isgoogleservices_available() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(this, status, 2404).show();

            }
            return false;
        }
        return true;

    }

    @Override
    protected void onDestroy() {
        //stop location service broadcast
        stopService(new Intent(this, UserlocationService.class));
        userservice_running = false;
        super.onDestroy();
    }


}

