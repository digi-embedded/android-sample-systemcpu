/**
 * Copyright (c) 2016, Digi International Inc. <support@digi.com>
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

import com.digi.android.system.cpu.CPUManager;
import com.digi.android.system.cpu.GovernorType;
import com.digi.android.system.cpu.exception.CPUException;
import com.digi.android.system.cpu.exception.UnsupportedCommandException;
import com.digi.android.sample.system.cpu.R;

public class ConfigureGovernorUserspaceDialog extends ConfigureGovernorDialog {

	// Constants.
	private static final String ERROR_CUSTOM_FREQ_EMPTY = "'Custom frequency' value cannot be empty.";

	private static final String ERROR_CUSTOM_FREQ_INVALID = "Invalid 'Custom frequency' value.";

	// Variables.
	private EditText customFreqEditText;

	public ConfigureGovernorUserspaceDialog(Context context, CPUManager cpuManager) {
		super(context, GovernorType.USERSPACE, cpuManager);
	}

	@Override
	protected void initializeControls() {
		customFreqEditText = (EditText)configureDialogView.findViewById(R.id.userspace_custom_freq);
	}

	@Override
	protected void initializeValues() {
		// Custom frequency setting.
		try {
			int customFreq = cpuManager.getFrequency();
			customFreqEditText.setText(String.valueOf(customFreq));
		} catch (CPUException e) {
			e.printStackTrace();
		}

		// Add the text change listeners to the edit text controls.
		customFreqEditText.addTextChangedListener(textWatcher);
	}

	@Override
	protected void applyValues() {
		// Custom frequency setting.
		String customFreqValue = customFreqEditText.getText().toString();
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
		String customFreqValue = customFreqEditText.getText().toString();
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
