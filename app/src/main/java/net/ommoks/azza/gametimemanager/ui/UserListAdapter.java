package net.ommoks.azza.gametimemanager.ui;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;

import net.ommoks.azza.gametimemanager.R;
import net.ommoks.azza.gametimemanager.database.User;

import java.util.ArrayList;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {

    public interface ItemListener {
        void onUserLongClicked(User user);
    }

    private ArrayList<User> mUserList;
    private ItemListener mItemListener;

    private static final int TIME_UNIT = 15;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final private MaterialTextView name;
        private AppCompatButton decrease;
        private AppCompatTextView time;
        private AppCompatButton increase;
        private AppCompatButton done;

        public ViewHolder(View view, final ItemListener listener) {
            super(view);

            name = view.findViewById(R.id.name);
            name.setOnLongClickListener(nameTextView -> {
                if (listener != null) {
                    listener.onUserLongClicked((User) nameTextView.getTag());
                }
                return true;
            });
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

    public UserListAdapter(ArrayList<User> userList, ItemListener listener) {
        mUserList = userList;
        mItemListener = listener;
    }

    public void changeDataSet(ArrayList<User> userList) {
        mUserList = userList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_list_item, parent, false);
        return new ViewHolder(view, mItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder vh, int position) {
        vh.name.setText(mUserList.get(position).name);
        vh.name.setTag(mUserList.get(position));
    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }
}
