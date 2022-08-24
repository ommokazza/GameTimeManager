package net.ommoks.azza.gametimemanager.ui;

import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;

import net.ommoks.azza.gametimemanager.R;
import net.ommoks.azza.gametimemanager.common.Common;
import net.ommoks.azza.gametimemanager.database.Record;

import java.util.List;

public class RecordListAdapter extends RecyclerView.Adapter<RecordListAdapter.ViewHolder> {

    private static final String TAG = "RecordListAdapter";

    private List<Record> mRecordList;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private MaterialTextView timestamp;
        private MaterialTextView name;
        private MaterialTextView log;

        public ViewHolder(View view) {
            super(view);
            timestamp = view.findViewById(R.id.timestamp);
            name = view.findViewById(R.id.name);
            log = view.findViewById(R.id.log);
        }
    }

    public RecordListAdapter(List<Record> recordList) {
        mRecordList = recordList;
    }

    public void changeDataSet(List<Record> recordList) {
        mRecordList = recordList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.record_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder vh, int position) {
        Record record = mRecordList.get(position);
        Resources res = vh.itemView.getResources();

        vh.timestamp.setText(record.getDateTime());
        vh.name.setText(record.user);
        if (record.type.equals(Common.DB_RECORD_TYPE_RECORD)) {
            vh.log.setText(res.getString(R.string.add_minutes, String.valueOf(record.useTime)));
        } else if (record.type.equals(Common.DB_RECORD_TYPE_SUMMARY)) {
            vh.log.setText(getPlayTimeText(res, record.useTime));
        } else if (record.type.equals(Common.DB_RECORD_TYPE_COMMENT)) {
            vh.log.setText(record.comment);
        } else {
            Log.e(TAG, "What's the problem?");
        }
    }

    private String getPlayTimeText(Resources res, Integer playTime) {
        return res.getString(R.string.play_time_2,
                String.valueOf(playTime / 60),
                String.valueOf(playTime % 60));
    }

    @Override
    public int getItemCount() {
        return mRecordList.size();
    }
}