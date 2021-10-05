package com.quangbruder.bikecustomer.ui.report;

import static android.app.Activity.RESULT_OK;

import static com.quangbruder.bikecustomer.help.Helper.retrieveToken;
import static com.quangbruder.bikecustomer.help.Helper.retrieveUserInfo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.quangbruder.bikecustomer.data.model.URLs;
import com.quangbruder.bikecustomer.databinding.FragmentReportBinding;
import com.quangbruder.bikecustomer.help.ExifUtil;
import com.quangbruder.bikecustomer.help.VolleyMultipartRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ReportFragment extends Fragment {

    private FragmentReportBinding binding;
    private ImageView imageView;
    private EditText editTextBikeId, editTextNote;
    private ProgressBar progressBar;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentReportBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        progressBar = binding.reportLoading;
        editTextBikeId = binding.reportBike;
        editTextNote = binding.reportNote;

        imageView = binding.imgView;
        Button btnChoose = binding.chooseFile;
        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fileChoosing();
            }
        });

        Button btnSend = binding.reportSend;
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String bikeId = editTextBikeId.getText().toString();
                String note = editTextNote.getText().toString();
                if(bikeId.isEmpty() || note.isEmpty()){
                    Toast.makeText(getContext(), "Lack of information", Toast.LENGTH_SHORT).show();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    sendReport(bikeId,note);
                }

            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    private static final int PICK_IMAGE_REQUEST =1 ;

    /**
     * choose a image from the storage
     */
    private void fileChoosing() {
        System.out.println("Func file choosing");
        //checkPermission();
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }


    private String filePath;
    private Bitmap uploadBitmap;
    @SuppressLint("SetTextI18n")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("Func onActivityResult");
            if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri picUri = data.getData();
            filePath = getPath(picUri);
            if (filePath != null) {
                try {
                    Toast.makeText(getContext(), "File Selected", Toast.LENGTH_SHORT).show();
                    Log.d("filePath", String.valueOf(filePath));
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), picUri);
                    Bitmap orientedBitmap = ExifUtil.rotateBitmap(filePath, bitmap);

                    uploadBitmap = orientedBitmap;
                    // Send HTTP Post multipart request

                    //uploadImage(orientedBitmap);

                    imageView.setImageBitmap(orientedBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                Toast.makeText(getContext(),"no image selected", Toast.LENGTH_LONG).show();
            }
        }

    }

    /**
     * Send POST Request to create the report
     * @param bikeId
     * @param note
     */
    public void sendReport(String bikeId, String note){
        System.out.println("user before sendReport: "+retrieveUserInfo(getContext()));
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("bikeId", bikeId);
            jsonObject.put("customerId",retrieveUserInfo(getContext()).getUserId());
            jsonObject.put("note",note);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URLs.URL_REPORT, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("Response: "+response);
                        try {
                            String reportId = response.getString("reportId");
                            System.out.println("reportId: "+reportId);
                            if(uploadBitmap !=null){
                                uploadImage(uploadBitmap,reportId);
                            } else {
                                progressBar.setVisibility(View.INVISIBLE);
                            }

                            Toast.makeText(getContext(), "Thanks for reporting the defect.",
                                    Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("Eror: "+ error);
                        progressBar.setVisibility(View.INVISIBLE);
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

    /**
     * Send PUT Request upload  the image
     * @param bitmap
     * @param reportId
     */
    public void uploadImage(Bitmap bitmap,String reportId){
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", retrieveToken(getContext()));

        VolleyMultipartRequest request = new VolleyMultipartRequest(Request.Method.PUT,URLs.URL_REPORT+"/"+reportId,headers,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        System.out.println("Response for upload image: "+response);
                        System.out.println("Status: "+response.statusCode);
                        /*
                        if (response.statusCode==201){
                            Toast.makeText(getContext(), "Thanks for reporting the defect.\n" +
                                    "You get 20 points", Toast.LENGTH_LONG).show();
                        }

                         */
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("Eror: ");
                        progressBar.setVisibility(View.INVISIBLE);
                        error.printStackTrace();
                    }
                }){
                @Override
                protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                params.put("image", new DataPart(imagename + ".png", getFileDataFromDrawable(bitmap)));
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(request);
    }

    /**
     * set path from Uri to String
      * @param uri
     * @return
     */
    public String getPath(Uri uri) {
        Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContext().getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }

    /**
     * convert the bitmap to the array of byte
     * @param bitmap
     * @return
     */
    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }


}