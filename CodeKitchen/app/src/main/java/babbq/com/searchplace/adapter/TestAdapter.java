package babbq.com.searchplace.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import babbq.com.searchplace.R;
import babbq.com.searchplace.model.PlaceAutocomplete;

/**
 * Created by alex on 11/14/15.
 */
public class TestAdapter extends RecyclerView.Adapter<TestAdapter.ViewHolder> {

    private List<PlaceAutocomplete> mList;
    private View.OnClickListener mListener;

    public TestAdapter(List<PlaceAutocomplete> list, View.OnClickListener listener) {
        mList = list;
        mListener = listener;
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

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.headerTextView);
            address = (TextView) itemView.findViewById(R.id.addressTextView);
        }
    }
}
