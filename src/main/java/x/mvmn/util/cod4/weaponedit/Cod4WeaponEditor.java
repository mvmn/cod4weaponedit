package x.mvmn.util.cod4.weaponedit;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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

	protected final JButton btnAddProperty = new JButton("Add property...");
	protected final JButton btnDeleteProperties = new JButton("Delete selected properties");
	protected final JButton btnLoad = new JButton("Load...");
	protected final JButton btnSave = new JButton("Save...");
	protected final JButton btnSaveAll = new JButton("Save all");

	protected final JTextField tfFilter = new JTextField("");
	protected final JCheckBox cbHideEqual = new JCheckBox("Hide equal rows", false);
	protected final JCheckBox cbSearchValues = new JCheckBox("Search include values", false);

	protected final WeaponDataService weaponDataService = new WeaponDataService();

	public Cod4WeaponEditor() {
		table = new JTable(mainModel);
		final TableRowSorter<MainTableModel> rowSorter = new TableRowSorter<MainTableModel>(mainModel);
		table.setRowSorter(rowSorter);

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

		final ActionListener updFilteringActListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateRowFilter(rowSorter);
			}
		};

		cbHideEqual.addActionListener(updFilteringActListener);
		cbSearchValues.addActionListener(updFilteringActListener);

		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actEvent) {
				final JFileChooser fileChoose = new JFileChooser();
				if (System.getProperty("user.dir") != null) {
					final File currentDir = new File(System.getProperty("user.dir"));
					if (currentDir.exists()) {
						fileChoose.setCurrentDirectory(currentDir);
					}
				}
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
			public void actionPerformed(ActionEvent e) {
				final int colCount = mainModel.getColumnCount();
				if (colCount > 1) {
					btnSave.setEnabled(false);

					final List<File> files = new ArrayList<File>(colCount - 1);
					final Map<File, WeaponData> dataToSave = new HashMap<File, WeaponData>();
					final Map<File, JCheckBox> checkboxesPerFile = new HashMap<File, JCheckBox>();
					final JPanel checkboxesPanel = new JPanel(new GridLayout(colCount, 1));
					checkboxesPanel.add(new JLabel("Select files to save:"));
					for (int i = 1; i < colCount; i++) {
						final File file = mainModel.getFile(i);
						files.add(file);
						dataToSave.put(file, mainModel.getData(i));
						final JCheckBox checkBox = new JCheckBox(file.getAbsolutePath());
						checkboxesPerFile.put(file, checkBox);
						checkboxesPanel.add(checkBox);
					}

					if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(mainWindow, checkboxesPanel, "Save files", JOptionPane.OK_CANCEL_OPTION)) {
						new Thread() {
							public void run() {
								try {
									synchronized (mainModel) {
										for (final File file : files) {
											if (checkboxesPerFile.get(file).isSelected()) {
												weaponDataService.save(file, dataToSave.get(file), true);
											}
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
					} else {
						btnSave.setEnabled(true);
					}
				}
			}
		});

		btnSaveAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actEvent) {
				final int colCount = mainModel.getColumnCount();
				if (colCount > 1) {
					btnSaveAll.setEnabled(false);
					final StringBuilder fileList = new StringBuilder("Are you sure? Files that will be overwritten:\n");
					final Map<File, WeaponData> dataToSave = new HashMap<File, WeaponData>();
					for (int i = 1; i < colCount; i++) {
						final File file = mainModel.getFile(i);
						dataToSave.put(file, mainModel.getData(i));
						fileList.append(" - ").append(file.getAbsolutePath()).append("\n");
					}

					if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(mainWindow, fileList.toString(), "Save all files?", JOptionPane.YES_NO_OPTION)) {
						new Thread() {
							public void run() {
								try {
									synchronized (mainModel) {
										for (Map.Entry<File, WeaponData> dataEntry : dataToSave.entrySet()) {
											weaponDataService.save(dataEntry.getKey(), dataEntry.getValue(), true);
										}
									}
								} finally {
									SwingUtilities.invokeLater(new Runnable() {
										public void run() {
											btnSaveAll.setEnabled(true);
										}
									});
								}
							}
						}.start();
					} else {
						btnSaveAll.setEnabled(true);
					}
				}
			}
		});

		btnAddProperty.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actEvent) {
				String input = JOptionPane.showInputDialog(mainWindow, "Enter new property key", "Add property", JOptionPane.PLAIN_MESSAGE);
				if (input != null && !input.trim().isEmpty()) {
					mainModel.addProperty(input.trim());
				}
			}
		});

		btnDeleteProperties.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (table.getSelectedRowCount() > 0) {
					final SortedSet<String> properties = new TreeSet<String>();
					for (final int row : table.getSelectedRows()) {
						properties.add(table.getValueAt(row, 0).toString());
					}
					final StringBuilder listOfProps = new StringBuilder();
					for (final String prop : properties) {
						if (listOfProps.length() > 0) {
							listOfProps.append(", ");
						}
						listOfProps.append(prop);
					}
					if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(mainWindow,
							"Are you sure you want to delete these properties: " + listOfProps.toString() + "?", "Confirm deletion", JOptionPane.YES_NO_OPTION)) {
						mainModel.deleteProperties(properties);
					}
				}
			}
		});

		final JPanel filterPanel = new JPanel(new GridLayout(3, 1));
		final JPanel propBtnPabel = new JPanel(new GridLayout(1, 2));
		filterPanel.setBorder(BorderFactory.createTitledBorder("Filters"));
		filterPanel.add(tfFilter);
		filterPanel.add(cbHideEqual);
		filterPanel.add(cbSearchValues);

		propBtnPabel.add(btnAddProperty);
		propBtnPabel.add(btnDeleteProperties);

		final JPanel mainContentPanel = new JPanel(new BorderLayout());
		mainContentPanel.setBorder(BorderFactory.createTitledBorder("Weapon properties"));
		mainContentPanel.add(new JScrollPane(table), BorderLayout.CENTER);
		mainContentPanel.add(propBtnPabel, BorderLayout.NORTH);

		final JPanel btnPanel = new JPanel(new GridLayout(1, 3));
		btnPanel.add(btnLoad);
		btnPanel.add(btnSave);
		btnPanel.add(btnSaveAll);
		btnPanel.setBorder(BorderFactory.createTitledBorder("File operations"));

		mainWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		mainWindow.getContentPane().setLayout(new BorderLayout());

		mainWindow.getContentPane().add(mainContentPanel, BorderLayout.CENTER);
		mainWindow.getContentPane().add(filterPanel, BorderLayout.NORTH);
		mainWindow.getContentPane().add(btnPanel, BorderLayout.SOUTH);

		mainWindow.pack();
		mainWindow.setVisible(true);
	}

	protected void updateRowFilter(final TableRowSorter<MainTableModel> rowSorter) {
		final String text = tfFilter.getText().toLowerCase();
		final boolean hideEqual = cbHideEqual.isSelected();
		final boolean searchValues = cbSearchValues.isSelected();
		rowSorter.setRowFilter(new RowFilter<MainTableModel, Integer>() {
			@Override
			public boolean include(javax.swing.RowFilter.Entry<? extends MainTableModel, ? extends Integer> entry) {
				final int columnCount = entry.getModel().getColumnCount();
				boolean result = text.isEmpty() || entry.getStringValue(0).toLowerCase().contains(text);
				if (!result && searchValues) {
					for (int i = 1; i < columnCount; i++) {
						result = entry.getStringValue(i).toLowerCase().contains(text);
						if (result) {
							break;
						}
					}
				}

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
