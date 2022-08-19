package net.ommoks.azza.gametimemanager.ui;

import android.app.Application;

import androidx.annotation.NonNull;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataViewModel extends AndroidViewModel {

    private final RecordDao recordDao;
    private final UserDao userDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final @NonNull
    MutableLiveData<ArrayList<User>> _userList = new MutableLiveData<>(new ArrayList<>());
    private final @NonNull
    MutableLiveData<Integer> _weekIndex = new MutableLiveData<>(0);

    public final @NonNull
    LiveData<ArrayList<User>> userList = _userList;
    public final @NonNull
    LiveData<Integer> weekIndex = _weekIndex;

    public DataViewModel(@NonNull Application application) {
        super(application);
        AppDatabase recordsDb = Room.databaseBuilder(application, AppDatabase.class, "records").build();
        recordDao = recordsDb.recordDao();
        AppDatabase usersDb = Room.databaseBuilder(application, AppDatabase.class, "users").build();
        userDao = usersDb.userDao();
        executorService.submit(() -> _userList.postValue(new ArrayList<>(userDao.getAll())));
    }

    public void addUser(User user) {
        executorService.submit(() -> {
            userDao.insert(user);
            _userList.postValue(new ArrayList<>(userDao.getAll()));
        });
    }

    public void deleteUser(User user) {
        executorService.submit(() -> {
            userDao.delete(user);
            _userList.postValue(new ArrayList<>(userDao.getAll()));
        });
    }

    public void fetchWeekIndex() {
        executorService.submit(() -> {
            Record lastOne = recordDao.getLastOne();
            _weekIndex.postValue(lastOne == null ? 0 : lastOne.weekIndex);
        });
    }
}
