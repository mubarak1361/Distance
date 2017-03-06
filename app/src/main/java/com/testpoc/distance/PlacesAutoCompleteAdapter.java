package com.testpoc.distance;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

public class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable,PlaceApi.MyPlaceApiAccessor {

    private ArrayList<String> resultList;
    private LayoutInflater inflater;
    private int resource;
    private PlaceApi pApi;

    public PlacesAutoCompleteAdapter(Context context, int resource) {
        super(context, resource);
        inflater = LayoutInflater.from(context);
        this.resource = resource;
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public String getItem(int index) {
        return resultList.get(index).toString();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView ==  null) {
            convertView =  inflater.inflate(resource, null);
        }
        TextView place = (TextView) convertView.findViewById(android.R.id.text1);
        place.setText(getItem(position));

        return convertView;
    }


    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {

                    // Retrieve the autocomplete results.
                    pApi =  new PlaceApi(PlacesAutoCompleteAdapter.this);
                    pApi.execute(new String[] {String.valueOf(constraint)});

                    // Assign the data to the FilterResults
                    filterResults.values = resultList;
                    filterResults.count = resultList.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                }
                else {
                    notifyDataSetInvalidated();
                }
            }};
        return filter;
    }

    @Override
    public void getPlacesList(ArrayList<String> allPlaces) {
        resultList = new ArrayList<String>();
        resultList = allPlaces;

    }


}