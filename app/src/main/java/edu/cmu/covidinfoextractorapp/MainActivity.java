package edu.cmu.covidinfoextractorapp;

/**
 * https://stackoverflow.com/questions/45940861/android-8-cleartext-http-traffic-not-permitted
 * https://stackoverflow.com/questions/5495534/java-net-connectexception-localhost-127-0-0-18080-connection-refused
 * https://www.youtube.com/watch?v=xPi-z3nOcn8
 * https://developer.android.com/reference/android/view/View
 * https://developer.android.com/training/volley
 * https://stackoverflow.com/questions/33535435/how-to-create-a-proper-volley-listener-for-cross-class-volley-method-calling
 */

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    // Views
    Button btn_getData;
    EditText et_dataInput;
    TextView tv_jsonResponse;
    ImageView iv_statusImage;

    // Get values to views
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Assign value to each control on the layout
        btn_getData = findViewById(R.id.btn_getData);
        et_dataInput = findViewById(R.id.et_dataInput);
        tv_jsonResponse = findViewById(R.id.lv_jsonResponse);
        iv_statusImage = findViewById(R.id.iv_statusImage);

        // Click listener for button
        btn_getData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputString = et_dataInput.getText().toString();
                // Check if user input is valid
                if (inputString.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter age range", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Set image view to 'loading'
                iv_statusImage.setImageResource(R.drawable.loading);
                // Call service and get response
                CovidInfoService covidInfoService = new CovidInfoService(MainActivity.this);
                covidInfoService.getResponseFromAPI(inputString, new CovidInfoService.VolleyResponseListener() {
                    @Override
                    public void onError(String message) {
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                        // Set image view to failed icon
                        iv_statusImage.setImageResource(R.drawable.failed);
                    }

                    @Override
                    public void onResponse(String responseData) {
                        // The app will not throw an error if the API successfully returns and error message
                        if (responseData == null) {
                            // Gracefully handle error
                            onError("Cannot retrieve data for the supplied input");
                            return;
                        }
                        // Set TextView to the formatted JSON response
                        tv_jsonResponse.setText(responseData);
                        // Set ImageView to show a tick mark indicating successful response
                        iv_statusImage.setImageResource(R.drawable.tick);
                    }
                });
            }
        });

    }
}