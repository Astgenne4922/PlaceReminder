package it.unipr.mobdev.placereminder.ui.list;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import it.unipr.mobdev.placereminder.db.PointOfInterestEntity;
import it.unipr.mobdev.placereminder.db.PointOfInterestViewModel;

public class PointOfInterestListAdapter extends ListAdapter<PointOfInterestEntity, PointOfInterestViewHolder> {
    private final PointOfInterestViewModel pointOfInterestViewModel;
    public PointOfInterestListAdapter(DiffUtil.ItemCallback<PointOfInterestEntity> diffCallback, PointOfInterestViewModel pointOfInterestViewModel) {
        super(diffCallback);
        this.pointOfInterestViewModel = pointOfInterestViewModel;
    }

    @NonNull
    @Override
    public PointOfInterestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return PointOfInterestViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull PointOfInterestViewHolder holder, int position) {
        PointOfInterestEntity current = this.getItem(position);
        holder.bind(current, this.pointOfInterestViewModel);
    }

    static class PointOFInterestDiff extends DiffUtil.ItemCallback<PointOfInterestEntity> {
        @Override
        public boolean areItemsTheSame(@NonNull PointOfInterestEntity oldItem, @NonNull PointOfInterestEntity newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull PointOfInterestEntity oldItem, @NonNull PointOfInterestEntity newItem) {
            return oldItem.toString().equals(newItem.toString());
        }
    }
}
