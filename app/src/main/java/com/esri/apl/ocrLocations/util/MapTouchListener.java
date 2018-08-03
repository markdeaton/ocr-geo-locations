package com.esri.apl.ocrLocations.util;

import android.content.Context;
import android.view.MotionEvent;

import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MapTouchListener extends DefaultMapViewOnTouchListener {
  public MapTouchListener(Context context, MapView mapView) {
    super(context, mapView);
  }

  @Override
  public boolean onSingleTapConfirmed(MotionEvent e) {
    mMapView.getCallout().dismiss();

    return super.onSingleTapConfirmed(e);
  }
}
