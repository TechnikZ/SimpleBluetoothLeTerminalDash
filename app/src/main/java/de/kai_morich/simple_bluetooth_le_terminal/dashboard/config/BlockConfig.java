package de.kai_morich.simple_bluetooth_le_terminal.dashboard.config;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockConfig {
    public String type;
    public String id;
    public String label;
    public int row;
    public int col;
    public int length;
    public JSONObject transform;
    public JSONArray styleRules;
    public JSONObject visibleIf;
    public JSONArray conditions;

    public static BlockConfig fromJson(JSONObject object) {
        BlockConfig config = new BlockConfig();
        config.type = object.optString("type", "");
        config.id = object.optString("id", "");
        config.label = object.optString("label", "");
        config.row = object.optInt("row", 0);
        config.col = object.optInt("col", 0);
        config.length = object.optInt("length", 0);
        config.transform = object.optJSONObject("transform");
        JSONObject style = object.optJSONObject("style");
        if (style != null) {
            config.styleRules = style.optJSONArray("rules");
        }
        config.visibleIf = object.optJSONObject("visibleIf");
        config.conditions = object.optJSONArray("conditions");
        return config;
    }

    public List<Integer> dependentKeys() {
        List<Integer> keys = new ArrayList<>();
        keys.add(key(row, col));
        if (visibleIf != null) {
            keys.add(key(visibleIf.optInt("row"), visibleIf.optInt("col")));
        }
        return keys;
    }

    public static int key(int row, int col) {
        return (row << 16) | (col & 0xffff);
    }

    public static int color(String name) {
        Map<String, Integer> colors = new HashMap<>();
        colors.put("red", 0xFFD32F2F);
        colors.put("yellow", 0xFFF9A825);
        colors.put("green", 0xFF388E3C);
        colors.put("blue", 0xFF1976D2);
        colors.put("grey", 0xFF757575);
        return colors.containsKey(name) ? colors.get(name) : colors.get("grey");
    }
}
