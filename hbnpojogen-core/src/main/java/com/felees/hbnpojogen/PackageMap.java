/**
 *
 */
package com.felees.hbnpojogen;

import java.io.Serializable;

/**
 * @author wallacew
 *
 */
public class PackageMap implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 128523304030636399L;
	/** Inner handle */
	private String objectPackage;
	/** Inner handle */
	private String objectInterfacePackage;
	/** Inner handle */
	private String objectTableRepoPackage;
	/** Inner handle */
	private String daoPackage;
	/** Inner handle */
	private String daoImplPackage;
    /** Inner handle */
    private String enumPackage;
    /** Inner handle */
    private String enumSubtypePackage;
    /** Inner handle */
	private String factoryPackage;
	   /** Inner handle */
		private String utilPackage;
	/** Inner handle */
	private String dataPackage;

	/** Object package.
	 * @return object package name
	 */
	public String getObjectPackage() {
		return this.objectPackage;
	}
	/**
	 * @param objectPackage
	 */
	public void setObjectPackage(String objectPackage) {
		this.objectPackage = objectPackage;
	}
	/**
	 * @return dao package
	 */
	public String getDaoPackage() {
		return this.daoPackage;
	}
	/**
	 * @param daoPackage
	 */
	public void setDaoPackage(String daoPackage) {
		this.daoPackage = daoPackage;
	}
	/**
	 * @return enum package
	 */
	public String getEnumPackage() {
		return this.enumPackage;
	}
	/**
	 * @param enumPackage
	 */
	public void setEnumPackage(String enumPackage) {
		this.enumPackage = enumPackage;
	}
	/**
	 * @return factory package
	 */
	public String getFactoryPackage() {
		return this.factoryPackage;
	}
	/**
	 * @param factoryPackage
	 */
	public void setFactoryPackage(String factoryPackage) {
		this.factoryPackage = factoryPackage;
	}
	/**
	 * @return data package
	 */
	public String getDataPackage() {
		return this.dataPackage;
	}
	/**
	 * @param dataPackage
	 */
	public void setDataPackage(String dataPackage) {
		this.dataPackage = dataPackage;
	}
	/**
	 * @return daoimpl
	 */
	public String getDaoImplPackage() {
		return this.daoImplPackage;
	}
	/**
	 * @param daoImplPackage
	 */
	public void setDaoImplPackage(String daoImplPackage) {
		this.daoImplPackage = daoImplPackage;
	}
	/**
	 * @return the objectInterfacePackage
	 */
	public final String getObjectInterfacePackage() {
		return this.objectInterfacePackage;
	}
	/**
	 * @param objectInterfacePackage the objectInterfacePackage to set
	 */
	public final void setObjectInterfacePackage(String objectInterfacePackage) {
		this.objectInterfacePackage = objectInterfacePackage;
	}




    /**
     * Gets enum subtype package
     *
     * @return enum subtype packge
     */
    public String getEnumSubtypePackage() {
        return this.enumSubtypePackage;
    }



    /**
     * Sets enum subtype package.
     *
     * @param enumSubtypePackage
     */
    public void setEnumSubtypePackage(String enumSubtypePackage) {
        this.enumSubtypePackage = enumSubtypePackage;
    }
	/**
	 * @return the objectTableRepoPackage
	 */
	public String getObjectTableRepoPackage() {
		return objectTableRepoPackage;
	}
	/**
	 * @param objectTableRepoPackage the objectTableRepoPackage to set
	 */
	public void setObjectTableRepoPackage(String objectTableRepoPackage) {
		this.objectTableRepoPackage = objectTableRepoPackage;
	}
	/**
	 * @return the utilPackage
	 */
	public String getUtilPackage() {
		return utilPackage;
	}
	/**
	 * @param utilPackage the utilPackage to set
	 */
	public void setUtilPackage(String utilPackage) {
		this.utilPackage = utilPackage;
	}

}
