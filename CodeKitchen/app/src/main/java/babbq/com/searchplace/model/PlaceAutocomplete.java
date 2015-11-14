package babbq.com.searchplace.model;

import android.graphics.Bitmap;

public class PlaceAutocomplete {

        public CharSequence placeId;
        public CharSequence description;
        public Bitmap bitmap;

        public PlaceAutocomplete(CharSequence placeId, CharSequence description, Bitmap bitmap) {
            this.placeId = placeId;
            this.description = description;
            this.bitmap = bitmap;
        }



        @Override
        public String toString() {
            return description.toString();
        }
    }