package com.esri.apl.ocrLocations.model;

import com.esri.arcgisruntime.mapping.view.Graphic;

public interface FoundLocationClickListener {
  void itemClicked(Graphic g);
  void showCallout(Graphic g);
}
