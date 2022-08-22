package net.ommoks.azza.gametimemanager;

import android.annotation.SuppressLint;
import android.os.Build;
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

import net.ommoks.azza.gametimemanager.database.User;
import net.ommoks.azza.gametimemanager.databinding.ActivityGtmBinding;
import net.ommoks.azza.gametimemanager.ui.AddChildDialog;
import net.ommoks.azza.gametimemanager.ui.DataViewModel;
import net.ommoks.azza.gametimemanager.ui.UserListAdapter;

import java.util.ArrayList;
import java.util.Comparator;
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
        mDataViewModel.weekIndex.observe(this, integer -> {
            mWeekIndex = integer;
            Log.d(TAG, "Week Index = " + integer);
        });
        mDataViewModel.fetchWeekIndex();

        mAdapter = new UserListAdapter(new ArrayList<>(), this);
        mBinding.userList.setAdapter(mAdapter);
        mDataViewModel.userList.observe(this, childList -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                childList.sort(Comparator.comparingInt(u -> u.id));
            }
            mAdapter.changeDataSet(childList);
        });
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
    }
    // UserListAdapter.ItemListener ]]
}
