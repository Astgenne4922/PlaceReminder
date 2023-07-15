package it.unipr.mobdev.placereminder.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import it.unipr.mobdev.placereminder.R;
import it.unipr.mobdev.placereminder.db.AppDatabase;
import it.unipr.mobdev.placereminder.db.PointOfInterestDAO;
import it.unipr.mobdev.placereminder.db.PointOfInterestEntity;

public class LocationTrackerService extends Service implements LocationListener {
    public static int ONGOING_NOTIFICATION = 1111;
    public static final int TWO_MINUTES = 1000 * 60 * 2;
    public static boolean IS_RUNNING;
    private LocationManager locationManager;
    private Location current_location;
    private PointOfInterestEntity previous_best;
    private PointOfInterestDAO poiDAO;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        IS_RUNNING = false;
        this.poiDAO = AppDatabase.getDatabase(this).getDAO();
        this.locationProvidersInit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        IS_RUNNING = false;
        this.stopForeground(true);
        this.locationManager.removeUpdates(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.startServiceTask();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public void startServiceTask() {
        IS_RUNNING = true;
        this.setServiceAsForeground();
    }

    private void setServiceAsForeground() {
        Intent intent = new Intent(this, LocationTrackerService.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("Ongoing_tracker_id", "Ongoing_tracker", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);
            builder = new NotificationCompat.Builder(this.getApplicationContext(), notificationChannel.getId());
        } else {
            builder = new NotificationCompat.Builder(this.getApplicationContext());
        }

        Notification notification  = builder.setContentTitle("Place Reminder Tracker")
                .setContentText("Checking for markers in a 100m radius")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentIntent(pIntent)
                .setSilent(true)
                .build();

        this.startForeground(ONGOING_NOTIFICATION, notification);
    }

    private void locationProvidersInit(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            this.locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            if(this.locationManager == null){
                Toast.makeText(this, "Location Manager not Available!", Toast.LENGTH_LONG).show();
                return;
            }

            if (this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            } else {
                Toast.makeText(this, "GPS is not enabled!", Toast.LENGTH_LONG).show();
            }

            if (this.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                this.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            } else {
                Toast.makeText(this, "LOCATION NETWORK PROVIDER is not enabled!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (this.current_location == null) {
            this.current_location = location;
        } else if (this.isBetterLocation(location, this.current_location)) {
            this.current_location = location;
        }

        this.checkProximity(this.current_location);
    }

    private boolean isBetterLocation(Location location, Location current) {
        if (current == null) { return true; }

        long timeDelta = location.getTime() - current.getTime();

        if (timeDelta > TWO_MINUTES) {
            return true;
        }
        if (timeDelta < -TWO_MINUTES) {
            return false;
        }

        int accuracyDelta = (int) (location.getAccuracy() - current.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        boolean isFromSameProvider = this.isSameProvider(location.getProvider(), current.getProvider());

        return isMoreAccurate || (timeDelta > 0 && (!isLessAccurate || (!isSignificantlyLessAccurate && isFromSameProvider)));
    }

    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    private void checkProximity(Location location) {
        List<PointOfInterestEntity> pois = this.poiDAO.getAll();
        if (pois == null) { return; }

        PointOfInterestEntity best = null;
        double best_distance = Double.POSITIVE_INFINITY;

        Location tmp = new Location("");
        for (PointOfInterestEntity poi : pois) {
            tmp.setLatitude(poi.getLatitude());
            tmp.setLongitude(poi.getLongitude());

            double distance = location.distanceTo(tmp);
            if(distance < best_distance && distance <= 200) {
                best_distance = distance;
                best = poi;
            }
        }

        if(best == null) { return; }
        if(this.previous_best != null && this.previous_best.equals(best)) { return; }
        this.previous_best = best;

        try {
            Geocoder coder = new Geocoder(this);
            Iterator<Address> i = coder.getFromLocation(best.getLatitude(), best.getLongitude(), 1).iterator();
            Address address = null;
            if (i.hasNext()) address = i.next();
            String address_s = "";
            if (address != null) address_s = address.getCountryName() + ", " + address.getAdminArea() + ", " + address.getLocality() + ", " + address.getThoroughfare() + ", " + address.getFeatureName() + ", " + address.getPostalCode();

            Intent intent = new Intent(this, LocationTrackerService.class);
            PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

            NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder builder;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel("Push_tracker_id", "Push_tracker", NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(notificationChannel);
                builder = new NotificationCompat.Builder(this.getApplicationContext(), notificationChannel.getId());
            } else {
                builder = new NotificationCompat.Builder(this.getApplicationContext());
            }

            Notification notification  = builder.setContentTitle("Place Reminder Tracker")
                    .setContentText("In proximity of the marker: " + best.getName())
                    .setStyle(new NotificationCompat.BigTextStyle().bigText("Name: " + best.getName() + "\nDescription: " + best.getDescription() + "\nAddress: " + address_s + "\nLat: " + best.getLatitude() + "\nLng: " + best.getLongitude() + "\nLast change: " + DateFormat.getDateTimeInstance().format(new Date(best.getChange_timestamp()))))
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentIntent(pIntent)
                    .build();

            notificationManager.notify(0, notification);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
