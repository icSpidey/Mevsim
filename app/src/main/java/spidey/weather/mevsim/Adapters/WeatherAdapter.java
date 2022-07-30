package spidey.weather.mevsim.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import spidey.weather.mevsim.Models.WeatherModel;
import spidey.weather.mevsim.R;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.ViewHolder> {
    private Context context;
    private ArrayList<WeatherModel> weatherModelArrayList;

    public WeatherAdapter(Context context, ArrayList<WeatherModel> weatherModelArrayList) {
        this.context = context;
        this.weatherModelArrayList = weatherModelArrayList;
    }

    @NonNull
    @Override
    public WeatherAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.weather_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherAdapter.ViewHolder holder, int position) {
        WeatherModel model = weatherModelArrayList.get(position);
        holder.temperature.setText(model.getTemperature() + "°c");
        holder.windSpeed.setText(model.getWindSpeed() + "Km/h");
        Picasso.get().load("http:".concat(model.getIcon())).into(holder.icon);
        SimpleDateFormat  input = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        SimpleDateFormat output = new SimpleDateFormat("hh:mm aa");
        try {
            Date date = input.parse(model.getTime());
            holder.time.setText(output.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return weatherModelArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView windSpeed, temperature, time;
        private ImageView icon;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            windSpeed = itemView.findViewById(R.id.windSpeed);
            temperature = itemView.findViewById(R.id.temperature);
            time = itemView.findViewById(R.id.timeView);
            icon = itemView.findViewById(R.id.weatherCondition);
        }
    }
}
