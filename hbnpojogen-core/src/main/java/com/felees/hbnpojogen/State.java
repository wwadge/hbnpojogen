package com.felees.hbnpojogen;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

import org.jvnet.inflector.RuleBasedPluralizer;

import com.felees.hbnpojogen.SyncUtils.CommitResults;
import com.felees.hbnpojogen.db.TableObj;
import com.felees.hbnpojogen.obj.Clazz;



/**
 * State class.
 *
 * @author wallacew
 *
 */
public class State
implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -3897252366514332962L;
	/** config */
	public String dbType;
	/** config */
	public String dbIP;
	/** config */
	public String dbCatalog;
	/** config */
	public String dbSchema;
	/** config */
	public String dbUsername;
	/** config */
	public String dbPassword;
	/** config */
	public String topLevel;
	/** config */
	public String libPath;
	/** config */
	public String projectName;
	/** config */
	private String sourceTarget;
	/** Catalogs. */
	public TreeSet<String> catalogs = new TreeSet<String>(new CaseInsensitiveComparator());
	public TreeSet<String> schemas = new TreeSet<String>(new CaseInsensitiveComparator());
	/** Classes will be placed here. */
	public TreeMap<String, Clazz> classes = new TreeMap<String, Clazz>(new CaseInsensitiveComparator());
	/** Key = classname, value=<propertyName, Annotations> */
	public TreeMap<String, TreeMap<String, CustomAnnotations>> customAnnotations =
		new TreeMap<String, TreeMap<String, CustomAnnotations>>(new CaseInsensitiveComparator());
	/** Key = classname, value=Annotation text */
	public TreeMap<String, String> customClassAnnotations = new TreeMap<String, String>(new CaseInsensitiveComparator());

	public TreeMap<String, String> getClassTypeDefsAnnotations() {

		return classTypeDefsAnnotations;
	}

	public void setClassTypeDefsAnnotations(TreeMap<String, String> classTypeDefsAnnotations) {
		this.classTypeDefsAnnotations = classTypeDefsAnnotations;
	}

	/** Key = classname, value=Annotation text */
	public TreeMap<String, String> classTypeDefsAnnotations = new TreeMap<String, String>(new CaseInsensitiveComparator());
	/** Key = classname, value=extra class code */
	public TreeMap<String, String> customClassCode = new TreeMap<String, String>(new CaseInsensitiveComparator());
	/** Key = classname, value=extra class code */
	public TreeMap<String, String> customClassCodeFields = new TreeMap<String, String>(new CaseInsensitiveComparator());

    /** Key = classname, value=imports */
    public TreeMap<String, TreeSet<String>> customClassImports = new TreeMap<String, TreeSet<String>>(new CaseInsensitiveComparator());
    /** Key = classname, value=interface */
    public TreeMap<String, TreeSet<String>> customClassInterfaces = new TreeMap<String, TreeSet<String>>(new CaseInsensitiveComparator());
    /** Key = classname, value=extends */
    public TreeMap<String, TreeSet<String>> customClassExtends = new TreeMap<String, TreeSet<String>>(new CaseInsensitiveComparator());
  	/** Holds info re link tables. Key = table name, value = (srcField, [set of destFields]) */
	public TreeMap<String, TreeMap<String, TreeSet<String>>> linkTables =
		new TreeMap<String, TreeMap<String, TreeSet<String>>>(new CaseInsensitiveComparator());
	/** Holds info re one-to-one tables. Key = table name, value = [set of Fields] */
	public TreeMap<String, TreeSet<String>> oneToOneTables = new TreeMap<String, TreeSet<String>>(new CaseInsensitiveComparator());
	/** Holds info re cyclicTableExclusionList tables. Key = table name, value = [set of Fields] */
	public TreeMap<String, TreeMap<String, String>> cyclicTableExclusionListTables =
		new TreeMap<String, TreeMap<String, String>>(new CaseInsensitiveComparator());
	/** rename field section. Key = schema, value = <src table.fieldname, (new name:inversename)>> */
	public TreeMap<String, TreeMap<String, String>> renameFieldMap = new TreeMap<String, TreeMap<String, String>>(new CaseInsensitiveComparator());
	/** enum treat as links map. Key = schema, value = <src table.fieldname, dst table.fieldname>> */
	public TreeMap<String, TreeMap<String, EnumMapping>> enumAsLinkMaps = new TreeMap<String, TreeMap<String, EnumMapping>>();
	/** Holds info re natural keys tables. Key = table name, value = natural key */
	TreeMap<String, TreeSet<String>> naturalKeys = new TreeMap<String, TreeSet<String>>(new CaseInsensitiveComparator());
	/** config */
	public TreeMap<String, TreeMap<String, String>> defaultTestValues = new TreeMap<String, TreeMap<String, String>>(new CaseInsensitiveComparator());
	   /** config */
    public TreeMap<String, String> testDataPools = new TreeMap<String, String>(new CaseInsensitiveComparator());
	/** config */
	public TreeSet<String> preventClean = new TreeSet<String>(new CaseInsensitiveComparator());
	/** List of classes to be generated as ABSTRACT -- chrisp */
    public TreeSet<String> abstractTables = new TreeSet<String>(new CaseInsensitiveComparator());
    /** List of classes to be generated as immutable. Key = table, value=generate static flag */
    private Map<String, Boolean> immutableTables = new TreeMap<String, Boolean>(new CaseInsensitiveComparator());

	private Set<String> equalityExcludes = new HashSet<String>();
	/** set of transient fields. */
	private Set<String> transientFields = new HashSet<String>();
	/** set of transient fields. */
	private Set<String> moneyFields = new HashSet<String>();

	/** set of transient fields. */
	private Set<String> currencyFields = new HashSet<String>();
