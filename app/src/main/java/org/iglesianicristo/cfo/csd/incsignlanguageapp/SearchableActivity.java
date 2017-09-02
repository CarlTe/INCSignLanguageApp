package org.iglesianicristo.cfo.csd.incsignlanguageapp;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchableActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<String> words = new ArrayList<>();
    private List<String> cats = new ArrayList<>();
    private List<String> files = new ArrayList<>();
    private List<String> roots = new ArrayList<>();
    public static List<Boolean> faves = new ArrayList<>();
    public static final String VIDEO_WORD = "org.iglesianicristo.cfo.csd.signlanguageapp.VIDEO_WORD";
    public static final String VIDEO_CAT = "org.iglesianicristo.cfo.csd.signlanguageapp.VIDEO_CAT";
    public static final String VIDEO_FILE = "org.iglesianicristo.cfo.csd.signlanguageapp.VIDEO_FILE";
    public static final String VIDEO_ROOT = "org.iglesianicristo.cfo.csd.signlanguageapp.VIDEO_ROOT";
    public static final String VIDEO_FAVE = "org.iglesianicristo.cfo.csd.signlanguageapp.VIDEO_FAVE";
    private MenuItem searchMenuItem;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

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

        // search process
        recyclerView = (RecyclerView) findViewById(R.id.search_recycler_view);
        recyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        search(getIntent());

        // define an adapter
        mAdapter = new RecyclerViewAdapter(this, words, cats, files, roots, faves);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        search(intent);
        mAdapter.notifyDataSetChanged();
        searchMenuItem.collapseActionView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        searchMenuItem = menu.findItem(R.id.search_search);
        MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                return true;
            }
        });

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) searchMenuItem.getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    private void search(Intent intent) {
        words.clear();
        cats.clear();
        files.clear();
        roots.clear();
        faves.clear();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Context context = getApplicationContext();
            // Handle a suggestions click (because the suggestions all use ACTION_VIEW)
            String extra = intent.getStringExtra(SearchManager.EXTRA_DATA_KEY);
            String[] extras = extra.split(":");

            words.add(extras[0]);
            cats.add(extras[1]);
            files.add(extras[2]);
            roots.add(extras[3]);
            faves.add(extras[4]=="1");

            getSupportActionBar().setTitle(extras[0]);

            Intent newIntent = new Intent(context, VideoActivity.class);
            newIntent.putExtra(SearchableActivity.VIDEO_WORD, extras[0]);
            newIntent.putExtra(SearchableActivity.VIDEO_CAT, extras[1]);
            newIntent.putExtra(SearchableActivity.VIDEO_FILE, extras[2]);
            newIntent.putExtra(SearchableActivity.VIDEO_ROOT, extras[3]);
            newIntent.putExtra(SearchableActivity.VIDEO_FAVE, extras[4]=="1");
            context.startActivity(newIntent);
        } else {
            // read from SLA database
            SLAdbHelper mDbHelper = new SLAdbHelper(this);
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            // projection to specify columns to retrieve
            String[] projection = {
                    SLAdbContract.SLAdbSLA.COL_WORD,
                    SLAdbContract.SLAdbSLA.COL_CAT,
                    SLAdbContract.SLAdbSLA.COL_FILE,
                    SLAdbContract.SLAdbSLA.COL_ROOT,
                    SLAdbContract.SLAdbSLA.COL_FAVE
            };
            String[] catproj = {SLAdbContract.SLAdbCAT.COL_CAT};
            String selection = "";
            String[] selectionArgs = {};

            // Get the intent, verify the action and get the query
            if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
                getSupportActionBar().setTitle(intent.getStringExtra(SearchManager.QUERY));
                selection = SLAdbContract.SLAdbSLA.COL_WORD + " like ?";
                selectionArgs = new String[]{intent.getStringExtra(SearchManager.QUERY) + "%"};
            } else {
                getSupportActionBar().setTitle(intent.getStringExtra(MainActivity.CATEGORY_NAME));
                String query = intent.getStringExtra(MainActivity.CATEGORY_FILTER);
                selection = SLAdbContract.SLAdbSLA.COL_CAT + " like ?";
                selectionArgs = new String[]{"%" + query + "%"};
            }

            // sort
            //String sortOrder = SLAdbContract.SLAdbSLA.COL_WORD;

            Cursor cursor = db.query(
                    SLAdbContract.SLAdbSLA.TABLE_NAME,      // The table to query
                    projection,                             // The columns to return
                    selection,                              // The columns for the WHERE clause
                    selectionArgs,                          // The values for the WHERE clause
                    null,                                   // don't group the rows
                    null,                                   // don't filter by row groups
                    null                                    // The sort order
            );

            while (cursor.moveToNext()) {
                String word = cursor.getString(cursor.getColumnIndexOrThrow(SLAdbContract.SLAdbSLA.COL_WORD));
                String cat = cursor.getString(cursor.getColumnIndexOrThrow(SLAdbContract.SLAdbSLA.COL_CAT));
                String id = cursor.getString(cursor.getColumnIndexOrThrow(SLAdbContract.SLAdbSLA.COL_FILE));
                String root = cursor.getString(cursor.getColumnIndexOrThrow(SLAdbContract.SLAdbSLA.COL_ROOT));
                Integer fave = cursor.getInt(cursor.getColumnIndexOrThrow(SLAdbContract.SLAdbSLA.COL_FAVE));
                words.add(word);
                files.add(id);
                roots.add(root);
                faves.add(fave==1);
                selection = SLAdbContract.SLAdbCAT.COL_ID + " in (?";
                int len = cat.length();
                if (len > 1)
                    selection += String.format("%0" + (len - 1) + "d", 0).replace("0", ",?");
                selection += ")";
                List<String> list = new ArrayList<String>(Arrays.asList(cat.split("")));
                list.remove(0);
                selectionArgs = list.toArray(new String[list.size()]);
                Cursor csr = db.query(SLAdbContract.SLAdbCAT.TABLE_NAME, catproj, selection, selectionArgs, null, null, null);
                cat = "";
                while (csr.moveToNext()) {
                    if (cat != "") cat += ", ";
                    cat += csr.getString(csr.getColumnIndexOrThrow(SLAdbContract.SLAdbCAT.COL_CAT));
                }
                cats.add(cat);
                csr.close();
            }
            cursor.close();
            db.close();
            mDbHelper.close();
        }
    }
}
