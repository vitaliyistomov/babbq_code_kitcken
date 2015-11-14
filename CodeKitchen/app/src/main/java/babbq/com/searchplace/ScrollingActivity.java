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
import android.view.Menu;
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

public class ScrollingActivity extends AppCompatActivity {

    private final static String TAG = ScrollingActivity.class.getSimpleName();

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

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
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
