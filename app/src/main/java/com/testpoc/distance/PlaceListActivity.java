package com.testpoc.distance;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

public class PlaceListActivity extends AppCompatActivity implements PlacesListAdapter.OnPlaceListAdapterListener{

    private RecyclerView recyclerView;
    private PlacesListAdapter placesListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_family_details);
        toolbar.setTitle("Your Delivery Places");
        setSupportActionBar(toolbar);


        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        placesListAdapter = new PlacesListAdapter();
        placesListAdapter.setOnPlaceListAdapterListener(this);
        recyclerView.setAdapter(placesListAdapter);

    }

    @Override
    public void onItemClick(int positon) {
        Intent intent = new Intent(this,MainActivity.class);
        intent.putExtra("DeliveryPlace",placesListAdapter.getItem(positon));
        startActivity(intent);
    }
}
