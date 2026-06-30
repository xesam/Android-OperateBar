package io.github.xesam.android.operatebar.example;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import io.github.xesam.android.operatebar.OperateBarLayout;

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

        OperateBarLayout operateBar = findViewById(R.id.operate_bar);
        View decorView = operateBar.getDecorView();
        View actionPrimary = findViewById(R.id.action_primary);
        View actionSecondary = findViewById(R.id.action_secondary);

        MaterialButton btnToggleDivider = findViewById(R.id.btn_toggle_divider);
        MaterialButton btnTogglePrimary = findViewById(R.id.btn_toggle_primary);
        MaterialButton btnToggleSecondary = findViewById(R.id.btn_toggle_secondary);

        btnToggleDivider.setOnClickListener(v -> {
            if (decorView == null) return;
            boolean show = decorView.getVisibility() != View.VISIBLE;
            decorView.setVisibility(show ? View.VISIBLE : View.GONE);
            btnToggleDivider.setText(show ? "关闭分隔线" : "开启分隔线");
        });

        btnTogglePrimary.setOnClickListener(v -> {
            boolean show = actionPrimary.getVisibility() != View.VISIBLE;
            actionPrimary.setVisibility(show ? View.VISIBLE : View.GONE);
            btnTogglePrimary.setText(show ? "隐藏主操作" : "显示主操作");
        });

        btnToggleSecondary.setOnClickListener(v -> {
            boolean show = actionSecondary.getVisibility() != View.VISIBLE;
            actionSecondary.setVisibility(show ? View.VISIBLE : View.GONE);
            btnToggleSecondary.setText(show ? "隐藏复杂项" : "显示复杂项");
        });
        MaterialButton btnToggleWidth = findViewById(R.id.btn_toggle_width);
        int customWidthPx = (int) (160 * getResources().getDisplayMetrics().density);
        boolean[] isCustomWidth = {false};

        btnToggleWidth.setOnClickListener(v -> {
            isCustomWidth[0] = !isCustomWidth[0];
            ViewGroup.LayoutParams lp = actionPrimary.getLayoutParams();
            lp.width = isCustomWidth[0] ? customWidthPx : ViewGroup.LayoutParams.MATCH_PARENT;
            actionPrimary.setLayoutParams(lp);
            btnToggleWidth.setText(isCustomWidth[0] ? "主操作恢复等分" : "主操作自定义宽度");
        });
    }
}
