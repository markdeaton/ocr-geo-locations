package com.esri.apl.ocrLocations.model;

import android.support.annotation.NonNull;

import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.view.Graphic;

public class FoundLocation {
  private String text;
  private Graphic location;
  private double score;

  public FoundLocation(@NonNull String text, Graphic location) {
    this.text = text;
    this.location = location;
  }

  /** Constructor meant for testing. Will create a WGS84 point from supplied coordinates. */
  public FoundLocation(@NonNull String text, double lon, double lat) {
    Point pt = new Point(lon, lat, SpatialReferences.getWgs84());
    this.text = text;
    Graphic g = new Graphic(pt);
    this.location = g;
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

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public Graphic getLocation() {
    return location;
  }

  public void setLocation(Graphic location) {
    this.location = location;
  }

  public double getScore() {
    return score;
  }

  public void setScore(double score) {
    this.score = score;
  }
}
