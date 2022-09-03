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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataViewModel extends AndroidViewModel {

    private final RecordDao recordDao;
    private final UserDao userDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final @NonNull
    MutableLiveData<ArrayList<User>> _users = new MutableLiveData<>(null);
    private final @NonNull
    MutableLiveData<Integer> _weekIndex = new MutableLiveData<>(null);
    private final @NonNull
    MutableLiveData<List<Record>> _weekRecords = new MutableLiveData<>(null);
    private final @NonNull
    MutableLiveData<User> _userAdded = new MutableLiveData<>(null);
    private final @NonNull
    MutableLiveData<User> _userDeleted = new MutableLiveData<>(null);


    public final @NonNull
    LiveData<ArrayList<User>> users = _users;
    public final @NonNull
    LiveData<Integer> weekIndex = _weekIndex;
    public final @NonNull
    LiveData<List<Record>> weekRecords = _weekRecords;
    public final @NonNull
    LiveData<User> userAdded = _userAdded;
    public final @NonNull
    LiveData<User> userDeleted = _userDeleted;

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
    public List<User> getAllUsers() {
        return userDao.getAll();
    }

    public void addNewUser(String name) {
        executorService.submit(() -> {
            User user = new User(name);
            user.id = userDao.insert(user);
            _users.postValue(new ArrayList<>(userDao.getAll()));
            _userAdded.postValue(user);
        });
    }

    public void deleteUser(User user) {
        executorService.submit(() -> {
            userDao.delete(user);
            _users.postValue(new ArrayList<>(userDao.getAll()));
            _userDeleted.postValue(user);
        });
    }
}
