package com.github.wwadge.hbnpojogen;


import com.github.wwadge.hbnpojogen.obj.Clazz;
import com.github.wwadge.hbnpojogen.obj.PropertyObj;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;


/**
 * Helper functions that can be called from velocity. We can do many of these directly in Velocity but the results become incredibly cluttered,
 * therefore we move some functionality here.
 *
 * @author wallacew
 */
public class VelocityHelper {

    /**
     * Holds the test value settings defined in the config file
     */
    private TreeMap<String, TreeMap<String, String>> defaultTestValues;


    /**
     * Constructor
     *
     * @param defaultTestValues
     */
    public VelocityHelper(TreeMap<String, TreeMap<String, String>> defaultTestValues) {
        this.defaultTestValues = defaultTestValues;
    }


    public static String removeUnderscores(String s) {
        return SyncUtils.removeUnderscores(s);
    }

    public static String upFirst(String s) {
        return SyncUtils.upfirstChar(s);
    }


    public static String maybeImplements(String s) {
        return s.trim().equals("") ? "" : "implements " + s;
    }


    /**
     * @param clazz
     * @return a valid enum path
     */
    public String getEnumPackagePath(Clazz clazz) {
        return SyncUtils.getConfigPackage(clazz.getTableObj().getDbCat(), PackageTypeEnum.ENUM);

    }


    public String convertCamelCaseToEnum(String s) {
        return SyncUtils.convertCamelCaseToEnum(s);
    }

    public String convertListToStringList(List<String> list) {
        String result = "";

        for (String entry : list) {
            result = result + "\"" + entry + "\", ";
        }
        return result.substring(0, result.length() - 2);
    }


    /**
     * Silly workaround. This will go away one day... (bug is if composite-key id is null)
     *
     * @param property
     * @return An init string
     */
    public String getHibernateBugWorkaround(PropertyObj property) {
        String returnValue;
        int fieldtype = property.getFieldObj().getFieldType();
        String fieldColumnType = property.getFieldObj().getFieldColumnType();

        switch (fieldtype) {
            case java.sql.Types.BOOLEAN:
            case java.sql.Types.BIT:
                returnValue = "new Boolean(true)";
                break;

            case java.sql.Types.TINYINT:
                returnValue = "new Byte((byte)0)";
                break;
            case java.sql.Types.SMALLINT:
                returnValue = "0";
                break;

            case java.sql.Types.INTEGER:
                if (property.getFieldObj().isFieldTypeUnsigned()) {
                    returnValue = "0L";
                } else {
                    returnValue = "0";
                }
                break;

            case java.sql.Types.OTHER:
                if (fieldColumnType.equalsIgnoreCase("UUID")) {
                    returnValue = java.util.UUID.randomUUID().toString();
                } else if (fieldColumnType.equalsIgnoreCase("UUID")) {
                    returnValue = "new String(\"dummy\");";
                } else {
                    returnValue = "new Object();";
                }
                break;
            case java.sql.Types.BIGINT:
                returnValue = "0L";
                break;
            // Removed from the lower group since mediumIn returns a decimal
            case java.sql.Types.DECIMAL:
            case java.sql.Types.DOUBLE:
            case java.sql.Types.FLOAT:
            case java.sql.Types.REAL:
                returnValue = "new Double(0.0)";
                break;

            case java.sql.Types.NUMERIC:
                returnValue = "new Float(0.0)";
                break;

            case java.sql.Types.CHAR:
                if (property.getJavaType().equals("String")) {
                    returnValue = "new String(\"hbnBugWorkaround\")";
                } else {
                    returnValue = "'A'";
                }
                break;

            case java.sql.Types.VARCHAR:
            case java.sql.Types.LONGVARCHAR:
            case java.sql.Types.NCHAR:
            case java.sql.Types.NVARCHAR:
            case java.sql.Types.LONGNVARCHAR:
                returnValue = "new String(\"hbnBugWorkaround\")";
                break;

            case java.sql.Types.DATE:
            case java.sql.Types.TIME:
            case java.sql.Types.TIMESTAMP:
                returnValue = "new Date()";
                break;

            case java.sql.Types.BINARY:
            case java.sql.Types.VARBINARY:
            case java.sql.Types.LONGVARBINARY:
            case java.sql.Types.ROWID:
            case java.sql.Types.NCLOB:
            case java.sql.Types.SQLXML:
            case java.sql.Types.NULL:
            case java.sql.Types.JAVA_OBJECT:
            case java.sql.Types.DISTINCT:
            case java.sql.Types.STRUCT:
            case java.sql.Types.ARRAY:
            case java.sql.Types.BLOB:
            case java.sql.Types.CLOB:
            case java.sql.Types.REF:
            case java.sql.Types.DATALINK:
                returnValue = "new Object()";
                break;

            default:
                returnValue = "new Object()";
                break;
        }

        return returnValue;
    }


