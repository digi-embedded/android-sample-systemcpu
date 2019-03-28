/*
 * Copyright (c) 2016-2019, Digi International Inc. <support@digi.com>
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
import com.digi.android.system.cpu.GovernorOnDemand;
import com.digi.android.system.cpu.GovernorType;
import com.digi.android.system.cpu.exception.CPUException;

public class ConfigureGovernorOndemandDialog extends ConfigureGovernorDialog {

	// Constants.
	private static final String ERROR_SAMPLING_RATE_EMPTY = "'Sampling rate' value cannot be empty.";
	private static final String ERROR_UP_THRESHOLD_EMPTY = "'Up threshold' value cannot be empty.";
	private static final String ERROR_SAMPLING_DOWN_FACTOR_EMPTY = "'Sampling down factor' value cannot be empty.";

	private static final String ERROR_SAMPLING_RATE_INVALID = "Invalid 'Sampling rate' value.";
	private static final String ERROR_UP_THRESHOLD_INVALID = "Invalid 'Up threshold' value.";
	private static final String ERROR_SAMPLING_DOWN_FACTOR_INVALID = "Invalid 'Sampling down factor' value.";

	// Variables.
	private EditText samplingRateEditText;
	private EditText upThresholdEditText;
	private EditText samplingDownFactorEditText;

	private TextView minSamplingRateTextView;

	private Switch ignoreNiceLoadSwitch;

	private GovernorOnDemand governorOnDemand;

	public ConfigureGovernorOndemandDialog(Context context, CPUManager cpuManager) {
		super(context, GovernorType.ONDEMAND, cpuManager);

		try {
			governorOnDemand = (GovernorOnDemand)cpuManager.getGovernor();
		} catch (CPUException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void initializeControls() {
		samplingRateEditText = configureDialogView.findViewById(R.id.ondemand_sampling_rate);
		upThresholdEditText = configureDialogView.findViewById(R.id.ondemand_up_threshold);
		samplingDownFactorEditText = configureDialogView.findViewById(R.id.ondemand_sampling_down_factor);

		minSamplingRateTextView = configureDialogView.findViewById(R.id.ondemand_min_sampling_rate);

		ignoreNiceLoadSwitch = configureDialogView.findViewById(R.id.ondemand_ignore_nice_load);
	}

	@Override
	protected void initializeValues() {
		if (governorOnDemand == null)
			return;

		// Sampling rate setting.
		try {
			long samplingRate = governorOnDemand.getSamplingRate();
			samplingRateEditText.setText(String.valueOf(samplingRate));
		} catch (CPUException e) {
			e.printStackTrace();
		}

		// Up threshold setting.
		try {
			int upThreshold = governorOnDemand.getUpThreshold();
			upThresholdEditText.setText(String.valueOf(upThreshold));
		} catch (CPUException e) {
			e.printStackTrace();
		}

		// Sampling down factor setting.
		try {
			int samplingDownFactor = governorOnDemand.getSamplingDownFactor();
			samplingDownFactorEditText.setText(String.valueOf(samplingDownFactor));
		} catch (CPUException e) {
			e.printStackTrace();
		}

		// Minimum sampling rate setting.
		try {
			long minSamplingRate = governorOnDemand.getMinSamplingRate();
			minSamplingRateTextView.setText(String.valueOf(minSamplingRate));
		} catch (CPUException e) {
			e.printStackTrace();
		}

		// Ignore nice load setting.
		try {
			boolean ignoreNiceLoad = governorOnDemand.getIgnoreNiceLoad();
			ignoreNiceLoadSwitch.setChecked(ignoreNiceLoad);
		} catch (CPUException e) {
			e.printStackTrace();
		}

		// Add the text change listeners to the edit text controls.
		samplingRateEditText.addTextChangedListener(getTextWatcher());
		upThresholdEditText.addTextChangedListener(getTextWatcher());
		samplingDownFactorEditText.addTextChangedListener(getTextWatcher());
	}

	@Override
	protected void applyValues() {
		if (governorOnDemand == null)
			return;

		// Sampling rate setting.
		String samplingRateValue = samplingRateEditText.getText().toString();
		long samplingRate;
		try {
			samplingRate = Long.parseLong(samplingRateValue.trim());
			governorOnDemand.setSamplingRate(samplingRate);
		} catch (NumberFormatException | CPUException e) {
			e.printStackTrace();
		}

		// Up threshold setting.
		String upThresholdValue = upThresholdEditText.getText().toString();
		int upThreshold;
		try {
			upThreshold = Integer.parseInt(upThresholdValue.trim());
			governorOnDemand.setUpThreshold(upThreshold);
		} catch (NumberFormatException | CPUException e) {
			e.printStackTrace();
		}

		// Sampling down factor setting.
		String samplingDownFactorValue = samplingDownFactorEditText.getText().toString();
		int samplingDownFactor;
		try {
			samplingDownFactor = Integer.parseInt(samplingDownFactorValue.trim());
			governorOnDemand.setSamplingDownFactor(samplingDownFactor);
		} catch (NumberFormatException | CPUException e) {
			e.printStackTrace();
		}

		// Ignore nice load setting.
		try {
			if (ignoreNiceLoadSwitch.isChecked())
				governorOnDemand.enableIgnoreNiceLoad();
			else
				governorOnDemand.disableIgnoreNiceLoad();
		} catch (CPUException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected String validateSettings() {
		// Sampling rate value.
		String samplingRateValue = samplingRateEditText.getText().toString();
		if (samplingRateValue.trim().length() == 0)
			return ERROR_SAMPLING_RATE_EMPTY;
		try {
			Long longVar = Long.parseLong(samplingRateValue.trim());
			long minSampleRate = governorOnDemand.getMinSamplingRate();
			if (longVar < minSampleRate || longVar > GovernorOnDemand.MAX_SAMPLING_RATE)
				return ERROR_SAMPLING_RATE_INVALID;
		} catch (CPUException | NumberFormatException e) {
			return ERROR_SAMPLING_RATE_INVALID;
		}

		// Up threshold value.
		String upThresholdValue = upThresholdEditText.getText().toString();
		if (upThresholdValue.trim().length() == 0)
			return ERROR_UP_THRESHOLD_EMPTY;
		try {
			int intVar = Integer.parseInt(upThresholdValue.trim());
			if (intVar <= GovernorOnDemand.MIN_UP_THRESHOLD || intVar > 100)
				return ERROR_UP_THRESHOLD_INVALID;
		} catch (NumberFormatException e) {
			return ERROR_UP_THRESHOLD_INVALID;
		}

		// Sampling down factor value.
		String samplingDownFactorValue = samplingDownFactorEditText.getText().toString();
		if (samplingDownFactorValue.trim().length() == 0)
			return ERROR_SAMPLING_DOWN_FACTOR_EMPTY;
		try {
			int intVar = Integer.parseInt(samplingDownFactorValue.trim());
			if (intVar < 1 || intVar > GovernorOnDemand.MAX_SAMPLING_DOWN_FACTOR)
				return ERROR_SAMPLING_DOWN_FACTOR_INVALID;
		} catch (NumberFormatException e) {
			return ERROR_SAMPLING_DOWN_FACTOR_INVALID;
		}

		return null;
	}
}
