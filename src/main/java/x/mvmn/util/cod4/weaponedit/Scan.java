package x.mvmn.util.cod4.weaponedit;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import x.mvmn.util.cod4.weaponedit.model.WeaponData;
import x.mvmn.util.cod4.weaponedit.service.WeaponDataService;

public class Scan {

	public static void main(String[] args) {
		final WeaponDataService wds = new WeaponDataService();
		final Map<File, WeaponData> dataMap = new TreeMap<File, WeaponData>();
		final Set<String> allProperties = new TreeSet<String>();
		final Map<String, Set<String>> propValsMap = new TreeMap<String, Set<String>>();
		for (File file : new File("/Users/mvmn/Desktop/iwds/iw_11/weapons/mp").listFiles()) {
			dataMap.put(file, wds.load(file));
		}

		for (final Map.Entry<File, WeaponData> dataEntry : dataMap.entrySet()) {
			allProperties.addAll(dataEntry.getValue().listProperties());
		}

		for (final Map.Entry<File, WeaponData> dataEntry : dataMap.entrySet()) {
			for (final String property : allProperties) {
				Set<String> vals = propValsMap.get(property);
				if (vals == null) {
					vals = new TreeSet<String>();
					propValsMap.put(property, vals);
				}
				vals.add(checkVal(dataEntry.getValue().getProperty(property)));
			}
		}

		System.out.println("{");
		for (final Map.Entry<String, Set<String>> entry : propValsMap.entrySet()) {
			System.out.print("  \"" + entry.getKey() + "\" : [");
			boolean first = true;
			for (final String value : entry.getValue()) {
				if (first) {
					first = false;
				} else {
					System.out.print(", ");
				}
				System.out.print("\"" + value + "\"");
			}
			System.out.println("],");
		}
		System.out.println("}");
	}

	protected static String checkVal(String val) {
		if (val != null) {
			try {
				Double.parseDouble(val);
				val = "1.23";
			} catch (Exception e) {
			}
			try {
				Integer.parseInt(val);
				val = "123";
			} catch (Exception e) {
			}
			if (val.contains("\n")) {
				val = "\\MULTILINE\\";
			}
		}
		return val != null ? val : "\\NULL\\";
	}
}
