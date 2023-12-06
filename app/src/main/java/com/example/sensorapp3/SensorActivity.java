package com.example.sensorapp3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import org.w3c.dom.Text;

import java.util.List;

public class SensorActivity extends AppCompatActivity {

    private static final String KEY_SUBTITLE_VISIBLE = "subtitleVisible";
    private SensorManager sensorManager;
    private List<Sensor> sensorList;
    private RecyclerView recycleView;
    private SensorAdapter adapter;
    public static final String KEY_EXTRA_SENSOR_TYPE = "SensorActivity.sensor";
    private boolean subtitleVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        if (savedInstanceState != null) {
            subtitleVisible = savedInstanceState.getBoolean(KEY_SUBTITLE_VISIBLE);
        }

        recycleView = findViewById(R.id.sensor_recycle_view);
        recycleView.setLayoutManager(new LinearLayoutManager(this));

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);

        if (adapter == null) {
            adapter = new SensorAdapter(sensorList);
            recycleView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
        updateSubtitle();
    }

    private void updateSubtitle() {String subtitle = null;
        if (subtitleVisible)
            subtitle = getString(R.string.subtitle_format, sensorList.size());
        getSupportActionBar().setSubtitle(subtitle);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
            case R.id.show_sensor_count:
                subtitleVisible = !subtitleVisible;
                invalidateOptionsMenu();
                updateSubtitle();
                return true;
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_SUBTITLE_VISIBLE,subtitleVisible);
    }

    private class SensorHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener{
        private ImageView iconImageView;
        private TextView nameTextView;
        private Sensor sensor;

        public SensorHolder(LayoutInflater inflater, ViewGroup parent){
            super(inflater.inflate(R.layout.sensor_list_item,parent,false));

            iconImageView = itemView.findViewById(R.id.sensor_image);
            nameTextView = itemView.findViewById(R.id.sensor_name);
            itemView.setOnLongClickListener(this);
            itemView.setOnClickListener(this);


        }
        private void showSensorDetails(Sensor sensor) {
            // Logika wyświetlania szczegółów czujnika (np. w nowym AlertDialog)
        }

        public void bind(Sensor sensor){
            this.sensor = sensor;
            iconImageView.setImageResource(R.drawable.ic_sensor);
            nameTextView.setText(sensor.getName());

            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER || sensor.getType() == Sensor.TYPE_LIGHT) {
                itemView.setBackgroundColor(Color.TRANSPARENT);
            } else {
                itemView.setBackgroundColor(Color.LTGRAY);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            Context context = v.getContext();
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getString(R.string.sensor_details_title));

            String vendor = context.getString(R.string.sensor_vendor, sensor.getVendor());
            String maxRange = context.getString(R.string.sensor_max_range, sensor.getMaximumRange());
            builder.setMessage(vendor + "\n" + maxRange);

            builder.setPositiveButton(context.getString(R.string.ok_button), null);
            AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        }

        @Override
        public void onClick(View view) {

            Intent intent = new Intent(SensorActivity.this, SensorDetailsActivity.class);
            intent.putExtra(KEY_EXTRA_SENSOR_TYPE, sensor.getType());
            startActivity(intent);
        }
    }
    private class SensorAdapter extends RecyclerView.Adapter<SensorHolder> {
        private List<Sensor> sensorList;

        public SensorAdapter(List<Sensor> sensors)
        {
            this.sensorList = sensors;
        }

        @NonNull
        @Override
        public SensorHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(SensorActivity.this);
            return new SensorHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull SensorHolder holder, int position) {
            Sensor sensor = sensorList.get(position);
            holder.bind(sensor);
        }

        @Override
        public int getItemCount() {
            return sensorList.size();
        }
    }

}