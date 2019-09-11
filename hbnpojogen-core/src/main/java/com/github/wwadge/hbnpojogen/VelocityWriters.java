package com.github.wwadge.hbnpojogen;


import com.github.wwadge.hbnpojogen.db.FieldObj;
import com.github.wwadge.hbnpojogen.db.TableObj;
import com.github.wwadge.hbnpojogen.obj.Clazz;
import com.github.wwadge.hbnpojogen.obj.JoinTable;
import com.github.wwadge.hbnpojogen.obj.PropertyObj;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import java.io.*;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.Map.Entry;


/**
 * Files are dumped via velocity from here
 *
 * @author wallacew
 */
public class VelocityWriters {

    /**
     * Constant
     */
    private static final String TABLE_SET_CYCLES = "tableSetCycles";
    /**
     * Constant
     */
    private static final String DISABLE_CLEAN_TABLES = "disableCleanTables";
    /**
     * Constant
     */
    private static final String DISABLE_TEST_ROLLBACK = "disableTestRollback";
    /**
     * Constant
     */
    private static final String DAO_FACTORIES = "daoFactories";
    /**
     * Constant
     */
    private static final String PRETTYCATALOG = "prettyCatalog";
    /**
     * Constant
     */
    private static final String CATALOG = "catalog";
    /**
     * Constant
     */
    private static final String CLASSES = "classes";
    /**
     * Constant
     */
    private static final String TABLES_REVERSE = "tablesReverse";
    /**
     * Constant
     */
    private static final String THIS = "this";
    /**
     * Constant
     */
    private static final String PRE_EXEC = "preExec";
    /**
     * Constant
     */
    private static final String PREPOPULATE = "prepopulate";
    /**
     * Constant
     */
    private static final String TOPLEVEL = "toplevel";
    /**
     * Constant
     */
    private static final String CLASSCONST = "class";
    /**
     * Constant
     */
    private static final String IMPORTS = "imports";
    /**
     * Constant
     */
    private static final String MODEL = "model";
    /**
     * Constant
     */
    private static final String PROJECTNAME = "projectname";
    /**
     * Constant
     */
    public static long serialCount = 0xDEADBABE;
    /**
     * For ignoring imports in interface.
     */
    private static TreeSet<String> notForInterfaceImports = new TreeSet<String>(new CaseInsensitiveComparator());
    private static boolean wroteAnEnum = false;


    /**
     * Gets
     *
     * @return
     */
    public static TreeSet<String> getNotForInterfaceImports() {
        return notForInterfaceImports;
    }

    static {
        notForInterfaceImports.add("java.io.Serializable");
        notForInterfaceImports.add("java.util.HashSet");
        notForInterfaceImports.add("javax.persistence.*");

        notForInterfaceImports.add("org.hibernate.annotations.Parameter");
        notForInterfaceImports.add("org.hibernate.annotations.Type");
        notForInterfaceImports.add("org.hibernate.annotations.TypeDef");
        notForInterfaceImports.add("org.hibernate.annotations.TypeDefs");
        notForInterfaceImports.add("javax.persistence.Inheritance");
        notForInterfaceImports.add("javax.persistence.InheritanceType");
        notForInterfaceImports.add("javax.persistence.PrimaryKeyJoinColumn");
        notForInterfaceImports.add("java.util.Arrays");
        notForInterfaceImports.add("org.hibernate.validator.Size");
        notForInterfaceImports.add("org.hibernate.validator.NotNull");
        notForInterfaceImports.add("org.hibernate.validator.NotEmpty");
        notForInterfaceImports.add("org.hibernate.validator.Valid");
        notForInterfaceImports.add("org.hibernate.proxy.HibernateProxy");
        notForInterfaceImports.add("java.util.Map");
        notForInterfaceImports.add("java.util.Collections");
        notForInterfaceImports.add("java.util.WeakHashMap");
        notForInterfaceImports.add("com.github.wwadge.hbnpojogen.persistence.validator.Mandatory");
//        notForInterfaceImports.add("com.github.wwadge.hbnpojogen.persistence.IPojoGenEntity");


        for (Clazz clazz : State.getInstance().getClasses().values()) {
            TreeSet<String> customClassImports = State.getInstance().customClassImports.get(clazz.getClassPackage() + "." + clazz.getClassName());
            if (customClassImports != null) {
                notForInterfaceImports.addAll(customClassImports);
            }

            customClassImports = State.getInstance().customClassImports.get("*." + clazz.getClassName());
            if (customClassImports != null) {
                notForInterfaceImports.addAll(customClassImports);
            }

            customClassImports = State.getInstance().customClassImports.get(clazz.getClassPackage() + ".*");
            if (customClassImports != null) {
                notForInterfaceImports.addAll(customClassImports);
            }

        }

        TreeSet<String> customClassImports = State.getInstance().customClassImports.get("*.*");
        if (customClassImports != null) {
            notForInterfaceImports.addAll(customClassImports);
        }


    }


    /**
     * Writes out the DAO class
     *
     * @param projectName
     * @param targetFolder
     * @param clazz
     * @throws ResourceNotFoundException
     * @throws ParseErrorException
     * @throws MethodInvocationException
     * @throws IOException
     * @throws Exception
     */
    public static void writeOutDaoClass(String projectName, String targetFolder, Clazz clazz)
            throws ResourceNotFoundException, ParseErrorException, MethodInvocationException, IOException, Exception {
        if (!State.getInstance().isEnableSpringData() && !Core.skipSchemaWrite(clazz) && (!clazz.isHiddenJoinTable())) {
            VelocityContext context = new VelocityContext();

            context.put(PROJECTNAME, projectName);
            context.put(CLASSCONST, clazz);
            context.put(TOPLEVEL, State.getInstance().topLevel);
            String pkImport = " ";
            if (clazz.hasEmbeddableClass()) {
                pkImport = "import " + clazz.getEmbeddableClass().getFullClassName() + ";";
            }
            if (clazz.isEmbeddable() && !clazz.isSubclass()) {
                pkImport = "import java.io.Serializable;";
            }


            context.put("importPK", pkImport);
            context.put("packagename", SyncUtils.getConfigPackage(clazz.getTableObj().getDbCat(), PackageTypeEnum.DAOIMPL));
            context.put("daoImport", SyncUtils.getConfigPackage(clazz.getTableObj().getDbCat(), PackageTypeEnum.DAO));
            context.put("objpackagename", SyncUtils.getConfigPackage(clazz.getTableObj().getDbCat(), PackageTypeEnum.OBJECT) + "." +
                    clazz.getClassName());

            String tmp = getAndCreateDaoPath(targetFolder + "/" + State.getInstance().getSrcFolder() + "/", clazz);
            PrintWriter daoWriter = new PrintWriter(new BufferedWriter(new FileWriter(tmp, false)));
            Template daoTemplate = Velocity.getTemplate("templates/dao.vm");
            daoTemplate.merge(context, daoWriter);
            daoWriter.close();

            context.put("packagename", SyncUtils.getConfigPackage(clazz.getTableObj().getDbCat(), PackageTypeEnum.DAO));
            tmp = getAndCreateDaoInterfacePath(targetFolder + "/" + State.getInstance().getSrcFolder() + "/", clazz);
            daoWriter = new PrintWriter(new BufferedWriter(new FileWriter(tmp, false)));
            daoTemplate = Velocity.getTemplate("templates/daoInterface.vm");
            daoTemplate.merge(context, daoWriter);
            daoWriter.close();
        }
    }


    /**
     * Returns the dao path.
     *
     * @param targetFolder
     * @param clazz
     * @return a valid path
     */
    private static String getAndCreateDaoPath(String targetFolder, Clazz clazz) {

        String config = SyncUtils.packageToDir(SyncUtils.getConfigPackage(clazz.getTableObj().getDbCat(), PackageTypeEnum.DAOIMPL));
        new File(targetFolder + "/" + config).mkdirs();

        String result = targetFolder + "/" + config + "/" + clazz.getClassName() + "DaoImpl.java";
        return result;
    }


    /**
     * Returns the dao path.
     *
     * @param targetFolder
     * @param clazz
     * @return a valid path
     */
    private static String getAndCreateRepoPath(String targetFolder, Clazz clazz) {

        String config = SyncUtils.packageToDir(SyncUtils.getConfigPackage(clazz.getTableObj().getDbCat(), PackageTypeEnum.TABLE_REPO));
        new File(targetFolder + "/" + config).mkdirs();

        String result = targetFolder + "/" + config + "/" + clazz.getClassName() + "Repository.java";
        return result;
    }

    /**
     * Returns the dao path.
     *
     * @param targetFolder
     * @param clazz
     * @return a valid path
     */
    private static void getAndCreateRepoFactoryPath(String targetFolder, Clazz clazz) {

        String config = SyncUtils.packageToDir(SyncUtils.getConfigPackage(clazz.getTableObj().getDbCat(), PackageTypeEnum.TABLE_REPO_FACTORY));
        new File(targetFolder + "/" + config).mkdirs();


    }

    /**
     * Returns the dao path.
     *
     * @param targetFolder
     * @param clazz
     * @return a valid path
     */
    private static String getAndCreateDaoInterfacePath(String targetFolder, Clazz clazz) {

        String config = SyncUtils.packageToDir(SyncUtils.getConfigPackage(clazz.getTableObj().getDbCat(), PackageTypeEnum.DAO));
        new File(targetFolder + "/" + config).mkdirs();

        String result = targetFolder + "/" + config + "/" + clazz.getClassName() + "Dao.java";
        return result;
    }


    /**
     * @param targetFolder
     * @param catalog
     * @param name
     * @return a valid path
     */
    private static String getAndCreateEnumPath(String targetFolder, String catalog, String name) {
        String config = SyncUtils.packageToDir(SyncUtils.getConfigPackage(catalog, PackageTypeEnum.ENUM));
        new File(targetFolder + "/" + config).mkdirs();

        String result = targetFolder + "/" + config + "/" + name + ".java";
        return result;
    }

