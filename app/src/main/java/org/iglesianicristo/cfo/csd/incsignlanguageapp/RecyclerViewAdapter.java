package org.iglesianicristo.cfo.csd.incsignlanguageapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    public static final String ADAPTER_POSITION = "ADAPTER_POSITION";
    private List<Integer> ids;
    private List<String> words;
    private List<String> cats;
    private List<String> files;
    private List<String> roots;
    private List<Boolean> faves;
    private List<String> vars;
    private Context context;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ImageView catIcon;
        public TextView txtHeader;
        public TextView txtFooter;
        public CheckBox favChk;
        public View layout;

        public ViewHolder(View v) {
            super(v);
            layout = v;
            catIcon = (ImageView) v.findViewById(R.id.icon);
            txtHeader = (TextView) v.findViewById(R.id.firstLine);
            txtFooter = (TextView) v.findViewById(R.id.secondLine);
            favChk = (CheckBox) v.findViewById(R.id.favorite);
            favChk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    faves.set(getAdapterPosition(),isChecked);
                    SLAdbHelper mDbHelper = new SLAdbHelper(context);
                    SQLiteDatabase db = mDbHelper.getWritableDatabase();
                    ContentValues cv = new ContentValues();
                    cv.put(SLAdbContract.SLAdbSLA.COL_FAVE,(isChecked?1:0));
                    db.update(SLAdbContract.SLAdbSLA.TABLE_NAME, cv, SLAdbContract.SLAdbSLA.COL_ID+"="+ids.get(getAdapterPosition()),null);
                    db.close();
                    mDbHelper.close();
                }
            });
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, VideoActivity.class);
                    intent.putExtra(SearchableActivity.VIDEO_ID, ids.get(getAdapterPosition()));
                    intent.putExtra(SearchableActivity.VIDEO_WORD, words.get(getAdapterPosition()));
                    intent.putExtra(SearchableActivity.VIDEO_CAT, cats.get(getAdapterPosition()));
                    intent.putExtra(SearchableActivity.VIDEO_FILE, files.get(getAdapterPosition()));
                    intent.putExtra(SearchableActivity.VIDEO_ROOT, roots.get(getAdapterPosition()));
                    intent.putExtra(SearchableActivity.VIDEO_FAVE, faves.get(getAdapterPosition()));
                    intent.putExtra(SearchableActivity.VIDEO_VAR, vars.get(getAdapterPosition()));
                    intent.putExtra(ADAPTER_POSITION,getAdapterPosition());
                    context.startActivity(intent);
                }
            });
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RecyclerViewAdapter(Context myContext, List<Integer> myIds, List<String> myWords, List<String> myCats, List<String> myFiles, List<String> myRoots, List<Boolean> myFaves, List<String> myVars) {
        context = myContext;
        ids = myIds;
        words = myWords;
        cats = myCats;
        files = myFiles;
        roots = myRoots;
        faves = myFaves;
        vars = myVars;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
        // create a new view
        LayoutInflater inflater = LayoutInflater.from(
                parent.getContext());
        View v =
                inflater.inflate(R.layout.row_layout, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.catIcon.setImageResource(R.mipmap.ic_launcher);
        holder.txtHeader.setText(words.get(position));
        holder.txtFooter.setText(cats.get(position));
        holder.favChk.setChecked(faves.get(position));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return words.size();
    }

}