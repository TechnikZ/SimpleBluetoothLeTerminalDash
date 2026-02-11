package de.kai_morich.simple_bluetooth_le_terminal.dashboard.parser;

import java.util.ArrayList;
import java.util.List;

public class AnsiParser {

    public static class ParsedUpdate {
        public final int row;
        public final int col;
        public final String value;

        public ParsedUpdate(int row, int col, String value) {
            this.row = row;
            this.col = col;
            this.value = value;
        }
    }

    private final StringBuilder carry = new StringBuilder();

    public synchronized List<ParsedUpdate> parse(String chunk) {
        if (chunk == null || chunk.isEmpty()) {
            return new ArrayList<>();
        }
        carry.append(chunk);
        List<ParsedUpdate> updates = new ArrayList<>();
        int idx = 0;
        while (idx < carry.length()) {
            int esc = carry.indexOf("\u001B[", idx);
            if (esc < 0) {
                trimCarryTail();
                break;
            }
            if (esc > 0) {
                carry.delete(0, esc);
                idx = 0;
                esc = 0;
            }
            int semicolon = findNumberDelimiter(carry, esc + 2, ';');
            if (semicolon < 0) {
                break;
            }
            int hPos = findNumberDelimiter(carry, semicolon + 1, 'H');
            if (hPos < 0) {
                break;
            }
            Integer row = parsePositiveInt(carry.substring(esc + 2, semicolon));
            Integer col = parsePositiveInt(carry.substring(semicolon + 1, hPos));
            if (row == null || col == null) {
                carry.delete(0, hPos + 1);
                idx = 0;
                continue;
            }
            int nextEsc = carry.indexOf("\u001B[", hPos + 1);
            if (nextEsc < 0) {
                String value = carry.substring(hPos + 1);
                if (!value.isEmpty()) {
                    updates.add(new ParsedUpdate(row, col, value));
                }
                carry.delete(0, hPos + 1);
                break;
            } else {
                String value = carry.substring(hPos + 1, nextEsc);
                updates.add(new ParsedUpdate(row, col, value));
                carry.delete(0, nextEsc);
                idx = 0;
            }
        }
        if (carry.length() > 4096) {
            carry.delete(0, carry.length() - 128);
        }
        return updates;
    }

    private static int findNumberDelimiter(StringBuilder source, int start, char delimiter) {
        for (int i = start; i < source.length(); i++) {
            char c = source.charAt(i);
            if (c == delimiter) {
                return i;
            }
            if (!Character.isDigit(c)) {
                return -1;
            }
        }
        return -1;
    }

    private static Integer parsePositiveInt(String value) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private void trimCarryTail() {
        int esc = carry.lastIndexOf("\u001B[");
        if (esc >= 0) {
            if (esc > 0) {
                carry.delete(0, esc);
            }
        } else if (carry.length() > 32) {
            carry.delete(0, carry.length() - 32);
        }
    }
}
