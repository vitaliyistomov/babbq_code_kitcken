package babbq.com.searchplace.adapter;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;

import java.util.List;

import babbq.com.searchplace.R;
import babbq.com.searchplace.model.PlaceAutocomplete;

/**
 * Created by alex on 11/14/15.
 */
public class TestAdapter extends RecyclerView.Adapter<TestAdapter.ViewHolder> {

    private List<PlaceAutocomplete> mList;
    private View.OnClickListener mListener;
    private GoogleApiClient mApiClient;

    public TestAdapter(List<PlaceAutocomplete> list, View.OnClickListener listener, GoogleApiClient client) {
        mList = list;
        mListener = listener;
        mApiClient = client;
    }

    public void setList(List<PlaceAutocomplete> list) {
        mList = list;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_card,
                parent,
                false);
        mView.setOnClickListener(mListener);
        return new ViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String name;
        String address;

        int separateIndex = mList.get(position).description.toString().indexOf(",");
        name = mList.get(position).description.toString().substring(0, separateIndex);
        address = mList.get(position).description.toString().substring(separateIndex + 2, mList.get(position).description.toString().length());

        holder.name.setText(name);
        holder.address.setText(address);

        new AsyncTask<Void, Void, Void>() {

            Bitmap image = null;

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    //if (mList.get(position).bitmap != null)
                    //holder.image.setImageBitmap(mList.get(position).bitmap);
                    PlacePhotoMetadataBuffer photoMetadataBuffer = null;
                    // Get a PlacePhotoMetadataResult containing metadata for the first 10 photos.
                    PlacePhotoMetadataResult result = Places.GeoDataApi
                            .getPlacePhotos(mApiClient, (String) mList.get(position).placeId).await();
                    // Get a PhotoMetadataBuffer instance containing a list of photos (PhotoMetadata).
                    if (result != null && result.getStatus().isSuccess()) {
                        photoMetadataBuffer = result.getPhotoMetadata();
                    }
                    if (photoMetadataBuffer != null && photoMetadataBuffer.get(0) != null) {
                        // Get the first photo in the list.
                        PlacePhotoMetadata photo = photoMetadataBuffer.get(0);
                        // Get a full-size bitmap for the photo.
                        image = photo.getPhoto(mApiClient).await()
                                .getBitmap();
                    }
                }catch (Exception ex) {

                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (image != null)
                    holder.image.setImageBitmap(image);
            }
        }.execute();

    }

    @Override
    public int getItemCount() {
        if (mList != null)
            return mList.size();
        else
            return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private TextView address;
        private ImageView image;

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.headerTextView);
            address = (TextView) itemView.findViewById(R.id.addressTextView);
            image = (ImageView) itemView.findViewById(R.id.imageView);
        }
    }
}