    public String doExtends(Clazz clazz) {
        TreeSet<String> entries = new TreeSet<String>();

        if (clazz.isSubclass()) {
            entries.add(clazz.getExtendsFrom().getClazz().getClassName());
        }

        entries.addAll(clazz.getCustomExtends());


        String result = "";
        if (!entries.isEmpty()) {
            for (String s : entries) {
                result = result + ", " + s;
            }
            result = " extends " + result.substring(2);

        }
        return result;
    }

    /**
     * For dataPoolfactory
     *
     * @param clazz
     * @return A (partial) method signature
     */
    public String getDAOSignature(Clazz clazz) {
        // Return string
        String returnValue = "";

        // Return the class name if this is a many to one relationship
        for (PropertyObj property : clazz.getAllPropertiesWithoutPFK().values()) {
            if (property.isManyToOne()) {
                String tmp = maybeExpandClass(property);
                returnValue += ", " + tmp + property.getManyToOneLink().getClazz().getClassName() + " " + property.getPropertyName();
            }
            if (property.isComposite()) {
                returnValue += ", " + property.getJavaType() + " " + property.getPropertyName();
            }
            if (property.isOneToOne() && !property.isOneTooneInverseSide()) {
                String tmp = maybeExpandClass(property);
                returnValue += ", " + tmp + property.getOneToOneLink().getClazz().getClassName() + " " + property.getPropertyName();
            }
        }
        return returnValue.substring(2);
    }


    /**
     * Helper method
     *
     * @param property
     * @return a suitable string
     */
    public String getGeneratorString(PropertyObj property) {
        return getGeneratorString(property, false);
    }


    /**
     * Helper method
     *
     * @param property
     * @param withParams
     * @return a suitable string
     */
    public String getGeneratorString(PropertyObj property, boolean withParams) {
        return getGeneratorString(null, null, property, withParams);
    }


    /**
     * Helper method
     *
     * @param classes
     * @param property
     * @return A valid string
     */
    public String getGeneratorString(TreeMap<String, Clazz> classes, PropertyObj property) {
        return getGeneratorString(null, classes, property, false);
    }


    /**
     * Used in daotest
     *
     * @param clazz
     * @param classes
     * @param property
     * @return A valid string
     */
    public String getGeneratorString(Clazz clazz, TreeMap<String, Clazz> classes, PropertyObj property) {
        return getGeneratorString(clazz, classes, property, false);

    }

    /**
     * Order a set by "dot" depth
     *
     * @param workset
     * @return An ordered list
     */
    private LinkedList<ObjectPair<Clazz, String>> sortByDepth(LinkedList<ObjectPair<Clazz, String>> workset) {
        LinkedList<ObjectPair<Clazz, String>> result = new LinkedList<ObjectPair<Clazz, String>>();
        if (!workset.isEmpty()) {
            // order by nested level
            int dotCount = 0;
            for (ObjectPair<Clazz, String> entry : workset) {
                String tmp = entry.getValue();
                dotCount = Math.max(dotCount, tmp.split("\\.").length); // find maximum nesting
            }
            for (int i = dotCount; i >= 0; i--) {
                for (ObjectPair<Clazz, String> entry : workset) {
                    String tmp = entry.getValue();

                    if (i == tmp.split("\\.").length) {
                        result.add(entry);
                    }
                }

            }
        }
        return result;

    }

