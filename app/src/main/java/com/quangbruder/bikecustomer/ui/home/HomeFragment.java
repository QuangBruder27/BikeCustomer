package com.quangbruder.bikecustomer.ui.home;

import static com.quangbruder.bikecustomer.help.Helper.retrieveRentBike;
import static com.quangbruder.bikecustomer.help.Helper.retrieveRentPin;
import static com.quangbruder.bikecustomer.help.Helper.retrieveRentStatus;
import static com.quangbruder.bikecustomer.help.Helper.retrieveToken;
import static com.quangbruder.bikecustomer.help.Helper.retrieveUserInfo;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.quangbruder.bikecustomer.MainActivity;
import com.quangbruder.bikecustomer.R;
import com.quangbruder.bikecustomer.data.model.URLs;
import com.quangbruder.bikecustomer.data.model.User;
import com.quangbruder.bikecustomer.databinding.FragmentHomeBinding;
import com.quangbruder.bikecustomer.help.Helper;
import com.quangbruder.bikecustomer.ui.rent.RentFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    CircleImageView circleImageView;
    ObjectAnimator anim;
    TextView tvBonusScore,tvNoBike,tvBikeId,tvBikePin, tvGreeting;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        tvBonusScore = binding.tvScore;
        circleImageView = binding.imgAvatar;
        tvBikeId = binding.tvBikeId;
        tvBikePin = binding.tvBikePin;
        tvNoBike = binding.tvNoBike;
        tvGreeting = binding.tvGreeting;
        tvGreeting.setText("Hello  "+MainActivity.loginUser.getName());

        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Circle click!!!", Toast.LENGTH_SHORT).show();
                changeToRentFragment();
            }
        });

        root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                    Toast.makeText(getContext(), "Refresh", Toast.LENGTH_SHORT).show();
                    updateUI(getContext());
                }
                return true;
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI(getContext());
    }

    public void updateUI(Context context){
        getBonusScore();
        getCurrentBooking();
    }

    public void changeToRentFragment(){
        // Create new fragment and transaction
        Fragment newFragment = new RentFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

    // Replace whatever is in the fragment_container view with this fragment,
    // and add the transaction to the back stack if needed
        transaction.replace(R.id.nav_host_fragment_content_main, newFragment);
        transaction.addToBackStack(null);

    // Commit the transaction
        transaction.commit();
    }

    private void rotation() {
        if(null != anim && anim.isStarted())anim.cancel();
        anim = ObjectAnimator.ofFloat(circleImageView, "rotation", 0, 1440);
        anim.setDuration(60000);
        anim.setRepeatCount(Animation.INFINITE);
        anim.setRepeatMode(ObjectAnimator.RESTART);
        anim.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // SEND GET REQUEST TO bonus service
    public void getBonusScore(){
            // Instantiate the RequestQueue.
            RequestQueue queue = Volley.newRequestQueue(getContext());
            String customerId = Helper.retrieveUserInfo(getContext()).getUserId();
            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.GET, URLs.URL_BONUS+"/"+customerId,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // Display the response.
                            System.out.println("Response is: "+ response);
                            tvBonusScore.setText("Bonus: "+response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println(error.toString());
                    if(error instanceof AuthFailureError){
                        System.out.println("falsch token");
                    }
                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<String, String>();
                    headers.put("Authorization",retrieveToken(getContext()));
                    return headers;
                }
            };
            // Add the request to the RequestQueue.
            queue.add(stringRequest);
    }

    public void getCurrentBooking(){
        System.out.println("GET Current booking func");
        User user = retrieveUserInfo(getContext());
        System.out.println("user before get Booking: "+ user);

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                URLs.URL_CURRENT_BOOKING+"/"+user.getUserId(), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("Response in getCurrentBooking: "+response);
                        try {
                            if (response.getString("bikeId").equals(retrieveRentBike(getContext()))){
                                    tvBikeId.setText("Bike: "+retrieveRentBike(getContext()));
                                    tvBikePin.setText("Pin: "+retrieveRentPin(getContext()));
                                    tvBikeId.setVisibility(View.VISIBLE);
                                    tvBikePin.setVisibility(View.VISIBLE);
                                    tvNoBike.setVisibility(View.INVISIBLE);
                                    rotation();
                                } else {
                                    if (anim!= null) anim.cancel();
                                    tvBikeId.setVisibility(View.INVISIBLE);
                                    tvBikePin.setVisibility(View.INVISIBLE);
                                    tvNoBike.setVisibility(View.VISIBLE);


                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("Eror in getCurrentBooking: "+ error);
                        if (anim!= null) anim.cancel();
                        tvBikeId.setVisibility(View.INVISIBLE);
                        tvBikePin.setVisibility(View.INVISIBLE);
                        tvNoBike.setVisibility(View.VISIBLE);
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