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
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.digi.android.sample.system.cpu.R;

import com.digi.android.system.cpu.CPUManager;
import com.digi.android.system.cpu.GovernorType;
import com.digi.android.system.cpu.exception.CPUException;
import com.digi.android.system.cpu.exception.UnsupportedCommandException;

import java.util.ArrayList;

public class ConfigureGovernorUserspaceDialog extends ConfigureGovernorDialog {

	// Constants.
	private static final String ERROR_CUSTOM_FREQ_EMPTY = "'Custom frequency' value cannot be empty.";

	private static final String ERROR_CUSTOM_FREQ_INVALID = "Invalid 'Custom frequency' value.";

	// Variables.
	private Spinner customFreqSpinner;

	public ConfigureGovernorUserspaceDialog(Context context, CPUManager cpuManager) {
		super(context, GovernorType.USERSPACE, cpuManager);
	}

	@Override
	protected void initializeControls() {
		customFreqSpinner = configureDialogView.findViewById(R.id.userspace_custom_freq);
	}

	@Override
	protected void initializeValues(Context context) {
		// Get the available frequencies and fill the frequencies list.
		ArrayAdapter<Integer> frequenciesAdapter;
		try {
			ArrayList<Integer> frequencies = cpuManager.getAvailableFrequencies();
			frequenciesAdapter = new ArrayAdapter<>(context, R.layout.spinner_item, frequencies);
			frequenciesAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
			customFreqSpinner.setAdapter(frequenciesAdapter);

			// Configure the selected custom frequency.
			Integer customFreq = cpuManager.getFrequency();
			if (frequenciesAdapter.getPosition(customFreq) != -1)
				customFreqSpinner.setSelection(frequenciesAdapter.getPosition(customFreq));
		} catch (CPUException e) {
			e.printStackTrace();
		}

		// Add the selection listeners to the spinner control.
		customFreqSpinner.setOnItemSelectedListener(getSelectedItemListener());
	}

	@Override
	protected void applyValues() {
		// Custom frequency setting.
		String customFreqValue = customFreqSpinner.getSelectedItem().toString();
		int customFreq;
		try {
			customFreq = Integer.parseInt(customFreqValue.trim());
			cpuManager.setFrequency(customFreq);
		} catch (NumberFormatException | CPUException | UnsupportedCommandException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected String validateSettings() {
		// Custom frequency value.
		String customFreqValue = customFreqSpinner.getSelectedItem().toString();
		if (customFreqValue.trim().length() == 0)
			return ERROR_CUSTOM_FREQ_EMPTY;
		try {
			int intVar = Integer.parseInt(customFreqValue.trim());
			if (!cpuManager.getAvailableFrequencies().contains(intVar))
				return ERROR_CUSTOM_FREQ_INVALID;
		} catch (CPUException | NumberFormatException e) {
			return ERROR_CUSTOM_FREQ_INVALID;
		}

		return null;
	}
}
