package hxc.connectors.database.mysql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import hxc.connectors.database.Table;
import hxc.utils.reflection.ClassInfo;
import hxc.utils.reflection.FieldInfo;
import hxc.utils.reflection.ReflectionHelper;

public class TableInfo
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	private ClassInfo classInfo;
	private String tableName;
	private String uniqueIndex;
	private static Map<String, TableInfo> tableInfoCache = new HashMap<String, TableInfo>();
	private Map<String, ColumnInfo> columnsByFieldName = new LinkedHashMap<String, ColumnInfo>();
	private Map<String, ColumnInfo> columnsByColumnName = new LinkedHashMap<String, ColumnInfo>();
	private List<ColumnInfo> primaryKeyColumns = new ArrayList<ColumnInfo>();
	private Map<String, String> ddlStrings = new HashMap<String, String>();
	private boolean existanceChecked;

	public ClassInfo getClassInfo()
	{
		return classInfo;
	}

	public String getTableName()
	{
		return tableName;
	}

	public String getUniqueIndex()
	{
		return uniqueIndex;
	}

	public Map<String, ColumnInfo> getColumnsByFieldName()
	{
		return columnsByFieldName;
	}

	public Map<String, ColumnInfo> getColumnsByColumnName()
	{
		return columnsByColumnName;
	}

	public List<ColumnInfo> getPrimaryKeyColumns()
	{
		return primaryKeyColumns;
	}

	public String getDDL(String ddlType)
	{
		return ddlStrings.get(ddlType);
	}

	public void settDDL(String ddlType, String ddl)
	{
		ddlStrings.put(ddlType, ddl);
	}

	public boolean isExistanceChecked()
	{
		return existanceChecked;
	}

	public void setExistanceChecked(boolean existanceChecked)
	{
		this.existanceChecked = existanceChecked;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	private TableInfo(Class<?> type) throws SQLException
	{
		// Create ClassInfo
		classInfo = ReflectionHelper.getClassInfo(type);

		// Get Annotation
		Table tableAnnotation = classInfo.getAnnotation(Table.class);

		// Set Table Name
		if (tableAnnotation != null && tableAnnotation.name() != null && tableAnnotation.name().length() > 0)
		{
			tableName = tableAnnotation.name();
			uniqueIndex = tableAnnotation.uniqueIndex();
		}
		else
		{
			tableName = type.getName();
			int lastDot = tableName.lastIndexOf(".");
			if (lastDot >= 0)
				tableName = tableName.substring(lastDot + 1);
		}

		// Create ColumnInfo
		for (FieldInfo fieldInfo : classInfo.getFields().values())
		{
			ColumnInfo columnInfo = ColumnInfo.Create(this, fieldInfo);
			if (columnInfo == null)
				continue;
			columnsByFieldName.put(columnInfo.getFieldName(), columnInfo);
			columnsByColumnName.put(columnInfo.getName().toLowerCase(), columnInfo);
		}
	}

	public static <T> TableInfo get(Class<T> type) throws SQLException
	{
		// Attempt to retrieve from Cache
		String typeName = type.getName();
		TableInfo tableInfo = tableInfoCache.get(type.getName());
		if (tableInfo != null)
			return tableInfo;

		// Create Instance
		tableInfo = new TableInfo(type);

		// Cache it
		synchronized (tableInfoCache)
		{
			if (!tableInfoCache.containsKey(typeName))
				tableInfoCache.put(typeName, tableInfo);
		}

		return tableInfo;
	}

	@Override
	public String toString()
	{
		return getTableName();
	}

	public static void clear()
	{
		tableInfoCache.clear();
	}

}
