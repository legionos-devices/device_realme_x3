/*
 * Copyright (C) 2016 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.realmeparts;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.MenuItem;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import androidx.preference.TwoStatePreference;

import java.io.IOException;
import java.text.DecimalFormat;

public class DeviceSettings extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    public static final String KEY_OTG_SWITCH = "otg";
    public static final String KEY_GAME_SWITCH = "game";
    public static final String KEY_DND_SWITCH = "dnd";
    public static final String KEY_CABC = "cabc";
    public static final String KEY_FPS_INFO = "fps_info";
    public static final String KEY_VIBRATION_STRENGTH = "vibration_strength";
    public static final String CABC_SYSTEM_PROPERTY = "persist.cabc_profile";
    public static final String VIB_STRENGTH_SYSTEM_PROPERTY = "persist.vib_strength";
    public static final String TP_DIRECTION = "/proc/touchpanel/oppo_tp_direction";
    private static NotificationManager mNotificationManager;
    private Vibrator mVibrator;
    public TwoStatePreference mDNDSwitch;
    public PreferenceCategory mPreferenceCategory;
    private TwoStatePreference mOTGModeSwitch;
    private TwoStatePreference mGameModeSwitch;
    private SwitchPreference mFpsInfo;
    private SecureSettingListPreference mCABC;
    private SecureSettingListPreference mVibStrength;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());

        addPreferencesFromResource(R.xml.main);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

        mOTGModeSwitch = (TwoStatePreference) findPreference(KEY_OTG_SWITCH);
        mOTGModeSwitch.setEnabled(OTGModeSwitch.isSupported());
        mOTGModeSwitch.setChecked(OTGModeSwitch.isCurrentlyEnabled(this.getContext()));
        mOTGModeSwitch.setOnPreferenceChangeListener(new OTGModeSwitch());

        mGameModeSwitch = findPreference(KEY_GAME_SWITCH);
        mGameModeSwitch.setEnabled(GameModeSwitch.isSupported());
        mGameModeSwitch.setChecked(GameModeSwitch.isCurrentlyEnabled(this.getContext()));
        mGameModeSwitch.setOnPreferenceChangeListener(new GameModeSwitch(getContext()));

        mDNDSwitch = findPreference(KEY_DND_SWITCH);
        mDNDSwitch.setChecked(prefs.getBoolean(KEY_DND_SWITCH, false));
        mDNDSwitch.setOnPreferenceChangeListener(this);

        mFpsInfo = findPreference(KEY_FPS_INFO);
        mFpsInfo.setChecked(Utils.isFpsInfoShowing(getActivity().getApplicationContext()));
        mFpsInfo.setOnPreferenceChangeListener(this);

        mCABC = (SecureSettingListPreference) findPreference(KEY_CABC);
        mCABC.setValue(Utils.getStringProp(CABC_SYSTEM_PROPERTY, "0"));
        mCABC.setSummary(mCABC.getEntry());
        mCABC.setOnPreferenceChangeListener(this);

        mVibStrength = (SecureSettingListPreference) findPreference(KEY_VIBRATION_STRENGTH);
        mVibStrength.setValue(Utils.getStringProp(VIB_STRENGTH_SYSTEM_PROPERTY, "2500"));
        mVibStrength.setSummary(mVibStrength.getEntry());
        mVibStrength.setOnPreferenceChangeListener(this);

        mVibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mFpsInfo) {
            boolean enabled = (Boolean) newValue;
            if (enabled) {
                Utils.startService(getActivity().getApplicationContext(), FPSInfoService.class);
            } else {
                Utils.stopService(getActivity().getApplicationContext(), FPSInfoService.class);
            }
        }

        if (preference == mCABC) {
            mCABC.setValue((String) newValue);
            mCABC.setSummary(mCABC.getEntry());
            Utils.setStringProp(CABC_SYSTEM_PROPERTY, (String) newValue);
        }

        if (preference == mVibStrength) {
            mVibStrength.setValue((String) newValue);
            mVibStrength.setSummary(mVibStrength.getEntry());
            Utils.setStringProp(VIB_STRENGTH_SYSTEM_PROPERTY, (String) newValue);
            mVibrator.vibrate(VibrationEffect.createOneShot(85, VibrationEffect.DEFAULT_AMPLITUDE));
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
