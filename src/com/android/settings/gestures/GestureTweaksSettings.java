/*
 * Copyright (C) 2020 ABC ROM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.gestures;

import android.content.ContentResolver;
import android.content.Context;
import android.content.om.IOverlayManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.ServiceManager;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.SettingsPreferenceFragment;

import com.syberia.settings.preference.CustomSeekBarPreference;
import com.syberia.settings.preference.SystemSettingListPreference;
import com.syberia.settings.preference.SystemSettingSwitchPreference;

import java.util.ArrayList;
import java.util.List;

public class GestureTweaksSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener, Indexable {

    private static final String KEY_GESTURE_BAR_SIZE = "navigation_handle_width";

    private static final String KEY_LONG_BACK_SWIPE_TIMEOUT = "long_back_swipe_timeout";
    private static final String KEY_BACK_SWIPE_EXTENDED = "back_swipe_type";
    private static final String KEY_LEFT_SWIPE_ACTIONS = "left_swipe_actions";
    private static final String KEY_RIGHT_SWIPE_ACTIONS = "right_swipe_actions";
    private static final String KEY_LEFT_SWIPE_APP_ACTION = "left_swipe_app_action";
    private static final String KEY_RIGHT_SWIPE_APP_ACTION = "right_swipe_app_action";
    private static final String KEY_LEFT_VERTICAL_SWIPE_ACTIONS = "left_vertical_swipe_actions";
    private static final String KEY_RIGHT_VERTICAL_SWIPE_ACTIONS = "right_vertical_swipe_actions";
    private static final String KEY_LEFT_VERTICAL_SWIPE_APP_ACTION = "left_vertical_swipe_app_action";
    private static final String KEY_RIGHT_VERTICAL_SWIPE_APP_ACTION = "right_vertical_swipe_app_action";

    private int leftSwipeActions;
    private int rightSwipeActions;

    private ListPreference mLeftSwipeActions;
    private ListPreference mRightSwipeActions;
    private ListPreference mLeftVerticalSwipeActions;
    private ListPreference mRightVerticalSwipeActions;
    private Preference mLeftSwipeAppSelection;
    private Preference mRightSwipeAppSelection;
    private Preference mLeftVerticalSwipeAppSelection;
    private Preference mRightVerticalSwipeAppSelection;
    private SystemSettingListPreference mTimeout;
    private SystemSettingSwitchPreference mExtendedSwipe;
    private ListPreference mGestureBarSize;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.gesture_nav_tweaks);

        final ContentResolver resolver = getActivity().getContentResolver();

        leftSwipeActions = Settings.System.getIntForUser(resolver,
                Settings.System.LEFT_LONG_BACK_SWIPE_ACTION, 0,
                UserHandle.USER_CURRENT);
        mLeftSwipeActions = (ListPreference) findPreference(KEY_LEFT_SWIPE_ACTIONS);
        mLeftSwipeActions.setValue(Integer.toString(leftSwipeActions));
        mLeftSwipeActions.setSummary(mLeftSwipeActions.getEntry());
        mLeftSwipeActions.setOnPreferenceChangeListener(this);

        rightSwipeActions = Settings.System.getIntForUser(resolver,
                Settings.System.RIGHT_LONG_BACK_SWIPE_ACTION, 0,
                UserHandle.USER_CURRENT);
        mRightSwipeActions = (ListPreference) findPreference(KEY_RIGHT_SWIPE_ACTIONS);
        mRightSwipeActions.setValue(Integer.toString(rightSwipeActions));
        mRightSwipeActions.setSummary(mRightSwipeActions.getEntry());
        mRightSwipeActions.setOnPreferenceChangeListener(this);

        mLeftSwipeAppSelection = (Preference) findPreference(KEY_LEFT_SWIPE_APP_ACTION);
        boolean isAppSelection = Settings.System.getIntForUser(resolver,
                Settings.System.LEFT_LONG_BACK_SWIPE_ACTION, 0, UserHandle.USER_CURRENT) == 5/*action_app_action*/;
        mLeftSwipeAppSelection.setEnabled(isAppSelection);

        mRightSwipeAppSelection = (Preference) findPreference(KEY_RIGHT_SWIPE_APP_ACTION);
        isAppSelection = Settings.System.getIntForUser(resolver,
                Settings.System.RIGHT_LONG_BACK_SWIPE_ACTION, 0, UserHandle.USER_CURRENT) == 5/*action_app_action*/;
        mRightSwipeAppSelection.setEnabled(isAppSelection);

        mLeftVerticalSwipeAppSelection = (Preference) findPreference(KEY_LEFT_VERTICAL_SWIPE_APP_ACTION);
        isAppSelection = Settings.System.getIntForUser(resolver,
                Settings.System.LEFT_VERTICAL_BACK_SWIPE_ACTION, 0, UserHandle.USER_CURRENT) == 5/*action_app_action*/;
        mLeftVerticalSwipeAppSelection.setEnabled(isAppSelection);

        mRightVerticalSwipeAppSelection = (Preference) findPreference(KEY_RIGHT_VERTICAL_SWIPE_APP_ACTION);
        isAppSelection = Settings.System.getIntForUser(resolver,
                Settings.System.RIGHT_VERTICAL_BACK_SWIPE_ACTION, 0, UserHandle.USER_CURRENT) == 5/*action_app_action*/;
        mRightVerticalSwipeAppSelection.setEnabled(isAppSelection);

        customAppCheck();

        int leftVerticalSwipeActions = Settings.System.getIntForUser(resolver,
                Settings.System.LEFT_VERTICAL_BACK_SWIPE_ACTION, 0,
                UserHandle.USER_CURRENT);
        mLeftVerticalSwipeActions = (ListPreference) findPreference(KEY_LEFT_VERTICAL_SWIPE_ACTIONS);
        mLeftVerticalSwipeActions.setValue(Integer.toString(leftVerticalSwipeActions));
        mLeftVerticalSwipeActions.setSummary(mLeftVerticalSwipeActions.getEntry());
        mLeftVerticalSwipeActions.setOnPreferenceChangeListener(this);

        int rightVerticalSwipeActions = Settings.System.getIntForUser(resolver,
                Settings.System.RIGHT_VERTICAL_BACK_SWIPE_ACTION, 0,
                UserHandle.USER_CURRENT);
        mRightVerticalSwipeActions = (ListPreference) findPreference(KEY_RIGHT_VERTICAL_SWIPE_ACTIONS);
        mRightVerticalSwipeActions.setValue(Integer.toString(rightVerticalSwipeActions));
        mRightVerticalSwipeActions.setSummary(mRightVerticalSwipeActions.getEntry());
        mRightVerticalSwipeActions.setOnPreferenceChangeListener(this);

        mTimeout = (SystemSettingListPreference) findPreference(KEY_LONG_BACK_SWIPE_TIMEOUT);

        mExtendedSwipe = (SystemSettingSwitchPreference) findPreference(KEY_BACK_SWIPE_EXTENDED);
        boolean extendedSwipe = Settings.System.getIntForUser(resolver,
            Settings.System.BACK_SWIPE_TYPE, 0,
            UserHandle.USER_CURRENT) != 0;
        mExtendedSwipe.setChecked(extendedSwipe);
        mExtendedSwipe.setOnPreferenceChangeListener(this);
        mTimeout.setEnabled(!mExtendedSwipe.isChecked());

        mGestureBarSize = (ListPreference) findPreference(KEY_GESTURE_BAR_SIZE);
        int gesturebarsize = Settings.System.getIntForUser(resolver,
                Settings.System.NAVIGATION_HANDLE_WIDTH, 1, UserHandle.USER_CURRENT);
        mGestureBarSize.setValue(String.valueOf(gesturebarsize));
        mGestureBarSize.setSummary(mGestureBarSize.getEntry());
        mGestureBarSize.setOnPreferenceChangeListener(this);
    }

    private IOverlayManager getOverlayManager() {
        return IOverlayManager.Stub.asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE));
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mLeftSwipeActions) {
            int leftSwipeActions = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.LEFT_LONG_BACK_SWIPE_ACTION, leftSwipeActions,
                    UserHandle.USER_CURRENT);
            int index = mLeftSwipeActions.findIndexOfValue((String) newValue);
            mLeftSwipeActions.setSummary(
                    mLeftSwipeActions.getEntries()[index]);
            mLeftSwipeAppSelection.setEnabled(leftSwipeActions == 5);
            customAppCheck();
            return true;
        } else if (preference == mRightSwipeActions) {
            int rightSwipeActions = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.RIGHT_LONG_BACK_SWIPE_ACTION, rightSwipeActions,
                    UserHandle.USER_CURRENT);
            int index = mRightSwipeActions.findIndexOfValue((String) newValue);
            mRightSwipeActions.setSummary(
                    mRightSwipeActions.getEntries()[index]);
            mRightSwipeAppSelection.setEnabled(rightSwipeActions == 5);
            customAppCheck();
            return true;
        } else if (preference == mExtendedSwipe) {
            boolean enabled = ((Boolean) newValue).booleanValue();
            mExtendedSwipe.setChecked(enabled);
            mTimeout.setEnabled(!enabled);
        } else if (preference == mGestureBarSize) {
            int value = Integer.parseInt((String) newValue);
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_HANDLE_WIDTH, value,
                    UserHandle.USER_CURRENT);
            int index = mGestureBarSize.findIndexOfValue((String) newValue);
            mGestureBarSize.setSummary(mGestureBarSize.getEntries()[index]);
            SystemNavigationGestureSettings.setBackSensivityOverlay(true);
            SystemNavigationGestureSettings.setCurrentSystemNavigationMode(getActivity(),
                    getOverlayManager(), SystemNavigationGestureSettings.getCurrentSystemNavigationMode(getActivity()));
            return true;
        } else if (preference == mLeftVerticalSwipeActions) {
            int leftVerticalSwipeActions = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.LEFT_VERTICAL_BACK_SWIPE_ACTION, leftVerticalSwipeActions,
                    UserHandle.USER_CURRENT);
            int index = mLeftVerticalSwipeActions.findIndexOfValue((String) newValue);
            mLeftVerticalSwipeActions.setSummary(
                    mLeftVerticalSwipeActions.getEntries()[index]);
            mLeftVerticalSwipeAppSelection.setEnabled(leftVerticalSwipeActions == 5);
            actionPreferenceReload();
            customAppCheck();
            return true;
        } else if (preference == mRightVerticalSwipeActions) {
            int rightVerticalSwipeActions = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.RIGHT_VERTICAL_BACK_SWIPE_ACTION, rightVerticalSwipeActions,
                    UserHandle.USER_CURRENT);
            int index = mRightVerticalSwipeActions.findIndexOfValue((String) newValue);
            mRightVerticalSwipeActions.setSummary(
                    mRightVerticalSwipeActions.getEntries()[index]);
            mRightVerticalSwipeAppSelection.setEnabled(rightVerticalSwipeActions == 5);
            actionPreferenceReload();
            customAppCheck();
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Ensure preferences sensible to change get updated
        actionPreferenceReload();
        customAppCheck();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Ensure preferences sensible to change gets updated
        actionPreferenceReload();
        customAppCheck();
    }

    /* Helper for reloading both short and long gesture as they might change on
       package uninstallation */
    private void actionPreferenceReload() {
        int leftSwipeActions = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.LEFT_LONG_BACK_SWIPE_ACTION, 0,
                UserHandle.USER_CURRENT);

        int rightSwipeActions = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.RIGHT_LONG_BACK_SWIPE_ACTION, 0,
                UserHandle.USER_CURRENT);

        int leftVerticalSwipeActions = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.LEFT_VERTICAL_BACK_SWIPE_ACTION, 0,
                UserHandle.USER_CURRENT);

        int rightVerticalSwipeActions = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.RIGHT_VERTICAL_BACK_SWIPE_ACTION, 0,
                UserHandle.USER_CURRENT);

        // Reload the action preferences
        mLeftSwipeActions.setValue(Integer.toString(leftSwipeActions));
        mLeftSwipeActions.setSummary(mLeftSwipeActions.getEntry());
        mLeftVerticalSwipeActions.setValue(Integer.toString(leftVerticalSwipeActions));
        mLeftVerticalSwipeActions.setSummary(mLeftVerticalSwipeActions.getEntry());

        mRightSwipeActions.setValue(Integer.toString(rightSwipeActions));
        mRightSwipeActions.setSummary(mRightSwipeActions.getEntry());
        mRightVerticalSwipeActions.setValue(Integer.toString(rightVerticalSwipeActions));
        mRightVerticalSwipeActions.setSummary(mRightVerticalSwipeActions.getEntry());

        mLeftSwipeAppSelection.setEnabled(mLeftSwipeActions.getEntryValues()
                [leftSwipeActions].equals("5"));
        mRightSwipeAppSelection.setEnabled(mRightSwipeActions.getEntryValues()
                [rightSwipeActions].equals("5"));
        mLeftVerticalSwipeAppSelection.setVisible(mLeftVerticalSwipeActions.getEntryValues()
                [leftVerticalSwipeActions].equals("5"));
        mRightVerticalSwipeAppSelection.setVisible(mRightVerticalSwipeActions.getEntryValues()
                [rightVerticalSwipeActions].equals("5"));
    }

    private void customAppCheck() {
        mLeftSwipeAppSelection.setSummary(Settings.System.getStringForUser(getContentResolver(),
                String.valueOf(Settings.System.LEFT_LONG_BACK_SWIPE_APP_FR_ACTION), UserHandle.USER_CURRENT));
        mRightSwipeAppSelection.setSummary(Settings.System.getStringForUser(getContentResolver(),
                String.valueOf(Settings.System.RIGHT_LONG_BACK_SWIPE_APP_FR_ACTION), UserHandle.USER_CURRENT));
        mLeftVerticalSwipeAppSelection.setSummary(Settings.System.getStringForUser(getContentResolver(),
                String.valueOf(Settings.System.LEFT_VERTICAL_BACK_SWIPE_APP_FR_ACTION), UserHandle.USER_CURRENT));
        mRightVerticalSwipeAppSelection.setSummary(Settings.System.getStringForUser(getContentResolver(),
                String.valueOf(Settings.System.RIGHT_VERTICAL_BACK_SWIPE_APP_FR_ACTION), UserHandle.USER_CURRENT));
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.SYBERIA;
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    final ArrayList<SearchIndexableResource> result = new ArrayList<>();
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.gesture_nav_tweaks;
                    result.add(sir);
                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    final List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
            };
}
