package net.ommoks.azza.gametimemanager.ui;

import android.content.Context;
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
import java.util.HashMap;
import java.util.Map;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {

    public interface ItemListener {
        void onUserLongClicked(User user);
        void onPlayTimeSubmitted(User user, int playTime);
    }

    private ArrayList<User> mUserList;
    private final Map<String, Integer> mPlayTimeMap = new HashMap<>();
    private final ItemListener mItemListener;

    private static final int TIME_UNIT = 15;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final private MaterialTextView name;
        private final AppCompatTextView time;
        private final AppCompatButton done;
        private final AppCompatButton playTime;

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
            AppCompatButton decrease = view.findViewById(R.id.decrease);
            decrease.setOnClickListener(view1 -> calculateTime(-TIME_UNIT));
            AppCompatButton increase = view.findViewById(R.id.increase);
            increase.setOnClickListener(view1 -> calculateTime(TIME_UNIT));
            done = view.findViewById(R.id.done);
            playTime = view.findViewById(R.id.play_time);
        }

        private void calculateTime(int value) {
            int minutes = Integer.parseInt(time.getText().toString());
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

    public void addPlayTime(String name, int playTime) {
        if (!mPlayTimeMap.containsKey(name)) {
            mPlayTimeMap.put(name, 0);
        }
        int prevPlayTime = mPlayTimeMap.get(name);
        mPlayTimeMap.put(name, prevPlayTime + playTime);
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
        String name = mUserList.get(position).name;
        vh.name.setText(name);
        vh.name.setTag(mUserList.get(position));

        vh.done.setOnClickListener(view12 -> {
            int minutes = Integer.parseInt(vh.time.getText().toString());
            vh.time.setText("0");
            if (mItemListener != null) {
                mItemListener.onPlayTimeSubmitted(mUserList.get(position), minutes);
            }
        });

        if (!mPlayTimeMap.containsKey(name)) {
            mPlayTimeMap.put(name, 0);
        }
        vh.playTime.setText(getPlayTimeText(vh.playTime.getContext(), mPlayTimeMap.get(name)));

    }

    private String getPlayTimeText(Context context, Integer playTime) {
        return context.getString(R.string.play_time,
                String.valueOf(playTime / 60),
                String.valueOf(playTime % 60));
    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }
}
