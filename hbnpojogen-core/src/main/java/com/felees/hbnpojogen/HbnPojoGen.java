package com.felees.hbnpojogen;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.maven.plugin.logging.Log;

import com.felees.hbnpojogen.SyncUtils.CommitResults;
import com.felees.hbnpojogen.obj.Clazz;
import com.felees.hbnpojogen.obj.GeneratorEnum;



/**
 * Hibernate Pojo Generator.
 *
 * Usage: Edit the input xml (see sample.xml for a sample) and pass it as an arg
 *
 * @author wallacew
 *
 */
public class HbnPojoGen {

    /** Config file setting */
    static String jdbcConnection;
    /** Config file setting */
    static String driver;
	private static Log outputLogger;


    private static Set<String> errors = new HashSet<String>();

    public static URL[] getSkeletonURL(String source) throws IOException {

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Enumeration[] e = new Enumeration[] {
                cl.getResources(source),
            };

        Set all = new LinkedHashSet();
        URL url;
        URLConnection conn;
        JarFile jarFile = null;
        for (int i = 0, s = e.length; i < s; ++i) {
            while (e[i].hasMoreElements()) {
                url = (URL) e[i].nextElement();
                conn = url.openConnection();
                conn.setUseCaches(false);
                conn.setDefaultUseCaches(false);
                if (conn instanceof JarURLConnection) {
                    jarFile = ((JarURLConnection) conn).getJarFile();
                }

                if (jarFile != null) {
                    searchJar(cl, all, jarFile);
                } else {
                    searchDir(all, new File(URLDecoder.decode(url.getFile(), "UTF-8")));
                }
            }
        }
        URL[] urlArray = (URL[]) all.toArray(new URL[all.size()]);
        return urlArray;
    }

    private static boolean searchDir(Set result, File file)
            throws IOException {
        if (file.exists() && file.isDirectory()) {
            File[] fc = file.listFiles();
            String path;
            URL src;
            for (int i = 0; i < fc.length; i++) {
                path = fc[i].getAbsolutePath();
                if (fc[i].isDirectory()) {
                    result.add(fc[i].toURI().toURL());
                    searchDir(result, fc[i]);
                }
                    result.add(fc[i].toURI().toURL());
            }
            return true;
        }
        return false;
    }





    private static void searchJar(ClassLoader cl, Set result, JarFile file) throws IOException {
        Enumeration e = file.entries();
        JarEntry entry;
        String name;
        while (e.hasMoreElements()) {
            try {
                entry = (JarEntry) e.nextElement();
            } catch (Throwable t) {
                continue;
            }
            name = entry.getName();
                Enumeration e2 = cl.getResources(name);
                while (e2.hasMoreElements()) {
                    result.add(e2.nextElement());
                }
        }

}


