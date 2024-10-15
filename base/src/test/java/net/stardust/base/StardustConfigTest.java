package net.stardust.base;

import org.bukkit.configuration.InvalidConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StardustConfigTest {

    private StardustConfig config;

    @BeforeEach
    void setup() throws IOException, InvalidConfigurationException {
        config = new StardustConfig(getClass().getResource("/vartest.env"));
        config.load(new InputStreamReader(getClass().getResourceAsStream("/TestConfig.yml")));
    }

    @Test
    @DisplayName("Should correctly show the environment variables")
    void test2() {
        assertEquals("spaced text", config.get("var2"));
        assertEquals("abc?", config.getStringList("var3").get(4));
        assertTrue(config.getBoolean("var4.var5.var6"));
        assertEquals(48, config.getInt("var4.var7"));
        var list = config.getMapList("var4.var8");
        assertEquals("no_space_text", ((Map<?, ?>) list.getFirst().get("var10")).get("var11"));
        assertEquals(-2.43, list.get(1).get("var12"));
        assertEquals("no_space_text/${VAR_14}", ((Map<?, ?>) list.get(1).get("var13")).get("var14"));
        assertEquals("${VAR_15}", config.get("var15"));
    }

}
