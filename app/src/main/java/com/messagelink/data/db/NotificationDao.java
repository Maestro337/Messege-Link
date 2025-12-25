package com.messagelink.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface NotificationDao {
    @Insert
    long insert(NotificationEntity e);

    @Query("DELETE FROM notifications")
    void clear();

    @Query("SELECT * FROM notifications ORDER BY postedAt DESC")
    LiveData<List<NotificationEntity>> observeAll();

    @Query("SELECT * FROM notifications WHERE packageName = :pkg ORDER BY postedAt DESC")
    LiveData<List<NotificationEntity>> observeByPackage(String pkg);

    @Query("SELECT * FROM notifications WHERE (title LIKE :q OR text LIKE :q OR appName LIKE :q) ORDER BY postedAt DESC")
    LiveData<List<NotificationEntity>> observeSearch(String q);

    @Query("SELECT * FROM notifications WHERE packageName = :pkg AND (title LIKE :q OR text LIKE :q OR appName LIKE :q) ORDER BY postedAt DESC")
    LiveData<List<NotificationEntity>> observeByPackageSearch(String pkg, String q);

    @Query("SELECT DISTINCT packageName FROM notifications ORDER BY packageName ASC")
    LiveData<List<String>> observePackages();

    @Query("SELECT COUNT(*) FROM notifications")
    LiveData<Integer> observeCount();

    @Query("DELETE FROM notifications WHERE postedAt < :timestamp")
    void deleteOlderThan(long timestamp);
}
