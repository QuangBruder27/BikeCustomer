package com.quangbruder.bikecustomer.ui.history;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.quangbruder.bikecustomer.data.model.Booking;
import com.quangbruder.bikecustomer.databinding.FragmentHistoryBinding;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Booking}.
 * TODO: Replace the implementation with code for your data type.
 */
public class BookingListAdapter extends RecyclerView.Adapter<BookingListAdapter.ViewHolder> {

    private final List<Booking> mValues;

    public BookingListAdapter(List<Booking> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ViewHolder(FragmentHistoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mBikeId.setText(mValues.get(position).bikeId);
        if (mValues.get(position).beginTime !=null) holder.mBeginTime.setText(mValues.get(position).beginTime);
        if (mValues.get(position).endTime!=null) holder.mEndTime.setText(mValues.get(position).endTime);
        if (mValues.get(position).distance!=null) holder.mDistance.setText(mValues.get(position).distance+" km");
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mBikeId;
        public final TextView mEndTime;
        public final TextView mBeginTime;
        public final TextView mDistance;

        public Booking mItem;

        public ViewHolder(FragmentHistoryBinding binding) {
            super(binding.getRoot());
            mBikeId = binding.bikeId;
            mBeginTime = binding.beginTime;
            mEndTime = binding.endTime;
            mDistance = binding.distance;
        }

        @Override
        public String toString() {
            return super.toString() + " '";
        }
    }
}