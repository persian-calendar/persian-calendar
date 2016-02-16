package com.byagowi.persiancalendar.adapter;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.Utils;
import com.byagowi.persiancalendar.view.dialog.LocationPreferenceDialog;

import java.util.Arrays;
import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {
    private String locale;
    private LocationPreferenceDialog locationPreferenceDialog;
    List<Utils.City> cities;
    private final Utils utils = Utils.getInstance();

    public LocationAdapter(LocationPreferenceDialog locationPreferenceDialog) {
        this.locationPreferenceDialog = locationPreferenceDialog;
        cities = Arrays.asList(utils.getAllCities(locationPreferenceDialog.getContext()));
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(locationPreferenceDialog.getContext());
        this.locale = prefs.getString("AppLanguage", "fa");
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView country;
        private TextView city;

        public ViewHolder(View itemView) {
            super(itemView);;
            itemView.setOnClickListener(this);
            city = (TextView) itemView.findViewById(R.id.text1);
            country = (TextView) itemView.findViewById(R.id.text2);
        }

        @Override
        public void onClick(View view) {
            locationPreferenceDialog.selectItem(getAdapterPosition());
        }
    }

    @Override
    public LocationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_city_name, parent, false);

        return new ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        utils.prepareTextView(locationPreferenceDialog.getContext(), holder.city);
        holder.city.setText(locale.equals("en")
                ? cities.get(position).en
                : utils.textShaper(cities.get(position).fa));

        utils.prepareTextView(locationPreferenceDialog.getContext(), holder.country);
        holder.country.setText(locale.equals("en")
                ? cities.get(position).en
                : utils.textShaper(cities.get(position).fa));
    }

    @Override
    public int getItemCount() {
        return cities.size();
    }
}