package org.iglesianicristo.cfo.csd.incsignlanguageapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class RecyclerViewAdapterMain extends RecyclerView.Adapter<RecyclerViewAdapterMain.ViewHolder> {
    private List<String> cats;
    private List<String> files;
    private Context context;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case

        public ImageView catIcon;
        public TextView txtHeader;
        public View layout;

        public ViewHolder(View v) {
            super(v);
            layout = v;
            catIcon = (ImageView) v.findViewById(R.id.iconMain);
            txtHeader = (TextView) v.findViewById(R.id.firstLineMain);
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, SearchableActivity.class);
                    intent.putExtra(MainActivity.CATEGORY_FILTER, files.get(getAdapterPosition()));
                    intent.putExtra(MainActivity.CATEGORY_NAME, cats.get(getAdapterPosition()));
                    context.startActivity(intent);
                }
            });
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RecyclerViewAdapterMain(Context myContext, List<String> myCats, List<String> myFiles) {
        context = myContext;
        cats = myCats;
        files = myFiles;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerViewAdapterMain.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                 int viewType) {
        // create a new view
        LayoutInflater inflater = LayoutInflater.from(
                parent.getContext());
        View v =
                inflater.inflate(R.layout.row_layout_main, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.txtHeader.setText(cats.get(position));
        // category icon
        Uri searchIconUri = Uri.parse("android.resource://org.iglesianicristo.cfo.csd.signlanguageapp/mipmap/ic_launcher.png");
        holder.catIcon.setImageResource(R.mipmap.ic_launcher);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return cats.size();
    }

}