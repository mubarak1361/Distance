package com.testpoc.distance;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

/**
 * Created by mubarakmohideen on 10/03/17.
 */

public class PlacesListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnPlaceListAdapterListener{
        public void onItemClick(int positon);
    }

    private List<String> places = Arrays.asList("Traders Hotel, Kuala Lumpur",
            "Villa Samadhi Kuala Lumpur by Samadhi","Grand Hyatt Kuala Lumpur",
            "The Saujana Hotel Kuala Lumpur","Villa Samadhi Kuala Lumpur","Georgetown Historic City",
            "Sunway Lagoon Theme Park","Semenggoh Nature Reserve","Royal Selangor Visitor Centre","Royal Selangor Visitor Centre");
    private OnPlaceListAdapterListener onPlaceListAdapterListener;


    public PlacesListAdapter(){

    }

    public void setOnPlaceListAdapterListener(OnPlaceListAdapterListener onPlaceListAdapterListener){
        this.onPlaceListAdapterListener = onPlaceListAdapterListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PlacesListAdapterViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_place,parent,false),onPlaceListAdapterListener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((PlacesListAdapterViewHolder)holder).txtView.setText(getItem(position));
    }

    public String getItem(int position){
        return places.get(position);
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    private static class PlacesListAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView txtView;
        private OnPlaceListAdapterListener onPlaceListAdapterListener;

        public PlacesListAdapterViewHolder(View itemView,OnPlaceListAdapterListener onPlaceListAdapterListener) {
            super(itemView);
            this.onPlaceListAdapterListener = onPlaceListAdapterListener;
            txtView = (TextView) itemView.findViewById(R.id.txt_view_place);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(onPlaceListAdapterListener!=null){
                onPlaceListAdapterListener.onItemClick(getAdapterPosition());
            }
        }
    }
}
