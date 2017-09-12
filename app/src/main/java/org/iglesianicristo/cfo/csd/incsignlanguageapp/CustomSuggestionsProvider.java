package org.iglesianicristo.cfo.csd.incsignlanguageapp;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class CustomSuggestionsProvider extends ContentProvider {

    @Override
    public boolean onCreate() { return false; }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // it is assumed that this ContentProvider class only provides custom suggestions (no other fancy URIs)
        // read from SLA database
        SLAdbHelper mDbHelper = new SLAdbHelper(getContext());
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        // projection to specify columns to retrieve
        String[] suggestionTable = new String[] {"_id",SearchManager.SUGGEST_COLUMN_TEXT_1,SearchManager.SUGGEST_COLUMN_TEXT_2,SearchManager.SUGGEST_COLUMN_ICON_1,SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA};
        projection = new String[] {
                SLAdbContract.SLAdbSLA.COL_ID,
                SLAdbContract.SLAdbSLA.COL_WORD,
                SLAdbContract.SLAdbSLA.COL_CAT,
                SLAdbContract.SLAdbSLA.COL_FILE,
                SLAdbContract.SLAdbSLA.COL_FAVE,
                SLAdbContract.SLAdbSLA.COL_VAR
        };
        MatrixCursor matrixCursor = new MatrixCursor(suggestionTable);

        selection = SLAdbContract.SLAdbSLA.COL_WORD + " like ?";
        selectionArgs = new String[] {uri.getLastPathSegment().toLowerCase() + "%"};

        Cursor cursor = db.query(
                SLAdbContract.SLAdbSLA.TABLE_NAME,      // The table to query
                projection,                             // The columns to return
                selection,                              // The columns for the WHERE clause
                selectionArgs,                          // The values for the WHERE clause
                null,                                   // don't group the rows
                null,                                   // don't filter by row groups
                null                                    // The sort order
        );

        while(cursor.moveToNext()) {
            Integer id = cursor.getInt(cursor.getColumnIndexOrThrow(SLAdbContract.SLAdbSLA.COL_ID));
            String word = cursor.getString(cursor.getColumnIndexOrThrow(SLAdbContract.SLAdbSLA.COL_WORD));
            String cat = cursor.getString(cursor.getColumnIndexOrThrow(SLAdbContract.SLAdbSLA.COL_CAT));
            Integer file = cursor.getInt(cursor.getColumnIndexOrThrow(SLAdbContract.SLAdbSLA.COL_FILE));
            Integer fave = cursor.getInt(cursor.getColumnIndexOrThrow(SLAdbContract.SLAdbSLA.COL_FAVE));
            String var = cursor.getString(cursor.getColumnIndexOrThrow(SLAdbContract.SLAdbSLA.COL_VAR));
            // get related words
            String[] proj = {SLAdbContract.SLAdbSLA.COL_WORD};
            // Filter results WHERE
            String sel = SLAdbContract.SLAdbSLA.COL_FILE + " = ? and " + SLAdbContract.SLAdbSLA.COL_WORD + " <> ?";
            String[] selArgs = { file.toString(), word };
            // sort
            sortOrder = SLAdbContract.SLAdbSLA.COL_WORD;
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
            // category icon
            Uri searchIconUri = Uri.parse("android.resource://org.iglesianicristo.cfo.csd.signlanguageapp/mipmap/ic_launcher.png");
            // add to cursor
            matrixCursor.addRow(new Object[] {id,word,related,searchIconUri,id+":"+word+":"+cat+":"+file+":"+related+":"+fave+":"+var});
        }
        cursor.close();
        db.close();
        mDbHelper.close();
        return matrixCursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }
}
