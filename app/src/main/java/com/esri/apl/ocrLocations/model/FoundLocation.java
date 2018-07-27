package com.esri.apl.ocrLocations.model;

import android.support.annotation.NonNull;

import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;

public class FoundLocation {
  public String text;
  public Point location;

  public FoundLocation(@NonNull String text, Point location) {
    this.text = text;
    this.location = location;
  }

  /** Constructor meant for testing. Will create a WGS84 point from supplied coordinates. */
  public FoundLocation(@NonNull String text, double lon, double lat) {
    Point pt = new Point(lon, lat, SpatialReferences.getWgs84());
    this.text = text;
    this.location = pt;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof FoundLocation)) return false;

    FoundLocation that = (FoundLocation)obj;
    boolean areBothStringsNull = (this.text == null && that.text == null);
    boolean areBothStringsNotNull = (this.text != null && that.text != null);
    boolean areStringsEqual = areBothStringsNull
                              || (areBothStringsNotNull && this.text.equals(that.text));
/*    boolean areBothLocsNull = (this.location == null && that.location == null);
    boolean areBothLocsNotNull = (this.location != null && that.location != null);
    boolean areLocsEqual = areBothLocsNull
                           || (areBothLocsNotNull && this.location.equals(that.location));*/
    return  areStringsEqual;
  }
}
