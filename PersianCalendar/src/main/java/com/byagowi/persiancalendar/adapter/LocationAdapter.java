package com.byagowi.persiancalendar.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.databinding.ListItemCityNameBinding;
import com.byagowi.persiancalendar.entity.CityEntity;
import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.preferences.LocationPreferenceDialog;
import com.byagowi.persiancalendar.viewmodel.LocationAdapterViewModel;

import java.util.List;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {
    private List<CityEntity> cities;
    private LocationPreferenceDialog locationPreferenceDialog;

    public LocationAdapter(LocationPreferenceDialog locationPreferenceDialog,
                           List<CityEntity> cities) {
        this.locationPreferenceDialog = locationPreferenceDialog;
        this.cities = cities;
    }

    @Override
    public LocationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ListItemCityNameBinding binding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                        R.layout.list_item_city_name, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(cities.get(position));
    }

    @Override
    public int getItemCount() {
        return cities.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ListItemCityNameBinding binding;

        ViewHolder(ListItemCityNameBinding itemBinding) {
            super(itemBinding.getRoot());
            this.binding = itemBinding;
        }

        void bind(CityEntity cityEntity) {
            String city, country;
            switch (Utils.getAppLanguage()) {
                case Constants.LANG_EN_IR:
                case Constants.LANG_EN_US:
                    city = cityEntity.getEn();
                    country = cityEntity.getCountryEn();
                    break;
                case Constants.LANG_CKB:
                    city = cityEntity.getCkb();
                    country = cityEntity.getCountryCkb();
                    break;
                case Constants.LANG_AR:
                    city = cityEntity.getAr();
                    country = cityEntity.getCountryAr();
                    break;
                default:
                    city = cityEntity.getFa();
                    country = cityEntity.getCountryFa();
                    break;
            }
            LocationAdapterViewModel model = new LocationAdapterViewModel(city, country, this);
            binding.setModel(model);
            binding.executePendingBindings();
        }

        @Override
        public void onClick(View view) {
            locationPreferenceDialog.selectItem(cities.get(getAdapterPosition()).getKey());
        }
    }
}