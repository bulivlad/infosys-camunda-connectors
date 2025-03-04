/*
 * Copyright (c) 2022 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */
package com.infosys.camundaconnectors.db.oracle.service;

import com.infosys.camundaconnectors.db.oracle.model.request.OracleDBRequestData;
import com.infosys.camundaconnectors.db.oracle.model.response.OracleDBResponse;
import com.infosys.camundaconnectors.db.oracle.model.response.QueryResponse;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

import io.camunda.connector.api.annotation.Secret;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateTableService implements OracleDBRequestData {
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateTableService.class);
  private static final String CONSTRAINT = "constraints";
  @NotBlank
  @Secret
  private String databaseName;
  @NotBlank
  private String tableName;

  @NotEmpty(message = "columnsList can't be null or empty")
  private List<Map<String, Object>> columnsList;

  @Override
  public OracleDBResponse invoke(Connection connection) throws SQLException {
    QueryResponse<String> queryResponse;
    try {
      String columns = getColumns(columnsList);
      if (columns.isBlank()) {
        String errMSg =
            "Invalid 'columnsList', It should be a list of maps for "
                + "column, with keys: 'colName', 'dataType' and optional 'constraints'";
        LOGGER.error(errMSg);
        throw new RuntimeException(errMSg);
      }
      String queryString = "CREATE TABLE " + tableName + " (" + columns + ")";
      LOGGER.info("Create Table Query: {}", queryString);
      createTable(connection, queryString, tableName);
      queryResponse = new QueryResponse<>("Table '" + tableName + "' created successfully");
      LOGGER.info("CreateTableQueryStatus: {}", queryResponse.getResponse());
    } finally {
      try {
        connection.close();
      } catch (SQLException e) {
        LOGGER.warn("Error while closing the database connection");
      }
    }
    return queryResponse;
  }

  private String getColumns(List<Map<String, Object>> columnsList) {
    StringBuilder columns = new StringBuilder();
    boolean first = true;
    for (Map<String, Object> colMap : columnsList) {
      if (colMap != null && !colMap.isEmpty()) {
        Map<String, Object> columnDetails = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        columnDetails.putAll(colMap);
        String columnNameStr = columnDetails.getOrDefault("colName", "").toString();
        String dataTypeStr = columnDetails.getOrDefault("dataType", "").toString();
        if (columnNameStr.isBlank() || dataTypeStr.isBlank()) {
          String errMSg = "colName or dataType can't be null or empty";
          LOGGER.error(errMSg);
          throw new RuntimeException(errMSg);
        }
        if (columnDetails.get(CONSTRAINT) != null) {
          StringBuilder constraints = new StringBuilder();
          if (columnDetails.get(CONSTRAINT) instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> columnConstraintsList = (List<String>) columnDetails.get(CONSTRAINT);
            for (String constraint : columnConstraintsList) {
              if (constraint != null && !constraint.isBlank())
                constraints.append(" ").append(constraint);
            }
          } else if (columnDetails.get(CONSTRAINT) instanceof String) {
            constraints.append(" ").append(columnDetails.get(CONSTRAINT));
          } else {
            String errMSg =
                "Invalid constraint type '"
                    + columnDetails.get(CONSTRAINT)
                    + "' passed in column"
                    + columnNameStr;
            LOGGER.error(errMSg);
            throw new RuntimeException(errMSg);
          }
          if (first) {
            columns
                .append(columnNameStr)
                .append(" ")
                .append(dataTypeStr)
                .append(" ")
                .append(constraints);
            first = false;
          } else {
            columns
                .append(",")
                .append(columnNameStr)
                .append(" ")
                .append(dataTypeStr)
                .append(" ")
                .append(constraints);
          }
        } else {
          if (first) {
            columns.append(columnNameStr).append(" ").append(dataTypeStr);
            first = false;
          } else {
            columns.append(",").append(columnNameStr).append(" ").append(dataTypeStr);
          }
        }
      }
    }
    return columns.toString();
  }

  private void createTable(Connection conn, String createTableQuery, String tableName)
      throws SQLException {
    try (Statement st = conn.createStatement()) {
      st.execute(createTableQuery);
    } catch (SQLException sqlException) {
      LOGGER.error("SQLException: {}", sqlException.getMessage());
      if (sqlException.getMessage().contains("name is already used by an existing object"))
        throw new RuntimeException(
            "InvalidTableName : Table '" + tableName + "' already exists in the database",
            sqlException);
      throw sqlException;
    }
  }

  public String getDatabaseName() {
    return databaseName;
  }

  public void setDatabaseName(String databaseName) {
    this.databaseName = databaseName;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public List<Map<String, Object>> getColumnsList() {
    return columnsList;
  }

  public void setColumnsList(List<Map<String, Object>> columnsList) {
    this.columnsList = columnsList;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CreateTableService that = (CreateTableService) o;
    return Objects.equals(databaseName, that.databaseName)
        && Objects.equals(tableName, that.tableName)
        && Objects.equals(columnsList, that.columnsList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(databaseName, tableName, columnsList);
  }

  @Override
  public String toString() {
    return "CreateTableService{"
        + "databaseName='"
        + databaseName
        + '\''
        + ", tableName='"
        + tableName
        + '\''
        + ", columnsList="
        + columnsList
        + '}';
  }
}
