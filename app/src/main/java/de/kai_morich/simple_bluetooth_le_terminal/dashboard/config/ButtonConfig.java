package de.kai_morich.simple_bluetooth_le_terminal.dashboard.config;

import org.json.JSONArray;
import org.json.JSONObject;

public class ButtonConfig {
    public String type;
    public String label;
    public String sendOn;
    public String sendOff;
    public JSONArray commands;

    public static ButtonConfig fromJson(JSONObject object) {
        ButtonConfig config = new ButtonConfig();
        config.type = object.optString("type", "");
        config.label = object.optString("label", "");
        config.sendOn = object.optString("sendOn", "");
        config.sendOff = object.optString("sendOff", "");
        config.commands = object.optJSONArray("commands");
        return config;
    }
}
