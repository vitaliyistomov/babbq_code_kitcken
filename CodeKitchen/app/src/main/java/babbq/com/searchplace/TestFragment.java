package babbq.com.searchplace;

import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import babbq.com.searchplace.adapter.TestAdapter;
import babbq.com.searchplace.model.PlaceAutocomplete;

/**
 * Created by alex on 11/14/15.
 */
public class TestFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private final static String TAG = ScrollingActivity.class.getSimpleName();

    private EditText mSearch;
    private Button mButton;

    private RecyclerView mRecyclerView;
    private TestAdapter mAdapter;

    private ArrayList<PlaceAutocomplete> resultList;

    private GoogleApiClient mGoogleApiClient;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        return inflater.inflate(R.layout.test_fragment, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        mSearch = (EditText) getActivity().findViewById(R.id.text);
        mButton = (Button) getActivity().findViewById(R.id.button);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PendingResult result =
                        Places.GeoDataApi.getAutocompletePredictions(mGoogleApiClient, mSearch.getText().toString(), null, null);
                result.setResultCallback(mUpdatePlaceDetailsCallback);
            }
        });

        mAdapter = new TestAdapter(null, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = mRecyclerView.getChildLayoutPosition(v);
                //Toast.makeText(getActivity(), "#" + position, Toast.LENGTH_SHORT).show();
                PendingResult result =
                        Places.GeoDataApi.getPlaceById(mGoogleApiClient, String.valueOf(resultList.get(position).placeId));
                result.setResultCallback(mCoordinatePlaceDetailsCallback);
            }
        }, mGoogleApiClient);
        mRecyclerView = (RecyclerView) getActivity().findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");
    }

    private ResultCallback<AutocompletePredictionBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<AutocompletePredictionBuffer>() {
        @Override
        public void onResult(AutocompletePredictionBuffer autocompletePredictions) {


            final Status status = autocompletePredictions.getStatus();
            if (!status.isSuccess()) {
                Toast.makeText(getContext(), "Error: " + status.toString(),
                        Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error getting place predictions: " + status
                        .toString());
                autocompletePredictions.release();
                return ;
            }

            Log.i(TAG, "Query completed. Received " + autocompletePredictions.getCount()
                    + " predictions.");
            String value = "";
            Bitmap image = null;
            Iterator<AutocompletePrediction> iterator = autocompletePredictions.iterator();
            resultList = new ArrayList<>(autocompletePredictions.getCount());
            while (iterator.hasNext()) {

                AutocompletePrediction prediction = iterator.next();

//                PlacePhotoMetadataBuffer photoMetadataBuffer = null;
//                // Get a PlacePhotoMetadataResult containing metadata for the first 10 photos.
//                PlacePhotoMetadataResult result = Places.GeoDataApi
//                        .getPlacePhotos(mGoogleApiClient, prediction.getPlaceId()).await();
//                // Get a PhotoMetadataBuffer instance containing a list of photos (PhotoMetadata).
//                if (result != null && result.getStatus().isSuccess()) {
//                    photoMetadataBuffer = result.getPhotoMetadata();
//                }
//                if (photoMetadataBuffer != null) {
//                    // Get the first photo in the list.
//                    PlacePhotoMetadata photo = photoMetadataBuffer.get(0);
//                    // Get a full-size bitmap for the photo.
//                    image = photo.getPhoto(mGoogleApiClient).await()
//                            .getBitmap();
//                }



                resultList.add(new PlaceAutocomplete(prediction.getPlaceId(),
                        prediction.getDescription(), image));
                /*if (value.equals("")) {
                    value = prediction.getDescription();
                    PendingResult result =
                            Places.GeoDataApi.getPlaceById(mGoogleApiClient, prediction.getPlaceId());
                    result.setResultCallback(mCoordinatePlaceDetailsCallback);
                }*/
                Log.d(TAG, "Place - Name:" + prediction.getDescription());

            }
            mAdapter.setList(resultList);
            Log.d(TAG, "Result list size:"+ resultList.size());
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
            Uri gmmIntentUri = Uri.parse("google.navigation:q="+place.getLatLng().latitude+","+place.getLatLng().longitude);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
            Log.d(TAG, "PlaceBuffer.onResult" + place.getLatLng().toString());
        }
    };

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
