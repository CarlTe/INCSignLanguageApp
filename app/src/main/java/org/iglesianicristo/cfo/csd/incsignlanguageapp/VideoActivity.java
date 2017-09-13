package org.iglesianicristo.cfo.csd.incsignlanguageapp;

import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
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
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        textViewWord.setMovementMethod(new ScrollingMovementMethod());
        int variantsNum = var.length();
        if(variantsNum > 1) {
            textViewWord.setText(var); // this must be 'collective' name
            variantsNum = 1;
        } else {
            textViewWord.setText(word);
            variantsNum = Integer.valueOf(var); // number of variants
        }

        TextView textViewRelated = (TextView) findViewById(R.id.textView_related);
        textViewRelated.setMovementMethod(new ScrollingMovementMethod());
        textViewRelated.setText(root);
        if(root.length()>0) {
            TextView textViewRelatedLabel = (TextView) findViewById(R.id.textView_relatedLabel);
            if(root.contains(",")) textViewRelatedLabel.setText(R.string.relateds);
            else textViewRelatedLabel.setText(R.string.related);
        }
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

        // get Categories
        String[] catproj = {SLAdbContract.SLAdbCAT.COL_CAT};
        String selection = SLAdbContract.SLAdbCAT.COL_ID + " in (?";
        int len = cat.length();
        if (len > 1)
            selection += String.format("%0" + (len - 1) + "d", 0).replace("0", ",?");
        selection += ")";
        List<String> list = new ArrayList<String>(Arrays.asList(cat.split("")));
        list.remove(0);
        String[] selectionArgs = list.toArray(new String[list.size()]);
        Cursor csr = db.query(SLAdbContract.SLAdbCAT.TABLE_NAME, catproj, selection, selectionArgs, null, null, null);
        cat = "";
        while (csr.moveToNext()) {
            if (cat != "") cat += ", ";
            cat += csr.getString(csr.getColumnIndexOrThrow(SLAdbContract.SLAdbCAT.COL_CAT));
        }
        csr.close();

        db.close();
        mDbHelper.close();

        TextView textViewCat = (TextView) findViewById(R.id.textView_categories);
        textViewCat.setText(cat);
        if(cat.length()>0) {
            TextView textViewCatLabel = (TextView) findViewById(R.id.textView_categoriesLabel);
            if(cat.contains(",")) textViewCatLabel.setText(R.string.categories);
            else textViewCatLabel.setText(R.string.category);
        }

        videoView = (VideoView)findViewById(R.id.video_view);

        packageName = getPackageName();
        videoFile = intent.getIntExtra(SearchableActivity.VIDEO_FILE,0);
        videoView.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + getResources().getIdentifier(videoFile+".mp4", "raw", packageName)));

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });
        videoView.start();

        if(variantsNum > 1) { // show bottom navigation tab if there are 2 or more variants of this sign
            BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
            switch(variantsNum) {
                case 2: navigation.getMenu().removeItem(R.id.navigation_var3);
                case 3: navigation.getMenu().removeItem(R.id.navigation_var4);
            }
            navigation.setVisibility(View.VISIBLE);
            navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
            // set related words (max lines) depending on orientation
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            textViewRelated.setMaxLines(2);
            else textViewRelated.setMaxLines(6);
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
                    videoView.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + getResources().getIdentifier(videoFile+".mp4", "raw", packageName)));
                    return true;
                case R.id.navigation_var2:
                    videoView.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + getResources().getIdentifier((videoFile+1)+".mp4", "raw", packageName)));
                    return true;
                case R.id.navigation_var3:
                    videoView.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + getResources().getIdentifier((videoFile+2)+".mp4", "raw", packageName)));
                    return true;
                case R.id.navigation_var4:
                    videoView.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + getResources().getIdentifier((videoFile+3)+".mp4", "raw", packageName)));
                    return true;
            }
            return false;
        }

    };
}
