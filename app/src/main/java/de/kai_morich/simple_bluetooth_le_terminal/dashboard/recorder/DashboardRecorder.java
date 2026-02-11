package de.kai_morich.simple_bluetooth_le_terminal.dashboard.recorder;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class DashboardRecorder {
    private final File directory;
    private FileOutputStream stream;
    private String activeFile;

    public DashboardRecorder(Context context) {
        directory = new File(context.getFilesDir(), "recordings");
        if (!directory.exists()) {
            //noinspection ResultOfMethodCallIgnored
            directory.mkdirs();
        }
    }

    public synchronized String start(String fileName) {
        stop();
        try {
            activeFile = fileName.endsWith(".csv") ? fileName : fileName + ".csv";
            stream = new FileOutputStream(new File(directory, activeFile), true);
            stream.write("timestamp,blockId,value\n".getBytes(StandardCharsets.UTF_8));
            return activeFile;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void stop() {
        if (stream != null) {
            try {
                stream.close();
            } catch (Exception ignored) {
            }
            stream = null;
        }
    }

    public synchronized void record(long timestamp, String blockId, String value) {
        if (stream == null) return;
        try {
            String safe = value.replace(',', ';').replace('\n', ' ');
            String line = timestamp + "," + blockId + "," + safe + "\n";
            stream.write(line.getBytes(StandardCharsets.UTF_8));
            stream.flush();
        } catch (Exception ignored) {
        }
    }

    public synchronized String getActiveFile() {
        return activeFile;
    }

    public synchronized boolean isRecording() {
        return stream != null;
    }
}
