package net.ommoks.azza.gametimemanager;

import static java.util.stream.Collectors.groupingBy;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import net.ommoks.azza.gametimemanager.common.Common;
import net.ommoks.azza.gametimemanager.database.Record;
import net.ommoks.azza.gametimemanager.database.User;
import net.ommoks.azza.gametimemanager.databinding.ActivityGtmBinding;
import net.ommoks.azza.gametimemanager.ui.AddChildDialog;
import net.ommoks.azza.gametimemanager.ui.DataViewModel;
import net.ommoks.azza.gametimemanager.ui.UserListAdapter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GTMActivity extends AppCompatActivity
        implements AddChildDialog.Listener, UserListAdapter.ItemListener {

    private static final String TAG = "GTMActivity";

    final private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private ActivityGtmBinding mBinding;

    private DataViewModel mDataViewModel;
    private UserListAdapter mAdapter;

    private int mWeekIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityGtmBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        mBinding.userList.setLayoutManager(new LinearLayoutManager(this));
        mBinding.userList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        mDataViewModel = new ViewModelProvider(this).get(DataViewModel.class);
        applyViewModel();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add_child) {
            new AddChildDialog()
                    .setListener(this)
                    .show(getSupportFragmentManager(), "add_child");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mExecutorService.shutdown();
    }

    @Override
    public void onChildAdded(String name) {
        mExecutorService.submit(() -> mDataViewModel.addUser(new User(name)));
    }

    @SuppressLint("NotifyDataSetChanged")
    private void applyViewModel() {
        mAdapter = new UserListAdapter(new ArrayList<>(), this);
        mBinding.userList.setAdapter(mAdapter);
        mDataViewModel.users.observe(this, childList -> {
            childList.sort(Comparator.comparingInt(u -> u.id));
            mAdapter.changeDataSet(childList);
        });
        mDataViewModel.fetchAllUsers();

        mDataViewModel.weekIndex.observe(this, weekIndex -> {
            if (weekIndex >= 0) {
                mWeekIndex = weekIndex;
                Log.d(TAG, "Week Index = " + weekIndex);
            }
        });
        mDataViewModel.fetchCurrentWeekIndex();

        mDataViewModel.latestWeekRecords.observe(this, records -> {
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

    // UserListAdapter.ItemListener [[
    @Override
    public void onUserLongClicked(User user) {
        new MaterialAlertDialogBuilder(this)
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
    }
    // UserListAdapter.ItemListener ]]
}
