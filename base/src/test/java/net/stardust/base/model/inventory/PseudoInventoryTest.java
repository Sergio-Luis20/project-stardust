package net.stardust.base.model.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.bukkit.Material;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import net.stardust.base.Stardust;
import net.stardust.base.utils.ObjectMapperFactory;

public class PseudoInventoryTest {
    
    private ObjectMapper mapper;
    private ObjectWriter writer;

    public PseudoInventoryTest() {
        mapper = ObjectMapperFactory.getDefault();
        writer = mapper.writerWithDefaultPrettyPrinter();
    }

    private PseudoInventory buildInventory() {
        PseudoInventory inventory = new PseudoInventory(); // default inventory size 54
        PseudoItem diamonds = new PseudoItem(Material.DIAMOND, 64);
        PseudoItem diamondSword = new PseudoItem(Material.DIAMOND_SWORD);
        diamondSword.setDisplayName("Diamond Sword Prototype");
        diamondSword.getLabels().put("mortadela", "com salsicha");
        diamondSword.getLabels().put("e salsicha", "com scooby-doo");
        diamondSword.getEnchantments().add(new PseudoEnchantment(Stardust.key("alou:boneca"), 1000));
        diamondSword.getTags().add(new PersistentObject(Stardust.key("meu:pau"), -1));
        inventory.setItem(4, diamonds);
        inventory.setItem(0, diamondSword);
        return inventory;
    }

    @Test
    @DisplayName("Inventory before serialization must be equals to inventory post serialization")
    public void serializationTest() throws JsonProcessingException {
        var inv = buildInventory();
        String json = writer.writeValueAsString(inv);
        var readInv = mapper.readValue(json, PseudoInventory.class);
        assertEquals(inv, readInv);
    }

    @Test
    @DisplayName("Should throw IndexOutOfBoundsException when putting item in slot greater or equal to inventory size")
    public void slotGreaterSizeTest() {
        var inv = buildInventory();
        assertThrows(IndexOutOfBoundsException.class, () -> inv.setItem(54, new PseudoItem()));
    }

}
