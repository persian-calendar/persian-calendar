package com.byagowi.persiancalendar.view.deviceinfo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.byagowi.persiancalendar.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.MyViewHolder> {

    private List<InfoList> infoLists;

    public DeviceAdapter(List<InfoList> moviesList) {
        this.infoLists = moviesList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.device_info_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        InfoList movie = infoLists.get(position);
        holder.title.setText(movie.getTitle());
        holder.content.setText(movie.getContent());
        holder.ver.setText(movie.getVer());
    }

    @Override
    public int getItemCount() {
        return infoLists.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title, ver, content;

        MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
            content = view.findViewById(R.id.content);
            ver = view.findViewById(R.id.ver);
        }
    }
}
