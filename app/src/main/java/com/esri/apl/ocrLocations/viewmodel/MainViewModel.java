package com.esri.apl.ocrLocations.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.databinding.ObservableArrayList;
import android.support.annotation.NonNull;

import com.esri.apl.ocrLocations.model.FoundLocation;

public class MainViewModel extends AndroidViewModel {
  /** Strings found but determined not to represent a geographic location */
  private ObservableArrayList<String> _rejectedStrings = new ObservableArrayList<>();
  private MutableLiveData<ObservableArrayList<FoundLocation>> _foundLocations = new MutableLiveData<>();

  public MainViewModel(@NonNull Application application) {
    super(application);
    _foundLocations.setValue(new ObservableArrayList<>());
  }

  public ObservableArrayList<String> getRejectedStrings() {
    return _rejectedStrings;
  }

  public LiveData<ObservableArrayList<FoundLocation>> getFoundLocations() {
    return _foundLocations;
  }

  /** Add a value to the rejected strings list, only if it's not already there */
  public void addRejectedString(String text) {
    if (!_rejectedStrings.contains(text)) _rejectedStrings.add(text);
  }

  /** Add a value to the found locations list, only if it's not already there */
  public void addFoundLocation(FoundLocation foundLocation) {
    if (!_foundLocations.getValue().contains(foundLocation)) _foundLocations.getValue().add(foundLocation);
    _foundLocations.postValue(_foundLocations.getValue());
  }

  public boolean rejectedStringExists(String text) {
    return _rejectedStrings.contains(text);
  }
}
