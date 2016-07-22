package imagemanipulations.zome.android.com;


import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.view.MenuItem;

import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class ThisSettingsActivity extends PreferenceActivity {
	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	private static Preference.OnPreferenceChangeListener sBindPreferenceModeToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String stringValue = value.toString();

			if (preference instanceof ListPreference) {
				// For list preferences, look up the correct display value in
				// the preference's 'entries' list.
				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);
				preference.setSummary(// Set the summary to reflect the new value.
						index >= 0
						? listPreference.getEntries()[index]
						: null);

			} else if (preference instanceof RingtonePreference) {
				// For ringtone preferences, look up the correct display value
				// using RingtoneManager.
				if (TextUtils.isEmpty(stringValue)) {
					// Empty values correspond to 'silent' (no ringtone).
					preference.setSummary(R.string.pref_ringtone_silent);

				} else {
					Ringtone ringtone = RingtoneManager.getRingtone(
							preference.getContext(), Uri.parse(stringValue));

					if (ringtone == null) {
						// Clear the summary if there was a lookup error.
						preference.setSummary(null);
					} else {
						// Set the summary to reflect the new ringtone display
						// name.
						String name = ringtone.getTitle(preference.getContext());
						preference.setSummary(name);
					}
				}

			} else {
				// For all other preferences, set the summary to the value's
				// simple string representation.
				preference.setSummary(stringValue);
			}
			return true;
		}
	};

	/**
	 * Helper method to determine if the device has an extra-large screen. For
	 * example, 10" tablets are extra-large.
	 */
	private static boolean isXLargeTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout
				& Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	/**
	 * Binds a preference's Mode to its value. More specifically, when the
	 * preference's value is changed, its Mode (line of text below the
	 * preference title) is updated to reflect the value. The summary is also
	 * immediately updated upon calling this method. The exact display format is
	 * dependent on the type of preference.
	 *
	 * @see #sBindPreferenceModeToValueListener
	 */
	private static void bindPreferenceModeToValue(Preference preference, Object defaultValue) {
		if (preference != null) {
			preference.setOnPreferenceChangeListener(sBindPreferenceModeToValueListener);// Set the listener to watch for value changes.
			// Trigger the listener immediately with the preference's current value.
			SharedPreferences sharedPreference = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
			if(defaultValue instanceof Boolean)
				sBindPreferenceModeToValueListener.onPreferenceChange(preference, sharedPreference.getBoolean(preference.getKey(), (Boolean) defaultValue));
			else if(defaultValue instanceof Integer)
				sBindPreferenceModeToValueListener.onPreferenceChange(preference, sharedPreference.getInt(preference.getKey(), (Integer) defaultValue));
			else
				sBindPreferenceModeToValueListener.onPreferenceChange(preference, sharedPreference.getString(preference.getKey(), (String) defaultValue));
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupActionBar();
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	private void setupActionBar() {
		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			// Show the Up button in the action bar.
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onIsMultiPane() {
		return isXLargeTablet(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.pref_headers, target);
	}

	/**
	 * This method stops fragment injection in malicious applications.
	 * Make sure to deny any unknown fragments here.
	 */
	protected boolean isValidFragment(String fragmentName) {
		return PreferenceFragment.class.getName().equals(fragmentName)
				|| ModePreferenceFragment.class.getName().equals(fragmentName)
				|| HelpAboutPreferenceFragment.class.getName().equals(fragmentName)
				|| SaveToPreferenceFragment.class.getName().equals(fragmentName);
	}

	/**
	 * This fragment shows general preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class ModePreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_mode);
			setHasOptionsMenu(true);

			// Bind the summaries of EditText/List/Dialog/Ringtone preferences
			// to their values. When their values change, their summaries are
			// updated to reflect the new value, per the Android Design
			// guidelines.
			bindPreferenceModeToValue(findPreference(getString(R.string.MASK_DETECTION_CHANNEL)), true);
			bindPreferenceModeToValue(findPreference(getString(R.string.CHECK_BOX_HISTOGRAM)), false);
			bindPreferenceModeToValue(findPreference(getString(R.string.THRESHOLD_WINDOW_INTEGRATION_DEEP)), "10");
			bindPreferenceModeToValue(findPreference(getString(R.string.THRESHOLD_SPOT_ACCUMULATION_WEIGHT)), "90");
			bindPreferenceModeToValue(findPreference(getString(R.string.MASK_BACKGROUND_VALUE)), "0");
			bindPreferenceModeToValue(findPreference(getString(R.string.THRESHOLD_SHOWING_PERCENTAGE)), "90");

			CharSequence[] entryTitles={
					getResources().getText(R.string.no_processing),
					getResources().getText(R.string.detect_by_area),
					getResources().getText(R.string.mask_spots)
			};
			CharSequence[] entryValues={
					String.valueOf(luckySpotsActivity.VIEW_MODE_RGBA),
					String.valueOf(luckySpotsActivity.VIEW_MODE_THRESHOLD_ZERO),
					String.valueOf(luckySpotsActivity.VIEW_MODE_THRESHOLD_SPOT)
			};
			ListPreference modeList = (ListPreference) findPreference(getResources().getText(R.string.VIEW_MODE_AS_STRING));
			modeList.setEntries(entryTitles);
			modeList.setEntryValues(entryValues);
			bindPreferenceModeToValue(modeList, String.valueOf(luckySpotsActivity.VIEW_MODE_THRESHOLD_SPOT));
			//modeList.setValue(new Integer(luckySpotsActivity.viewMode).toString());
			/*
			bindPreferenceModeToValue(findPreference("example_text"));
			bindPreferenceModeToValue(findPreference("video_path"));
			bindPreferenceModeToValue(findPreference("example_list"));
			*/
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			int id = item.getItemId();
			if (id == android.R.id.home) {
				startActivity(new Intent(getActivity(), ThisSettingsActivity.class));
				return true;
			}
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * This fragment shows notification preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class SaveToPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_saveto);
			setHasOptionsMenu(true);

			// Bind the summaries of EditText/List/Dialog/Ringtone preferences
			// to their values. When their values change, their summaries are
			// updated to reflect the new value, per the Android Design
			// guidelines.
			bindPreferenceModeToValue(findPreference(getResources().getText(R.string.THRESHOLD_SAVING_PATH_PICTURE)), "");
			bindPreferenceModeToValue(findPreference(getResources().getText(R.string.THRESHOLD_SAVING_PATH_VIDEO)), "");
			//bindPreferenceModeToValue(findPreference("notifications_new_message_ringtone"));
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			int id = item.getItemId();
			if (id == android.R.id.home) {
				startActivity(new Intent(getActivity(), ThisSettingsActivity.class));
				return true;
			}
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * This fragment shows data and sync preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class HelpAboutPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_help);
			setHasOptionsMenu(true);

			// Bind the summaries of EditText/List/Dialog/Ringtone preferences
			// to their values. When their values change, their summaries are
			// updated to reflect the new value, per the Android Design
			// guidelines.
			bindPreferenceModeToValue(findPreference("sync_frequency"), "");
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			int id = item.getItemId();
			if (id == android.R.id.home) {
				startActivity(new Intent(getActivity(), ThisSettingsActivity.class));
				return true;
			}
			return super.onOptionsItemSelected(item);
		}
	}
}