    /**
     * @param targetFolder
     * @param catalog
     * @param name
     * @return a valid path
     */
    private static String getAndCreateSubtypeEnumPath(String targetFolder, String catalog, String name) {
        String config = SyncUtils.packageToDir(SyncUtils.getConfigPackage(catalog, PackageTypeEnum.SUBTYPE_ENUM));
        new File(targetFolder + "/" + config).mkdirs();

        String result = targetFolder + "/" + config + "/" + name + ".java";
        return result;
    }


    /**
     * @param targetFolder
     * @param clazz
     * @param doEmbeddable
     * @param isInterface
     * @return a valid path
     */
    private static String getAndCreateClassPath(String targetFolder, Clazz clazz, boolean doEmbeddable, boolean isInterface) {
        PackageTypeEnum type = isInterface ? PackageTypeEnum.OBJECTINTERFACE : PackageTypeEnum.OBJECT;
        String config = SyncUtils.packageToDir(SyncUtils.getConfigPackage(clazz.getTableObj().getDbCat(), type));
        new File(targetFolder + "/" + config).mkdirs();

        String result = targetFolder + "/" + config + "/" + (isInterface ? "I" : "") + clazz.getClassName();
        if (doEmbeddable && clazz.getEmbeddableClass() != null) {
            result = targetFolder + "/" + config + "/" + (isInterface ? "I" : "") + clazz.getClassNameNoSuffix();
            result = result + "PK" + clazz.getSuffix();
        }

        result += ".java";
        return result;
    }

    private static String getAndCreateOpenApiPath(String targetFolder, Clazz clazz) {
        PackageTypeEnum type = PackageTypeEnum.OPENAPI_SCHEMA;
        String config = State.getInstance().getOpenApiOutputDir();
        new File(config).mkdirs();

        String result = config + "/" + clazz.getClassName();
        result += ".yaml";
        return result;
    }

    /**
     * @param targetFolder
     * @param catalog
     * @return a valid path
     */
    private static String getAndCreateDataPoolFactoryPath(String targetFolder, String catalog) {
        String prettyCatalog = SyncUtils.removeUnderscores(SyncUtils.upfirstChar(catalog));
        String config = SyncUtils.packageToDir(SyncUtils.getConfigPackage(catalog, PackageTypeEnum.FACTORY));
        new File(targetFolder + "/" + config).mkdirs();

        String result = targetFolder + "/" + config + "/" + prettyCatalog + "DataPoolFactory.java";
        return result;

    }

    /**
     * @param targetFolder
     * @param catalog
     * @return a valid path
     */
    private static String getAndCreateDataPoolFactoryInterfacePath(String targetFolder, String catalog) {
        String config = SyncUtils.packageToDir(SyncUtils.getConfigPackage(catalog, PackageTypeEnum.FACTORY));
        new File(targetFolder + "/" + config).mkdirs();

        String result = targetFolder + "/" + config + "/" + "DataPoolFactory.java";
        return result;

    }

    /**
     * @param targetFolder
     * @return a valid path
     */
    private static String getAndCreateUtilPath(String targetFolder) {
        String config = SyncUtils.packageToDir(SyncUtils.getConfigPackage("", PackageTypeEnum.UTIL));
        new File(targetFolder + "/" + State.getInstance().getSrcFolder() + "/" + config).mkdirs();

        String result = targetFolder + "/" + State.getInstance().getSrcFolder() + "/" + config + "/";
        return result;

    }

    /**
     * @param targetFolder
     * @return a valid path
     */
    private static String getAndCreateRepoFactoryPath(String targetFolder) {
        String config = SyncUtils.packageToDir(SyncUtils.getConfigPackage("", PackageTypeEnum.TABLE_REPO_FACTORY));
        new File(targetFolder + "/" + State.getInstance().getSrcFolder() + "/" + config).mkdirs();

        String result = targetFolder + "/" + State.getInstance().getSrcFolder() + "/" + config + "/";
        return result;

    }

    private static String getAndCreateTestPath(String targetFolder) {

        String tmp =
                State.getInstance().getSourceTarget() + "/" + State.getInstance().getTestFolder() + "/" +
                        State.getInstance().getTopLevel().replaceAll("\\.", "/") + "/" + State.getInstance().projectName.replaceAll("\\.", "/");
        new File(tmp).mkdirs();


        return tmp + "/";

    }


    /**
     * @param catalog
     * @param targetFolder
     * @param suffix
     * @return a valid path
     */
    private static String getAndCreateDataLayerHelper(String catalog, String targetFolder, String suffix) {
        String prettyCatalog = SyncUtils.removeUnderscores(SyncUtils.upfirstChar(catalog));

        String config = SyncUtils.packageToDir(SyncUtils.getConfigPackage(catalog, PackageTypeEnum.DATA));
        new File(targetFolder + "/" + config).mkdirs();

        String result = targetFolder + "/" + config + "/DataLayer" + prettyCatalog + suffix;
        result += ".java";
        return result;
    }


    /**
     * @param catalog
     * @param targetFolder
     * @return a valid path
     */
    private static String getAndCreateDBVersionCheckLayerHelper(String catalog, String targetFolder) {
        String prettyCatalog = SyncUtils.removeUnderscores(SyncUtils.upfirstChar(catalog));

        String config = SyncUtils.packageToDir(SyncUtils.getConfigPackage(catalog, PackageTypeEnum.DATA));
        new File(targetFolder + "/" + config).mkdirs();

        String result = targetFolder + "/" + config + "/DBVersionCheck" + prettyCatalog;
        result += ".java";
        return result;
    }


    /**
     * @param targetFolder
     * @param catalog
     * @return a valid path
     */
    private static String getAndCreateFactoryPath(String targetFolder, String catalog) {
        String config = SyncUtils.packageToDir(SyncUtils.getConfigPackage(catalog, PackageTypeEnum.FACTORY));
        new File(targetFolder + "/" + config).mkdirs();

        String result = targetFolder + "/" + config + "/Hibernate" + SyncUtils.removeUnderscores(SyncUtils.upfirstChar(catalog)) + "DaoFactory.java";
        return result;
    }


