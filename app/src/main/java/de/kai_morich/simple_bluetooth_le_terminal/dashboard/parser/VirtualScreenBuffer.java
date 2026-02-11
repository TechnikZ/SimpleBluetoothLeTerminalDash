package de.kai_morich.simple_bluetooth_le_terminal.dashboard.parser;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VirtualScreenBuffer {
    private final Map<Position, String> values = new ConcurrentHashMap<>();

    public boolean update(int row, int col, String value) {
        Position position = new Position(row, col);
        String normalized = value == null ? "" : value;
        String previous = values.put(position, normalized);
        return previous == null || !previous.equals(normalized);
    }

    public String get(int row, int col, int length) {
        String value = values.get(new Position(row, col));
        if (value == null) {
            value = "";
        }
        if (length <= 0) {
            return value.trim();
        }
        if (value.length() >= length) {
            return value.substring(0, length).trim();
        }
        StringBuilder padded = new StringBuilder(value);
        while (padded.length() < length) {
            padded.append(' ');
        }
        return padded.toString().trim();
    }
}
