package it.unipr.mobdev.placereminder.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "points_of_interest")
public class PointOfInterestEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "description")
    private String description;
    @ColumnInfo(name = "latitude")
    private double latitude;
    @ColumnInfo(name = "longitude")
    private double longitude;
    @ColumnInfo(name = "change_timestamp")
    private long change_timestamp;

    public PointOfInterestEntity() {  }

    @Ignore
    public PointOfInterestEntity(String name, String description, double latitude, double longitude, long change_timestamp) {
        super();
        this.name = name;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.change_timestamp = change_timestamp;
    }

    @Ignore
    public PointOfInterestEntity(String name, String description, double latitude, double longitude) {
        this(name, description, latitude, longitude, System.currentTimeMillis());
    }

    public int getId() { return this.id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return this.description; }
    public void setDescription(String description) { this.description = description; }

    public double getLatitude() { return this.latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return this.longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public long getChange_timestamp() { return this.change_timestamp; }
    public void setChange_timestamp(long change_timestamp) { this.change_timestamp = change_timestamp; }

    @NonNull
    @Override
    public String toString() {
        return "PointOfInterestEntity{" +
                "uid=" + this.id +
                ", name='" + this.name + '\'' +
                ", description='" + this.description + '\'' +
                ", latitude=" + this.latitude +
                ", longitude=" + this.longitude +
                ", change_timestamp=" + this.change_timestamp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        PointOfInterestEntity that = (PointOfInterestEntity) o;

        if (this.id != that.id) return false;
        if (Double.compare(that.latitude, this.latitude) != 0) return false;
        if (Double.compare(that.longitude, this.longitude) != 0) return false;
        if (this.change_timestamp != that.change_timestamp) return false;
        if (!Objects.equals(this.name, that.name)) return false;
        return Objects.equals(this.description, that.description);
    }
}
