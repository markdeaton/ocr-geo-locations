package com.esri.apl.ocrLocations.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;

import com.esri.apl.ocrLocations.util.ArrayListCCI;
import com.esri.apl.ocrLocations.util.MessageUtils;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.symbology.Renderer;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters;
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult;
import com.esri.arcgisruntime.tasks.geocode.LocatorTask;
import com.google.firebase.ml.vision.text.FirebaseVisionText;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainViewModel extends AndroidViewModel {
  private static final String TAG = "MainViewModel";
  /** The initial location name found by OCR */
  public static final String ATTR_OCRED_LOCATION_NAME = "OCRedLocationName";
  /** The final name for a location found by the geocoder */
  public static final String ATTR_GEOCODED_LOCATION_NAME = "ResolvedLocationName";
  public static final String ATTR_GEOCODE_SCORE = "GeocodeScore";
  public static final int MIN_GEOCODE_SCORE = 100;
  /** Tabs */
  public static final String TABID_CAMERA = "Camera Tab";
  public static final String TABID_MAP = "Map Tab";
  private String _currentTab = TABID_CAMERA;

  /** Strings found but determined not to represent a geographic location */
  private List<String> _rejectedStrings = new ArrayListCCI();
  private List<String> _underEvaluationStrings = new ArrayListCCI();
  private GraphicsOverlay _graphics = new GraphicsOverlay();

  private ArcGISMap _map = new ArcGISMap(Basemap.createTopographic());
  private Viewpoint _currentViewpoint = _map.getInitialViewpoint();
  private GeocodeParameters mGeocodeParams;
  private LocatorTask mLocator;

  public MainViewModel(@NonNull Application application) {
    super(application);
    mGeocodeParams = new GeocodeParameters();
    mGeocodeParams.setMaxResults(1);
    // minscore doesn't work online
    // mGeocodeParams.setMinScore(100);
    mLocator = new LocatorTask("https://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer");

    SimpleMarkerSymbol sms = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.parseColor("#55000000"), 12);
    Renderer rend = new SimpleRenderer(sms);
    _graphics.setRenderer(rend);
  }

  public List<String> getRejectedStrings() {
    return _rejectedStrings;
  }

  public GraphicsOverlay getFoundLocationGraphics() {
    return _graphics;
  }

  public ArcGISMap getMap() {
    return _map;
  }

  /** Add a value to the rejected strings list, even if it's already there */
  private void addRejectedString(String text) {
     _rejectedStrings.add(text);
  }

  /** Add a value to the found locations list, even if it's already there */
/*  public void addFoundLocation(FoundLocation foundLocation) {
    _foundLocations.add(foundLocation);
  }*/
  private void addFoundLocation(Graphic g) {
    _graphics.getGraphics().add(g);
  }

  public Viewpoint getCurrentViewpoint() {
    return _currentViewpoint;
  }

  public void setCurrentViewpoint(Viewpoint _currentViewpoint) {
    this._currentViewpoint = _currentViewpoint;
  }

  public String getCurrentTab() {
    return _currentTab;
  }

  public void setCurrentTab(String _currentTab) {
    this._currentTab = _currentTab;
  }

  /** Check to see whether various combinations of elements on a line geocode for a known location
   *  or specify coordinates.
   *  All calculations here are one-based instead of zero-based.
   *
   * @param line A line (one or more pieces) of recognized text
   */
  public void evaluateLocation(FirebaseVisionText.Line line) {
    int lineElements = line.getElements().size();
    // Handle various-sized continguous chunks of the entire line
    for (int sublineElements = 1; sublineElements <= lineElements; sublineElements++) {
      // Handle each contiguous chunk of size sublineElements
      for (int sublineEnd = sublineElements; sublineEnd <= lineElements; sublineEnd++) {
        // Concatenate the strings in this subline
        StringBuilder s = new StringBuilder();
        for (int zeroBasedSublineStart = sublineEnd - sublineElements; zeroBasedSublineStart < sublineEnd; zeroBasedSublineStart++) {
          if (zeroBasedSublineStart > sublineEnd - sublineElements) s.append(" ");
          s.append(line.getElements().get(zeroBasedSublineStart).getText());
        }
        // If the text was already found or rejected, ignore it
        String sLoc = s.toString();
        if (isStringRejected(sLoc) || isStringFound(sLoc) || isStringUnderEvaluation(sLoc)) return;

        _underEvaluationStrings.add(sLoc);

        // Can ArcGIS find it? If so, add it to the "found" list
        long start = System.nanoTime();
        ListenableFuture<List<GeocodeResult>> res = mLocator.geocodeAsync(sLoc, mGeocodeParams);
        res.addDoneListener(() -> {
          long elapsedMs = Math.round((System.nanoTime() - start) * 1E-6);
          _underEvaluationStrings.remove(sLoc);
          String sMsg = "Geocode for '" + sLoc + "' took " + elapsedMs + " ms - status: ";
          try {
            List<GeocodeResult> results = res.get();
            // Here's where we have to filter out low-quality matches
            if (results.size() > 0 && results.get(0).getScore() >= MIN_GEOCODE_SCORE) {
              GeocodeResult geocodeResult = results.get(0);
              Graphic g = new Graphic(geocodeResult.getDisplayLocation(),
                      geocodeResult.getAttributes());
              g.getAttributes().put(ATTR_OCRED_LOCATION_NAME, sLoc);
              g.getAttributes().put(ATTR_GEOCODED_LOCATION_NAME, geocodeResult.getLabel());
              g.getAttributes().put(ATTR_GEOCODE_SCORE, geocodeResult.getScore());
              addFoundLocation(g);
              sMsg += "succeeded" + " - score: " + geocodeResult.getScore() + " - label: '" + geocodeResult.getLabel() + "'";
            } else {
              addRejectedString(sLoc);
              sMsg += "failed";
            }
            Log.d(TAG, sMsg);
          } catch (Exception e) {
            Log.i(TAG, "Geocoding error: " + e.getLocalizedMessage(), e);
            if (e instanceof ExecutionException) { // probably a network error
              MessageUtils.showToast(getApplication(), "Error geocoding: " + e.getLocalizedMessage());
            }
          }
        });
      }
    }
  }

  private boolean isStringRejected(String sLoc) {
    return _rejectedStrings.contains(sLoc);
  }
  private boolean isStringFound(String sLoc) {
    for (Graphic g : _graphics.getGraphics()) {
      if (((String)g.getAttributes().get(ATTR_OCRED_LOCATION_NAME)).equalsIgnoreCase(sLoc))
        return true;
    }
    return false;
  }
  private boolean isStringUnderEvaluation(String sLoc) {
    return _underEvaluationStrings.contains(sLoc);
  }
}
