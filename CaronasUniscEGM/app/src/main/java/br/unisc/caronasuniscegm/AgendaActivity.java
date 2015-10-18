package br.unisc.caronasuniscegm;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import br.unisc.caronasuniscegm.Utils.TokenUtils;
import br.unisc.caronasuniscegm.adapters.AgendaAdapter;
import br.unisc.caronasuniscegm.rest.ApiEndpoints;
import br.unisc.caronasuniscegm.rest.RideIntention;

/**
 * Created by MateusFelipe on 11/10/2015.
 */
public class AgendaActivity extends AppCompatActivity {

    private Button mButton;
    private ListView mListView;
    private List<RideIntention> mRideIntentionList;
    private AgendaAdapter mAdapter;
    private List<RideIntention> thisWeekRideIntentionList;
    private ProgressDialog pd;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda);

       mButton = (Button) findViewById( R.id.add_ride );

       mButton.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
               Intent intent = new Intent(getApplicationContext(), ConfigureRideActivity.class);
               startActivity(intent);
           }
       });

        mRideIntentionList = new ArrayList<RideIntention>();

       mAdapter = new AgendaAdapter(getApplicationContext(), getLayoutInflater(),
               R.layout.activity_agenda_item_row, mRideIntentionList);

        mListView = (ListView) findViewById(R.id.list_view_ride_intention);
       mListView.setAdapter(mAdapter);
       mListView.setEmptyView(findViewById(R.id.rideIntentionEmptyElement));
       mListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

       mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

           }

       });

        mRideIntentionList = getThisWeekRideIntentionList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_agenda, menu);
        return true;
    }


    public List<RideIntention> getThisWeekRideIntentionList() {

        final String token = TokenUtils.getToken(this.getApplicationContext());

        // Resposta de sucesso
        Response.Listener<JSONArray> successListener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArrayResponse) {
                hideProgressDialog();
                try {
                    formatRideIntentionList(jsonArrayResponse);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                mAdapter.setData(mRideIntentionList);
                mAdapter.notifyDataSetChanged();
            }
        };

        // Resposta de erro
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                hideProgressDialog();
                volleyError.printStackTrace();
            }
        };

        // Envia requisição
        showProgressDialog();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();

        String url = ApiEndpoints.RIDE_AVAIABILITIES + "/week/" + sdf.format(date);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url,
                successListener, errorListener){
            @Override
            public HashMap<String, String> getHeaders() {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("Authentication-Token", token);
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(ApiEndpoints.TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);

        return thisWeekRideIntentionList;
    }

    private void formatRideIntentionList(JSONArray jsonArrayResponse) throws JSONException, ParseException {

        mRideIntentionList = new ArrayList<RideIntention>();
        for(int i = 0; i < jsonArrayResponse.length(); i++){
            RideIntention rideIntention = new RideIntention();
            rideIntention.setAvailabilityType(jsonArrayResponse.getJSONObject(i).getString("availability_type"));

            String string = jsonArrayResponse.getJSONObject(i).getString("date");
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date date = format.parse(string);
            rideIntention.setDate(date);

            rideIntention.setPeriod(jsonArrayResponse.getJSONObject(i).getString("period"));
            rideIntention.setStartingLocationAddress(jsonArrayResponse.getJSONObject(i).getString("starting_location_address"));
            rideIntention.setStartingLocationLatitude(jsonArrayResponse.getJSONObject(i).getDouble("starting_location_latitude"));
            rideIntention.setStartingLocationLongitude(jsonArrayResponse.getJSONObject(i).getDouble("starting_location_longitude"));

            if( rideIntention.getAvailabilityType() == "give" ) {
                rideIntention.setAvailablePlacesInCar(jsonArrayResponse.getJSONObject(i).getInt("available_places_in_car"));
            }

            mRideIntentionList.add(rideIntention);

        }

    }

    private void showProgressDialog() {
        String message = getResources().getString(R.string.please_wait);
        pd = ProgressDialog.show(this, "", message, false);
    }

    private void hideProgressDialog() {
        pd.dismiss();
        pd = null;
    }
}
