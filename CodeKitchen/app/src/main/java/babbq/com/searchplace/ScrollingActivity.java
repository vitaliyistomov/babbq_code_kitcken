package babbq.com.searchplace;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.util.ArrayList;
import java.util.Iterator;

public class ScrollingActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private final static String TAG = ScrollingActivity.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private static final int REQUEST_PLACE_PICKER = 100;
    private static final int RC_SEARCH = 0;
    private Toolbar toolbar;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_search) {
            // get the icon's location on screen to pass through to the search screen
            final View searchMenuView = toolbar.findViewById(R.id.menu_search);
            int[] loc = new int[2];
            searchMenuView.getLocationOnScreen(loc);
            startActivityForResult(SearchActivity.createStartIntent(this, loc[0], loc[0] +
                    (searchMenuView.getWidth() / 2)), RC_SEARCH, ActivityOptions
                    .makeSceneTransitionAnimation(this).toBundle());
            searchMenuView.setAlpha(0f);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();

    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");
        mLocationRequest = new LocationRequest().create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(3000); // Update location every second
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

        /*try {
            PlacePicker.IntentBuilder intentBuilder =
                    new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(this);
            // Start the intent by requesting a result,
            // identified by a request code.
            startActivityForResult(intent, REQUEST_PLACE_PICKER);

        } catch (GooglePlayServicesRepairableException e) {
            // ...
        } catch (GooglePlayServicesNotAvailableException e) {
            // ...
        }*/

        /*PendingResult result =
                Places.GeoDataApi.getAutocompletePredictions(mGoogleApiClient, query,
                        mBounds, mAutocompleteFilter);*/
        PendingResult result =
                Places.GeoDataApi.getAutocompletePredictions(mGoogleApiClient, "starbucks", null, null);
        result.setResultCallback(mUpdatePlaceDetailsCallback);

    }

    /**
     * Callback for results from a Places Geo Data API query that shows the first place result in
     * the details view on screen.
     */
    private ResultCallback<AutocompletePredictionBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<AutocompletePredictionBuffer>() {

        @Override
        public void onResult(AutocompletePredictionBuffer autocompletePredictions) {
            final Status status = autocompletePredictions.getStatus();
            if (!status.isSuccess()) {
                Toast.makeText(getApplicationContext(), "Error: " + status.toString(),
                        Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error getting place predictions: " + status
                        .toString());
                autocompletePredictions.release();
                return;
            }

            Log.i(TAG, "Query completed. Received " + autocompletePredictions.getCount()
                    + " predictions.");
            String value = "";
            Iterator<AutocompletePrediction> iterator = autocompletePredictions.iterator();
            ArrayList resultList = new ArrayList<>(autocompletePredictions.getCount());
            while (iterator.hasNext()) {
                AutocompletePrediction prediction = iterator.next();
                resultList.add(new PlaceAutocomplete(prediction.getPlaceId(),
                        prediction.getDescription()));
                if (value.equals("")) {
                    value = prediction.getDescription();
                    PendingResult result =
                            Places.GeoDataApi.getPlaceById(mGoogleApiClient, prediction.getPlaceId());
                    result.setResultCallback(mCoordinatePlaceDetailsCallback);

                }
                Log.d(TAG, "Place - Name:" + prediction.getDescription());

            }
            Log.d(TAG, "Result list size:" + resultList.size());
            if (resultList.size() > 0) {
                //Uri gmmIntentUri = Uri.parse("geo:0,0?q=1600 Amphitheatre Parkway, Mountain+View, California");

            }
            // Buffer release
            autocompletePredictions.release();
        }
    };

    private ResultCallback<PlaceBuffer> mCoordinatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {

        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                Log.e(TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }
            // Get the Place object from the buffer.
            final Place place = places.get(0);
            //Uri gmmIntentUri = Uri.parse("geo:"+place.getLatLng().latitude+","+place.getLatLng().longitude);
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + place.getLatLng().latitude + "," + place.getLatLng().longitude);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
            Log.d(TAG, "PlaceBuffer.onResult" + place.getLatLng().toString());
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_SEARCH:
                // reset the search icon which we hid
                View searchMenuView = toolbar.findViewById(R.id.menu_search);
                if (searchMenuView != null) {
                    searchMenuView.setAlpha(1f);
                }
                break;
            case REQUEST_PLACE_PICKER:
                if (resultCode == Activity.RESULT_OK) {

                    // The user has selected a place. Extract the name and address.
                    final Place place = PlacePicker.getPlace(data, this);

                    final CharSequence name = place.getName();
                    final CharSequence address = place.getAddress();
                    String attributions = PlacePicker.getAttributions(data);
                    if (attributions == null) {
                        attributions = "";
                    }

                    Toast.makeText(getApplicationContext(), "Name:" + name + "; address: " + address, Toast.LENGTH_SHORT).show();
                    //mViewName.setText(name);
                    //mViewAddress.setText(address);
                    //mViewAttributions.setText(Html.fromHtml(attributions));

                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged (" + location.getLatitude() + ";" + location.getLongitude() + ")");
    }

    class PlaceAutocomplete {

        public CharSequence placeId;
        public CharSequence description;

        PlaceAutocomplete(CharSequence placeId, CharSequence description) {
            this.placeId = placeId;
            this.description = description;
        }

        @Override
        public String toString() {
            return description.toString();
        }
    }
}
