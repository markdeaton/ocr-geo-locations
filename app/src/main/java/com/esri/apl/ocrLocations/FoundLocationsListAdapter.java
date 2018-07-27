package com.esri.apl.ocrLocations;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.esri.apl.ocrLocations.model.FoundLocation;

import java.util.List;

public class FoundLocationsListAdapter extends RecyclerView.Adapter<FoundLocationsListAdapter.FoundLocationHolder> {
  private List<FoundLocation> foundLocations;

  FoundLocationsListAdapter(List<FoundLocation> foundLocations) {
    this.foundLocations = foundLocations;
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
}
