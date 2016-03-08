package x.mvmn.util.cod4.weaponedit;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

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

	public void addData(final File file, final WeaponData data) {
		final Integer index = counter.incrementAndGet();
		synchronized (this) {
			final SortedSet<String> propertiesNamesSet = new TreeSet<String>(propertiesNames);
			propertiesNamesSet.addAll(data.listProperties());
			propertiesNames = new ArrayList<String>(propertiesNamesSet);
			dataMap.put(index, data);
			fileMap.put(index, file);
		}
		tableChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
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
