package com.example.campusguessr;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.campusguessr.POJOs.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RankingsActivity extends AppCompatActivity {

    public class RankingsAdapter extends RecyclerView.Adapter<RankingsAdapter.ViewHolder> {
        private ArrayList<User> users;
        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView username;
            public TextView score;
            public TextView rank;
            public ViewHolder(View v) {
                super(v);
                username = (TextView) v.findViewById(R.id.rank_item_username);
                score = (TextView) v.findViewById(R.id.rank_item_score);
                rank = (TextView) v.findViewById(R.id.rank_item_rank);
            }
        }
        public RankingsAdapter(ArrayList<User> users) {
            this.users = users;
        }
        @Override
        public RankingsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rankings_row_item, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (users.get(position) == null) {
                return;
            }
            holder.username.setText(users.get(position).getName());
            holder.score.setText(Long.toString(users.get(position).getScore()));
            holder.rank.setText(Integer.toString(position + 1));
        }
        @Override
        public int getItemCount() {
            return users.size();
        }
    }

    private DatabaseReference mDatabase;
    private String TAG = "RankingsActivity";

    protected RecyclerView mRecyclerView;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected RecyclerView.Adapter mAdapter;
    private ArrayList<User> users = new ArrayList<User>(0);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rankings);
        ImageButton PlayButton = (ImageButton) findViewById(R.id.navigate_play_tab_button);
        ImageButton CreateButton = (ImageButton) findViewById(R.id.navigate_create_tab_button);
        ImageButton ProfileButton = (ImageButton) findViewById(R.id.navigate_profile_tab_button);
        PlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), StartChallengeActivity.class));
            }
        });
        CreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), CreateChallengeActivity.class));
            }
        });
        ProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
            }
        });

        mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref = mDatabase.child("users");
        ref.orderByChild("score").limitToFirst(10).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                Log.d(TAG, "onDataChange: " + dataSnapshot);
                ObjectMapper mapper = new ObjectMapper();
                int i = 0;
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    User user = mapper.convertValue(child.getValue(), User.class);
                    users.add(user);
                    i++;
                    mAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.dailyLayout);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new RankingsAdapter(users);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }
}
