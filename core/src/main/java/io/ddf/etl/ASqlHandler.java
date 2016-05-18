/**
 *
 */
package io.ddf.etl;


import io.ddf.DDF;
import io.ddf.TableNameReplacer;
import io.ddf.content.Schema;
import io.ddf.content.Schema.Column;
import io.ddf.content.SqlResult;
import io.ddf.datasource.DataFormat;
import io.ddf.datasource.DataSourceDescriptor;
import io.ddf.datasource.JDBCDataSourceDescriptor;
import io.ddf.datasource.SQLDataSourceDescriptor;
import io.ddf.exception.DDFException;
import io.ddf.misc.ADDFFunctionalGroupHandler;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.describe.DescribeTable;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.show.ShowTables;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 */
public abstract class ASqlHandler extends ADDFFunctionalGroupHandler implements IHandleSql {

  public ASqlHandler(DDF theDDF) {
    super(theDDF);
  }

  /**
   * @return The table names.
   * @brief Show tables in the database.
   */
  public SqlResult showTables() throws DDFException {
    List<String> tableNames = new ArrayList<String>();
    for (DDF ddf : this.getManager().listDDFs()) {
      if (ddf.getName() != null) {
        tableNames.add(ddf.getName());
      }
    }
    List<Column> columnList = new ArrayList<Column>();
    columnList.add(new Column("table_name", Schema.ColumnType.STRING));
    Schema schema = new Schema("tables", columnList);
    return new SqlResult(schema, tableNames);
  }

  /**
   * @param name      The URI or the name of the ddf.
   * @param namespace The namespace.
   * @return The column information.
   * @throws DDFException
   * @brief Get the column information of this table.
   */
  private SqlResult describeTable(String name, String namespace)
          throws DDFException {
    DDF ddf = null;
    try {
      ddf = this.getManager().getOrRestoreDDFUri(name);
    } catch (Exception e) {
      if (null == namespace) {
        throw new DDFException("ERROR: there is no ddf " + name);
      }
      try {
        mLog.info("trying to restore " + "ddf://" + namespace + "/" + name);
        ddf = this.getManager().getOrRestoreDDFUri("ddf://" + namespace + "/" + name);
      } catch (Exception e2) {
        throw new DDFException("ERROR: there is no ddf " + name);
      }
    }

    int colSize = ddf.getNumColumns();
    List<String> ret = new ArrayList<String>();
    for (int colIdx = 0; colIdx < colSize; ++colIdx) {
      Schema.Column col = ddf.getColumn(ddf.getColumnName(colIdx));
      ret.add(col.getName().concat("\t").concat(col.getType().toString()
              .toLowerCase()));
    }
    List<Column> columnList = new ArrayList<Column>();
    columnList.add(new Column("column_name", Schema.ColumnType.STRING));
    columnList.add(new Column("value_type", Schema.ColumnType.STRING));
    Schema schema = new Schema("table_info", columnList);
    return new SqlResult(schema, ret);
  }

  public SqlResult sqlHandle(String command,
                             Integer maxRows,
                             DataSourceDescriptor dataSource) throws DDFException {
    return this.sqlHandle(command,
            maxRows,
            dataSource,
            new TableNameReplacer(this.getManager(), dataSource));
  }


