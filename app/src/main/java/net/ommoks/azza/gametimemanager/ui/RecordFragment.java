package net.ommoks.azza.gametimemanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;

import net.ommoks.azza.gametimemanager.R;
import net.ommoks.azza.gametimemanager.common.Common;
import net.ommoks.azza.gametimemanager.database.Record;
import net.ommoks.azza.gametimemanager.databinding.FragmentRecordBinding;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class RecordFragment extends Fragment {

    private static final String ARG_KEY_NAME = "name";
    private static final String ARG_KEY_WEEK_INDEX = "week_index";

    private DataViewModel mDataViewModel;
    FragmentRecordBinding mBinding;
    private RecordListAdapter mAdapter = new RecordListAdapter(Collections.emptyList());

    private String mName;
    private int mLastWeekIndex;

    private MutableLiveData<Integer> _mWeekIndex;
    private LiveData<Integer> mWeekIndex;

    public RecordFragment() {
        setHasOptionsMenu(true);
    }

    public static RecordFragment newInstance(String name, int weekIndex) {
        RecordFragment fragment = new RecordFragment();
        Bundle args = new Bundle();
        args.putString(ARG_KEY_NAME, name);
        args.putInt(ARG_KEY_WEEK_INDEX, weekIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mName = getArguments().getString(ARG_KEY_NAME, "");
            mLastWeekIndex = getArguments().getInt(ARG_KEY_WEEK_INDEX, 0);
            _mWeekIndex = new MutableLiveData<>(mLastWeekIndex);
            mWeekIndex = _mWeekIndex;
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
        mBinding.topAppBar.setOnMenuItemClickListener(this::onMenuItemClick);

        updateNavigationButtons(mLastWeekIndex);
        mWeekIndex.observe(getViewLifecycleOwner(), this::weekIndexChanged);
        mBinding.prev.setOnClickListener(this::onPrevNextClickListener);
        mBinding.next.setOnClickListener(this::onPrevNextClickListener);

        mBinding.recordList.addItemDecoration(
                new DividerItemDecoration((requireActivity()), DividerItemDecoration.VERTICAL));
        mBinding.recordList.setAdapter(mAdapter);

        loadViewData();
        return view;
    }

    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, getRecordListText());
            sendIntent.setType("text/plain");

            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
            return true;
        } else {
            return false;
        }
    }

    private String getRecordListText() {
        return mAdapter.getRecordListText(requireActivity());
    }

    private void loadViewData() {
        new Thread(() -> {
            mDataViewModel.fetchRecordsWithWeekIndex(mLastWeekIndex);
            _mWeekIndex.postValue(mLastWeekIndex);
        }).start();
        applyViewModel();
    }

    private void applyViewModel() {
        mDataViewModel.weekRecords.observe(getViewLifecycleOwner(), this::onWeekRecordsChanged);
    }

    private void weekIndexChanged(int weekIndex) {
        mDataViewModel.fetchRecordsWithWeekIndex(weekIndex);
        updateNavigationButtons(weekIndex);
    }

    private void updateNavigationButtons(int weekIndex) {
        mBinding.prev.setEnabled(weekIndex > 0);
        mBinding.next.setEnabled(weekIndex < mLastWeekIndex);
    }

    private void onPrevNextClickListener(View v) {
        int weekIndex = Optional.ofNullable(mWeekIndex.getValue()).orElse(0);
        if (v.getId() == R.id.prev) {
            if (weekIndex > 0) {
                _mWeekIndex.postValue(weekIndex - 1);
            }
        } else if (v.getId() == R.id.next) {
            if (weekIndex < mLastWeekIndex) {
                _mWeekIndex.postValue(weekIndex + 1);
            }
        }
    }

    private List<Record> filterRecordList(List<Record> recordList) {
        return recordList.stream()
                .filter(r -> (r.type.equals(Common.DB_RECORD_TYPE_COMMENT)) || (mName.equals(r.user)))
                .collect(Collectors.toList());
    }

    private void onWeekRecordsChanged(List<Record> records) {
        Objects.requireNonNull(mWeekIndex.getValue());
        mBinding.recordList.post(() -> mAdapter.changeDataSet(filterRecordList(records)));
    }
}