    /**
     * Helper routine
     *
     * @param clazz
     * @param testHandle
     * @return An ordered list
     */
    public static String replaceLast(String string, String toReplace, String replacement) {
        int pos = string.lastIndexOf(toReplace);
        if (pos > -1) {
            return string.substring(0, pos)
                    + replacement
                    + string.substring(pos + toReplace.length(), string.length());
        }
        return string;

    }

    String replaceFirstFrom(String str, int from, String regex, String replacement) {
        String prefix = str.substring(0, from);
        String rest = str.substring(from);
        rest = rest.replaceFirst(regex, replacement);
        return prefix + rest;
    }

    public LinkedList<String> getUncascaded(Clazz clazz, String testHandle) {
        LinkedList<String> result = new LinkedList<String>();
        LinkedList<ObjectPair<Clazz, String>> uncascadedOps = sortByDepth(getUncascadedInternal(clazz, testHandle, null, false));
        for (ObjectPair<Clazz, String> entry : uncascadedOps) {


            String s = entry.getValue().substring(0, entry.getValue().lastIndexOf(".") + 1) + "set";
            String method = entry.getValue().substring(entry.getValue().lastIndexOf(".") + 1);
            if (method.startsWith("get")) {
                s = s + method.substring(3);
            } else {
                s = s + method.substring(2); // isXXX
            }


            String set = replaceLast(s, "()", "(");

            if (State.getInstance().isEnableSpringData()) {
                result.add(set + "(" + entry.getKey().getRepositoryClassNamePropertyName() + ".save(" +
                        entry.getValue() + ")))");

            } else {
                result.add(entry.getKey().getFullHibernateDAOFactory() + ".get" + entry.getKey().getClassName() + "Dao().saveOrUpdate(" +
                        entry.getValue() + ")");
            }
        }

        clazz.setUncascadedOps(sortByDepth(getUncascadedInternal(clazz, testHandle, null, true)));
        return result;

    }


    /**
     * Traverses a class as deep as possible in order to return a list of all those items that need saving manually (i.e. cascading would not work)
     *
     * @param clazz
     * @param testHandle
     * @param seen
     * @return A list
     */
    @SuppressWarnings("all")
    private LinkedList<ObjectPair<Clazz, String>> getUncascadedInternal(Clazz clazz, String testHandle, TreeSet<String> seen, boolean full) {
        LinkedList<ObjectPair<Clazz, String>> result = new LinkedList<ObjectPair<Clazz, String>>();

        if (seen == null) {
            seen = new TreeSet<String>();
        }
        if (seen.contains(clazz.getFullClassName())) {
            return result;
        }
        seen.add(clazz.getFullClassName());

        TreeMap<String, PropertyObj> workingProps = full ? clazz.getAllPropertiesIncludingAllNonAbstractClass() : clazz.getAllPropertiesIncludingOfFirstNonAbstractClass();
        for (PropertyObj property : workingProps.values()) {

            if (property.isComposite() && !property.isNullable()) {
                result.addAll(getUncascadedCompositeInternal(property.getCompositeLink(), testHandle + ".getId()", seen, full));
                continue;
            }
            // a cyclicExclusionField should be considered as nullable - chrisp
            if ((property.isManyToOne() || property.isOneToOne()) && ((!property.isNullable()) && (!property.isCyclicDependencyProperty()))) {
                String tmp;
                if (testHandle.indexOf('.') > 0) {
                    tmp = "((" + property.getClazz().getFullClassName() + ")" + testHandle + ").get" + property.getJavaName() + "()";
                } else {
                    tmp = testHandle + ".get" + property.getJavaName() + "()";
                }
                // make recursive call inside this item
                if (property.isManyToOne()) {
                    result.addAll(getUncascadedInternal(property.getManyToOneLink().getClazz(), tmp, seen, full));
                    if (property.getManyToOneLink().isIdField() && (!property.isManyToOneCascadeEnabledByConfig() || !property.getManyToOneLink().isAutoInc())) {
                        result.add(new ObjectPair<Clazz, String>(property.getManyToOneLink().getClazz(), tmp));
                    }
                } else if (!property.isOneTooneInverseSide()) {
                    result.addAll(getUncascadedInternal(property.getOneToOneLink().getClazz(), tmp, seen, full));
                    if (property.getOneToOneLink().isIdField() && !property.isOneToOneCascadeEnabledByConfig()) {
                        result.add(new ObjectPair<Clazz, String>(property.getOneToOneLink().getClazz(), tmp));
                    }
                }
            } else {
                seen.clear();
            }

        }
        return result;
    }


