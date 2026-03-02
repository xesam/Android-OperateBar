package io.github.xesam.android.operatebar.example;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        View primaryWithDivider = findViewById(R.id.action_primary);
        View primaryWithoutDivider = findViewById(R.id.action_primary_no_divider);
        MaterialButton togglePrimaryButton = findViewById(R.id.toggle_primary_button);

        togglePrimaryButton.setOnClickListener(v -> {
            boolean showPrimary = primaryWithDivider.getVisibility() != View.VISIBLE;
            int visibility = showPrimary ? View.VISIBLE : View.GONE;
            primaryWithDivider.setVisibility(visibility);
            primaryWithoutDivider.setVisibility(visibility);
            togglePrimaryButton.setText(showPrimary ? "隐藏主操作" : "显示主操作");
        });
    }
}
