package de.kai_morich.simple_bluetooth_le_terminal.dashboard.config;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DashboardConfig {
    public static class LoadedConfig {
        public final DashboardConfig config;
        public final String json;

        public LoadedConfig(DashboardConfig config, String json) {
            this.config = config;
            this.json = json;
        }
    }

    public String updateMode = "onChange";
    public final List<BlockConfig> blocks = new ArrayList<>();
    public final List<ButtonConfig> buttons = new ArrayList<>();

    public static DashboardConfig fromJson(JSONObject root) {
        DashboardConfig config = new DashboardConfig();
        JSONObject settings = root.optJSONObject("settings");
        if (settings != null) {
            config.updateMode = settings.optString("updateMode", "onChange");
        }
        JSONArray blocksArray = root.optJSONArray("blocks");
        if (blocksArray != null) {
            for (int i = 0; i < blocksArray.length(); i++) {
                JSONObject item = blocksArray.optJSONObject(i);
                if (item != null) {
                    config.blocks.add(BlockConfig.fromJson(item));
                }
            }
        }
        JSONArray buttonsArray = root.optJSONArray("buttons");
        if (buttonsArray != null) {
            for (int i = 0; i < buttonsArray.length(); i++) {
                JSONObject item = buttonsArray.optJSONObject(i);
                if (item != null) {
                    config.buttons.add(ButtonConfig.fromJson(item));
                }
            }
        }
        return config;
    }
}
