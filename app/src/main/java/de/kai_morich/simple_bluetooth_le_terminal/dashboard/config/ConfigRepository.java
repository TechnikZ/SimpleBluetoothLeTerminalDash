package de.kai_morich.simple_bluetooth_le_terminal.dashboard.config;

import android.content.Context;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConfigRepository {
    private final File baseDir;

    public ConfigRepository(Context context) {
        baseDir = new File(context.getFilesDir(), "dashboard_configs");
        if (!baseDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            baseDir.mkdirs();
        }
    }

    public void ensureDefault() {
        if (listConfigs().isEmpty()) {
            save("default", defaultJson());
        }
    }

    public List<String> listConfigs() {
        String[] files = baseDir.list((dir, name) -> name.endsWith(".json"));
        List<String> names = new ArrayList<>();
        if (files != null) {
            for (String file : files) {
                names.add(file.substring(0, file.length() - 5));
            }
        }
        Collections.sort(names);
        return names;
    }

    public DashboardConfig.LoadedConfig load(String name) {
        try {
            File file = new File(baseDir, name + ".json");
            String json = readText(file);
            JSONObject root = ConfigValidator.parseAndValidate(json);
            return new DashboardConfig.LoadedConfig(DashboardConfig.fromJson(root), json);
        } catch (Exception e) {
            throw new RuntimeException("Load failed: " + e.getMessage(), e);
        }
    }

    public void save(String name, String json) {
        try {
            JSONObject root = ConfigValidator.parseAndValidate(json);
            File file = new File(baseDir, name + ".json");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(root.toString(2).getBytes(StandardCharsets.UTF_8));
            fos.close();
        } catch (Exception e) {
            throw new RuntimeException("Save failed: " + e.getMessage(), e);
        }
    }

    public void delete(String name) {
        File file = new File(baseDir, name + ".json");
        if (file.exists() && !file.delete()) {
            throw new RuntimeException("Delete failed");
        }
    }

    private String readText(File file) throws Exception {
        StringBuilder sb = new StringBuilder();
        FileInputStream fis = new FileInputStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append('\n');
        }
        br.close();
        fis.close();
        return sb.toString();
    }

    private String defaultJson() {
        return "{\n" +
                "  \"settings\": {\"updateMode\": \"onChange\"},\n" +
                "  \"blocks\": [\n" +
                "    {\"type\":\"numeric\",\"id\":\"pressure\",\"label\":\"Pressure\",\"row\":1,\"col\":12,\"length\":5,\"transform\":{\"type\":\"scale\",\"factor\":0.1,\"unit\":\"bar\"}},\n" +
                "    {\"type\":\"lamp\",\"id\":\"motor\",\"label\":\"Motor\",\"row\":21,\"col\":22,\"conditions\":[{\"equals\":\"On\",\"color\":\"green\"},{\"equals\":\"Off\",\"color\":\"red\"}]}\n" +
                "  ],\n" +
                "  \"buttons\": [\n" +
                "    {\"type\":\"toggle\",\"label\":\"Pump\",\"sendOn\":\"P1\",\"sendOff\":\"P0\"},\n" +
                "    {\"type\":\"macro\",\"label\":\"Init\",\"commands\":[{\"value\":\"A\",\"delayMs\":0},{\"value\":\"B\",\"delayMs\":200}]}\n" +
                "  ]\n" +
                "}";
    }
}
