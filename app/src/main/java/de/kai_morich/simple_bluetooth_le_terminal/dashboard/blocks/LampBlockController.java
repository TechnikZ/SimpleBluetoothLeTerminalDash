package de.kai_morich.simple_bluetooth_le_terminal.dashboard.blocks;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import de.kai_morich.simple_bluetooth_le_terminal.dashboard.DashboardUiModel;
import de.kai_morich.simple_bluetooth_le_terminal.dashboard.config.BlockConfig;
import de.kai_morich.simple_bluetooth_le_terminal.dashboard.parser.VirtualScreenBuffer;

public class LampBlockController extends BaseBlockController {

    public LampBlockController(BlockConfig config) {
        super(config);
    }

    @Override
    public View createView(Context context) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(24, 16, 24, 16);

        View indicator = new View(context);
        LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(36, 36);
        dotParams.rightMargin = 16;
        indicator.setLayoutParams(dotParams);
        row.addView(indicator);

        TextView text = new TextView(context);
        row.addView(text);
        row.setTag(new int[]{indicator.getId(), text.getId()});
        if (indicator.getId() == View.NO_ID) indicator.setId(View.generateViewId());
        if (text.getId() == View.NO_ID) text.setId(View.generateViewId());
        row.setTag(new int[]{indicator.getId(), text.getId()});
        rootView = row;
        return row;
    }

    @Override
    public void updateView(DashboardUiModel model) {
        int[] ids = (int[]) rootView.getTag();
        View indicator = rootView.findViewById(ids[0]);
        TextView text = rootView.findViewById(ids[1]);
        text.setText(model.label + ": " + model.displayValue);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(model.color);
        indicator.setBackground(drawable);
        rootView.setVisibility(model.visible ? View.VISIBLE : View.GONE);
    }

    @Override
    protected DashboardUiModel computeUiModel(VirtualScreenBuffer screen, boolean visible) {
        String raw = screen.get(config.row, config.col, config.length);
        int color = Color.GRAY;
        JSONArray conditions = config.conditions;
        if (conditions != null) {
            for (int i = 0; i < conditions.length(); i++) {
                JSONObject item = conditions.optJSONObject(i);
                if (item == null) continue;
                if (item.has("equals") && raw.trim().equals(item.optString("equals"))) {
                    color = BlockConfig.color(item.optString("color", "grey"));
                    break;
                }
                if (item.has("if") && NumericBlockController.matchesExpr(item.optString("if"), raw)) {
                    color = BlockConfig.color(item.optString("color", "grey"));
                    break;
                }
            }
        }
        return new DashboardUiModel(config.id, config.label, raw, color, visible);
    }
}
