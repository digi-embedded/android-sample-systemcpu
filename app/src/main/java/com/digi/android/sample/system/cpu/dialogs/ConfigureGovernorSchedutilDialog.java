/*
 * Copyright (c) 2019-2021, Digi International Inc. <support@digi.com>
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

import com.digi.android.sample.system.cpu.R;

import com.digi.android.system.cpu.CPUManager;
import com.digi.android.system.cpu.GovernorSchedUtil;
import com.digi.android.system.cpu.GovernorType;
import com.digi.android.system.cpu.exception.CPUException;

import java.util.Locale;

public class ConfigureGovernorSchedutilDialog extends ConfigureGovernorDialog {

    // Constants.
    private static final String ERROR_RATE_LIMIT_EMPTY = "'%s rate limit' value cannot be empty.";

    private static final String ERROR_RATE_LIMIT_INVALID = "Invalid '%s rate limit' value.";
    private static final String ERROR_RATE_LIMIT_LIMITS = "Value must be between %d and %d.";

    // Variables.
    private EditText downRateLimitText;
    private EditText upRateLimitText;

    private GovernorSchedUtil governorSchedutil;

    public ConfigureGovernorSchedutilDialog(Context context, CPUManager cpuManager) {
        super(context, GovernorType.SCHEDUTIL, cpuManager);

        try {
            governorSchedutil = (GovernorSchedUtil)cpuManager.getGovernor();
        } catch (CPUException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initializeControls() {
        downRateLimitText = configureDialogView.findViewById(R.id.schedutil_down_rate_limit);
        upRateLimitText = configureDialogView.findViewById(R.id.schedutil_up_rate_limit);
    }

    @Override
    protected void initializeValues(Context context) {
        if (governorSchedutil == null)
            return;

        // Down rate limit setting.
        try {
            long downRateLimit = governorSchedutil.getDownRateLimit();
            downRateLimitText.setText(String.valueOf(downRateLimit));
        } catch (CPUException e) {
            e.printStackTrace();
        }

        // Up rate limit setting.
        try {
            long upRateLimit = governorSchedutil.getUpRateLimit();
            upRateLimitText.setText(String.valueOf(upRateLimit));
        } catch (CPUException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void applyValues() {
        if (governorSchedutil == null)
            return;

        // Down rate limit.
        String downRateLimitValue = downRateLimitText.getText().toString();
        long downRateLimit;
        try {
            downRateLimit = Long.parseLong(downRateLimitValue.trim());
            governorSchedutil.setDownRateLimit(downRateLimit);
        } catch (NumberFormatException | CPUException e) {
            e.printStackTrace();
        }

        // Up rate limit.
        String upRateLimitValue = upRateLimitText.getText().toString();
        long upRateLimit;
        try {
            upRateLimit = Long.parseLong(upRateLimitValue.trim());
            governorSchedutil.setUpRateLimit(upRateLimit);
        } catch (NumberFormatException | CPUException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String validateSettings() {
        // Down rate limit.
        String downRateLimitValue = downRateLimitText.getText().toString();
        if (downRateLimitValue.trim().length() == 0)
            return String.format(ERROR_RATE_LIMIT_EMPTY, "Down");
        try {
            long longVar = Long.parseLong(downRateLimitValue.trim());
            if (longVar < GovernorSchedUtil.MIN_RATE_LIMIT || longVar > GovernorSchedUtil.MAX_RATE_LIMIT)
                return String.format(ERROR_RATE_LIMIT_INVALID, "Down") + " "
                        + String.format(Locale.getDefault(), ERROR_RATE_LIMIT_LIMITS, GovernorSchedUtil.MIN_RATE_LIMIT, GovernorSchedUtil.MAX_RATE_LIMIT);
        } catch (NumberFormatException e) {
            return String.format(ERROR_RATE_LIMIT_INVALID, "Down");
        }

        // Up rate limit.
        String upRateLimitValue = upRateLimitText.getText().toString();
        if (upRateLimitValue.trim().length() == 0)
            return String.format(ERROR_RATE_LIMIT_EMPTY, "Up");
        try {
            long longVar = Long.parseLong(upRateLimitValue.trim());
            if (longVar < GovernorSchedUtil.MIN_RATE_LIMIT || longVar > GovernorSchedUtil.MAX_RATE_LIMIT)
                return String.format(ERROR_RATE_LIMIT_INVALID, "Up") + " "
                        + String.format(Locale.getDefault(), ERROR_RATE_LIMIT_LIMITS, GovernorSchedUtil.MIN_RATE_LIMIT, GovernorSchedUtil.MAX_RATE_LIMIT);
        } catch (NumberFormatException e) {
            return String.format(ERROR_RATE_LIMIT_INVALID, "Up");
        }

        return null;
    }
}