    /**
     * Traverses a class as deep as possible in order to return a list of all those items that need saving manually (i.e. cascading would not work)
     *
     * @param clazz
     * @param testHandle
     * @param seen
     * @return A list
     */
    @SuppressWarnings("all")
    private LinkedList<ObjectPair<Clazz, String>> getUncascadedCompositeInternal(Clazz clazz, String testHandle, TreeSet<String> seen, boolean full) {
        LinkedList<ObjectPair<Clazz, String>> result = new LinkedList<ObjectPair<Clazz, String>>();

        if (seen == null) {
            seen = new TreeSet<String>();
        }
        if (seen.contains(clazz.getFullClassName())) {
            return result;
        }
        seen.add(clazz.getFullClassName());

        TreeMap<String, PropertyObj> workingProps = full ? clazz.getAllPropertiesIncludingAllNonAbstractClass() : clazz.getAllPropertiesIncludingOfFirstNonAbstractClass();

        for (PropertyObj property : workingProps.values()) {


            // a cyclicExclusionField should be considered as nullable - chrisp
            if (property.isManyToOne() && ((!property.isNullable()) && (!property.isCyclicDependencyProperty()))) {
                String tmp;
                if (testHandle.indexOf('.') > 0) {
                    tmp = "((" + property.getClazz().getFullClassName() + ")" + testHandle + ").get" + property.getJavaName() + "()";
                } else {
                    tmp = testHandle + ".get" + property.getJavaName() + "()";
                }
                // make recursive call inside this item
                result.addAll(getUncascadedInternal(property.getManyToOneLink().getClazz(), tmp, seen, full));
                if (property.getManyToOneLink().isIdField()) {
                    result.add(new ObjectPair<Clazz, String>(property.getManyToOneLink().getClazz(), tmp));

                    // result.add(new ObjectPair<Clazz, String>(property.getManyToOneLink().getClazz().getHibernateDAO() + ".get" +
                    // property.getManyToOneLink().getClazz().getClassName() + "Dao().saveOrUpdate(" + tmp + ")");
                }
            } else {
                seen.clear();
            }

        }
        return result;
    }


    /**
     * If cascading is disabled, we'll need a way to locate a parent class that is will eventually
     * reach this child table.
     *
     * @param clazz
     * @return testhandle
     */
    public String findReachable(Clazz clazz) {
        String result = clazz.getTableObj().getTestHandle();

        for (PropertyObj prop : clazz.getAllProperties().values()) {
            if (prop.isManyToOne() && !prop.isManyToOneCascadeEnabledByConfig()) {
                System.out.println(prop);
            }
        }
        return result;
    }

    /**
     * Helper
     *
     * @param property
     * @return "ALL" or "NONE"
     */
    public String getManyToOneCascadeEnabled(PropertyObj property) {
        return (property.isManyToOneCascadeEnabledByConfig()) ? "cascade = { CascadeType.PERSIST, CascadeType.MERGE }, " : "";
    }