//	private Set<String> encryptedFields = new HashSet<String>();
	/** prepopulate stuff */
	public LinkedList<CustomDB> prepopulateList = new LinkedList<CustomDB>();
	/** prepopulate stuff */
	public LinkedList<CustomDB> preExecList = new LinkedList<CustomDB>();
	/** Tables to ignore during generation */
    public TreeSet<String> ignoreTableList = new TreeSet<String>(new CaseInsensitiveComparator());
    /** Fields to encrypt. Key = field, value = exception list */
    public TreeMap<String, List<String>> encryptList = new TreeMap<String, List<String>>(new CaseInsensitiveComparator());
    /** Fields to ignore during generation. Key = field, value = exception list */
    public TreeMap<String, List<String>> ignoreFieldList = new TreeMap<String, List<String>>(new CaseInsensitiveComparator());
    /** Ignore-except list */
    public TreeSet<String> ignoreEverythingExceptList = new TreeSet<String>(new CaseInsensitiveComparator());
    /** Schemas that should not be written out to disk. */
	public TreeSet<String> noOutPutForSchemaList = new TreeSet<String>(new CaseInsensitiveComparator());
	/** Schemas that should be written out to disk. */
	public TreeSet<String> noOutPutForExceptSchemaList = new TreeSet<String>(new CaseInsensitiveComparator());

	/** Table representation of the DB */
	public TreeMap<String, PackageMap> packageMaps = new TreeMap<String, PackageMap>(new CaseInsensitiveComparator());
	/** Table representation of the DB */
	public TreeMap<String, TableObj> tables = new TreeMap<String, TableObj>(new CaseInsensitiveComparator());
	/** Map for ID generators. Key = schema, Value = tables */
	public TreeMap<String, GeneratedValueSchemas> generators = new TreeMap<String, GeneratedValueSchemas>(new CaseInsensitiveComparator());

       /**
     * Key = schema name, Value =
     * <table.field, new enum name>
     */
	public TreeMap<String, TreeMap<String, String>> enumMappings = new TreeMap<String, TreeMap<String, String>>(new CaseInsensitiveComparator());
	/** Which schema generation to use */
	public Integer schemaRestrict = -1;
	/** The commit result */
	public CommitResults commitResult;
	/** If true, don't do underscore conversion to follow Java rules */
	public boolean disableUnderscoreConversion = false;
    /** If true, don't write code to delete tables in the JUnit test */
    public boolean disableCleanTables = false;
    /** If true, don't write code to generate subtype enums. */
    public boolean disableSubtypeEnumGeneration = false;
	/** Prevent OneToMany renaming to follow nice english rules */
	public boolean disableEnglishPlural = false;
	/** If true, daotest will not rollback after each test. */
	public boolean disableTestRollback = false;
	/** If true, hibernate pojo gen state will be dumped to disk. */
	public boolean enableStateSave = false;

	/** If true, jackson annotations will be added to allow for serialization */
	public boolean enableJacksonSupport = false;
	/** If true, generate mockito context file. */
	public boolean enableMockitoBeans = true;
	public String mockitoFilename;
	/** 0=MySQL, 1=MSSQL, 2=POSTGRESQL */
	public int dbMode = 0;
	/** Inner handle. */
	private String applicationContextFilename;
	private boolean disableApplicationContext;
	/** Inner handle. */
    private String sessionFactoryItems;
    /** Inner handle. */
    private String transactionManagerItems;
    /** Inner handle. */
    private String additionalContextItems;
    /** Spring version to use. */
    private Integer springVersion;
		/** Eg <toplevel>/src */
	private String srcFolder;
	/** Eg <toplevel>/test/unit */
	private String testFolder;
	private String daoCustomContextConfig;
	/** Eg <toplevel>/test/unit */
	private String resourceFolder;
	/** Eg <toplevel>/test/unit */
	private String testResourceFolder;

	/** Clean DB order. Only filled after writing test out. Used for dump state. */
	private LinkedList<VelocityTable> cleanDbTables;
	/** Table cycles for cleaning DB. */
	private LinkedList<LinkedList<Clazz>> tableSetCycles;
	/** Instance handle.*/
	private static State instance = null;
	public static String customDialect;
	/** C3P0, BoneCP, ... */
    private String connectionPool;


    /** enable joda-time */
    public boolean enableJodaSupport;
    public boolean enableJDK8Support;
       public boolean enableSpringData;
    public String springDataFactoryClass="";
    public String springDataRepoInterface=" org.springframework.data.jpa.repository.JpaRepository";

	/** enable validator. */
	public boolean enableHibernateValidator;
	/** disable backlinks in data pool factory */
    public boolean disableBackLinksInDataPoolFactory;
    /** disable backlinks in data pool factory */
    public boolean disableLazyConnections;
    /** disable backlinks in data pool factory */
    public boolean enablePropertyPlaceholderConfigurer;
    /** prefix */
    public String propertyPlaceholderConfigurerPrefix;
    public boolean propertyPlaceholderConfigurerSuppressBean;
	/** key = fromtable, value = TreeMap<ToTable string, fromField list>> */
    private TreeMap<String, TreeMap<String, TreeSet<String>>> disableBackLinkTables = new TreeMap<String, TreeMap<String, TreeSet<String>>>(new CaseInsensitiveComparator());
    /** key = fromtable, value = TreeMap<ToTable string, fromField list>> */
    private TreeMap<String, TreeMap<String, TreeSet<String>>> noFollowTables = new TreeMap<String, TreeMap<String, TreeSet<String>>>(new CaseInsensitiveComparator());
    /** key = table, value = fields */
    private TreeMap<String, TreeSet<String>> uniqueKeys  = new TreeMap<String, TreeSet<String>>(new CaseInsensitiveComparator());

	/** If true, we'll perform a version check during startup. */
	private boolean versionCheckEnabled = false;
	/** If true, entries listed in the versionCheck map are disabled (exception list). */
	private boolean versionCheckDefaultEnabled = false;
	/** key = schema, value = orderClause. */
    private TreeMap<String, String> versionCheckOrderBy = new TreeMap<String, String>(new CaseInsensitiveComparator());
	/** key = schema, value = whereClause. */
    private TreeMap<String, String> versionCheckWhereClause = new TreeMap<String, String>(new CaseInsensitiveComparator());
	/** key = schema, value = tableName, list of fields to include in check. */
	public TreeMap<String, ObjectPair<String, Set<String>>> versionCheck= new TreeMap<String, ObjectPair<String, Set<String>>>(new CaseInsensitiveComparator());
		/** key = package.class.property, value = enabled/disabled. */
	public TreeMap<String, CascadeState> oneToManyCascadeEnabled = new TreeMap<String, CascadeState>(new CaseInsensitiveComparator());
	/** key = package.class.property, value = enabled/disabled. */
	public TreeMap<String, CascadeState> manyToOneCascadeEnabled = new TreeMap<String, CascadeState>(new CaseInsensitiveComparator());
	/** key = package.class.property, value = enabled/disabled. */
	public TreeMap<String, CascadeState> manyToManyCascadeEnabled = new TreeMap<String, CascadeState>(new CaseInsensitiveComparator());
	/** key = package.class.property, value = enabled/disabled. */
	public TreeMap<String, CascadeState> OneToOneCascadeEnabled = new TreeMap<String, CascadeState>(new CaseInsensitiveComparator());
	/** key = package.class.property, value = enabled/disabled. */
	public TreeMap<String, Boolean> oneToManyLazyEnabled = new TreeMap<String, Boolean>(new CaseInsensitiveComparator());
	/** key = package.class.property, value = enabled/disabled. */
	public TreeMap<String, Boolean> manyToOneLazyEnabled = new TreeMap<String, Boolean>(new CaseInsensitiveComparator());
	/** key = package.class.property, value = enabled/disabled. */
	public TreeMap<String, Boolean> manyToManyLazyEnabled = new TreeMap<String, Boolean>(new CaseInsensitiveComparator());
	/** key = package.class.property, value = enabled/disabled. */
	public TreeMap<String, Boolean> OneToOneLazyEnabled = new TreeMap<String, Boolean>(new CaseInsensitiveComparator());
	/** key = packageName.className, Value = Clazz. convenience mapping only. **/
	public TreeMap<String, Clazz> packageClazzMap = null;
	/** key = schema.dbTable, value = list of list of values (1 per row with each row having multiple columns)> read during code gen. */
	public TreeMap<String, List<List<String>>> versionsRead = new TreeMap<String, List<List<String>>>();
	/** key = schema.dbTable, value = Column names. */
	public TreeMap<String, List<String>> versionColumnsRead = new TreeMap<String, List<String>>();
	/** key = schema.dbTable, value = set of version column names marked with >= */
	public TreeMap<String, Set<String>> versionGTE = new TreeMap<String, Set<String>>();
	/** Maven setting.  */
    private String mavenDistributionManagement = "";
    /** Maven setting.  */
    private String mavenAdditionalPomEntries = " ";
    /** Maven setting.  */
	private String mavenVersion = "1.0";
	/** Maven setting.  */
	private String mavenJavaVersion = "1.7";
	/** Maven setting.  */
	private String mavenName = "";
	/** Maven setting.  */
	private String mavenDependency = "";
	/** Maven setting.  */
	private String mavenArtifactId = "";
	/** Maven setting.  */
	private String mavenGroupId = "";
	/** Key = package/classname, value = suffix */
    public TreeMap<String, String> classSuffixes = new TreeMap<String, String>();
	/** Key = table, value = {Pattern, enabled} */
    public TreeMap<String, FakeFKPattern> fakeFK = new TreeMap<String, FakeFKPattern>();
    private TreeMap<String, HashSet<RelationItem>>  fakeFKmatched= new TreeMap<String, HashSet<RelationItem>> ();

    private String ldapServer;
    private String ldapBase;
    private String ldapCn;

    private String customCurrencyUnitType;
    private String customMoneyType;

	public TreeMap<String, HashSet<RelationItem>> getFakeFKmatched() {
		return fakeFKmatched;
	}



	public void setFakeFKmatched(TreeMap<String, HashSet<RelationItem>>  fakeFKmatched) {
		this.fakeFKmatched = fakeFKmatched;
	}



	public TreeMap<String, FakeFKPattern> getFakeFK() {
		return this.fakeFK;
	}



	public void setFakeFK(TreeMap<String, FakeFKPattern> fakeFK) {
		this.fakeFK = fakeFK;
	}

	public String getMavenArtifactId() {
		return this.mavenArtifactId;
	}



	public void setMavenArtifactId(String mavenArtifactId) {
		this.mavenArtifactId = mavenArtifactId;
	}



	/**
	 * @return the mavenDependency
	 */
	public final String getMavenDependency() {
		return this.mavenDependency;
	}



	/**
	 * @param mavenDependency the mavenDependency to set
	 */
	public final void setMavenDependency(String mavenDependency) {
		this.mavenDependency = mavenDependency;
	}



	/**
	 * @return the mavenName
	 */
	public final String getMavenName() {
		return this.mavenName;
	}



	/**
	 * @param mavenName the mavenName to set
	 */
	public final void setMavenName(String mavenName) {
		this.mavenName = mavenName;
	}



	/**
	 * @return the mavenVersion
	 */
	public final String getMavenVersion() {
		return this.mavenVersion;
	}



	/**
	 * @param mavenVersion the mavenVersion to set
	 */
	public final void setMavenVersion(String mavenVersion) {
		this.mavenVersion = mavenVersion;
	}



	/** Maven setting. */
	private boolean mavenEnabled = false;
	private boolean mavenPomEnabled = true;
    private RuleBasedPluralizer customPluralizer;
	private boolean mavenArtifactVersionsDisabled;
	private boolean useLDAP;
	private boolean useLDAPImport;
	private String synchronizerVersion;
	private boolean useDynamicLDAPDataSource;
	private boolean mavenNoDeps;
	private boolean mavenUseExternalLib;
	private boolean skipModelInterfaces;


	/**
	 * @return the skipModelInterfaces
	 */
	public boolean isSkipModelInterfaces() {
		return skipModelInterfaces;
	}


	/**
	 * @return the mavenEnabled
	 */
	public final boolean isMavenEnabled() {
		return this.mavenEnabled;
	}



	/**
	 * @param mavenEnabled the mavenEnabled to set
	 */
	public final void setMavenEnabled(boolean mavenEnabled) {
		this.mavenEnabled = mavenEnabled;
	}



	/**
	 * @return the mavenDistributionManagement
	 */
	public final String getMavenDistributionManagement() {
		return this.mavenDistributionManagement;
	}



	/**
	 * @param mavenDistributionManagement the mavenDistributionManagement to set
	 */
	public final void setMavenDistributionManagement(
			String mavenDistributionManagement) {
		this.mavenDistributionManagement = mavenDistributionManagement;
	}






	/**
	 * @return the packageClazzMap
	 */
	public final TreeMap<String, Clazz> getPackageClazzMap() {
		if (packageClazzMap == null) {
			// lazily build a map
            packageClazzMap = new TreeMap<String, Clazz>(new CaseInsensitiveComparator());
			for (Clazz clazz: classes.values()){
				packageClazzMap.put(clazz.getPackageNameAbbrvPlusClassName(), clazz);
			}
		}
		return packageClazzMap;
	}



	/**
	 * @param packageClazzMap the packageClazzMap to set
	 */
	public final void setPackageClazzMap(TreeMap<String, Clazz> packageClazzMap) {
		this.packageClazzMap = packageClazzMap;
	}



	/**
	 * @return instance
	 */
	public static synchronized State getInstance() {
		if (instance == null) {
			instance = new State();
		}
		return instance;
	}

	/** DFSv2 requires multiple state files. Unfortunately, the generator
	 * was never designed for this. Therefore we use this method
	 * to switch to the "active" state at runtime
	 * @param state to set
	 */
	public static synchronized void setInstance(State state){
		instance = state;
	}



	/**
	 * @return the oneToOneTables
	 */
	public final TreeMap<String, TreeSet<String>> getOneToOneTables() {
		return this.oneToOneTables;
	}



	/**
	 * @param oneToOneTables the oneToOneTables to set
	 */
	public final void setOneToOneTables(TreeMap<String, TreeSet<String>> oneToOneTables) {
		this.oneToOneTables = oneToOneTables;
	}



	/**
	 * @return the cyclicTableExclusionListTables - chrisp
	 */
	public final TreeMap<String, TreeMap<String, String>> getCyclicTableExclusionListTables() {
		return this.cyclicTableExclusionListTables;
	}



	/**
	 * @param cyclicTableExclusionListTables the cyclicTableExclusionListTables to set - chrisp
	 */
	public final void setCyclicTableExclusionListTables(TreeMap<String, TreeMap<String, String>> cyclicTableExclusionListTables) {
		this.cyclicTableExclusionListTables = cyclicTableExclusionListTables;
	}



	/**
	 * Return link tables.
	 *
	 * @return link tables
	 */
	public TreeMap<String, TreeMap<String, TreeSet<String>>> getLinkTables() {
		return linkTables;
	}



	/**
	 * @param tableName
	 * @return object path
	 */
	public String doObjectImport(String tableName) {
		String catalog = SyncUtils.getTableCatalog(tableName);
		String name = SyncUtils.getTableName(tableName);
		return doObjectImport(catalog, name);
	}

	public boolean isEnableJacksonSupport() {
		return enableJacksonSupport;
	}

	public void setEnableJacksonSupport(boolean enableJacksonSupport) {
		this.enableJacksonSupport = enableJacksonSupport;
	}



	/**
	 * @param catalog
	 * @param tableName
	 * @return object path
	 */
	public String doObjectImport(String catalog, String tableName) {
		String cat = SyncUtils.removeUnderscores(catalog);
		String name = SyncUtils.upfirstChar(SyncUtils.removeUnderscores(tableName));
		return SyncUtils.getConfigPackage(cat, PackageTypeEnum.OBJECT) + "." + name;
	}



	/**
	 * @param filename String - The filename of the file it is saved to.
	 * @throws IOException error
	 */
	public void serializeState(String filename)
	throws IOException {
		ObjectOutputStream objstream = new ObjectOutputStream(new FileOutputStream(filename));
		objstream.writeObject(this);
		objstream.close();
	}



	/**
	 * @param filename String - The jar containing the serialization file to be loaded
	 * @return State object
	 * @throws Exception
	 */
	public State loadState(String uriResource)
	throws Exception {
		URI tmp;
		if (! (uriResource.toUpperCase().startsWith("HTTP") || uriResource.toUpperCase().startsWith("FTP"))){
			tmp = new URI("file:///"+uriResource);
		}
        else {
            tmp = new URI(uriResource);
        }
		JarInputStream zw = new JarInputStream(tmp.toURL().openStream());
		ZipEntry ze = zw.getNextEntry();
		Object obj = null;
		while (ze != null) {
			if (ze.getName().equalsIgnoreCase("SYNCHRONIZER.STATE")){
				ObjectInputStream objstream = new ObjectInputStream(zw);
				obj = objstream.readObject();
				objstream.close();
				break;
			}
			ze = zw.getNextEntry();
		}
		zw.close();
		return (State) obj;
	}


	/**
	 * @return the enableStateSave
	 */
	public final boolean isEnableStateSave() {
		return this.enableStateSave;
	}



	/**
	 * @param enableStateSave the enableStateSave to set
	 */
	public final void setEnableStateSave(boolean enableStateSave) {
		this.enableStateSave = enableStateSave;
	}



	/**
	 * @return the dbType
	 */
	public final String getDbType() {
		return dbType;
	}



	/**
	 * @param dbType the dbType to set
	 */
	public final void setDbType(String dbType) {
		this.dbType = dbType;
	}



	/**
	 * @return the dbIP
	 */
	public final String getDbIP() {
		return dbIP;
	}



	/**
	 * @param dbIP the dbIP to set
	 */
	public final void setDbIP(String dbIP) {
		this.dbIP = dbIP;
	}



	/**
	 * @return the dbCatalog
	 */
	public final String getDbCatalog() {
		return dbCatalog;
	}



	/**
	 * @param dbCatalog the dbCatalog to set
	 */
	public final void setDbCatalog(String dbCatalog) {
		this.dbCatalog = dbCatalog;
	}



	/**
	 * @return the dbUsername
	 */
	public final String getDbUsername() {
		return dbUsername;
	}



	/**
	 * @param dbUsername the dbUsername to set
	 */
	public final void setDbUsername(String dbUsername) {
		this.dbUsername = dbUsername;
	}



	/**
	 * @return the dbPassword
	 */
	public final String getDbPassword() {
		return dbPassword;
	}



	/**
	 * @param dbPassword the dbPassword to set
	 */
	public final void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}



	/**
	 * @return the topLevel
	 */
	public final String getTopLevel() {
		return topLevel;
	}



	/**
	 * @param topLevel the topLevel to set
	 */
	public final void setTopLevel(String topLevel) {
		this.topLevel = topLevel;
	}



	/**
	 * @return the libPath
	 */
	public final String getLibPath() {
		return libPath;
	}



	/**
	 * @param libPath the libPath to set
	 */
	public final void setLibPath(String libPath) {
		this.libPath = libPath;
	}



	/**
	 * @return the projectName
	 */
	public final String getProjectName() {
		return projectName;
	}



	/**
	 * @param projectName the projectName to set
	 */
	public final void setProjectName(String projectName) {
		this.projectName = projectName;
	}



	/**
	 * @return the catalogs
	 */
	public final TreeSet<String> getCatalogs() {
		return catalogs;
	}



	/**
	 * @param catalogs the catalogs to set
	 */
	public final void setCatalogs(TreeSet<String> catalogs) {
		this.catalogs = catalogs;
	}



	/**
	 * @return the classes
	 */
	public final TreeMap<String, Clazz> getClasses() {
		return classes;
	}



	/**
	 * @param classes the classes to set
	 */
	public final void setClasses(TreeMap<String, Clazz> classes) {
		this.classes = classes;
	}



	/**
	 * @return the customAnnotations
	 */
	public final TreeMap<String, TreeMap<String, CustomAnnotations>> getCustomAnnotations() {
		return customAnnotations;
	}



	/**
	 * @param customAnnotations the customAnnotations to set
	 */
	public final void setCustomAnnotations(TreeMap<String, TreeMap<String, CustomAnnotations>> customAnnotations) {
		this.customAnnotations = customAnnotations;
	}



	/**
	 * @return the customClassAnnotations
	 */
	public final TreeMap<String, String> getCustomClassAnnotations() {
		return customClassAnnotations;
	}



	/**
	 * @param customClassAnnotations the customClassAnnotations to set
	 */
	public final void setCustomClassAnnotations(TreeMap<String, String> customClassAnnotations) {
		this.customClassAnnotations = customClassAnnotations;
	}



	/**
	 * @return the naturalKeys
	 */
	public final TreeMap<String, TreeSet<String>> getNaturalKeys() {
		return naturalKeys;
	}



	/**
	 * @param naturalKeys the naturalKeys to set
	 */
	public final void setNaturalKeys(TreeMap<String, TreeSet<String>> naturalKeys) {
		this.naturalKeys = naturalKeys;
	}



	/**
	 * @return the defaultTestValues
	 */
	public final TreeMap<String, TreeMap<String, String>> getDefaultTestValues() {
		return defaultTestValues;
	}



	/**
	 * @param defaultTestValues the defaultTestValues to set
	 */
	public final void setDefaultTestValues(TreeMap<String, TreeMap<String, String>> defaultTestValues) {
		this.defaultTestValues = defaultTestValues;
	}



	/**
	 * @return the preventClean
	 */
	public final TreeSet<String> getPreventClean() {
		return preventClean;
	}



	/**
	 * @param preventClean the preventClean to set
	 */
	public final void setPreventClean(TreeSet<String> preventClean) {
		this.preventClean = preventClean;
	}



	/**
	 * @return the abstractTables
	 */
	public final TreeSet<String> getAbstractTables() {
		return abstractTables;
	}



	/**
	 * @param abstractTables the abstractTables to set
	 */
	public final void setAbstractTables(TreeSet<String> abstractTables) {
		this.abstractTables = abstractTables;
	}



	/**
	 * @return the prepopulateList
	 */
	public final LinkedList<CustomDB> getPrepopulateList() {
		return prepopulateList;
	}



	/**
	 * @param prepopulateList the prepopulateList to set
	 */
	public final void setPrepopulateList(LinkedList<CustomDB> prepopulateList) {
		this.prepopulateList = prepopulateList;
	}



	/**
	 * @return the preExecList
	 */
	public final LinkedList<CustomDB> getPreExecList() {
		return preExecList;
	}



	/**
	 * @param preExecList the preExecList to set
	 */
	public final void setPreExecList(LinkedList<CustomDB> preExecList) {
		this.preExecList = preExecList;
	}



	/**
	 * @return the ignoreTableList
	 */
	public final TreeSet<String> getIgnoreTableList() {
		return ignoreTableList;
	}



	/**
	 * @param ignoreTableList the ignoreTableList to set
	 */
	public final void setIgnoreTableList(TreeSet<String> ignoreTableList) {
		this.ignoreTableList = ignoreTableList;
	}



	/**
	 * @return the packageMaps
	 */
	public final TreeMap<String, PackageMap> getPackageMaps() {
		return packageMaps;
	}



	/**
	 * @param packageMaps the packageMaps to set
	 */
	public final void setPackageMaps(TreeMap<String, PackageMap> packageMaps) {
		this.packageMaps = packageMaps;
	}



	/**
	 * @return the tables
	 */
	public final TreeMap<String, TableObj> getTables() {
		return tables;
	}



	/**
	 * @param tables the tables to set
	 */
	public final void setTables(TreeMap<String, TableObj> tables) {
		this.tables = tables;
	}



	/**
	 * @return the generators
	 */
	public final TreeMap<String, GeneratedValueSchemas> getGenerators() {
		return generators;
	}



	/**
	 * @param generators the generators to set
	 */
	public final void setGenerators(TreeMap<String, GeneratedValueSchemas> generators) {
		this.generators = generators;
	}



	/**
	 * @return the enumMappings
	 */
	public final TreeMap<String, TreeMap<String, String>> getEnumMappings() {
		return enumMappings;
	}



	/**
	 * @param enumMappings the enumMappings to set
	 */
	public final void setEnumMappings(TreeMap<String, TreeMap<String, String>> enumMappings) {
		this.enumMappings = enumMappings;
	}



	/**
	 * @return the schemaRestrict
	 */
	public final Integer getSchemaRestrict() {
		return schemaRestrict;
	}



	/**
	 * @param schemaRestrict the schemaRestrict to set
	 */
	public final void setSchemaRestrict(Integer schemaRestrict) {
		this.schemaRestrict = schemaRestrict;
	}



	/**
	 * @return the commitResult
	 */
	public final CommitResults getCommitResult() {
		return commitResult;
	}



	/**
	 * @param commitResult the commitResult to set
	 */
	public final void setCommitResult(CommitResults commitResult) {
		this.commitResult = commitResult;
	}



	/**
	 * @return the disableUnderscoreConversion
	 */
	public final boolean isDisableUnderscoreConversion() {
		return disableUnderscoreConversion;
	}



	/**
	 * @param disableUnderscoreConversion the disableUnderscoreConversion to set
	 */
	public final void setDisableUnderscoreConversion(boolean disableUnderscoreConversion) {
		this.disableUnderscoreConversion = disableUnderscoreConversion;
	}



	/**
	 * @return the disableCleanTables
	 */
	public final boolean isDisableCleanTables() {
		return disableCleanTables;
	}



	/**
	 * @param disableCleanTables the disableCleanTables to set
	 */
	public final void setDisableCleanTables(boolean disableCleanTables) {
		this.disableCleanTables = disableCleanTables;
	}



	/**
	 * @return the disableEnglishPlural
	 */
	public final boolean isDisableEnglishPlural() {
		return disableEnglishPlural;
	}



	/**
	 * @param disableEnglishPlural the disableEnglishPlural to set
	 */
	public final void setDisableEnglishPlural(boolean disableEnglishPlural) {
		this.disableEnglishPlural = disableEnglishPlural;
	}



	/**
	 * @return the disableTestRollback
	 */
	public final boolean isDisableTestRollback() {
		return disableTestRollback;
	}



	/**
	 * @param disableTestRollback the disableTestRollback to set
	 */
	public final void setDisableTestRollback(boolean disableTestRollback) {
		this.disableTestRollback = disableTestRollback;
	}



	/**
	 * @return the dbMode
	 */
	public final int getDbMode() {
		return dbMode;
	}



	/**
	 * @param dbMode the dbMode to set
	 */
	public final void setDbMode(int dbMode) {
		this.dbMode = dbMode;
	}



	/**
	 * @param linkTables the linkTables to set
	 */
	public final void setLinkTables(TreeMap<String, TreeMap<String, TreeSet<String>>> linkTables) {
		this.linkTables = linkTables;
	}



	/**
	 * @return the applicationContextFilename
	 */
	public final String getApplicationContextFilename() {
		return applicationContextFilename;
	}



	/**
	 * @param applicationContextFilename the applicationContextFilename to set
	 */
	public final void setApplicationContextFilename(
			String applicationContextFilename) {
		this.applicationContextFilename = applicationContextFilename;
	}



	/**
	 * @return the cleanDbTables
	 */
	public final LinkedList<VelocityTable> getCleanDbTables() {
		return cleanDbTables;
	}



	/**
	 * @param cleanDbTables the cleanDbTables to set
	 */
	public final void setCleanDbTables(LinkedList<VelocityTable> cleanDbTables) {
		this.cleanDbTables = cleanDbTables;
	}



	/**
	 * @return the tableSetCycles
	 */
	public final LinkedList<LinkedList<Clazz>> getTableSetCycles() {
		return tableSetCycles;
	}



	/**
	 * @param tableSetCycles the tableSetCycles to set
	 */
	public final void setTableSetCycles(LinkedList<LinkedList<Clazz>> tableSetCycles) {
		this.tableSetCycles = tableSetCycles;
	}



	/**
	 * @return the noOutPutForSchemaList
	 */
	public final TreeSet<String> getNoOutPutForSchemaList() {
		return noOutPutForSchemaList;
	}



	/**
	 * @param noOutPutForSchemaList the noOutPutForSchemaList to set
	 */
	public final void setNoOutPutForSchemaList(TreeSet<String> noOutPutForSchemaList) {
		this.noOutPutForSchemaList = noOutPutForSchemaList;
	}



	/**
	 * @return the customClassImports
	 */
	public final TreeMap<String, TreeSet<String>> getCustomClassImports() {
		return customClassImports;
	}



	/**
	 * @param customClassImports the customClassImports to set
	 */
	public final void setCustomClassImports(
			TreeMap<String, TreeSet<String>> customClassImports) {
		this.customClassImports = customClassImports;
	}



	/**
	 * @return the disableBackLinkTables
	 */
	public final TreeMap<String, TreeMap<String, TreeSet<String>>> getDisableBackLinkTables() {
		return this.disableBackLinkTables;
	}



	/**
	 * @param disableBackLinkTables the disableBackLinkTables to set
	 */
	public final void setDisableBackLinkTables(
			TreeMap<String, TreeMap<String, TreeSet<String>>> disableBackLinkTables) {
		this.disableBackLinkTables = disableBackLinkTables;
	}



	/**
	 * @return the enableHibernateValidator
	 */
	public  boolean isEnableHibernateValidator() {
		return this.enableHibernateValidator;
	}



	/**
	 * @param enableHibernateValidator the enableHibernateValidator to set
	 */
	public final void setEnableHibernateValidator(
			boolean enableHibernateValidator) {
		this.enableHibernateValidator = enableHibernateValidator;
	}



	/**
	 * @return the oneToManyCascadeEnabled
	 */
	public final TreeMap<String, CascadeState> getOneToManyCascadeEnabled() {
		return oneToManyCascadeEnabled;
	}



	/**
	 * @return the manyToOneCascadeEnabled
	 */
	public final TreeMap<String, CascadeState> getManyToOneCascadeEnabled() {
		return manyToOneCascadeEnabled;
	}



	/**
	 * @return the manyToManyCascadeEnabled
	 */
	public final TreeMap<String, CascadeState> getManyToManyCascadeEnabled() {
		return manyToManyCascadeEnabled;
	}



	/**
	 * @return the oneToOneCascadeEnabled
	 */
	public final TreeMap<String, CascadeState> getOneToOneCascadeEnabled() {
		return OneToOneCascadeEnabled;
	}



	/**
	 * @return the oneToManyLazyEnabled
	 */
	public final TreeMap<String, Boolean> getOneToManyLazyEnabled() {
		return oneToManyLazyEnabled;
	}



	/**
	 * @return the manyToOneLazyEnabled
	 */
	public final TreeMap<String, Boolean> getManyToOneLazyEnabled() {
		return manyToOneLazyEnabled;
	}



	/**
	 * @return the manyToManyLazyEnabled
	 */
	public final TreeMap<String, Boolean> getManyToManyLazyEnabled() {
		return manyToManyLazyEnabled;
	}



	/**
	 * @return the oneToOneLazyEnabled
	 */
	public final TreeMap<String, Boolean> getOneToOneLazyEnabled() {
		return OneToOneLazyEnabled;
	}



	/**
	 * @return the customClassCode
	 */
	public final TreeMap<String, String> getCustomClassCode() {
		return this.customClassCode;
	}



	/**
	 * @param customClassCode the customClassCode to set
	 */
	public final void setCustomClassCode(TreeMap<String, String> customClassCode) {
		this.customClassCode = customClassCode;
	}


	/**
	 * @return the srcFolder
	 */
	public final String getSrcFolder() {
		return this.srcFolder;
	}



	/**
	 * @param srcFolder the srcFolder to set
	 */
	public final void setSrcFolder(String srcFolder) {
		this.srcFolder = srcFolder;
	}



	/**
	 * @return the testFolder
	 */
	public final String getTestFolder() {
		return this.testFolder;
	}



	/**
	 * @param testFolder the testFolder to set
	 */
	public final void setTestFolder(String testFolder) {
		this.testFolder = testFolder;
	}



	/**
	 * @return the sourceTarget
	 */
	public final String getSourceTarget() {
		return this.sourceTarget;
	}



	/**
	 * @param sourceTarget the sourceTarget to set
	 */
	public final void setSourceTarget(String sourceTarget) {
		this.sourceTarget = sourceTarget;
	}

	/**
	 * @return the renameField
	 */
	public final TreeMap<String, TreeMap<String, String>> getRenameFieldMap() {
		return this.renameFieldMap;
	}


	/**
	 * @param renameField the renameField to set
	 */
	public final void setRenameFieldMap(
			TreeMap<String, TreeMap<String, String>> renameFieldMap) {
		this.renameFieldMap = renameFieldMap;
	}
	/**
	 * @return the versionCheckEnabled
	 */
	public boolean isVersionCheckEnabled() {
		return this.versionCheckEnabled;
	}



	/**
	 * @param versionCheckEnabled the versionCheckEnabled to set
	 */
	public void setVersionCheckEnabled(boolean versionCheckEnabled) {
		this.versionCheckEnabled = versionCheckEnabled;
	}

	/**
	 * @return the versionCheck
	 */
	public TreeMap<String, ObjectPair<String, Set<String>>> getVersionCheck() {
		return this.versionCheck;
	}



	/**
	 * @param versionCheck the versionCheck to set
	 */
	public void setVersionCheck(
			TreeMap<String, ObjectPair<String, Set<String>>> versionCheck) {
		this.versionCheck = versionCheck;
	}


	/**
	 * @return the versionCheckDefaultEnabled
	 */
	public final boolean isVersionCheckDefaultEnabled() {
		return versionCheckDefaultEnabled;
	}



	/**
	 * @param versionCheckDefaultEnabled the versionCheckDefaultEnabled to set
	 */
	public final void setVersionCheckDefaultEnabled(
			boolean versionCheckDefaultEnabled) {
		this.versionCheckDefaultEnabled = versionCheckDefaultEnabled;
	}



	public String getResourceFolder() {
		return this.resourceFolder;
	}



	public void setResourceFolder(String resourceFolder) {
		this.resourceFolder = resourceFolder;
	}



	public String getTestResourceFolder() {
		return this.testResourceFolder;
	}



	public void setTestResourceFolder(String testResourceFolder) {
		this.testResourceFolder = testResourceFolder;
	}



	public String getSessionFactoryItems() {
		return this.sessionFactoryItems;
	}



	public void setSessionFactoryItems(String sessionFactoryItems) {
		this.sessionFactoryItems = sessionFactoryItems;
	}



	public String getMavenGroupId() {
		return mavenGroupId;
	}



	public void setMavenGroupId(String mavenGroupId) {
		this.mavenGroupId = mavenGroupId;
	}




    /**
     * Gets
     *
     * @return
     */
    public TreeMap<String, TreeMap<String, EnumMapping>> getEnumAsLinkMaps() {
        return this.enumAsLinkMaps;
    }




    /**
     * Sets
     *
     * @param enumAsLinkMaps
     */
    public void setEnumAsLinkMaps(TreeMap<String, TreeMap<String, EnumMapping>> enumAsLinkMaps) {
        this.enumAsLinkMaps = enumAsLinkMaps;
    }




    /**
     * Gets
     *
     * @return
     */
    public TreeMap<String, TreeSet<String>> getCustomClassInterfaces() {
        return this.customClassInterfaces;
    }



    /**
     * Sets
     *
     * @param customClassInterfaces
     */
    public void setCustomClassInterfaces(TreeMap<String, TreeSet<String>> customClassInterfaces) {
        this.customClassInterfaces = customClassInterfaces;
    }


    /**
     * Gets
     *
     * @return
     */
    public boolean isDisableLazyConnections() {
        return this.disableLazyConnections;
    }



    /**
     * Sets
     *
     * @param disableLazyConnections
     */
    public void setDisableLazyConnections(boolean disableLazyConnections) {
        this.disableLazyConnections = disableLazyConnections;
    }



    /**
     * Gets
     *
     * @return
     */
    public String getTransactionManagerItems() {
        return this.transactionManagerItems;
    }




    /**
     * Sets
     *
     * @param transactionManagerItems
     */
    public void setTransactionManagerItems(String transactionManagerItems) {
        this.transactionManagerItems = transactionManagerItems;
    }




    /**
     * Gets
     *
     * @return
     */
    public String getAdditionalContextItems() {
        return this.additionalContextItems;
    }




    /**
     * Sets
     *
     * @param additionalContextItems
     */
    public void setAdditionalContextItems(String additionalContextItems) {
        this.additionalContextItems = additionalContextItems;
    }




    /**
     * Gets
     *
     * @return
     */
    public Set<String> getEqualityExcludes() {
        return this.equalityExcludes;
    }




    /**
     * Sets
     *
     * @param equalityExcludes
     */
    public void setEqualityExcludes(Set<String> equalityExcludes) {
        this.equalityExcludes = equalityExcludes;
    }




    /**
     * Gets
     *
     * @return
     */
    public boolean isEnableJodaSupport() {
        return this.enableJodaSupport;
    }




    /**
     * Sets
     *
     * @param enableJodaSupport
     */
    public void setEnableJodaSupport(boolean enableJodaSupport) {
        this.enableJodaSupport = enableJodaSupport;
    }




    /**
     * Gets
     *
     * @return
     */
    public String getMavenAdditionalPomEntries() {
        return this.mavenAdditionalPomEntries;
    }




    /**
     * Sets
     *
     * @param mavenAdditionalPomEntries
     */
    public void setMavenAdditionalPomEntries(String mavenAdditionalPomEntries) {
        this.mavenAdditionalPomEntries = mavenAdditionalPomEntries;
    }







    /**
     * Gets
     *
     * @return
     */
    public TreeMap<String, String> getTestDataPools() {
        return this.testDataPools;
    }




    /**
     * Sets
     *
     * @param testDataPools
     */
    public void setTestDataPools(TreeMap<String, String> testDataPools) {
        this.testDataPools = testDataPools;
    }




    /**
     * Gets
     *
     * @return
     */
    public Map<String, Boolean> getImmutableTables() {
        return this.immutableTables;
    }




    /**
     * Sets
     *
     * @param immutableTables
     */
    public void setImmutableTables(Map<String, Boolean> immutableTables) {
        this.immutableTables = immutableTables;
    }




    /**
     * Gets
     *
     * @return
     */
    public TreeMap<String, TreeMap<String, TreeSet<String>>> getNoFollowTables() {
        return this.noFollowTables;
    }




    /**
     * Sets
     *
     * @param noFollowTables
     */
    public void setNoFollowTables(TreeMap<String, TreeMap<String, TreeSet<String>>> noFollowTables) {
        this.noFollowTables = noFollowTables;
    }




    /**
     * Gets
     *
     * @return
     */
    public TreeMap<String, String> getClassSuffixes() {
        return this.classSuffixes;
    }




    /**
     * Sets
     *
     * @param classSuffixes
     */
    public void setClassSuffixes(TreeMap<String, String> classSuffixes) {
        this.classSuffixes = classSuffixes;
    }



    /**
     *
     *
     * @param ruleBasedPluralizer
     */
    public void setCustomPluralizer(RuleBasedPluralizer ruleBasedPluralizer) {
        this.customPluralizer = ruleBasedPluralizer;
    }




    /**
     * Gets
     *
     * @return
     */
    public RuleBasedPluralizer getCustomPluralizer() {
        return this.customPluralizer;
    }




    /**
     * Gets
     *
     * @return
     */
    public TreeSet<String> getIgnoreEverythingExceptList() {
        return this.ignoreEverythingExceptList;
    }




    /**
     * Sets
     *
     * @param ignoreEverythingExceptList
     */
    public void setIgnoreEverythingExceptList(TreeSet<String> ignoreEverythingExceptList) {
        this.ignoreEverythingExceptList = ignoreEverythingExceptList;
    }




    /**
     * Gets
     *
     * @return
     */
    public TreeMap<String, TreeSet<String>> getUniqueKeys() {
        return this.uniqueKeys;
    }




    /**
     * Sets
     *
     * @param uniqueKeys
     */
    public void setUniqueKeys(TreeMap<String, TreeSet<String>> uniqueKeys) {
        this.uniqueKeys = uniqueKeys;
    }




    /**
     * Gets
     *
     * @return
     */
    public String getConnectionPool() {
        return this.connectionPool;
    }




    /**
     * Sets
     *
     * @param connectionPool
     */
    public void setConnectionPool(String connectionPool) {
        this.connectionPool = connectionPool;
    }




    /**
     * Gets
     *
     * @return
     */
    public TreeMap<String, String> getVersionCheckWhereClause() {
        return this.versionCheckWhereClause;
    }




    /**
     * Sets
     *
     * @param versionCheckWhereClause
     */
    public void setVersionCheckWhereClause(TreeMap<String, String> versionCheckWhereClause) {
        this.versionCheckWhereClause = versionCheckWhereClause;
    }




    public TreeMap<String, TreeSet<String>> getCustomClassExtends() {
		return this.customClassExtends;
	}



	public void setCustomClassExtends(
			TreeMap<String, TreeSet<String>> customClassExtends) {
		this.customClassExtends = customClassExtends;
	}




    public boolean isEnablePropertyPlaceholderConfigurer() {
		return enablePropertyPlaceholderConfigurer;
	}



	public void setEnablePropertyPlaceholderConfigurer(
			boolean enablePropertyPlaceholderConfigurer) {
		this.enablePropertyPlaceholderConfigurer = enablePropertyPlaceholderConfigurer;
	}



	public void setMavenArtifactVersionsDisabled(boolean disabled) {
		this.mavenArtifactVersionsDisabled = disabled;

	}



	public boolean isMavenArtifactVersionsDisabled() {
		return mavenArtifactVersionsDisabled;
	}





	protected TreeMap<String, String> getVersionCheckOrderBy() {
		return versionCheckOrderBy;
	}



	public void setUseLDAP(boolean useLDAP) {
		this.useLDAP = useLDAP;
	}

	public boolean getUseLDAP() {
		return this.useLDAP;
	}



	public void setUseLDAPImport(boolean useLDAPImport) {
		this.useLDAPImport = useLDAPImport;

	}



	/**
	 * @return the useLDAPImport
	 */
	public boolean isUseLDAPImport() {
		return useLDAPImport;
	}



	public void setSynchronizerVersion(String version) {
		this.synchronizerVersion = version;

	}



	/**
	 * @return the synchronizerVersion
	 */
	public String getSynchronizerVersion() {
		return synchronizerVersion;
	}



	public boolean isUseDynamicLDAPDataSource() {
		return this.useDynamicLDAPDataSource;
	}



	/**
	 * @param useDynamicLDAPDataSource the useDynamicLDAPDataSource to set
	 */
	public void setUseDynamicLDAPDataSource(boolean useDynamicLDAPDataSource) {
		this.useDynamicLDAPDataSource = useDynamicLDAPDataSource;
	}



	/**
	 * @return the ldapServer
	 */
	public String getLdapServer() {
		return ldapServer;
	}



	/**
	 * @param ldapServer the ldapServer to set
	 */
	public void setLdapServer(String ldapServer) {
		this.ldapServer = ldapServer;
	}


	/**
	 * @return the ldapBase
	 */
	public String getLdapBase() {
		return ldapBase;
	}



	/**
	 * @param ldapBase the ldapBase to set
	 */
	public void setLdapBase(String ldapBase) {
		this.ldapBase = ldapBase;
	}






	/**
	 * @return the ldapCn
	 */
	public String getLdapCn() {
		return ldapCn;
	}



	/**
	 * @param ldapCn the ldapCn to set
	 */
	public void setLdapCn(String ldapCn) {
		this.ldapCn = ldapCn;
	}



	/**
	 * @return the customClassCodeFields
	 */
	public TreeMap<String, String> getCustomClassCodeFields() {
		return customClassCodeFields;
	}



	/**
	 * @param customClassCodeFields the customClassCodeFields to set
	 */
	public void setCustomClassCodeFields(
			TreeMap<String, String> customClassCodeFields) {
		this.customClassCodeFields = customClassCodeFields;
	}



	/**
	/**
	 * @return the versionColumnsRead
	 */
	public TreeMap<String, List<String>> getVersionColumnsRead() {
		return versionColumnsRead;
	}



	/**
	 * @param versionColumnsRead the versionColumnsRead to set
	 */
	public void setVersionColumnsRead(
			TreeMap<String, List<String>> versionColumnsRead) {
		this.versionColumnsRead = versionColumnsRead;
	}



	/**
	 * @return the versionsRead
	 */
	public TreeMap<String, List<List<String>>> getVersionsRead() {
		return versionsRead;
	}



	/**
	 * @param versionsRead the versionsRead to set
	 */
	public void setVersionsRead(TreeMap<String, List<List<String>>> versionsRead) {
		this.versionsRead = versionsRead;
	}



	public void setMavenNoDeps(boolean noDeps) {
		this.mavenNoDeps = noDeps;

	}



	/**
	 * @return the mavenUseExternalLib
	 */
	public boolean isMavenUseExternalLib() {
		return mavenUseExternalLib;
	}



	/**
	 * @param mavenUseExternalLib the mavenUseExternalLib to set
	 */
	public void setMavenUseExternalLib(boolean mavenUseExternalLib) {
		this.mavenUseExternalLib = mavenUseExternalLib;
	}



	/**
	 * @return the mavenNoDeps
	 */
	public boolean isMavenNoDeps() {
		return mavenNoDeps;
	}



	/**
	 * @return the transientFields
	 */
	public Set<String> getTransientFields() {
		return transientFields;
	}



	/**
	 * @param transientFields the transientFields to set
	 */
	public void setTransientFields(Set<String> transientFields) {
		this.transientFields = transientFields;
	}


	/**
	 * @return the transientFields
	 */
	public Set<String> getMoneyFields() {
		return moneyFields;
	}



	/**
	 * @param transientFields the transientFields to set
	 */
	public void setMoneyFields(Set<String> moneyFields) {
		this.moneyFields = moneyFields;
	}



	/**
	 * @return the mavenPomEnabled
	 */
	public boolean isMavenPomEnabled() {
		return mavenPomEnabled;
	}



	/**
	 * @param mavenPomEnabled the mavenPomEnabled to set
	 */
	public void setMavenPomEnabled(boolean mavenPomEnabled) {
		this.mavenPomEnabled = mavenPomEnabled;
	}



	/**
	 * @return the springVersion
	 */
	public Integer getSpringVersion() {
		return springVersion;
	}



	/**
	 * @param springVersion the springVersion to set
	 */
	public void setSpringVersion(Integer springVersion) {
		this.springVersion = springVersion;
	}



	/**
	 * @return the noOutPutForExceptSchemaList
	 */
	public TreeSet<String> getNoOutPutForExceptSchemaList() {
		return this.noOutPutForExceptSchemaList;
	}



	/**
	 * @param noOutPutForExceptSchemaList the noOutPutForExceptSchemaList to set
	 */
	public void setNoOutPutForExceptSchemaList(
			TreeSet<String> noOutPutForExceptSchemaList) {
		this.noOutPutForExceptSchemaList = noOutPutForExceptSchemaList;
	}



	/**
	 * @return the ignoreFieldList
	 */
	public TreeMap<String, List<String>> getIgnoreFieldList() {
		return this.ignoreFieldList;
	}



	/**
	 * @param ignoreFieldList the ignoreFieldList to set
	 */
	public void setIgnoreFieldList(TreeMap<String, List<String>> ignoreFieldList) {
		this.ignoreFieldList = ignoreFieldList;
	}



	/**
	 * @return the schemas
	 */
	public TreeSet<String> getSchemas() {
		return schemas;
	}



	/**
	 * @param schemas the schemas to set
	 */
	public void setSchemas(TreeSet<String> schemas) {
		this.schemas = schemas;
	}



	/**
	 * @return the mavenJavaVersion
	 */
	public String getMavenJavaVersion() {
		return mavenJavaVersion;
	}



	/**
	 * @param mavenJavaVersion the mavenJavaVersion to set
	 */
	public void setMavenJavaVersion(String mavenJavaVersion) {
		this.mavenJavaVersion = mavenJavaVersion;
	}



	/**
	 * @return the enableSpringData
	 */
	public boolean isEnableSpringData() {
		return enableSpringData;
	}



	/**
	 * @param enableSpringData the enableSpringData to set
	 */
	public void setEnableSpringData(boolean enableSpringData) {
		this.enableSpringData = enableSpringData;
	}



	/**
	 * @return the springDataFactoryClass
	 */
	protected String getSpringDataFactoryClass() {
		return springDataFactoryClass;
	}



	/**
	 * @param springDataFactoryClass the springDataFactoryClass to set
	 */
	protected void setSpringDataFactoryClass(String springDataFactoryClass) {
		this.springDataFactoryClass = springDataFactoryClass;
	}



	/**
	 * @return the springDataRepoInterface
	 */
	protected String getSpringDataRepoInterface() {
		return springDataRepoInterface;
	}



	/**
	 * @param springDataRepoInterface the springDataRepoInterface to set
	 */
	protected void setSpringDataRepoInterface(String springDataRepoInterface) {
		this.springDataRepoInterface = springDataRepoInterface;
	}



	/**
	 * @return the disableApplicationContext
	 */
	public boolean isDisableApplicationContext() {
		return disableApplicationContext;
	}



	/**
	 * @param disableApplicationContext the disableApplicationContext to set
	 */
	public void setDisableApplicationContext(boolean disableApplicationContext) {
		this.disableApplicationContext = disableApplicationContext;
	}



	/**
	 * @return the enableMockitoBeans
	 */
	public boolean isEnableMockitoBeans() {
		return enableMockitoBeans;
	}



	/**
	 * @param enableMockitoBeans the enableMockitoBeans to set
	 */
	public void setEnableMockitoBeans(boolean enableMockitoBeans) {
		this.enableMockitoBeans = enableMockitoBeans;
	}



	/**
	 * @return the mockitoFilename
	 */
	public String getMockitoFilename() {
		return mockitoFilename;
	}



	/**
	 * @param mockitoFilename the mockitoFilename to set
	 */
	public void setMockitoFilename(String mockitoFilename) {
		this.mockitoFilename = mockitoFilename;
	}



	/**
	 * @param boolean1
	 */
	public void skipModelInterfaces(boolean skip) {
		this.skipModelInterfaces = skip;
	}



	/**
	 * @return the propertyPlaceholderConfigurerPrefix
	 */
	public String getPropertyPlaceholderConfigurerPrefix() {
		return propertyPlaceholderConfigurerPrefix;
	}



	/**
	 * @param propertyPlaceholderConfigurerPrefix the propertyPlaceholderConfigurerPrefix to set
	 */
	public void setPropertyPlaceholderConfigurerPrefix(
			String propertyPlaceholderConfigurerPrefix) {
		this.propertyPlaceholderConfigurerPrefix = propertyPlaceholderConfigurerPrefix;
	}



	/**
	 * @return the propertyPlaceholderConfigurerSuppressBean
	 */
	public boolean isPropertyPlaceholderConfigurerSuppressBean() {
		return propertyPlaceholderConfigurerSuppressBean;
	}



	/**
	 * @param propertyPlaceholderConfigurerSuppressBean the propertyPlaceholderConfigurerSuppressBean to set
	 */
	public void setPropertyPlaceholderConfigurerSuppressBean(
			boolean propertyPlaceholderConfigurerSuppressBean) {
		this.propertyPlaceholderConfigurerSuppressBean = propertyPlaceholderConfigurerSuppressBean;
	}



	/**
	 * @return the currencyFields
	 */
	public Set<String> getCurrencyFields() {
		return currencyFields;
	}



	/**
	 * @param currencyFields the currencyFields to set
	 */
	public void setCurrencyFields(Set<String> currencyFields) {
		this.currencyFields = currencyFields;
	}



	/**
	 * @return the customCurrencyUnitType
	 */
	public String getCustomCurrencyUnitType() {
		return customCurrencyUnitType;
	}



	/**
	 * @param customCurrencyUnitType the customCurrencyUnitType to set
	 */
	public void setCustomCurrencyUnitType(String customCurrencyUnitType) {
		this.customCurrencyUnitType = customCurrencyUnitType;
	}



	/**
	 * @return the customMoneyType
	 */
	public String getCustomMoneyType() {
		return customMoneyType;
	}



	/**
	 * @param customMoneyType the customMoneyType to set
	 */
	public void setCustomMoneyType(String customMoneyType) {
		this.customMoneyType = customMoneyType;
	}



	/**
	 * @return the encryptList
	 */
	public TreeMap<String, List<String>> getEncryptList() {
		return encryptList;
	}



	/**
	 * @param encryptList the encryptList to set
	 */
	public void setEncryptList(TreeMap<String, List<String>> encryptList) {
		this.encryptList = encryptList;
	}



	/**
	 * @return the daoCustomContextConfig
	 */
	public String getDaoCustomContextConfig() {
		return daoCustomContextConfig;
	}



	/**
	 * @param daoCustomContextConfig the daoCustomContextConfig to set
	 */
	public void setDaoCustomContextConfig(String daoCustomContextConfig) {
		this.daoCustomContextConfig = daoCustomContextConfig;
	}



	/**
	 * @return the enableJDK8Support
	 */
	public boolean isEnableJDK8Support() {
		return enableJDK8Support;
	}



	/**
	 * @param enableJDK8Support the enableJDK8Support to set
	 */
	public void setEnableJDK8Support(boolean enableJDK8Support) {
		this.enableJDK8Support = enableJDK8Support;
	}





}
