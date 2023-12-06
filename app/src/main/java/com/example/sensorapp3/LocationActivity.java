package com.example.sensorapp3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LocationActivity extends AppCompatActivity {
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private Button downloadButton;
    private Button getAddres;
    private Location lastLocation;
    private TextView locationTextView;
    private TextView adressTextView;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        adressTextView = findViewById(R.id.textview_adress);
        locationTextView = findViewById(R.id.textview_location);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getAddres = findViewById(R.id.download_adress_button);
        downloadButton = findViewById(R.id.download_button);

        getAddres.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                executeGeocoding();
            }
        });
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocation();
            }
        });
    }
    protected void getLocation(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION_PERMISSION);
            Log.d("TAG","getLocation(): perrmision denied" );
        }else{
            Log.d("TAG","getLocation(): perrmision grantedf" );
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                if(location != null){
                    lastLocation=location;
                    locationTextView.setText(getString(R.string.location_text, location.getLatitude(), location.getLongitude(), location.getTime()));
                }
                else{
                    locationTextView.setText(R.string.no_location);
                }
            });
        }

    }
    private String locationGeocoding(Context context, Location location){
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;
        String ressult ="";
        try{
            addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
        }catch(IOException e)
        {
            ressult = context.getString(R.string.service_not_available);
            Log.e("TAG",ressult, e);

        }

        if(addresses == null || addresses.isEmpty() ){
            if(ressult.isEmpty()){
                ressult = context.getString(R.string.no_addreses_found);
                Log.e("TAG", ressult);
            }
        }else{
            Address address = addresses.get(0);
            List<String> addressParts = new ArrayList<>();

            for(int i = 0; i<= address.getMaxAddressLineIndex();i++){
                addressParts.add(address.getAddressLine(i));
            }
            ressult = TextUtils.join("\n", addressParts);
        }

        return ressult;
    }
    private void executeGeocoding(){
        if(lastLocation!=null){
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<String> returnAddress = executor.submit(()->locationGeocoding(getApplicationContext(),lastLocation));
            try{
                String result = returnAddress.get();
                adressTextView.setText(getString(R.string.address_text,result,System.currentTimeMillis()));

            }
            catch (ExecutionException | InterruptedException e){
                Log.e("TAG", e.getMessage(), e);
                Thread.currentThread().interrupt();
            }
        }
    }

}