package net.ommoks.azza.gametimemanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import net.ommoks.azza.gametimemanager.R;
import net.ommoks.azza.gametimemanager.common.Common;
import net.ommoks.azza.gametimemanager.database.Record;
import net.ommoks.azza.gametimemanager.database.User;
import net.ommoks.azza.gametimemanager.databinding.FragmentMainBinding;

import java.util.List;

public class MainFragment extends Fragment
        implements AddChildDialog.Listener, UserListAdapter.ItemListener{

    private static final String TAG = "GTM/MainFragment";

    private FragmentMainBinding mBinding;

    private DataViewModel mDataViewModel;
    private UserListAdapter mAdapter;
    private int mWeekIndex = 0;

    public MainFragment() {
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

        initialize();
        return view;
    }

    private void initialize() {
        mAdapter = new UserListAdapter(this);
        mBinding.userList.setAdapter(mAdapter);
        applyViewModel();

        // Asynchronous Initialize
        new Thread(() -> {
            List<User> userList = mDataViewModel.getAllUsers();
            mWeekIndex = mDataViewModel.getLastWeekIndex();
            List<Record> lastWeekRecords = mDataViewModel.getRecordsWithWeekIndex(mWeekIndex);

            requireActivity().runOnUiThread(() -> mAdapter.initializeDataSet(userList, lastWeekRecords));
        }).start();
    }

    private void applyViewModel() {
        mDataViewModel.userAdded.observe(getViewLifecycleOwner(), new Observer<User>() {
                    @Override
                    public void onChanged(User user) {
                        mAdapter.addNewUser(user);
                    }
                });

        mDataViewModel.userDeleted.observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                mAdapter.deleteUser(user);
            }
        });
    }

    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.add_child) {
            new AddChildDialog()
                    .setListener(this)
                    .show(requireActivity().getSupportFragmentManager(), "add_child");
            return true;
        } else if (item.getItemId() == R.id.new_week) {
            new MaterialAlertDialogBuilder(requireActivity())
                    .setTitle(R.string.start_a_new_week)
                    .setPositiveButton(R.string.ok, (di, which) -> startNewWeek())
                    .setNegativeButton(R.string.cancel, (di, which) -> di.dismiss())
                    .show();
            return true;
        } else if (item.getItemId() == R.id.share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, getSummaryText());
            sendIntent.setType("text/plain");

            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
            return true;
        } else {
            return false;
        }
    }

    private String getSummaryText() {
        return mAdapter.getSummaryText(requireActivity());
    }

    private void startNewWeek() {
        Toast.makeText(requireActivity(), R.string.start_a_new_week, Toast.LENGTH_SHORT).show();
        long timestamp = System.currentTimeMillis();

        // Record summary of this week
        Record end = Record.newRecord(mWeekIndex, Common.DB_RECORD_TYPE_COMMENT);
        end.timestamp = ++timestamp;
        end.comment = getString(R.string.end_this_week);
        mDataViewModel.insertRecord(end);

        mWeekIndex++;
        mAdapter.clearTotalPlayTime();

        // Record notice for new week
        Record notice = Record.newRecord(mWeekIndex, Common.DB_RECORD_TYPE_COMMENT);
        notice.timestamp = ++timestamp;
        notice.comment = getString(R.string.start_a_new_week);
        mDataViewModel.insertRecord(notice);
    }

    // AddChildDialog.Listener [[
    @Override
    public void onChildAdded(String name) {
        mDataViewModel.addNewUser(name);
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

    @Override
    public void onPlayTimeClicked(User user) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in, 0, 0, R.anim.slide_out)
                .add(R.id.fragment_container, RecordFragment.newInstance(user.name, mWeekIndex))
                .setReorderingAllowed(true)
                .addToBackStack("home")
                .commit();
    }
    // UserListAdapter.ItemListener ]]
}
