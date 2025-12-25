package com.messagelink.data.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "notifications",
        indices = {@Index(value = {"packageName"}), @Index(value = {"postedAt"}), @Index(value = {"title"}), @Index(value = {"text"})})  // Изменение: Переставлен порядок индексов
public class NotificationEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull  // Изменение: Добавлена аннотация для non-null
    public String packageName;
    public String appName;
    @NonNull  // Изменение: Добавлена аннотация для non-null
    public String title;
    public String text;
    @NonNull  // Изменение: Добавлена аннотация для non-null (long is primitive, but for consistency)
    public long postedAt;

    // Изменение: Добавлен комментарий
    /**
     * Конструктор для создания сущности уведомления.
     * @param packageName Имя пакета приложения
     * @param appName Название приложения
     * @param title Заголовок уведомления
     * @param text Текст уведомления
     * @param postedAt Время публикации (timestamp)
     */
    public NotificationEntity(@NonNull String packageName, String appName, @NonNull String title, String text, long postedAt) {
        this.packageName = packageName;
        this.appName = appName;
        this.title = title;
        this.text = text;
        this.postedAt = postedAt;
    }

    // Новый метод: Для отладки (toString)
    @Override
    public String toString() {
        return "NotificationEntity{" +
                "id=" + id +
                ", packageName='" + packageName + '\'' +
                ", appName='" + appName + '\'' +
                ", title='" + title + '\'' +
                ", text='" + text + '\'' +
                ", postedAt=" + postedAt +
                '}';
    }
}