package com.github.wwadge.hbnpojogen;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * Representation of annotations as read from the config file
 *
 * @author wallacew
 */
public class CustomAnnotations implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -1540962841938250926L;
    /**
     * annotations to set on property
     */
    private LinkedList<String> propertyLevelAnnotations = new LinkedList<String>();
    /**
     * annotations to set on getter methods
     */
    private LinkedList<String> methodLevelAnnotationsOnGetters = new LinkedList<String>();
    /**
     * annotations to set on setter methods
     */
    private LinkedList<String> methodLevelAnnotationsOnSetters = new LinkedList<String>();
    /**
     * annotations to set on getter methods
     */
    private LinkedList<String> methodLevelGetterPrecondition = new LinkedList<String>();
    /**
     * annotations to set on setter methods
     */
    private LinkedList<String> methodLevelSetterPrecondition = new LinkedList<String>();
    /**
     * annotations to set on getter methods
     */
    private LinkedList<String> methodLevelGetterPostcondition = new LinkedList<String>();
    /**
     * annotations to set on setter methods
     */
    private LinkedList<String> methodLevelSetterPostcondition = new LinkedList<String>();

    /**
     * Return a list of property-set annotations
     *
     * @return the propertyLevelAnnotations
     */
    public final LinkedList<String> getPropertyLevelAnnotations() {
        return this.propertyLevelAnnotations;
    }

    /**
     * Sets list of property-set annotations
     *
     * @param propertyLevelAnnotations the propertyLevelAnnotations to set
     */
    public final void setPropertyLevelAnnotations(
            LinkedList<String> propertyLevelAnnotations) {
        this.propertyLevelAnnotations = propertyLevelAnnotations;
    }

    /**
     * Returns list of getter method annotations
     *
     * @return the methodLevelAnnotationsOnGetters
     */
    public final LinkedList<String> getMethodLevelAnnotationsOnGetters() {
        return this.methodLevelAnnotationsOnGetters;
    }

    /**
     * Sets list of getter method annotations
     *
     * @param methodLevelAnnotationsOnGetters the methodLevelAnnotationsOnGetters to set
     */
    public final void setMethodLevelAnnotationsOnGetters(
            LinkedList<String> methodLevelAnnotationsOnGetters) {
        this.methodLevelAnnotationsOnGetters = methodLevelAnnotationsOnGetters;
    }

    /**
     * Return list of setter method annotations
     *
     * @return the methodLevelAnnotationsOnSetters
     */
    public final LinkedList<String> getMethodLevelAnnotationsOnSetters() {
        return this.methodLevelAnnotationsOnSetters;
    }

    /**
     * Sets list of setter method annotations
     *
     * @param methodLevelAnnotationsOnSetters the methodLevelAnnotationsOnSetters to set
     */
    public final void setMethodLevelAnnotationsOnSetters(
            LinkedList<String> methodLevelAnnotationsOnSetters) {
        this.methodLevelAnnotationsOnSetters = methodLevelAnnotationsOnSetters;
    }

    /**
     * @return the methodLevelGetterPrecondition
     */
    public final LinkedList<String> getMethodLevelGetterPrecondition() {
        return methodLevelGetterPrecondition;
    }

    /**
     * @param methodLevelGetterPrecondition the methodLevelGetterPrecondition to set
     */
    public final void setMethodLevelGetterPrecondition(
            LinkedList<String> methodLevelGetterPrecondition) {
        this.methodLevelGetterPrecondition = methodLevelGetterPrecondition;
    }

    /**
     * @return the methodLevelSetterPrecondition
     */
    public final LinkedList<String> getMethodLevelSetterPrecondition() {
        return methodLevelSetterPrecondition;
    }

    /**
     * @param methodLevelSetterPrecondition the methodLevelSetterPrecondition to set
     */
    public final void setMethodLevelSetterPrecondition(
            LinkedList<String> methodLevelSetterPrecondition) {
        this.methodLevelSetterPrecondition = methodLevelSetterPrecondition;
    }

    /**
     * @return the methodLevelGetterPostcondition
     */
    public final LinkedList<String> getMethodLevelGetterPostcondition() {
        return methodLevelGetterPostcondition;
    }

    /**
     * @param methodLevelGetterPostcondition the methodLevelGetterPostcondition to set
     */
    public final void setMethodLevelGetterPostcondition(
            LinkedList<String> methodLevelGetterPostcondition) {
        this.methodLevelGetterPostcondition = methodLevelGetterPostcondition;
    }

    /**
     * @return the methodLevelSetterPostcondition
     */
    public final LinkedList<String> getMethodLevelSetterPostcondition() {
        return methodLevelSetterPostcondition;
    }

    /**
     * @param methodLevelSetterPostcondition the methodLevelSetterPostcondition to set
     */
    public final void setMethodLevelSetterPostcondition(
            LinkedList<String> methodLevelSetterPostcondition) {
        this.methodLevelSetterPostcondition = methodLevelSetterPostcondition;
    }
}
