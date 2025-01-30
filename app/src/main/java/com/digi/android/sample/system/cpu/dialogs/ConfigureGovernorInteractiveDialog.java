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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import com.digi.android.sample.system.cpu.R;

import com.digi.android.system.cpu.CPUManager;
import com.digi.android.system.cpu.GovernorInteractive;
import com.digi.android.system.cpu.GovernorType;
import com.digi.android.system.cpu.exception.CPUException;

import java.util.ArrayList;
import java.util.Locale;

public class ConfigureGovernorInteractiveDialog extends ConfigureGovernorDialog {

	// Constants.
	private static final String ERROR_MIN_SAMPLE_RATE_EMPTY = "'Sample rate' value cannot be empty.";
	private static final String ERROR_HI_SPEED_FREQ_EMPTY = "'High speed frequency' value cannot be empty.";
	private static final String ERROR_GO_HI_SPEED_LOAD_EMPTY = "'Go high speed load' value cannot be empty.";
	private static final String ERROR_ABOVE_HI_SPEED_DELAY_EMPTY = "'Above high speed delay' value cannot be empty.";
	private static final String ERROR_TIMER_RATE_EMPTY = "'Timer rate' value cannot be empty.";
	private static final String ERROR_TIMER_SLACK_EMPTY = "'Timer slack' value cannot be empty.";
	private static final String ERROR_BOOST_PULSE_DURATION_EMPTY = "'Boost pulse duration' value cannot be empty.";

	private static final String ERROR_MIN_SAMPLE_RATE_INVALID = "Invalid 'Sample rate' value.";
	private static final String ERROR_HI_SPEED_FREQ_INVALID = "Invalid 'High speed frequency' value.";
	private static final String ERROR_GO_HI_SPEED_LOAD_INVALID = "Invalid 'Go high speed load' value.";
	private static final String ERROR_ABOVE_HI_SPEED_DELAY_INVALID = "Invalid 'Above high speed delay' value.";
	private static final String ERROR_TIMER_RATE_INVALID = "Invalid 'Timer rate' value.";
	private static final String ERROR_TIMER_SLACK_INVALID = "Invalid 'Timer slack' value.";
	private static final String ERROR_BOOST_PULSE_DURATION_INVALID = "Invalid 'Boost pulse duration' value.";

	// Variables.
	private EditText minSampleRateEditText;
	private Spinner hiSpeedFreqSpinner;
	private EditText goHiSpeedLoadEditText;
	private EditText aboveHiSpeedDelayEditText;
	private EditText timerRateEditText;
	private EditText timerSlackEditText;
	private EditText boostPulseDurationEditText;

	private Switch boostSwitch;

	private GovernorInteractive governorInteractive;

