package com.esri.apl.ocrLocations;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.esri.apl.ocrLocations.viewmodel.MainViewModel;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.util.ListChangedEvent;
import com.esri.arcgisruntime.util.ListChangedListener;
import com.esri.arcgisruntime.util.ListenableList;

public class FoundLocationsListAdapter extends RecyclerView.Adapter<FoundLocationsListAdapter.FoundLocationHolder> {
  private ListenableList<Graphic> foundLocations;

  FoundLocationsListAdapter(ListenableList<Graphic> foundLocations) {
    this.foundLocations = foundLocations;
    this.foundLocations.addListChangedListener(mListChanged);
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

    void bind(Graphic foundLocation) {
      TextView txt = (TextView)itemView.findViewById(android.R.id.text1);
      String sOCRLoc = foundLocation.getAttributes().get(MainViewModel.ATTR_OCRED_LOCATION_NAME).toString();
      String sLocName = foundLocation.getAttributes().get(MainViewModel.ATTR_GEOCODED_LOCATION_NAME).toString();
      txt.setText(sOCRLoc + " -> " + sLocName);
    }
  }

  private ListChangedListener mListChanged = new ListChangedListener() {
    @Override
    public void listChanged(ListChangedEvent listChangedEvent) {
      switch (listChangedEvent.getAction()) {
        case ADDED:
          notifyItemRangeInserted(listChangedEvent.getIndex(), listChangedEvent.getItems().size());
          break;
        case REMOVED:
          notifyItemRangeRemoved(listChangedEvent.getIndex(), listChangedEvent.getItems().size());
          break;
        default:
          notifyDataSetChanged();
          break;
      }
    }
  };
}
