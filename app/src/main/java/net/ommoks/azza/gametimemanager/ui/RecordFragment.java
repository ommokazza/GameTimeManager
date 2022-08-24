package net.ommoks.azza.gametimemanager.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;

import net.ommoks.azza.gametimemanager.R;
import net.ommoks.azza.gametimemanager.common.Common;
import net.ommoks.azza.gametimemanager.database.Record;
import net.ommoks.azza.gametimemanager.databinding.FragmentRecordBinding;

import java.util.List;
import java.util.stream.Collectors;

public class RecordFragment extends Fragment {

    private static final String ARG_KEY_NAME = "name";

    private DataViewModel mDataViewModel;
    FragmentRecordBinding mBinding;
    private String mName;

    private int mWeekIndex;
    private int mLastWeekIndex;

    public RecordFragment() {
        // Required empty public constructor
    }

    public static RecordFragment newInstance(String name) {
        RecordFragment fragment = new RecordFragment();
        Bundle args = new Bundle();
        args.putString(ARG_KEY_NAME, name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mName = getArguments().getString(ARG_KEY_NAME, "");
        }

        mDataViewModel = new ViewModelProvider(this).get(DataViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);
        mBinding = FragmentRecordBinding.bind(view);
        mBinding.topAppBar.setTitle(getString(R.string.record_list_title)
                + ((mName == null) ? "" : (" - " + mName)));
        mBinding.topAppBar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        mBinding.recordList.addItemDecoration(
                new DividerItemDecoration((requireActivity()), DividerItemDecoration.VERTICAL));
        loadRecordData();

        //TODO: Week Navigation

        return view;
    }

    private void loadRecordData() {
        new Thread(() -> {
            mWeekIndex = mDataViewModel.getCurrentWeekIndex();
            mLastWeekIndex = mWeekIndex;

            List<Record> recordList = mDataViewModel.getRecordsWithWeekIndex(mWeekIndex).stream()
                    .filter(r -> (r.type.equals(Common.DB_RECORD_TYPE_COMMENT)) || (mName.equals(r.user)))
                    .collect(Collectors.toList());
            final RecordListAdapter adapter = new RecordListAdapter(recordList);
            mBinding.recordList.post(() -> mBinding.recordList.setAdapter(adapter));
        }).start();

    }

    @Override
    public void onResume() {
        super.onResume();
    }
}