    /**
     * Helper
     *
     * @param property
     * @return "ALL" or "NONE"
     */
    public String getOneToManyCascadeEnabled(PropertyObj property) {
        return (property.isOneToManyCascadeEnabledByConfig()) ? "cascade = { CascadeType.PERSIST, CascadeType.MERGE }, " : "";
    }

    public String getManyToOneSuperClass(Clazz inputClazz, PropertyObj property) {

        Clazz clazz = inputClazz.isEmbeddable() ? property.getManyToOneLink().getClazz() : inputClazz;
        if (property.getManyToOneLink().getClazz().getProperties(true).get(clazz.getClassPropertyName()) != null) {
            return clazz.getClassName();
        }
        if (clazz.getExtendsFrom() != null) {
            return getManyToOneSuperClass(clazz.getExtendsFrom().getClazz(), property);
        }

        return "???";
    }

    /**
     * Helper
     *
     * @param property
     * @return "ALL" or "NONE"
     */
    public String getOneToOneCascadeEnabled(PropertyObj property) {
        return (property.isOneToOneCascadeEnabledByConfig()) ? "cascade = { CascadeType.PERSIST, CascadeType.MERGE }, " : "";
    }

    /**
     * Helper
     *
     * @param property
     * @return "ALL" or "NONE"
     */
    public String getManyToManyCascadeEnabled(PropertyObj property) {
        return (property.isOneToManyCascadeEnabledByConfig()) ? "cascade = { CascadeType.PERSIST, CascadeType.MERGE }, " : "";
    }

