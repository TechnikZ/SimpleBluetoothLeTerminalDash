package de.kai_morich.simple_bluetooth_le_terminal.dashboard.config;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ConfigValidator {

    public static JSONObject parseAndValidate(String json) {
        try {
            JSONObject root = new JSONObject(json);
            JSONArray blocks = root.optJSONArray("blocks");
            if (blocks != null) {
                for (int i = 0; i < blocks.length(); i++) {
                    JSONObject block = blocks.getJSONObject(i);
                    String type = block.optString("type", "");
                    if (type.isEmpty()) throw new IllegalArgumentException("Block #" + i + " missing type");
                    if (block.optString("label", "").isEmpty()) throw new IllegalArgumentException("Block #" + i + " missing label");
                    if ("numeric".equals(type) || "lamp".equals(type)) {
                        if (block.optInt("row", 0) <= 0 || block.optInt("col", 0) <= 0) {
                            throw new IllegalArgumentException("Block #" + i + " invalid row/col");
                        }
                    }
                }
            }
            JSONArray buttons = root.optJSONArray("buttons");
            if (buttons != null) {
                for (int i = 0; i < buttons.length(); i++) {
                    JSONObject button = buttons.getJSONObject(i);
                    if (button.optString("type", "").isEmpty()) throw new IllegalArgumentException("Button #" + i + " missing type");
                    if (button.optString("label", "").isEmpty()) throw new IllegalArgumentException("Button #" + i + " missing label");
                }
            }
            return root;
        } catch (JSONException e) {
            throw new IllegalArgumentException("Invalid JSON format", e);
        }
    }
}
