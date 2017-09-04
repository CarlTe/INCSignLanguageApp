package org.iglesianicristo.cfo.csd.incsignlanguageapp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.IOException;

public class VideoActivity extends AppCompatActivity {
    private VideoView videoView;
    private int videoFile;
    private String packageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Intent intent = getIntent();
        final Integer id = intent.getIntExtra(SearchableActivity.VIDEO_ID,0);
        String word = intent.getStringExtra(SearchableActivity.VIDEO_WORD);
        String cat = intent.getStringExtra(SearchableActivity.VIDEO_CAT);
        String root = intent.getStringExtra(SearchableActivity.VIDEO_ROOT);
        Boolean fave = intent.getBooleanExtra(SearchableActivity.VIDEO_FAVE,false);
        final Integer pos = intent.getIntExtra(RecyclerViewAdapter.ADAPTER_POSITION,0);
        String var = intent.getStringExtra(SearchableActivity.VIDEO_VAR);

        getSupportActionBar().setTitle(word);
        TextView textViewWord = (TextView) findViewById(R.id.textView_word);
        int variantsNum = var.length();
        if(variantsNum > 1) {
            textViewWord.setText(var); // this must be 'collective' name
            variantsNum = 1;
        } else {
            textViewWord.setText(word);
            variantsNum = Integer.valueOf(var); // number of variants
        }
        TextView textViewCat = (TextView) findViewById(R.id.textView_categories);
        textViewCat.setText(cat);
        if(cat.length()>0) {
            TextView textViewCatLabel = (TextView) findViewById(R.id.textView_categoriesLabel);
            if(cat.contains(",")) textViewCatLabel.setText(R.string.categories);
            else textViewCatLabel.setText(R.string.category);
        }
        TextView textViewRelated = (TextView) findViewById(R.id.textView_related);
        CheckBox checkBox = (CheckBox) findViewById(R.id.favorite);
        checkBox.setChecked(fave);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SearchableActivity.faves.set(pos,isChecked);
                SLAdbHelper mDbHelper = new SLAdbHelper(getApplicationContext());
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                ContentValues cv = new ContentValues();
                cv.put(SLAdbContract.SLAdbSLA.COL_FAVE,(isChecked?1:0));
                db.update(SLAdbContract.SLAdbSLA.TABLE_NAME, cv, SLAdbContract.SLAdbSLA.COL_ID+"="+id,null);
                db.close();
                mDbHelper.close();
            }
        });

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

        // projection
        String[] projection = {SLAdbContract.SLAdbSLA.COL_WORD};

        // Filter results WHERE
        String selection = SLAdbContract.SLAdbSLA.COL_ROOT + " = ? and " + SLAdbContract.SLAdbSLA.COL_WORD + " <> ?";
        String[] selectionArgs = { root, word };

        // sort
        String sortOrder = SLAdbContract.SLAdbSLA.COL_WORD;

        Cursor cursor = db.query(
                SLAdbContract.SLAdbSLA.TABLE_NAME,          // The table to query
                projection,                                 // The columns to return
                selection,                                  // The columns for the WHERE clause
                selectionArgs,                              // The values for the WHERE clause
                null,                                       // don't group the rows
                null,                                       // don't filter by row groups
                sortOrder                                   // The sort order
        );

        String related = "";
        while(cursor.moveToNext()) {
            if (related != "") related += ", ";
            related += cursor.getString(cursor.getColumnIndexOrThrow(SLAdbContract.SLAdbSLA.COL_WORD));
        }
        cursor.close();
        db.close();
        mDbHelper.close();

        textViewRelated.setText(related);
        if(related.length()>0) {
            TextView textViewRelatedLabel = (TextView) findViewById(R.id.textView_relatedLabel);
            if(related.contains(",")) textViewRelatedLabel.setText(R.string.relateds);
            else textViewRelatedLabel.setText(R.string.related);
        }

        videoView = (VideoView)findViewById(R.id.video_view);

        packageName = getPackageName();
        videoFile = Integer.valueOf(intent.getStringExtra(SearchableActivity.VIDEO_FILE));
        videoView.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + getResources().getIdentifier("v"+videoFile, "raw", packageName)));

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });
        videoView.start();

        if(variantsNum > 1) { // show bottom navigation tab is there are 2 or more variants of this sign
            BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
            if(variantsNum == 2) navigation.getMenu().removeItem(R.id.navigation_var3);
            navigation.setVisibility(View.VISIBLE);
            navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoView.start();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_var1:
                    videoView.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + getResources().getIdentifier("v"+videoFile, "raw", packageName)));
                    return true;
                case R.id.navigation_var2:
                    videoView.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + getResources().getIdentifier("v"+(videoFile+1), "raw", packageName)));
                    return true;
                case R.id.navigation_var3:
                    videoView.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + getResources().getIdentifier("v"+(videoFile+2), "raw", packageName)));
                    return true;
            }
            return false;
        }

    };
}
