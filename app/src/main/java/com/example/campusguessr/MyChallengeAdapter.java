package com.example.campusguessr;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyChallengeAdapter extends RecyclerView.Adapter<MyChallengeAdapter.ViewHolder> {

    //private String[] localDataSet;
    // hashmap structure: assume that all of the challenges here are the user's challenges they submitted
    // challengeid
    //      -> ArrayList
    //          -> title
    //          -> description
    //          -> photo url
    Map<String, ArrayList<String>> myChallenges;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            textView = (TextView) view.findViewById(R.id.textView);
            imageView = view.findViewById(R.id.my_challenge_image);
        }

        public TextView getTextView() {
            return textView;
        }

        public ImageView getImageView() {
            return imageView;
        }
    }

    /**
     * Initialize the dataset of the Adapter
     *
     * @param dataSet String[] containing the data to populate views to be used
     *                by RecyclerView
     */
    public MyChallengeAdapter(Map<String, ArrayList<String>> dataSet) {
        myChallenges = dataSet;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recyclerview_mychallenges, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        String text = "";
        Object key = null;

        List keys = new ArrayList(myChallenges.keySet());
        for (int i = 0; i < keys.size(); i++) {
            if (i == position) {
                key = keys.get(i);
            }
        }

        ArrayList<String> valueArray = myChallenges.get(key);
        Picasso.get().load(valueArray.get(2)).rotate(90f).resize(800,1000).centerCrop().into(viewHolder.getImageView());
        //viewHolder.getTextView().setText(valueArray.get(2));

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return myChallenges.size();
    }
}
