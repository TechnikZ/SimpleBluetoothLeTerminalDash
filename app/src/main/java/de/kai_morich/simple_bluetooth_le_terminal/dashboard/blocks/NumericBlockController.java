package de.kai_morich.simple_bluetooth_le_terminal.dashboard.blocks;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;

import de.kai_morich.simple_bluetooth_le_terminal.dashboard.DashboardUiModel;
import de.kai_morich.simple_bluetooth_le_terminal.dashboard.config.BlockConfig;
import de.kai_morich.simple_bluetooth_le_terminal.dashboard.parser.VirtualScreenBuffer;

public class NumericBlockController extends BaseBlockController {

    public NumericBlockController(BlockConfig config) {
        super(config);
    }

    @Override
    public View createView(Context context) {
        rootView = createBaseLayout(context, false);
        return rootView;
    }

    @Override
    public void updateView(DashboardUiModel model) {
        int[] ids = (int[]) rootView.getTag();
        TextView labelView = rootView.findViewById(ids[0]);
        TextView valueView = rootView.findViewById(ids[1]);
        labelView.setText(model.label);
        valueView.setText(model.displayValue);
        valueView.setTextColor(model.color);
        rootView.setVisibility(model.visible ? View.VISIBLE : View.GONE);
    }

    @Override
    protected DashboardUiModel computeUiModel(VirtualScreenBuffer screen, boolean visible) {
        String raw = screen.get(config.row, config.col, config.length);
        String display = applyTransform(raw);
        int color = applyStyle(display, raw);
        return new DashboardUiModel(config.id, config.label, display, color, visible);
    }

    private String applyTransform(String raw) {
        if (config.transform == null) return raw;
        try {
            String type = config.transform.optString("type", "");
            switch (type) {
                case "trim":
                    return raw.trim();
                case "substring":
                    int start = config.transform.optInt("start", 0);
                    int end = config.transform.has("end") ? config.transform.optInt("end") : Math.min(raw.length(), start + config.transform.optInt("length", raw.length()));
                    if (start < 0 || start >= raw.length()) return raw;
                    return raw.substring(start, Math.min(end, raw.length()));
                case "scale": {
                    double factor = config.transform.optDouble("factor", 1d);
                    String unit = config.transform.optString("unit", "");
                    double value = Double.parseDouble(raw.trim()) * factor;
                    return new DecimalFormat("0.###").format(value) + (unit.isEmpty() ? "" : " " + unit);
                }
                case "offset": {
                    double off = config.transform.optDouble("offset", 0d);
                    double value = Double.parseDouble(raw.trim()) + off;
                    return new DecimalFormat("0.###").format(value);
                }
                case "map": {
                    JSONObject values = config.transform.optJSONObject("values");
                    if (values != null && values.has(raw.trim())) {
                        return values.optString(raw.trim(), raw);
                    }
                    return raw;
                }
                default:
                    return raw;
            }
        } catch (Exception e) {
            return raw;
        }
    }

    private int applyStyle(String display, String raw) {
        JSONArray rules = config.styleRules;
        if (rules == null) {
            return Color.DKGRAY;
        }
        for (int i = 0; i < rules.length(); i++) {
            JSONObject rule = rules.optJSONObject(i);
            if (rule == null) continue;
            String colorName = rule.optString("color", "grey");
            if (rule.has("equals") && rule.optString("equals").equals(display)) {
                return BlockConfig.color(colorName);
            }
            if (rule.has("if") && matchesExpr(rule.optString("if"), raw)) {
                return BlockConfig.color(colorName);
            }
        }
        return Color.DKGRAY;
    }

    static boolean matchesExpr(String expr, String raw) {
        try {
            String e = expr.replace(" ", "");
            double val = Double.parseDouble(raw.trim());
            if (e.startsWith(">=")) return val >= Double.parseDouble(e.substring(2));
            if (e.startsWith("<=")) return val <= Double.parseDouble(e.substring(2));
            if (e.startsWith("==")) return val == Double.parseDouble(e.substring(2));
            if (e.startsWith(">")) return val > Double.parseDouble(e.substring(1));
            if (e.startsWith("<")) return val < Double.parseDouble(e.substring(1));
        } catch (Exception ignored) {
        }
        return false;
    }
}
