package com.esri.apl.ocrLocations.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.support.annotation.NonNull;
import android.util.Log;

import com.esri.apl.ocrLocations.model.FoundLocation;
import com.esri.apl.ocrLocations.util.MessageUtils;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters;
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult;
import com.esri.arcgisruntime.tasks.geocode.LocatorTask;
import com.google.firebase.ml.vision.text.FirebaseVisionText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainViewModel extends AndroidViewModel {
  private static final String TAG = "MainViewModel";

  /** Strings found but determined not to represent a geographic location */
  private ObservableList<String> _rejectedStrings = new ObservableArrayList<>();
  private ObservableList<FoundLocation> _foundLocations = new ObservableArrayList<>();
  private List<String> _underEvaluationStrings = new ArrayList<>();

  private GeocodeParameters mGeocodeParams;
  private LocatorTask mLocator;

  public MainViewModel(@NonNull Application application) {
    super(application);
    mGeocodeParams = new GeocodeParameters();
    mGeocodeParams.setMaxResults(1); // minscore doesn't work online mGeocodeParams.setMinScore(100);
    mLocator = new LocatorTask("https://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer");
  }

  public ObservableList<String> getRejectedStrings() {
    return _rejectedStrings;
  }

  public ObservableList<FoundLocation> getFoundLocations() {
    return _foundLocations;
  }

  /** Add a value to the rejected strings list, even if it's not already there */
  public void addRejectedString(String text) {
     _rejectedStrings.add(text);
  }

  /** Add a value to the found locations list, even if it's not already there */
  public void addFoundLocation(FoundLocation foundLocation) {
    _foundLocations.add(foundLocation);
  }

  /** Check to see whether an element geocodes for known location or specifies coordinates
   * @param element A single piece of recognized text
   */
//  public void evaluateLocation(FirebaseVisionText.Element element) {
//
//  }

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
            if (results.size() > 0 && results.get(0).getScore() >= 100) {
              GeocodeResult geocodeResult = results.get(0);
              Graphic g = new Graphic(geocodeResult.getDisplayLocation(),
                      geocodeResult.getAttributes());
              FoundLocation fl = new FoundLocation(sLoc, g);
              fl.setScore(geocodeResult.getScore());
              addFoundLocation(fl);
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
/*    for (int iElement = 0; iElement < lineElements; iElement++) {
      FirebaseVisionText.Element element = line.getElements().get(iElement);
      addFoundLocation(new FoundLocation(element.getText(), null));
    }*/
  }

  private boolean isStringRejected(String sLoc) {
    return _rejectedStrings.contains(sLoc);
  }
  private boolean isStringFound(String sLoc) {
    // Create a new, fake FoundLocation just to see if it already exists.
    // This works because FoundLocation.equals() only compares the text portion, not the graphic.
    return _foundLocations.contains(new FoundLocation(sLoc, null));
  }
  private boolean isStringUnderEvaluation(String sLoc) {
    return _underEvaluationStrings.contains(sLoc);
  }
}
