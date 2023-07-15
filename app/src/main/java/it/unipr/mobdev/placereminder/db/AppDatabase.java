package it.unipr.mobdev.placereminder.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {PointOfInterestEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract PointOfInterestDAO getDAO();
    private static AppDatabase INSTANCE;
    public static AppDatabase getDatabase(Context context) {
        if(INSTANCE == null) {
            INSTANCE = Room
                    .databaseBuilder(context, AppDatabase.class, "points_of_interest_db")
                    .allowMainThreadQueries()
                    .build();
        }
        return INSTANCE;
    }
}
