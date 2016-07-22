package com.github.wwadge.hbnpojogen;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * Used to populate any optional custom insert statements in the DB at some points in DAOTest
 *
 * @author wallacew
 */
public class CustomDB implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -1255063006196789454L;
    /**
     * The connection url
     */
    private String connectionURL;
    /**
     * The connection username
     */
    private String connectionUsername;
    /**
     * The connection password
     */
    private String connectionPassword;
    /**
     * The driver to use
     */
    private String driver;
    /**
     * The statements to execute
     */
    private LinkedList<String> statements;

    /**
     * Return the connection URL
     *
     * @return the connectionURL
     */
    public final String getConnectionURL() {
        return this.connectionURL;
    }

    /**
     * Set the connection URL
     *
     * @param connectionURL the connectionURL to set
     */
    public final void setConnectionURL(String connectionURL) {
        this.connectionURL = connectionURL;
    }

    /**
     * Return the connection username
     *
     * @return the connectionUsername
     */
    public final String getConnectionUsername() {
        return this.connectionUsername;
    }

    /**
     * Sets the connection username
     *
     * @param connectionUsername the connectionUsername to set
     */
    public final void setConnectionUsername(String connectionUsername) {
        this.connectionUsername = connectionUsername;
    }

    /**
     * Return the connection password
     *
     * @return the connectionPassword
     */
    public final String getConnectionPassword() {
        return this.connectionPassword;
    }

    /**
     * Sets the connection password
     *
     * @param connectionPassword the connectionPassword to set
     */
    public final void setConnectionPassword(String connectionPassword) {
        this.connectionPassword = connectionPassword;
    }

    /**
     * Return the statements to execute
     *
     * @return the statements
     */
    public final LinkedList<String> getStatements() {
        return this.statements;
    }

    /**
     * Sets the statements to execute
     *
     * @param statements the statements to set
     */
    public final void setStatements(LinkedList<String> statements) {
        this.statements = statements;
    }

    /**
     * Return the driver to use
     *
     * @return the driver
     */
    public final String getDriver() {
        return this.driver;
    }

    /**
     * Sets the driver to use
     *
     * @param driver the driver to set
     */
    public final void setDriver(String driver) {
        this.driver = driver;
    }
}
