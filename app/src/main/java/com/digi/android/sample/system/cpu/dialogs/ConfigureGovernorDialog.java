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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.digi.android.system.cpu.CPUManager;
import com.digi.android.system.cpu.GovernorType;
import com.digi.android.sample.system.cpu.R;

public abstract class ConfigureGovernorDialog {

	// Variables.
	private Context context;

	private TextView statusText;

	protected View configureDialogView;

	private AlertDialog configureDialog;

	private GovernorType governorType;

	protected CPUManager cpuManager;

	public ConfigureGovernorDialog(Context context, GovernorType governorType, CPUManager cpuManager) {
		this.context = context;
		this.governorType = governorType;
		this.cpuManager = cpuManager;

		// Setup the layout.
		setupLayout();
	}

	/**
	 * Displays the governor configuration dialog.
	 */
	public void show() {
		// Reset the value.
		createDialog();
		initializeValues();

		configureDialog.show();

		validateDialog();

		configureDialog.getButton(AlertDialog.BUTTON_POSITIVE).setFocusable(true);
		configureDialog.getButton(AlertDialog.BUTTON_POSITIVE).requestFocus();
	}

	/**
	 * Configures the layout of the governor configuration dialog.
	 */
	protected void setupLayout() {
		// Create the layout.
		LayoutInflater layoutInflater = LayoutInflater.from(context);
		switch (governorType) {
			case INTERACTIVE:
				configureDialogView = layoutInflater.inflate(R.layout.configure_governor_interactive_dialog, null);
				break;
			case ONDEMAND:
				configureDialogView = layoutInflater.inflate(R.layout.configure_governor_ondemand_dialog, null);
				break;
			case CONSERVATIVE:
				configureDialogView = layoutInflater.inflate(R.layout.configure_governor_conservative_dialog, null);
				break;
			case USERSPACE:
			default:
				configureDialogView = layoutInflater.inflate(R.layout.configure_governor_userspace_dialog, null);
				break;
		}

		// Get the status text.
		statusText = (TextView) configureDialogView.findViewById(R.id.status_text);

		// Initialize rest of controls.
		initializeControls();
	}

	/**
	 * Creates the alert dialog that will be displayed.
	 */
	private void createDialog() {
		// Setup the dialog window.
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		alertDialogBuilder.setView(configureDialogView);
		switch (governorType) {
			case INTERACTIVE:
				alertDialogBuilder.setTitle(R.string.title_governor_interactive);
				break;
			case ONDEMAND:
				alertDialogBuilder.setTitle(R.string.title_governor_ondemand);
				break;
			case CONSERVATIVE:
				alertDialogBuilder.setTitle(R.string.title_governor_conservative);
				break;
			case USERSPACE:
			default:
				alertDialogBuilder.setTitle(R.string.title_governor_userspace);
				break;
		}
		alertDialogBuilder.setCancelable(false);
		alertDialogBuilder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				applyValues();
				synchronized (ConfigureGovernorDialog.this) {
					ConfigureGovernorDialog.this.notify();
				}
			}
		});
		alertDialogBuilder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
				synchronized (ConfigureGovernorDialog.this) {
					ConfigureGovernorDialog.this.notify();
				}
			}
		});
		// Create the dialog.
		configureDialog = alertDialogBuilder.create();
	}

	/**
	 * Validates the dialog setting the corresponding configuration error.
	 */
	protected void validateDialog() {
		String errorMessage = validateSettings();
		if (errorMessage != null) {
			statusText.setError(errorMessage);
			statusText.setText(errorMessage);
			statusText.setTextColor(context.getResources().getColor(R.color.red));
		} else {
			statusText.setError(null);
			statusText.setText(R.string.description_configure_governor);
			statusText.setTextColor(context.getResources().getColor(R.color.black));
		}

		configureDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(errorMessage == null);
	}

	protected TextWatcher textWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

		@Override
		public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

		@Override
		public void afterTextChanged(Editable editable) {
			validateDialog();
		}
	};

	/**
	 * Initializes the specific settings controls of the corresponding governor type.
	 */
	protected abstract void initializeControls();

	/**
	 * Initializes (reads and sets) the configuration values of the governor in the corresponding
	 * settings fields.
	 */
	protected abstract void initializeValues();

	/**
	 * Applies (writes) the configured values of the governor from the configuration fields.
	 */
	protected abstract void applyValues();

	/**
	 * Validates the configuration settings of the governor.
	 *
	 * @return The error message of the configuration settings, {@code null} if the settings are
	 *         configured correctly.
	 */
	protected abstract String validateSettings();
}
