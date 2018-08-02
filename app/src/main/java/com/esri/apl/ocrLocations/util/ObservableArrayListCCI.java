package com.esri.apl.ocrLocations.util;

import android.databinding.ObservableArrayList;

/**
 * The only way this differs from a normal ObservableArrayList is that its contains()
 * method compares strings insensitive to case.
 */
public class ObservableArrayListCCI extends ObservableArrayList<String> {
  @Override
  public boolean contains(Object o) {
    String sVal = (String)o;
    for (String s : this) {
      if (sVal.equalsIgnoreCase(s)) return true;
    }
    return false;
  }
}
