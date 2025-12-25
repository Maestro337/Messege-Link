package com.messagelink.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.messagelink.R;
import com.messagelink.data.db.NotificationEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {
    private List<NotificationEntity> items = new ArrayList<>();
    private final SimpleDateFormat fmt = new SimpleDateFormat("dd.MM HH:mm", Locale.getDefault());
    private OnItemClickListener onItemClickListener;

    // Интерфейс для обработки кликов
    public interface OnItemClickListener {
        void onItemClick(NotificationEntity notification, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void submit(List<NotificationEntity> list) {
        if (list == null) {
            list = new ArrayList<>();
        }

        // Используем DiffUtil для плавных анимаций обновления
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new NotificationDiffCallback(items, list));
        items.clear();
        items.addAll(list);
        diffResult.dispatchUpdatesTo(this);
    }

    public void clear() {
        submit(new ArrayList<>());
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        NotificationEntity e = items.get(position);

        // Устанавливаем данные
        h.appName.setText(safe(e.appName, e.packageName));
        h.title.setText(safe(e.title, ""));
        h.text.setText(safe(e.text, ""));

        // Безопасное форматирование времени
        if (e.postedAt > 0) {
            h.time.setText(fmt.format(new Date(e.postedAt)));
        } else {
            h.time.setText("-");
        }

        // Обработка клика на элементе
        h.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(e, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onViewRecycled(@NonNull VH holder) {
        super.onViewRecycled(holder);
        // Очищаем слушатели при переиспользовании ViewHolder для избежания утечек
        holder.itemView.setOnClickListener(null);
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView appName;
        TextView time;
        TextView title;
        TextView text;

        VH(@NonNull View itemView) {
            super(itemView);
            appName = itemView.findViewById(R.id.appName);
            time = itemView.findViewById(R.id.time);
            title = itemView.findViewById(R.id.title);
            text = itemView.findViewById(R.id.text);
        }
    }

    private String safe(String a, String b) {
        if (a != null && !a.trim().isEmpty()) return a.trim();
        if (b != null) return b.trim();
        return "";
    }

    // Получение элемента по позиции (для внешнего использования)
    public NotificationEntity getItem(int position) {
        if (position >= 0 && position < items.size()) {
            return items.get(position);
        }
        return null;
    }

    // DiffUtil Callback для оптимизированного обновления списка
    private static class NotificationDiffCallback extends DiffUtil.Callback {
        private final List<NotificationEntity> oldList;
        private final List<NotificationEntity> newList;

        public NotificationDiffCallback(List<NotificationEntity> oldList, List<NotificationEntity> newList) {
            this.oldList = oldList != null ? oldList : new ArrayList<>();
            this.newList = newList != null ? newList : new ArrayList<>();
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            NotificationEntity oldItem = oldList.get(oldItemPosition);
            NotificationEntity newItem = newList.get(newItemPosition);
            // Сравниваем по времени создания и пакету, так как ID может не быть
            return oldItem.postedAt == newItem.postedAt &&
                    safeEquals(oldItem.packageName, newItem.packageName);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            NotificationEntity oldItem = oldList.get(oldItemPosition);
            NotificationEntity newItem = newList.get(newItemPosition);

            return safeEquals(oldItem.appName, newItem.appName) &&
                    safeEquals(oldItem.title, newItem.title) &&
                    safeEquals(oldItem.text, newItem.text) &&
                    oldItem.postedAt == newItem.postedAt;
        }

        private boolean safeEquals(String a, String b) {
            if (a == null && b == null) return true;
            if (a == null || b == null) return false;
            return a.equals(b);
        }
    }
}