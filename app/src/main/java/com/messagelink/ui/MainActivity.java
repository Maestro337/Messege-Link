package com.messagelink.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.messagelink.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String KEY_SELECTED_PACKAGE = "selected_package";
    private static final String KEY_SEARCH_QUERY = "search_query";
    private static final long SEARCH_DELAY_MS = 300L; // Задержка для поиска

    private MainViewModel vm;
    private NotificationAdapter adapter;
    private ArrayAdapter<String> spinnerAdapter;
    private final List<String> spinnerPkgs = new ArrayList<>();
    private String selectedPkg = "";
    private String search = "";
    private Handler searchHandler;
    private Runnable searchRunnable;

    // Идентификаторы меню
    private static final int MENU_SETTINGS = R.id.action_settings;
    private static final int MENU_NOTIFICATION_ACCESS = R.id.action_notification_access;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Восстановление состояния при повороте экрана
        if (savedInstanceState != null) {
            selectedPkg = savedInstanceState.getString(KEY_SELECTED_PACKAGE, "");
            search = savedInstanceState.getString(KEY_SEARCH_QUERY, "");
        }

        // Инициализация Handler для отложенного поиска
        searchHandler = new Handler(Looper.getMainLooper());

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(this::onMenu);

        // Устанавливаем обработчик навигации (для кнопки "назад" в Toolbar, если есть)
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        TextView status = findViewById(R.id.statusText);
        EditText searchInput = findViewById(R.id.searchInput);
        Spinner spinner = findViewById(R.id.appSpinner);
        RecyclerView rv = findViewById(R.id.recycler);

        adapter = new NotificationAdapter();
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        // Добавляем обработчик кликов по элементам списка
        adapter.setOnItemClickListener((notification, position) -> {
            if (notification != null && notification.text != null && !notification.text.trim().isEmpty()) {
                // Показываем полный текст уведомления в Toast
                String text = notification.text;
                if (text.length() > 100) {
                    text = text.substring(0, 97) + "...";
                }
                Toast.makeText(this, text, Toast.LENGTH_LONG).show();
            }
        });

        spinnerPkgs.add(getString(R.string.all_apps));
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spinnerPkgs);
        spinner.setAdapter(spinnerAdapter);

        // Восстанавливаем позицию спиннера
        if (!selectedPkg.isEmpty()) {
            int position = spinnerPkgs.indexOf(selectedPkg);
            if (position != -1) {
                spinner.setSelection(position);
            }
        }

        // Восстанавливаем текст поиска
        if (!search.isEmpty()) {
            searchInput.setText(search);
        }

        vm = new ViewModelProvider(this).get(MainViewModel.class);

        // Подписываемся на данные
        vm.items().observe(this, items -> {
            if (items != null && !items.isEmpty()) {
                adapter.submit(items);
                rv.scrollToPosition(0); // Прокручиваем к началу при новых данных
            } else {
                adapter.submit(new ArrayList<>());
            }
        });

        vm.count().observe(this, count -> {
            int n = count == null ? 0 : count;
            // Используем простой формат, так как plurals ресурс не существует
            String statusText = "Saved: " + n;
            status.setText(statusText);
        });

        vm.packages().observe(this, packages -> {
            if (packages != null) {
                spinnerPkgs.clear();
                spinnerPkgs.add(getString(R.string.all_apps));
                spinnerPkgs.addAll(packages);
                spinnerAdapter.notifyDataSetChanged();

                // Восстанавливаем выбор пакета после обновления списка
                if (!selectedPkg.isEmpty()) {
                    int position = spinnerPkgs.indexOf(selectedPkg);
                    if (position != -1) {
                        spinner.setSelection(position);
                    }
                }
            }
        });

        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position >= 0 && position < spinnerPkgs.size()) {
                    String selected = spinnerPkgs.get(position);
                    selectedPkg = selected.equals(getString(R.string.all_apps)) ? "" : selected;
                    vm.setFilter(selectedPkg, search);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                selectedPkg = "";
                vm.setFilter("", search);
            }
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Отменяем предыдущий поиск
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                // Создаем новый поиск с задержкой
                String newSearch = s == null ? "" : s.toString().trim();
                searchRunnable = () -> {
                    search = newSearch;
                    vm.setFilter(selectedPkg, search);
                };

                // Запускаем поиск с задержкой
                searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_SELECTED_PACKAGE, selectedPkg);
        outState.putString(KEY_SEARCH_QUERY, search);
    }

    @Override
    protected void onDestroy() {
        // Очищаем Handler для предотвращения утечек памяти
        if (searchHandler != null) {
            searchHandler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }

    private boolean onMenu(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == MENU_SETTINGS) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (id == MENU_NOTIFICATION_ACCESS) {
            try {
                startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
            } catch (Exception e) {
                // Используем хардкодную строку вместо ресурса
                Toast.makeText(this, "Cannot open notification settings", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return false;
    }

    // Вспомогательный метод для очистки поиска
    public void clearSearch() {
        EditText searchInput = findViewById(R.id.searchInput);
        if (searchInput != null) {
            searchInput.setText("");
        }
    }
}