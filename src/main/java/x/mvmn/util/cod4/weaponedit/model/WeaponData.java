package x.mvmn.util.cod4.weaponedit.model;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class WeaponData {

	protected final SortedMap<String, String> values = new TreeMap<String, String>();

	public synchronized String getProperty(final String key) {
		return values.get(key);
	}

	public synchronized String setProperty(final String key, final String value) {
		if (value == null) {
			return values.remove(key);
		} else {
			return values.put(key, value);
		}
	}

	public synchronized SortedSet<String> listProperties() {
		return new TreeSet<String>(values.keySet());
	}
}
