package edu.cmu.covidinfoextractorapp;

/**
 * Helper class that sends app REST requests, formats input/output, and sends back a response
 *
 * Resources:
 * https://gist.github.com/kristopherjohnson/3189389
 * https://stackoverflow.com/questions/3213205/how-to-detect-system-information-like-os-or-device-type
 * https://mkyong.com/java/how-to-get-current-timestamps-in-java/
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.util.Iterator;

public class CovidInfoService {
    Context context;
    // Base URL of the web service deployed to heroku
    private final static String BASE_URL = "https://akshaysicmu.herokuapp.com/covid-resources";

    public CovidInfoService(Context context) {
        this.context = context;
    }

    /**
     * This interface is used for callback and helps the app to send the request in the background thread
     */
    public interface VolleyResponseListener {
        void onError(String message);
        void onResponse(String responseData);
    }

    /**
     * Creates a request and gets the response from the API, finally invokes callback to inform the main thread
     * @param ageRange input age range
     * @param volleyResponseListener callback interface
     */
    public void getResponseFromAPI(String ageRange, VolleyResponseListener volleyResponseListener) {
        // Testing whether button is clicked
        Toast.makeText(context, "Retrieving data for range: " + ageRange, Toast.LENGTH_SHORT).show();

        // Call API with button text
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);
        JSONObject requestBody = constructRequestBody(ageRange);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, BASE_URL, requestBody, new Response.Listener<JSONObject>() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onResponse(JSONObject response) {
                String formattedResponse = null;
                try {
                    formattedResponse = formatResponse(response, ageRange);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                volleyResponseListener.onResponse(formattedResponse);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMessage = "Cannot retrieve data for the range: " + ageRange;
                volleyResponseListener.onError(errorMessage);
            }
        });

        // Add the request to the RequestQueue.
        queue.add(request);
    }

    /**
     * Formats the JSON response returned by the service
     *
     * @param response response from the service
     * @param ageRange input age range
     * @return formatted response
     * @throws JSONException when then request is invalid, handled by main activity upon callback
     */
    @SuppressLint("DefaultLocale")
    private String formatResponse(JSONObject response, String ageRange) throws JSONException {
        StringBuilder sb = new StringBuilder();
        sb.append("Age Range: ").append(ageRange).append("\n");
        sb.append(String.format("Effective sample size : %.2f\n\n", response.getDouble("effective_sample_size")));
        sb.append("Survey Results (Yes, No): \n\n");

        JSONObject surveyResults = response.getJSONObject("survey_results");
        // Count the number of rows returned
        int count = 1;
        for (Iterator<String> it = surveyResults.keys(); it.hasNext(); count++) {
            String locationName = it.next();
            sb.append(count).append(". ");
            sb.append(WordUtils.capitalize(locationName.replace("_", " "))).append(": ").append(surveyResults.get(locationName)).append("\n");
        }
        return sb.toString();
    }

    /**
     * Creates a POST request and adds logging specific data about the phone
     *
     * @param ageRange age range input from the user
     * @return JSONObject which is the body of the POST request
     */
    private JSONObject constructRequestBody(String ageRange) {
        JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("ageRange", ageRange);
            jsonParam.put("applicationID", Build.DEVICE);
            jsonParam.put("buildBrand", Build.BRAND);
            jsonParam.put("buildId", Build.ID);
            jsonParam.put("buildModel", Build.MODEL);
            jsonParam.put("country", context.getResources().getConfiguration().getLocales().get(0).getCountry());
            jsonParam.put("timeStamp", Instant.now());
        } catch (JSONException e) {
            System.out.println("Error while creating JSON request Body");
        }
        return jsonParam;
    }


}