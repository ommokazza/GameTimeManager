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
import androidx.room.Room;

import net.ommoks.azza.gametimemanager.database.AppDatabase;
import net.ommoks.azza.gametimemanager.database.Record;
import net.ommoks.azza.gametimemanager.database.RecordDao;
import net.ommoks.azza.gametimemanager.database.User;
import net.ommoks.azza.gametimemanager.database.UserDao;
import net.ommoks.azza.gametimemanager.databinding.ActivityGtmBinding;
import net.ommoks.azza.gametimemanager.ui.AddChildDialog;
import net.ommoks.azza.gametimemanager.ui.DataViewModel;
import net.ommoks.azza.gametimemanager.ui.UserListAdapter;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GTMActivity extends AppCompatActivity implements AddChildDialog.Listener {

    private static final String TAG = "MainActivity";

    final private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private ActivityGtmBinding mBinding;

    private DataViewModel mDataViewModel;
    private UserListAdapter mAdapter;

    private RecordDao mRecordDao;
    private UserDao mUserDao;
    private int mWeekIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityGtmBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        mBinding.userList.setLayoutManager(new LinearLayoutManager(this));
        mBinding.userList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        AppDatabase recordsDb = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "records").build();
        mRecordDao = recordsDb.recordDao();
        AppDatabase usersDb = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "users").build();
        mUserDao = usersDb.userDao();

        mDataViewModel = new ViewModelProvider(this).get(DataViewModel.class);
        applyViewModel();
        asyncInit();
    }

    private void asyncInit() {
        mExecutorService.submit(() -> {
            // Add child list
            List<User> childList = mUserDao.getAll();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                childList.sort(Comparator.comparingInt(u -> u.id));
            }
            for (User child : childList) {
                mDataViewModel.addChild(child.name);
            }

            // Read record data from DB
            Record lastOne = mRecordDao.getLastOne();
            if (lastOne != null) {
                mWeekIndex = lastOne.weekIndex;
            } else {
                Log.e(TAG, "getLastOne() returned null");
            }
            Log.d(TAG, "current week index = " + mWeekIndex);

        });
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

    private void insertRecord(final Record record) {
        mExecutorService.submit(() -> mRecordDao.insert(record));
    }

    @Override
    public void onChildAdded(String name) {
        mExecutorService.submit(() -> {
            mUserDao.insert(new User(name));
            mDataViewModel.addChild(name);
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void applyViewModel() {
        mAdapter = new UserListAdapter(mDataViewModel.childList.getValue());
        mBinding.userList.setAdapter(mAdapter);
        mDataViewModel.childList.observe(this, childList -> mAdapter.notifyDataSetChanged());
    }
}