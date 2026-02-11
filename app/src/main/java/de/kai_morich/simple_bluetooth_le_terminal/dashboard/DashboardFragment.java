package de.kai_morich.simple_bluetooth_le_terminal.dashboard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.kai_morich.simple_bluetooth_le_terminal.R;
import de.kai_morich.simple_bluetooth_le_terminal.SerialService;
import de.kai_morich.simple_bluetooth_le_terminal.dashboard.blocks.BaseBlockController;
import de.kai_morich.simple_bluetooth_le_terminal.dashboard.blocks.MacroButtonController;
import de.kai_morich.simple_bluetooth_le_terminal.dashboard.blocks.ToggleButtonController;
import de.kai_morich.simple_bluetooth_le_terminal.dashboard.config.ButtonConfig;
import de.kai_morich.simple_bluetooth_le_terminal.dashboard.config.ConfigRepository;
import de.kai_morich.simple_bluetooth_le_terminal.dashboard.config.DashboardConfig;
import de.kai_morich.simple_bluetooth_le_terminal.dashboard.recorder.DashboardRecorder;

public class DashboardFragment extends Fragment implements ServiceConnection, DashboardDataTap, DashboardEngine.Listener {

    private SerialService service;
    private ConfigRepository repository;
    private DashboardEngine engine;
    private DashboardRecorder recorder;

    private Spinner configSpinner;
    private TextView recorderStatus;
    private LinearLayout blockContainer;
    private LinearLayout buttonContainer;

    private final Map<String, BaseBlockController> blockById = new HashMap<>();
    private String activeJson = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = new ConfigRepository(requireContext());
        repository.ensureDefault();
        engine = new DashboardEngine();
        engine.setListener(this);
        recorder = new DashboardRecorder(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        configSpinner = view.findViewById(R.id.config_spinner);
        recorderStatus = view.findViewById(R.id.recorder_status);
        blockContainer = view.findViewById(R.id.block_container);
        buttonContainer = view.findViewById(R.id.button_container);

        view.findViewById(R.id.config_new).setOnClickListener(v -> promptNewConfig());
        view.findViewById(R.id.config_save).setOnClickListener(v -> saveCurrent());
        view.findViewById(R.id.config_delete).setOnClickListener(v -> deleteCurrent());
        view.findViewById(R.id.config_edit).setOnClickListener(v -> openEditor());
        view.findViewById(R.id.config_load).setOnClickListener(v -> loadSelected());
        view.findViewById(R.id.recorder_toggle).setOnClickListener(v -> toggleRecorder((Button) v));

        reloadConfigList();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        requireActivity().bindService(new Intent(requireActivity(), SerialService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        if (service != null) {
            service.unregisterDashboardDataTap(this);
        }
        try {
            requireActivity().unbindService(this);
        } catch (Exception ignored) {
        }
        super.onStop();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        service.registerDashboardDataTap(this);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    @Override
    public void onRawSerialText(String text) {
        engine.onSerialText(text);
    }

    @Override
    public void onBlocksUpdated(List<DashboardUiModel> updates, boolean alwaysMode) {
        for (DashboardUiModel model : updates) {
            BaseBlockController controller = blockById.get(model.id);
            if (controller != null) {
                controller.updateView(model);
                if (recorder.isRecording()) {
                    recorder.record(System.currentTimeMillis(), model.id, model.displayValue);
                }
            }
        }
    }

    private void reloadConfigList() {
        List<String> names = repository.listConfigs();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, names);
        configSpinner.setAdapter(adapter);
        if (!names.isEmpty()) {
            loadConfigByName(names.get(0));
        }
    }

    private void loadSelected() {
        Object item = configSpinner.getSelectedItem();
        if (item != null) {
            loadConfigByName(item.toString());
        }
    }

    private void loadConfigByName(String name) {
        try {
            DashboardConfig.LoadedConfig loaded = repository.load(name);
            activeJson = loaded.json;
            engine.setConfig(loaded.config);
            rebuildBlockViews(engine.getBlocks());
            rebuildButtonViews(loaded.config.buttons);
            Toast.makeText(requireContext(), "Loaded " + name, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void rebuildBlockViews(List<BaseBlockController> controllers) {
        blockContainer.removeAllViews();
        blockById.clear();
        for (BaseBlockController controller : controllers) {
            blockContainer.addView(controller.createView(requireContext()));
            blockById.put(controller.getId(), controller);
        }
    }

    private void rebuildButtonViews(List<ButtonConfig> buttons) {
        buttonContainer.removeAllViews();
        for (ButtonConfig config : buttons) {
            if ("toggle".equals(config.type)) {
                buttonContainer.addView(new ToggleButtonController(config).createButton(requireContext(), service));
            } else if ("macro".equals(config.type)) {
                buttonContainer.addView(new MacroButtonController(config).createButton(requireContext(), service));
            }
        }
    }

    private void promptNewConfig() {
        EditText input = new EditText(requireContext());
        input.setHint("config name");
        new AlertDialog.Builder(requireContext())
                .setTitle("New Config")
                .setView(input)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        repository.save(name, activeJson.isEmpty() ? repository.load("default").json : activeJson);
                        reloadConfigList();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void saveCurrent() {
        Object item = configSpinner.getSelectedItem();
        if (item == null) return;
        try {
            repository.save(item.toString(), activeJson);
            Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void deleteCurrent() {
        Object item = configSpinner.getSelectedItem();
        if (item == null) return;
        try {
            repository.delete(item.toString());
            repository.ensureDefault();
            reloadConfigList();
        } catch (Exception e) {
            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void openEditor() {
        final EditText editor = new EditText(requireContext());
        editor.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editor.setSingleLine(false);
        editor.setText(activeJson);
        editor.setTypeface(android.graphics.Typeface.MONOSPACE);
        ScrollView wrap = new ScrollView(requireContext());
        wrap.addView(editor);
        new AlertDialog.Builder(requireContext())
                .setTitle("Edit JSON")
                .setView(wrap)
                .setPositiveButton("Save", (dialog, which) -> {
                    activeJson = editor.getText().toString();
                    saveCurrent();
                    loadSelected();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void toggleRecorder(Button button) {
        if (recorder.isRecording()) {
            recorder.stop();
            recorderStatus.setText("Recorder: stopped");
            button.setText("Start Recorder");
        } else {
            String fileName = "dashboard_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".csv";
            String activeFile = recorder.start(fileName);
            recorderStatus.setText("Recorder: " + activeFile);
            button.setText("Stop Recorder");
        }
    }
}
