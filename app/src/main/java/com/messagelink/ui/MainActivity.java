package com.messagelink.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

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
    private MainViewModel vm;
    private NotificationAdapter adapter;
    private ArrayAdapter<String> spinnerAdapter;
    private final List<String> spinnerPkgs = new ArrayList<>();
    private String selectedPkg = "";
    private String search = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(this::onMenu);

        TextView status = findViewById(R.id.statusText);
        EditText searchInput = findViewById(R.id.searchInput);
        Spinner spinner = findViewById(R.id.appSpinner);
        RecyclerView rv = findViewById(R.id.recycler);

        adapter = new NotificationAdapter();
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        spinnerPkgs.add(getString(R.string.all_apps));
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spinnerPkgs);
        spinner.setAdapter(spinnerAdapter);

        vm = new ViewModelProvider(this).get(MainViewModel.class);
        vm.items().observe(this, adapter::submit);
        vm.count().observe(this, c -> {
            int n = c == null ? 0 : c;
            status.setText("Saved: " + n);
        });
        vm.packages().observe(this, pkgs -> {
            spinnerPkgs.clear();
            spinnerPkgs.add(getString(R.string.all_apps));
            if (pkgs != null) spinnerPkgs.addAll(pkgs);
            spinnerAdapter.notifyDataSetChanged();
        });

        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                String v = spinnerPkgs.get(position);
                selectedPkg = v.equals(getString(R.string.all_apps)) ? "" : v;
                vm.setFilter(selectedPkg, search);
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                search = s == null ? "" : s.toString();
                vm.setFilter(selectedPkg, search);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private boolean onMenu(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_clear) {
            showClearDialog();
            return true;
        }
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (id == R.id.action_notification_access) {
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
            return true;
        }
        return false;
    }
    private void showClearDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Очистить уведомления?")
                .setMessage("Все уведомления будут удалены безвозвратно.")
                .setPositiveButton("Да", (dialog, which) -> {
                    vm.clearNotifications();
                    Toast.makeText(this, "Уведомления очищены", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Нет", null)
                .show();
    }
}
