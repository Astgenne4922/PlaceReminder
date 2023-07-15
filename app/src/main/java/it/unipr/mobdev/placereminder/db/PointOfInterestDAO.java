package it.unipr.mobdev.placereminder.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PointOfInterestDAO {
    @Query("SELECT * FROM points_of_interest ORDER BY change_timestamp DESC")
    LiveData<List<PointOfInterestEntity>> getAllLiveData();
    @Query("SELECT * FROM points_of_interest ORDER BY change_timestamp DESC")
    List<PointOfInterestEntity> getAll();

    @Update(entity = PointOfInterestEntity.class)
    void update(PointOfInterestEntity poi);
    @Insert
    void insertAll(PointOfInterestEntity... pois);
    @Delete
    void delete(PointOfInterestEntity logDescriptor);
}
