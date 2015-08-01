package com.example.ernesto.wenderlichtuts;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity
        implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final String PREFS = "prefs";
    private static final String PREF_NAME = "name";
    private static final String QUERY_URL = "http://openlibrary.org/search.json?q=";

    TextView mainTextVIew;
    Button mainButton;
    EditText mainEditText;
    ListView mainListView;
    ArrayAdapter mArrayAdapter;
    JSONAdapter mJSONAdapter;
    ArrayList mNameList = new ArrayList();
    ShareActionProvider mShareActioProvider;
    SharedPreferences mSharedPreferences;
    ProgressDialog mDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainTextVIew = (TextView) findViewById(R.id.main_textview);
        //mainTextVIew.setText("Set in java");

        mainButton = (Button)findViewById(R.id.main_button);
        mainButton.setOnClickListener(this);

        mainEditText = (EditText)findViewById(R.id.main_edittext);

        mainListView = (ListView)findViewById(R.id.main_listview);
        //mArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, mNameList);
        //mainListView.setAdapter(mArrayAdapter);
        mainListView.setOnItemClickListener(this);

        displayWelcome();

        mJSONAdapter = new JSONAdapter(this, getLayoutInflater());
        mainListView.setAdapter(mJSONAdapter);

        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Searching for book");
        mDialog.setCancelable(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem shareItem = menu.findItem(R.id.menu_item_share);

        if(shareItem != null){
            mShareActioProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        }

        setSharedIntent();

        return true;
    }


    private void setSharedIntent(){
        if(mShareActioProvider != null){
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "android development");
            shareIntent.putExtra(Intent.EXTRA_TEXT, mainTextVIew.getText());

            mShareActioProvider.setShareIntent(shareIntent);
        }
    }

    public void displayWelcome(){
        mSharedPreferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        String name = mSharedPreferences.getString(PREF_NAME, "");

        if(name.length() > 0){
            Toast.makeText(this, "Welcome back, " + name, Toast.LENGTH_LONG).show();
        }else{
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Hello");
            alert.setMessage("Whats is your name");

            final EditText input = new EditText(this);
            alert.setView(input);

            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String inputName = input.getText().toString();

                    SharedPreferences.Editor e = mSharedPreferences.edit();
                    e.putString(PREF_NAME, inputName);
                    e.commit();

                    Toast.makeText(getApplicationContext(), "Welcome " + inputName, Toast.LENGTH_LONG).show();
                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            alert.show();
        }
    }



    @Override
    public void onClick(View v) {
        /*
        mainTextVIew.setText(mainEditText.getText().toString());
        mNameList.add(mainEditText.getText().toString());
        mArrayAdapter.notifyDataSetChanged();
        */

        // Hide keyboar after button clicked
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);


        queryBooks(mainEditText.getText().toString());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //Log.d("omg android", position + ": " + mNameList.get(position));

        JSONObject jsonObject = (JSONObject) mJSONAdapter.getItem(position);
        String coverID = jsonObject.optString("cover_i", "");

        String firstSentence = "";
        if(jsonObject.has("first_sentence")){
            firstSentence = jsonObject.optJSONArray("first_sentence").optString(0);
        }

        String title = "book";
        if(jsonObject.has("title")){
            title = jsonObject.optString("title");
        }


        Intent detailIntent = new Intent(this, DetailActivity.class);
        detailIntent.putExtra("coverID", coverID);
        detailIntent.putExtra("first_sentence", firstSentence);
        detailIntent.putExtra("title", title);

        startActivity(detailIntent);
    }

    private void queryBooks(String searchString){
        String urlString = "";

        try{
            urlString = URLEncoder.encode(searchString, "UTF-8");
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        AsyncHttpClient client = new AsyncHttpClient();
        mDialog.show();

        client.get(QUERY_URL + urlString,
                    new JsonHttpResponseHandler(){
                        @Override
                        public void onSuccess(JSONObject response) {
                            mDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Success ", Toast.LENGTH_LONG).show();
                            Log.d("omg android", response.toString());
                            mJSONAdapter.updateData(response.optJSONArray("docs"));

                        }

                        @Override
                        public void onFailure(int statusCode, Throwable e, JSONObject errorResponse) {
                            mDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Error: " + statusCode + " " + e.getMessage() , Toast.LENGTH_LONG).show();
                            Log.d("omg android", statusCode + " " +  e.getMessage());
                        }
                    });
    }
}





















