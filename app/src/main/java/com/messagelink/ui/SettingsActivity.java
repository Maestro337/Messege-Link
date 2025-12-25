package com.messagelink.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.messagelink.R;
import com.messagelink.data.SettingsStore;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationOnClickListener(v -> finish());

        SwitchMaterial piSwitch = findViewById(R.id.piSwitch);
        EditText piUrl = findViewById(R.id.piUrl);
        View save = findViewById(R.id.saveBtn);

        EditText retentionInput = findViewById(R.id.retentionDays);
        retentionInput.setText(String.valueOf(SettingsStore.getRetentionDays(this)));

        piSwitch.setChecked(SettingsStore.isPiEnabled(this));
        piUrl.setText(SettingsStore.getPiUrl(this));

        save.setOnClickListener(v -> {
            SettingsStore.setPiEnabled(this, piSwitch.isChecked());
            SettingsStore.setPiUrl(this, piUrl.getText() == null ? "" : piUrl.getText().toString());
            finish();
            try {
                int days = Integer.parseInt(retentionInput.getText().toString());
                SettingsStore.setRetentionDays(this, days);
            } catch (Exception ignored) {}
        });
    }
}
