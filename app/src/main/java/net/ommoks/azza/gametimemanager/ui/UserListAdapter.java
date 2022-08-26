package net.ommoks.azza.gametimemanager.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
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
import java.util.Optional;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {

    private static final String TAG = "UserListAdapter";

    public interface ItemListener {
        void onUserLongClicked(User user);
        void onPlayTimeSubmitted(User user, int playTime);
        void onPlayTimeClicked(User user);
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
            done.setEnabled(minutes != 0);
        }
    }

    public UserListAdapter(ArrayList<User> userList, ItemListener listener) {
        mUserList = userList;
        mItemListener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void changeDataSet(ArrayList<User> userList) {
        mUserList = userList;
        notifyDataSetChanged();
    }

    public void addPlayTime(String userName, int playTime) {
        if (isValidName(userName)) {
            addPlayTime(getUserByName(userName), playTime);
        } else {
            Log.e(TAG, "addPlayTime() : Can't find user - " + userName);
        }
    }

    private boolean isValidName(String userName) {
        return mUserList.stream()
                .anyMatch(r -> r.name.equals(userName));
    }

    private User getUserByName(String userName) {
        return mUserList.stream()
                .filter(r -> r.name.equals(userName))
                .findFirst()
                .orElse(null);
    }

    public void addPlayTime(User user, int playTime) {
        if (!mPlayTimeMap.containsKey(user.name)) {
            mPlayTimeMap.put(user.name, 0);
        }
        int prevPlayTime = mPlayTimeMap.get(user.name);
        mPlayTimeMap.put(user.name, prevPlayTime + playTime);
        notifyItemChanged(mUserList.indexOf(user));
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

        vh.done.setOnClickListener(doneBtn -> {
            int minutes = Integer.parseInt(vh.time.getText().toString());
            vh.time.setText("0");
            vh.done.setEnabled(false);
            if (mItemListener != null) {
                mItemListener.onPlayTimeSubmitted(mUserList.get(position), minutes);
            }
        });

        if (mPlayTimeMap.containsKey(name)) {
            vh.playTime.setText(getPlayTimeText(vh.playTime.getContext(), mPlayTimeMap.get(name)));
        }
        vh.playTime.setOnClickListener(view -> {
            if (mItemListener != null) {
                mItemListener.onPlayTimeClicked((User) vh.name.getTag());
            }
        });
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

    public int getTotalPlayTime(String name) {
        return Optional.ofNullable(mPlayTimeMap.get(name)).orElse(0);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clearTotalPlayTime() {
        mPlayTimeMap.keySet().forEach(key -> mPlayTimeMap.put(key, 0));
        notifyDataSetChanged();
    }

    public String getSummaryText(Context context) {
        StringBuffer sb = new StringBuffer();
        for (User user : mUserList) {
            sb.append(user.name)
                    .append(" : ")
                    .append(getPlayTimeText(context, getTotalPlayTime(user.name)))
                    .append("\n");
        }
        sb.delete(sb.length() - 1, sb.length());
        return sb.toString();
    }
}
