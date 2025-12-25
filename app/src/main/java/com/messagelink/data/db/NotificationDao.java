package com.messagelink.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * Data Access Object for {@link NotificationEntity}.
 * Provides methods for inserting, observing and querying notifications.
 */
@Dao
public interface NotificationDao {

    /**
     * Inserts a notification into the database.
     * Replaces the existing entry if a conflict occurs.
     *
     * @param entity notification entity to insert
     * @return row id of the inserted item
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(NotificationEntity entity);

    /**
     * Removes all notifications from the database.
     */
    @Query("DELETE FROM notifications")
    void clear();

    /**
     * Observes all notifications ordered by posting time (descending).
     */
    @Query("SELECT * FROM notifications ORDER BY postedAt DESC")
    LiveData<List<NotificationEntity>> observeAll();

    /**
     * Observes notifications for a specific application package.
     *
     * @param pkg application package name
     */
    @Query("SELECT * FROM notifications WHERE packageName = :pkg ORDER BY postedAt DESC")
    LiveData<List<NotificationEntity>> observeByPackage(String pkg);

    /**
     * Observes notifications matching a search query.
     *
     * @param q search pattern (LIKE)
     */
    @Query(
            "SELECT * FROM notifications " +
                    "WHERE (title LIKE :q ESCAPE '\\' " +
                    "OR text LIKE :q ESCAPE '\\' " +
                    "OR appName LIKE :q ESCAPE '\\') " +
                    "ORDER BY postedAt DESC"
    )
    LiveData<List<NotificationEntity>> observeSearch(String q);

    /**
     * Observes notifications for a package filtered by a search query.
     *
     * @param pkg application package name
     * @param q   search pattern (LIKE)
     */
    @Query(
            "SELECT * FROM notifications " +
                    "WHERE packageName = :pkg AND " +
                    "(title LIKE :q ESCAPE '\\' " +
                    "OR text LIKE :q ESCAPE '\\' " +
                    "OR appName LIKE :q ESCAPE '\\') " +
                    "ORDER BY postedAt DESC"
    )
    LiveData<List<NotificationEntity>> observeByPackageSearch(String pkg, String q);

    /**
     * Observes distinct application package names.
     */
    @Query("SELECT DISTINCT packageName FROM notifications ORDER BY packageName ASC")
    LiveData<List<String>> observePackages();

    /**
     * Observes total number of notifications.
     */
    @Query("SELECT COUNT(*) FROM notifications")
    LiveData<Integer> observeCount();
}