    /**
     * This is the core function
     *
     * @param targetFolder Where files are to be created
     * @param dbmd DB Connection meta data
     * @param dbCatalog Database catalog
     * @param connection Connection to use
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws IOException
     * @throws Exception
     */
    private static void sync(final String targetFolder, final DatabaseMetaData dbmd, final String dbCatalog, final Connection connection)
            throws ClassNotFoundException, SQLException, IOException, Exception {


        long startTime = System.currentTimeMillis();

        log("Stage 1: Copying skeletons");
        URL[] urls = getSkeletonURL("skeleton");
        SyncUtils.copyDirectory(urls, targetFolder);

        BufferedReader br = new BufferedReader(new InputStreamReader(HbnPojoGen.class.getResource("/synchronizer.version").openStream()));
        State.getInstance().setSynchronizerVersion(br.readLine());
        br.close();

        //SyncUtils.copyDirectory(new File("skeleton"), targetFolder);

        // Fetch the commit order of the db, this makes creation of test cases easier later on
        // as dependencies would have already been resolved.

        LinkedList<String> commitOrder = new LinkedList<String>();
        LinkedList<String> tmp = null;

        tmp = new LinkedList<String>();

        switch (State.getInstance().schemaRestrict) {
            case 0: // RESTRICT
                log("Stage 2: Getting commit order in 'RESTRICT' strategy");
                State.getInstance().commitResult = SyncUtils.getCommitOrder(connection, true, false);
                tmp.addAll(State.getInstance().commitResult.getCommitList());
                break;
            case 1: // PARTIAL
                log("Stage 2: Getting commit order in 'PARTIAL' strategy");
                State.getInstance().commitResult = SyncUtils.getCommitOrder(connection, false, false);
                tmp.addAll(State.getInstance().commitResult.getCommitList());
                break;
            case 2: // FULL
                log("Stage 2: Getting commit order in 'FULL' strategy");
                State.getInstance().commitResult = SyncUtils.getCommitOrder(connection, false, true);
                tmp.addAll(State.getInstance().commitResult.getCommitList());
                break;
            case 3: // ALL
                log("Stage 2: Getting commit order in 'ALL' strategy");
                tmp = new LinkedList<String>();
                State.getInstance().commitResult = SyncUtils.getCompleteCommitOrder(connection);
                tmp.addAll(State.getInstance().commitResult.getCommitList());
                break;
            default:
                State.getInstance().commitResult = new CommitResults();
                HbnPojoGen.logE("Stage 2: Getting commit order in [Error: Unknown commit order.]");
                break;
        }

//        log(State.getInstance().commitResult);
        // find loops
        State.getInstance().commitResult.buildCycleList();

        int ignoredTables = 0;
        for (String table : tmp) {
            if (!State.getInstance().ignoreTableList.contains(table.toUpperCase()) &&
                    (!State.getInstance().ignoreTableList.contains(SyncUtils.getTableCatalog(table).toUpperCase() + ".*")) &&
                    (!State.getInstance().ignoreTableList.contains("*." + SyncUtils.getTableName(table).toUpperCase()))) {
                commitOrder.add(table);
            }
            else {
                ignoredTables++;
            }
            State.getInstance().catalogs.add(SyncUtils.getTableCatalog(table));
            if (State.getInstance().dbMode == 2){ // postgresql
            	State.getInstance().schemas.add(SyncUtils.getTableSchema(table));
            } else {
            	State.getInstance().schemas.add(SyncUtils.getTableCatalog(table));

            }
        }

        // Create all target folders
        String srcFolder = targetFolder + "/"+State.getInstance().getSrcFolder()+"/" + State.getInstance().topLevel.replaceAll("\\.", "/");
        // Read in our table model
        log("Stage 3: Parsing tables");
        Core.parseTables(dbmd, dbCatalog, connection, commitOrder);

        log("Stage 4: Building object model");
        Core.buildObjectModel(State.getInstance().classes, commitOrder);

        int tmpCount = 0;
        for (byte b : State.getInstance().getProjectName().getBytes()) {
            tmpCount++;
            VelocityWriters.serialCount += 10 * tmpCount * b; //
        }

        // Write all the classes
        log("Stage 5: Writing classes");
        VelocityWriters.writeClasses(targetFolder, State.getInstance().classes);

        log("Stage 6: Writing interfaces" + (State.getInstance().isSkipModelInterfaces() ? " [disabled]" : ""));
        if (!State.getInstance().isSkipModelInterfaces()){
        		VelocityWriters.writeInterfaceClasses(targetFolder, State.getInstance().classes);
        }
        // Dump the enums
        log("Stage 7: Writing enums");
        VelocityWriters.writeEnums(targetFolder);
        VelocityWriters.writeSubtypeEnums(targetFolder);


        log("Stage 8: Writing DAO Factory classes");
        VelocityWriters.writeOutDaoFactoryClass(State.getInstance().classes, targetFolder, State.getInstance().schemas);

        log("Stage 9: Writing Spring, EhCache configs, etc");
        if (!State.getInstance().isDisableApplicationContext()){
        		VelocityWriters.writeSpringApplicationContext(targetFolder, State.getInstance().classes, dbCatalog);
        }
//        VelocityWriters.writeAntBuildFile(targetFolder, dbCatalog);
        VelocityWriters.writeEHCache(targetFolder);
        VelocityWriters.writeUtils(targetFolder);
        if (!State.getInstance().isPropertyPlaceholderConfigurerSuppressBean()){
        		VelocityWriters.writeSpringOverrideFile(targetFolder);
        }

        log("Stage 10: Writing DAO Test classes");
        VelocityWriters.writeOutDaoTestClass(targetFolder, State.getInstance().classes, commitOrder, srcFolder);

        log("Stage 11: Writing Data pool Factory classes");
        VelocityWriters.writeOutDataPoolFactoryClass(State.getInstance().classes, targetFolder, State.getInstance().schemas);

        log("Stage 12: Writing Data layer helpers");
        VelocityWriters.writeOutDataLayerHelpers(targetFolder, State.getInstance().classes, State.getInstance().schemas);


        if (!State.getInstance().isMavenEnabled() || !State.getInstance().isMavenPomEnabled()){
        	log("Stage 13: Writing Maven pom.xml [Disabled]");
        } else {
        	log("Stage 13: Writing Maven pom.xml");
        	VelocityWriters.writeMavenPom(targetFolder);
        }

        	log("Stage 14: Writing DB Version Check helpers");
        	if (State.getInstance().isVersionCheckEnabled()){
        		VelocityWriters.writeOutDBVersionCheck(targetFolder, State.getInstance().classes, State.getInstance().schemas);
        	}
        if (State.getInstance().isEnableMockitoBeans()){
        	log("Stage 15: Writing mockito test context file");
        	VelocityWriters.writeOutMockitoBean(targetFolder, State.getInstance().classes, State.getInstance().schemas);
        }

        printStatistics(State.getInstance().classes, ignoredTables);

        long endTime = System.currentTimeMillis() - startTime;
        log("");
        log("All done! Time taken: " + endTime / 1000 + " sec");
        // Save what we found out, for easier re-use in other libraries.
        if (State.getInstance().isEnableStateSave()) {
            log("Dumping state to disk as requested by config file");

            State.getInstance().serializeState(targetFolder + File.separator + State.getInstance().getResourceFolder() + File.separator + "synchronizer.state");
        }
        log("Output written to  : " + targetFolder);


    }



