package net.ommoks.azza.gametimemanager.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;

import net.ommoks.azza.gametimemanager.R;

import java.util.ArrayList;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {

    final private ArrayList<String> mUserList;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final private MaterialTextView name;
        private AppCompatButton remove;
        private AppCompatTextView time;
        private AppCompatButton add;
        private AppCompatButton done;

        public ViewHolder(View view) {
            super(view);

            name = view.findViewById(R.id.name);
            //TODO: click listener

        }
    }

    public UserListAdapter(ArrayList<String> mUserList) {
        this.mUserList = mUserList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder vh, int position) {
        vh.name.setText(mUserList.get(position));
    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }
}
