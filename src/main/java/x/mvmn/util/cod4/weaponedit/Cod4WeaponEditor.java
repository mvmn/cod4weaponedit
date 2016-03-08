package x.mvmn.util.cod4.weaponedit;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;

import x.mvmn.util.cod4.weaponedit.model.WeaponData;
import x.mvmn.util.cod4.weaponedit.service.WeaponDataService;

public class Cod4WeaponEditor {

	protected final JFrame mainWindow = new JFrame("CoD4 Weapon File Editor");

	protected final JTable table;

	protected final MainTableModel mainModel = new MainTableModel();

	protected final JButton btnLoad = new JButton("Load...");
	protected final JButton btnSave = new JButton("Save all");

	protected final JTextField tfFilter = new JTextField("");
	protected final JCheckBox cbHideEqual = new JCheckBox("Hide equal rows", false);

	protected final WeaponDataService weaponDataService = new WeaponDataService();

	public Cod4WeaponEditor() {
		table = new JTable(mainModel);
		final TableRowSorter<MainTableModel> rowSorter = new TableRowSorter<MainTableModel>(mainModel);
		table.setRowSorter(rowSorter);

		tfFilter.setBorder(BorderFactory.createTitledBorder("Filter properties"));
		tfFilter.getDocument().addDocumentListener(new DocumentListener() {
			public void removeUpdate(DocumentEvent e) {
				onChange();
			}

			public void insertUpdate(DocumentEvent e) {
				onChange();
			}

			public void changedUpdate(DocumentEvent e) {
			}

			protected void onChange() {
				updateRowFilter(rowSorter);
			}
		});
		cbHideEqual.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateRowFilter(rowSorter);
			}
		});

		final JPanel btnPanel = new JPanel(new GridLayout(1, 2));
		btnPanel.add(btnLoad);
		btnPanel.add(btnSave);

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

		mainWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		mainWindow.getContentPane().setLayout(new BorderLayout());
		mainWindow.getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
		JPanel filterPanel = new JPanel(new GridLayout(2, 1));
		filterPanel.add(tfFilter);
		filterPanel.add(cbHideEqual);
		mainWindow.getContentPane().add(filterPanel, BorderLayout.NORTH);
		mainWindow.getContentPane().add(btnPanel, BorderLayout.SOUTH);

		mainWindow.pack();
		mainWindow.setVisible(true);
	}

	protected void updateRowFilter(final TableRowSorter<MainTableModel> rowSorter) {
		final String text = tfFilter.getText().toLowerCase();
		final boolean hideEqual = cbHideEqual.isSelected();
		rowSorter.setRowFilter(new RowFilter<MainTableModel, Integer>() {
			@Override
			public boolean include(javax.swing.RowFilter.Entry<? extends MainTableModel, ? extends Integer> entry) {
				final int columnCount = entry.getModel().getColumnCount();
				boolean result = text.isEmpty() || entry.getStringValue(0).toLowerCase().contains(text);
				if (result && hideEqual && columnCount > 2) {
					result = false;
					final String one = entry.getStringValue(1);
					for (int i = 2; i < columnCount; i++) {
						if (!entry.getStringValue(i).equals(one)) {
							result = true;
							break;
						}
					}
				}

				return result;
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
