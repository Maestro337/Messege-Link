package com.messagelink.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.messagelink.R;
import com.messagelink.data.db.NotificationEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {
    private final List<NotificationEntity> items = new ArrayList<>();
    private final SimpleDateFormat fmt = new SimpleDateFormat("dd.MM HH:mm", Locale.getDefault());

    public void submit(List<NotificationEntity> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
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
        h.appName.setText(safe(e.appName, e.packageName));
        h.title.setText(safe(e.title, ""));
        h.text.setText(safe(e.text, ""));
        h.time.setText(fmt.format(new Date(e.postedAt)));
    }

    @Override
    public int getItemCount() {
        return items.size();
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
}
