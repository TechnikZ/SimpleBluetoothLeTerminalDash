package de.kai_morich.simple_bluetooth_le_terminal.dashboard.blocks;

import android.widget.Button;

import de.kai_morich.simple_bluetooth_le_terminal.SerialService;
import de.kai_morich.simple_bluetooth_le_terminal.dashboard.config.ButtonConfig;

public class ToggleButtonController {
    private final ButtonConfig config;
    private boolean on;

    public ToggleButtonController(ButtonConfig config) {
        this.config = config;
    }

    public Button createButton(android.content.Context context, SerialService service) {
        Button button = new Button(context);
        updateText(button);
        button.setOnClickListener(v -> {
            if (service == null) return;
            try {
                String value = on ? config.sendOff : config.sendOn;
                service.write(value.getBytes());
                on = !on;
                updateText(button);
            } catch (Exception ignored) {
            }
        });
        return button;
    }

    private void updateText(Button button) {
        button.setText(config.label + (on ? " ON" : " OFF"));
    }
}
