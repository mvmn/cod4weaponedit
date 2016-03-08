package x.mvmn.util.cod4.weaponedit.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Set;

import javax.swing.JFileChooser;

import org.apache.commons.io.IOUtils;

import x.mvmn.util.cod4.weaponedit.model.WeaponData;

public class WeaponDataService {

	public boolean save(final File file, final WeaponData weaponData, final boolean overwrite) {
		boolean result = false;
		if (file.exists() && overwrite || !file.exists()) {
			if (file.exists()) {
				file.delete();
			}
			final StringBuilder content = new StringBuilder("WEAPONFILE");
			Set<String> keys = weaponData.listProperties();
			for (final String key : keys) {
				content.append("\\").append(key).append("\\").append(weaponData.getProperty(key));
			}
			try {
				IOUtils.write(content.toString(), new FileOutputStream(file));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public WeaponData load(final File file) {
		WeaponData result = null;
		if (file.exists()) {
			try {
				final String fileContent = IOUtils.toString(new FileInputStream(file));
				if (fileContent.startsWith("WEAPONFILE\\")) {
					String[] splits = fileContent.split("\\\\");
					result = new WeaponData();
					String key = null;
					for (int i = 1; i < splits.length; i++) {
						if (key == null) {
							key = splits[i];
						} else {
							result.setProperty(key, splits[i]);
							key = null;
						}
					}
				} else {
					throw new Exception("Not a weapon file: " + file.getAbsolutePath());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return result;
	}

	public static void main(String args[]) {
		JFileChooser fileChoose = new JFileChooser();
		if (JFileChooser.APPROVE_OPTION == fileChoose.showOpenDialog(null)) {
			WeaponData weaponData = new WeaponDataService().load(fileChoose.getSelectedFile());
			Set<String> keys = weaponData.listProperties();
			for (final String key : keys) {
				System.out.println(key + " == " + weaponData.getProperty(key));
			}
		}
	}
}
