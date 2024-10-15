package net.stardust.base.database;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManagerFactory;
import net.stardust.base.BasePlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.hibernate.cfg.Configuration;
import org.reflections.Reflections;

import java.util.Properties;

public final class JPA {

    private JPA() {
    }

    public static EntityManagerFactory entityManagerFactory(Properties properties) {
        return entityManagerFactory(properties, BasePlugin.getStardustEntitiesReflections());
    }

    public static EntityManagerFactory entityManagerFactory(FileConfiguration config) {
        return entityManagerFactory(config, BasePlugin.getStardustEntitiesReflections());
    }

    public static EntityManagerFactory inMemory() {
        return inMemory(BasePlugin.getStardustEntitiesReflections());
    }

    public static EntityManagerFactory entityManagerFactory(Properties properties, Reflections reflections) {
        Configuration configuration = new Configuration();
        configuration.setProperties(properties);

        reflections.getTypesAnnotatedWith(Entity.class).forEach(configuration::addAnnotatedClass);

        return configuration.buildSessionFactory();
    }

    public static EntityManagerFactory entityManagerFactory(FileConfiguration config, Reflections reflections) {
        ConfigurationSection con = config.getConfigurationSection("hibernate.connection");

        Properties props = new HibernateConfigurationBuilder()
                .url(con.getString("url"))
                .username(con.getString("username"))
                .password(con.getString("password"))
                .dialect(config.getString("hibernate.dialect"))
                .hbm2ddlAuto(config.getString("hibernate.hbm2ddl.auto"))
                .build();

        return entityManagerFactory(props, reflections);
    }

    public static EntityManagerFactory inMemory(Reflections reflections) {
        Properties props = new HibernateConfigurationBuilder()
                .url("jdbc:h2:mem:memorydb")
                .username("sa")
                .password("")
                .dialect("org.hibernate.dialect.H2Dialect")
                .hbm2ddlAuto("update")
                .build();

        return entityManagerFactory(props, reflections);
    }

}