    /**
     * @param classes
     * @param ignoredTables
     */
    private static void printStatistics(TreeMap<String, Clazz> classes, int ignoredTables) {
        int embeddable = 0;
        int subclasses = 0;
        int superclasses = 0;
        int jointables = 0;
        int hiddenJoinTables = 0;
        int nameClashed = 0;
        int properties = 0;
        int fields = 0;
        int abstractClasses = 0;

        for (Clazz clazz : classes.values()) {
            if (clazz.isEmbeddable()) {
                embeddable++;
            }
            if (clazz.isSubclass()) {
                subclasses++;
            }
            if (clazz.isSuperclass()) {
                superclasses++;
            }
            if (clazz.isJoinTable()) {
                jointables++;
            }
            if (clazz.isHiddenJoinTable()) {
                hiddenJoinTables++;
            }
            if (clazz.isNameAmbiguityPossible()) {
                nameClashed++;
            }
            if (clazz.isAbstractClass()) {
                abstractClasses++;
            }

            fields += clazz.getTableObj().getFields().size();
            properties += clazz.getProperties().size();
        }
        log(String.format("Tables parsed      : %d", State.getInstance().tables.size()));
        log(String.format("Classes written    : %d", classes.size()));
        log(String.format("Table Fields       : %d", fields));
        log(String.format("Properties         : %d", properties));
        log(String.format("Embeddable classes : %d", embeddable));
        log(String.format("Sub Classes        : %d", subclasses));
        log(String.format("Super Classes      : %d", superclasses));
        log(String.format("Join Tables        : %d", jointables));
        log(String.format("Hidden Join Tables : %d", hiddenJoinTables));
        log(String.format("Class Name clashes : %d", nameClashed));
        log(String.format("Tables ignored     : %d", ignoredTables));
        log(String.format("Abstract Classes   : %d", abstractClasses));

        if (!State.getInstance().noOutPutForSchemaList.isEmpty()) {
            log("Warning: Config file specifies no output wanted for schemas: " + State.getInstance().noOutPutForSchemaList);
            log("Warning: Compilation errors may result");
        }

        if (!State.getInstance().noOutPutForExceptSchemaList.isEmpty()) {
            log("Warning: Config file specifies no output wanted for any schemas except: " + State.getInstance().noOutPutForExceptSchemaList);
            log("Warning: Compilation errors may result");
        }

    }



