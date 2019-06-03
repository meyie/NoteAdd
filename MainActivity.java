package com.mcksfg.noteadd;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mNoteList;
    private DatabaseReference mDatabase;
    private DatabaseReference mUsers;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private RelativeLayout goUpLayout;

    private String departmentId;
    private FirebaseRecyclerAdapter <Note, NoteViewHolder> adapter;

    @Override
    protected void onRestart() {
        super.onRestart();

        goUpLayout.setVisibility(View.VISIBLE);
        goUpLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNoteList.smoothScrollToPosition(adapter.getItemCount() - 1);
                goUpLayout.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mNoteList = findViewById(R.id.note_list);
        goUpLayout = findViewById(R.id.goUpLayout);

        mNoteList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mNoteList.setLayoutManager(linearLayoutManager);
        mNoteList.setItemViewCacheSize(20);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("NoteAdd");
        mUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()==null){
                    Log.i("bilgi", "null");
                    finish();
                    Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(loginIntent);
                } else {
                    Log.i("bilgi", "null degil");
                }
            }
        };

        if(mAuth.getCurrentUser() != null) {
            Log.i("bilgi", "kullanıcı null degil");

            mUsers.child(mAuth.getCurrentUser().getUid()).child("department").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.i("bilgi", "department: " + dataSnapshot.getValue().toString());
                    departmentId = dataSnapshot.getValue().toString();

                    DatabaseReference mDepartmentDatabase = mDatabase.child("Department").child(departmentId);

                    FirebaseRecyclerOptions<Note> options = new FirebaseRecyclerOptions.Builder<Note>()
                            .setQuery(mDepartmentDatabase, new SnapshotParser<Note>() {
                                @NonNull
                                @Override
                                public Note parseSnapshot(@NonNull DataSnapshot snapshot) {
                                    if(!snapshot.getKey().equals("name")) {
                                        Log.i("bilgi", "key: " + snapshot.getKey());
                                        String description = snapshot.child("description").getValue().toString();
                                        String image = snapshot.child("image").getValue().toString();
                                        String lessonId = snapshot.child("lessonId").getValue().toString();
                                        String timestamp = snapshot.child("timestamp").getValue().toString();
                                        String uid = snapshot.child("uid").getValue().toString();
                                        String username = snapshot.child("username").getValue().toString();

                                        Log.i("bilgi", "yeni not: " + description);

                                        return new Note(image, timestamp, uid, username, description, lessonId);
                                    } else {
                                        return new Note();
                                    }
                                }
                            })
                            .build();

                    adapter =
                            new FirebaseRecyclerAdapter<Note, NoteViewHolder>(options) {
                        @NonNull
                        @Override
                        public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_row, parent, false);
                            return new NoteViewHolder(view);
                        }

                        @Override
                        protected void onBindViewHolder(@NonNull NoteViewHolder holder, int position, @NonNull final Note note) {
                            if(note.getDescription() != null) {
                                holder.setUserName(note.getUsername());
                                holder.setImage(note.getImage());
                                holder.setDescription(note.getDescription());
                                holder.setAvatar(note.getUid());
                                holder.setLesson(note.getLessonId(), departmentId);

                                holder.mView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(MainActivity.this, SingleNoteActivity.class);
                                        intent.putExtra("image_url", note.getImage());
                                        startActivity(intent);
                                    }
                                });

                                final LinearLayout threedotlayout = holder.mView.findViewById(R.id.threedotlayout);
                                threedotlayout.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        PopupMenu popup = new PopupMenu(MainActivity.this, threedotlayout);
                                        popup.getMenuInflater()
                                                .inflate(R.menu.three_dot_menu, popup.getMenu());

                                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                            public boolean onMenuItemClick(MenuItem item) {
                                                DatabaseReference reportedRef = mDatabase.child("Reports").push();
                                                reportedRef.child("reportedUser").setValue(note.getUsername());
                                                reportedRef.child("noteTime").setValue(note.getTimestamp());

                                                Toast.makeText(MainActivity.this, "Reported", Toast.LENGTH_SHORT).show();
                                                return true;
                                            }
                                        });

                                        popup.show();
                                    }
                                });
                            }
                        }
                    };

                    mNoteList.setAdapter(adapter);

                    adapter.startListening();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            Log.i("bilgi", "kullanıcı null");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public NoteViewHolder(View itemView){
            super(itemView);
            mView = itemView;
        }

        private void setLesson(String lessonId, String departmentId){
            FirebaseDatabase.getInstance().getReference().child("NoteAdd").child("DepartmentLessons").child(departmentId).child(lessonId).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.getValue().toString();
                    TextView post_lesson = mView.findViewById(R.id.textLesson);
                    post_lesson.setText(name);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        private void setDescription(String description){
            TextView post_desc = mView.findViewById(R.id.textDescription);
            post_desc.setText(description);
        }

        public void setAvatar(String uid) {
            FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("image").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String url = dataSnapshot.getValue().toString();
                    CircleImageView avatarView = mView.findViewById(R.id.avatar);
                    Picasso.with(mView.getContext()).load(url).placeholder(R.mipmap.defpho).into(avatarView);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void setImage(String image){
            ImageView post_image = mView.findViewById(R.id.post_image);
            // Picasso.with(mView.getContext()).load(image).into(post_image);
            Glide.with(mView.getContext()).load(image).into(post_image);
        }

        public void setUserName(String userName){
            TextView postUserName = itemView.findViewById(R.id.textUsername);
            postUserName.setText(userName);

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SetupActivity.class);
            startActivity(intent);
        }
        else if(id == R.id.addIcon){
            Intent intent = new Intent(MainActivity.this, PostActivity.class);
            intent.putExtra("departmentId", departmentId);
            startActivity(intent);
        }
        else if(id == R.id.logout){
            mAuth.signOut();
        }
        return super.onOptionsItemSelected(item);
    }
}