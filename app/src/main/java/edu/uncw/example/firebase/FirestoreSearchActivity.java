package edu.uncw.example.firebase;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class FirestoreSearchActivity extends AppCompatActivity {

    private static final String TAG = "DisplayActivity";
    private static final String PEOPLE = "people";

    private final FirebaseFirestore mDb = FirebaseFirestore.getInstance();
    private EditText mSearchBox;

    private PersonRecyclerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firestore_search);

        mSearchBox = findViewById(R.id.searchBox);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        Query query = mDb.collection(PEOPLE)
                .orderBy("createdTime", Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<Person> options = new FirestoreRecyclerOptions.Builder<Person>()
                .setQuery(query, Person.class)
                .build();

        mAdapter = new PersonRecyclerAdapter(options, new PersonRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Person person = mAdapter.getSnapshots().getSnapshot(position).toObject(Person.class);
                String id = mAdapter.getSnapshots().getSnapshot(position).getId();
                mDb.collection(PEOPLE).document(id).delete();

                Toast.makeText(getApplicationContext(),"Deleting " + person.getUserId(),Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(mAdapter);


        mSearchBox = findViewById(R.id.searchBox);
        mSearchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "Searchbox changed to: " + s.toString());
                Query query = mDb.collection(PEOPLE)
                        .whereEqualTo("last", s.toString())
                        .orderBy("createdTime", Query.Direction.ASCENDING);
                FirestoreRecyclerOptions<Person> options = new FirestoreRecyclerOptions.Builder<Person>()
                        .setQuery(query, Person.class)
                        .build();
                mAdapter.updateOptions(options);
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
