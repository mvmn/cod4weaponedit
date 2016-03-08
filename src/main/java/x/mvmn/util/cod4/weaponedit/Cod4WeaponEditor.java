package x.mvmn.util.cod4.weaponedit;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import x.mvmn.util.cod4.weaponedit.model.WeaponData;
import x.mvmn.util.cod4.weaponedit.service.WeaponDataService;

public class Cod4WeaponEditor {

	protected final JFrame mainWindow = new JFrame("CoD4 Weapon File Editor");

	protected final JTable table;

	protected final MainTableModel mainModel = new MainTableModel();

	protected final JButton btnLoad = new JButton("Load...");
	protected final JButton btnSave = new JButton("Save all");

	protected final WeaponDataService weaponDataService = new WeaponDataService();

	public Cod4WeaponEditor() {
		table = new JTable(mainModel);

		mainWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		mainWindow.getContentPane().setLayout(new BorderLayout());
		mainWindow.getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
		mainWindow.getContentPane().add(btnLoad, BorderLayout.NORTH);
		mainWindow.getContentPane().add(btnSave, BorderLayout.SOUTH);

		mainWindow.pack();
		mainWindow.setVisible(true);

		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actEvent) {
				final JFileChooser fileChoose = new JFileChooser();
				fileChoose.setMultiSelectionEnabled(true);
				fileChoose.setFileSelectionMode(JFileChooser.FILES_ONLY);
				if (JFileChooser.APPROVE_OPTION == fileChoose.showOpenDialog(null)) {
					for (final File file : fileChoose.getSelectedFiles()) {
						// TODO: Move off EDT
						final WeaponData weaponData = weaponDataService.load(file);
						setData(file, weaponData);
					}
				}
			}
		});

		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actEvent) {
				btnSave.setEnabled(false);

				new Thread() {
					public void run() {
						try {
							synchronized (mainModel) {
								for (int i = 1; i < mainModel.getColumnCount(); i++) {
									final File file = mainModel.getFile(i);
									final WeaponData weaponData = mainModel.getData(i);
									weaponDataService.save(file, weaponData, true);
								}
							}
						} finally {
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									btnSave.setEnabled(true);
								}
							});
						}
					}
				}.start();
			}
		});
	}

	protected void setData(final File file, final WeaponData data) {
		mainModel.addData(file, data);
	}

	public static void main(String args[]) {
		new Cod4WeaponEditor();
	}
}
