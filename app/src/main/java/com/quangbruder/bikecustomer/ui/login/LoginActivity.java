package com.quangbruder.bikecustomer.ui.login;


import static com.quangbruder.bikecustomer.help.Helper.createUserFromJSON;
import static com.quangbruder.bikecustomer.help.Helper.retrieveUserInfo;
import static com.quangbruder.bikecustomer.help.Helper.storeToken;
import static com.quangbruder.bikecustomer.help.Helper.storeUserInfo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.common.hash.Hashing;
import com.quangbruder.bikecustomer.MainActivity;
import com.quangbruder.bikecustomer.data.model.URLs;
import com.quangbruder.bikecustomer.data.model.User;
import com.quangbruder.bikecustomer.databinding.ActivityLoginBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    boolean registerMode = false;

    String inputEmail,inputPassword,inputName;

    EditText emailEditText,passwordEditText,nameEditText;
    Button loginButton,registerButton;
    ProgressBar loadingProgressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        emailEditText = binding.email;
        passwordEditText = binding.password;
        loginButton = binding.login;
        nameEditText = binding.name;
        loadingProgressBar = binding.loading;
        registerButton = binding.createAcc;

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerMode = true;
                nameEditText.setVisibility(View.VISIBLE);
                loginButton.setText("REGISTER");
                registerButton.setVisibility(View.INVISIBLE);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
                inputEmail = emailEditText.getText().toString();
                inputPassword = passwordEditText.getText().toString();
                inputName = nameEditText.getText().toString();
                loadingProgressBar.setVisibility(View.VISIBLE);
                User inputUser = new User(inputEmail,inputPassword);
                inputUser.setName(inputName);

                try {
                    if (registerMode) {
                        postRegister(inputUser, getApplicationContext());
                    } else {
                        postLogIn(inputUser, getApplicationContext());
                    }
                } catch (JSONException e) {
                        e.printStackTrace();
                }
                }


        });
    }

    /**
     * hash the password
     * @param password
     * @return
     */
    public String hashPassword(String password){
        System.out.println("old password: "+password);
        String hashed = Hashing.sha256()
                .hashString(password, StandardCharsets.UTF_8)
                .toString();
        System.out.println("Hashing: "+hashed);
        return hashed;
    }

    /**
     * set user interface after successful registration
     * @param response
     * @param user
     * @param context
     * @throws JSONException
     */
    public void setUIAfterRegister(JSONObject response, User user, Context context) throws JSONException {
        User loginUser = createUserFromJSON(response);
        loginUser.setEmail(user.getEmail());
        loginUser.setPassword(user.getPassword());
        System.out.println("loginUser: ");
        storeUserInfo(context,loginUser);
        System.out.println("Retrieve user: "+retrieveUserInfo(context));
        storeToken(context,response.getString("token"));
    }

    /**
     * SEND Post Request to register a new account
     * @param user
     * @param context
     * @throws JSONException
     */
    public void postRegister(User user, Context context) throws JSONException {
        System.out.println("postRegister Func: "+user);
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JSONObject object = new JSONObject();
        object.put("email",user.getEmail());
        object.put("password",hashPassword(user.getPassword()));
        object.put("name",user.getName());

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URLs.URL_REGISTER, object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                System.out.println("RESPONSEin postRegister: "+response.toString());
                try {
                    setUIAfterRegister(response,user,context);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

               gotoMainActivity();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Error: " + error);
                System.out.println("Register fails");
                loadingProgressBar.setVisibility(View.INVISIBLE);
                if (error.networkResponse.statusCode==409){
                    Toast.makeText(getApplicationContext(), "This email address is already in use.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Register failed. Please try again", Toast.LENGTH_LONG).show();
                }
            }
        });
        requestQueue.add(request);
    }

    /**
     * set user interface after  successful login
     * @param response
     * @param user
     * @param context
     * @throws JSONException
     */
    public void setUIAfterLogin(JSONObject response, User user,Context context) throws JSONException {
        User loginUser = createUserFromJSON(response);
        loginUser.setEmail(user.getEmail());
        loginUser.setPassword(user.getPassword());
        System.out.println("loginUser: ");
        storeUserInfo(context,loginUser);
        System.out.println("Retrieve user: "+retrieveUserInfo(context));
        storeToken(context,response.getString("token"));
    }

    /**
     * SEND Post Request to login
     * @param user
     * @param context
     * @throws JSONException
     */
    public void postLogIn(User user, Context context) throws JSONException {
        System.out.println("postLogIn Func");
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JSONObject object = new JSONObject();
        object.put("email",user.getEmail());
        object.put("password",hashPassword(user.getPassword()));

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URLs.URL_LOGIN, object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                System.out.println("RESPONSE in postLogIn: "+response.toString());
                try {
                    setUIAfterLogin(response,user,context);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                gotoMainActivity();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Error: " + error);
                showLoginFailed(error);
            }
        });
        requestQueue.add(request);
    }

    /**
     * show toast in case of incorrect registration
     * @param error
     */
    private void showLoginFailed(VolleyError error) {
        System.out.println("Login fails:");
        loadingProgressBar.setVisibility(View.INVISIBLE);
        if (error instanceof AuthFailureError){
            Toast.makeText(this, "Wrong email or password", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Login failed. Please try again", Toast.LENGTH_LONG).show();
        }
    }

    public void gotoMainActivity(){
        finish();
        Intent myIntent = new Intent(this, MainActivity.class);
        startActivity(myIntent);
    }

}