  public SqlResult sqlHandle(String sqlcmd,
                             Integer maxRows,
                             DataSourceDescriptor dataSource,
                             TableNameReplacer tableNameReplacer) throws DDFException {
    // If the user specifies the datasource, we should directly send the sql
    // command to the sql engine.
    if (dataSource != null) {
      // TODO: add support for other datasource.
      if (dataSource instanceof JDBCDataSourceDescriptor) {
        // It's the jdbc datasource.
        return this.sql(sqlcmd, maxRows, dataSource);
      }
      SQLDataSourceDescriptor sqlDataSourceDescriptor = (SQLDataSourceDescriptor) dataSource;
      if (sqlDataSourceDescriptor == null) {
        throw new DDFException("ERROR: Handling datasource");
      }
      if (sqlDataSourceDescriptor.getDataSource() != null) {
        switch (sqlDataSourceDescriptor.getDataSource()) {
          case "SparkSQL":
          case "spark":
          case "Spark":
            String sql = sqlcmd;

            if (sql.toLowerCase().trim().startsWith("select")) {
              sql = handleSqlExpression(sql);
            }

            return this.sql(sql, maxRows, dataSource);
          default:
            //throw new DDFException("ERROR: Unrecognized datasource");
            return this.sql(sqlcmd, maxRows, dataSource);
        }
      }
    }


    this.mLog.info("Handle SQL: " + sqlcmd);
    CCJSqlParserManager parserManager = new CCJSqlParserManager();
    StringReader reader = new StringReader(sqlcmd);
    try {
      Statement statement = parserManager.parse(reader);
      if (statement instanceof ShowTables) {
        return this.showTables();
      } else if (statement instanceof DescribeTable) {
        return this.describeTable(((DescribeTable) statement).getName()
                .getName(), tableNameReplacer.getNamespace());
      } else if (statement instanceof Select) {
        // Standard SQL.
        statement = tableNameReplacer.run(statement);
        this.mLog.info("Reformulate SQL to " + statement.toString());
        String sql = handleSqlExpression(statement.toString());
        return this.sql(sql, maxRows, dataSource);
      } else if (statement instanceof Drop) {
        // TODO: +rename
        return null;
      } else {
        throw new DDFException("ERROR: Only show tables, describe tables, " +
                "select, drop, and rename operations are allowed on ddf");
      }
    } catch (JSQLParserException e) {
      throw new DDFException(" SQL Syntax ERROR: " + e.getCause().getMessage
              ().split("\n")[0]);
    } catch (DDFException e) {
      throw e;
    } catch (Exception e) {
      throw new DDFException(e);
    }
  }


  public DDF sql2ddfHandle(String command,
                           Schema schema,
                           DataSourceDescriptor dataSource,
                           DataFormat dataFormat) throws DDFException {
    return sql2ddfHandle(command,
            schema,
            dataSource,
            dataFormat,
            new TableNameReplacer(this.getManager(), dataSource));
  }

  public DDF sql2ddfHandle(String command,
                           Schema schema,
                           DataSourceDescriptor dataSource,
                           DataFormat dataFormat,
                           TableNameReplacer tableNameReplacer) throws DDFException {

    if (dataSource != null) {
      if (dataSource instanceof JDBCDataSourceDescriptor) {
        return this.sql2ddf(command, schema, dataSource, dataFormat);
      }
      SQLDataSourceDescriptor sqlDataSourceDescriptor = (SQLDataSourceDescriptor) dataSource;
      if (sqlDataSourceDescriptor == null) {
        throw new DDFException("ERROR: Handling datasource");
      }
      if (sqlDataSourceDescriptor.getDataSource() != null) {
        switch (sqlDataSourceDescriptor.getDataSource()) {
          case "SparkSQL":
          case "spark":
          case "Spark":
            String sql = command;
            if (sql.toLowerCase().trim().startsWith("select")) {
              sql = handleSqlExpression(sql);
            }
            return this.sql2ddf(sql, schema, dataSource, dataFormat);
          default:
            // throw new DDFException("ERROR: Unrecognized datasource:
            // " + dataSource);
            return this.sql2ddf(command, schema, dataSource, dataFormat);
        }
      }
    }

    this.mLog.info("Handle SQL: " + command);
    CCJSqlParserManager parserManager = new CCJSqlParserManager();
    StringReader reader = new StringReader(command);
    try {
      Statement statement = parserManager.parse(reader);
      if (!(statement instanceof Select)) {
        throw new DDFException("ERROR: Only select is allowed in this sql2ddf");
      } else {
        statement = tableNameReplacer.run(statement);
        this.mLog.info("Reformulate SQL to " + statement.toString());
        String sql = handleSqlExpression(statement.toString());

        // TODO(fanj) optimization here;
        return this.sql2ddf(sql, schema, dataSource,
                dataFormat);

      }
    } catch (JSQLParserException e) {
      throw new DDFException(" SQL Syntax ERROR: " + e.getCause().getMessage().split("\n")[0]);
    } catch (DDFException e) {
      throw e;
    } catch (Exception e) {
      throw new DDFException(e);
    }
  }

  /**
   * This is a util function that should be implemented by sub-classes to handle engine-specific expression
   * Use-cases:
   * - Support a new UDF that is mapped into existing one. For example, median() which is mapped to percentile()
   *
   * @param sql The SQL query to handle
   * @return modified sql
   */
  protected String handleSqlExpression(String sql) throws DDFException {
    return sql;
  }
}
