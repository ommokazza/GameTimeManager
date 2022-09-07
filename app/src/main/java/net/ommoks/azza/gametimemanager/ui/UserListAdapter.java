package net.ommoks.azza.gametimemanager.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;

import net.ommoks.azza.gametimemanager.R;
import net.ommoks.azza.gametimemanager.common.Common;
import net.ommoks.azza.gametimemanager.database.Record;
import net.ommoks.azza.gametimemanager.database.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {

    private static final String TAG = "UserListAdapter";

    public interface ItemListener {
        void onUserLongClicked(User user);
        void onPlayTimeSubmitted(User user, int playTime);
        void onPlayTimeClicked(User user);
    }

    private ArrayList<User> mUserList;
    private final Map<User, Integer> mPlayTimeMap = new HashMap<>();
    private final ItemListener mItemListener;

    private static final int TIME_UNIT = 15;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final private MaterialTextView name;
        private final AppCompatButton minus;
        private final AppCompatTextView time;
        private final AppCompatButton plus;
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
            minus = view.findViewById(R.id.minus);
            minus.setOnClickListener(view1 -> calculateTime(-TIME_UNIT));
            plus = view.findViewById(R.id.plus);
            plus.setOnClickListener(view1 -> calculateTime(TIME_UNIT));
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

    public UserListAdapter(ItemListener listener) {
        mUserList = new ArrayList<>();
        mItemListener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void initializeDataSet(List<User> userList, List<Record> recordList) {
        mUserList = new ArrayList<>(userList);
        mUserList.forEach(u -> mPlayTimeMap.put(u, 0));

        recordList.stream()
                .filter(r -> r.type.equals(Common.DB_RECORD_TYPE_RECORD))
                .sequential()
                .forEach(r -> {
                    User user = getUserByName(r.user);
                    if (user != null) {
                        int prevPlayTime = Optional.ofNullable(mPlayTimeMap.get(user)).orElse(0);
                        mPlayTimeMap.put(user, prevPlayTime + r.useTime);
                    }
                });
        notifyDataSetChanged();
    }

    public void addNewUser(User user) {
        mUserList.add(user);
        mPlayTimeMap.put(user, 0);
        notifyItemInserted(mUserList.indexOf(user));
    }

    public void deleteUser(User user) {
        int position = mUserList.indexOf(user);
        if (position > -1) {
            mUserList.remove(position);
            mPlayTimeMap.remove(user);
            notifyItemRemoved(position);
        }
    }

    private User getUserByName(String userName) {
        return mUserList.stream()
                .filter(r -> r.name.equals(userName))
                .findFirst()
                .orElse(null);
    }

    public void addPlayTime(User user, int playTime) {
        if (mPlayTimeMap.containsKey(user)) {
            int prevPlayTime = Objects.requireNonNull(mPlayTimeMap.get(user));
            mPlayTimeMap.put(user, prevPlayTime + playTime);
            notifyItemChanged(mUserList.indexOf(user));
        } else {
            Log.e(TAG, "Error : Invalid User");
        }
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
        Context context = vh.itemView.getContext();

        User user = mUserList.get(position);
        vh.name.setText(user.name);
        vh.name.setContentDescription(user.name);
        vh.name.setTag(user);

        vh.minus.setContentDescription(vh.name + " " + context.getString(R.string.minus));
        vh.plus.setContentDescription(vh.name + " " + context.getString(R.string.plus));

        vh.done.setOnClickListener(doneBtn -> {
            int minutes = Integer.parseInt(vh.time.getText().toString());
            vh.time.setText("0");
            vh.done.setEnabled(false);
            if (mItemListener != null) {
                mItemListener.onPlayTimeSubmitted(mUserList.get(position), minutes);
            }
        });
        int useTime = Objects.requireNonNull(mPlayTimeMap.get(user));
        vh.done.setContentDescription(vh.name + " " + context.getString(R.string.add_minutes, String.valueOf(useTime)));

        vh.playTime.setText(getPlayTimeText(vh.playTime.getContext(), useTime));
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

    public int getTotalPlayTime(User user) {
        return Optional.ofNullable(mPlayTimeMap.get(user)).orElse(0);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clearTotalPlayTime() {
        mPlayTimeMap.keySet().forEach(key -> mPlayTimeMap.put(key, 0));
        notifyDataSetChanged();
    }

    @Nullable
    public String getSummaryText(Context context) {
        if (mUserList == null || mUserList.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (User user : mUserList) {
            sb.append(user.name)
                    .append(" : ")
                    .append(getPlayTimeText(context, getTotalPlayTime(user)))
                    .append("\n");
        }
        sb.delete(sb.length() - 1, sb.length());
        return sb.toString();
    }

    public boolean checkDuplicated(String name) {
        return mUserList.stream()
                .map(u -> u.name)
                .anyMatch(n -> n.equals(name));
    }
}
