package net.ommoks.azza.gametimemanager.ui;

import static java.util.stream.Collectors.groupingBy;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import net.ommoks.azza.gametimemanager.R;
import net.ommoks.azza.gametimemanager.common.Common;
import net.ommoks.azza.gametimemanager.database.Record;
import net.ommoks.azza.gametimemanager.database.User;
import net.ommoks.azza.gametimemanager.databinding.FragmentMainBinding;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class MainFragment extends Fragment
        implements AddChildDialog.Listener, UserListAdapter.ItemListener{

    private static final String TAG = "GTM/MainFragment";

    private FragmentMainBinding mBinding;

    private DataViewModel mDataViewModel;
    private UserListAdapter mAdapter;
    private int mWeekIndex = 0;

    public MainFragment() {
        // Required empty public constructor
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            // Handle arguments. Currently nothing to do.
        }

        mDataViewModel = new ViewModelProvider(this).get(DataViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        mBinding = FragmentMainBinding.bind(view);
        mBinding.userList.addItemDecoration(new DividerItemDecoration((requireActivity()), DividerItemDecoration.VERTICAL));
        mBinding.topAppBar.setOnMenuItemClickListener(this::onMenuItemClick);

        applyViewModel();
        return view;
    }

    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.add_child) {
            new AddChildDialog()
                    .setListener(this)
                    .show(requireActivity().getSupportFragmentManager(), "add_child");
            return true;
        }
        return false;
    }

    private void applyViewModel() {
        mAdapter = new UserListAdapter(new ArrayList<>(), this);
        mBinding.userList.setAdapter(mAdapter);
        mDataViewModel.users.observe(getViewLifecycleOwner(), childList -> {
            childList.sort(Comparator.comparingInt(u -> u.id));
            mAdapter.changeDataSet(childList);
        });
        mDataViewModel.fetchAllUsers();

        mDataViewModel.weekIndex.observe(getViewLifecycleOwner(), weekIndex -> {
            if (weekIndex >= 0) {
                mWeekIndex = weekIndex;
                Log.d(TAG, "Week Index = " + weekIndex);
            }
        });
        mDataViewModel.fetchCurrentWeekIndex();

        mDataViewModel.latestWeekRecords.observe(getViewLifecycleOwner(), records -> {
            Map<String, List<Record>> groupByName = records.stream()
                    .filter(r -> r.type.equals(Common.DB_RECORD_TYPE_RECORD))
                    .collect(groupingBy(r -> r.user));

            groupByName.forEach((name, records1) -> {
                int totalPlayTime = records1.stream()
                        .mapToInt(r -> r.useTime)
                        .sum();
                mAdapter.addPlayTime(name, totalPlayTime);
            });
        });
        mDataViewModel.fetchLatestWeekRecords();
    }

    // AddChildDialog.Listener [[
    @Override
    public void onChildAdded(String name) {
        mDataViewModel.addUser(new User(name));
    }
    // AddChildDialog.Listener ]]

    // UserListAdapter.ItemListener [[
    @Override
    public void onUserLongClicked(User user) {
        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.do_you_delete_this)
                .setNegativeButton(android.R.string.no,
                        (dialogInterface, i) -> dialogInterface.dismiss())
                .setPositiveButton(android.R.string.yes,
                        (dialogInterface, i) -> mDataViewModel.deleteUser(user))
                .show();
    }

    @Override
    public void onPlayTimeSubmitted(User user, int playTime) {
        mAdapter.addPlayTime(user, playTime);
        Record r = new Record();
        r.weekIndex = mWeekIndex;
        r.type = Common.DB_RECORD_TYPE_RECORD;
        r.timestamp = System.currentTimeMillis();
        r.useTime = playTime;
        r.user = user.name;
        r.comment = "";
        mDataViewModel.insertRecord(r);
        Toast.makeText(requireActivity(),
                getString(R.string.toast_msg_when_adding_time, user.name, String.valueOf(playTime)),
                Toast.LENGTH_SHORT).show();
    }
    // UserListAdapter.ItemListener ]]
}
