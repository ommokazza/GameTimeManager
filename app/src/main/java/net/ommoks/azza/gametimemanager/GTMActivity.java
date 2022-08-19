package net.ommoks.azza.gametimemanager;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.room.Room;

import net.ommoks.azza.gametimemanager.database.AppDatabase;
import net.ommoks.azza.gametimemanager.database.Record;
import net.ommoks.azza.gametimemanager.database.RecordDao;
import net.ommoks.azza.gametimemanager.databinding.ActivityGtmBinding;
import net.ommoks.azza.gametimemanager.ui.UserListAdapter;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GTMActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private ActivityGtmBinding mBinding;

    private RecordDao mRecordDao;
    private int mWeekIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityGtmBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mBinding.userList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "records").build();
        mRecordDao = db.recordDao();
        asyncInit();
    }

    private void asyncInit() {
        executorService.submit(() -> {
            Record lastOne = mRecordDao.getLastOne();
            if (lastOne != null) {
                mWeekIndex = lastOne.weekIndex;
            } else {
                Log.e(TAG, "getLastOne() returned null");
            }
            Log.d(TAG, "current week index = " + mWeekIndex);

            //TODO: Manage current week data
            List<Record> currentWeekRecords = mRecordDao.getAllWithWeekIndex(mWeekIndex);

            //Test [[
            ArrayList<String> userList = new ArrayList<>();
            userList.add("1.한결");
            userList.add("2.소은");
            userList.add("3.시원");
            UserListAdapter adapter = new UserListAdapter(userList);
            //Test ]]
            mBinding.userList.setLayoutManager(new LinearLayoutManager(this));
            mBinding.userList.setAdapter(adapter);

        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Test [[
//        Record r = new Record();
//        r.weekIndex = weekIndex;
//        r.type = 1;
//        r.timestamp = System.currentTimeMillis();
//        r.useTime = 15;
//        r.user = "한결";
//        r.comment = "";
//        insertRecord(r);
//
//        executorService.submit(new Runnable() {
//            @Override
//            public void run() {
//                for (Record r : recordDao.getAll()) {
//                    Log.d(TAG, r.toString());
//                }
//            }
//        });
        //Test]]
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    private void insertRecord(final Record record) {
        executorService.submit(() -> mRecordDao.insert(record));
    }
}