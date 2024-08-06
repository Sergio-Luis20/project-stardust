package net.stardust.base.database;

import java.util.Properties;

import org.hibernate.cfg.Configuration;

/**
 * Utility builder class for making a {@link Properties} object in
 * the models of hibernate.
 * 
 * @implNote This class doesn't do any verification or validation during
 *           setter methods, so any value can be passed, even if it is not valid
 *           for
 *           Hibernate. The only validation done is during the {@link #build()}
 *           method,
 *           which checks if an attribute is null, and if true, do not adds it
 *           to the
 *           result {@link Properties} object.
 * 
 * @see JPA
 * @see Properties
 * @see Configuration
 * @see #build()
 * 
 * @author Sergio Luis
 */
public class HibernateConfigurationBuilder {

    private String url, username, password, dialect, hbm2ddlAuto;

    /**
     * Sets the connection url (hibernate.connection.url).
     * 
     * @param url the connection url
     * @return this builder
     */
    public HibernateConfigurationBuilder url(String url) {
        this.url = url;
        return this;
    }

    /**
     * Sets the connection username for accessing the database
     * (hibernate.connection.username).
     * 
     * @param username the connection username
     * @return this builder
     */
    public HibernateConfigurationBuilder username(String username) {
        this.username = username;
        return this;
    }

    /**
     * Sets the connection password for accessing the database
     * (hibernate.connection.password).
     * 
     * @param password the connection password
     * @return this builder
     */
    public HibernateConfigurationBuilder password(String password) {
        this.password = password;
        return this;
    }

    /**
     * Sets the hibernate database dialect (hibernate.dialect).
     * 
     * @param dialect the dialect
     * @return this builder
     */
    public HibernateConfigurationBuilder dialect(String dialect) {
        this.dialect = dialect;
        return this;
    }

    /**
     * Sets the hibernate configuration for database schema managing
     * (hibernate.hbm2ddl.auto). The possible values for this property are:
     * <b>
     * <ul>
     * <li>none
     * <li>validate
     * <li>update
     * <li>create
     * <li>create-drop
     * </ul>
     * </b>
     * 
     * @param hbm2ddlAuto the hbm2ddlAuto property
     * @return this builder
     */
    public HibernateConfigurationBuilder hbm2ddlAuto(String hbm2ddlAuto) {
        this.hbm2ddlAuto = hbm2ddlAuto;
        return this;
    }

    /**
     * Creates a new {@link Properties} object with the objects
     * obtained by setters. If an attribute was not defined (null),
     * it will not be added to the result {@link Properties}.
     * 
     * @return the built {@link Properties} object
     */
    public Properties build() {
        Properties props = new Properties();
        addIfNotNull(props, "hibernate.connection.url", url);
        addIfNotNull(props, "hibernate.connection.username", username);
        addIfNotNull(props, "hibernate.connection.password", password);
        addIfNotNull(props, "hibernate.dialect", dialect);
        addIfNotNull(props, "hibernate.hbm2ddl.auto", hbm2ddlAuto);
        return props;
    }

    private void addIfNotNull(Properties properties, String key, String value) {
        if (value != null) {
            properties.put(key, value);
        }
    }

}
