package com.injiri.healthcarepoints.adapters;


import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.injiri.healthcarepoints.R;
import com.injiri.healthcarepoints.model.Carepoint;

import java.util.ArrayList;

public class CarepointAdapter extends RecyclerView.Adapter<CarepointAdapter.CarepointsHolder> {

    ArrayList<Carepoint> carepoints;
    Context context;

    public CarepointAdapter(ArrayList<Carepoint> carepoints, Context context) {
        this.carepoints = carepoints;
        this.context = context;
    }

    @NonNull
    @Override
    public CarepointsHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {


        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

        View view = inflater.inflate(R.layout.carepoint_item, viewGroup, false);
        return new CarepointsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarepointsHolder carepointsHolder, int position) {
        Carepoint carePoint = carepoints.get(position);
        carepointsHolder.hospital.setText("Hospital");
        carepointsHolder.hospitalName.setText(carePoint.getName());
        carepointsHolder.distance.setText(".KM"); //carePoint.getCarepointDistance()
        carepointsHolder.isOpenOrClosed.setText(carePoint.getOpeningHours());

    }

    @Override
    public int getItemCount() {
        return carepoints.size();
    }

    public class CarepointsHolder extends RecyclerView.ViewHolder {
        TextView hospital, hospitalName, isOpenOrClosed, distance;

        public CarepointsHolder(@NonNull View itemView) {
            super(itemView);

            hospital = (TextView) itemView.findViewById(R.id.hospital_title);
            hospitalName = (TextView) itemView.findViewById(R.id.hospital_title);
            isOpenOrClosed = (TextView) itemView.findViewById(R.id.open_or_closed);
            distance = (TextView) itemView.findViewById(R.id.distance);

        }
    }
}
