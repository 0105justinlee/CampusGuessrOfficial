package com.example.campusguessr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
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
        private final ImageButton imageButton;
        private final ImageButton popupImage;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            textView = (TextView) view.findViewById(R.id.textView);
            imageButton = view.findViewById(R.id.my_challenge_image);
            popupImage = view.findViewById(R.id.popup_delete_image);
        }

        public TextView getTextView() {
            return textView;
        }

        public ImageButton getImageButton() {
            return imageButton;
        }

        public ImageButton getPopupImage() { return popupImage; }
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

    private MyChallengeAdapter thisAdapter = this;
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
        int i;
        for (i = 0; i < keys.size(); i++) {
            if (i == position) {
                key = keys.get(i);
                break;
            }
        }

        final Object deleteKey = key;
        final int deletePosition = i;

        ArrayList<String> valueArray = myChallenges.get(key);
        //Picasso.get().load(valueArray.get(2)).rotate(90f).resize(800,1000).centerCrop().into(viewHolder.getImageButton());
        //viewHolder.getTextView().setText(valueArray.get(2));

        viewHolder.getImageButton().setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                // inflate the layout of the popup window
                LayoutInflater inflater = (LayoutInflater)
                        viewHolder.getImageButton().getContext().getSystemService(viewHolder.getImageButton().getContext().LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.popup_delete_challenge, null);

                // create the popup window
                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                boolean focusable = true; // lets taps outside the popup also dismiss it
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                // show the popup window
                // which view you pass in doesn't matter, it is only used for the window tolken
                popupWindow.showAtLocation(viewHolder.getImageButton(), Gravity.CENTER, 0, 0);

                // dim background when popup shows up
                Context context = popupWindow.getContentView().getContext();
                View container = (View) popupWindow.getContentView().getParent();
                WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
                p.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                p.dimAmount = 0.5f;
                wm.updateViewLayout(container, p);

                ((TextView)popupView.findViewById(R.id.popup_delete_title)).setText(valueArray.get(0));
                ((TextView)popupView.findViewById(R.id.popup_delete_description)).setText(valueArray.get(1));
                //Picasso.get().load(valueArray.get(2)).rotate(90f).resize(800,800).centerCrop().into((ImageButton) popupView.findViewById(R.id.popup_delete_image));

                // dismiss popup window when touching cancel button
                // popup window still dismisses when touching outside of it too
                Button cancel = popupView.findViewById(R.id.popup_cancel_button);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        popupWindow.dismiss();
                    }
                });

                // delete challenge when pressing delete button
                Button delete = popupView.findViewById(R.id.popup_delete_button);
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // First find the appropriate key
                        //System.out.println("Delete Key: " + deleteKey.toString());
                        FirebaseAuth mAuth;
                        FirebaseStorage storage;
                        DatabaseReference mDatabase;

                        mAuth = FirebaseAuth.getInstance();
                        mDatabase = FirebaseDatabase.getInstance().getReference();
                        Query deleteQuery = mDatabase.child("challenges").child(deleteKey.toString());
                        deleteQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                snapshot.getRef().removeValue();
                                popupWindow.dismiss();
                                context.startActivity(new Intent(context.getApplicationContext(), ProfileActivity.class));
                                ((Activity)context).finish();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                });
            }
        });

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return myChallenges.size();
    }
}
