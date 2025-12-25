package com.messagelink.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.messagelink.data.NotificationRepository;
import com.messagelink.data.db.NotificationEntity;

import java.util.List;

public class MainViewModel extends AndroidViewModel {
    private final NotificationRepository repo;
    private final MediatorLiveData<List<NotificationEntity>> items = new MediatorLiveData<>();
    private final MediatorLiveData<List<String>> packages = new MediatorLiveData<>();
    private LiveData<List<NotificationEntity>> activeSource;

    private String currentPkg = "";
    private String currentQuery = "";

    public MainViewModel(@NonNull Application application) {
        super(application);
        repo = NotificationRepository.get(application);
        packages.addSource(repo.observePackages(), packages::setValue);
        setFilter("", "");
    }

    public LiveData<List<NotificationEntity>> items() {
        return items;
    }

    public LiveData<List<String>> packages() {
        return packages;
    }

    public LiveData<Integer> count() {
        return repo.observeCount();
    }

    public void setFilter(String pkg, String query) {
        currentPkg = pkg == null ? "" : pkg;
        currentQuery = query == null ? "" : query;

        LiveData<List<NotificationEntity>> next;
        String q = currentQuery.trim();
        String p = currentPkg.trim();
        boolean hasQ = !q.isEmpty();
        boolean hasP = !p.isEmpty();

        if (hasP && hasQ) next = repo.observeByPackageSearch(p, like(q));
        else if (hasP) next = repo.observeByPackage(p);
        else if (hasQ) next = repo.observeSearch(like(q));
        else next = repo.observeAll();

        if (activeSource != null) items.removeSource(activeSource);
        activeSource = next;
        items.addSource(activeSource, items::setValue);
    }

    private String like(String q) {
        String s = q == null ? "" : q.trim();

        s = s.replace("\\", "\\\\");
                s = s.replace("%", "\\%")
                        .replace("_", "\\_");

        return "%" + s + "%";
    }
}
