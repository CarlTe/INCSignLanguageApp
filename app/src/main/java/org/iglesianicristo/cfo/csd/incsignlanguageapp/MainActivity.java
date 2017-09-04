package org.iglesianicristo.cfo.csd.incsignlanguageapp;

import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String CATEGORY_FILTER = "org.iglesianicristo.cfo.csd.signlanguageapp.CATEGORY_FILTER";
    public static final String CATEGORY_NAME = "org.iglesianicristo.cfo.csd.signlanguageapp.CATEGORY_NAME";
    private MenuItem searchMenuItem;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchMenuItem.expandActionView();
                searchView.requestFocus();
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInputFromWindow(searchView.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
            }
        });

        RecyclerView recyclerView;
        RecyclerView.Adapter mAdapter;
        RecyclerView.LayoutManager mLayoutManager;

        recyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);
        // use this setting to
        // improve performance if you know that changes
        // in content do not change the layout size
        // of the RecyclerView
        recyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        List<String> cats = new ArrayList<>();
        List<String> ids = new ArrayList<>();

        // read from SLA database
        SLAdbHelper mDbHelper = new SLAdbHelper(this);
        try {
            mDbHelper.createDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }
        try {
            mDbHelper.openDataBase();
        } catch(SQLException sqle){
            throw new Error("Unable to open database");
        }
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        // projection to specify columns to retrieve
        String[] projection = {
                SLAdbContract.SLAdbCAT.COL_ID,
                SLAdbContract.SLAdbCAT.COL_CAT
        };

        // Filter results WHERE
        // String selection = SLAdbContract.SLAdbEntry.COL_CAT + " = ?";
        // String[] selectionArgs = { "shapes" };

        // sort
        // String sortOrder = SLAdbContract.SLAdbCAT.COL_CAT + " DESC";

        Cursor cursor = db.query(
                SLAdbContract.SLAdbCAT.TABLE_NAME,      // The table to query
                projection,                               // The columns to return
                null,                                     // The columns for the WHERE clause
                null,                                     // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                     // The sort order
        );

        while(cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow(SLAdbContract.SLAdbCAT.COL_ID));
            String cat = cursor.getString(cursor.getColumnIndexOrThrow(SLAdbContract.SLAdbCAT.COL_CAT));
            ids.add(id);
            cats.add(cat);
        }
        cursor.close();
        db.close();
        mDbHelper.close();

        // define an adapter
        mAdapter = new RecyclerViewAdapterMain(this,cats,ids);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        searchMenuItem = menu.findItem(R.id.search_main);
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) searchMenuItem.getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(searchMenuItem!=null) searchMenuItem.collapseActionView();
    }
}