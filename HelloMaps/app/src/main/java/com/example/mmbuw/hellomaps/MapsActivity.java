package com.example.mmbuw.hellomaps;

import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.model.VisibleRegion;

import android.content.SharedPreferences;
import android.content.Context;

import java.util.HashSet;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,LocationListener,OnMapLongClickListener,GoogleMap.OnCameraChangeListener
{
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    public static final String TAG = MapsActivity.class.getSimpleName();
    private Location location;

    private EditText infoEdith;
    private Set<String> markerCollection;
    private final static String markerKey="marker_savedKey";
    private Set<LatLng> markersPostion;
    private Set<Circle> circleCollection;

    public enum positionType
    {
        top,
        bottom ,
        left ,
        right,
        undefined,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        infoEdith = (EditText)findViewById(R.id.edittext);
        markersPostion = new HashSet<LatLng>();
        circleCollection = new HashSet<Circle>();
        setUpMapIfNeeded();
        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        else {
            handleNewLocation(location);
            setUpMap();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        mGoogleApiClient.connect();
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null && location!=null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */



    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());
    }

    private void LoadMarker(){
        SharedPreferences sharedPref = MapsActivity.this.getPreferences(Context.MODE_PRIVATE);
        markerCollection = null;
        markerCollection = sharedPref.getStringSet( markerKey, new HashSet<String>());
    }

    private void saveMarker(){
        SharedPreferences sharedPref = MapsActivity.this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.putStringSet(markerKey, markerCollection);
        editor.commit();
    }
    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    private void setUpMap() {
        markersPostion.clear();
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        markersPostion.add(latLng);
        circleCollection.add(drawCircle(latLng, 100000, Color.BLUE));

        LoadMarker();
        if (markerCollection != null)
        {
            for(String marker : markerCollection)
            {
                mMap.addMarker(paserMarker(marker));
            }
        }

        mMap.setOnMapLongClickListener(this);
        mMap.setOnCameraChangeListener(this);
    }

    private MarkerOptions paserMarker(String marker)
    {
        //String a = "lat/lng: (43.31207400066337,18.594161868095398)_Welcome Here";
        String lat = marker.substring(marker.indexOf("(")+1, marker.indexOf(","));
        String lng = marker.substring(marker.indexOf(",")+1, marker.indexOf(")"));
        double dlat = Double.parseDouble(lat);
        double dlng = Double.parseDouble(lng);
        LatLng latLng = new LatLng(dlat,dlng);
        String title = marker.substring(marker.indexOf("_")+1);
        markersPostion.add(latLng);
        circleCollection.add(drawCircle(latLng, 100000, Color.RED));

        return new MarkerOptions().position(latLng).title(title);
    }

    @Override
    public void onMapLongClick(LatLng point) {
        if (infoEdith.getText().toString().equals("")){
            infoEdith.setText("Welcome Here");
        }
        mMap.addMarker(new MarkerOptions().position(point).title(infoEdith.getText().toString()));
        markerCollection.add(point.toString() + "_" + infoEdith.getText().toString());
        saveMarker();
        markersPostion.add(point);
        circleCollection.add(drawCircle(point, 100000, Color.RED));
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        //mClusterManager.onCameraChange(cameraPosition);
        // Instantiates a new CircleOptions object and defines the center and radius
        VisibleRegion vr = this.mMap.getProjection().getVisibleRegion();
        LatLngBounds bounds = vr.latLngBounds;
        double r = 0;
        LatLng center = bounds.getCenter();
        LatLng p = new LatLng(0,0);
        for(Circle c:circleCollection) {
            if(!bounds.contains(c.getCenter())) {
                c.setVisible(true);
                positionType type = calculatePosition(c.getCenter(), bounds);

                switch (type){
                    case left:
                        p = new LatLng(c.getCenter().latitude, bounds.southwest.longitude);
                        r = distanceBetween(c.getCenter(), p);
                        break;
                    case right:
                        p = new LatLng(c.getCenter().latitude, bounds.northeast.longitude);
                        r = distanceBetween(c.getCenter(), p);
                        break;
                    case top:
                        p = new LatLng(bounds.northeast.latitude, c.getCenter().longitude);
                        r = distanceBetween(c.getCenter(), p);
                        break;
                    case bottom:
                        p = new LatLng( bounds.southwest.latitude, c.getCenter().longitude);
                        r = distanceBetween(c.getCenter(), p);
                        break;
                    default:
                        double r1 = distanceBetween(c.getCenter(), vr.farRight);
                        double r2 = distanceBetween(c.getCenter(), vr.farLeft);
                        double r3 = distanceBetween(c.getCenter(), vr.nearLeft);
                        double r4 = distanceBetween(c.getCenter(), vr.nearRight);
                        r = Math.min(Math.min(r1,r2), Math.min(r3,r4));
                        if(r == r1)
                            p = vr.farRight;
                        else if(r == r2)
                            p = vr.farLeft;
                        else if(r == r3)
                            p = vr.nearLeft;
                        else if(r == r4)
                            p = vr.nearRight;

                        break;
                }
                double rplus = distanceBetween(center, p)/5;

                c.setRadius(r + rplus);
            }
            else
            {
                c.setVisible(false);
            }
        }

    }

    private Circle drawCircle(LatLng point, int r, int color)
    {
        CircleOptions circleOptions = new CircleOptions()
                .center(point)
                .radius(r)
                .strokeColor(color); // In meters


        // Get back the mutable Circle
        Circle circle = mMap.addCircle(circleOptions);
        return circle;
    }

    private float distanceBetween(LatLng latLng1, LatLng latLng2) {

        Location loc1 = new Location(LocationManager.GPS_PROVIDER);
        Location loc2 = new Location(LocationManager.GPS_PROVIDER);

        loc1.setLatitude(latLng1.latitude);
        loc1.setLongitude(latLng1.longitude);

        loc2.setLatitude(latLng2.latitude);
        loc2.setLongitude(latLng2.longitude);


        return loc1.distanceTo(loc2);
    }

    private positionType calculatePosition(LatLng point, LatLngBounds rect)
    {
        double left = rect.southwest.longitude;
        double top = rect.northeast.latitude;
        double right = rect.northeast.longitude;
        double bottom = rect.southwest.latitude;

        double x = point.longitude;
        double y = point.latitude;

        if (left > x && ((bottom < y) && (top > y)))
        {
            return positionType.left;
        }
        else if (right < x && ((bottom < y) && (top > y)))
        {
            return positionType.right;
        }
        else if (bottom > y && ((left < x) && (right > x)))
        {
            return positionType.bottom;
        }
        else if (top < y && ((left < x) && (right > x)))
        {
            return positionType.top;
        }
        return positionType.undefined;
    }
}
