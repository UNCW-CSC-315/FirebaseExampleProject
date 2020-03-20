package edu.uncw.example.firebase;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Date;

public class DisplayActivity extends AppCompatActivity {

    private static final String TAG = "DisplayActivity";
    private static final String USERS = "users";

    private final FirebaseFirestore mDb = FirebaseFirestore.getInstance();
    private Spinner mFirstNamesSpinner;
    private Spinner mLastNamesSpinner;

    private UserRecyclerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        // Set up the Spinners so we can quickly add new data
        mFirstNamesSpinner = findViewById(R.id.first_names);
        ArrayAdapter<CharSequence> firstNamesAdapter = ArrayAdapter.createFromResource(this,
                R.array.first_names, android.R.layout.simple_spinner_item);
        firstNamesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mFirstNamesSpinner.setAdapter(firstNamesAdapter);

        mLastNamesSpinner = findViewById(R.id.last_names);
        ArrayAdapter<CharSequence> lastNamesAdapter = ArrayAdapter.createFromResource(this,
                R.array.last_names, android.R.layout.simple_spinner_item);
        lastNamesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mLastNamesSpinner.setAdapter(lastNamesAdapter);



        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        Query query = mDb.collection(USERS)
                .orderBy("createdTime", Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();

        mAdapter = new UserRecyclerAdapter(options, new UserRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                User user = mAdapter.getSnapshots().getSnapshot(position).toObject(User.class);
                String id = mAdapter.getSnapshots().getSnapshot(position).getId();
                mDb.collection(USERS).document(id).delete();

                Toast.makeText(getApplicationContext(),"Deleting " +user.getUserId(),Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(mAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    public void addUser(View v) {
        String first = mFirstNamesSpinner.getSelectedItem().toString();
        String last = mLastNamesSpinner.getSelectedItem().toString();
        String userId = (first.charAt(0) + last).toLowerCase();

        User newUser = new User(first, last, userId, new Date());

        Toast.makeText(this, "Adding " + first + " " + last, Toast.LENGTH_SHORT).show();
        mDb.collection(USERS)
                .add(newUser)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "User added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding user", e);
                    }
                });
    }
}