	public ConfigureGovernorInteractiveDialog(Context context, CPUManager cpuManager) {
		super(context, GovernorType.INTERACTIVE, cpuManager);

		try {
			governorInteractive = (GovernorInteractive)cpuManager.getGovernor();
		} catch (CPUException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void initializeControls() {
		minSampleRateEditText = configureDialogView.findViewById(R.id.interactive_min_sample_time);
		hiSpeedFreqSpinner = configureDialogView.findViewById(R.id.interactive_hi_speed_freq);
		goHiSpeedLoadEditText = configureDialogView.findViewById(R.id.interactive_go_hi_speed_load);
		aboveHiSpeedDelayEditText = configureDialogView.findViewById(R.id.interactive_above_hi_speed_delay);
		timerRateEditText = configureDialogView.findViewById(R.id.interactive_timer_rate);
		timerSlackEditText = configureDialogView.findViewById(R.id.interactive_timer_slack);
		boostPulseDurationEditText = configureDialogView.findViewById(R.id.interactive_boost_pulse_duration);

		boostSwitch = configureDialogView.findViewById(R.id.interactive_boost);
	}

	@Override
	protected void initializeValues(Context context) {
		if (governorInteractive == null)
			return;

		// Minimum sample rate setting.
		try {
			long minSampleRate = governorInteractive.getMinSampleTime();
			minSampleRateEditText.setText(String.valueOf(minSampleRate));
		} catch (CPUException e) {
			e.printStackTrace();
		}

		// High speed frequency setting.
		ArrayAdapter<Integer> frequenciesAdapter;
		try {
			// Get the available frequencies and fill the frequencies list.
			ArrayList<Integer> frequencies = cpuManager.getAvailableFrequencies();
			frequenciesAdapter = new ArrayAdapter<>(context, R.layout.spinner_item, frequencies);
			frequenciesAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
			hiSpeedFreqSpinner.setAdapter(frequenciesAdapter);

			// Configure the selected High frequency.
			Integer hiSpeedFreq = governorInteractive.getHiSpeedFreq();
			if (frequenciesAdapter.getPosition(hiSpeedFreq) != -1)
				hiSpeedFreqSpinner.setSelection(frequenciesAdapter.getPosition(hiSpeedFreq));
		} catch (CPUException e) {
			e.printStackTrace();
		}

		// Go high speed load setting.
		try {
			int goHiSpeedLoad = governorInteractive.getGoHiSpeedLoad();
			goHiSpeedLoadEditText.setText(String.valueOf(goHiSpeedLoad));
		} catch (CPUException e) {
			e.printStackTrace();
		}

		// Above high speed delay setting.
		try {
			long aboveHiSpeedDelay = governorInteractive.getAboveHiSpeedDelay();
			aboveHiSpeedDelayEditText.setText(String.valueOf(aboveHiSpeedDelay));
		} catch (CPUException e) {
			e.printStackTrace();
		}

		// Timer rate setting.
		try {
			long timerRate = governorInteractive.getTimerRate();
			timerRateEditText.setText(String.valueOf(timerRate));
		} catch (CPUException e) {
			e.printStackTrace();
		}

		// Timer slack setting.
		try {
			long timerSlack = governorInteractive.getTimerSlack();
			timerSlackEditText.setText(String.valueOf(timerSlack));
		} catch (CPUException e) {
			e.printStackTrace();
		}

		// Boost pulse duration setting.
		try {
			long boostPulseDuration = governorInteractive.getBoostPulseDuration();
			boostPulseDurationEditText.setText(String.valueOf(boostPulseDuration));
		} catch (CPUException e) {
			e.printStackTrace();
		}

		// Boost setting.
		try {
			boolean boost = governorInteractive.getBoost();

			boostSwitch.setChecked(boost);
		} catch (CPUException e) {
			e.printStackTrace();
		}

		// Add the text change listeners to the edit text controls.
		minSampleRateEditText.addTextChangedListener(getTextWatcher());
		hiSpeedFreqSpinner.setOnItemSelectedListener(getSelectedItemListener());
		goHiSpeedLoadEditText.addTextChangedListener(getTextWatcher());
		aboveHiSpeedDelayEditText.addTextChangedListener(getTextWatcher());
		timerRateEditText.addTextChangedListener(getTextWatcher());
		timerSlackEditText.addTextChangedListener(getTextWatcher());
		boostPulseDurationEditText.addTextChangedListener(getTextWatcher());
	}

	@Override
	protected void applyValues() {
		if (governorInteractive == null)
			return;

		// Minimum sample rate setting.
		String minSampleRateValue = minSampleRateEditText.getText().toString();
		long minSampleRate;
		try {
			minSampleRate = Long.parseLong(minSampleRateValue.trim());
			governorInteractive.setMinSampleTime(minSampleRate);
		} catch (NumberFormatException | CPUException e) {
			e.printStackTrace();
		}

		// High speed frequency setting.
		String hiSpeedFreqValue = hiSpeedFreqSpinner.getSelectedItem().toString();
		int hiSpeedFreq;
		try {
			hiSpeedFreq = Integer.parseInt(hiSpeedFreqValue.trim());
			governorInteractive.setHiSpeedFreq(hiSpeedFreq);
		} catch (NumberFormatException | CPUException e) {
			e.printStackTrace();
		}

		// Go high speed load setting.
		String goHiSpeedLoadValue = goHiSpeedLoadEditText.getText().toString();
		int goHiSpeedLoad;
		try {
			goHiSpeedLoad = Integer.parseInt(goHiSpeedLoadValue.trim());
			governorInteractive.setGoHiSpeedLoad(goHiSpeedLoad);
		} catch (NumberFormatException | CPUException e) {
			e.printStackTrace();
		}

		// Above high speed delay setting.
		String aboveHiSpeedDelayValue = aboveHiSpeedDelayEditText.getText().toString();
		long aboveHiSpeedDelay;
		try {
			aboveHiSpeedDelay = Long.parseLong(aboveHiSpeedDelayValue.trim());
			governorInteractive.setAboveHiSpeedDelay(aboveHiSpeedDelay);
		} catch (NumberFormatException | CPUException e) {
			e.printStackTrace();
		}

		// Timer rate setting.
		String timerRateValue = timerRateEditText.getText().toString();
		long timerRate;
		try {
			timerRate = Long.parseLong(timerRateValue.trim());
			governorInteractive.setTimerRate(timerRate);
		} catch (NumberFormatException | CPUException e) {
			e.printStackTrace();
		}

		// Timer slack setting.
		String timerSlackValue = timerSlackEditText.getText().toString();
		long timerSlack;
		try {
			timerSlack = Long.parseLong(timerSlackValue.trim());
			governorInteractive.setTimerSlack(timerSlack);
		} catch (NumberFormatException | CPUException e) {
			e.printStackTrace();
		}

		// Boost pulse duration setting.
		String boostPulseDurationValue = boostPulseDurationEditText.getText().toString();
		long boostPulseDuration;
		try {
			boostPulseDuration = Long.parseLong(boostPulseDurationValue.trim());
			governorInteractive.setBoostPulseDuration(boostPulseDuration);
		} catch (NumberFormatException | CPUException e) {
			e.printStackTrace();
		}

		// Boost setting.
		try {
			if (boostSwitch.isChecked())
				governorInteractive.enableBoost();
			else
				governorInteractive.disableBoost();
		} catch (CPUException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected String validateSettings() {
		// Minimum sample rate value.
		String minSampleRateValue = minSampleRateEditText.getText().toString();
		if (minSampleRateValue.trim().isEmpty())
			return ERROR_MIN_SAMPLE_RATE_EMPTY;
		try {
			long longVar = Long.parseLong(minSampleRateValue.trim());
			if (longVar < 0 || longVar > GovernorInteractive.MAX_MIN_SAMPLE_TIME)
				return ERROR_MIN_SAMPLE_RATE_INVALID + " "
						+ String.format(Locale.getDefault(), ERROR_LIMITS, 0, GovernorInteractive.MAX_MIN_SAMPLE_TIME);
		} catch (NumberFormatException e) {
			return ERROR_MIN_SAMPLE_RATE_INVALID;
		}

		// High speed frequency value.
		String hiSpeedFreqValue = hiSpeedFreqSpinner.getSelectedItem().toString();
		if (hiSpeedFreqValue.trim().isEmpty())
			return ERROR_HI_SPEED_FREQ_EMPTY;
		try {
			int intVar = Integer.parseInt(hiSpeedFreqValue.trim());
			if (!cpuManager.getAvailableFrequencies().contains(intVar))
				return ERROR_HI_SPEED_FREQ_INVALID + " Value must be an available frequency";
		} catch (CPUException | NumberFormatException e) {
			return ERROR_HI_SPEED_FREQ_INVALID;
		}

		// Go high speed load value.
		String goHiSpeedLoadValue = goHiSpeedLoadEditText.getText().toString();
		if (goHiSpeedLoadValue.trim().isEmpty())
			return ERROR_GO_HI_SPEED_LOAD_EMPTY;
		try {
			int intVar = Integer.parseInt(goHiSpeedLoadValue.trim());
			if (intVar < 0 || intVar > 100)
				return ERROR_GO_HI_SPEED_LOAD_INVALID + " "
						+ String.format(Locale.getDefault(), ERROR_LIMITS, 0, 100);
		} catch (NumberFormatException e) {
			return ERROR_GO_HI_SPEED_LOAD_INVALID;
		}

		// Above high speed delay value.
		String aboveHiSpeedDelayValue = aboveHiSpeedDelayEditText.getText().toString();
		if (aboveHiSpeedDelayValue.trim().isEmpty())
			return ERROR_ABOVE_HI_SPEED_DELAY_EMPTY;
		try {
			long longVar = Long.parseLong(aboveHiSpeedDelayValue.trim());
			if (longVar < 0 || longVar > GovernorInteractive.MAX_ABOVE_HIGH_SPEED_DELAY)
				return ERROR_ABOVE_HI_SPEED_DELAY_INVALID + " "
						+ String.format(Locale.getDefault(), ERROR_LIMITS, 0, GovernorInteractive.MAX_ABOVE_HIGH_SPEED_DELAY);
		} catch (NumberFormatException e) {
			return ERROR_ABOVE_HI_SPEED_DELAY_INVALID;
		}

		// Timer rate value.
		String timerRateValue = timerRateEditText.getText().toString();
		if (timerRateValue.trim().isEmpty())
			return ERROR_TIMER_RATE_EMPTY;
		try {
			long longVar = Long.parseLong(timerRateValue.trim());
			if (longVar < 0 || longVar > GovernorInteractive.MAX_TIMER_RATE)
				return ERROR_TIMER_RATE_INVALID + " "
						+ String.format(Locale.getDefault(), ERROR_LIMITS, 0, GovernorInteractive.MAX_TIMER_RATE);
		} catch (NumberFormatException e) {
			return ERROR_TIMER_RATE_INVALID;
		}

		// Timer slack value.
		String timerSlackValue = timerSlackEditText.getText().toString();
		if (timerSlackValue.trim().isEmpty())
			return ERROR_TIMER_SLACK_EMPTY;
		try {
			long longVar = Long.parseLong(timerSlackValue.trim());
			if (longVar < -1 || longVar > GovernorInteractive.MAX_TIMER_SLACK)
				return ERROR_TIMER_SLACK_INVALID + " "
						+ String.format(Locale.getDefault(), ERROR_LIMITS, -1, GovernorInteractive.MAX_TIMER_SLACK);
		} catch (NumberFormatException e) {
			return ERROR_TIMER_SLACK_INVALID;
		}

		// Boost pulse duration value.
		String boostPulseDurationValue = boostPulseDurationEditText.getText().toString();
		if (boostPulseDurationValue.trim().isEmpty())
			return ERROR_BOOST_PULSE_DURATION_EMPTY;
		try {
			long longVar = Long.parseLong(boostPulseDurationValue.trim());
			if (longVar < 0 || longVar > GovernorInteractive.MAX_BOOSTPULSE_DURATION)
				return ERROR_BOOST_PULSE_DURATION_INVALID + " "
						+ String.format(Locale.getDefault(), ERROR_LIMITS, 0, GovernorInteractive.MAX_BOOSTPULSE_DURATION);
		} catch (NumberFormatException e) {
			return ERROR_BOOST_PULSE_DURATION_INVALID;
		}

		return null;
	}
}
