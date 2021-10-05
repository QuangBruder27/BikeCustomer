package com.quangbruder.bikecustomer.ui.rent;

import static com.quangbruder.bikecustomer.MainActivity.locationPermissionGranted;
import static com.quangbruder.bikecustomer.help.Helper.retrieveToken;
import static com.quangbruder.bikecustomer.help.Helper.retrieveUserInfo;
import static com.quangbruder.bikecustomer.help.Helper.storeRentBike;
import static com.quangbruder.bikecustomer.help.Helper.storeRentPin;
import static com.quangbruder.bikecustomer.help.Helper.storeRentStatus;

import android.app.AlertDialog;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.quangbruder.bikecustomer.R;
import com.quangbruder.bikecustomer.data.model.Bike;
import com.quangbruder.bikecustomer.data.model.URLs;
import com.quangbruder.bikecustomer.databinding.FragmentRentBinding;
import com.quangbruder.bikecustomer.help.CustomArrayRequest;
import com.quangbruder.bikecustomer.help.Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RentFragment extends Fragment implements OnMapReadyCallback, RoutingListener {

    private static final String TAG ="TAG";
    //private RentViewModel rentViewModel;
    private FragmentRentBinding binding;
    private List<Polyline> polylines=null;

    private GoogleMap map;
    private Location currentLocation;

    private PlacesClient placesClient;
    private FusedLocationProviderClient fusedLocationProviderClient;

    // A default location
    private final LatLng defaultLocation = new LatLng(52.50319541397275, 13.469601517010412);
    private static final int DEFAULT_ZOOM = 15;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //rentViewModel = new ViewModelProvider(this).get(RentViewModel.class);

        binding = FragmentRentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        // Construct a PlacesClient
        Places.initialize(getContext(), getString(R.string.google_maps_key));
        placesClient = Places.createClient(getContext());

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                // Turn on the My Location layer and the related control on the map.
                updateLocationUI();
                // Get the current location of the device and set the position of the map.
                getDeviceLocation();
                mapListener();
                return false;
            }
        });
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
        mapListener();
    }

    public void mapListener(){
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                System.out.println("Infowindows Click---------------------------------------------");
                System.out.println("Marker: "+marker.getTitle());
                createDialog(marker);
            }
        });

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                System.out.println("Marker Click---------------------------------------------");
                System.out.println("Marker: "+marker.getTitle());
                return false;
            }
        });
    }

    //-------------------------------------------------------------------------

    /**
     * find routes by using Google-Direction-Android
     * @param Begin
     * @param End
     */
    public void findRoutes(LatLng Begin, LatLng End) {
        if(Begin==null || End==null) {
            Toast.makeText(getContext(),"Unable to get location", Toast.LENGTH_LONG).show();
        } else {
            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.WALKING)
                    .withListener(this)
                    .alternativeRoutes(true)
                    .waypoints(Begin, End)
                    .key(getString(R.string.google_maps_key)) 
                    .build();
            routing.execute();
        }
    }

    @Override
    public void onRoutingCancelled() {
        //System.out.println("Routing cancel-------------------");
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        System.out.println("Routing: Failure");
        System.out.println("Exception: "+e.toString());
        Toast.makeText(getContext(), "Cannot find the route to this bike",Toast.LENGTH_LONG);

    }

    @Override
    public void onRoutingStart() {
        System.out.println("Finding Routing-----------------------------------");
    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> routes, int shortestRouteIndex) {
        System.out.println("Routing success----------------------------------");
        //CameraUpdate center = CameraUpdateFactory.newLatLng(start);
        //CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
        if(polylines!=null) {
            for(Polyline line : polylines) {
                System.out.println("Remove Line-------------------------------");
                line.remove();
            }
            polylines.clear();
            System.out.println("Remove polylines-------------------------------");
        }

        PolylineOptions polyOptions = new PolylineOptions();
        LatLng polylineStartLatLng=null;
        LatLng polylineEndLatLng=null;

        polylines = new ArrayList<>();
        //add route(s) to the map using polyline
        for (int i = 0; i < routes.size(); i++) {
            if(i==shortestRouteIndex) {
                polyOptions.color(getResources().getColor(R.color.quantum_teal));
                polyOptions.width(7);
                polyOptions.addAll(routes.get(shortestRouteIndex).getPoints());
                Polyline polyline = map.addPolyline(polyOptions);
                polylineStartLatLng=polyline.getPoints().get(0);
                int k=polyline.getPoints().size();
                polylineEndLatLng=polyline.getPoints().get(k-1);
                polylines.add(polyline);
            } else {

            }
        }
    }





    //------------------------------------------------------------------------------------

    /**
     * create parameters for function getBikeLocation()
     * @param latitude
     * @param longtitude
     * @return
     */
    public static Map<String,String> createParametersGetBikeLocation(String latitude, String longtitude){
        Map<String, String> result = new HashMap<String, String>();
        result.put("latitude",latitude);
        result.put("longtitude",longtitude);
        return result;
    }

    /**
     * Send POST Request to get bikes nearby
     * @param params
     * @param context
     */
    public void getBikeLocations(Map<String, String> params, Context context){
        System.out.println("getBikeLocations Func");
        System.out.println("Parameter:"+params.size()+", "+params.toString());
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        CustomArrayRequest request = new CustomArrayRequest(Request.Method.POST,URLs.URL_GET_BIKE_LOCATION,params, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                System.out.println("RESPONSE: "+response.toString());
                try {
                    System.out.println("GET BIKE LOCATION ------------------------------------------------");
                    List<Bike> listOfBike = createListOfBikeFromJSonArray(response);
                    addPointer(createMarkerFromBikeLocation(listOfBike));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Error in GETBIKELOCATION: " + error);
                Toast.makeText(getContext(), "No available bike was found.", Toast.LENGTH_SHORT).show();
            }
        }){

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization",retrieveToken(context));
                return headers;
            }
        };
        requestQueue.add(request);
    }

    /**
     * Send POST Request to create the booking for rent a bike
     * @param bikeId
     * @param customerId
     * @param context
     */
    public void rentBike(String bikeId, String customerId, Context context){
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("bikeId",bikeId);
            jsonObject.put("customerId",customerId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String requestBody = jsonObject.toString();

        StringRequest request = new StringRequest(Request.Method.POST, URLs.URL_RENT_BIKE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println("RESPONSE: "+response);
                createAlertDialogForPIN(bikeId,response);
                storeRentStatus(context,true);
                storeRentBike(context,bikeId);
                storeRentPin(context,response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Error: "+error);
                Toast.makeText(getContext(), "Please try again", Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    return null;
                }
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization",retrieveToken(context));
                return headers;
            }
        }
                ;
        requestQueue.add(request);
    }

    /**
     * create the list of bikes from the array of JSON objects
     * @param jsonArray
     * @return
     * @throws JSONException
     */
    public List<Bike> createListOfBikeFromJSonArray(JSONArray jsonArray) throws JSONException {
        List<Bike> result = new ArrayList<>();
        for (int a = 0; a < jsonArray.length(); a++) {
            JSONObject obj = jsonArray.getJSONObject(a);
            Bike bike = new Bike();
            bike.setBikeId(obj.getString("bikeId"));
            bike.setLatitude(obj.getString("latitude"));
            bike.setLongtitude(obj.getString("longtitude"));
            result.add(bike);
        }
        return result;
    }

    /**
     *  create markers on the map from locations of bike
     * @param list
     * @return
     */
    public List<MarkerOptions> createMarkerFromBikeLocation(List<Bike> list){
        List<MarkerOptions> listOfMarker = new ArrayList<>();
        for (Bike bike:list){
            MarkerOptions marker = createMarker(bike.getBikeId(),bike.getLatitude(),bike.getLongtitude());
            listOfMarker.add(marker);
        }
        return listOfMarker;
    }

    /**
     * creater the marker
     * @param bikeId
     * @param latitude
     * @param longtitude
     * @return
     */
    public MarkerOptions createMarker(String bikeId, String latitude, String longtitude){
        System.out.println("CREATE MARKER: "+latitude+", "+longtitude);
        return new MarkerOptions().title("Bike: "+bikeId)
                .position(new LatLng(Double.valueOf(latitude), Double.valueOf(longtitude)))
                .icon(BitmapDescriptorFactory.fromBitmap(Helper.rescaleBitMap(R.drawable.bike,getContext())));
    }


    /**
     * add pointer on the map
     * @param listOfMarker
     */
    public void addPointer(List<MarkerOptions> listOfMarker){
        System.out.println("Add Pointer----------------------------------------------");
        for (MarkerOptions marker: listOfMarker){
            System.out.println("Addingggg");
            System.out.println("Marker: "+marker.getTitle());
            map.addMarker(marker);
        }
    }

    /**
     * Gets the current location of the user, and positions the map's camera.
     */
    // [START maps_current_place_get_device_location]
    private void getDeviceLocation() {
        System.out.println("get Device Location func");
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                System.out.println("Permission granted");
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current lo cation of the device.
                            currentLocation = task.getResult();
                            if (currentLocation != null) {
                                Log.d(TAG,"-----------------------------------------------" +
                                        "Location: longtitude:"+currentLocation.getLongitude()+"; latitude"+currentLocation.getLatitude());
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(currentLocation.getLatitude(),
                                                currentLocation.getLongitude()), DEFAULT_ZOOM));

                                String latitude = String.valueOf(currentLocation.getLatitude());
                                String longtitude = String.valueOf(currentLocation.getLongitude());
                                getBikeLocations(createParametersGetBikeLocation(latitude,longtitude),
                                        getContext());

                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            map.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }
    // [END maps_current_place_get_device_location]


    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    // [START maps_current_place_update_location_ui]
    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                currentLocation = null;
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    // [END maps_current_place_update_location_ui]


    /**
     * create the dialog by click of the marker
     * @param marker
     */
    public void createDialog(Marker marker) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Rent a bike\n"+marker.getTitle());
        builder.setCancelable(true);

        builder.setPositiveButton("Route", (dialog, id) -> {
            System.out.println("YES----------");
            dialog.cancel();
            findRoutes(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()),marker.getPosition());
        });

        builder.setNegativeButton("Rent", (dialog, id) -> {
            System.out.println("No-------------------");
            System.out.println("BikeId:"+marker.getTitle().substring(6));
            rentBike(marker.getTitle().substring(6),retrieveUserInfo(getContext()).getUserId(),getContext());

            dialog.cancel();
        });

        builder.create().show();
    }


    public void createAlertDialogForPIN(String bikeId, String pin){
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Bike: "+bikeId);
        builder.setMessage("PIN: "+pin);

        // add a button
        builder.setPositiveButton("OK", null);

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCancelable(false);
    }

}