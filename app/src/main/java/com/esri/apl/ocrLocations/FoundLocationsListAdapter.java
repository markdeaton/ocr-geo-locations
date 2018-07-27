package com.esri.apl.ocrLocations;

import android.databinding.ObservableList;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.esri.apl.ocrLocations.model.FoundLocation;

public class FoundLocationsListAdapter extends RecyclerView.Adapter<FoundLocationsListAdapter.FoundLocationHolder> {
  private ObservableList<FoundLocation> foundLocations;

  FoundLocationsListAdapter(ObservableList<FoundLocation> foundLocations) {
    this.foundLocations = foundLocations;
    this.foundLocations.addOnListChangedCallback(listChanged);
  }

  @NonNull
  @Override
  public FoundLocationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.location_list_item, parent, false);
    return new FoundLocationHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull FoundLocationHolder holder, int position) {
    holder.bind(foundLocations.get(position));
  }

  @Override
  public int getItemCount() {
    return foundLocations.size();
  }

  class FoundLocationHolder extends RecyclerView.ViewHolder {
    FoundLocationHolder(View itemView) {
      super(itemView);
    }

    void bind(FoundLocation foundLocation) {
      TextView txt = (TextView)itemView.findViewById(android.R.id.text1);
      txt.setText(foundLocation.text);
    }
  }

  private ObservableList.OnListChangedCallback<ObservableList<FoundLocation>> listChanged =
          new ObservableList.OnListChangedCallback<ObservableList<FoundLocation>>() {
    @Override
    public void onChanged(ObservableList<FoundLocation> sender) {
      notifyDataSetChanged();
    }

    @Override
    public void onItemRangeChanged(ObservableList<FoundLocation> sender, int positionStart, int itemCount) {
      notifyItemRangeChanged(positionStart, itemCount);
    }

    @Override
    public void onItemRangeInserted(ObservableList<FoundLocation> sender, int positionStart, int itemCount) {
      notifyItemRangeInserted(positionStart, itemCount);
    }

    @Override
    public void onItemRangeMoved(ObservableList<FoundLocation> sender, int fromPosition, int toPosition, int itemCount) {
      notifyItemRangeRemoved(fromPosition, itemCount);
      notifyItemRangeInserted(toPosition, itemCount);
    }

    @Override
    public void onItemRangeRemoved(ObservableList<FoundLocation> sender, int positionStart, int itemCount) {
      notifyItemRangeRemoved(positionStart, itemCount);
    }
  };
}
