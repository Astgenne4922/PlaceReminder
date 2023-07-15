package it.unipr.mobdev.placereminder.ui.list;

import android.app.AlertDialog;
import android.app.Dialog;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;

import it.unipr.mobdev.placereminder.R;
import it.unipr.mobdev.placereminder.db.PointOfInterestEntity;
import it.unipr.mobdev.placereminder.db.PointOfInterestViewModel;

public class PointOfInterestViewHolder extends RecyclerView.ViewHolder implements OnClickListener, OnLongClickListener {
    private final View view;
    private PointOfInterestEntity poi;
    private PointOfInterestViewModel pointOfInterestViewModel;

    private PointOfInterestViewHolder(View itemView) {
        super(itemView);
        this.view = itemView;
    }

    public void bind(@NonNull PointOfInterestEntity poi, PointOfInterestViewModel pointOfInterestViewModel) {
        this.poi = poi;
        this.pointOfInterestViewModel = pointOfInterestViewModel;

        this.view.setOnClickListener(this);
        this.view.setOnLongClickListener(this);

        TextView textView_name = this.view.findViewById(R.id.textview_name);
        textView_name.setText(poi.getName().isEmpty() ? "N/A" : poi.getName());

        TextView textView_description = this.view.findViewById(R.id.textview_description);
        textView_description.setText(poi.getDescription().isEmpty() ? "N/A" : poi.getDescription());

        TextView textView_address = this.view.findViewById(R.id.textview_address);
        try {
            Geocoder coder = new Geocoder(this.view.getContext());
            Iterator<Address> i = coder.getFromLocation(poi.getLatitude(), poi.getLongitude(), 1).iterator();
            Address address = null;
            if (i.hasNext()) address = i.next();
            String address_s = "";
            if (address != null) address_s = address.getCountryName() + ", " + address.getAdminArea() + ", " + address.getLocality() + ", " + address.getThoroughfare() + ", " + address.getFeatureName() + ", " + address.getPostalCode();
            textView_address.setText(address_s);
        } catch (IOException e) {
            textView_address.setText("N/A");
        }

        TextView textView_lat = this.view.findViewById(R.id.textview_lat);
        textView_lat.setText(String.valueOf(poi.getLatitude()));

        TextView textView_lng = this.view.findViewById(R.id.textview_lng);
        textView_lng.setText(String.valueOf(poi.getLongitude()));

        TextView textView_date = this.view.findViewById(R.id.textview_date);
        textView_date.setText(DateFormat.getDateTimeInstance().format(new Date(poi.getChange_timestamp())));
    }

    @NonNull
    public static PointOfInterestViewHolder create(@NonNull ViewGroup parent) {
        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.recyclerview_item, parent, false);
        return new PointOfInterestViewHolder(v);
    }

    @Override
    public void onClick(View view) {
        Dialog dialog = new Dialog(this.view.getContext());
        dialog.setContentView(R.layout.creation_dialog);
        dialog.setTitle("Edit Marker");

        EditText editText_name = dialog.findViewById(R.id.edittext_name);
        editText_name.setText(this.poi.getName());
        EditText editText_description = dialog.findViewById(R.id.edittext_description);
        editText_description.setText(this.poi.getDescription());

        Button button_cancel = dialog.findViewById(R.id.button_cancel);
        button_cancel.setOnClickListener(view1 -> dialog.dismiss());

        Button button_edit = dialog.findViewById(R.id.button_create);
        button_edit.setText("Edit");
        button_edit.setOnClickListener(view1 -> {
            String name = editText_name.getText().toString();
            String description = editText_description.getText().toString();

            PointOfInterestEntity changed_poi = new PointOfInterestEntity(name, description, this.poi.getLatitude(), this.poi.getLongitude());
            changed_poi.setId(this.poi.getId());
            this.pointOfInterestViewModel.update(changed_poi);

            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public boolean onLongClick(View view) {
        AlertDialog.Builder confirm_dialog = new AlertDialog.Builder(this.view.getContext());
        confirm_dialog.setMessage("Do you want to delete the marker?")
                .setPositiveButton("Yes", (dialogInterface, i) -> this.pointOfInterestViewModel.delete(this.poi))
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                .create()
                .show();

        return false;
    }
}
