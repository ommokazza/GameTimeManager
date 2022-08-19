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
import net.ommoks.azza.gametimemanager.databinding.UserListItemBinding;

import java.util.ArrayList;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {

    private final ArrayList<String> mUserList;

    private static final int TIME_UNIT = 15;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private UserListItemBinding mBinding;

        final private MaterialTextView name;
        private AppCompatButton decrease;
        private AppCompatTextView time;
        private AppCompatButton increase;
        private AppCompatButton done;

        public ViewHolder(View view) {
            super(view);

            name = view.findViewById(R.id.name);
            time = view.findViewById(R.id.minutes);
            decrease = view.findViewById(R.id.decrease);
            decrease.setOnClickListener(view1 -> calculateTime(-TIME_UNIT));
            increase = view.findViewById(R.id.increase);
            increase.setOnClickListener(view1 -> calculateTime(TIME_UNIT));
            done = view.findViewById(R.id.done);
            done.setOnClickListener(view12 -> {
                int minutes = Integer.valueOf(time.getText().toString());
                time.setText("0");
                //TODO: call listener to submit time
            });
        }

        private void calculateTime(int value) {
            int minutes = Integer.valueOf(time.getText().toString());
            minutes += value;
            time.setText(String.valueOf(minutes));
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
