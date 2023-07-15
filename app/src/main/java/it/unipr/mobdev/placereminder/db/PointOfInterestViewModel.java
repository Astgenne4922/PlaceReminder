package it.unipr.mobdev.placereminder.db;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.google.android.gms.maps.CameraUpdate;

import java.util.List;

public class PointOfInterestViewModel extends AndroidViewModel {
    private final PointOfInterestDAO poiDAO;
    private final LiveData<List<PointOfInterestEntity>> savedPoIs;
    private CameraUpdate current_camera;

    public PointOfInterestViewModel(Application application) {
        super(application);
        this.poiDAO = AppDatabase.getDatabase(application).getDAO();
        this.savedPoIs = this.poiDAO.getAllLiveData();
        this.current_camera = null;
    }

    public LiveData<List<PointOfInterestEntity>> getSavedPoIs() { return this.savedPoIs; }

    public void setCurrent_camera(CameraUpdate new_camera) { this.current_camera = new_camera; }
    public CameraUpdate getCurrent_camera() { return this.current_camera; }

    public void update(PointOfInterestEntity poi) { this.poiDAO.update(poi); }
    public void insert(PointOfInterestEntity poi) { this.poiDAO.insertAll(poi); }
    public void delete(PointOfInterestEntity poi) { this.poiDAO.delete(poi); }
}
