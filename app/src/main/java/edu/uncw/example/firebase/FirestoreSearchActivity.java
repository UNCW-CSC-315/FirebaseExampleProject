package edu.uncw.example.firebase;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class FirestoreSearchActivity extends AppCompatActivity {

    private static final String TAG = "FirestoreSearchActivity";
    private static final String PEOPLE = "people";

    private final FirebaseFirestore mDb = FirebaseFirestore.getInstance();

    private PersonRecyclerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firestore_search);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        Query query = mDb.collection(PEOPLE)
                .orderBy("createdTime", Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<Person> options = new FirestoreRecyclerOptions.Builder<Person>()
                .setQuery(query, Person.class)
                .build();

        mAdapter = new PersonRecyclerAdapter(options);
        recyclerView.setAdapter(mAdapter);

        EditText searchBox = findViewById(R.id.searchBox);
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "Searchbox has changed to: " + s.toString());
                Query query;
                if (s.toString().isEmpty()) {
                    query = mDb.collection(PEOPLE)
                            .orderBy("createdTime", Query.Direction.ASCENDING);
                } else {
                    query = mDb.collection(PEOPLE)
                            .whereEqualTo("last", s.toString())
                            .orderBy("createdTime", Query.Direction.ASCENDING);
                }
                FirestoreRecyclerOptions<Person> options = new FirestoreRecyclerOptions.Builder<Person>()
                        .setQuery(query, Person.class)
                        .build();
                mAdapter.updateOptions(options);
                mAdapter.notifyDataSetChanged();
            }
        });

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
}
