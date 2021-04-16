/*
 * Copyright (c) 2016-2021, Digi International Inc. <support@digi.com>
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.digi.android.sample.system.cpu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;

import com.digi.android.sample.system.cpu.dialogs.ConfigureGovernorConservativeDialog;
import com.digi.android.sample.system.cpu.dialogs.ConfigureGovernorDialog;
import com.digi.android.sample.system.cpu.dialogs.ConfigureGovernorInteractiveDialog;
import com.digi.android.sample.system.cpu.dialogs.ConfigureGovernorOndemandDialog;
import com.digi.android.sample.system.cpu.dialogs.ConfigureGovernorSchedutilDialog;
import com.digi.android.sample.system.cpu.dialogs.ConfigureGovernorUserspaceDialog;
import com.digi.android.sample.system.cpu.pi.Pi;
import com.digi.android.sample.system.cpu.pi.PiParallel;

import com.digi.android.system.cpu.CPUManager;
import com.digi.android.system.cpu.GovernorType;
import com.digi.android.system.cpu.exception.CPUException;
import com.digi.android.system.cpu.exception.CPUTemperatureException;
import com.digi.android.system.cpu.exception.NoSuchCoreException;
import com.digi.android.system.memory.MemoryManager;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * CPU sample application.
 *
 * <p>This example displays some panels to configure the CPU cores and settings
 * and monitoring some of the CPU parameters.</p>
 *
 * <p>For a complete description on the example, refer to the 'README.md' file
 * included in the example directory.</p>
 */
public class CPUSampleApp extends Activity {

	// Constants.
	public static final String PI_STATUS_CANCELED = "Canceled";
	public static final String PI_STATUS_FINISHED = "Finished";

	private static final int CPU_USAGE_MAX_TIME = 60;
	private static final long MAX_DIGITS = 30000000L;
	private static final int MAX_DIGITS_RESULT = 1000;
	private static final int STATUS_PERIOD = 3;

	// Variables.
	private TextView piTimeText;
	private TextView piProgressText;
	private TextView statusTemperatureText;
	private TextView statusUsageText;
	private TextView statusFreqText;
	private TextView statusMemoryText;

	private EditText piDigitsEditText;

	private Switch core1Switch;
	private Switch core2Switch;
	private Switch core3Switch;
	private Switch core4Switch;

	private Spinner maxFrequencySpinner;
	private Spinner minFrequencySpinner;
	private Spinner governorsSpinner;

	private Button configureGovernorButton;
	private Button piResultsButton;

	private ToggleButton piCalculationButton;

	private CheckBox core1CheckBox;
	private CheckBox core2CheckBox;
	private CheckBox core3CheckBox;
	private CheckBox core4CheckBox;

	private XYPlot cpuPlot;

	private static SimpleXYSeries cpuSeries;
	private static SimpleXYSeries core1Series;
	private static SimpleXYSeries core2Series;
	private static SimpleXYSeries core3Series;
	private static SimpleXYSeries core4Series;

	private LineAndPointFormatter core1Formatter;
	private LineAndPointFormatter core2Formatter;
	private LineAndPointFormatter core3Formatter;
	private LineAndPointFormatter core4Formatter;

	private CPUManager cpuManager;
	private MemoryManager memoryManager;

	private boolean core2Enabled = false;
	private boolean core3Enabled = false;
	private boolean core4Enabled = false;
	private boolean readingUsage = false;
	private boolean governorDlgOpen = false;

	private int numberOfCores = 1;
	private int statusLoops = 0;

	private float overallUsage = 0.0f;

	private String pi = "";

	private GovernorType currentGovernorType = GovernorType.UNKNOWN;

	private Timer timer;

	private Thread cpuUsageThread;

	private static CPUSampleApp instance;

	private ProgressReceiver progressReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		instance = this;

		setContentView(R.layout.main);

		// Initialize the application controls.
		initializeControls();
		addControlsCallbacks();
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (progressReceiver == null)
			progressReceiver = new ProgressReceiver();
		registerReceiver(progressReceiver, new IntentFilter(Pi.NEW_PROGRESS_INTENT));

