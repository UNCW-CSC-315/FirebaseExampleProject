package edu.uncw.example.firebase;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.text.SimpleDateFormat;

public class UserRecyclerAdapter extends FirestoreRecyclerAdapter<User, UserRecyclerAdapter.UserViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private final SimpleDateFormat format = new SimpleDateFormat("MM-dd-yy");
    private final OnItemClickListener listener;

    UserRecyclerAdapter(FirestoreRecyclerOptions<User> options, OnItemClickListener listener) {
        super(options);
        this.listener = listener;
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        final CardView view;
        final TextView userId;
        final TextView firstName;
        final TextView lastName;
        final TextView createdOn;

        UserViewHolder(CardView v) {
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
    public void onBindViewHolder(final UserRecyclerAdapter.UserViewHolder holder, @NonNull int position, @NonNull final User user) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.userId.setText(user.getUserId());
        holder.firstName.setText(user.getFirst());
        holder.lastName.setText(user.getLast());
        holder.createdOn.setText(holder.view.getContext()
                .getString(R.string.created_on, format.format(user.getCreatedTime())));
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(holder.getAdapterPosition());
            }
        });
    }

    // Create new views (invoked by the layout manager)
    @Override
    public UserRecyclerAdapter.UserViewHolder onCreateViewHolder(ViewGroup parent,
                                                                 int viewType) {
        // create a new view
        CardView v = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_list_item, parent, false);

        return new UserViewHolder(v);
    }
}



