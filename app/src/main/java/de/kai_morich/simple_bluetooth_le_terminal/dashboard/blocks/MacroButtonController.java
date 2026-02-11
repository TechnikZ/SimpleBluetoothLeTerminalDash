package de.kai_morich.simple_bluetooth_le_terminal.dashboard.blocks;

import android.os.Handler;
import android.os.Looper;
import android.widget.Button;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.kai_morich.simple_bluetooth_le_terminal.SerialService;
import de.kai_morich.simple_bluetooth_le_terminal.dashboard.config.ButtonConfig;

public class MacroButtonController {
    private final ButtonConfig config;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public MacroButtonController(ButtonConfig config) {
        this.config = config;
    }

    public Button createButton(android.content.Context context, SerialService service) {
        Button button = new Button(context);
        button.setText(config.label);
        button.setOnClickListener(v -> executor.submit(() -> runMacro(service)));
        return button;
    }

    private void runMacro(SerialService service) {
        if (service == null || config.commands == null) {
            return;
        }
        Handler handler = new Handler(Looper.getMainLooper());
        for (int i = 0; i < config.commands.length(); i++) {
            JSONObject cmd = config.commands.optJSONObject(i);
            if (cmd == null) continue;
            int delayMs = cmd.optInt("delayMs", 0);
            String value = cmd.optString("value", "");
            handler.postDelayed(() -> {
                try {
                    service.write(value.getBytes());
                } catch (Exception ignored) {
                }
            }, Math.max(delayMs, 0));
        }
    }
}
