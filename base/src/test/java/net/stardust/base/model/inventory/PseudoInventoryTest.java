package net.stardust.base.model.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import net.stardust.base.utils.ObjectMapperFactory;
import net.stardust.base.utils.persistence.DataManager;

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
        diamondSword.getEnchantments().add(new PseudoEnchantment(DataManager.key("alou:boneca"), 1000));
        diamondSword.getTags().add(new PersistentObject(DataManager.key("meu:pau"), -1));
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
    @DisplayName("Inventory size at empty constructor must be equals to constant PseudoInventory.DEFAULT_INVENTORY_SIZE")
    public void sizeTest() {
        var inv = buildInventory();
        assertEquals(inv.getSize(), PseudoInventory.DEFAULT_INVENTORY_SIZE);
    }

    @Test
    @DisplayName("Should throw IndexOutOfBoundsException when putting item in slot greater or equals to inventory size")
    public void slotGreaterSizeTest() {
        var inv = buildInventory();
        assertThrows(IndexOutOfBoundsException.class, () -> inv.setItem(54, new PseudoItem()));
    }

    @Test
    @DisplayName("Should update inventory size to next multiple of 9 when setting items map with item at slot greater or equals to current size")
    public void sizeGrowTest() {
        var inv = buildInventory();
        Map<Integer, PseudoItem> items = new HashMap<>();
        items.put(54, new PseudoItem());
        inv.setItems(items);
        assertEquals(63, inv.getSize());
        items = new HashMap<>();
        items.put(100, new PseudoItem());
        inv.setItems(items);
        assertEquals(108, inv.getSize());
    }

}
