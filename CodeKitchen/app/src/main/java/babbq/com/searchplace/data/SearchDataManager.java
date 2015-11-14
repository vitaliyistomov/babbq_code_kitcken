/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package babbq.com.searchplace.data;

import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import babbq.com.searchplace.model.PlaceAutocomplete;

/**
 * Responsible for loading search results from dribbble and designer news. Instantiating classes are
 * responsible for providing the {code onDataLoaded} method to do something with the data.
 */
public abstract class SearchDataManager implements DataLoadingSubject {

    private final static String TAG = SearchDataManager.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;
    private ResultCallback<PlaceBuffer> mCoordinatePlaceDetailsCallback;

    public SearchDataManager(GoogleApiClient mGoogleApiClient, ResultCallback<PlaceBuffer> mCoordinatePlaceDetailsCallback) {
        this.mGoogleApiClient = mGoogleApiClient;
        this.mCoordinatePlaceDetailsCallback = mCoordinatePlaceDetailsCallback;
    }

    // state
    private String query = "";
    private boolean loadingSearchResults = false;
    private int page = 1;

    @Override
    public boolean isDataLoading() {
        return loadingSearchResults;
    }

    public void searchFor(String query) {
        if (!this.query.equals(query)) {
            clear();
            this.query = query;
        } else {
            page++;
        }
        searchLocations(query, page);
    }

    public void loadMore() {
        searchFor(query);
    }

    public abstract void onDataLoaded(List<? extends PlaceAutocomplete> data);

    public void clear() {
        query = "";
        page = 1;
        loadingSearchResults = false;
    }

    public String getQuery() {
        return query;
    }

    private void searchLocations(final String query, final int resultsPage) {
        loadingSearchResults = true;

        PendingResult result =
                Places.GeoDataApi.getAutocompletePredictions(mGoogleApiClient, query, null, null);
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
                Toast.makeText(mGoogleApiClient.getContext(), "Error: " + status.toString(),
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
            List<PlaceAutocomplete> resultList = new ArrayList<>(autocompletePredictions.getCount());
            while (iterator.hasNext()) {
                AutocompletePrediction prediction = iterator.next();
                resultList.add(new PlaceAutocomplete(prediction.getPlaceId(),
                        prediction.getDescription(), null));
//                if (value.equals("")) {
//                    value = prediction.getDescription();
//                    PendingResult result =
//                            Places.GeoDataApi.getPlaceById(mGoogleApiClient, prediction.getPlaceId());
//                    result.setResultCallback(mCoordinatePlaceDetailsCallback);
//
//                }
                Log.d(TAG, "Place - Name:" + prediction.getDescription());

            }
            Log.d(TAG, "Result list size:" + resultList.size());
            if (resultList.size() > 0) {
                //Uri gmmIntentUri = Uri.parse("geo:0,0?q=1600 Amphitheatre Parkway, Mountain+View, California");

            }

            onDataLoaded(resultList);
            // Buffer release
            autocompletePredictions.release();
        }
    };
}
