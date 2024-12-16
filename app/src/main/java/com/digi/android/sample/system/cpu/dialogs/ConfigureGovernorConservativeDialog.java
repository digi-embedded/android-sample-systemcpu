/*
 * Copyright (c) 2016-2025, Digi International Inc. <support@digi.com>
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

package com.digi.android.sample.system.cpu.dialogs;

import android.content.Context;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.digi.android.sample.system.cpu.R;

import com.digi.android.system.cpu.CPUManager;
import com.digi.android.system.cpu.GovernorConservative;
import com.digi.android.system.cpu.GovernorType;
import com.digi.android.system.cpu.exception.CPUException;

import java.util.Locale;

public class ConfigureGovernorConservativeDialog extends ConfigureGovernorDialog {

	// Constants.
	private static final String ERROR_SAMPLING_RATE_EMPTY = "'Sampling rate' value cannot be empty.";
	private static final String ERROR_UP_THRESHOLD_EMPTY = "'Up threshold' value cannot be empty.";
	private static final String ERROR_SAMPLING_DOWN_FACTOR_EMPTY = "'Sampling down factor' value cannot be empty.";
	private static final String ERROR_DOWN_THRESHOLD_EMPTY = "'Down threshold' value cannot be empty.";
	private static final String ERROR_FREQ_STEP_EMPTY = "'Frequency step' value cannot be empty.";

	private static final String ERROR_SAMPLING_RATE_INVALID = "Invalid 'Sampling rate' value.";
	private static final String ERROR_UP_THRESHOLD_INVALID = "Invalid 'Up threshold' value.";
	private static final String ERROR_SAMPLING_DOWN_FACTOR_INVALID = "Invalid 'Sampling down factor' value.";
	private static final String ERROR_DOWN_THRESHOLD_INVALID = "Invalid 'Down threshold' value.";
	private static final String ERROR_FREQ_STEP_INVALID = "Invalid 'Frequency step' value.";

	// Variables.
	private EditText samplingRateEditText;
	private EditText upThresholdEditText;
	private EditText samplingDownFactorEditText;
	private EditText downThresholdEditText;
	private EditText freqStepEditText;

	private TextView minSamplingRateTextView;

	private Switch ignoreNiceLoadSwitch;

	private GovernorConservative governorConservative;

	public ConfigureGovernorConservativeDialog(Context context, CPUManager cpuManager) {
		super(context, GovernorType.CONSERVATIVE, cpuManager);

		try {
			governorConservative = (GovernorConservative)cpuManager.getGovernor();
		} catch (CPUException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void initializeControls() {
		samplingRateEditText = configureDialogView.findViewById(R.id.ondemand_sampling_rate);
		upThresholdEditText = configureDialogView.findViewById(R.id.ondemand_up_threshold);
		samplingDownFactorEditText = configureDialogView.findViewById(R.id.ondemand_sampling_down_factor);
		downThresholdEditText = configureDialogView.findViewById(R.id.conservative_down_threshold);
		freqStepEditText = configureDialogView.findViewById(R.id.conservative_freq_step);

		minSamplingRateTextView = configureDialogView.findViewById(R.id.ondemand_min_sampling_rate);

		ignoreNiceLoadSwitch = configureDialogView.findViewById(R.id.ondemand_ignore_nice_load);
	}

	@Override
	protected void initializeValues(Context context) {
		if (governorConservative == null)
			return;

		// Sampling rate setting.
		try {
			long samplingRate = governorConservative.getSamplingRate();
			samplingRateEditText.setText(String.valueOf(samplingRate));
		} catch (CPUException e) {
			e.printStackTrace();
		}

		// Up threshold setting.
		try {
			int upThreshold = governorConservative.getUpThreshold();
			upThresholdEditText.setText(String.valueOf(upThreshold));
		} catch (CPUException e) {
			e.printStackTrace();
		}

		// Sampling down factor setting.
		try {
			int samplingDownFactor = governorConservative.getSamplingDownFactor();
			samplingDownFactorEditText.setText(String.valueOf(samplingDownFactor));
		} catch (CPUException e) {
			e.printStackTrace();
		}

		// Down threshold setting.
		try {
			int downThreshold = governorConservative.getDownThreshold();
			downThresholdEditText.setText(String.valueOf(downThreshold));
		} catch (CPUException e) {
			e.printStackTrace();
		}

		// Frequency step setting.
		try {
			int freqStep = governorConservative.getFreqStep();
			freqStepEditText.setText(String.valueOf(freqStep));
		} catch (CPUException e) {
			e.printStackTrace();
		}

		// Minimum sampling rate setting.
		try {
			long minSamplingRate = governorConservative.getMinSamplingRate();
			minSamplingRateTextView.setText(String.valueOf(minSamplingRate));
		} catch (CPUException e) {
			e.printStackTrace();
		}

		// Ignore nice load setting.
		try {
			boolean ignoreNiceLoad = governorConservative.getIgnoreNiceLoad();
			ignoreNiceLoadSwitch.setChecked(ignoreNiceLoad);
		} catch (CPUException e) {
			e.printStackTrace();
		}

		// Add the text change listeners to the edit text controls.
		samplingRateEditText.addTextChangedListener(getTextWatcher());
		upThresholdEditText.addTextChangedListener(getTextWatcher());
		samplingDownFactorEditText.addTextChangedListener(getTextWatcher());
		downThresholdEditText.addTextChangedListener(getTextWatcher());
		freqStepEditText.addTextChangedListener(getTextWatcher());
	}

	@Override
	protected void applyValues() {
		if (governorConservative == null)
			return;

		// Sampling rate setting.
		String samplingRateValue = samplingRateEditText.getText().toString();
		long samplingRate;
		try {
			samplingRate = Long.parseLong(samplingRateValue.trim());
			governorConservative.setSamplingRate(samplingRate);
		} catch (NumberFormatException | CPUException e) {
			e.printStackTrace();
		}

		// Up threshold setting.
		String upThresholdValue = upThresholdEditText.getText().toString();
		int upThreshold;
		try {
			upThreshold = Integer.parseInt(upThresholdValue.trim());
			governorConservative.setUpThreshold(upThreshold);
		} catch (NumberFormatException | CPUException e) {
			e.printStackTrace();
		}

		// Sampling down factor setting.
		String samplingDownFactorValue = samplingDownFactorEditText.getText().toString();
		int samplingDownFactor;
		try {
			samplingDownFactor = Integer.parseInt(samplingDownFactorValue.trim());
			governorConservative.setSamplingDownFactor(samplingDownFactor);
		} catch (NumberFormatException | CPUException e) {
			e.printStackTrace();
		}

		// Down threshold setting.
		String downThresholdValue = downThresholdEditText.getText().toString();
		int downThreshold;
		try {
			downThreshold = Integer.parseInt(downThresholdValue.trim());
			governorConservative.setDownThreshold(downThreshold);
		} catch (NumberFormatException | CPUException e) {
			e.printStackTrace();
		}

		// Frequency step setting.
		String freqStepValue = freqStepEditText.getText().toString();
		int freqStep;
		try {
			freqStep = Integer.parseInt(freqStepValue.trim());
			governorConservative.setFreqStep(freqStep);
		} catch (NumberFormatException | CPUException e) {
			e.printStackTrace();
		}

		// Ignore nice load setting.
		try {
			if (ignoreNiceLoadSwitch.isChecked())
				governorConservative.enableIgnoreNiceLoad();
			else
				governorConservative.disableIgnoreNiceLoad();
		} catch (CPUException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected String validateSettings() {
		// Sampling rate value.
		String samplingRateValue = samplingRateEditText.getText().toString();
		if (samplingRateValue.trim().isEmpty())
			return ERROR_SAMPLING_RATE_EMPTY;
		try {
			long longVar = Long.parseLong(samplingRateValue.trim());
			long minSampleRate = governorConservative.getMinSamplingRate();
			if (longVar < minSampleRate || longVar > GovernorConservative.MAX_SAMPLING_RATE)
				return ERROR_SAMPLING_RATE_INVALID + " "
						+ String.format(Locale.getDefault(), ERROR_LIMITS, minSampleRate, GovernorConservative.MAX_SAMPLING_RATE);
		} catch (NumberFormatException | CPUException e) {
			return ERROR_SAMPLING_RATE_INVALID;
		}

		// Up threshold value.
		String upThresholdValue = upThresholdEditText.getText().toString();
		int upThr;
		if (upThresholdValue.trim().isEmpty())
			return ERROR_UP_THRESHOLD_EMPTY;
		try {
			upThr = Integer.parseInt(upThresholdValue.trim());
		} catch (NumberFormatException e) {
			return ERROR_UP_THRESHOLD_INVALID;
		}

		// Down threshold value.
		String downThresholdValue = downThresholdEditText.getText().toString();
		int downThr;
		if (downThresholdValue.trim().isEmpty())
			return ERROR_DOWN_THRESHOLD_EMPTY;
		try {
			downThr = Integer.parseInt(downThresholdValue.trim());
		} catch (NumberFormatException e) {
			return ERROR_DOWN_THRESHOLD_INVALID;
		}

		// Up threshold limits.
		if (upThr <= downThr || upThr > 100)
			return ERROR_UP_THRESHOLD_INVALID + " "
					+ String.format(Locale.getDefault(), ERROR_LIMITS, downThr, 100);

		// Down threshold limits.
		if (downThr < GovernorConservative.MIN_DOWN_THRESHOLD)
			return ERROR_DOWN_THRESHOLD_INVALID + " "
					+ String.format(Locale.getDefault(), ERROR_LIMITS, GovernorConservative.MIN_DOWN_THRESHOLD, upThr);


		// Sampling down factor value.
		String samplingDownFactorValue = samplingDownFactorEditText.getText().toString();
		if (samplingDownFactorValue.trim().isEmpty())
			return ERROR_SAMPLING_DOWN_FACTOR_EMPTY;
		try {
			int intVar = Integer.parseInt(samplingDownFactorValue.trim());
			if (intVar < 1 || intVar > GovernorConservative.MAX_SAMPLING_DOWN_FACTOR)
				return ERROR_SAMPLING_DOWN_FACTOR_INVALID + " "
						+ String.format(Locale.getDefault(), ERROR_LIMITS, 1, GovernorConservative.MAX_SAMPLING_DOWN_FACTOR);
		} catch (NumberFormatException e) {
			return ERROR_SAMPLING_DOWN_FACTOR_INVALID;
		}

		// Frequency step value.
		String freqStepValue = freqStepEditText.getText().toString();
		if (freqStepValue.trim().isEmpty())
			return ERROR_FREQ_STEP_EMPTY;
		try {
			int intVar = Integer.parseInt(freqStepValue.trim());
			if (intVar < 0 || intVar > 100)
				return ERROR_FREQ_STEP_INVALID + " "
						+ String.format(Locale.getDefault(), ERROR_LIMITS, 0, 100);
		} catch (NumberFormatException e) {
			return ERROR_FREQ_STEP_INVALID;
		}

		return null;
	}
}
