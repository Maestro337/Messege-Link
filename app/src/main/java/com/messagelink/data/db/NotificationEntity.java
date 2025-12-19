package com.messagelink.data.db;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "notifications",
        indices = {@Index(value = {"postedAt"}), @Index(value = {"packageName"}), @Index(value = {"title"}), @Index(value = {"text"})})
public class NotificationEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String packageName;
    public String appName;
    public String title;
    public String text;
    public long postedAt;

    public NotificationEntity(String packageName, String appName, String title, String text, long postedAt) {
        this.packageName = packageName;
        this.appName = appName;
        this.title = title;
        this.text = text;
        this.postedAt = postedAt;
    }
}