    public static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(100);
        buffer.put(bytes);
        buffer.flip();//need flip
        return buffer.getLong();
    }

    /**
     * Writes out the class representing the table
     *
     * @param projectName
     * @param clazz
     * @param classWriter
     * @param isInterface
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public static void writeClass(final String projectName, final Clazz clazz, final PrintWriter classWriter, boolean isInterface, boolean isOpenApiSchema)
            throws IOException, NoSuchAlgorithmException {
        if (!Core.skipSchemaWrite(clazz)) {

            TreeSet<String> imports = new TreeSet<String>();
            VelocityContext context = new VelocityContext();
            context.put(PROJECTNAME, projectName);
            context.put(MODEL, MODEL);


            String classAnnotations = "";
            String typedefs = "";
            for (String s : Iterables.concat(clazz.getClassAnnotation(), clazz.getClassTypedefsAnnotation())) {
                if (s.indexOf("TypeDef") > -1) {
                    typedefs += "\t\t" + s + ",\n";
                } else {
                    classAnnotations += "\t\t" + s + "\n";
                }
            }

            if (clazz.hasPropertyWithMoneyType()) {
                String type = State.getInstance().getCustomMoneyType().substring(State.getInstance().getCustomMoneyType().lastIndexOf(".") + 1);
                typedefs += "\t\t@TypeDef(name = \"moneyAmountWithCurrencyType\", typeClass = " + type + ".class),\n";
            }
            if (clazz.hasPropertyWithCurrencyType()) {
                String type = State.getInstance().getCustomCurrencyUnitType().substring(State.getInstance().getCustomCurrencyUnitType().lastIndexOf(".") + 1);
                typedefs += "\t\t@TypeDef(name = \"currencyUnitType\", typeClass = " + type + ".class),\n";
            }
            if (clazz.hasPropertyWithEncryptedType()) {
                typedefs += "\t\t@TypeDef(name = \"encryptedString\", typeClass = EncryptedStringType.class, " +
                        "\n\t\t\tparameters= {@Parameter(name=\"encryptorRegisteredName\", value=\"STRING_ENCRYPTOR\")}\n\t\t),\n";
                clazz.getImports().add("org.hibernate.annotations.Parameter");

            }

            if (!typedefs.isEmpty()) {
                typedefs = "@TypeDefs(value = {\n" + typedefs.substring(0, typedefs.lastIndexOf(",")) + "\n})";
                classAnnotations += typedefs;
                clazz.getImports().add("org.hibernate.annotations.TypeDefs");

            }

            if (isInterface) {
                if (clazz.isSubclass()) {
                    imports.add(SyncUtils.getConfigPackage(clazz.getExtendsFrom().getClazz().getTableObj().getDbCat(), PackageTypeEnum.OBJECTINTERFACE) + ".I" + clazz.getExtendsFrom().getClazz().getClassName());
                }

                // let's add all our imports again because we're not in the same
                // package. Also if we're extending another interface, make sure
                // we also remove the redundant imports too.
                for (String importLine : clazz.getImports()) {
                    if (!notForInterfaceImports.contains(importLine) && !importLine.equalsIgnoreCase(clazz.getDataLayerImplFullClassName())) {
                        imports.add(importLine);
                    }
                }

                for (JoinTable joinTable : clazz.getJoinMappings()) {
                    imports.add(joinTable.getDstProperty().getClazz().getFullClassName());
                }

                for (PropertyObj property : clazz.getPropertiesNoBackLinksNoForwardLinks().values()) {
                    String tmp = getClassLink(property);
                    if (tmp != null) {
                        imports.add(tmp);
                    }
                }
            } else {
                if (clazz.isSubclass()) {
                    imports.add(SyncUtils.getConfigPackage(clazz.getExtendsFrom().getClazz().getTableObj().getDbCat(), PackageTypeEnum.OBJECT) + "." + clazz.getExtendsFrom().getClazz().getClassName());
                }

                imports.addAll(clazz.getImports());
                if (!State.getInstance().isSkipModelInterfaces()) {
                    imports.add(clazz.getInterfacePackageName());
                }
                context.put("customInterfaces", clazz.getCustomInterfaces());
            }

            context.put(IMPORTS, imports);
            context.put(CLASSCONST, clazz);
            context.put(TOPLEVEL, State.getInstance().topLevel);
            PackageTypeEnum type = isInterface ? PackageTypeEnum.OBJECTINTERFACE : PackageTypeEnum.OBJECT;
            context.put("packagename", SyncUtils.getConfigPackage(clazz.getTableObj().getDbCat(), type));

            context.put(THIS, new VelocityHelper(State.getInstance().defaultTestValues));

            context.put("classAnnotation", classAnnotations);
            context.put("classCustomCode", clazz.getClassCustomCode());
            List<String> interfacesToShow = new LinkedList<String>();
            if (!clazz.isSubclass()) {
                interfacesToShow.add("Cloneable");
                interfacesToShow.add("Serializable");
            }
//            if (!clazz.isEmbeddable() && !clazz.isSubclass()) {
//                interfacesToShow.add("IPojoGenEntity");
//            }

            if (!State.getInstance().isSkipModelInterfaces()) {
                interfacesToShow.add("I" + clazz.getClassName());
            }
            for (String s : clazz.getCustomInterfaces()) {
                interfacesToShow.add(s);
            }

            String tmpInterfaces = "";
            for (String s : interfacesToShow) {
                tmpInterfaces += s + ", ";
            }
            if (tmpInterfaces.endsWith(", ")) {
                tmpInterfaces = tmpInterfaces.substring(0, tmpInterfaces.length() - 2);
            }
            context.put("interfacesToShow", tmpInterfaces);
            context.put("enableJackson", State.getInstance().isEnableJacksonSupport());
            context.put("enableJacksonManagedReference", State.getInstance().isEnableJacksonManagedReferences());
            context.put("skipInterface", State.getInstance().isSkipModelInterfaces());
            context.put("classCustomCodeFields", StringUtils.isBlank(clazz.getClassCustomCodeFields()) ? "" : clazz.getClassCustomCodeFields());
            context.put("restrictCatalog", State.getInstance().dbMode == 1 || State.getInstance().schemaRestrict == 0);
            context.put("isSubtypeGenerationEnabled", !State.getInstance().disableSubtypeEnumGeneration);
            MessageDigest hash = MessageDigest.getInstance("MD5");


            context.put("serial", String.valueOf(bytesToLong(hash.digest(clazz.getClassName().getBytes()))) + "L");
            if (clazz.isEmbeddable() || clazz.hasCompositeKey()) {
                context.put("properties", clazz.getAllProperties());
            } else {
                context.put("properties", clazz.getPropertiesWithoutPFKNoBackLinksNoForwardLinks());
            }
            if (isInterface) {
                Config.interfaceTemplate.merge(context, classWriter);
            } else if (isOpenApiSchema) {
                Config.templateOpenApiSchema.merge(context, classWriter);
            } else {
                Config.template.merge(context, classWriter);
            }
            classWriter.close();
        } else {
            serialCount++;
        }
    }

    /**
     * Writes out the class representing the table for spring data
     *
     * @param projectName
     * @param clazz
     * @param classWriter
     * @param isInterface
     * @throws Exception
     * @throws ParseErrorException
     * @throws ResourceNotFoundException
     * @throws IOException
     */
    public static void writeOutRepoClass(String projectName, String targetFolder, Clazz clazz) throws ResourceNotFoundException, ParseErrorException, Exception {
        if (!Core.skipSchemaWrite(clazz)) {

            if (!Core.skipSchemaWrite(clazz) && (!clazz.isHiddenJoinTable())) {
                VelocityContext context = new VelocityContext();

                context.put(PROJECTNAME, projectName);
                context.put(CLASSCONST, clazz);
                context.put(TOPLEVEL, State.getInstance().topLevel);

                context.put("packagename", SyncUtils.getConfigPackage(clazz.getTableObj().getDbCat(), PackageTypeEnum.TABLE_REPO));
                context.put("impl", clazz.getFullClassName());
                context.put("id", clazz.getTypeOfId());
                String repo = State.getInstance().getSpringDataRepoInterface();
                context.put("springRepoShort", repo.substring(repo.lastIndexOf(".") + 1));
                context.put("springRepo", State.getInstance().getSpringDataRepoInterface());
                String tmp = getAndCreateRepoPath(targetFolder + "/" + State.getInstance().getSrcFolder() + "/", clazz);

                if (!new File(tmp).exists()) {
                    PrintWriter daoWriter = new PrintWriter(new BufferedWriter(new FileWriter(tmp, false)));
                    Template daoTemplate = Velocity.getTemplate("templates/classRepo.vm");
                    daoTemplate.merge(context, daoWriter);
                    daoWriter.close();
                }
            }
        }
    }

    /**
     * @param property
     * @return classlink name
     */
    private static String getClassLink(PropertyObj property) {
        String result = null;

        if (property.isOneToMany()) {
            result =
                    property.getOneToManyLink().getClazz().isEmbeddable() ? property.getOneToManyLink().getClazz().getEmbeddedFrom()
                            .getFullClassName() : property.getOneToManyLink().getClazz().getFullClassName();
        }
        if (property.isManyToMany()) {
            result = property.getManyToManyLink().getDstProperty().getClazz().getFullClassName();
        }
        if (property.isManyToOne()) {
            result = property.getManyToOneLink().getClazz().getFullClassName();
        }
        if (property.isOneToOne()) {
            result = property.getOneToOneLink().getClazz().getFullClassName();
        }
        if (property.isComposite()) {
            result = property.getCompositeLink().getFullClassName();
        }

        return result;
    }


    /**
     * Writes out the dao factory
     *
     * @param classes
     * @param targetFolder
     * @param catalogs
     * @throws IOException
     * @throws ResourceNotFoundException
     * @throws ParseErrorException
     * @throws MethodInvocationException
     * @throws Exception
     */
    public static void writeOutDaoFactoryClass(TreeMap<String, Clazz> classes, String targetFolder, TreeSet<String> catalogs)
            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException, Exception {

        if (State.getInstance().isEnableSpringData()) {
            return;
        }

        for (String catalog : catalogs) {
            if (Core.skipSchemaWrite(catalog)) {
                continue;
            }

            catalog = SyncUtils.removeUnderscores(catalog);
            TreeSet<String> imports = new TreeSet<String>(new CaseInsensitiveComparator());
            TreeMap<String, Clazz> tmpClasses = new TreeMap<String, Clazz>();
            for (Entry<String, Clazz> co : classes.entrySet()) {


                if (co.getValue().getClassPackage().equalsIgnoreCase(catalog)) {
                    imports.add(SyncUtils.getConfigPackage(co.getValue().getTableObj().getDbCat(), PackageTypeEnum.DAO) + ".*");
                    tmpClasses.put(co.getKey(), co.getValue());
                }


                Template daoFactoryTemplate = Velocity.getTemplate("templates/daoFactory.vm");
                VelocityContext context = new VelocityContext();
                String prettyCatalog = SyncUtils.upfirstChar(catalog);
                context.put(PROJECTNAME, State.getInstance().projectName);
                context.put(CLASSES, tmpClasses);
                context.put(TOPLEVEL, State.getInstance().topLevel);
                context.put(IMPORTS, imports);
                context.put(CATALOG, catalog);
                context.put(PRETTYCATALOG, prettyCatalog);
                context.put("packagename", SyncUtils.getConfigPackage(catalog, PackageTypeEnum.FACTORY));
                String tmp = getAndCreateFactoryPath(targetFolder + "/" + State.getInstance().getSrcFolder() + "/", catalog);

                PrintWriter daoFactoryWriter = new PrintWriter(new BufferedWriter(new FileWriter(tmp, false)));

                daoFactoryTemplate.merge(context, daoFactoryWriter);

                daoFactoryWriter.close();
            }
        }
    }


    /**
     * Write out all the classes
     *
     * @param targetFolder
     * @param classes
     * @throws IOException
     * @throws Exception
     */
    public static void writeClasses(final String targetFolder, TreeMap<String, Clazz> classes)
            throws IOException, Exception {


        for (Clazz co : classes.values()) {
            if (!co.isHiddenJoinTable() && !Core.skipSchemaWrite(co)) {
                // System.out.println(co.getClassName());
                String tmp = getAndCreateClassPath(targetFolder + "/" + State.getInstance().getSrcFolder() + "/", co, false, false);
                PrintWriter classWriter = new PrintWriter(new BufferedWriter(new FileWriter(tmp, false)));


                // Write Class
                VelocityWriters.writeClass(State.getInstance().projectName, co, classWriter, false, false);


                if (co.getEmbeddableClass() != null) {
                    tmp = getAndCreateClassPath(targetFolder + "/" + State.getInstance().getSrcFolder() + "/", co, true, false);
                    classWriter = new PrintWriter(new BufferedWriter(new FileWriter(tmp, false)));
                    VelocityWriters.writeClass(State.getInstance().projectName, co.getEmbeddableClass(), classWriter, false, false);
                }


                // Write out DAO class

                VelocityWriters.writeOutDaoClass(State.getInstance().projectName, targetFolder, co);
                if (State.getInstance().isEnableSpringData()) {
                    VelocityWriters.writeOutRepoClass(State.getInstance().projectName, targetFolder, co);
                }

            }
        }
    }


    /**
     * Write out all the classes
     *
     * @param targetFolder
     * @param classes
     * @throws IOException
     * @throws Exception
     */
    public static void writeInterfaceClasses(final String targetFolder, TreeMap<String, Clazz> classes)
            throws IOException, Exception {


        for (Clazz co : classes.values()) {
            if (!co.isHiddenJoinTable() && !Core.skipSchemaWrite(co)) {
                // System.out.println(co.getClassName());
                String tmp = getAndCreateClassPath(targetFolder + "/" + State.getInstance().getSrcFolder() + "/", co, false, true);
                PrintWriter classWriter = new PrintWriter(new BufferedWriter(new FileWriter(tmp, false)));


                // Write Class
                VelocityWriters.writeClass(State.getInstance().projectName, co, classWriter, true, false);

                if (co.getEmbeddableClass() != null) {
                    tmp = getAndCreateClassPath(targetFolder + "/" + State.getInstance().getSrcFolder() + "/", co, true, true);
                    classWriter = new PrintWriter(new BufferedWriter(new FileWriter(tmp, false)));
                    VelocityWriters.writeClass(State.getInstance().projectName, co.getEmbeddableClass(), classWriter, true, false);
                }


                // Write out DAO class
                VelocityWriters.writeOutDaoClass(State.getInstance().projectName, targetFolder, co);
            }
        }
    }

    public static void writeOpenApiSchemas(final String targetFolder, TreeMap<String, Clazz> classes)
            throws IOException, Exception {


        for (Clazz co : classes.values()) {
            if (!co.isHiddenJoinTable() && !Core.skipSchemaWrite(co)) {
                // System.out.println(co.getClassName());
                String tmp = getAndCreateOpenApiPath(targetFolder + "/" + State.getInstance().getSrcFolder() + "/", co);
                PrintWriter classWriter = new PrintWriter(new BufferedWriter(new FileWriter(tmp, false)));

                // Write Class
                VelocityWriters.writeClass(State.getInstance().projectName, co, classWriter, false, true);

            }
        }
    }
    /**
     * Writes out the unit test
     *
     * @param targetFolder
     * @param classes
     * @param commitOrder
     * @param srcFolder
     * @throws IOException
     * @throws ResourceNotFoundException
     * @throws ParseErrorException
     * @throws MethodInvocationException
     * @throws Exception
     */
    public static void writeOutDaoTestClass(String targetFolder, TreeMap<String, Clazz> classes, LinkedList<String> commitOrder, String srcFolder)
            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException, Exception {
        Template daoTestTemplate = Velocity.getTemplate("templates/daotest.vm");

        TreeMap<String, Clazz> tmpClasses = new TreeMap<String, Clazz>(new CaseInsensitiveComparator());
        LinkedList<VelocityTable> vtables = new LinkedList<VelocityTable>();

        LinkedList<VelocityTable> vtablesReverse = new LinkedList<VelocityTable>(); // for cleans
        // (table
        // dependency)
        TreeSet<String> imports = new TreeSet<String>();
        for (String commit : commitOrder) {
            if (Core.checkInIgnoreList(commit)) {
                continue;
            }
            Clazz clazz = State.getInstance().tables.get(commit).getClazz();
            if (Core.skipSchemaWrite(clazz)) {
                continue;
            }
            tmpClasses.put(clazz.getClassName(), clazz);
            // do not clean bootstrap classes - chrisp
            if (clazz.isSuperclass()) {
                if (clazz.isCyclicExclusionTable() || clazz.isCyclicExclusionReplacementTable()) {
                    // do not clean
                    State.getInstance().preventClean.add(clazz.getTableObj().getFullTableName());
                }
            }
            if (clazz.isSubclass()) {
                if (clazz.getExtendsFrom().getClazz().isCyclicExclusionTable() ||
                        clazz.getExtendsFrom().getClazz().isCyclicExclusionReplacementTable()) {
                    // do not clean
                    State.getInstance().preventClean.add(clazz.getTableObj().getFullTableName());
                }
            }

            // import factories even if the actual class should not be cleaned, just in case -
            // chrisp
            if (!clazz.isNameAmbiguityPossible()) {
                imports.add(SyncUtils.getConfigPackage(State.getInstance().tables.get(commit).getDbCat(), PackageTypeEnum.OBJECT) + ".*");
                // imports.add(SyncUtils.getConfigPackage(State.getInstance().tables.get(commit).getDbCat(),
                // PackageTypeEnum.DAO) + ".*");
            }
            imports.add(SyncUtils.getConfigPackage(State.getInstance().tables.get(commit).getDbCat(), PackageTypeEnum.FACTORY) + ".*");
            if (wroteAnEnum) {
                imports.add(SyncUtils.getConfigPackage(State.getInstance().tables.get(commit).getDbCat(), PackageTypeEnum.ENUM) + ".*");
            }

            if (!State.getInstance().isEnableSpringData()) {
                imports.add(clazz.getDataLayerImplFullClassName());
                imports.add(clazz.getDataLayerInterfaceFullClassName());

                imports.add("com.github.wwadge.hbnpojogen.persistence.IPojoGenEntity");
            } else {
//                imports.add(SyncUtils.getConfigPackage("", PackageTypeEnum.UTIL) + ".IPojoGenEntity");
                imports.add(SyncUtils.getConfigPackage(State.getInstance().tables.get(commit).getDbCat(), PackageTypeEnum.TABLE_REPO) + ".*");
            }
            // do not clean abstract classes - chrisp
            if (!State.getInstance().preventClean.contains(commit) && (!clazz.isHiddenJoinTable() && (!clazz.isAbstractClass()))) {
                VelocityTable vt = new VelocityTable();

                vt.setKey(SyncUtils.upfirstChar(commit));
                vt.setValue(State.getInstance().tables.get(commit));
                vtablesReverse.add(0, vt);
                vtables.add(vt);
            }


        }

        /**
         * Save this list because it might be useful to whoever uses the state in another program.
         */
        State.getInstance().setCleanDbTables(vtablesReverse);

        VelocityContext context = new VelocityContext();
        context.put("springData", State.getInstance().isEnableSpringData());

        context.put(PROJECTNAME, State.getInstance().projectName);
        context.put(CLASSES, tmpClasses);
        context.put(TABLES_REVERSE, vtablesReverse);
        context.put(PREPOPULATE, State.getInstance().prepopulateList);
        context.put(PRE_EXEC, State.getInstance().preExecList);
        context.put(THIS, new VelocityHelper(State.getInstance().defaultTestValues));
        context.put(TOPLEVEL, State.getInstance().topLevel + "." + State.getInstance().projectName);
        context.put(IMPORTS, imports);
        String tmpContext = State.getInstance().getApplicationContextFilename();
        if (tmpContext.lastIndexOf("/") > -1) {
            tmpContext = tmpContext.substring(tmpContext.lastIndexOf('/') + 1);
        }
        context.put("appContextFilename", tmpContext);
        context.put("daoCustomContextConfig", State.getInstance().getDaoCustomContextConfig());

        context.put(DISABLE_CLEAN_TABLES, State.getInstance().disableCleanTables);
        context.put(DISABLE_TEST_ROLLBACK, State.getInstance().disableTestRollback);
        TreeSet<String> seen = new TreeSet<String>();

        LinkedList<LinkedList<Clazz>> tableSetCycles = new LinkedList<LinkedList<Clazz>>();

        for (Entry<String, LinkedList<String>> entry : State.getInstance().commitResult.getCycleList().entrySet()) {
            TreeSet<String> tmp = new TreeSet<String>(new CaseInsensitiveComparator());

            // remove any ordering (make them consistent)
            tmp.add(entry.getKey());
            tmp.addAll(entry.getValue());

            if (!seen.contains(tmp.toString())) {
                seen.add(tmp.toString()); // one cycle combo only

                LinkedList<Clazz> tableSet = new LinkedList<Clazz>();
                for (String table : entry.getValue()) {
                    if (Core.checkInIgnoreList(table)) {
                        continue;
                    }
                    // do not clean tables in preventClean List
                    if (!State.getInstance().preventClean.contains(table)) {

                        // Do not try to resolve exclusion list cyclic dependencies - chrisp
                        TableObj cyclicTable = State.getInstance().tables.get(table);

                        boolean resolve = true;
                        if (cyclicTable.getClazz().isSuperclass()) {
                            if (cyclicTable.getClazz().isCyclicExclusionTable()) {
                                resolve = false;
                            }
                        }
                        if (cyclicTable.getClazz().isSubclass()) {
                            if (cyclicTable.getClazz().getExtendsFrom().getClazz().isCyclicExclusionTable()) {
                                resolve = false;
                            }
                        }
                        if (resolve) {
                            // not in exclusion list - try to resolve
                            tableSet.add(cyclicTable.getClazz());
                        }
                    }

                }
                // Add to Cycles only if tableSet is not empty
                if (!tableSet.isEmpty()) {
                    tableSetCycles.add(tableSet);
                }
            }
        }
        State.getInstance().setTableSetCycles(tableSetCycles);
        context.put(TABLE_SET_CYCLES, tableSetCycles);
        // FIXME
        String tmp = getAndCreateTestPath(targetFolder);
//				State.getInstance().getSourceTarget() + "/" + State.getInstance().getTestFolder() + "/" +
//						State.getInstance().getTopLevel().replaceAll("\\.", "/");
        new File(tmp).mkdirs();

        PrintWriter testWriter = new PrintWriter(new BufferedWriter(new FileWriter(tmp + "/DAOIntegrationTest.java", false)));
        daoTestTemplate.merge(context, testWriter);
        testWriter.close();
    }


    /**
     * Write out the data pool factory
     *
     * @param classes
     * @param targetFolder
     * @param catalogs
     * @throws IOException
     * @throws ResourceNotFoundException
     * @throws ParseErrorException
     * @throws MethodInvocationException
     * @throws Exception
     */
    public static void writeOutDataPoolFactoryClass(TreeMap<String, Clazz> classes, String targetFolder, TreeSet<String> catalogs)
            throws ResourceNotFoundException, ParseErrorException, MethodInvocationException, Exception {

        try {
            Template dataPoolFactoryTemplate = Velocity.getTemplate("templates/dataPoolFactory.vm");

            VelocityContext context = new VelocityContext();
            for (String catalog : catalogs) {
                if (Core.skipSchemaWrite(catalog)) {
                    continue;
                }
                catalog = SyncUtils.removeUnderscores(catalog);
                TreeSet<String> imports = new TreeSet<String>(new CaseInsensitiveComparator());
                //                if (Core.skipSchemaWrite(cat)){
                imports.add(SyncUtils.getConfigPackage(catalog, PackageTypeEnum.OBJECT) + ".*");
                //                }

                if (State.getInstance().isEnableSpringData()) {
                    imports.add(SyncUtils.getConfigPackage(catalog, PackageTypeEnum.TABLE_REPO) + ".*");
                    imports.add(SyncUtils.getConfigPackage("", PackageTypeEnum.UTIL) + ".BasicDataGenerator");

                } else {
                    imports.add("com.github.wwadge.hbnpojogen.randomlib.data.dataGeneration.BasicDataGenerator");

                }


                TreeMap<String, Clazz> tmpClasses = new TreeMap<String, Clazz>(new CaseInsensitiveComparator());
                boolean hasImmutable = false;
                for (Entry<String, Clazz> co : classes.entrySet()) {

                    if (co.getValue().isSubclass()) {
                        if (!Core.skipSchemaWrite(co.getValue().getExtendsFrom().getClazz().getTableObj().getDbCat())) {
                            imports.add(SyncUtils.getConfigPackage(co.getValue().getExtendsFrom().getClazz().getTableObj().getDbCat(), PackageTypeEnum.OBJECT) + ".*");
                        }
                    }
                    if (co.getValue().isImmutableAndNotStaticTest() && !co.getValue().isHiddenJoinTable() && !co.getValue().isAbstractClass()) {
                        hasImmutable = true;
                    }
                }

                for (Entry<String, Clazz> co : classes.entrySet()) {


                    if (hasImmutable) {
                        if (!State.getInstance().isEnableSpringData()) {
                            imports.add(co.getValue().getDataLayerInterfaceFullClassName());
                        } else {
                            imports.add("org.springframework.data.domain.PageRequest");
                        }
                    }
                    if (co.getValue().getClassPackage().equalsIgnoreCase(catalog)) {
                        tmpClasses.put(co.getKey(), co.getValue());

                        for (PropertyObj property : co.getValue().getAllPropertiesWithoutPFK().values()) {
                            if (!property.isNullable()) {
                                if (!property.isAutoInc()) {

                                    if (State.getInstance().isEnableSpringData()) {
                                        imports.add(SyncUtils.getConfigPackage(catalog, PackageTypeEnum.TABLE_REPO) + ".*");
                                        imports.add(SyncUtils.getConfigPackage("", PackageTypeEnum.UTIL) + ".BasicDataGenerator");

                                    } else {
                                        imports.add("com.github.wwadge.hbnpojogen.randomlib.data.dataGeneration.BasicDataGenerator");

                                    }

                                }
                                if (property.isManyToOne()) {
                                    imports.add(property.getManyToOneLink().getClazz().getFullDataPoolFactory());
                                }
                                if (property.getJavaType().equals("DateTime") || property.getJavaType().equals("OffsetDateTime")) {
                                    if (State.getInstance().isEnableJodaSupport()) {
                                        imports.add("org.joda.time.DateTime");
                                    } else {
                                        imports.add("java.time.OffsetDateTime");

                                    }
                                }
                                if (property.getJavaType().equals("LocalDate")) {
                                    if (State.getInstance().isEnableJodaSupport()) {

                                        imports.add("org.joda.time.LocalDate");
                                    } else {
                                        imports.add("java.time.LocalDate");

                                    }
                                }


                            }
                        }
                    }

                }

                imports.add("org.springframework.stereotype.Component");
                if (hasImmutable) {
                    imports.add("org.springframework.beans.factory.annotation.Autowired");
                }
                String prettyCatalog = SyncUtils.upfirstChar(catalog);
                context.put(PROJECTNAME, State.getInstance().projectName);
                context.put(CLASSES, tmpClasses);
                context.put(THIS, new VelocityHelper(State.getInstance().defaultTestValues));
                context.put(TOPLEVEL, State.getInstance().topLevel);
                context.put(IMPORTS, imports);
                context.put(CATALOG, catalog);
                context.put("hasImmutable", hasImmutable);
                context.put("springData", State.getInstance().isEnableSpringData());
                context.put(PRETTYCATALOG, prettyCatalog);
                context.put("tableDeps", State.getInstance().commitResult.getTableDeps().entrySet());
                String commitOrder = "";
                String classCommitOrder = "";

                for (VelocityTable vt : State.getInstance().getCleanDbTables()) {
                    commitOrder = commitOrder + "\"" + vt.getValue().getClazz().getClassName() + "\", ";
                    classCommitOrder = classCommitOrder + vt.getValue().getClazz().getClassName() + ".class, ";
                }
                if (commitOrder.length() > 2) {
                    commitOrder = commitOrder.substring(0, commitOrder.length() - 2);
                    classCommitOrder = classCommitOrder.substring(0, classCommitOrder.length() - 2);
                }
                context.put("commitOrder", commitOrder);
                context.put("classCommitOrder", classCommitOrder);

                context.put("packagename", SyncUtils.getConfigPackage(catalog, PackageTypeEnum.FACTORY));
                String tmp = getAndCreateDataPoolFactoryPath(targetFolder + "/" + State.getInstance().getSrcFolder() + "/", catalog);
                PrintWriter daoFactoryWriter = new PrintWriter(new BufferedWriter(new FileWriter(tmp, false)));

                dataPoolFactoryTemplate.merge(context, daoFactoryWriter);
                daoFactoryWriter.close();

                dataPoolFactoryTemplate = Velocity.getTemplate("templates/dataPoolFactoryInterface.vm");
                context.put("packagename", SyncUtils.getConfigPackage(catalog, PackageTypeEnum.FACTORY));
                tmp = getAndCreateDataPoolFactoryInterfacePath(targetFolder + "/" + State.getInstance().getSrcFolder() + "/", catalog);
                daoFactoryWriter = new PrintWriter(new BufferedWriter(new FileWriter(tmp, false)));

                dataPoolFactoryTemplate.merge(context, daoFactoryWriter);
                daoFactoryWriter.close();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Write out the spring configuration
     *
     * @param targetFolder
     * @param classes
     * @param dbCatalog
     * @throws IOException
     * @throws ResourceNotFoundException
     * @throws ParseErrorException
     * @throws MethodInvocationException
     * @throws Exception
     */
    public static void writeSpringApplicationContext(String targetFolder, TreeMap<String, Clazz> classes, String dbCatalog)
            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException, Exception {
        Template appContextTemplate = Velocity.getTemplate("templates/applicationContext.vm");

        TreeSet<DAOFactory> daoFactories = new TreeSet<DAOFactory>(new Comparator<DAOFactory>() {

            @Override
            public int compare(DAOFactory o1, DAOFactory o2) {
                return o1.classPath.compareTo(o2.classPath);
            }

        });

        for (Clazz clazz : classes.values()) {
            daoFactories.add(new DAOFactory(clazz.getClassPackage(), clazz.getFullHibernateDAOFactory()));
        }

        new File(State.getInstance().getSourceTarget() + File.separator + State.getInstance().getResourceFolder() + File.separator).mkdirs();

        String tmp = State.getInstance().getApplicationContextFilename();
        tmp =
                State.getInstance().getSourceTarget() + File.separator + State.getInstance().getResourceFolder() + File.separator +
                        State.getInstance().getApplicationContextFilename();

        if (new File(tmp).exists()) {
            return;
        }

        PrintWriter appContextWriter = new PrintWriter(new BufferedWriter(new FileWriter(tmp, false)));
        VelocityContext context = new VelocityContext();
        context.put(PROJECTNAME, State.getInstance().projectName);
        context.put(CLASSES, classes);
        context.put("lazyConnections", !State.getInstance().isDisableLazyConnections());
        context.put("embedPersistence", State.getInstance().isEnableSpringData());
        context.put(TOPLEVEL, State.getInstance().topLevel);
        context.put("driverClass", HbnPojoGen.driver);
        context.put("springData", State.getInstance().isEnableSpringData());
        context.put("suppressPlaceholder", State.getInstance().isPropertyPlaceholderConfigurerSuppressBean());

        context.put("factoryClass", State.getInstance().getSpringDataFactoryClass().equals("") ? "" : "factory-class=\"" + State.getInstance().getSpringDataFactoryClass() + "\"");
        Set<String> packages = new TreeSet<String>();
        Set<String> repoPackages = new TreeSet<String>();
        for (String schema : State.getInstance().getSchemas()) {

            if (!State.getInstance().ignoreEverythingExceptList.isEmpty() &&
                    !State.getInstance().ignoreEverythingExceptList.contains(schema)) {
                continue;
            }

            if (State.getInstance().noOutPutForSchemaList.contains(schema)) {
                continue;
            }

/*
            String shortest = SyncUtils.getConfigPackage(schema, PackageTypeEnum.DAO);
			String item = SyncUtils.getConfigPackage(schema, PackageTypeEnum.DAOIMPL);
			shortest = shortest.substring(0, StringUtils.indexOfDifference(shortest, item));

			item = SyncUtils.getConfigPackage(schema, PackageTypeEnum.DATA);
			shortest = shortest.substring(0, StringUtils.indexOfDifference(shortest, item));

			item = SyncUtils.getConfigPackage(schema, PackageTypeEnum.ENUM);
			shortest = shortest.substring(0, StringUtils.indexOfDifference(shortest, item));

			item = SyncUtils.getConfigPackage(schema, PackageTypeEnum.OBJECTINTERFACE);
			shortest = shortest.substring(0, StringUtils.indexOfDifference(shortest, item));

			item = SyncUtils.getConfigPackage(schema, PackageTypeEnum.OBJECT);
			shortest = shortest.substring(0, StringUtils.indexOfDifference(shortest, item));

*/
            if (!State.getInstance().isEnableSpringData()) {
                packages.add(StringUtils.removeEnd(SyncUtils.getConfigPackage(schema, PackageTypeEnum.DAO), "."));
                packages.add(StringUtils.removeEnd(SyncUtils.getConfigPackage(schema, PackageTypeEnum.DAOIMPL), "."));
                packages.add(StringUtils.removeEnd(SyncUtils.getConfigPackage(schema, PackageTypeEnum.DATA), "."));
            } else {
                packages.add(StringUtils.removeEnd(SyncUtils.getConfigPackage(schema, PackageTypeEnum.TABLE_REPO), "."));
                packages.add(StringUtils.removeEnd(SyncUtils.getConfigPackage(schema, PackageTypeEnum.TABLE_REPO_FACTORY), "."));

            }
            packages.add(StringUtils.removeEnd(SyncUtils.getConfigPackage(schema, PackageTypeEnum.ENUM), "."));
            packages.add(StringUtils.removeEnd(SyncUtils.getConfigPackage(schema, PackageTypeEnum.UTIL), "."));


            if (!State.getInstance().isSkipModelInterfaces()) {
                packages.add(StringUtils.removeEnd(SyncUtils.getConfigPackage(schema, PackageTypeEnum.OBJECTINTERFACE), "."));
            }

            packages.add(StringUtils.removeEnd(SyncUtils.getConfigPackage(schema, PackageTypeEnum.OBJECT), "."));

//			shortest = StringUtils.removeEnd(shortest, ".");
//			packages.add(shortest);
        }


        for (String schema : State.getInstance().getSchemas()) {
            repoPackages.add(SyncUtils.getConfigPackage(schema, PackageTypeEnum.TABLE_REPO));
        }
        context.put("packages", packages);
        context.put("repoPackages", repoPackages);
        context.put(DAO_FACTORIES, daoFactories);
        context.put("dbIP", State.getInstance().dbIP);
        context.put("dbCatalog", dbCatalog);
        context.put("springVersion", State.getInstance().getSpringVersion() == 2 ? "2.5" : "3.0");
        context.put("v2SpringVersion", State.getInstance().getSpringVersion() == 2);
        /*
        <property name="jdbcUrl" value="${db.connection.url}" />
        <property name="user" value="${db.connection.username}"/>
        <property name="password" value="${db.connection.password}"/>
        <property name="idleConnectionTestPeriod" value="${db.connection_pool.idle_connection_test_period}"/>
        <property name="maxIdleTime" value="${db.connection_pool.max_idle_time}"/>
        <property name="maxPoolSize" value="${db.connection_pool.max_pool_size}"/>
        <property name="minPoolSize" value="${db.connection_pool.min_pool_size}"/>
        <property name="initialPoolSize" value="${db.connection_pool.initial_pool_size}"/>
        <property name="maxStatements" value="${db.connection_pool.max_statements}"/>
        <property name="acquireIncrement" value="${db.connection_pool.acquire_increment}"/>
		 */
        boolean propOverride = !State.getInstance().isEnablePropertyPlaceholderConfigurer();
        String dialect;
        switch (State.getInstance().dbMode) {
            case 1:
                dialect = "org.hibernate.dialect.SQLServerDialect";
                break;
            case 2:
                dialect = "org.hibernate.dialect.PostgreSQL9Dialect";
                break;
            default:
                dialect = "org.hibernate.dialect.MySQLDialect";
                break;
        }

        if (State.getInstance().customDialect != null) {
            dialect = State.getInstance().customDialect;
        }
        context.put("dialect", State.getInstance().dbMode == 1 ? "org.hibernate.dialect.SQLServerDialect" : dialect);
        context.put("restrictCatalog", State.getInstance().dbMode == 1);

        String db;
        switch (State.getInstance().dbMode) {
            case 1:
                db = "jdbc:jtds:sqlserver";
                break;
            case 2:
                db = "jdbc:postgresql";
                break;
            default:
                db = "jdbc:mysql";
                break;
        }
        context.put("dbUrl", propOverride ? db + "://" + State.getInstance().getDbIP() + "/" + State.getInstance().getDbCatalog() : "${" + State.getInstance().getPropertyPlaceholderConfigurerPrefix() + "db.connection.url}");
        context.put("dbUsername", propOverride ? State.getInstance().dbUsername : "${" + State.getInstance().getPropertyPlaceholderConfigurerPrefix() + "db.connection.username}");
        context.put("dbPassword", propOverride ? State.getInstance().dbPassword : "${" + State.getInstance().getPropertyPlaceholderConfigurerPrefix() + "db.connection.password}");
        context.put("dbIdleConnectionTestPeriod", propOverride ? 60 : "${" + State.getInstance().getPropertyPlaceholderConfigurerPrefix() + "db.connection_pool.idle_connection_test_period}");
        context.put("dbMaxIdle", propOverride ? 240 : "${" + State.getInstance().getPropertyPlaceholderConfigurerPrefix() + "db.connection_pool.max_idle_time}");
        context.put("dbMaxPool", propOverride ? 20 : "${" + State.getInstance().getPropertyPlaceholderConfigurerPrefix() + "db.connections.max}");
        context.put("dbMinPool", propOverride ? 10 : "${" + State.getInstance().getPropertyPlaceholderConfigurerPrefix() + "db.connections.min}");
        context.put("dbPartitions", propOverride ? 1 : "${" + State.getInstance().getPropertyPlaceholderConfigurerPrefix() + "db.connection_pool.partition_count}");
        context.put("dbInitPool", propOverride ? 10 : "${" + State.getInstance().getPropertyPlaceholderConfigurerPrefix() + "db.connection_pool.initial_pool_size}");
        context.put("dbMaxStatements", propOverride ? 100 : "${" + State.getInstance().getPropertyPlaceholderConfigurerPrefix() + "db.connection_pool.max_statements}");
        context.put("dbAcquireIncrement", propOverride ? 3 : "${" + State.getInstance().getPropertyPlaceholderConfigurerPrefix() + "db.connection_pool.acquire_increment}");

        if (State.getInstance().getUseLDAP() || State.getInstance().isUseLDAPImport()) {
            context.put("dbUrl", "${jdbcUrl}");
            context.put("dbUsername", "${username}");
            context.put("dbPassword", "${password}");
            context.put("dbIdleConnectionTestPeriod", "${idleConnectionTestPeriod}");
            context.put("dbMaxIdle", "${idleMaxAge}");
            context.put("dbMaxPool", "${maxConnectionsPerPartition}");
            context.put("dbMinPool", "${minConnectionsPerPartition}");
            context.put("dbPartitions", "${partitionCount}");
            context.put("dbInitPool", "${dbInitPool}");
            context.put("dbMaxStatements", "${statementsCacheSize}");
            context.put("dbAcquireIncrement", "${acquireIncrement}");

        }
        context.put("pool", State.getInstance().getConnectionPool().toUpperCase());
        context.put("useLDAP", State.getInstance().getUseLDAP());
        context.put("useLDAPImport", State.getInstance().isUseLDAPImport());

        context.put("ldapServer", State.getInstance().getLdapServer());
        context.put("ldapBase", State.getInstance().getLdapBase());
        context.put("ldapCn", State.getInstance().getLdapCn());

        context.put("useDynamicLdapDataSource", State.getInstance().isUseDynamicLDAPDataSource());
        context.put("jndiRef", "jdbc/" + State.getInstance().getProjectName());
        context.put("sessionfactoryitems", State.getInstance().getSessionFactoryItems());
        context.put("transactionManagerItems", State.getInstance().getTransactionManagerItems());
        context.put("additionalContextItems", State.getInstance().getAdditionalContextItems());
        context.put("propOverride", !State.getInstance().isEnablePropertyPlaceholderConfigurer());

        appContextTemplate.merge(context, appContextWriter);
        appContextWriter.close();
    }

    public static void writeSpringOverrideFile(String targetFolder)
            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException, Exception {


        String override = targetFolder + File.separator + State.getInstance().getTestResourceFolder();
        new File(override).mkdirs();
        override = override + File.separator + State.getInstance().projectName + ".db.properties";
        FileWriter outFile = new FileWriter(override);
        PrintWriter out = new PrintWriter(outFile);
        String db;
        switch (State.getInstance().dbMode) {
            case 1:
                db = "jdbc:jtds:sqlserver";
                break;
            case 2:
                db = "jdbc:postgresql";
                break;
            default:
                db = "jdbc:mysql";
                break;
        }
        if (State.getInstance().isEnablePropertyPlaceholderConfigurer()) {
            out.println(State.getInstance().getPropertyPlaceholderConfigurerPrefix() + "db.connection.url=" + db + "://" + State.getInstance().getDbIP() + "/" + State.getInstance().getDbCatalog());
            out.println(State.getInstance().getPropertyPlaceholderConfigurerPrefix() + "db.connection.username=" + State.getInstance().dbUsername);
            out.println(State.getInstance().getPropertyPlaceholderConfigurerPrefix() + "db.connection.password=" + State.getInstance().dbPassword);
            out.println(State.getInstance().getPropertyPlaceholderConfigurerPrefix() + "db.connection_pool.idle_connection_test_period=60");
            out.println(State.getInstance().getPropertyPlaceholderConfigurerPrefix() + "db.connection_pool.max_idle_time=240");
            out.println(State.getInstance().getPropertyPlaceholderConfigurerPrefix() + "db.connections.max=30");
            out.println(State.getInstance().getPropertyPlaceholderConfigurerPrefix() + "db.connections.min=10");
            out.println(State.getInstance().getPropertyPlaceholderConfigurerPrefix() + "db.connection_pool.partition_count=3");
            out.println(State.getInstance().getPropertyPlaceholderConfigurerPrefix() + "db.connection_pool.initial_pool_size=10");
            out.println(State.getInstance().getPropertyPlaceholderConfigurerPrefix() + "db.connection_pool.max_statements=100");
            out.println(State.getInstance().getPropertyPlaceholderConfigurerPrefix() + "db.connection_pool.acquire_increment=3");
        } else {
            out.println("mainDataSource.logStatementsEnabled=true");

        }

        out.close();


    }

    /**
     * Write out the spring configuration
     *
     * @param targetFolder
     * @param classes
     * @param dbCatalog
     * @throws IOException
     * @throws ResourceNotFoundException
     * @throws ParseErrorException
     * @throws MethodInvocationException
     * @throws Exception
     */
    public static void writeAntBuildFile(String targetFolder, String dbCatalog)
            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException, Exception {
        Template appContextTemplate = Velocity.getTemplate("templates/hbnPojoGenBuild.vm");

        String tmp =
                State.getInstance().getSourceTarget() + File.separator + State.getInstance().getResourceFolder() + File.separator +
                        "hbnPojoGenBuild.xml";
        PrintWriter appContextWriter = new PrintWriter(new BufferedWriter(new FileWriter(tmp, false)));
        VelocityContext context = new VelocityContext();
        context.put(PROJECTNAME, State.getInstance().projectName);
        context.put(TOPLEVEL, State.getInstance().topLevel);
        context.put("lib", State.getInstance().getLibPath());
        context.put("target", "bin");
        context.put("src", State.getInstance().getSrcFolder());
        context.put("test", State.getInstance().getTestFolder());
        context.put("applicationContextFilename", State.getInstance().getApplicationContextFilename());

        appContextTemplate.merge(context, appContextWriter);
        appContextWriter.close();
    }

    /**
     * Write out the ehCache configuration
     *
     * @param targetFolder
     * @param classes
     * @param dbCatalog
     * @throws IOException
     * @throws ResourceNotFoundException
     * @throws ParseErrorException
     * @throws MethodInvocationException
     * @throws Exception
     */
    public static void writeEHCache(String targetFolder)
            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException, Exception {
        Template appContextTemplate = Velocity.getTemplate("templates/ehcache.vm");

        String tmp =
                State.getInstance().getSourceTarget() + File.separator + State.getInstance().getResourceFolder() + File.separator + "ehcache-model.xml";
        PrintWriter appContextWriter = new PrintWriter(new BufferedWriter(new FileWriter(tmp, false)));
        VelocityContext context = new VelocityContext();
        TreeSet<String> classes = new TreeSet<String>();
        for (Clazz clazz : State.getInstance().getClasses().values()) {
            classes.add(clazz.getFullClassName());
        }
        context.put("classes", classes);
        context.put(PROJECTNAME, State.getInstance().projectName);

        appContextTemplate.merge(context, appContextWriter);
        appContextWriter.close();
    }

    public static void writeUtils(String targetFolder, String basePath, String[] utilClasses, String packagename)
            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException, Exception {

        for (String s : utilClasses) {
            Template appContextTemplate = Velocity.getTemplate("templates/" + s + ".vm");

            String tmp = basePath + s + ".java";

            PrintWriter appContextWriter = new PrintWriter(new BufferedWriter(new FileWriter(tmp, false)));
            VelocityContext context = new VelocityContext();
            TreeSet<String> classes = new TreeSet<String>();
            for (Clazz clazz : State.getInstance().getClasses().values()) {
                classes.add(clazz.getFullClassName());
            }
            context.put("packagename", packagename);
            context.put("repositoryClass", SyncUtils.getConfigPackage("", PackageTypeEnum.TABLE_REPO_FACTORY) + ".CustomRepository");

            appContextTemplate.merge(context, appContextWriter);
            appContextWriter.close();
        }
    }


    public static void writeUtils(String targetFolder)
            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException, Exception {
        String[] utilClasses = {"StringValuedEnum", "StringValuedEnumReflect", "StringValuedEnumType", "IPojoGenEntity", "BasicDataGenerator"};

        if (State.getInstance().enableUtilsBeans) {
            writeUtils(targetFolder, getAndCreateUtilPath(targetFolder), utilClasses, SyncUtils.getConfigPackage("", PackageTypeEnum.UTIL));
        }
        if (State.getInstance().enableMockitoBeans) {
            writeUtils(targetFolder, getAndCreateUtilPath(targetFolder), new String[]{"MockDatabase"}, SyncUtils.getConfigPackage("", PackageTypeEnum.UTIL));
        }
        String[] repoClasses = {"CustomRepository", "CustomRepositoryImpl", "RepositoryFactoryBean"};

        if (State.getInstance().isEnableSpringDataFactoryWrite()) {
            writeUtils(targetFolder, getAndCreateRepoFactoryPath(targetFolder), repoClasses, SyncUtils.getConfigPackage("", PackageTypeEnum.TABLE_REPO_FACTORY));
        }

    }


    /**
     * @param targetFolder
     * @param classes
     * @param catalogs
     * @throws ResourceNotFoundException
     * @throws ParseErrorException
     * @throws Exception
     */
    public static void writeOutDataLayerHelpers(String targetFolder, TreeMap<String, Clazz> classes, TreeSet<String> schemas)
            throws ResourceNotFoundException, ParseErrorException, Exception {

        if (State.getInstance().isEnableSpringData()) {
            return;
        }


        Template hbnTemplate = Velocity.getTemplate("templates/datalayer.vm");


        for (String schema : schemas) {
            if (Core.skipSchemaWrite(schema)) {
                continue;
            }
            schema = SyncUtils.removeUnderscores(schema);
            TreeSet<String> imports = new TreeSet<String>(new CaseInsensitiveComparator());
            imports.add("java.io.Serializable");

            TreeMap<String, Clazz> tmpClasses = new TreeMap<String, Clazz>(new CaseInsensitiveComparator());
            for (Entry<String, Clazz> co : classes.entrySet()) {
                if (co.getValue().getClassPackage().equalsIgnoreCase(schema)) {
                    if (co.getValue().isEmbeddable()) {
                        imports.add(co.getValue().getFullClassName());
                    }
                    if ((!co.getValue().isEmbeddable()) && (!co.getValue().isHiddenJoinTable())) {
                        tmpClasses.put(co.getKey(), co.getValue());
                        imports.add(SyncUtils.getConfigPackage(schema, PackageTypeEnum.OBJECT) + "." + co.getValue().getClassName());
                    }
                    imports.add(SyncUtils.getConfigPackage(schema, PackageTypeEnum.FACTORY) + ".*");
                }
            }

            String prettyCatalog = SyncUtils.upfirstChar(schema);
            VelocityContext context = new VelocityContext();
            context.put(PROJECTNAME, State.getInstance().projectName);
            context.put(CLASSES, tmpClasses);
            context.put(THIS, new VelocityHelper(State.getInstance().defaultTestValues));
            context.put(TOPLEVEL, State.getInstance().topLevel);
            context.put(IMPORTS, imports);
            context.put(CATALOG, prettyCatalog);
            context.put("packagename", SyncUtils.getConfigPackage(schema, PackageTypeEnum.DATA));
            context.put("interface", false);
            context.put("beanname", "dataLayer" + SyncUtils.upfirstChar(schema) + "Impl");

            String tmp = getAndCreateDataLayerHelper(schema, targetFolder + "/" + State.getInstance().getSrcFolder() + "/", "Impl");
            PrintWriter hbnWriter = new PrintWriter(new BufferedWriter(new FileWriter(tmp, false)));

            hbnTemplate.merge(context, hbnWriter);
            hbnWriter.close();

            // Now write the interface
            // imports.clear();
            for (Entry<String, Clazz> co : classes.entrySet()) {
                if (co.getValue().getClassPackage().equalsIgnoreCase(schema)) {
                    imports.remove(SyncUtils.getConfigPackage(schema, PackageTypeEnum.FACTORY) + ".*");
                }
            }
            context.put("interface", true);
            tmp = getAndCreateDataLayerHelper(schema, targetFolder + "/" + State.getInstance().getSrcFolder() + "/", "");
            hbnWriter = new PrintWriter(new BufferedWriter(new FileWriter(tmp, false)));
            hbnTemplate.merge(context, hbnWriter);
            hbnWriter.close();


        }
    }


    /**
     * @param targetFolder
     * @param classes
     * @param catalogs
     * @throws ResourceNotFoundException
     * @throws ParseErrorException
     * @throws Exception
     */
    public static void writeOutDBVersionCheck(String targetFolder, TreeMap<String, Clazz> classes, TreeSet<String> schemas)
            throws ResourceNotFoundException, ParseErrorException, Exception {
        Template hbnTemplate = Velocity.getTemplate("templates/dbVersionCheck.vm");


        for (String schema : schemas) {
            if (Core.skipSchemaWrite(schema)) {
                continue;
            }
            VelocityContext context = new VelocityContext();
            new HashSet<String>();

            for (Entry<String, List<String>> tmp : State.getInstance().getVersionColumnsRead().entrySet()) {
                String cat = tmp.getKey().split("\\.")[0];
                if (State.getInstance().dbMode == 2) {
                    cat = tmp.getKey().split("\\.")[1];
                }
                if (cat.equalsIgnoreCase(schema)) {
                    schema = SyncUtils.removeUnderscores(schema);
                    if (State.getInstance().dbMode == 1 || State.getInstance().schemaRestrict == 0) {
                        context.put("tableName", tmp.getKey().split("\\.")[1]);
                    } else {
                        context.put("tableName", tmp.getKey());
                    }
                    context.put("versionColumns", tmp.getValue());
                    context.put("versionsRead", State.getInstance().getVersionsRead().get(tmp.getKey()));
                    context.put("gteVersions", State.getInstance().versionGTE.get(tmp.getKey()));
                    break;
                }
            }

            context.put(THIS, new VelocityHelper(State.getInstance().defaultTestValues));
            context.put("packagename", SyncUtils.getConfigPackage(schema, PackageTypeEnum.DATA));
            context.put("beanname", SyncUtils.removeUnderscores(SyncUtils.upfirstChar(schema)));
            String whereClause = State.getInstance().getVersionCheckWhereClause().get(schema);
            if (whereClause == null) {
                whereClause = State.getInstance().getVersionCheckWhereClause().get("*");
            }
            if (whereClause == null) {
                whereClause = "";
            }
            if (!whereClause.trim().equals("")) {
                whereClause = "WHERE " + whereClause;
            }
            context.put("whereClause", whereClause);

            String orderBy = State.getInstance().getVersionCheckOrderBy().get(schema);
            if (orderBy == null) {
                orderBy = State.getInstance().getVersionCheckOrderBy().get("*");
            }
            if (orderBy == null) {
                orderBy = "";
            }
            if (!orderBy.trim().equals("")) {
                orderBy = " ORDER BY " + orderBy;
            }
            context.put("orderBy", orderBy);

            String tmp = getAndCreateDBVersionCheckLayerHelper(schema, targetFolder + "/" + State.getInstance().getSrcFolder() + "/");
            PrintWriter hbnWriter = new PrintWriter(new BufferedWriter(new FileWriter(tmp, false)));

            hbnTemplate.merge(context, hbnWriter);
            hbnWriter.close();
        }
    }


    /**
     * Write out the enumerations
     *
     * @param targetFolder
     * @throws Exception
     * @throws ParseErrorException
     * @throws ResourceNotFoundException
     */
    public static void writeMavenPom(String targetFolder)
            throws ResourceNotFoundException, ParseErrorException, Exception {
        Template pomTemplate = Velocity.getTemplate("templates/pom.vm");
        VelocityContext context = new VelocityContext();
        context.put("pool", State.getInstance().getConnectionPool());
        context.put("groupId", State.getInstance().getMavenGroupId());
        context.put("distrib", State.getInstance().getMavenDistributionManagement().trim());
        context.put("additionalPom", State.getInstance().getMavenAdditionalPomEntries());
        context.put("mavenVersion", State.getInstance().getMavenVersion());
        context.put("artifactId", State.getInstance().getMavenArtifactId());
        context.put("embedPersistence", State.getInstance().isEnableSpringData());
        context.put("noDeps", State.getInstance().isMavenNoDeps());
        context.put("mavenName", State.getInstance().getMavenName());
        context.put("javaVersion", State.getInstance().getMavenJavaVersion());
        context.put("springData", State.getInstance().enableSpringData);
        context.put("validator", State.getInstance().enableHibernateValidator);
        context.put("v2SpringVersion", State.getInstance().getSpringVersion() == 2);
        context.put("useExternalLib", State.getInstance().isMavenUseExternalLib());
        context.put("useDynamicLdapDataSource", State.getInstance().isUseDynamicLDAPDataSource());
        context.put("hasJodaSupport", State.getInstance().isEnableJodaSupport());
        context.put("doPomVersion", !State.getInstance().isMavenArtifactVersionsDisabled());
        TreeMap<String, String> map = new TreeMap<String, String>();
        for (Entry<String, List<List<String>>> tmp : State.getInstance().getVersionsRead().entrySet()) {
            String cat = tmp.getKey().split("\\.")[0];
            map.put(cat, tmp.getValue().toString());
        }
        context.put("schemas", map.toString());
        context.put("dependencies", State.getInstance().getMavenDependency());
        context.put("srcDir", State.getInstance().getSrcFolder());
        context.put("testDir", State.getInstance().getTestFolder());
        context.put("syncVersion", State.getInstance().getSynchronizerVersion());
        context.put("dbMode", State.getInstance().dbMode);

        PrintWriter pomWriter = new PrintWriter(new BufferedWriter(new FileWriter(targetFolder + "/pom.xml", false)));

        pomTemplate.merge(context, pomWriter);
        pomWriter.close();
    }

    /**
     * Write out the enumerations
     *
     * @param targetFolder
     * @throws Exception
     * @throws ParseErrorException
     * @throws ResourceNotFoundException
     */
    public static void writeEnums(String targetFolder)
            throws ResourceNotFoundException, ParseErrorException, Exception {
        Template enumTemplate = Velocity.getTemplate("templates/enum.vm");
        Iterator<Entry<String, TableObj>> iter = State.getInstance().tables.entrySet().iterator();

        // for all tables..
        while (iter.hasNext()) {
            // and all fields...

            Entry<String, TableObj> entry = iter.next();
            if (Core.skipSchemaWrite(entry.getValue().getDbCat())) {
                continue;
            }
            String catalog = SyncUtils.removeUnderscores(entry.getValue().getDbCat());
            Iterator<Entry<String, FieldObj>> fieldIter = entry.getValue().getFields().entrySet().iterator();

            while (fieldIter.hasNext()) {

                Entry<String, FieldObj> field = fieldIter.next();

                if (field.getValue().getEnumName() != null) {
                    String name = field.getValue().getEnumName();

                    String tmp = getAndCreateEnumPath(targetFolder + "/" + State.getInstance().getSrcFolder() + "/", catalog, name);
                    PrintWriter enumWriter = new PrintWriter(new BufferedWriter(new FileWriter(tmp, false)));

                    VelocityContext context = new VelocityContext();
                    context.put(PROJECTNAME, State.getInstance().projectName);
                    context.put("enumName", name);
                    context.put("enumCount", field.getValue().getEnumValues().length);
                    context.put("enums", field.getValue().getEnumValues());
                    context.put("others", field.getValue().getEnumOtherCols());
                    context.put(TOPLEVEL, State.getInstance().topLevel);
                    context.put(CATALOG, catalog);

                    if (State.getInstance().isEnableSpringData()) {
                        context.put("stringValuedEnum", SyncUtils.getConfigPackage("", PackageTypeEnum.UTIL) + ".StringValuedEnum");
                    } else {
                        context.put("stringValuedEnum", "com.github.wwadge.hbnpojogen.persistence.impl.StringValuedEnum");
                    }
                    context.put("packagename", SyncUtils.getConfigPackage(catalog, PackageTypeEnum.ENUM));
                    context.put(THIS, new VelocityHelper(State.getInstance().defaultTestValues));
                    if (!Core.skipSchemaWrite(entry.getValue().getDbCat())) {
                        enumTemplate.merge(context, enumWriter);
                        wroteAnEnum = true;
                    }
                    enumWriter.close();
                }
            }
        }
    }

    public static void writeSubtypeEnums(String targetFolder)
            throws ResourceNotFoundException, ParseErrorException, Exception {
        Template enumTemplate = Velocity.getTemplate("templates/enumClass.vm");

        Iterator<Entry<String, TableObj>> iter = State.getInstance().tables.entrySet().iterator();

        // for all tables..
        while (iter.hasNext()) {
            // and all fields...

            Entry<String, TableObj> entry = iter.next();
            if (Core.skipSchemaWrite(entry.getValue().getDbCat())) {
                continue;
            }

            Clazz clazz = entry.getValue().getClazz();

            if (clazz == null || clazz.getSubclassEnum().isEmpty()) {
                continue;
            }

            String catalog = SyncUtils.removeUnderscores(entry.getValue().getDbCat());


            String name = clazz.getClassName() + "SubclassType";

            String tmp = getAndCreateSubtypeEnumPath(targetFolder + "/" + State.getInstance().getSrcFolder() + "/", catalog, name);
            PrintWriter enumWriter = new PrintWriter(new BufferedWriter(new FileWriter(tmp, false)));

            VelocityContext context = new VelocityContext();
            context.put(PROJECTNAME, State.getInstance().projectName);
            context.put("enumName", name);
            context.put("enumCount", clazz.getSubclassEnum().size());
            context.put("enums", clazz.getSubclassEnum());
            context.put(TOPLEVEL, State.getInstance().topLevel);
            context.put(CATALOG, catalog);
            context.put("packagename", SyncUtils.getConfigPackage(catalog, PackageTypeEnum.SUBTYPE_ENUM));

            if (!Core.skipSchemaWrite(entry.getValue().getDbCat())) {
                enumTemplate.merge(context, enumWriter);
            }
            enumWriter.close();
        }
    }

    /**
     * DAO Factory
     *
     * @author wallacew
     */
    public static final class DAOFactory {

        /**
         * For application context
         */
        private String beanName;
        /**
         * For application context
         */
        String classPath;


        /**
         * Helper constructor
         *
         * @param beanName
         * @param classPath
         */
        public DAOFactory(String beanName, String classPath) {
            this.beanName = beanName;
            this.classPath = classPath;
        }


        /**
         * Returns the bean name
         *
         * @return the beanName
         */
        public final String getBeanName() {
            return this.beanName;
        }


        /**
         * Sets the bean name
         *
         * @param beanName the beanName to set
         */
        public final void setBeanName(String beanName) {
            this.beanName = beanName;
        }


        /**
         * Returns the classpath
         *
         * @return the classPath
         */
        public final String getClassPath() {
            return this.classPath;
        }


        /**
         * Sets the classpath
         *
         * @param classPath the classPath to set
         */
        public final void setClassPath(String classPath) {
            this.classPath = classPath;
        }
    }

    /**
     * @param targetFolder
     * @param classes2
     * @param schemas
     * @throws Exception
     * @throws ParseErrorException
     * @throws ResourceNotFoundException
     */
    public static void writeOutMockitoBean(String targetFolder,
                                           TreeMap<String, Clazz> classes, TreeSet<String> schemas) throws ResourceNotFoundException, ParseErrorException, Exception {


        Template appContextTemplate = Velocity.getTemplate("templates/mocktest.vm");


        new File(State.getInstance().getSourceTarget() + File.separator + State.getInstance().getTestResourceFolder() + File.separator).mkdirs();

        String tmp;
        tmp =
                State.getInstance().getSourceTarget() + File.separator + State.getInstance().getTestResourceFolder() + File.separator +
                        State.getInstance().getMockitoFilename();

        PrintWriter appContextWriter = new PrintWriter(new BufferedWriter(new FileWriter(tmp, false)));
        VelocityContext context = new VelocityContext();

        List<Clazz> classesToExpose = new LinkedList<Clazz>();
        for (Clazz clazz : classes.values()) {
            if (!Core.skipSchemaWrite(clazz) && (!clazz.isHiddenJoinTable())) {
                classesToExpose.add(clazz);
            }
        }
        Collections.sort(classesToExpose, new Comparator<Clazz>() {
            /* (non-Javadoc)
             * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
             */
            @Override
            public int compare(Clazz o1, Clazz o2) {
                return o1.getClassName().compareTo(o2.getClassName());
            }

        });
        context.put(CLASSES, classesToExpose);
        context.put("mockdatabase", SyncUtils.getConfigPackage("", PackageTypeEnum.UTIL) + ".MockDatabase");
        appContextTemplate.merge(context, appContextWriter);
        appContextWriter.close();


    }


}
