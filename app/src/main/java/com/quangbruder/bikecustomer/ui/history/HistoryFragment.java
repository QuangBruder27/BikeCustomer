package com.quangbruder.bikecustomer.ui.history;

import static com.quangbruder.bikecustomer.help.Helper.retrieveToken;
import static com.quangbruder.bikecustomer.help.Helper.retrieveUserInfo;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.quangbruder.bikecustomer.R;
import com.quangbruder.bikecustomer.data.model.Booking;
import com.quangbruder.bikecustomer.data.model.URLs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A fragment representing a list of Items.
 */
public class HistoryFragment extends Fragment {

    List<Booking> listOfBooking;
    private static final String ARG_COLUMN_COUNT = "column-count";


    public HistoryFragment() {
    }

    public static HistoryFragment newInstance(int columnCount) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
           // recyclerView.setAdapter(new BookingListAdapter(getListBooking()));
        }
        getBookingList();
        return view;
    }


    /**
     * Create a list of booking from the array of JSON Object
     * @param jsonArray
     * @return
     * @throws JSONException
     */
    public List<Booking> createListOfBookingFromJSonArray(JSONArray jsonArray) throws JSONException {
        List<Booking> result = new ArrayList<>();
        for (int a = 0; a < jsonArray.length(); a++) {
            JSONObject obj = jsonArray.getJSONObject(a);
            Booking booking = new Booking();
            booking.setId(obj.getString("id"));
            booking.setBikeId(obj.getString("bikeId"));
            if (obj.getString("status").equals("completed")){
                 if(!obj.isNull("beginTime")) booking.setBeginTime(obj.getString("beginTime"));
                 if(!obj.isNull("endTime")) booking.setEndTime(obj.getString("endTime"));
                 if(!obj.isNull("distance")) booking.setDistance(obj.getString("distance"));
            }
            result.add(booking);
        }
        return result;
    }

    /**
     * update user interface
     * @param response
     * @throws JSONException
     */
    public void updateUI(JSONArray response) throws JSONException {
        listOfBooking = createListOfBookingFromJSonArray(response);
        recyclerView.setAdapter(new BookingListAdapter(listOfBooking));
    }

    /**
     * Send GET Request to get all bookings
     */
    public void getBookingList(){
        System.out.println("user before sendReport: "+retrieveUserInfo(getContext()));
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        String customerId = retrieveUserInfo(getContext()).getUserId();
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URLs.URL_BOOKING_HISTORY+"/"+customerId ,null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        System.out.println("Response: "+response);
                        try {
                            updateUI(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("Eror: "+ error);
                    }
                }){

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization",retrieveToken(getContext()));
                return headers;
            }
        };

        requestQueue.add(request);
    }

}