package com.messagelink.data;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.messagelink.data.db.AppDatabase;
import com.messagelink.data.db.NotificationDao;
import com.messagelink.data.db.NotificationEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class NotificationRepository {
    private static final String TAG = "NotificationRepository";
    private static volatile NotificationRepository INSTANCE;
    private final NotificationDao dao;
    private final ExecutorService io;

    private NotificationRepository(NotificationDao dao) {
        this.dao = dao;
        // Используем cached thread pool для лучшей производительности
        this.io = Executors.newCachedThreadPool();
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

    // Метод для корректного завершения работы
    public void shutdown() {
        if (io != null && !io.isShutdown()) {
            io.shutdown();
            Log.d(TAG, "ExecutorService shutdown initiated");
        }
    }

    public LiveData<List<NotificationEntity>> observeAll() {
        Log.d(TAG, "observeAll() called");
        return dao.observeAll();
    }

    public LiveData<List<NotificationEntity>> observeByPackage(String pkg) {
        if (pkg == null || pkg.trim().isEmpty()) {
            Log.w(TAG, "observeByPackage called with null or empty package name");
            return dao.observeAll();
        }
        Log.d(TAG, "observeByPackage() called for package: " + pkg);
        return dao.observeByPackage(pkg);
    }

    public LiveData<List<NotificationEntity>> observeSearch(String q) {
        if (q == null) {
            Log.w(TAG, "observeSearch called with null query");
            q = "";
        }
        Log.d(TAG, "observeSearch() called with query: " + q);
        return dao.observeSearch(q);
    }

    public LiveData<List<NotificationEntity>> observeByPackageSearch(String pkg, String q) {
        if (pkg == null || pkg.trim().isEmpty()) {
            Log.w(TAG, "observeByPackageSearch called with invalid package name");
            return dao.observeSearch(q != null ? q : "");
        }

        if (q == null) {
            Log.w(TAG, "observeByPackageSearch called with null search query");
            return dao.observeByPackage(pkg);
        }

        Log.d(TAG, "observeByPackageSearch() called for package: " + pkg + ", query: " + q);
        return dao.observeByPackageSearch(pkg, q);
    }

    public LiveData<List<String>> observePackages() {
        Log.d(TAG, "observePackages() called");
        return dao.observePackages();
    }

    public LiveData<Integer> observeCount() {
        Log.d(TAG, "observeCount() called");
        return dao.observeCount();
    }

    public void insert(NotificationEntity e) {
        if (e == null) {
            Log.e(TAG, "Attempted to insert null NotificationEntity");
            return;
        }

        io.execute(() -> {
            try {
                dao.insert(e);
                Log.d(TAG, "Successfully inserted notification");
            } catch (Exception ex) {
                Log.e(TAG, "Failed to insert notification", ex);
            }
        });
    }

    // Добавим вспомогательный метод для проверки состояния ExecutorService
    public boolean isExecutorActive() {
        return io != null && !io.isShutdown() && !io.isTerminated();
    }
}