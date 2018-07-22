package com.byagowi.persiancalendar.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.entity.CityEntity;
import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.preferences.LocationPreferenceDialog;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {
    private String locale;
    private List<CityEntity> cities;
    private LocationPreferenceDialog locationPreferenceDialog;
    private LayoutInflater layoutInflater;

    public LocationAdapter(LocationPreferenceDialog locationPreferenceDialog) {
        Context context = locationPreferenceDialog.getContext();
        this.layoutInflater = LayoutInflater.from(context);
        this.locationPreferenceDialog = locationPreferenceDialog;
        this.cities = Utils.getAllCities(context, true);
        this.locale = Utils.getAppLanguage();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView tvCountry;
        private TextView tvCity;

        ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            tvCity = itemView.findViewById(R.id.tvCity);
            tvCountry = itemView.findViewById(R.id.tvCountry);
        }

        @Override
        public void onClick(View view) {
            locationPreferenceDialog.selectItem(cities.get(getAdapterPosition()).getKey());
        }
    }

    @Override
    public LocationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(layoutInflater.inflate(R.layout.list_item_city_name, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (locale.equals(Constants.LANG_EN)) {
            holder.tvCity.setText(cities.get(position).getEn());
            holder.tvCountry.setText(cities.get(position).getCountryEn());
        } else if (locale.equals(Constants.LANG_CKB)) {
            holder.tvCity.setText(cities.get(position).getCkb());
            holder.tvCountry.setText(cities.get(position).getCountryCkb());
        } else {
            holder.tvCity.setText(cities.get(position).getFa());
            holder.tvCountry.setText(cities.get(position).getCountryFa());
        }
    }

    @Override
    public int getItemCount() {
        return cities.size();
    }
}