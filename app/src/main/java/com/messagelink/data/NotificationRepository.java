package com.messagelink.data;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.messagelink.data.db.AppDatabase;
import com.messagelink.data.db.NotificationDao;
import com.messagelink.data.db.NotificationEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class NotificationRepository {
    private static volatile NotificationRepository INSTANCE;
    private final NotificationDao dao;
    private final ExecutorService io;

    private NotificationRepository(NotificationDao dao) {
        this.dao = dao;
        this.io = Executors.newSingleThreadExecutor();
    }

    public static NotificationRepository get(Context c) {
        if (INSTANCE == null) {
            synchronized (NotificationRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new NotificationRepository(AppDatabase.get(c).notificationDao());
                }
            }
        }
        return INSTANCE;
    }

    public ExecutorService io() {
        return io;
    }

    public LiveData<List<NotificationEntity>> observeAll() {
        return dao.observeAll();
    }

    public LiveData<List<NotificationEntity>> observeByPackage(String pkg) {
        return dao.observeByPackage(pkg);
    }

    public LiveData<List<NotificationEntity>> observeSearch(String q) {
        return dao.observeSearch(q);
    }

    public LiveData<List<NotificationEntity>> observeByPackageSearch(String pkg, String q) {
        return dao.observeByPackageSearch(pkg, q);
    }

    public LiveData<List<String>> observePackages() {
        return dao.observePackages();
    }

    public LiveData<Integer> observeCount() {
        return dao.observeCount();
    }

    public void insert(NotificationEntity e) {
        io.execute(() -> dao.insert(e));
    }

    public void clear() {
        io.execute(() -> dao.clear());
    }
    public void cleanupOld(long olderThanTimestamp) {
        io.execute(() -> dao.deleteOlderThan(olderThanTimestamp));
    }
}