    /**
     * Used in daotest
     *
     * @param clazz
     * @param classes
     * @param property
     * @param withParams for daoFactory
     * @return A valid string
     */
    public String getGeneratorString(Clazz clazz, TreeMap<String, Clazz> classes, PropertyObj property, boolean withParams) {
        // check if property is part of cyclic dependency exclusion list - chrisp
        if (clazz != null) {
            boolean cyclicClass = clazz.isCyclicExclusionTable();
            if (clazz.isSuperclass()) {
                if (clazz.isCyclicExclusionTable()) {
                    cyclicClass = true;
                }
            }
            if (clazz.isSubclass()) {
                if (clazz.isParentCyclicExclusionTable()) {
                    cyclicClass = true;
                }
            }


            if (cyclicClass) {
                if (property.isCyclicDependencyProperty()) {
                    return "getHbnPjoBootstrap" + property.getCyclicDependencyReplacementClazz().getClassName() + "()";
                }
            }

        }


        // Return string
        String returnValue = null;
        String unique = property.isUnique() ? "true" : "";
        try {
            String tableName = property.getClazz().getTableObj().getFullTableName();
            int fieldtype = property.getFieldObj().getFieldType();
            String fieldColumntype = property.getFieldObj().getFieldColumnType();
            String propertyName = property.getPropertyName();

            // if we have a default value for a particular field set in the config
            // file, return it
            TreeMap<String, String> defTest = this.defaultTestValues.get(tableName.toUpperCase());

            if (defTest != null) {
                returnValue = defTest.get(propertyName.toUpperCase());
            } else {

                defTest = this.defaultTestValues.get(tableName.substring(0, tableName.lastIndexOf(".")).toUpperCase() + ".*");
                if (defTest != null) {
                    returnValue = defTest.get(propertyName.toUpperCase());
                }
            }


            if (returnValue == null) {
                if (property.isComposite()) {
                    String result;
                    if (!withParams) {
                        result = "get" + property.getClazz().getClassNameNoSuffix() + "PK" + property.getClazz().getSuffix() + "()";
                    } else {
                        result = "id";
                    }
                    return result;
                }

                if (property.isOneToOne()) {
                    if (!withParams) {

                        String tmp = maybeExpandFactory(property, property.getOneToOneLink());
                        String result;
                        if (property.isNullable() || property.isCyclicReference()) {
                            result = "null";
                        }
                        // find a suitable subclass to instantiate -- chrisp
                        else if (property.getOneToOneLink().getClazz().isAbstractClass()) {

                            Clazz subClass = findNonAbstractSubclass(property.getOneToOneLink().getClazz(), classes);
                            if (subClass == null) {
                                result = "null";
                            } else {
                                result = tmp + ("get" + subClass.getClassName() + "()");
                            }

                        } else {
                            result = tmp + ("get" + property.getOneToOneLink().getClazz().getClassName() + "()");
                        }
                        return result;
                    }
                    return property.getLowerCaseFriendlyName();

                }
                // Return the class name if this is a many to one relationship
                if (property.isManyToOne()) {
                    if (!withParams) {
                        String tmp = maybeExpandFactory(property, property.getManyToOneLink());
                        String result;
                        if (property.isNullable() || property.isCyclicReference()) {
                            result = "null";
                        }
                        // find a suitable subclass to instantiate -- chrisp
                        else if (property.getManyToOneLink().getClazz().isAbstractClass()) {

                            Clazz subClass = findNonAbstractSubclass(property.getManyToOneLink().getClazz(), State.getInstance().classes);
                            if (subClass == null) {
                                result = "null";
                            } else {
                                result = tmp + ("get" + subClass.getClassName() + "()");
                            }

                        } else {
                            result = tmp + ("get" + property.getManyToOneLink().getClazz().getClassName() + "()");
                        }
                        return result;
                    }
                    return property.getLowerCaseFriendlyName();
                }
                if (property.isEnumType()) {
                    return "BasicDataGenerator.generateRandomEnumFromClass(" +
                            SyncUtils.getConfigPackage(property.getClazz().getTableObj().getDbCat(), PackageTypeEnum.ENUM) + "." +
                            property.getFieldObj().getEnumFilename() + ".class)";
                }
                if (property.isMoneyType()) {
                    return "org.javamoney.moneta.Money.of(BasicDataGenerator.generateRandomDecimal(" + property.getFieldObj().getPrecision() + ", " +
                            property.getFieldObj().getScale() + "), \"USD\")";
                }
                if (property.isCurrencyType()) {
                    return "javax.money.Monetary.getCurrency(\"USD\")";
                }

                switch (fieldtype) {
                    case java.sql.Types.BOOLEAN:
                    case java.sql.Types.BIT:
                        returnValue = "BasicDataGenerator.generateRandomBoolean()";
                        break;

                    case java.sql.Types.TINYINT:

                        returnValue = "(byte)(BasicDataGenerator.generateRandomTinyInt(" + unique + "))";
                        break;
                    case java.sql.Types.SMALLINT:
                        returnValue = "BasicDataGenerator.generateRandomInt(" + unique + ")";
                        break;

                    case java.sql.Types.INTEGER:
                        if (property.getFieldObj().isFieldTypeUnsigned()) {
                            returnValue = "BasicDataGenerator.generateRandomLong(" + unique + ")";
                        } else {
                            returnValue = "BasicDataGenerator.generateRandomInt(" + unique + ")";
                        }
                        break;

                    case java.sql.Types.BIGINT:
                        returnValue = "BasicDataGenerator.generateRandomLong(" + unique + ")";
                        break;
                    // Removed from the lower group since mediumIn returns a decimal
                    case java.sql.Types.DECIMAL:
                    case java.sql.Types.NUMERIC:
                        returnValue =
                                "BasicDataGenerator.generateRandomDecimal(" + property.getFieldObj().getPrecision() + ", " +
                                        property.getFieldObj().getScale() + ")";
                        break;

                    case java.sql.Types.DOUBLE:
                        returnValue = "BasicDataGenerator.generateRandomDouble(" + unique + ")";
                        break;
                    case java.sql.Types.FLOAT:
                        returnValue = "BasicDataGenerator.generateRandomDouble(" + unique + ")";
                        break;

                    case java.sql.Types.REAL:
                        returnValue = "BasicDataGenerator.generateRandomDouble(" + unique + ")";
                        break;

                    case java.sql.Types.CHAR:
                        if (!unique.equals("")) {
                            returnValue = "BasicDataGenerator.generateRandomStringChar(" + property.getLength() + ", true)";
                        } else {
                            returnValue = "BasicDataGenerator.generateRandomStringChar(" + property.getLength() + ")";
                        }
                        break;

                    case java.sql.Types.VARCHAR:
                    case java.sql.Types.LONGVARCHAR:
                    case java.sql.Types.NCHAR:
                    case java.sql.Types.NVARCHAR:
                    case java.sql.Types.LONGNVARCHAR:
                        if (!unique.equals("")) {
                            returnValue = "BasicDataGenerator.generateRandomString(" + property.getLength() + ", true)";
                        } else {
                            returnValue = "BasicDataGenerator.generateRandomString(" + property.getLength() + ")";
                        }
                        break;

                    case java.sql.Types.DATE:
                        if (State.getInstance().isEnableJodaSupport()) {
                            returnValue = "new LocalDate(BasicDataGenerator.generateDate())";
                        } else {
                            if (State.getInstance().isEnableJDK8Support()) {
                                returnValue = "BasicDataGenerator.generateDate().toLocalDateTime().toLocalDate()";
                            } else {
                                returnValue = "BasicDataGenerator.generateDate()";

                            }
                        }
                        break;
                    case java.sql.Types.TIME:
                    case java.sql.Types.TIMESTAMP:
                        if (State.getInstance().isEnableJodaSupport()) {
                            returnValue = "new DateTime(BasicDataGenerator.generateDate())";
                        } else {
                            if (State.getInstance().isEnableJDK8Support()) {

                                returnValue = "BasicDataGenerator.generateDate().toLocalDateTime().atOffset(java.time.ZoneOffset.UTC)";
                            } else {
                                returnValue = "BasicDataGenerator.generateDate()";

                            }
                        }
                        break;

                    case java.sql.Types.BINARY:
                    case java.sql.Types.VARBINARY:
                    case java.sql.Types.LONGVARBINARY:
                        if (!unique.equals("")) {
                            returnValue = "BasicDataGenerator.generateRandomBinary(" + property.getFieldObj().getLength() + ", true)";
                        } else {
                            returnValue = "BasicDataGenerator.generateRandomBinary(" + property.getFieldObj().getLength() + ")";
                        }
                        break;
                    case java.sql.Types.OTHER:
                        if (fieldColumntype.equalsIgnoreCase("UUID")) {
                            returnValue = "java.util.UUID.randomUUID()";
                        } else if (fieldColumntype.equalsIgnoreCase("JSON")) {
                            returnValue = "BasicDataGenerator.generateRandomString(" + property.getLength() + ")";
                        } else {
                            returnValue = "new Object()";
                        }

                        break;
                    case java.sql.Types.ROWID:
                    case java.sql.Types.NCLOB:
                    case java.sql.Types.SQLXML:
                    case java.sql.Types.NULL:
                    case java.sql.Types.JAVA_OBJECT:
                    case java.sql.Types.DISTINCT:
                    case java.sql.Types.STRUCT:
                    case java.sql.Types.ARRAY:
                    case java.sql.Types.BLOB:
                    case java.sql.Types.CLOB:
                    case java.sql.Types.REF:
                    case java.sql.Types.DATALINK:
                        returnValue = "new Object()";
                        break;

                    default:
                        returnValue = "new Object()";
                        break;
                }
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return returnValue;
    }


    /**
     * Expand the data factory in case of a name clash
     *
     * @param property
     * @param link
     * @return A valid string
     */
    private String maybeExpandFactory(PropertyObj property, PropertyObj link) {
        String result = "";
        String datapool = State.getInstance().getTestDataPools().get(link.getClazz().getTableObj().getDbCat());
        if (datapool != null) {
            return datapool + ".";
        }

        if (!link.getClazz().getClassPackage().equalsIgnoreCase(property.getClazz().getClassPackage())) {
            result = link.getClazz().getDataPoolFactory() + ".";
        }
        return result;
    }


    /**
     * Expand the class in case of a name clash
     *
     * @param property
     * @return a valid string
     */
    private String maybeExpandClass(PropertyObj property) {
        String result = "";
        Clazz link = null;
        if (property.isManyToOne()) {
            link = property.getManyToOneLink().getClazz();
        }
        if (property.isOneToOne()) {
            link = property.getOneToOneLink().getClazz();
        }
        if (!link.getClassPackage().equalsIgnoreCase(property.getClazz().getClassPackage())) {
            result = SyncUtils.getConfigPackage(link.getTableObj().getDbCat(), PackageTypeEnum.OBJECT) + ".";
        }

        return result;
    }


    /**
     * Return true if we're restricting the schema
     *
     * @return true if we're in restrict mode
     */
    public Boolean isSchemaRestricted() {
        return State.getInstance().schemaRestrict > 0;
    }

    public Boolean isDifferentCase(Clazz clazz) {
        return !clazz.getTableObj().getDbName().equals(clazz.getClassNameNoSuffix());
    }

    /**
     * Return true if the class in question contains underscores
     *
     * @param clazz
     * @return true if the class in question contains underscores
     */
    public Boolean hasUnderscores(Clazz clazz) {
        return (clazz.getTableObj().getDbName().indexOf('_') >= 0);
    }


    /**
     * Returns a non Abstract SubClass to the provided superClass by searching through the class list.
     *
     * @param superClass
     * @param classes
     * @return Clazz Non abstract subclass of superclass
     */
    public static Clazz findNonAbstractSubclass(Clazz superClass, TreeMap<String, Clazz> classes) {
        if (classes != null) {
            for (Entry<String, Clazz> co : classes.entrySet()) {
                if (co.getValue().isSubclass()) {
                    if (co.getValue().getExtendsFrom().getClazz().equals(superClass)) {
                        // found subclass
                        if (co.getValue().isAbstractClass()) {
                            return findNonAbstractSubclass(co.getValue(), classes);
                        }
                        return co.getValue();
                    }
                }
            }
        }
        return null;
    }

    public static String textType(Object obj) {
        if (obj instanceof String) {
            return "String";
        }
        if (obj instanceof Long) {
            return "Long";
        }
        if (obj instanceof Integer) {
            return "Integer";
        }
        return obj.getClass().getSimpleName();
    }

    /**
     * Returns a non Abstract SubClass to the provided superClass by searching through the class list.
     *
     * @param superClass
     * @param classes
     * @return Clazz Non abstract subclass of superclass
     */
    public static List<Clazz> findAllNonAbstractSubclass(Clazz superClass, TreeMap<String, Clazz> classes) {
        List<Clazz> result = new LinkedList<Clazz>();
        if (classes != null) {
            for (Entry<String, Clazz> co : classes.entrySet()) {
                if (co.getValue().isSubclass()) {
                    if (co.getValue().getExtendsFrom().getClazz().equals(superClass)) {
                        // found subclass
                        if (co.getValue().isAbstractClass()) {
                            result.addAll(findAllNonAbstractSubclass(co.getValue(), classes));
                        }
                        result.add(co.getValue());
                    }
                }
            }
        }
        return result;
    }

    /**
     * @return true if backlinks are disabled in the config
     */
    public static boolean isBacklinksDisabled() {
        return State.getInstance().disableBackLinksInDataPoolFactory;
    }

    public static boolean isPGSQL() {
        return State.getInstance().dbMode == 2;
    }


    public static String getClassName(String name) {
        try {
            return State.getInstance().getTables().get(name).getClazz().getClassName();
        } catch (Exception e) {
            return "";
        }
    }
}
