package com.felees.hbnpojogen;


import java.util.*;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.jvnet.inflector.Noun;
import org.jvnet.inflector.Rule;
import org.jvnet.inflector.RuleBasedPluralizer;
import org.jvnet.inflector.rule.RegexReplacementRule;



/**
 * Configuration
 *
 * @author wallacew
 *
 */
public class Config {

    public static final String DISABLED_FROM_CONFIG = "__DISABLED FROM CONFIG__";
	/** Internal constant */
    private static XMLConfiguration config;
    /** config */
    public static Template template;
    /** config */
    public static Template interfaceTemplate;
    /** config */
    public static Template repoTemplate;



    /**
     * Configuration file parser
     *
     * @param configFile
     * @param overridePath
     * @param overrideIP
     * @return source target path
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static String parseConfig(String configFile, String overridePath, String overrideIP)
            throws Exception {
        try {
            Config.config = new XMLConfiguration(configFile);
        }
        catch (ConfigurationException cex) {
            cex.printStackTrace();
        }

        Properties p = new Properties();
        p.setProperty( "resource.loader", "class" );
        p.setProperty( "class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader" );

//        p.setProperty("file.resource.loader.path", "templates");
        p.setProperty("file.resource.loader.cache", "true");
        p.setProperty("runtime.interpolate.string.literals", "true");
        Velocity.init(p);

        template = Velocity.getTemplate("templates/class.vm");
        interfaceTemplate = Velocity.getTemplate("templates/classIface.vm");
        repoTemplate = Velocity.getTemplate("templates/classRepo.vm");

        fillMaven();
        String path = fillBasicEntries();
        if (overridePath != null){
        	HbnPojoGen.log("Overriding source target to: "+overridePath);
        	State.getInstance().setSourceTarget(overridePath);
        	path = overridePath;
        }
        if (overrideIP != null) {
            if (HbnPojoGen.jdbcConnection.indexOf("*") > -1) {
                HbnPojoGen.log("Overriding database IP to source to : " + overrideIP);
            }
            else {
                HbnPojoGen.log("Overriding database IP given, but source IP is hardcoded. Use jdbc://*/yourSchema to enable overriding.");
            }

            HbnPojoGen.jdbcConnection = HbnPojoGen.jdbcConnection.replaceAll("\\*", overrideIP);
        }

        fillSchemaStrategy();
        fillPreventCleanList();
        // Fill in Abstract Classes - chrisp
        fillAbstractTablesList();
        fillImmutableTablesList();
        fillIgnoreTableList();
        fillPrepopulateList();
        // Build the generator map list
        fillGeneratorMap();
        // Build the prepopulate list
        fillPreExecuteList();
        // Build the link "treat as enum" map
        fillTreatAsEnum();
        // Build the renaming section map
        fillRenameSection();
        // Build the package map list
        fillPackageMap();
        fillEnumMap();
        // Build the annotations list
        fillVersionCheck();

        // build the linkTables list
        fillLinkTableList();
        // build the list of declare one-to-one relations
        fillOne2OneList();
        // build cyclic table exclusion list
        fillCyclicTableExclusionList();
        // build the natural keys list
        fillNaturalKeysList();
        // build the test values list
        fillTestValuesList();
     // build the disable back links list
        fillDisableBackLinks();
     // build the disable back links list
        fillNoFollowLinks();
        // build the noOutput list
        fillNoOutputForSchema();
        // build the cascade list
        fillCascadeList();
        // build the fetchtype list
        fillFetchTypeList();
     // build the suffix list
        fillSuffix();
        fillFakeFK();
        fillCustomPluralization();

        fillEqualityExcludes();
        fillTransientFields();
        fillMoneyFields();
        fillCurrencyFields();
        fillEncryptedFields();
               fillUniqueKeys();

        // switch config get List here:
        fillLdap();
        fillAnnotationList();

        return path;
    }


    /**
     *
     *
     */
    @SuppressWarnings("unchecked")
    private static void fillEqualityExcludes() {

            ArrayList<String> excludes = (ArrayList<String>) Config.config.getList("equalityExcludes.field");
            State.getInstance().getEqualityExcludes().addAll(excludes);
    }

    private static void fillTransientFields() {
        ArrayList<String> transients = (ArrayList<String>) Config.config.getList("transientFields.field");
        State.getInstance().getTransientFields().addAll(transients);
}

    private static void fillMoneyFields() {
        ArrayList<String> money = (ArrayList<String>) Config.config.getList("moneyFields.field");
        State.getInstance().getMoneyFields().addAll(money);
        State.getInstance().setCustomMoneyType(Config.config.getString("moneyFields[@typeOverride]", "org.jadira.usertype.moneyandcurrency.moneta.PersistentMoneyAmountAndCurrency"));

}
    private static void fillCurrencyFields() {
        ArrayList<String> currency = (ArrayList<String>) Config.config.getList("currencyUnitFields.field");
        State.getInstance().getCurrencyFields().addAll(currency);
        State.getInstance().setCustomCurrencyUnitType(Config.config.getString("currencyUnitFields[@typeOverride]", "org.jadira.usertype.moneyandcurrency.moneta.PersistentCurrencyUnit"));

}

    private static void fillEncryptedFields() {
//        ArrayList<String> enc = (ArrayList<String>) Config.config.getList("encryptedFields.field");

        List<String> tmp = Config.config.getList("encryptedFields.field");
        for (int i = 0; i < tmp.size(); i++) {
            State.getInstance().encryptList.put(tmp.get(i).toUpperCase(), Config.config.getList(String.format("encryptedFields.field(%d)[@except-for]", i), new LinkedList<String>()));
        }


//        State.getInstance().getEncryptedFields().addAll(enc);
    }

    /**
     * @param cascadeType
     * @param state
     */
    @SuppressWarnings("unchecked")
	private static void fillCascade(String cascadeType, TreeMap<String, CascadeState> state){
    	boolean defaultXToX = Config.config.getBoolean("cascading."+cascadeType+"[@default]", true);
    	List<String> defaultCascadeL = Config.config.getList("cascading."+cascadeType+"[@defaultCascade]");
    	 if (defaultCascadeL.isEmpty()){
    	     defaultCascadeL.add("SAVE_UPDATE");
        }
    	state.put("*", new CascadeState(defaultXToX, new HashSet<String>(defaultCascadeL)));

    	ArrayList<Object> tmpCascade = (ArrayList<Object>) Config.config.getList("cascading."+cascadeType+".except[@package]");
        for (int i = 0; i < tmpCascade.size(); i++) {

            String packageName = Config.config.getString(String.format("cascading.%s.except(%d)[@package]", cascadeType, i), "*");
            String className = Config.config.getString(String.format("cascading.%s.except(%d)[@class]", cascadeType, i), "*");
            String propertyName = Config.config.getString(String.format("cascading.%s.except(%d)[@property]", cascadeType, i), "*");
            List<String> cascadeL = Config.config.getList(String.format("cascading.%s.except(%d)[@cascade]", cascadeType, i));


            if (!cascadeL.isEmpty()){
                 cascadeL.add("SAVE_UPDATE");
            }

            Boolean enabled = Config.config.getBoolean(String.format("cascading.%s.except(%d)[@enabled]", cascadeType, i), !defaultXToX);

            state.put(packageName+"."+className+"."+propertyName, new CascadeState(enabled, new HashSet<String>(cascadeL)));
        }

        tmpCascade = (ArrayList<Object>) Config.config.getList("cascading."+cascadeType+".except-to[@to-package]");
        for (int i = 0; i < tmpCascade.size(); i++) {

            String packageName = Config.config.getString(String.format("cascading.%s.except-to(%d)[@to-package]", cascadeType, i), "*");
            String className = Config.config.getString(String.format("cascading.%s.except-to(%d)[@to-class]", cascadeType, i), "*");
            String propertyName = Config.config.getString(String.format("cascading.%s.except-to(%d)[@to-property]", cascadeType, i), "*");
            List<String> cascadeL = Config.config.getList(String.format("cascading.%s.except-to(%d)[@cascade]", cascadeType, i));
            if (!cascadeL.isEmpty()){
                cascadeL.add("SAVE_UPDATE");
           }

            Boolean enabled = Config.config.getBoolean(String.format("cascading.%s.except-to(%d)[@enabled]", cascadeType, i), !defaultXToX);
            state.put("to:"+packageName+"."+className+"."+propertyName, new CascadeState(enabled, new HashSet<String>(cascadeL)));
        }

    }


    /**
     */
    @SuppressWarnings("unchecked")
    private static void fillSuffix(){
        String defaultSuffix = Config.config.getString("suffix[@default]", "");
        Map<String, String> state = State.getInstance().getClassSuffixes();

        state.put("*", defaultSuffix);

        ArrayList<Object> tmpSuffix = (ArrayList<Object>) Config.config.getList("suffix.except[@package]");

        for (int i = 0; i < tmpSuffix.size(); i++) {
            String packageName = Config.config.getString(String.format("suffix.except(%d)[@package]", i), "*");
            String className = Config.config.getString(String.format("suffix.except(%d)[@class]",  i), "*");
            String suffix = Config.config.getString(String.format("suffix.except(%d)[@suffix]", i), "");
            state.put(packageName+"."+className, suffix);
        }

    }
    /**
     */
    @SuppressWarnings("unchecked")
    private static void fillFakeFK(){
        boolean enabled = Config.config.getBoolean("fakeFK[@enabled]", false);
        String pattern = Config.config.getString("fakeFK[@pattern]", "_DONT_MATCH_BY_DEFAULT_");
        String replacePattern = Config.config.getString("fakeFK[@replacePattern]", "$0");

        TreeMap<String, FakeFKPattern> state = State.getInstance().getFakeFK();

        state.put("*", new FakeFKPattern(pattern, replacePattern, enabled));

        ArrayList<Object> tmpSuffix = (ArrayList<Object>) Config.config.getList("fakeFK.except[@table]");

        for (int i = 0; i < tmpSuffix.size(); i++) {
            String table = Config.config.getString(String.format("fakeFK.except(%d)[@table]", i), "*");
            String patternItem = Config.config.getString(String.format("fakeFK.except(%d)[@pattern]",  i), "*");
            String replacePatternItem = Config.config.getString(String.format("fakeFK.except(%d)[@replacePattern]",  i), "*");
            Boolean enabledItem = Config.config.getBoolean(String.format("fakeFK.except(%d)[@enabled]", i), false);
            state.put(table, new FakeFKPattern(patternItem, replacePatternItem, enabledItem));
        }

    }


    /**
     */
    @SuppressWarnings("unchecked")
    private static void fillCustomPluralization(){

        ArrayList<Object> tmpRule = (ArrayList<Object>) Config.config.getList("customPluralization.rule[@regexmatch]");

        List<Rule> customRules =  new ArrayList<Rule>();


        for (int i = 0; i < tmpRule.size(); i++) {
            String match = Config.config.getString(String.format("customPluralization.rule(%d)[@regexmatch]", i), "*");
            String replace = Config.config.getString(String.format("customPluralization.rule(%d)[@regexreplace]", i), "");
            customRules.add(new RegexReplacementRule(match, replace));
        }

        State.getInstance().setCustomPluralizer(new RuleBasedPluralizer(customRules, Locale.ENGLISH, Noun.pluralizer(Locale.ENGLISH)));

    }


	@SuppressWarnings("unchecked")
	private static void fillVersionCheck(){
		try{
			Config.config.configurationAt("versionCheck");
			State.getInstance().setVersionCheckEnabled(true);
		} catch (IllegalArgumentException iae) {
			State.getInstance().setVersionCheckEnabled(false);
		}
    	boolean defaultEnable = Config.config.getBoolean("versionCheck[@defaultEnabled]", true);
    	String defaultTable = Config.config.getString("versionCheck[@defaultTable]", "db_version");
    	String defaultVersionCheckWhereClause = Config.config.getString("versionCheck[@whereClause]", "");
    	String defaultVersionCheckOrderBy = Config.config.getString("versionCheck[@orderBy]", "");


    	if (!defaultEnable){
    		defaultTable = Config.DISABLED_FROM_CONFIG;
    	} else {

    	    State.getInstance().ignoreTableList.add("*."+defaultTable);
    	}
    	List defList = new LinkedList<String>();
    	defList.add("branch");
    	defList.add("alter_no");
//        defList.add("system");

    	List defaultFields = Config.config.getList("versionCheck[@defaultFields]", defList);
    	State.getInstance().setVersionCheckDefaultEnabled(defaultEnable);

    	Set<String> fields = new HashSet<String>(defaultFields);

    	State.getInstance().getVersionCheck().put("*", new ObjectPair<String, Set<String>>(defaultTable, fields));
    	State.getInstance().getVersionCheckWhereClause().put("*", defaultVersionCheckWhereClause);
    	State.getInstance().getVersionCheckOrderBy().put("*", defaultVersionCheckOrderBy);

    	ArrayList<Object> tmpVersion = (ArrayList<Object>) Config.config.getList("versionCheck.except[@schema]");
        for (int i = 0; i < tmpVersion.size(); i++) {
        	Boolean enabled = Config.config.getBoolean(String.format("versionCheck.except(%d)[@enabled]",  i), false);
       		String schema = Config.config.getString(String.format("versionCheck.except(%d)[@schema]", i));
       		String table = Config.config.getString(String.format("versionCheck.except(%d)[@table]", i));
       		String whereClause = Config.config.getString(String.format("versionCheck.except(%d)[@whereClause]", i), defaultVersionCheckWhereClause);
       		String orderBy = Config.config.getString(String.format("versionCheck.except(%d)[@orderBy]", i), defaultVersionCheckOrderBy);
       		List vfields = Config.config.getList(String.format("versionCheck.except(%d)[@fields]", i), defList);

        		if (!enabled){
        			table=DISABLED_FROM_CONFIG;
        		} else {
                    State.getInstance().ignoreTableList.add(schema+"."+table);
        		}

        	State.getInstance().getVersionCheck().put(schema, new ObjectPair<String, Set<String>>(table, new HashSet<String>(vfields)));
        	State.getInstance().getVersionCheckWhereClause().put(schema, whereClause);
        	State.getInstance().getVersionCheckOrderBy().put(schema, orderBy);
        }
    }

	/**
	 *
	 */
	@SuppressWarnings("unchecked")
	private static void fillMaven(){
		boolean mavenEnabled = Config.config.getBoolean("maven[@enabled]", false);
		boolean mavenPomEnabled = Config.config.getBoolean("maven[@enablePomGeneration]", true);
		State.getInstance().setMavenVersion(Config.config.getString("maven[@version]", "1.0"));
		State.getInstance().setMavenJavaVersion(Config.config.getString("maven[@javaVersion]", "1.7"));
		State.getInstance().setMavenName(Config.config.getString("maven[@name]", State.getInstance().getProjectName()));
		State.getInstance().setMavenArtifactVersionsDisabled(Config.config.getBoolean("maven[@noArtifactVersions]", false));
		State.getInstance().setMavenEnabled(mavenEnabled);
		State.getInstance().setMavenPomEnabled(mavenPomEnabled);
		if (mavenEnabled) {
		    State.getInstance().setMavenAdditionalPomEntries(Config.config.getString("maven.additionalPomEntries", ""));
		    State.getInstance().setMavenDistributionManagement(Config.config.getString("maven.distributionManagement", ""));
			State.getInstance().setMavenDependency(Config.config.getString("maven.dependencies"));
			State.getInstance().setMavenArtifactId(Config.config.getString("maven[@artifactId]", State.getInstance().getProjectName()));
			State.getInstance().setMavenGroupId(Config.config.getString("maven[@groupId]", State.getInstance().getTopLevel()));
			State.getInstance().setMavenNoDeps(Config.config.getBoolean("maven[@noDeps]", false));
			State.getInstance().setMavenUseExternalLib(Config.config.getBoolean("maven[@useExternalLib]", false));
		}
    }



    /** Lazy fetchtype
     * @param fetchType
     * @param state
     */
    @SuppressWarnings("unchecked")
	private static void fillLazy(String fetchType, TreeMap<String, Boolean> state){
    	boolean defaultXToX = Config.config.getBoolean("fetchtype."+fetchType+"[@defaultlazy]", true);
    	state.put("*", defaultXToX);

    	ArrayList<Object> tmpCascade = (ArrayList<Object>) Config.config.getList("fetchtype."+fetchType+".except[@package]");


        for (int i = 0; i < tmpCascade.size(); i++) {

            String packageName = Config.config.getString(String.format("fetchtype.%s.except(%d)[@package]", fetchType, i));
            String className = Config.config.getString(String.format("fetchtype.%s.except(%d)[@class]", fetchType, i));
            String propertyName = Config.config.getString(String.format("fetchtype.%s.except(%d)[@property]", fetchType, i));
            state.put(packageName+"."+className+"."+propertyName, !defaultXToX);
        }

    }

    /**
     * Fills the cascade exception list.
     */
    @SuppressWarnings("unchecked")
	private static void fillCascadeList() {
    	fillCascade("one-to-many", State.getInstance().getOneToManyCascadeEnabled());
    	fillCascade("many-to-one", State.getInstance().getManyToOneCascadeEnabled());
    	fillCascade("many-to-many", State.getInstance().getManyToManyCascadeEnabled());
    	fillCascade("one-to-one", State.getInstance().getOneToOneCascadeEnabled());
    }


    /**
     * Fills the cascade exception list.
     */
    @SuppressWarnings("unchecked")
	private static void fillFetchTypeList() {
    	fillLazy("one-to-many", State.getInstance().getOneToManyLazyEnabled());
    	fillLazy("many-to-one", State.getInstance().getManyToOneLazyEnabled());
    	fillLazy("many-to-many", State.getInstance().getManyToManyLazyEnabled());
    	fillLazy("one-to-one", State.getInstance().getOneToOneLazyEnabled());
    }


	/**
	 *
	 */
	@SuppressWarnings("unchecked")
	private static void fillNoOutputForSchema() {
        ArrayList<String> tmp = (ArrayList<String>) Config.config.getList("noOutputForSchema.ignore");
        for (int i = 0; i < tmp.size(); i++) {
            State.getInstance().noOutPutForSchemaList.add(tmp.get(i).toUpperCase());
        }

        ArrayList<String> tmp2 = (ArrayList<String>) Config.config.getList("noOutputForSchema.ignoreAllExcept");
        for (int i = 0; i < tmp2.size(); i++) {
            State.getInstance().noOutPutForExceptSchemaList.add(tmp2.get(i).toUpperCase());
        }


	}



	/**
	 *
	 */
	@SuppressWarnings("unchecked")
	private static void fillDisableBackLinks() {
    	 ArrayList<Object> tmpOne2One = (ArrayList<Object>) Config.config.getList("disableBackLinks.nobacklink[@from]");
         for (int i = 0; i < tmpOne2One.size(); i++) {
             String fromTable = Config.config.getString(String.format("disableBackLinks.nobacklink(%d)[@from]", i));
             String toTable = Config.config.getString(String.format("disableBackLinks.nobacklink(%d)[@to]", i));
             String fromField = Config.config.getString(String.format("disableBackLinks.nobacklink(%d)[@from-field]", i));

             TreeMap<String, TreeSet<String>> entry = State.getInstance().getDisableBackLinkTables().get(fromTable);

             if (entry == null) {
                 // this is a new table we're seeing. Create an entry
                 entry = new TreeMap<String, TreeSet<String>>(new CaseInsensitiveComparator());
                 State.getInstance().getDisableBackLinkTables().put(fromTable, entry);
             }

             TreeSet<String> fromFields = entry.get(fromTable);
             if (fromFields == null){
            	 fromFields = new TreeSet<String>();
            	 entry.put(toTable, fromFields);
             }
             fromFields.add(fromField);
         }

	}

	/**
    *
    */
   @SuppressWarnings("unchecked")
   private static void fillNoFollowLinks() {
        ArrayList<Object> tmpOne2One = (ArrayList<Object>) Config.config.getList("noFollowLinks.nofollow[@from]");
        for (int i = 0; i < tmpOne2One.size(); i++) {
            String fromTable = Config.config.getString(String.format("noFollowLinks.nofollow(%d)[@from]", i));
            String toTable = Config.config.getString(String.format("noFollowLinks.nofollow(%d)[@to]", i));
            String fromField = Config.config.getString(String.format("noFollowLinks.nofollow(%d)[@from-field]", i));

            TreeMap<String, TreeSet<String>> entry = State.getInstance().getNoFollowTables().get(fromTable);

            if (entry == null) {
                // this is a new table we're seeing. Create an entry
                entry = new TreeMap<String, TreeSet<String>>(new CaseInsensitiveComparator());
                State.getInstance().getNoFollowTables().put(fromTable, entry);
            }

            TreeSet<String> fromFields = entry.get(fromTable);
            if (fromFields == null){
                fromFields = new TreeSet<String>();
                entry.put(toTable, fromFields);
            }
            fromFields.add(fromField);
        }

   }

   private static void fillUniqueKeys() {
       ArrayList<Object> tmpUnique= (ArrayList<Object>) Config.config.getList("uniqueKeys.unique[@schema]");
       for (int i = 0; i < tmpUnique.size(); i++) {
           String schema = Config.config.getString(String.format("uniqueKeys.unique(%d)[@schema]", i));
           String table = Config.config.getString(String.format("uniqueKeys.unique(%d)[@table]", i));
           String field = Config.config.getString(String.format("uniqueKeys.unique(%d)[@field]", i));
           String fullTable = schema + "." + table;

           TreeSet<String> entry = State.getInstance().getUniqueKeys().get(fullTable);

           if (entry == null) {
               // this is a new table we're seeing. Create an entry
               entry = new TreeSet<String>(new CaseInsensitiveComparator());
               State.getInstance().getUniqueKeys().put(fullTable, entry);
           }

           entry.add(field);
       }

  }


	/**
     *
     */
    @SuppressWarnings("unchecked")
    private static void fillIgnoreTableList() {
        ArrayList<String> tmp = (ArrayList<String>) Config.config.getList("ignore.table");
        for (int i = 0; i < tmp.size(); i++) {
            State.getInstance().ignoreTableList.add(tmp.get(i).toUpperCase());
            }

        tmp = (ArrayList<String>) Config.config.getList("ignore.field");
        for (int i = 0; i < tmp.size(); i++) {
            State.getInstance().ignoreFieldList.put(tmp.get(i).toUpperCase(), Config.config.getList(String.format("ignore.field(%d)[@except-for]", i), null));
        }

        tmp = (ArrayList<String>) Config.config.getList("ignore.everything-except.table");
        for (int i = 0; i < tmp.size(); i++) {
            State.getInstance().ignoreEverythingExceptList.add(tmp.get(i).toUpperCase());
        }


    }



    /**
     *
     */
    @SuppressWarnings("unchecked")
    private static void fillPreventCleanList() {
        ArrayList<Object> preventCleanList = (ArrayList<Object>) Config.config.getList("preventclean.table");

        for (int i = 0; i < preventCleanList.size(); i++) {
            State.getInstance().preventClean.add(preventCleanList.get(i).toString().toUpperCase());
        }
    }



    /**
     * Fills in the list of Abstract tables - chrisp
     */
    @SuppressWarnings("unchecked")
    private static void fillAbstractTablesList() {
        ArrayList<Object> abstractTablesList = (ArrayList<Object>) Config.config.getList("abstractTables.table");

        for (int i = 0; i < abstractTablesList.size(); i++) {
            State.getInstance().abstractTables.add(abstractTablesList.get(i).toString().toUpperCase());
        }
    }

    /**
     * Fills in the list of Abstract tables - chrisp
     */
    @SuppressWarnings("unchecked")
    private static void fillImmutableTablesList() {
        ArrayList<Object> immutableTablesList = (ArrayList<Object>) Config.config.getList("immutableTables.table");

        for (int i = 0; i < immutableTablesList.size(); i++) {
            boolean genStatic = Config.config.getBoolean(String.format("immutableTables.table(%d)[@generate-static-test]", i), false);
            State.getInstance().getImmutableTables().put(immutableTablesList.get(i).toString().toUpperCase(), genStatic );
        }
    }


    /**
     * @return path
     */
    private static String fillBasicEntries() {
        State.getInstance().dbType = Config.config.getString(Constants.DATABASE_TYPE);
        if (State.getInstance().dbType != null) {
            if (State.getInstance().dbType.equalsIgnoreCase("MYSQL")) {
                State.getInstance().dbMode = 0;
            }
            else if (State.getInstance().dbType.equalsIgnoreCase("MSSQL")) {
                State.getInstance().dbMode = 1;
            }
            else if (State.getInstance().dbType.equalsIgnoreCase("PostgreSQL")) {
            	                State.getInstance().dbMode = 2;
            	            }
        }
        State.getInstance().customDialect = Config.config.getString("dbType[@dialect]", null);
        State.getInstance().dbIP = Config.config.getString(Constants.DATABASE_IP);
        State.getInstance().dbCatalog = Config.config.getString(Constants.DATABASE_CATALOG);
        State.getInstance().dbSchema = Config.config.getString(Constants.DATABASE_SCHEMA);
        if (State.getInstance().dbSchema == null){
        	State.getInstance().dbSchema = "public";
        }
        State.getInstance().dbUsername = Config.config.getString(Constants.DATABASE_USERNAME);
        State.getInstance().dbPassword = Config.config.getString(Constants.DATABASE_PASSWORD);
        String path = Config.config.getString(Constants.SOURCE_TARGET);
        State.getInstance().setSourceTarget(path);
        State.getInstance().setConnectionPool(Config.config.getString("connectionPool", "HIKARICP").toUpperCase());


        State.getInstance().projectName = Config.config.getString(Constants.PROJECT_NAME);
        HbnPojoGen.driver = Config.config.getString(Constants.DRIVER);
        HbnPojoGen.jdbcConnection = Config.config.getString(Constants.JDBC_CONNECTION_STRING);
        State.getInstance().topLevel = Config.config.getString(Constants.TOP_LEVEL);
        State.getInstance().libPath = Config.config.getString(Constants.LIB_PATH);
        State.getInstance().disableUnderscoreConversion = Config.config.getString(Constants.DISABLE_UNDERSCORE_CONVERSION, "false").equalsIgnoreCase("TRUE");
        State.getInstance().disableCleanTables = Config.config.getBoolean("disableCleanTables", false);
        State.getInstance().disableSubtypeEnumGeneration = Config.config.getBoolean("disableSubtypeEnumGeneration", false);
        State.getInstance().disableEnglishPlural = Config.config.getString(Constants.DISABLE_ENGLISH_PLURAL, "false").equalsIgnoreCase("TRUE");
        State.getInstance().disableTestRollback = Config.config.getString("disableTestRollback", "false").equalsIgnoreCase("TRUE");
        State.getInstance().enableStateSave = Config.config.getString("enableStateSave", "false").equalsIgnoreCase("TRUE");
        State.getInstance().enableMockitoBeans = Config.config.getString("enableMockitoBeans", "true").equalsIgnoreCase("TRUE");
		State.getInstance().setMockitoFilename(Config.config.getString("enableMockitoBeans[@filename]", "beans.test.mockito.xml"));
        State.getInstance().enableHibernateValidator = Config.config.getString("enableHibernateValidator", "false").equalsIgnoreCase("TRUE");
        State.getInstance().setEnableJodaSupport(Config.config.getString("enableJodaSupport", "false").equalsIgnoreCase("TRUE"));
        State.getInstance().setEnableJDK8Support(Config.config.getString("enableJDK8Support", "false").equalsIgnoreCase("TRUE"));
        State.getInstance().setEnableSpringData(Config.config.getString("enableSpringData", "true").equalsIgnoreCase("TRUE"));
		State.getInstance().setSpringDataFactoryClass(Config.config.getString("enableSpringData[@factoryClass]", ""));
		State.getInstance().setSpringDataRepoInterface(Config.config.getString("enableSpringData[@repoInterface]", " org.springframework.data.jpa.repository.JpaRepository"));

        State.getInstance().disableBackLinksInDataPoolFactory = Config.config.getString("disableBackLinksInDataPoolFactory", "false").equalsIgnoreCase("TRUE");
        State.getInstance().setDisableLazyConnections(Config.config.getString("disableLazyConnections", "false").equalsIgnoreCase("TRUE"));
        State.getInstance().setEnablePropertyPlaceholderConfigurer(Config.config.getString("enablePropertyPlaceholderConfigurer", "false").equalsIgnoreCase("TRUE"));
        State.getInstance().setPropertyPlaceholderConfigurerPrefix(Config.config.getString("enablePropertyPlaceholderConfigurer[@prefix]", ""));
        State.getInstance().setPropertyPlaceholderConfigurerSuppressBean(Config.config.getBoolean("enablePropertyPlaceholderConfigurer[@suppressBean]", false));

        State.getInstance().setApplicationContextFilename(Config.config.getString("applicationContextFilename", "applicationContext.xml"));
		State.getInstance().setDisableApplicationContext(Config.config.getBoolean("applicationContextFilename[@disableGeneration]", false));
        State.getInstance().setSessionFactoryItems(Config.config.getString("sessionFactory", ""));
        State.getInstance().setTransactionManagerItems(Config.config.getString("transactionManagerItems", ""));
        State.getInstance().setAdditionalContextItems(Config.config.getString("additionalContextItems", ""));
        State.getInstance().setSpringVersion(Config.config.getInt("springVersion", 2));

        boolean mavenEnabled = State.getInstance().isMavenEnabled();
        String defaultSrc = mavenEnabled ?  "src/main/java" : "src";
        State.getInstance().setSrcFolder(Config.config.getString("sourceFolderName", defaultSrc));
        defaultSrc = mavenEnabled ?  "src/test/java" : "test/unit";
        State.getInstance().setTestFolder(Config.config.getString("testFolderName", defaultSrc));
        StringBuilder sb = new StringBuilder();
        for(Iterator<Object> it = Config.config.getList("testDaoCustomContextConfig", Collections.emptyList()).iterator(); it.hasNext();) {
            sb.append(it.next());
            if (it.hasNext()) {
                sb.append(Config.config.getListDelimiter());
            }
        }
        State.getInstance().setDaoCustomContextConfig(sb.toString());
        defaultSrc = mavenEnabled ?  "src/main/resources" : "resources";
        State.getInstance().setResourceFolder(Config.config.getString("resourceFolder", defaultSrc));
        defaultSrc = mavenEnabled ?  "src/test/resources": "test/unit/resources";
        State.getInstance().setTestResourceFolder(Config.config.getString("testResourceFolder", defaultSrc));

        return path;
    }


	private static void fillLdap() {
        config.setDelimiterParsingDisabled(true);
        config.setListDelimiter((char)1);
        Config.config.clear();
        try {
			config.load();
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		State.getInstance().setUseLDAP(Config.config.getBoolean("connectionPool[@useLDAP]", false));
        State.getInstance().setUseDynamicLDAPDataSource(Config.config.getBoolean("connectionPool[@enableDynamicLdapDataSource]", false));
        State.getInstance().setUseLDAPImport(Config.config.getBoolean("connectionPool[@useLDAPImport]", false));

        State.getInstance().setLdapServer(Config.config.getString("connectionPool[@ldapServer]"));
        State.getInstance().setLdapCn(Config.config.getString("connectionPool[@ldapCn]"));
        State.getInstance().setLdapBase(Config.config.getString("connectionPool[@ldapBase]"));
	}



    /**
     *
     */
    private static void fillSchemaStrategy() {
        String schemaRestrictTmp = Config.config.getString(Constants.SCHEMA_STRATEGY);
        State.getInstance().schemaRestrict = -1;

        if (schemaRestrictTmp.equalsIgnoreCase(Constants.RESTRICT)) {
            State.getInstance().schemaRestrict = 0;
        }
        else {
            if (schemaRestrictTmp.equalsIgnoreCase(Constants.PARTIAL)) {
                State.getInstance().schemaRestrict = 1;
            }
            else {
                if (schemaRestrictTmp.equalsIgnoreCase(Constants.FULL)) {
                    State.getInstance().schemaRestrict = 2;
                }
                else if (schemaRestrictTmp.equalsIgnoreCase(Constants.ALL)) {
                    State.getInstance().schemaRestrict = 3;
                }

            }
        }
        if (State.getInstance().schemaRestrict < 0) {
            HbnPojoGen.logE("Schema Restrict option in config file is invalid (it must be RESTRICT, PARTIAL, FULL or ALL. Defaulting to RESTRICT");
            State.getInstance().schemaRestrict = 0;
        }
    }



    /**
     *
     */
    @SuppressWarnings("unchecked")
    private static void fillTestValuesList() {
        ArrayList<Object> testValues = (ArrayList<Object>) Config.config.getList("testValues.tables.table.name");


        for (int i = 0; i < testValues.size(); i++) {

        		TreeMap<String, String> properties = State.getInstance().defaultTestValues.get(testValues.get(i).toString().toUpperCase());
        		if (properties == null){
        			properties  = new TreeMap<String, String>();
        				State.getInstance().defaultTestValues.put(testValues.get(i).toString().toUpperCase(), properties);
        		}

            ArrayList<Object> fieldValues = (ArrayList<Object>) Config.config.getList("testValues.tables.table(" + i + ").fields.name");

            for (int j = 0; j < fieldValues.size(); j++) {
                properties.put(fieldValues.get(j).toString().toUpperCase(), Config.config.getString("testValues.tables.table(" + i + ").fields(" + j +
                        ").value"));
            }
        }



        testValues = (ArrayList<Object>) Config.config.getList("testValues.target[@schema]");
        for (int i = 0; i < testValues.size(); i++) {
            String schema=Config.config.getString(String.format("testValues.target(%d)[@schema]", i));
            String datapool=Config.config.getString(String.format("testValues.target(%d)[@datapool]", i));
                State.getInstance().testDataPools.put(schema, datapool);
        }

    }



    /**
     *
     */
    @SuppressWarnings("unchecked")
    private static void fillNaturalKeysList() {
        ArrayList<Object> tmpKeys = (ArrayList<Object>) Config.config.getList("naturalKeys.table[@name]");
        for (int i = 0; i < tmpKeys.size(); i++) {

            TreeSet<String> entry = State.getInstance().naturalKeys.get(tmpKeys.get(i));

            if (entry == null) {
                // this is a new table we're seeing. Create an entry
                entry = new TreeSet<String>(new CaseInsensitiveComparator());
                State.getInstance().naturalKeys.put((String) tmpKeys.get(i), entry);
            }

            ArrayList<Object> tmpFields = (ArrayList<Object>) Config.config.getList(String.format("naturalKeys.table(%d).key[@field]", i));
            for (int j = 0; j < tmpFields.size(); j++) {
                // get the set of destLinks
                entry.add(Config.config.getString(String.format("naturalKeys.table(%d).key(%d)[@field]", i, j)));
            }
        }
    }



    /**
     *
     */
    @SuppressWarnings("unchecked")
    private static void fillOne2OneList() {
        ArrayList<Object> tmpOne2One = (ArrayList<Object>) Config.config.getList("oneToOne.table[@name]");
        for (int i = 0; i < tmpOne2One.size(); i++) {
            String tableName = Config.config.getString(String.format("oneToOne.table(%d)[@name]", i));
            String fieldName = Config.config.getString(String.format("oneToOne.table(%d)[@field]", i));


            TreeSet<String> entry = State.getInstance().oneToOneTables.get(tableName);

            if (entry == null) {
                // this is a new table we're seeing. Create an entry
                entry = new TreeSet<String>(new CaseInsensitiveComparator());
                State.getInstance().oneToOneTables.put(tableName, entry);
            }
            entry.add(fieldName);
        }
    }



    /**
     * Fills in the list of cyclicTableExclusionList tables. - chrisp
     *
     */
    @SuppressWarnings("unchecked")
    private static void fillCyclicTableExclusionList() {
        ArrayList<Object> tmpCyclicTableExclusionList = (ArrayList<Object>) Config.config.getList("cyclicTableExclusionList.table[@name]");
        for (int i = 0; i < tmpCyclicTableExclusionList.size(); i++) {
            String tableName = Config.config.getString(String.format("cyclicTableExclusionList.table(%d)[@name]", i));
            String fieldName = Config.config.getString(String.format("cyclicTableExclusionList.table(%d)[@field]", i));
            String replacement = Config.config.getString(String.format("cyclicTableExclusionList.table(%d)[@replacement]", i));


            TreeMap<String, String> entry = State.getInstance().cyclicTableExclusionListTables.get(tableName);

            if (entry == null) {
                // this is a new table we're seeing. Create an entry
                entry = new TreeMap<String, String>(new CaseInsensitiveComparator());
                State.getInstance().cyclicTableExclusionListTables.put(tableName, entry);
            }
            entry.put(fieldName, replacement);
        }
    }



    /**
     *
     */
    @SuppressWarnings("unchecked")
    private static void fillLinkTableList() {
        ArrayList<Object> tmpLinks = (ArrayList<Object>) Config.config.getList("linktables.table[@name]");
        for (int i = 0; i < tmpLinks.size(); i++) {

            TreeMap<String, TreeSet<String>> entry = State.getInstance().linkTables.get(tmpLinks.get(i));

            if (entry == null) {
                // this is a new table we're seeing. Create an entry
                entry = new TreeMap<String, TreeSet<String>>(new CaseInsensitiveComparator());
                State.getInstance().linkTables.put((String) tmpLinks.get(i), entry);
            }

            ArrayList<Object> tmpFields = (ArrayList<Object>) Config.config.getList(String.format("linktables.table(%d).link[@srcField]", i));
            for (int j = 0; j < tmpFields.size(); j++) {
                // get the set of destLinks
                String srcField = Config.config.getString(String.format("linktables.table(%d).link(%d)[@srcField]", i, j));
                TreeSet<String> src = entry.get(srcField);

                if (src == null) {
                    // we're seeing a new srcField...
                    src = new TreeSet<String>(new CaseInsensitiveComparator());
                    entry.put(srcField, src);
                }
                src.add(Config.config.getString(String.format("linktables.table(%d).link(%d)[@dstField]", i, j)));
            }
        }
    }



    /**
     *
     */
    @SuppressWarnings({ "unchecked", "cast" })
    private static void fillAnnotationList() {
    	   config.setDelimiterParsingDisabled(true);
           Config.config.clear();
           try {
   			config.load();
   		} catch (ConfigurationException e) {
   			// TODO Auto-generated catch block
   			e.printStackTrace();
   		}

        ArrayList<Object> tmpAnnotationClasses = (ArrayList<Object>) Config.config.getList("annotations.class[@name]");

        for (int i = 0; i < tmpAnnotationClasses.size(); i++) {
            TreeMap<String, CustomAnnotations> property = new TreeMap<String, CustomAnnotations>(new CaseInsensitiveComparator());
            State.getInstance().customClassAnnotations.put(tmpAnnotationClasses.get(i).toString(), Config.config.getString(String.format(
                    "annotations.class(%d).classAnnotation", i)));

            State.getInstance().customClassCode.put(tmpAnnotationClasses.get(i).toString(), Config.config.getString(String.format(
                    "annotations.class(%d).customClassCode", i)));

            State.getInstance().customClassCodeFields.put(tmpAnnotationClasses.get(i).toString(), Config.config.getString(String.format(
                    "annotations.class(%d).customClassCodeFields", i)));

            State.getInstance().customAnnotations.put(tmpAnnotationClasses.get(i).toString(), property);

            // Build the annotations list
            TreeSet<String> tmpAnnotationImports = new TreeSet<String>(new CaseInsensitiveComparator());
            tmpAnnotationImports.addAll((ArrayList<String>) Config.config.getList(String.format("annotations.class(%d).imports.import", i)));
            State.getInstance().customClassImports.put(tmpAnnotationClasses.get(i).toString(), tmpAnnotationImports);

            TreeSet<String> tmpAnnotationInterfaces = new TreeSet<String>(new CaseInsensitiveComparator());
            tmpAnnotationInterfaces.addAll((ArrayList<String>) Config.config.getList(String.format("annotations.class(%d).implements.interface", i)));
            State.getInstance().customClassInterfaces.put(tmpAnnotationClasses.get(i).toString(), tmpAnnotationInterfaces);

            TreeSet<String> tmpAnnotationExtends = new TreeSet<String>(new CaseInsensitiveComparator());
            tmpAnnotationExtends.addAll((ArrayList<String>) Config.config.getList(String.format("annotations.class(%d).extends.extend", i)));
            State.getInstance().customClassExtends.put(tmpAnnotationClasses.get(i).toString(), tmpAnnotationExtends);



            // Build the annotations list
            ArrayList<Object> tmpAnnotationProperties =
                    (ArrayList<Object>) Config.config.getList(String.format("annotations.class(%d).property[@name]", i));

            for (int j = 0; j < tmpAnnotationProperties.size(); j++) {
                ArrayList<Object> tmpProperties =
                        (ArrayList<Object>) Config.config.getList(String.format("annotations.class(%d).property(%d).annotation", i, j));
                CustomAnnotations annotation = new CustomAnnotations();
                property.put(Config.config.getString(String.format("annotations.class(%d).property(%d)[@name]", i, j)), annotation);

                for (int k = 0; k < tmpProperties.size(); k++) {
                    String type =
                    	Config.config.getString(String.format("annotations.class(%d).property(%d).annotation(%d)[@type]", i, j, k)).toUpperCase();
                    if (type.equals("PROPERTY")) {
                        annotation.getPropertyLevelAnnotations().add((String) tmpProperties.get(k));
                    }
                    else if (type.equals("GETTER")) {
                        annotation.getMethodLevelAnnotationsOnGetters().add((String) tmpProperties.get(k));
                    }
                    else if (type.equals("SETTER")) {
                        annotation.getMethodLevelAnnotationsOnSetters().add((String) tmpProperties.get(k));
                    }
                    else if (type.equals("SETTERPRECONDITION")) {
                        annotation.getMethodLevelSetterPrecondition().add((String) tmpProperties.get(k));
                    }
                    else if (type.equals("GETTERPRECONDITION")) {
                        annotation.getMethodLevelGetterPrecondition().add((String) tmpProperties.get(k));
                    }
                    else if (type.equals("SETTERPOSTCONDITION")) {
                        annotation.getMethodLevelSetterPostcondition().add((String) tmpProperties.get(k));
                    }
                    else if (type.equals("GETTERPOSTCONDITION")) {
                        annotation.getMethodLevelGetterPostcondition().add((String) tmpProperties.get(k));
                    }


                }
            }
        }
    }



    /**
     *
     */
    @SuppressWarnings("unchecked")
    private static void fillEnumMap() {
        ArrayList<Object> tmpEnumMap = (ArrayList<Object>) Config.config.getList("coalesceEnums.schema[@name]");
        for (int i = 0; i < tmpEnumMap.size(); i++) {
            String schema = (String) tmpEnumMap.get(i);
            TreeMap<String, String> map = State.getInstance().enumMappings.get(schema);
            if (map == null) {
                map = new TreeMap<String, String>(new CaseInsensitiveComparator());
                State.getInstance().enumMappings.put(schema, map);

            }

            ArrayList<Object> tmpEnum = (ArrayList<Object>) Config.config.getList(String.format("coalesceEnums.schema(%d).enum[@name]", i));
            for (int j = 0; j < tmpEnum.size(); j++) {
                String enumName = (String) tmpEnum.get(j);

                ArrayList<Object> fieldList =
                        (ArrayList<Object>) Config.config.getList(String.format("coalesceEnums.schema(%d).enum(%d).field", i, j));

                for (int k = 0; k < fieldList.size(); k++) {

                    String field = Config.config.getString(String.format("coalesceEnums.schema(%d).enum(%d).field(%d)", i, j, k));
                    // HbnPojoGen.log(field + " - "+enumName);
                    map.put(field, enumName);
                }
            }
        }
    }



    /**
     *
     */
    @SuppressWarnings("unchecked")
    private static void fillPackageMap() {
        ArrayList<Object> tmpPackageClasses = (ArrayList<Object>) Config.config.getList("dbPackageMap.map[@schema]");
        for (int i = 0; i < tmpPackageClasses.size(); i++) {
            PackageMap packageMap = new PackageMap();
            packageMap.setDaoPackage(Config.config.getString(String.format("dbPackageMap.map(%d).daoPackage", i)));
            packageMap.setDaoImplPackage(Config.config.getString(String.format("dbPackageMap.map(%d).daoImplPackage", i)));
            packageMap.setDataPackage(Config.config.getString(String.format("dbPackageMap.map(%d).dataPackage", i)));
            packageMap.setEnumPackage(Config.config.getString(String.format("dbPackageMap.map(%d).enumPackage", i)));
            packageMap.setEnumPackageTargetBase(Config.config.getString(String.format("dbPackageMap.map(%d).enumPackage[@targetbase]", i)));
            packageMap.setEnumSubtypePackage(Config.config.getString(String.format("dbPackageMap.map(%d).enumSubtypePackage", i)));
            packageMap.setFactoryPackage(Config.config.getString(String.format("dbPackageMap.map(%d).factoryPackage", i)));
            packageMap.setUtilPackage(Config.config.getString(String.format("dbPackageMap.map(%d).utilPackage", i)));
            packageMap.setObjectPackage(Config.config.getString(String.format("dbPackageMap.map(%d).objectPackage", i)));
            packageMap.setObjectInterfacePackage(Config.config.getString(String.format("dbPackageMap.map(%d).objectInterfacePackage", i)));
            State.getInstance().skipModelInterfaces(Config.config.getBoolean(String.format("dbPackageMap.map(%d).objectInterfacePackage[@skip]", i), false));
            packageMap.setObjectTableRepoPackage(Config.config.getString(String.format("dbPackageMap.map(%d).objectTableRepoPackage", i)));
            packageMap.setRepositoryFactoryPackage(Config.config.getString(String.format("dbPackageMap.map(%d).repositoryFactoryPackage", i)));
            State.getInstance().packageMaps.put(SyncUtils.removeUnderscores((String) tmpPackageClasses.get(i)), packageMap);
        }

        // test for default packagemap. Make sure there's always *something* to work upon
        PackageMap packageMap = State.getInstance().packageMaps.get("DEFAULT");
        if (packageMap == null) {
            packageMap = new PackageMap();
            State.getInstance().packageMaps.put("DEFAULT", packageMap);
        }

        if (packageMap.getDaoPackage() == null) {
            packageMap.setDaoPackage(String.format("%s.%s.model.dao.${DB}", State.getInstance().topLevel, State.getInstance().projectName));
        }
        if (packageMap.getObjectPackage() == null) {
            packageMap.setObjectPackage(String.format("%s.%s.model.obj.${DB}", State.getInstance().topLevel, State.getInstance().projectName));
        }
        if (packageMap.getObjectInterfacePackage() == null) {
            packageMap.setObjectInterfacePackage(String.format("%s.%s.model.obj.${DB}.iface", State.getInstance().topLevel, State.getInstance().projectName));
        }
        if (packageMap.getObjectTableRepoPackage() == null) {
            packageMap.setObjectTableRepoPackage(String.format("%s.%s.model.obj.${DB}.repository", State.getInstance().topLevel, State.getInstance().projectName));
        }
        if (packageMap.getRepositoryFactoryPackage() == null) {
            packageMap.setRepositoryFactoryPackage(String.format("%s.%s.model.obj.${DB}.repository.factory", State.getInstance().topLevel, State.getInstance().projectName));
        }
        if (packageMap.getDataPackage() == null) {
            packageMap.setDataPackage(String.format("%s.%s.services.data", State.getInstance().topLevel, State.getInstance().projectName));
        }
        if (packageMap.getEnumPackage() == null) {
            packageMap.setEnumPackage(String.format("%s.%s.enums.db.${DB}", State.getInstance().topLevel, State.getInstance().projectName));
        }
        if (packageMap.getEnumPackageTargetBase() == null) {
            packageMap.setEnumPackageTargetBase( 	State.getInstance().getSourceTarget());
        }
        if (packageMap.getEnumSubtypePackage() == null) {
            packageMap.setEnumSubtypePackage(String.format("%s.%s.enums.subtype.${DB}", State.getInstance().topLevel, State.getInstance().projectName));
        }
        if (packageMap.getFactoryPackage() == null) {
            packageMap.setFactoryPackage(String.format("%s.%s.factories.${DB}", State.getInstance().topLevel, State.getInstance().projectName));
        }
        if (packageMap.getUtilPackage() == null) {
            packageMap.setUtilPackage(String.format("%s.%s.util", State.getInstance().topLevel, State.getInstance().projectName));
        }
        if (packageMap.getDaoImplPackage() == null) {
            packageMap.setDaoImplPackage(String.format("%s.%s.model.dao.${DB}.impl", State.getInstance().topLevel, State.getInstance().projectName));
        }
    }



    /**
     *
     */
    @SuppressWarnings("unchecked")
    private static void fillTreatAsEnum() {
        ArrayList<Object> tmpEnumLink = (ArrayList<Object>) Config.config.getList("treatLinkAsEnum.schema[@name]");
        for (int i = 0; i < tmpEnumLink.size(); i++) {
            // packageMap.setObjectPackage(config.getString(String.format("dbPackageMap.map(%d).objectPackage", i)));
            // packageMaps.put(, packageMap);
            String schema = SyncUtils.removeUnderscores((String) tmpEnumLink.get(i));
            TreeMap<String, EnumMapping> definitions = State.getInstance().enumAsLinkMaps.get(schema);
            if (definitions == null) {
                definitions = new TreeMap<String, EnumMapping>(new CaseInsensitiveComparator());
                State.getInstance().enumAsLinkMaps.put(schema, definitions);
            }
            // HbnPojoGen.log(schema);
            ArrayList<Object> tmpEnumLinkField =
                    (ArrayList<Object>) Config.config.getList(String.format("treatLinkAsEnum.schema(%d).field[@src]", i));
            for (int j = 0; j < tmpEnumLinkField.size(); j++) {
                String srcdef = Config.config.getString(String.format("treatLinkAsEnum.schema(%d).field(%d)[@src]", i, j));
                String dstTable = Config.config.getString(String.format("treatLinkAsEnum.schema(%d).field(%d)[@dstTable]", i, j));
                String keyCol = Config.config.getString(String.format("treatLinkAsEnum.schema(%d).field(%d)[@keyColumnName]", i, j));
                String valCol = Config.config.getString(String.format("treatLinkAsEnum.schema(%d).field(%d)[@valueColumnName]", i, j));
                TreeSet<String> otherCol = new TreeSet<String>(Config.config.getList(String.format("treatLinkAsEnum.schema(%d).field(%d)[@otherColumnNames]", i, j)));

                String srctable = SyncUtils.removeUnderscores(SyncUtils.getTableCatalog(srcdef));
                String srcfield = SyncUtils.removeUnderscores(SyncUtils.getTableName(srcdef));

                srcdef = srctable + "." + srcfield;
                EnumMapping map = new EnumMapping();
                map.setKeyColumnLabel(keyCol);
                map.setValueColumnLabel(valCol);
                map.setDsttableFieldname(dstTable);
                map.setOtherColumnLabels(otherCol);
                definitions.put(srcdef, map);
            }
        }
    }

    /**
     *
     */
    @SuppressWarnings("unchecked")
    private static void fillRenameSection() {
        ArrayList<Object> tmpRename = (ArrayList<Object>) Config.config.getList("renaming.schema[@name]");
        for (int i = 0; i < tmpRename.size(); i++) {
            String schema = SyncUtils.removeUnderscores((String) tmpRename.get(i));
            TreeMap<String, String> definitions = State.getInstance().renameFieldMap.get(schema);
            if (definitions == null) {
                definitions = new TreeMap<String, String>(new CaseInsensitiveComparator());
                State.getInstance().renameFieldMap.put(schema, definitions);
            }
            // HbnPojoGen.log(schema);
            ArrayList<Object> tmpRenameField =
                    (ArrayList<Object>) Config.config.getList(String.format("renaming.schema(%d).field[@srcName]", i));
            for (int j = 0; j < tmpRenameField.size(); j++) {
                String srcdef = Config.config.getString(String.format("renaming.schema(%d).field(%d)[@srcName]", i, j));
                String dstName = Config.config.getString(String.format("renaming.schema(%d).field(%d)[@dstName]", i, j));
                String inverseName = Config.config.getString(String.format("renaming.schema(%d).field(%d)[@inverseName]", i, j));

                String srctable = SyncUtils.removeUnderscores(SyncUtils.getTableCatalog(srcdef));
                String srcfield = SyncUtils.removeUnderscores(SyncUtils.getTableName(srcdef));
                srcdef = srctable + "." + srcfield;
                definitions.put(srcdef, dstName + ":" + inverseName);
            }
        }
    }


    /**
     *
     */
    @SuppressWarnings("unchecked")
    private static void fillPreExecuteList() {
        ArrayList<Object> preExecuteList = (ArrayList<Object>) Config.config.getList("preexec.connections.connection");

        for (int i = 0; i < preExecuteList.size(); i++) {
            CustomDB pre = new CustomDB();
            pre.setStatements(new LinkedList<String>());
            pre.setConnectionURL(preExecuteList.get(i).toString());
            pre.setDriver(Config.config.getString("preexec.connections(" + i + ").driver"));
            pre.setConnectionUsername(Config.config.getString("preexec.connections(" + i + ").connectionUsername"));
            pre.setConnectionPassword(Config.config.getString("preexec.connections(" + i + ").connectionPassword"));
            pre.getStatements().addAll(Config.config.getList("preexec.connections(" + i + ").statements.statement"));
            State.getInstance().preExecList.add(pre);

        }
    }



    /**
     *
     */
    @SuppressWarnings("unchecked")
    private static void fillGeneratorMap() {
        ArrayList<String> genIdList = (ArrayList<String>) Config.config.getList("generatedId.map[@schema]");
        for (int i = 0; i < genIdList.size(); i++) {
            String schema = Config.config.getString(String.format("generatedId.map(%d)[@schema]", i));
            GeneratedValueSchemas genSchema = State.getInstance().generators.get(schema);
            if (genSchema == null) {
                genSchema = new GeneratedValueSchemas();
                State.getInstance().generators.put(schema, genSchema);
            }
            ArrayList<String> genIdTables = (ArrayList<String>) Config.config.getList(String.format("generatedId.map(%d).table[@name]", i));
            for (int j = 0; j < genIdTables.size(); j++) {
                String tableName = Config.config.getString(String.format("generatedId.map(%d).table(%d)[@name]", i, j));
                String fieldName = Config.config.getString(String.format("generatedId.map(%d).table(%d)[@field]", i, j));
                String generator = Config.config.getString(String.format("generatedId.map(%d).table(%d)[@generator]", i, j));

                GeneratedValueFields genFields = genSchema.getTables().get(tableName);
                if (genFields == null) {
                    genFields = new GeneratedValueFields();
                    genSchema.getTables().put(tableName, genFields);
                }

                genFields.getFields().put(fieldName, HbnPojoGen.mapGeneratorType(generator));
            }

        }

        // Add the default generator
        String defaultGenerator = Config.config.getString("generatedId.default[@generator]");
        String defaultGeneratorPattern = Config.config.getString("generatedId.default[@idpattern]");
        if (defaultGenerator == null || !HbnPojoGen.isValidGenerator(defaultGenerator)) {
            defaultGenerator = "AUTO";
        }
        if (defaultGeneratorPattern == null) {
            defaultGeneratorPattern = "${DB}_id";
        }
        if (HbnPojoGen.isValidGenerator(defaultGenerator)) {
            GeneratedValueSchemas genSchema = new GeneratedValueSchemas();
            GeneratedValueFields genField = new GeneratedValueFields();
            genField.getFields().put(defaultGeneratorPattern, HbnPojoGen.mapGeneratorType(defaultGenerator));
            genSchema.getTables().put("*", genField);
            State.getInstance().generators.put("DEFAULT", genSchema);
        }
    }



    /**
     *
     */
    @SuppressWarnings("unchecked")
    private static void fillPrepopulateList() {
        // Build the prepopulate list
        ArrayList<Object> prepopList = (ArrayList<Object>) Config.config.getList("prepopulateDB.connections.connection");

        for (int i = 0; i < prepopList.size(); i++) {
            CustomDB pre = new CustomDB();
            pre.setStatements(new LinkedList<String>());
            pre.setConnectionURL(prepopList.get(i).toString());
            pre.setDriver(Config.config.getString("prepopulateDB.connections(" + i + ").driver"));
            pre.setConnectionUsername(Config.config.getString("prepopulateDB.connections(" + i + ").connectionUsername"));
            pre.setConnectionPassword(Config.config.getString("prepopulateDB.connections(" + i + ").connectionPassword"));
            pre.getStatements().addAll(Config.config.getList("prepopulateDB.connections(" + i + ").statements.statement"));
            State.getInstance().prepopulateList.add(pre);
        }
    }
}
