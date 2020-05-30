package edu.harvard.cs50.mapsplacesapp.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.rtchagas.pingplacepicker.PingPlacePicker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.harvard.cs50.mapsplacesapp.R;
import edu.harvard.cs50.mapsplacesapp.adapters.CustomInfoWindowAdapter;
import edu.harvard.cs50.mapsplacesapp.adapters.PlaceAutocompleteAdapter;
import edu.harvard.cs50.mapsplacesapp.model.PlaceInfo;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MapActivity";

    private static final String COARSE_LOCATION =  Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final String FINE_LOCATION =  Manifest.permission.ACCESS_FINE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final int PLACE_PICKER_REQUEST = 1;
    private static final float DEFAULT_ZOOM = 15f;

    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40, -168), new LatLng(71, 136));

    private FusedLocationProviderClient mFusedLocationProviderClient;

    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;

    private Boolean mLocationPermissionGranted = false;

    private AutoCompleteTextView mSearchText;

    private PlacesClient mPlacesClient;

    private ImageView mGps, mInfo, mPlacePicker;
    private GoogleMap mMap;

    private PlaceInfo mPlace;
    private Marker mMarker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // make the activitry on full screen
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getSupportActionBar().hide();
        setContentView(R.layout.activity_map);

        mSearchText = findViewById(R.id.input_search);

        mGps = findViewById(R.id.ic_gps);
        mInfo = findViewById(R.id.place_info);
        mPlacePicker = findViewById(R.id.place_picker);

        getLocationPermission();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void init() {
        Log.d(TAG, "init: initializing");

        // Initialize the SDK
        Places.initialize(
                getApplicationContext(),
                getResources().getString(R.string.google_maps_API_key)
        );

        // Create a new Places client instance
        mPlacesClient = Places.createClient(this);

        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(
                getApplicationContext(), mPlacesClient, LAT_LNG_BOUNDS
        );

        mSearchText.setAdapter(mPlaceAutocompleteAdapter);

        mSearchText.setOnItemClickListener(mAutoCompleteClickListener);

        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || event.getAction() == KeyEvent.ACTION_DOWN
                    || event.getAction() == KeyEvent.KEYCODE_ENTER) {
                    // execute searching
                    geoLocate();
                }
                return false;
            }
        });

        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked gps icon");
                getDeviceLocation();
            }
        });

        //https://github.com/rtchagas/pingplacepicker
        //
        mPlacePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PingPlacePicker.IntentBuilder builder = new PingPlacePicker.IntentBuilder();
                builder.setAndroidApiKey(getResources().getString(R.string.google_places_API_key));

                try {
                    startActivityForResult(
                            builder.build(MapActivity.this), PLACE_PICKER_REQUEST);
                } catch (Exception e) {
                    Log.e(TAG,
                            "onClick: GooglePlayServicesRepairableException: " +
                                    e.getMessage());
                }
            }
        });

        hideSoftKeyboard();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = (Place) PingPlacePicker.getPlace(data);

                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest
                        .newInstance(place.getId(),
                                Arrays.asList(
                                        Place.Field.ID,
                                        Place.Field.NAME,
                                        Place.Field.LAT_LNG,
                                        Place.Field.ADDRESS,
                                        Place.Field.VIEWPORT,
                                        Place.Field.WEBSITE_URI,
                                        Place.Field.PHONE_NUMBER,
                                        Place.Field.RATING
                                ));

                mPlacesClient.fetchPlace(fetchPlaceRequest)
                        .addOnSuccessListener(updatePlaceDetails)
                        .addOnFailureListener(onFailureListener);
            }
        }
    }

    private void geoLocate() {
        Log.d(TAG, "geoLocate: geolocating");

        String searchString = mSearchText.getText().toString();

        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString, 1);
        } catch (IOException e) {
            Log.e(TAG, "geoLocate: IOException" + e.getMessage());
        }

        if (list.size() > 0) {
            Address address = list.get(0);

            Log.d(TAG, "geoLocate: found a location: " + address.toString());

            moveCamera(
                    new LatLng(address.getLatitude(), address.getLongitude()),
                    DEFAULT_ZOOM,
                    address.getAddressLine(0));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onReady: map is ready");
        mMap = googleMap;

        if (mLocationPermissionGranted) {
            getDeviceLocation();

            if (ActivityCompat
                .checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                                PackageManager.PERMISSION_GRANTED) {
                return;
            }

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionResult: called");
        mLocationPermissionGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0 ; i < grantResults.length ; i++ ) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted ");
                    mLocationPermissionGranted = true;
                    // initialize our map
                    initMap();
                    init();
                }
            }
        }
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");
        mFusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionGranted) {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location!");
                            final Location currentLocation = (Location) task.getResult();

                            try {
                                // must wait half a second to prevent NullPointerException at
                                //  .. currentLocation = task.getResult()
                                Thread.sleep(500L);
                            } catch (InterruptedException e) {
                                Log.d(TAG, "await(location) has thrown exception: " +
                                        e.toString());
                            }

                            moveCamera(
                                new LatLng(
                                    currentLocation.getLatitude(),
                                    currentLocation.getLongitude()
                                ),
                                DEFAULT_ZOOM,
                            "My Location"
                            );

                            mSearchText.setText("");
                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapActivity.this,
                                "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: ");
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title) {
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude +
                ", lng: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if (!title.equals("My Location")) {
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.addMarker(options);
        }

        hideSoftKeyboard();
    }

    private void moveCamera(LatLng latLng, float zoom, PlaceInfo mPlace) {
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude +
                ", lng: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        mMap.clear();

        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapActivity.this));

        if (mPlace!=null) {
            try {

                final StringBuilder snippedBuilder = new StringBuilder();
                if (mPlace.getAddress() != null) {
                    snippedBuilder.append("Address: " + mPlace.getAddress() + "\n");
                }
                if (mPlace.getPhoneNumber() != null) {
                    snippedBuilder.append("Phone Number: " + mPlace.getPhoneNumber() + "\n");
                }
                if (mPlace.getWebsiteUri() != null) {
                    snippedBuilder.append("Website: " + mPlace.getWebsiteUri() + "\n");
                }
                if (mPlace.getRating() != 0) {
                    snippedBuilder.append("Price Rating: " + mPlace.getRating() + "\n");
                }

                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .title(mPlace.getName())
                        .snippet(snippedBuilder.toString());

                mMarker = mMap.addMarker(options);

            } catch (NullPointerException e) {
                Log.e(TAG, "moveCamera: NullPointerException: " + e.getMessage() );
            }
        } else {
            Log.i(TAG, "The place information is not found.");
            mMap.addMarker(new MarkerOptions().position(latLng));
        }

        mInfo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked place info");

                try {
                    if (mMarker.isInfoWindowShown()) {
                        mMarker.hideInfoWindow();
                    } else {
                        Log.d(TAG, "onClick: place info: " + mPlace.toString());
                        mMarker.showInfoWindow();
                    }
                } catch (NullPointerException e) {
                    Log.e(TAG, "onClick: NullPointerException: " + e.getMessage());
                }
            }
        });

        hideSoftKeyboard();
    }

    private void initMap() {
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(MapActivity.this);
    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location");
        String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initMap();
                init();
            } else {
                ActivityCompat.requestPermissions(
                        this, permission, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(
                    this, permission, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getApplicationContext()
                .getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSearchText.getWindowToken(), 0);
    }

    /*
     --------------------- google places API autocomplete suggestions ---------------------------
     */

    private AdapterView.OnItemClickListener mAutoCompleteClickListener =
            new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            hideSoftKeyboard();

            final AutocompletePrediction item = mPlaceAutocompleteAdapter.getItem(position);
            final String placeId = item.getPlaceId();

            FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest
                    .newInstance(placeId,
                            Arrays.asList(
                                    Place.Field.ID,
                                    Place.Field.NAME,
                                    Place.Field.LAT_LNG,
                                    Place.Field.ADDRESS,
                                    Place.Field.VIEWPORT,
                                    Place.Field.WEBSITE_URI,
                                    Place.Field.PHONE_NUMBER,
                                    Place.Field.RATING
                            ));

            mPlacesClient.fetchPlace(fetchPlaceRequest)
                    .addOnSuccessListener(updatePlaceDetails)
                    .addOnFailureListener(onFailureListener);
        }
    };

    private OnSuccessListener updatePlaceDetails = new OnSuccessListener() {
        @Override
        public void onSuccess(Object o) {
            FetchPlaceResponse result = (FetchPlaceResponse) o;

            try {
                Place place  = result.getPlace();

                mPlace = new PlaceInfo();
                if (place.getName() != null) {
                    mPlace.setName(place.getName());
                }
                if (place.getRating() != null) {
                    mPlace.setRating(place.getRating().floatValue());
                }
                if (place.getLatLng() != null) {
                    mPlace.setLatLng(place.getLatLng());
                }
                if (place.getAddress() != null) {
                    mPlace.setAddress(place.getAddress());
                }
                if (place.getWebsiteUri() != null) {
                    mPlace.setWebsiteUri(place.getWebsiteUri());
                }
                if (place.getPhoneNumber() != null) {
                    mPlace.setPhoneNumber(place.getPhoneNumber());
                }

                Log.i(TAG, "Query completed. Received place : " +
                        mPlace.toString() + " .");

                moveCamera(mPlace.getLatLng(), DEFAULT_ZOOM, mPlace);

            } catch (RuntimeExecutionException e) {
                // If the query did not complete successfully return null
                Toast.makeText(getApplicationContext(), "Error contacting API: " + e.toString(),
                        Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Place query did not complete successfully. Error :", e);
            }
        }
    };

    private OnFailureListener onFailureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            Log.d(TAG, "Place query did not complete successfully: " + e.getMessage());
        }
    };

}
