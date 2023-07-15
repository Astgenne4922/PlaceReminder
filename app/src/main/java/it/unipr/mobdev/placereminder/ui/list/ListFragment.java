package it.unipr.mobdev.placereminder.ui.list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import it.unipr.mobdev.placereminder.R;
import it.unipr.mobdev.placereminder.databinding.FragmentListBinding;
import it.unipr.mobdev.placereminder.db.PointOfInterestViewModel;

public class ListFragment extends Fragment {
    private FragmentListBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.binding = FragmentListBinding.inflate(inflater, container, false);
        View root = this.binding.getRoot();

        PointOfInterestViewModel pointOfInterestViewModel = new ViewModelProvider(this.requireActivity()).get(PointOfInterestViewModel.class);
        PointOfInterestListAdapter adapter = new PointOfInterestListAdapter(new PointOfInterestListAdapter.PointOFInterestDiff(), pointOfInterestViewModel);
        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));

        pointOfInterestViewModel.getSavedPoIs().observe(this.getViewLifecycleOwner(), adapter::submitList);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.binding = null;
    }
}