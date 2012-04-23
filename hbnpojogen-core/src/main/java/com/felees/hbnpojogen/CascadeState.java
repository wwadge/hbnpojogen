package com.felees.hbnpojogen;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


/**
 * 
 *
 * @author wallacew
 * @version $Revision: 2$
 */
public class CascadeState implements Serializable {
    
    /** */
    private static final long serialVersionUID = -8188145324423752745L;
    private boolean cascadeEnabled;
    private Set<String> cascadeType = new HashSet<String>();
    
    public CascadeState(boolean cascadeEnabled, Set<String> cascadeType) {
        this.cascadeEnabled = cascadeEnabled;
        this.cascadeType = cascadeType;
    }
 
    public CascadeState(boolean cascadeEnabled, String cascadeType) {
        this.cascadeEnabled = cascadeEnabled;
        this.cascadeType.add(cascadeType);
    }
    
    /**
     * Gets 
     *
     * @return 
     */
    public boolean isCascadeEnabled() {
        return this.cascadeEnabled;
    }
    
    /**
     * Sets 
     *
     * @param cascadeEnabled 
     */
    public void setCascadeEnabled(boolean cascadeEnabled) {
        this.cascadeEnabled = cascadeEnabled;
    }
    
    /**
     * Gets 
     *
     * @return 
     */
    public Set<String> getCascadeType() {
        return this.cascadeType;
    }
    
    /**
     * Sets 
     *
     * @param cascadeType 
     */
    public void setCascadeType(Set<String> cascadeType) {
        this.cascadeType = cascadeType;
    }
    
    @Override
    public String toString(){
        return "cascade="+cascadeEnabled+", cascade="+cascadeType.toString();
    }
}
