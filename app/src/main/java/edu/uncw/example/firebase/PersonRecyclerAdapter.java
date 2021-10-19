package edu.uncw.example.firebase;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class PersonRecyclerAdapter extends FirestoreRecyclerAdapter<Person, PersonRecyclerAdapter.PersonViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private final SimpleDateFormat format = new SimpleDateFormat("MM-dd-yy", Locale.US);
    private final OnItemClickListener listener;

    PersonRecyclerAdapter(FirestoreRecyclerOptions<Person> options, OnItemClickListener listener) {
        super(options);
        this.listener = listener;
    }

    PersonRecyclerAdapter(FirestoreRecyclerOptions<Person> options) {
        super(options);
        this.listener = null;
    }

    class PersonViewHolder extends RecyclerView.ViewHolder {
        final CardView view;
        final TextView userId;
        final TextView firstName;
        final TextView lastName;
        final TextView createdOn;

        PersonViewHolder(CardView v) {
            super(v);
            view = v;
            userId = v.findViewById(R.id.item_user_id);
            firstName = v.findViewById(R.id.item_first_name);
            lastName = v.findViewById(R.id.item_last_name);
            createdOn = v.findViewById(R.id.item_created_on);
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final PersonViewHolder holder, @NonNull int position, @NonNull final Person person) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.userId.setText(person.getUserId());
        holder.firstName.setText(person.getFirst());
        holder.lastName.setText(person.getLast());
        holder.createdOn.setText(holder.view.getContext()
                .getString(R.string.created_on, format.format(person.getCreatedTime())));
        if (listener != null) {
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(holder.getAbsoluteAdapterPosition());
                }
            });
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PersonViewHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
        // create a new view
        CardView v = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_list_item, parent, false);

        return new PersonViewHolder(v);
    }
}



