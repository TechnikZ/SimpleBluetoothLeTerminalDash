package de.kai_morich.simple_bluetooth_le_terminal.dashboard;

public class DashboardUiModel {
    public final String id;
    public final String label;
    public final String displayValue;
    public final int color;
    public final boolean visible;

    public DashboardUiModel(String id, String label, String displayValue, int color, boolean visible) {
        this.id = id;
        this.label = label;
        this.displayValue = displayValue;
        this.color = color;
        this.visible = visible;
    }
}
