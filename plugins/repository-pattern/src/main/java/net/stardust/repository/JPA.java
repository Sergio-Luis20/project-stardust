package net.stardust.repository;

import java.util.Properties;

import org.bukkit.configuration.file.FileConfiguration;
import org.hibernate.cfg.Configuration;
import org.reflections.Reflections;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManagerFactory;

public final class JPA {
    
    private JPA() {
    }

    public static EntityManagerFactory entityManagerFactory(FileConfiguration config, Reflections reflections) {
        var con = config.getConfigurationSection("hibernate.connection");
        var props = new Properties();
        props.put("hibernate.connection.url", con.get("url"));
        props.put("hibernate.connection.username", con.get("username"));
        props.put("hibernate.connection.password", con.get("password"));
        props.put("hibernate.dialect", config.get("hibernate.dialect"));
        props.put("hibernate.hbm2ddl.auto", config.get("hibernate.hbm2ddl.auto"));

        var configuration = new Configuration();
        configuration.setProperties(props);

        reflections.getTypesAnnotatedWith(Entity.class).forEach(configuration::addAnnotatedClass);

        return configuration.buildSessionFactory();
    }

    public static EntityManagerFactory inMemory(Reflections reflections) {
        var props = new Properties();
        props.put("hibernate.connection.url", "jdbc:h2:mem:testdb");
        props.put("hibernate.connection.username", "sa");
        props.put("hibernate.connection.password", "");
        props.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        props.put("hibernate.hbm2ddl.auto", "update");

        var configuration = new Configuration();
        configuration.setProperties(props);

        reflections.getTypesAnnotatedWith(Entity.class).forEach(configuration::addAnnotatedClass);

        return configuration.buildSessionFactory();
    }

}