    /**
     * @param generator
     * @return enum equivalent
     */
    static GeneratorEnum mapGeneratorType(String generator) {
        if (generator.equalsIgnoreCase("AUTO")) {
            return GeneratorEnum.AUTO;
        }
        if (generator.equalsIgnoreCase("UUID")) {
            return GeneratorEnum.UUID;
        }
        if (generator.equalsIgnoreCase("UUID-WITHOUTDASHES")) {
            return GeneratorEnum.UUIDWithoutDashes;
        }
        if (generator.equalsIgnoreCase("GUID")) {
        	return GeneratorEnum.GUID;
        }
        if (generator.equalsIgnoreCase("CUSTOM")) {
            return GeneratorEnum.CUSTOM;
        }
        if (generator.equalsIgnoreCase("IDAWARE")) {
            return GeneratorEnum.IDAWARE;
        }
        if (generator.equalsIgnoreCase("PKS")) {
            return GeneratorEnum.PKS;
        }

        return null;
    }



    /**
     * @param generator
     * @return true/false
     */
    static boolean isValidGenerator(String generator) {
        return mapGeneratorType(generator) != null;
    }

    /**
     * @param log
     */
    public static void setLog(Log log){
    	outputLogger = log;
    }

    public static void log(String s){
    	if (outputLogger == null){
    		System.out.println(s);
    	} else {
    		outputLogger.info(s);
    	}
    }


    public static void logE(String s){
    	if (!errors.contains(s)){

    	if (outputLogger == null){
    		System.err.println(s);
    	} else {
    		outputLogger.error(s);
    	}
    	errors.add(s);
    	}
    }

    public static void run(String config, String overridePath, String overrideIP){
    	try{

        String path = Config.parseConfig(config, overridePath, overrideIP);

        log("Hibernate POJO Generator\n");
        log("Reading from config: " + new File(config).getAbsolutePath());

        // Setup the DB connection
        Class.forName(driver);

        Properties props = new Properties();

        props.put("user", State.getInstance().dbUsername);
        props.put("password", State.getInstance().dbPassword);
        props.put("useInformationSchema", "false");

        Connection connection = DriverManager.getConnection(jdbcConnection, props);

        State.getInstance().dbCatalog = connection.getCatalog();


        DatabaseMetaData dbmd = connection.getMetaData();
        sync(path, dbmd, State.getInstance().dbCatalog, connection);

    }
    catch (Exception e) {
        e.printStackTrace();
    }
    }

    /**
     * Main method
     *
     * @param args
     */
    public static void main(String[] args) {

            if (args.length < 1) {
                log("This will generate: Source files (1 per table - including the annotations), enums, dao, test cases");
                log("Syntax: hbnSync configfile.xml [target-dir (override)] [ipaddress to use as source (override)] ");
                log("Eg: java -jar hbnSync.jar config.xml /var/tmp 10.0.0.2");
                log("Edit the templates (templates/*) and regenerate if the style is not to your liking");
                System.exit(1);
            }

            System.setProperty("line.separator", "\n");
            String overridePath = null;
            String overrideIP = null;
            String config = args[0];
            if (args.length > 1) {
            	overridePath = args[1];
            }
            if (args.length > 2) {
                overrideIP = args[2];
            }

            run(config, overridePath, overrideIP);


    }


}
