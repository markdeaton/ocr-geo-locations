// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.esri.apl.ocrLocations;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.esri.apl.ocrLocations.model.FoundLocationClickListener;
import com.esri.apl.ocrLocations.textrecognition.CameraSource;
import com.esri.apl.ocrLocations.textrecognition.CameraSourcePreview;
import com.esri.apl.ocrLocations.textrecognition.TextRecognitionProcessor;
import com.esri.apl.ocrLocations.viewmodel.MainViewModel;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.google.android.gms.common.annotation.KeepName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.esri.apl.ocrLocations.viewmodel.MainViewModel.ATTR_GEOCODED_LOCATION_NAME;
import static com.esri.apl.ocrLocations.viewmodel.MainViewModel.ATTR_GEOCODE_SCORE;
import static com.esri.apl.ocrLocations.viewmodel.MainViewModel.ATTR_OCRED_LOCATION_NAME;
import static com.esri.apl.ocrLocations.viewmodel.MainViewModel.TABID_CAMERA;
import static com.esri.apl.ocrLocations.viewmodel.MainViewModel.TABID_MAP;

/** Demo app showing the various features of ML Kit for Firebase. This class is used to
 * set up continuous frame processing on frames from a camera source. */
@KeepName
public final class LivePreviewActivity extends AppCompatActivity
    implements OnRequestPermissionsResultCallback, CompoundButton.OnCheckedChangeListener {
  private static final String TAG = "LivePreviewActivity";
  private static final int PERMISSION_REQUESTS = 1;

  public MainViewModel mainViewModel;

  private CameraSource cameraSource = null;
  private CameraSourcePreview preview;
  private GraphicOverlay graphicOverlay;

  private MapView mapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate");

    mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);

    setContentView(R.layout.activity_live_preview);

    // Camera view
    preview = (CameraSourcePreview) findViewById(R.id.firePreview);
    if (preview == null)
      Log.d(TAG, "Preview is null");
    else {
//      preview.getSurfaceView().setZOrderMediaOverlay(true);
//      preview.getSurfaceView().setZOrderOnTop(true);
    }

    graphicOverlay = (GraphicOverlay) findViewById(R.id.fireFaceOverlay);
    if (graphicOverlay == null) {
      Log.d(TAG, "graphicOverlay is null");
    }

    // Set up map
    mapView = (MapView)findViewById(R.id.map);
