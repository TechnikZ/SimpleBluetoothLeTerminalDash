package de.kai_morich.simple_bluetooth_le_terminal.dashboard.blocks;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.kai_morich.simple_bluetooth_le_terminal.dashboard.DashboardUiModel;
import de.kai_morich.simple_bluetooth_le_terminal.dashboard.config.BlockConfig;
import de.kai_morich.simple_bluetooth_le_terminal.dashboard.parser.VirtualScreenBuffer;

public abstract class BaseBlockController {
    protected final BlockConfig config;
    protected View rootView;

    protected BaseBlockController(BlockConfig config) {
        this.config = config;
    }

    public String getId() {
        return config.id;
    }

    public BlockConfig getConfig() {
        return config;
    }

    public abstract View createView(Context context);

    public abstract void updateView(DashboardUiModel model);

    public DashboardUiModel evaluate(VirtualScreenBuffer screen) {
        boolean visible = computeVisibility(screen);
        return computeUiModel(screen, visible);
    }

    protected abstract DashboardUiModel computeUiModel(VirtualScreenBuffer screen, boolean visible);

    protected boolean computeVisibility(VirtualScreenBuffer screen) {
        if (config.visibleIf == null) return true;
        int row = config.visibleIf.optInt("row", -1);
        int col = config.visibleIf.optInt("col", -1);
        String equals = config.visibleIf.optString("equals", "");
        return equals.equals(screen.get(row, col, 0));
    }

    protected View createBaseLayout(Context context, boolean lamp) {
        LinearLayout wrapper = new LinearLayout(context);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setPadding(24, 16, 24, 16);
        TextView label = new TextView(context);
        label.setId(View.generateViewId());
        label.setTextColor(Color.DKGRAY);
        label.setTextSize(14f);
        wrapper.addView(label);
        if (!lamp) {
            TextView value = new TextView(context);
            value.setId(View.generateViewId());
            value.setTextSize(24f);
            wrapper.addView(value);
        }
        wrapper.setTag(new int[]{label.getId(), lamp ? -1 : wrapper.getChildAt(1).getId()});
        return wrapper;
    }
}
