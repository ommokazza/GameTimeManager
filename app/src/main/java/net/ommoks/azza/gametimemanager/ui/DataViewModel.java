package net.ommoks.azza.gametimemanager.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Room;

import net.ommoks.azza.gametimemanager.database.AppDatabase;
import net.ommoks.azza.gametimemanager.database.Record;
import net.ommoks.azza.gametimemanager.database.RecordDao;
import net.ommoks.azza.gametimemanager.database.User;
import net.ommoks.azza.gametimemanager.database.UserDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataViewModel extends AndroidViewModel {

    private final RecordDao recordDao;
    private final UserDao userDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final @NonNull
    MutableLiveData<ArrayList<User>> _users = new MutableLiveData<>(new ArrayList<>());
    private final @NonNull
    MutableLiveData<Integer> _weekIndex = new MutableLiveData<>(-1);
    private final @NonNull
    MutableLiveData<List<Record>> _weekRecords = new MutableLiveData<>(new ArrayList<>());

    public final @NonNull
    LiveData<ArrayList<User>> users = _users;
    public final @NonNull
    LiveData<Integer> weekIndex = _weekIndex;
    public final @NonNull
    LiveData<List<Record>> weekRecords = _weekRecords;

    public DataViewModel(@NonNull Application application) {
        super(application);
        AppDatabase recordsDb = Room.databaseBuilder(application, AppDatabase.class, "records").build();
        recordDao = recordsDb.recordDao();
        AppDatabase usersDb = Room.databaseBuilder(application, AppDatabase.class, "users").build();
        userDao = usersDb.userDao();
    }

    public void insertRecord(Record r) {
        executorService.submit(() -> recordDao.insert(r));
    }

    public void fetchLastWeekIndex() {
        executorService.submit(() -> {
            Record lastOne = recordDao.getLastOne();
            _weekIndex.postValue(lastOne == null ? 0 : lastOne.weekIndex);
        });
    }

    @WorkerThread
    public int getLastWeekIndex() {
        return Optional.ofNullable(recordDao.getLastOne())
                .map(record -> record.weekIndex).orElse(0);
    }

    public void fetchRecordsWithWeekIndex(int weekIndex) {
        executorService.submit(() -> {
            List<Record> result = recordDao.getAllWithWeekIndex(weekIndex);
            result.sort((r1, r2) -> (int) (r1.timestamp - r2.timestamp));
            _weekRecords.postValue(result);
        });
    }

    @WorkerThread
    public List<Record> getRecordsWithWeekIndex(int weekIndex) {
        List<Record> result = recordDao.getAllWithWeekIndex(weekIndex);
        result.sort((r1, r2) -> (int) (r1.timestamp - r2.timestamp));
        return result;
    }

    public void fetchAllUsers() {
        executorService.submit(() -> _users.postValue(new ArrayList<>(userDao.getAll())));
    }

    @WorkerThread
    public ArrayList<User> getAllUsers() {
        return new ArrayList<>(userDao.getAll());
    }

    public void addUser(User user) {
        executorService.submit(() -> {
            userDao.insert(user);
            _users.postValue(new ArrayList<>(userDao.getAll()));
        });
    }

    public void deleteUser(User user) {
        executorService.submit(() -> {
            userDao.delete(user);
            _users.postValue(new ArrayList<>(userDao.getAll()));
        });
    }
}
