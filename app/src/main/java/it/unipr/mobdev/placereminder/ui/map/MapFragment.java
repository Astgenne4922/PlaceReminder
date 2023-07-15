package it.unipr.mobdev.placereminder.ui.map;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import it.unipr.mobdev.placereminder.R;
import it.unipr.mobdev.placereminder.databinding.FragmentMapBinding;
import it.unipr.mobdev.placereminder.db.PointOfInterestEntity;
import it.unipr.mobdev.placereminder.db.PointOfInterestViewModel;

public class MapFragment extends Fragment implements OnMapReadyCallback, OnMapLongClickListener, OnInfoWindowClickListener, OnInfoWindowLongClickListener {
    private FragmentMapBinding binding;
    private GoogleMap map;
    private Map<PointOfInterestEntity, Marker> markers;
    private PointOfInterestViewModel pointOfInterestViewModel;
    private CameraUpdate current_camera = null;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.binding = FragmentMapBinding.inflate(inflater, container, false);
        View root = this.binding.getRoot();

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        this.pointOfInterestViewModel = new ViewModelProvider(this.requireActivity()).get(PointOfInterestViewModel.class);

        this.current_camera = this.pointOfInterestViewModel.getCurrent_camera();

        return root;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.map = googleMap;
        this.markers = new HashMap<>();
        this.initMap();
    }

    private void initMap() {
        if (this.map != null) {
            if (ActivityCompat.checkSelfPermission(this.requireActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this.requireActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                this.map.setMyLocationEnabled(true);
                this.map.getUiSettings().setMyLocationButtonEnabled(true);

                if(this.current_camera == null) {
                    LocationManager locationManager = (LocationManager) this.getActivity().getSystemService(Context.LOCATION_SERVICE);
                    Location pos = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    this.current_camera = CameraUpdateFactory.newLatLngZoom(new LatLng(pos.getLatitude(), pos.getLongitude()), 15);
                }
                this.map.moveCamera(this.current_camera);
            }

            this.pointOfInterestViewModel.getSavedPoIs().observe(this.getViewLifecycleOwner(), poi_list -> {
                for (PointOfInterestEntity poi : poi_list) {
                    this.addPoiToMap(poi);
                }
            });

            this.map.setOnMapLongClickListener(this);
            this.map.setOnInfoWindowClickListener(this);
            this.map.setOnInfoWindowLongClickListener(this);
        }
    }

    @Override
    public void onMapLongClick(@NonNull LatLng point) {
        Dialog dialog = new Dialog(this.requireActivity());
        dialog.setContentView(R.layout.creation_dialog);
        dialog.setTitle("Create Marker");

        Button button_cancel = dialog.findViewById(R.id.button_cancel);
        button_cancel.setOnClickListener(view -> dialog.dismiss());

        Button button_create = dialog.findViewById(R.id.button_create);
        button_create.setOnClickListener(view -> {
            EditText editText_name = dialog.findViewById(R.id.edittext_name);
            EditText editText_description = dialog.findViewById(R.id.edittext_description);

            PointOfInterestEntity poi = new PointOfInterestEntity(editText_name.getText().toString(), editText_description.getText().toString(), point.latitude, point.longitude);
            this.addPoiToMap(poi);
            this.pointOfInterestViewModel.insert(poi);

            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onInfoWindowClick(@NonNull Marker marker) {
        for (Map.Entry<PointOfInterestEntity, Marker> entry : this.markers.entrySet()) {
            if(entry.getValue().equals(marker)) {
                PointOfInterestEntity poi = entry.getKey();

                Dialog dialog = new Dialog(this.requireActivity());
                dialog.setContentView(R.layout.details_dialog);
                dialog.setTitle("Details");
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                TextView textView_name = dialog.findViewById(R.id.textview_name);
                textView_name.setText(poi.getName().isEmpty() ? "N/A" : poi.getName());

                TextView textView_description = dialog.findViewById(R.id.textview_description);
                textView_description.setText(poi.getDescription().isEmpty() ? "N/A" : poi.getDescription());

                TextView textView_address = dialog.findViewById(R.id.textview_address);
                try {
                    Geocoder coder = new Geocoder(this.requireActivity());
                    Iterator<Address> i = coder.getFromLocation(poi.getLatitude(), poi.getLongitude(), 1).iterator();
                    Address address = null;
                    if (i.hasNext()) address = i.next();
                    String address_s = "";
                    if (address != null) address_s = address.getCountryName() + ", " + address.getAdminArea() + ", " + address.getLocality() + ", " + address.getThoroughfare() + ", " + address.getFeatureName() + ", " + address.getPostalCode();
                    textView_address.setText(address_s);
                } catch (IOException e) {
                    textView_address.setText("N/A");
                }

                TextView textView_lat = dialog.findViewById(R.id.textview_lat);
                textView_lat.setText(String.valueOf(poi.getLatitude()));

                TextView textView_lng = dialog.findViewById(R.id.textview_lng);
                textView_lng.setText(String.valueOf(poi.getLongitude()));

                TextView textView_date = dialog.findViewById(R.id.textview_date);
                textView_date.setText(DateFormat.getDateTimeInstance().format(new Date(poi.getChange_timestamp())));

                dialog.show();
            }
        }
    }

    @Override
    public void onInfoWindowLongClick(@NonNull Marker marker) {
        for (Map.Entry<PointOfInterestEntity, Marker> entry : this.markers.entrySet()) {
            if(entry.getValue().equals(marker)) {
                PointOfInterestEntity poi = entry.getKey();

                Dialog dialog = new Dialog(this.requireActivity());
                dialog.setContentView(R.layout.creation_dialog);
                dialog.setTitle("Edit Marker");

                EditText editText_name = dialog.findViewById(R.id.edittext_name);
                editText_name.setText(poi.getName());
                EditText editText_description = dialog.findViewById(R.id.edittext_description);
                editText_description.setText(poi.getDescription());

                Button button_cancel = dialog.findViewById(R.id.button_cancel);
                button_cancel.setOnClickListener(view -> dialog.dismiss());

                Button button_edit = dialog.findViewById(R.id.button_create);
                button_edit.setText("Edit");
                button_edit.setOnClickListener(view -> {
                    String name = editText_name.getText().toString();
                    String description = editText_description.getText().toString();

                    marker.setTitle(name);
                    marker.setSnippet(description);

                    PointOfInterestEntity changed_poi = new PointOfInterestEntity(name, description, poi.getLatitude(), poi.getLongitude());
                    changed_poi.setId(poi.getId());
                    this.pointOfInterestViewModel.update(changed_poi);

                    dialog.dismiss();
                });

                dialog.show();
            }
        }
    }

    private void addPoiToMap(@NonNull PointOfInterestEntity poi){
        MarkerOptions markerOptions = new MarkerOptions()
                .title(poi.getName())
                .snippet(poi.getDescription())
                .position(new LatLng(poi.getLatitude(), poi.getLongitude()));

        if(this.markers.containsKey(poi)) this.markers.get(poi).remove();
        this.markers.put(poi, this.map.addMarker(markerOptions));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        for (Map.Entry<PointOfInterestEntity, Marker> entry : this.markers.entrySet()) {
            entry.getValue().remove();
        }

        this.pointOfInterestViewModel.setCurrent_camera(CameraUpdateFactory.newCameraPosition(this.map.getCameraPosition()));

        this.binding = null;
    }
}
