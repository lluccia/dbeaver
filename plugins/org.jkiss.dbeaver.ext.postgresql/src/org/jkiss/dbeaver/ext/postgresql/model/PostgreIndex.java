/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2016 Serge Rieder (serge@jkiss.org)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (version 2)
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.jkiss.dbeaver.ext.postgresql.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.postgresql.PostgreUtils;
import org.jkiss.dbeaver.model.DBPHiddenObject;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.jdbc.struct.JDBCTableIndex;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.rdb.DBSIndexType;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * PostgreIndex
 */
public class PostgreIndex extends JDBCTableIndex<PostgreSchema, PostgreTableBase> implements DBPHiddenObject
{
    private boolean isUnique;
    private boolean isPrimary; // Primary index - implicit
    private boolean isExclusion;
    private boolean isImmediate;
    private boolean isClustered;
    private boolean isValid;
    private boolean isCheckXMin;
    private boolean isReady;
    private String description;
    private List<PostgreIndexColumn> columns = new ArrayList<>();
    private int amId;

    public PostgreIndex(PostgreTableBase parent, String indexName, ResultSet dbResult) {
        super(
            parent.getContainer(),
            parent,
            indexName,
            DBSIndexType.UNKNOWN,
            true);
        this.isUnique = JDBCUtils.safeGetBoolean(dbResult, "indisunique");
        this.isPrimary = JDBCUtils.safeGetBoolean(dbResult, "indisprimary");
        this.isExclusion = JDBCUtils.safeGetBoolean(dbResult, "indisexclusion");
        this.isImmediate = JDBCUtils.safeGetBoolean(dbResult, "indimmediate");
        this.isClustered = JDBCUtils.safeGetBoolean(dbResult, "indisclustered");
        this.isValid = JDBCUtils.safeGetBoolean(dbResult, "indisvalid");
        this.isCheckXMin = JDBCUtils.safeGetBoolean(dbResult, "indcheckxmin");
        this.isReady = JDBCUtils.safeGetBoolean(dbResult, "indisready");

        this.description = JDBCUtils.safeGetString(dbResult, "description");
        this.amId = JDBCUtils.safeGetInt(dbResult, "relam");
    }

    public PostgreIndex(PostgreTableBase parent, String name, DBSIndexType indexType) {
        super(parent.getContainer(), parent, name, indexType, false);
    }

    @NotNull
    @Override
    public PostgreDataSource getDataSource()
    {
        return getTable().getDataSource();
    }

    @Override
    @Property(viewable = true, order = 5)
    public boolean isUnique()
    {
        return !isUnique;
    }

    @Property(viewable = false, order = 20)
    public boolean isPrimary() {
        return isPrimary;
    }

    @Property(viewable = false, order = 21)
    public boolean isExclusion() {
        return isExclusion;
    }

    @Property(viewable = false, order = 22)
    public boolean isImmediate() {
        return isImmediate;
    }

    @Property(viewable = false, order = 23)
    public boolean isClustered() {
        return isClustered;
    }

    @Property(viewable = false, order = 24)
    public boolean isValid() {
        return isValid;
    }

    @Property(viewable = false, order = 25)
    public boolean isCheckXMin() {
        return isCheckXMin;
    }

    @Property(viewable = false, order = 26)
    public boolean isReady() {
        return isReady;
    }

    public DBSIndexType getIndexType()
    {
        return super.getIndexType();
    }

    @Nullable
    @Override
    @Property(viewable = true, order = 100)
    public String getDescription()
    {
        return description;
    }

    @Nullable
    @Property(viewable = true, order = 30)
    public PostgreAccessMethod getAccessMethod(DBRProgressMonitor monitor) throws DBException {
        if (amId <= 0) {
            return null;
        }
        return PostgreUtils.getObjectById(monitor, getTable().getDatabase().accessMethodCache, getTable().getDatabase(), amId);
    }

    @Override
    public List<PostgreIndexColumn> getAttributeReferences(DBRProgressMonitor monitor)
    {
        return columns;
    }

    public PostgreIndexColumn getColumn(String columnName)
    {
        return DBUtils.findObject(columns, columnName);
    }

    void setColumns(List<PostgreIndexColumn> columns)
    {
        this.columns = columns;
    }

    public void addColumn(PostgreIndexColumn column)
    {
        if (columns == null) {
            columns = new ArrayList<>();
        }
        columns.add(column);
    }

    @NotNull
    @Override
    public String getFullQualifiedName()
    {
        return DBUtils.getFullQualifiedName(getDataSource(),
            getTable().getContainer(),
            this);
    }

    @Override
    public boolean isHidden() {
        return isPrimary;
    }
}