		// Initialize all the CPU values and set them in the corresponding controls.
		initializeValues();

		// Initialize CPU Usage plot
		initializeCPUUsagePlot();

		// Start the CPU usage thread and CPU status timer.
		startReadingUsage();
		startCPUStatusTimer();

		// Set focus to the start Pi calculation button.
		piCalculationButton.setFocusable(true);
		piCalculationButton.requestFocus();
	}

	@Override
	protected void onPause() {
		super.onPause();
		stopReadingUsage();
		stopCPUStatusTimer();
		PiParallel.cancel();

		cpuPlot.removeSeries(cpuSeries);
		cpuPlot.removeSeries(core1Series);
		cpuPlot.removeSeries(core2Series);
		cpuPlot.removeSeries(core3Series);
		cpuPlot.removeSeries(core4Series);

		if (progressReceiver != null)
			unregisterReceiver(progressReceiver);
	}

	/**
	 * Initializes application controls.
	 */
	private void initializeControls() {
		// Declare the managers to be used in the application.
		cpuManager = new CPUManager(this);
		memoryManager = new MemoryManager(this);

		// Declare views by retrieving them with the ID.
		piTimeText = findViewById(R.id.pi_total_time);
		piProgressText = findViewById(R.id.pi_progress);
		statusTemperatureText = findViewById(R.id.status_temperature);
		statusUsageText = findViewById(R.id.status_usage);
		statusFreqText = findViewById(R.id.status_frequency);
		statusMemoryText = findViewById(R.id.status_memory);

		piDigitsEditText = findViewById(R.id.pi_digits);

		core1Switch = findViewById(R.id.core1_switch);
		core2Switch = findViewById(R.id.core2_switch);
		core3Switch = findViewById(R.id.core3_switch);
		core4Switch = findViewById(R.id.core4_switch);

		maxFrequencySpinner = findViewById(R.id.setting_max_freq_list);
		minFrequencySpinner = findViewById(R.id.setting_min_freq_list);
		governorsSpinner = findViewById(R.id.setting_governors_list);

		configureGovernorButton = findViewById(R.id.setting_config_governor_button);
		piResultsButton = findViewById(R.id.results_button);

		piCalculationButton = findViewById(R.id.start_calc_button);

		core1CheckBox = findViewById(R.id.track_core1_button);
		core2CheckBox = findViewById(R.id.track_core2_button);
		core3CheckBox = findViewById(R.id.track_core3_button);
		core4CheckBox = findViewById(R.id.track_core4_button);
	}

	/**
	 * Initializes the cores status and configuration values of the CPU. Resets the usage plot and
	 * Pi calculation values.
	 */
	private void initializeValues() {
		// Get the available cores and their status, configure the controls accordingly.
		numberOfCores = cpuManager.getNumberOfCores();
		try {
			core1CheckBox.setEnabled(true);
			core1Switch.setChecked(true);
			core1CheckBox.setChecked(true);
			if (numberOfCores > 1) {
				core2Switch.setEnabled(true);
				core2Enabled = cpuManager.isCoreEnabled(1);
				core2CheckBox.setEnabled(true);
				core2CheckBox.setChecked(true);
			}
			if (numberOfCores > 2) {
				core3Switch.setEnabled(true);
				core3Enabled = cpuManager.isCoreEnabled(2);
				core3CheckBox.setEnabled(true);
				core3CheckBox.setChecked(true);
			}
			if (numberOfCores > 3) {
				core4Switch.setEnabled(true);
				core4Enabled = cpuManager.isCoreEnabled(3);
				core4CheckBox.setEnabled(true);
				core4CheckBox.setChecked(true);
			}
		} catch (NoSuchCoreException | CPUException e) {
			displayError(e.getMessage());
		}

		// Get the available frequencies and fill the configuration controls.
		ArrayAdapter<Integer> frequenciesListAdapter;
		try {
			ArrayList<Integer> frequencies = cpuManager.getAvailableFrequencies();
			frequenciesListAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, frequencies);
			frequenciesListAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
			maxFrequencySpinner.setAdapter(frequenciesListAdapter);
			minFrequencySpinner.setAdapter(frequenciesListAdapter);

			// Configure the selected max and min scaling frequencies.
			int maxScalingFreq = cpuManager.getMaxScalingFrequency();
			int minScalingFreq = cpuManager.getMinScalingFrequency();

			if (frequenciesListAdapter.getPosition(maxScalingFreq) != -1)
				maxFrequencySpinner.setSelection(frequenciesListAdapter.getPosition(maxScalingFreq));
			if (frequenciesListAdapter.getPosition(minScalingFreq) != -1)
				minFrequencySpinner.setSelection(frequenciesListAdapter.getPosition(minScalingFreq));
		} catch (CPUException e) {
			displayError(e.getMessage());
		}

		// Get the available governor types and fill the governors list.
		ArrayAdapter<GovernorType> governorTypesAdapter;
		try {
			ArrayList<GovernorType> governorTypes = cpuManager.getAvailableGovernorTypes();
			governorTypesAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, governorTypes);
			governorTypesAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
			governorsSpinner.setAdapter(governorTypesAdapter);

			// Configure the selected governor type.
			currentGovernorType = cpuManager.getGovernor().getGovernorType();
			if (governorTypesAdapter.getPosition(currentGovernorType) != -1)
				governorsSpinner.setSelection(governorTypesAdapter.getPosition(currentGovernorType));
		} catch (CPUException e) {
			displayError(e.getMessage());
		}

		// Refresh the status of controls.
		refreshCoresControls();
		refreshConfigGovernorButton();
	}

	/**
	 * Adds the callbacks to all the UI controls the user will interact with.
	 */
	private void addControlsCallbacks() {
		// Set the core switches callbacks.
		core2Switch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				handleCoreEnablePressed(1);
			}
		});
		core3Switch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				handleCoreEnablePressed(2);
			}
		});
		core4Switch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				handleCoreEnablePressed(3);
			}
		});

		// Set the spinners callbacks.
		maxFrequencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				handleMaxFreqChanged();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) { }
		});
		minFrequencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				handleMinFreqChanged();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) { }
		});
		governorsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				handleGovernorChanged();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) { }
		});

		// Set the edit texts callbacks.
		piDigitsEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }

			@Override
			public void afterTextChanged(Editable arg0) {
				try {
					piCalculationButton.setEnabled(Long.parseLong(piDigitsEditText.getText().toString()) > 0);
				} catch (NumberFormatException ex) {
					piCalculationButton.setEnabled(false);
				}
			}
		});

		// Set the buttons callbacks.
		configureGovernorButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				handleConfigureGovernorButtonPressed();
			}
		});
		piCalculationButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				handleCalculatePiButtonPressed();
			}
		});
		piResultsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				handleViewResultsButtonPressed();
			}
		});

		// Set the core check boxes callbacks.
		core1CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				if (core1CheckBox.isShown())
					showCoreUsage(1, b);
			}
		});
		core2CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				if (core2CheckBox.isShown())
					showCoreUsage(2, b);
			}
		});
		core3CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				if (core3CheckBox.isShown())
					showCoreUsage(3, b);
			}
		});
		core4CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				if (core4CheckBox.isShown())
					showCoreUsage(4, b);
			}
		});
	}

	/**
	 * Initializes and configures the CPU plot and series.
	 */
	private void initializeCPUUsagePlot() {
		cpuPlot = findViewById(R.id.cpu_usage_plot);

		cpuPlot.setRangeBoundaries(0, 100, BoundaryMode.FIXED);
		cpuPlot.setDomainBoundaries(0, 60, BoundaryMode.FIXED);
		cpuPlot.setDomainStepValue(7);

		cpuPlot.setRangeStepValue(11);

		// Customize domain and range labels.
		cpuPlot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).
				setFormat(new DecimalFormat("0"));
		cpuPlot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM)
				.setFormat(new DecimalFormat("0"));

		cpuPlot.getLayoutManager().remove(cpuPlot.getLegend());
		cpuPlot.getLayoutManager().remove(cpuPlot.getTitle());
		cpuPlot.getLayoutManager().remove(cpuPlot.getDomainTitle());
		cpuPlot.getLayoutManager().remove(cpuPlot.getRangeTitle());

		// CPU series (always present).
		cpuSeries = new SimpleXYSeries("CPU Usage (%)");
		cpuSeries.useImplicitXVals();
		LineAndPointFormatter cpuFormatter = new LineAndPointFormatter(getResources().getColor(R.color.blue), null,
				getResources().getColor(R.color.blue), null);
		Paint cpuPaint = new Paint();
		cpuPaint.setAlpha(100);
		cpuPaint.setShader(new LinearGradient(0, 0, 0, 250, getResources().getColor(R.color.blue),
				getResources().getColor(R.color.blue), Shader.TileMode.MIRROR));
		cpuFormatter.setFillPaint(cpuPaint);
		cpuPlot.addSeries(cpuSeries, cpuFormatter);

		// Core 1 series (always present).
		core1Series = new SimpleXYSeries("Core 1 Usage (%)");
		core1Series.useImplicitXVals();
		core1Formatter = new LineAndPointFormatter(getResources().getColor(R.color.dark_green), null,
				Color.TRANSPARENT, null);
		cpuPlot.addSeries(core1Series, core1Formatter);

		// Core 2 series.
		if (numberOfCores > 1) {
			core2Series = new SimpleXYSeries("Core 2 Usage (%)");
			core2Series.useImplicitXVals();
			core2Formatter = new LineAndPointFormatter(getResources().getColor(R.color.red), null,
					Color.TRANSPARENT, null);
			cpuPlot.addSeries(core2Series, core2Formatter);
		}

		// Core 3 series.
		if (numberOfCores > 2) {
			core3Series = new SimpleXYSeries("Core 3 Usage (%)");
			core3Series.useImplicitXVals();
			core3Formatter = new LineAndPointFormatter(getResources().getColor(R.color.orange), null,
					Color.TRANSPARENT, null);
			cpuPlot.addSeries(core3Series, core3Formatter);
		}

		// Core 4 series.
		if (numberOfCores > 3) {
			core4Series = new SimpleXYSeries("Core 4 Usage (%)");
			core4Series.useImplicitXVals();
			core4Formatter = new LineAndPointFormatter(getResources().getColor(R.color.purple), null,
					Color.TRANSPARENT, null);
			cpuPlot.addSeries(core4Series, core4Formatter);
		}
	}

	/**
	 * Enables or disables the core corresponding to the provided index.
	 *
	 * @param coreIndex Index of the core to toggle.
	 */
	private void handleCoreEnablePressed(int coreIndex) {
		try {
			switch (coreIndex) {
				case 1:
					if (core2Enabled) {
						cpuManager.disableCore(coreIndex);
						core2Enabled = false;
					} else {
						cpuManager.enableCore(coreIndex);
						core2Enabled = true;
					}
					break;
				case 2:
					if (core3Enabled) {
						cpuManager.disableCore(coreIndex);
						core3Enabled = false;
					} else {
						cpuManager.enableCore(coreIndex);
						core3Enabled = true;
					}
					break;
				case 3:
					if (core4Enabled) {
						cpuManager.disableCore(coreIndex);
						core4Enabled = false;
					} else {
						cpuManager.enableCore(coreIndex);
						core4Enabled = true;
					}
					break;
				default:
					break;
			}
			refreshCoresControls();
		} catch (CPUException | NoSuchCoreException e) {
			displayError(e.getMessage());
		}
	}

	/**
	 * Shows or hides the core series in the CPU usage graph.
	 *
	 * @param coreID The ID of the core to show or hide from the CPU usage core.
	 * @param show {@code true} to show the core usage in the graph, {@code false} to hide it.
	 */
	private void showCoreUsage(int coreID, boolean show) {
		switch (coreID) {
			case 1:
				if (show)
					cpuPlot.addSeries(core1Series, core1Formatter);
				else
					cpuPlot.removeSeries(core1Series);
				break;
			case 2:
				if (show)
					cpuPlot.addSeries(core2Series, core2Formatter);
				else
					cpuPlot.removeSeries(core2Series);
				break;
			case 3:
				if (show)
					cpuPlot.addSeries(core3Series, core3Formatter);
				else
					cpuPlot.removeSeries(core3Series);
				break;
			case 4:
				if (show)
					cpuPlot.addSeries(core4Series, core4Formatter);
				else
					cpuPlot.removeSeries(core4Series);
				break;
			default:
		}
		cpuPlot.redraw();
	}

	/**
	 * Refreshes the core controls.
	 */
	private void refreshCoresControls() {
		core2Switch.setChecked(core2Enabled);
		core3Switch.setChecked(core3Enabled);
		core4Switch.setChecked(core4Enabled);
	}

	/**
	 * Updates the status (enabled or disabled) of the governor configuration button based on the
	 * selected governor.
	 */
	private void refreshConfigGovernorButton() {
		if (governorsSpinner.getSelectedItem() == null
				|| governorsSpinner.getSelectedItemPosition() == -1)
			return;

		GovernorType governorType = (GovernorType)governorsSpinner.getSelectedItem();
		switch (governorType) {
			case CONSERVATIVE:
			case ONDEMAND:
			case INTERACTIVE:
			case USERSPACE:
            case SCHEDUTIL:
				configureGovernorButton.setEnabled(true);
				break;
			case PERFORMANCE:
			case POWERSAVE:
			default:
				configureGovernorButton.setEnabled(false);
		}
	}

	/**
	 * Opens the corresponding governor configuration dialog based on the type of governor
	 * configured.
	 */
	private void handleConfigureGovernorButtonPressed() {
		if (governorDlgOpen)
			return;

		governorDlgOpen = true;

		final ConfigureGovernorDialog configureGovernorDialog;
		switch (currentGovernorType) {
			case CONSERVATIVE:
				configureGovernorDialog = new ConfigureGovernorConservativeDialog(this, cpuManager);
				break;
			case ONDEMAND:
				configureGovernorDialog = new ConfigureGovernorOndemandDialog(this, cpuManager);
				break;
			case INTERACTIVE:
				configureGovernorDialog = new ConfigureGovernorInteractiveDialog(this, cpuManager);
				break;
            case SCHEDUTIL:
                configureGovernorDialog = new ConfigureGovernorSchedutilDialog(this, cpuManager);
                break;
			case USERSPACE:
			default:
				configureGovernorDialog = new ConfigureGovernorUserspaceDialog(this, cpuManager);
				break;
		}

		configureGovernorDialog.show();

		// This thread waits until dialog is closed, dialog is notified itself when that happens.
		Thread waitThread = new Thread() {
			@Override
			public void run() {
				synchronized (configureGovernorDialog) {
					try {
						configureGovernorDialog.wait();
						governorDlgOpen = false;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		waitThread.start();
	}

	/**
	 * Configures the maximum frequency of the CPU with the one selected in the application UI.
	 */
	private void handleMaxFreqChanged() {
		if (maxFrequencySpinner.getSelectedItem() == null
				|| maxFrequencySpinner.getSelectedItemPosition() == -1)
			return;

		int maxScalingFrequency = (int)maxFrequencySpinner.getSelectedItem();
		int minScalingFrequency = (int)minFrequencySpinner.getSelectedItem();

		try {
			// Check the value of the frequency.
			if (maxScalingFrequency < minScalingFrequency) {
				Toast toast = Toast.makeText(getApplicationContext(),
						"Maximum scaling frequency cannot be smaller than the minimum scaling frequency.", Toast.LENGTH_LONG);
				ArrayAdapter<Integer> frequenciesListAdapter = (ArrayAdapter<Integer>)maxFrequencySpinner.getAdapter();
				maxFrequencySpinner.setSelection(frequenciesListAdapter.getPosition(cpuManager.getMaxScalingFrequency()));
				toast.show();
				return;
			}

			cpuManager.setMaxScalingFrequency(maxScalingFrequency);
		} catch (CPUException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Configures the minimum frequency of the CPU with the one selected in the application UI.
	 */
	private void handleMinFreqChanged() {
		if (minFrequencySpinner.getSelectedItem() == null
				|| minFrequencySpinner.getSelectedItemPosition() == -1)
			return;

		int minScalingFrequency = (int)minFrequencySpinner.getSelectedItem();
		int maxScalingFrequency = (int)maxFrequencySpinner.getSelectedItem();

		try {
			// Check the value of the frequency.
			if (maxScalingFrequency < minScalingFrequency) {
				Toast toast = Toast.makeText(getApplicationContext(),
						"Minimum scaling frequency cannot be greater than the maximum scaling frequency.", Toast.LENGTH_LONG);
				ArrayAdapter<Integer> frequenciesListAdapter = (ArrayAdapter<Integer>)minFrequencySpinner.getAdapter();
				minFrequencySpinner.setSelection(frequenciesListAdapter.getPosition(cpuManager.getMinScalingFrequency()));
				toast.show();
				return;
			}

			cpuManager.setMinScalingFrequency(minScalingFrequency);
		} catch (CPUException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Configures the governor type in the CPU with the one selected in the application UI.
	 */
	private void handleGovernorChanged() {
		if (governorsSpinner.getSelectedItem() == null
				|| governorsSpinner.getSelectedItemPosition() == -1)
			return;

		GovernorType governorType = (GovernorType)governorsSpinner.getSelectedItem();
		try {
			cpuManager.setGovernorType(governorType);
			currentGovernorType = governorType;
		} catch (CPUException e) {
			e.printStackTrace();
		}

		refreshConfigGovernorButton();
	}

	/**
	 * Starts the Pi calculation process with the CPU configuration established.
	 */
	private void handleCalculatePiButtonPressed() {
		if (piCalculationButton.isChecked()) {
			if (Long.parseLong(piDigitsEditText.getText().toString()) > MAX_DIGITS)
				piDigitsEditText.setText(String.valueOf(MAX_DIGITS));

			piProgressText.setText("0%");
			piTimeText.setText("");
			piResultsButton.setEnabled(false);

			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						long time = System.currentTimeMillis();
						PiParallel.calculatePi(Long.parseLong(piDigitsEditText.getText().toString()));
						final long elapsed = System.currentTimeMillis() - time;
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								piProgressText.setText(PI_STATUS_FINISHED);
								piTimeText.setText(String.format(Locale.getDefault(), "%.2f s", elapsed / 1000.0));
								piResultsButton.setEnabled(true);
								piCalculationButton.setChecked(false);
							}
						});
					} catch (ThreadDeath ignored) {}
				}
			}).start();
		} else {
			PiParallel.cancel();
			piProgressText.setText(PI_STATUS_CANCELED);
		}
	}

	/**
	 * Opens a pop-up with the result of Pi number calculation.
	 */
	private void handleViewResultsButtonPressed() {
		long digits = Long.parseLong(piDigitsEditText.getText().toString());
		String title = getResources().getString(R.string.pi_result);
		if (digits > MAX_DIGITS_RESULT)
			title += " " + String.format(getResources().getString(R.string.pi_first_digits), MAX_DIGITS_RESULT);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(pi).
				setTitle(title);
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	/**
	 * Displays a pop-up with the given error message and exits the application.
	 *
	 * @param errorMessage The error message to display.
	 */
	private void displayError(String errorMessage) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(errorMessage);
		builder.setCancelable(true);

		builder.setPositiveButton(
				"OK",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
						finish();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	/**
	 * Thread used to read and store the CPU usage of the device.
	 */
	private class CPUUsageThread extends Thread {
		@Override
		public void run() {
			readingUsage = true;
			while (readingUsage) {
				try {
					ArrayList<Float> usages = cpuManager.getUsage(250);
					overallUsage = ((int)(usages.get(0) * 100))/100.0f;
					if (!readingUsage)
						return;

					if (cpuSeries.size() > CPU_USAGE_MAX_TIME)
						cpuSeries.removeFirst();
					cpuSeries.addLast(null, usages.get(0));
					if (core1Series.size() > CPU_USAGE_MAX_TIME)
						core1Series.removeFirst();
					core1Series.addLast(null, usages.get(1));

					if (numberOfCores > 1) {
						if (core2Series.size() > CPU_USAGE_MAX_TIME)
							core2Series.removeFirst();
						core2Series.addLast(null, usages.get(2));
					}
					if (numberOfCores > 2) {
						if (core3Series.size() > CPU_USAGE_MAX_TIME)
							core3Series.removeFirst();
						core3Series.addLast(null, usages.get(3));
					}
					if (numberOfCores > 3) {
						if (core4Series.size() > CPU_USAGE_MAX_TIME)
							core4Series.removeFirst();
						core4Series.addLast(null, usages.get(4));
					}
				} catch (CPUException e) {
					e.printStackTrace();
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException ignored) { }
			}
		}
	}

	/**
	 * Starts reading the usage of the CPU.
	 */
	private void startReadingUsage() {
		cpuUsageThread = new CPUUsageThread();
		cpuUsageThread.start();
	}

	/**
	 * Stops reading the usage of the CPU.
	 */
	private void stopReadingUsage() {
		readingUsage = false;
		if (cpuUsageThread != null) {
			cpuUsageThread.interrupt();
			cpuUsageThread = null;
		}
	}

	/**
	 * Starts the timer which updates the UI with the new parameters.
	 */
	private void startCPUStatusTimer() {
		if (timer != null)
			timer.cancel();
		timer = new Timer(true);
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						cpuPlot.redraw();
						statusLoops += 1;

						// Update the status values.
						if (statusLoops == STATUS_PERIOD) {
							statusLoops = 0;
							int currentFrequency = -1;
							float temperature = -1.0f;
							long memory = -1;
							long totalMemory = -1;
							try {
								currentFrequency = cpuManager.getFrequency();
								temperature = cpuManager.getCurrentTemperature();
								memory = memoryManager.getFreeMemory();
								totalMemory = memoryManager.getTotalMemory();
							} catch (CPUException | CPUTemperatureException | IOException e) {
								e.printStackTrace();
							}
							statusTemperatureText.setText(String.format(Locale.getDefault(), "%.2f °C", temperature));
							statusUsageText.setText(String.format(Locale.getDefault(),"%.2f %%", overallUsage));
							statusFreqText.setText(String.format(Locale.getDefault(),"%d kHz", currentFrequency));
							statusMemoryText.setText(String.format(Locale.getDefault(),"%d / %d kB", memory, totalMemory));
						}
					}
				});
			}
		}, 0, 1000);
	}

	/**
	 * Stops the timer which updates the UI with the new parameters.
	 */
	private void stopCPUStatusTimer() {
		if (timer != null) {
			timer.cancel();
			timer.purge();
		}
	}

	/**
	 * This receiver listens for new progress from the Pi calculus.
	 */
	private class ProgressReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle b = intent.getExtras();
			String progress = b != null ? b.getString("progress") : null;
			if (progress != null) {
				piProgressText.setText(progress.equals("100%") ? "Finishing..." : progress);
			}
			String result = b != null ? b.getString("result") : null;
			if (result != null)
				pi = result;
		}
	}

	/**
	 * Retrieves the running MainApplication instance.
	 *
	 * @return MainApplication instance.
	 */
	public static CPUSampleApp getInstance() {
		return instance;
	}
}
