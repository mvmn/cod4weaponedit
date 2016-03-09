package x.mvmn.util.cod4.weaponedit;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import x.mvmn.util.cod4.weaponedit.model.WeaponData;

public class MainTableModel implements TableModel {

	protected final ConcurrentLinkedQueue<TableModelListener> listeners = new ConcurrentLinkedQueue<TableModelListener>();

	protected volatile List<String> propertiesNames = Collections.emptyList();
	protected volatile Map<Integer, WeaponData> dataMap = new TreeMap<Integer, WeaponData>();
	protected volatile Map<Integer, File> fileMap = new TreeMap<Integer, File>();
	protected final AtomicInteger counter = new AtomicInteger(0);

	protected final Object lockObject = new Object();

	public void deleteProperties(final Set<String> propertyNames) {
		synchronized (lockObject) {
			propertiesNames.removeAll(propertyNames);
			for (final WeaponData data : dataMap.values()) {
				for (final String propertyName : propertyNames) {
					data.setProperty(propertyName, null);
				}
			}
		}
		tableChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
	}

	public void addProperty(final String propertyName) {
		synchronized (lockObject) {
			final SortedSet<String> propertiesNamesSet = new TreeSet<String>(propertiesNames);
			propertiesNamesSet.add(propertyName);
			propertiesNames = new ArrayList<String>(propertiesNamesSet);
		}
		tableChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
	}

	public void removeData(final int index) {
		synchronized (lockObject) {
			final int lastIndex = counter.get();
			final Map<Integer, WeaponData> newDataMap = new TreeMap<Integer, WeaponData>(dataMap);
			final Map<Integer, File> newFileMap = new TreeMap<Integer, File>(fileMap);
			newDataMap.remove(index);
			newFileMap.remove(index);
			for (int i = index; i < lastIndex - 1; i++) {
				newDataMap.put(i, newDataMap.get(i + 1));
				newFileMap.put(i, newFileMap.get(i + 1));
			}
			newDataMap.remove(lastIndex);
			newFileMap.remove(lastIndex);
			final SortedSet<String> propertiesNamesSet = new TreeSet<String>();
			for (final WeaponData data : newDataMap.values()) {
				propertiesNamesSet.addAll(data.listProperties());
			}
			final List<String> newPropertiesNames = new ArrayList<String>(propertiesNamesSet);
			counter.decrementAndGet();
			fileMap = newFileMap;
			dataMap = newDataMap;
			propertiesNames = newPropertiesNames;
		}
		tableChanged(new TableModelEvent(MainTableModel.this, TableModelEvent.HEADER_ROW));
	}

	public void addData(final File file, final WeaponData data) {
		if (!fileMap.containsValue(file)) {
			synchronized (lockObject) {
				final Integer index = counter.incrementAndGet();
				final SortedSet<String> propertiesNamesSet = new TreeSet<String>(propertiesNames);
				final Map<Integer, WeaponData> newDataMap = new TreeMap<Integer, WeaponData>(dataMap);
				final Map<Integer, File> newFileMap = new TreeMap<Integer, File>(fileMap);
				propertiesNamesSet.addAll(data.listProperties());
				final List<String> newPropertiesNames = new ArrayList<String>(propertiesNamesSet);
				newFileMap.put(index, file);
				newDataMap.put(index, data);
				try {
					SwingUtilities.invokeAndWait(new Runnable() {
						public void run() {
							fileMap = newFileMap;
							dataMap = newDataMap;
							propertiesNames = newPropertiesNames;
							tableChanged(new TableModelEvent(MainTableModel.this, TableModelEvent.HEADER_ROW));
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		// tableChanged(new TableModelEvent(this, 0, getRowCount(), TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
	}

	public int getRowCount() {
		return propertiesNames.size();
	}

	public int getColumnCount() {
		return dataMap.keySet().size() + 1;
	}

	public String getColumnName(int columnIndex) {
		if (columnIndex > 0) {
			return fileMap.get(columnIndex).getName();
		} else {
			return "Property";
		}
	}

	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return rowIndex > 0 && columnIndex > 0;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		final String key = propertiesNames.get(rowIndex);
		if (columnIndex > 0) {
			return dataMap.get(columnIndex).getProperty(key);
		} else {
			return key;
		}
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex > 0) {
			final String key = propertiesNames.get(rowIndex);
			dataMap.get(columnIndex).setProperty(key, aValue != null ? aValue.toString() : null);
			tableChanged(new TableModelEvent(this, rowIndex, rowIndex, columnIndex, TableModelEvent.UPDATE));
		}

	}

	public void addTableModelListener(TableModelListener l) {
		listeners.add(l);
	}

	public void removeTableModelListener(TableModelListener l) {
		listeners.remove(l);
	}

	protected void tableChanged(final TableModelEvent event) {
		for (TableModelListener listener : listeners) {
			listener.tableChanged(event);
		}
	}

	public File getFile(int index) {
		return fileMap.get(index);
	}

	public WeaponData getData(int index) {
		return dataMap.get(index);
	}
}
