package imagemanipulations.zome.android.com;

/**
 * Created by Sasha on 6/30/2016.
 */

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.Preference;
import android.util.AttributeSet;

import java.util.Date;

public class VersionPreference extends Preference {
	public VersionPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		String versionName;
		final PackageManager packageManager = context.getPackageManager();
		if (packageManager != null) {
			try {
				PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
				versionName = "Version:"+packageInfo.versionName+"\nUpdated:" +new Date(packageInfo.lastUpdateTime).toString();
			} catch (PackageManager.NameNotFoundException e) {
				versionName = null;
			}
			setSummary(versionName);
		}
	}
}
