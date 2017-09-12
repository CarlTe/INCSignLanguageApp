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
import java.util.List;

public class SearchableActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<Integer> ids = new ArrayList<>();
    private List<String> words = new ArrayList<>();
    private List<String> cats = new ArrayList<>();
    private List<Integer> files = new ArrayList<>();
    private List<String> roots = new ArrayList<>();
    public static List<Boolean> faves = new ArrayList<>();
    private List<String> vars = new ArrayList<>();
    public static final String VIDEO_ID = "org.iglesianicristo.cfo.csd.incsignlanguageapp.VIDEO_ID";
    public static final String VIDEO_WORD = "org.iglesianicristo.cfo.csd.incsignlanguageapp.VIDEO_WORD";
    public static final String VIDEO_CAT = "org.iglesianicristo.cfo.csd.incsignlanguageapp.VIDEO_CAT";
    public static final String VIDEO_FILE = "org.iglesianicristo.cfo.csd.incsignlanguageapp.VIDEO_FILE";
    public static final String VIDEO_ROOT = "org.iglesianicristo.cfo.csd.incsignlanguageapp.VIDEO_ROOT";
    public static final String VIDEO_FAVE = "org.iglesianicristo.cfo.csd.incsignlanguageapp.VIDEO_FAVE";
    public static final String VIDEO_VAR = "org.iglesianicristo.cfo.csd.incsignlanguageapp.VIDEO_VAR";
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
        mAdapter = new RecyclerViewAdapter(this, ids, words, cats, files, roots, faves, vars);
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
        ids.clear();
        words.clear();
        cats.clear();
        files.clear();
        roots.clear();
        faves.clear();
        vars.clear();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Context context = getApplicationContext();
            // Handle a suggestions click (because the suggestions all use ACTION_VIEW)
            String extra = intent.getStringExtra(SearchManager.EXTRA_DATA_KEY);
            String[] extras = extra.split(":");

            ids.add(Integer.valueOf(extras[0]));
            words.add(extras[1]);
            cats.add(extras[2]);
            files.add(Integer.valueOf(extras[3]));
            roots.add(extras[4]);
            faves.add(extras[5].equals("1"));
            vars.add(extras[6]);

            getSupportActionBar().setTitle(extras[1]);

            Intent newIntent = new Intent(context, VideoActivity.class);
            newIntent.putExtra(SearchableActivity.VIDEO_ID, extras[0]);
            newIntent.putExtra(SearchableActivity.VIDEO_WORD, extras[1]);
            newIntent.putExtra(SearchableActivity.VIDEO_CAT, extras[2]);
            newIntent.putExtra(SearchableActivity.VIDEO_FILE, extras[3]);
            newIntent.putExtra(SearchableActivity.VIDEO_ROOT, extras[4]);
            newIntent.putExtra(SearchableActivity.VIDEO_FAVE, extras[5].equals("1"));
            newIntent.putExtra(SearchableActivity.VIDEO_VAR, extras[6]);
            context.startActivity(newIntent);
        } else {
            // read from SLA database
            SLAdbHelper mDbHelper = new SLAdbHelper(this);
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            // projection to specify columns to retrieve
            String[] projection = {
                    SLAdbContract.SLAdbSLA.COL_ID,
                    SLAdbContract.SLAdbSLA.COL_WORD,
                    SLAdbContract.SLAdbSLA.COL_CAT,
                    SLAdbContract.SLAdbSLA.COL_FILE,
                    SLAdbContract.SLAdbSLA.COL_FAVE,
                    SLAdbContract.SLAdbSLA.COL_VAR
            };

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
                if(query.equals("0")) {
                    selection = SLAdbContract.SLAdbSLA.COL_FAVE + " = ?";
                    selectionArgs = new String[]{"1"};
                } else {
                    selection = SLAdbContract.SLAdbSLA.COL_CAT + " like ?";
                    selectionArgs = new String[]{"%" + query + "%"};
                }
            }

            // sort
            String sortOrder = SLAdbContract.SLAdbSLA.COL_WORD;

            Cursor cursor = db.query(
                    SLAdbContract.SLAdbSLA.TABLE_NAME,      // The table to query
                    projection,                             // The columns to return
                    selection,                              // The columns for the WHERE clause
                    selectionArgs,                          // The values for the WHERE clause
                    null,                                   // don't group the rows
                    null,                                   // don't filter by row groups
                    sortOrder                               // The sort order
            );

            while (cursor.moveToNext()) {
                Integer id = cursor.getInt(cursor.getColumnIndexOrThrow(SLAdbContract.SLAdbSLA.COL_ID));
                String word = cursor.getString(cursor.getColumnIndexOrThrow(SLAdbContract.SLAdbSLA.COL_WORD));
                String cat = cursor.getString(cursor.getColumnIndexOrThrow(SLAdbContract.SLAdbSLA.COL_CAT));
                Integer file = cursor.getInt(cursor.getColumnIndexOrThrow(SLAdbContract.SLAdbSLA.COL_FILE));
                Integer fave = cursor.getInt(cursor.getColumnIndexOrThrow(SLAdbContract.SLAdbSLA.COL_FAVE));
                String var = cursor.getString(cursor.getColumnIndexOrThrow(SLAdbContract.SLAdbSLA.COL_VAR));
                ids.add(id);
                words.add(word);
                files.add(file);
                cats.add(cat);
                faves.add(fave==1);
                vars.add(var);
                // get related words
                String[] proj = {SLAdbContract.SLAdbSLA.COL_WORD};
                // Filter results WHERE
                String sel = SLAdbContract.SLAdbSLA.COL_FILE + " = ? and " + SLAdbContract.SLAdbSLA.COL_WORD + " <> ?";
                String[] selArgs = { file.toString(), word };
                Cursor cur = db.query(
                        SLAdbContract.SLAdbSLA.TABLE_NAME,      // The table to query
                        proj,                                   // The columns to return
                        sel,                                    // The columns for the WHERE clause
                        selArgs,                                // The values for the WHERE clause
                        null,                                   // don't group the rows
                        null,                                   // don't filter by row groups
                        sortOrder                               // The sort order
                );
                String related = "";
                while(cur.moveToNext()) {
                    if (related != "") related += ", ";
                    related += cur.getString(cur.getColumnIndexOrThrow(SLAdbContract.SLAdbSLA.COL_WORD));
                }
                cur.close();
                roots.add(related);
            }
            cursor.close();
            db.close();
            mDbHelper.close();
        }
    }
}
