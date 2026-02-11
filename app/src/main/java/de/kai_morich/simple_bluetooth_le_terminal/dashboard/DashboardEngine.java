package de.kai_morich.simple_bluetooth_le_terminal.dashboard;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.kai_morich.simple_bluetooth_le_terminal.dashboard.blocks.BaseBlockController;
import de.kai_morich.simple_bluetooth_le_terminal.dashboard.config.BlockConfig;
import de.kai_morich.simple_bluetooth_le_terminal.dashboard.config.DashboardConfig;
import de.kai_morich.simple_bluetooth_le_terminal.dashboard.parser.AnsiParser;
import de.kai_morich.simple_bluetooth_le_terminal.dashboard.parser.VirtualScreenBuffer;

public class DashboardEngine {
    public interface Listener {
        void onBlocksUpdated(List<DashboardUiModel> updates, boolean alwaysMode);
    }

    private final AnsiParser parser = new AnsiParser();
    private final VirtualScreenBuffer screenBuffer = new VirtualScreenBuffer();
    private final DashboardFactory factory = new DashboardFactory();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Map<String, DashboardUiModel> lastById = new HashMap<>();
    private final List<BaseBlockController> blocks = new ArrayList<>();

    private String updateMode = "onChange";
    private Listener listener;

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setConfig(DashboardConfig config) {
        blocks.clear();
        blocks.addAll(factory.createBlocks(config.blocks));
        lastById.clear();
        updateMode = config.updateMode;
    }

    public List<BaseBlockController> getBlocks() {
        return new ArrayList<>(blocks);
    }

    public void onSerialText(String raw) {
        executor.submit(() -> process(raw));
    }

    private void process(String raw) {
        List<AnsiParser.ParsedUpdate> updates = parser.parse(raw);
        if (updates.isEmpty() && !"always".equals(updateMode)) {
            return;
        }
        Set<Integer> changedKeys = new HashSet<>();
        for (AnsiParser.ParsedUpdate update : updates) {
            if (screenBuffer.update(update.row, update.col, update.value)) {
                changedKeys.add(BlockConfig.key(update.row, update.col));
            }
        }

        List<DashboardUiModel> changedModels = new ArrayList<>();
        boolean always = "always".equals(updateMode);
        for (BaseBlockController block : blocks) {
            boolean relevant = always;
            if (!relevant) {
                for (Integer key : block.getConfig().dependentKeys()) {
                    if (changedKeys.contains(key)) {
                        relevant = true;
                        break;
                    }
                }
            }
            if (!relevant) continue;
            DashboardUiModel model = block.evaluate(screenBuffer);
            DashboardUiModel previous = lastById.get(model.id);
            if (always || previous == null || !equals(previous, model)) {
                lastById.put(model.id, model);
                changedModels.add(model);
            }
        }

        if (!changedModels.isEmpty() && listener != null) {
            mainHandler.post(() -> {
                if (listener != null) {
                    listener.onBlocksUpdated(changedModels, always);
                }
            });
        }
    }

    private boolean equals(DashboardUiModel a, DashboardUiModel b) {
        return a.visible == b.visible && a.color == b.color && a.displayValue.equals(b.displayValue);
    }
}