//    mapView.setZOrderMediaOverlay(false);
    mapView.setMap(mainViewModel.getMap());
    mapView.getGraphicsOverlays().add(mainViewModel.getFoundLocationGraphics());

    ToggleButton facingSwitch = (ToggleButton) findViewById(R.id.facingswitch);
    facingSwitch.setOnCheckedChangeListener(this);

    if (allPermissionsGranted()) {
      createCameraSource();
    } else {
      getRuntimePermissions();
    }

    // Set up list view
    RecyclerView lstFoundLocations = (RecyclerView) findViewById(R.id.lstLocations);
    lstFoundLocations.setAdapter(new FoundLocationsListAdapter(
            mainViewModel.getFoundLocationGraphics().getGraphics(), mListItemClicked));

    // Set up tabs
    TabHost tabhost = (TabHost)findViewById(R.id.tabHost);
    tabhost.setup();
    TabHost.TabSpec tsCam = tabhost.newTabSpec(TABID_CAMERA).setIndicator("Camera")
            .setContent(R.id.tabcontentCamera);
    tabhost.addTab(tsCam);
    TabHost.TabSpec tsMap = tabhost.newTabSpec(TABID_MAP).setIndicator("Map")
            .setContent(R.id.tabcontentMap);
    tabhost.addTab(tsMap);
    tabhost.setOnTabChangedListener(mOnTabChanged);
    tabhost.setCurrentTabByTag(mainViewModel.getCurrentTab());
  }

  TabHost.OnTabChangeListener mOnTabChanged = new TabHost.OnTabChangeListener() {
    @Override
    public void onTabChanged(String tabId) {
      mainViewModel.setCurrentTab(tabId);
      switch (tabId) {
        case TABID_CAMERA:
//          mapView.pause();
          mapView.setVisibility(View.INVISIBLE);
          preview.getSurfaceView().setVisibility(View.VISIBLE);
          startCameraSource();
          break;
        case TABID_MAP:
          preview.stop();
          preview.getSurfaceView().setVisibility(View.INVISIBLE);
          mapView.setVisibility(View.VISIBLE);
//          mapView.resume();
          break;
      }
    }
  };

  private FoundLocationClickListener mListItemClicked = new FoundLocationClickListener() {
    @Override
    public void itemClicked(Graphic g) {
      Point pt = (Point)g.getGeometry();

      TextView tv = (TextView)getLayoutInflater().inflate(R.layout.callout, mapView, false);
      Callout callout = mapView.getCallout();
      callout.setLocation((Point)g.getGeometry());
      tv.setText(getString(R.string.callout,
              g.getAttributes().get(ATTR_OCRED_LOCATION_NAME.toString()),
              g.getAttributes().get(ATTR_GEOCODED_LOCATION_NAME).toString(),
              (double)g.getAttributes().get(ATTR_GEOCODE_SCORE)));
      callout.setContent(tv);
      callout.show();

      mapView.setViewpointCenterAsync(pt);
    }
  };

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    Log.d(TAG, "Set facing");
    if (cameraSource != null) {
      if (isChecked) {
        cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
      } else {
        cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
      }
    }
    preview.stop();
    startCameraSource();
  }

  private void createCameraSource() {
    // If there's no existing cameraSource, create one.
    if (cameraSource == null) {
      cameraSource = new CameraSource(this, graphicOverlay);
    }

    Log.i(TAG, "Using Text Detector Processor");
    cameraSource.setMachineLearningFrameProcessor(new TextRecognitionProcessor(mainViewModel));
  }

  /**
   * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
   * (e.g., because onResume was called before the camera source was created), this will be called
   * again when the camera source is created.
   */
  private void startCameraSource() {
    if (cameraSource != null) {
      try {
        if (preview == null) {
          Log.d(TAG, "resume: Preview is null");
        }
        if (graphicOverlay == null) {
          Log.d(TAG, "resume: graphOverlay is null");
        }
        preview.start(cameraSource, graphicOverlay);
      } catch (IOException e) {
        Log.e(TAG, "Unable to start camera source.", e);
        cameraSource.release();
        cameraSource = null;
      }
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    Log.d(TAG, "onResume");
    startCameraSource();
    mapView.resume();
  }

  /** Stops the camera. */
  @Override
  protected void onPause() {
    super.onPause();
    preview.stop();
    mapView.pause();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (cameraSource != null) {
      cameraSource.release();
    }
    // Required so we don't get an error upon rotation
    mapView.getGraphicsOverlays().clear();
    mapView.dispose();
  }

  private String[] getRequiredPermissions() {
    try {
      PackageInfo info =
          this.getPackageManager()
              .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
      String[] ps = info.requestedPermissions;
      if (ps != null && ps.length > 0) {
        return ps;
      } else {
        return new String[0];
      }
    } catch (Exception e) {
      return new String[0];
    }
  }

  private boolean allPermissionsGranted() {
    for (String permission : getRequiredPermissions()) {
      if (!isPermissionGranted(this, permission)) {
        return false;
      }
    }
    return true;
  }

  private void getRuntimePermissions() {
    List<String> allNeededPermissions = new ArrayList<>();
    for (String permission : getRequiredPermissions()) {
      if (!isPermissionGranted(this, permission)) {
        allNeededPermissions.add(permission);
      }
    }

    if (!allNeededPermissions.isEmpty()) {
      ActivityCompat.requestPermissions(
          this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
    }
  }

  @Override
  public void onRequestPermissionsResult(
          int requestCode, String[] permissions, int[] grantResults) {
    Log.i(TAG, "Permission granted!");
    if (allPermissionsGranted()) {
      createCameraSource();
    }
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  private static boolean isPermissionGranted(Context context, String permission) {
    if (ContextCompat.checkSelfPermission(context, permission)
        == PackageManager.PERMISSION_GRANTED) {
      Log.i(TAG, "Permission granted: " + permission);
      return true;
    }
    Log.i(TAG, "Permission NOT granted: " + permission);
    return false;
  }
}
