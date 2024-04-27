package net.stardust.delay;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.SmithingTransformRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import net.stardust.base.utils.ItemUtils;

import java.util.Arrays;

public class DelayPlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        Bukkit.getOnlinePlayers().forEach(this::configurePlayer);
        configureAxesRecipes();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        configurePlayer(event.getPlayer());
    }

    private void configurePlayer(Player p) {
        AttributeInstance delay = p.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        delay.setBaseValue(24);
        p.saveData();
    }

    private void configureAxesRecipes() {
        Material[] axes = {Material.WOODEN_AXE, Material.GOLDEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.DIAMOND_AXE};
        for(Material axe : axes) {
            changeRecipe(axe.getKey(), false);
        }
        changeRecipe(NamespacedKey.minecraft("netherite_axe_smithing"), true);
    }

    private void changeRecipe(NamespacedKey key, boolean isSmithing) {
        Recipe oldRecipe = Bukkit.getRecipe(key);
        Recipe recipe;
        if(isSmithing) {
            SmithingTransformRecipe smithing = (SmithingTransformRecipe) oldRecipe;
            SmithingTransformRecipe newRecipe = new SmithingTransformRecipe(smithing.getKey(), ItemUtils.item(smithing.getResult()), 
                    smithing.getTemplate(), smithing.getBase(), smithing.getAddition(), smithing.willCopyNbt());
            recipe = newRecipe;
        } else {
            ShapedRecipe shaped = (ShapedRecipe) oldRecipe;
            ShapedRecipe newRecipe = new ShapedRecipe(shaped.getKey(), ItemUtils.item(oldRecipe.getResult()));
            newRecipe.setGroup(shaped.getGroup());
            newRecipe.setCategory(shaped.getCategory());
            newRecipe.shape(shaped.getShape());
            shaped.getChoiceMap().forEach(newRecipe::setIngredient);
            recipe = newRecipe;
        }
        getServer().removeRecipe(key);
        Bukkit.addRecipe(recipe);
    }
    
}
