package com.felees.hbnpojogen;

public class RelationItem  {
	private String catalog;
	private String schema;
	private String fkSchema;
	private String tableName;
	private String fkTableName;
	private String fkCatalog;
	private String fkName;
	private String fkColumnName;
	private String pkColumnName;
	private Integer keySeq;

	public String getFkName() {
		return fkName;
	}
	public void setFkName(String fkName) {
		this.fkName = fkName;
	}
	public String getPkColumnName() {
		return pkColumnName;
	}
	public void setPkColumnName(String pkColumnName) {
		this.pkColumnName = pkColumnName;
	}

	public Integer getKeySeq() {
		return keySeq;
	}
	public void setKeySeq(Integer keySeq) {
		this.keySeq = keySeq;
	}
	public String getCatalog() {
		return catalog;
	}
	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getFkColumnName() {
		return fkColumnName;
	}
	public void setFkColumnName(String fkColumnName) {
		this.fkColumnName = fkColumnName;
	}

	@Override
	public String toString() {
		return "Catalog = " + catalog +", tableName = " + tableName +", fkColumnName = "+fkColumnName + ", FK table: "+this.fkTableName;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof RelationItem)){
			return false;
		}

		RelationItem rel = (RelationItem)obj;

		return rel != null 
		&& rel.catalog.equals(this.catalog) 
		&& rel.schema.equals(this.schema)
		&& rel.fkSchema.equals(this.fkSchema)
		&& rel.fkName.equals(this.fkName) 
		&& rel.fkTableName.equals(this.fkTableName)
		&& rel.tableName.equals(this.tableName) 
		&& rel.fkColumnName.equals(this.fkColumnName) 
		&& rel.pkColumnName.equals(this.pkColumnName)
		&& rel.fkCatalog.equals(this.fkCatalog) 
		&& rel.keySeq.equals(this.keySeq); 
		 
		
	}
	public String getFkTableName() {
		return fkTableName;
	}
	public void setFkTableName(String fkTableName) {
		this.fkTableName = fkTableName;
	}
	public String getFkCatalog() {
		return fkCatalog;
	}
	public void setFkCatalog(String fkCatalog) {
		this.fkCatalog = fkCatalog;
	}
	/**
	 * @return the schema
	 */
	public String getSchema() {
		return schema;
	}
	/**
	 * @param schema the schema to set
	 */
	public void setSchema(String schema) {
		this.schema = schema;
	}
	/**
	 * @return the fkSchema
	 */
	public String getFkSchema() {
		return fkSchema;
	}
	/**
	 * @param fkSchema the fkSchema to set
	 */
	public void setFkSchema(String fkSchema) {
		this.fkSchema = fkSchema;
	}
}
