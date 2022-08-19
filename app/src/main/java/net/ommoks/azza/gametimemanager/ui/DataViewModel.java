package net.ommoks.azza.gametimemanager.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Objects;

public class DataViewModel extends ViewModel {

    private final @NonNull
    MutableLiveData<ArrayList<String>> _childList = new MutableLiveData<>(new ArrayList<>());

    public final @NonNull
    LiveData<ArrayList<String>> childList = _childList;

    public void addChild(String name) {
        Objects.requireNonNull(_childList.getValue()).add(name);
        _childList.postValue(_childList.getValue());
    }
}
