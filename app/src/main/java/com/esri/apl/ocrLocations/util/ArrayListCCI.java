package com.esri.apl.ocrLocations.util;

import java.util.ArrayList;

/**
 * The only way this differs from a normal ArrayList is that its contains()
 * method compares strings insensitive to case.
 */
public class ArrayListCCI extends ArrayList<String> {
  @Override
  public boolean contains(Object o) {
    String sVal = (String)o;
    for (String s : this) {
      if (sVal.equalsIgnoreCase(s)) return true;
    }
    return false;
  }
}
