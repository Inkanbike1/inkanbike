package cl.ozcc.inkanbike.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

import cl.ozcc.inkanbike.R;
import cl.ozcc.inkanbike.objects.CItemSos;
import cl.ozcc.inkanbike.objects.CListSos;
import cl.ozcc.inkanbike.objects.User;
import cl.ozcc.inkanbike.objects.Valid;

/**
 * Created by root on 14-08-15.
 */
public class IndexFragment extends Fragment implements OnMapReadyCallback,
                                                        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private final static Valid validObj = new Valid();
    public static SharedPreferences prefs;
    public static SharedPreferences.Editor editor;
    GoogleApiClient mGoogleApiClient;
    SupportMapFragment SupportMap;
    GoogleMap Gmap;
    Context ctx;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        final View view = inflater.inflate(R.layout.content_inkan, container, false);
        ctx = getContext();
        SupportMap = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.maps);
        SupportMap.getMapAsync(this);
        Gmap = SupportMap.getMap();
        setHasOptionsMenu(true);
        mGoogleApiClient = new GoogleApiClient.Builder(ctx)
                .addApi(LocationServices.API).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();

        Gmap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Toast.makeText(getContext(), "MARKER_CLICK", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        Gmap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
            if(validObj.ValidGPS(ctx)){
                LatLng latLng = new LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude);
                new User().getGarageFromServer(latLng, getContext(), Gmap);
            }

            }
        });
        ArrayList<CItemSos> adapter = new ArrayList<>();
        int imgBike = R.drawable.ic_bike_black_36dp;
        adapter.add(new CItemSos(ctx.getString(R.string.SOS_FLAT_TIRE),imgBike ));
        adapter.add(new CItemSos(ctx.getString(R.string.SOS_TIRE), imgBike));
        adapter.add(new CItemSos(ctx.getString(R.string.SOS_BRAKE), imgBike));
        adapter.add(new CItemSos(ctx.getString(R.string.SOS_CHAIN), imgBike));
        adapter.add(new CItemSos(ctx.getString(R.string.SOS_OTHER), imgBike));
        adapter.add(new CItemSos(ctx.getString(R.string.SOS_THIEFT), R.drawable.ic_run_black_36dp));

        CListSos adapterC = new CListSos(getActivity().getApplicationContext(), adapter);

        final AlertDialog.Builder dialog = new AlertDialog.Builder(view.getContext());

        dialog.setTitle(ctx.getString(R.string.SOS));
        dialog.setCancelable(true);
        dialog.setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final FloatingActionButton btnAlert = (FloatingActionButton) view.findViewById(R.id.sendSosFloatButton);
        btnAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });
        dialog.setAdapter(adapterC, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.v("DEBUG_SOS", "CLICK PRESSED" );
                //send which++ because selected argument start on 0, but swhitch start on 1
                new User().sendSos(which, ctx);

            }
        });
        prefs = ctx.getSharedPreferences("broadcast", Context.MODE_PRIVATE);
        editor = prefs.edit();
        editor.putString("fragment", "IndexFragment");
        editor.commit();

        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
    }
    @Override
    public void onStop() {
        super.onStop();
    }
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        try {
            Gmap = googleMap;
            googleMap.setMyLocationEnabled(true);
        }catch (SecurityException se){

        }
    }
    @Override
    public void onConnected(Bundle bundle) {
        try {
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                if(validObj.ValidGPS(ctx)){
                    centerMap(mLastLocation.getLatitude(), mLastLocation.getLongitude());

                    editor.putString("lastLat", String.valueOf(mLastLocation.getLatitude()));
                    editor.putString("lastLng", String.valueOf(mLastLocation.getLongitude()));
                    editor.commit();
                }
            }
        }catch (SecurityException se){

        }
    }
    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        mGoogleApiClient.connect();
    }
    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }
    public void centerMap(Double lat, Double lon) {
        LatLng mPosition = new LatLng(lat, lon);
        CameraPosition camPos = new CameraPosition.Builder()
                .target(mPosition).zoom(15).bearing(0).tilt(90).build();
        CameraUpdate camUpd = CameraUpdateFactory.newCameraPosition(camPos);
        Gmap.animateCamera(camUpd);
    }
}