package de.kai_morich.simple_bluetooth_le_terminal.dashboard;

import java.util.ArrayList;
import java.util.List;

import de.kai_morich.simple_bluetooth_le_terminal.dashboard.blocks.BaseBlockController;
import de.kai_morich.simple_bluetooth_le_terminal.dashboard.blocks.LampBlockController;
import de.kai_morich.simple_bluetooth_le_terminal.dashboard.blocks.NumericBlockController;
import de.kai_morich.simple_bluetooth_le_terminal.dashboard.config.BlockConfig;

public class DashboardFactory {

    public List<BaseBlockController> createBlocks(List<BlockConfig> configs) {
        List<BaseBlockController> blocks = new ArrayList<>();
        for (BlockConfig config : configs) {
            if ("numeric".equals(config.type)) {
                blocks.add(new NumericBlockController(config));
            } else if ("lamp".equals(config.type)) {
                blocks.add(new LampBlockController(config));
            }
        }
        return blocks;
    }
}
