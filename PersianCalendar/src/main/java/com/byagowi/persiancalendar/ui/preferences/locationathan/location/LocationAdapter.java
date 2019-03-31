package com.byagowi.persiancalendar.ui.preferences.locationathan.location;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.databinding.ListItemCityNameBinding;
import com.byagowi.persiancalendar.entities.CityItem;
import com.byagowi.persiancalendar.utils.Utils;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {
    final private List<CityItem> mCities;
    final private LocationPreferenceDialog mLocationPreferenceDialog;

    LocationAdapter(LocationPreferenceDialog locationPreferenceDialog,
                    List<CityItem> cities) {
        mLocationPreferenceDialog = locationPreferenceDialog;
        mCities = cities;
    }

    @NonNull
    @Override
    public LocationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemCityNameBinding binding = ListItemCityNameBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(mCities.get(position));
    }

    @Override
    public int getItemCount() {
        return mCities.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ListItemCityNameBinding binding;

        ViewHolder(ListItemCityNameBinding itemBinding) {
            super(itemBinding.getRoot());
            this.binding = itemBinding;
        }

        void bind(CityItem cityEntity) {
            String city, country;
            switch (Utils.getAppLanguage()) {
                case Constants.LANG_EN_IR:
                case Constants.LANG_EN_US:
                case Constants.LANG_JA:
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
            LocationAdapterModel model = new LocationAdapterModel(city, country, this);
            binding.setModel(model);
            binding.executePendingBindings();
        }

        @Override
        public void onClick(View view) {
            mLocationPreferenceDialog.selectItem(mCities.get(getAdapterPosition()).getKey());
        }
    }
}
