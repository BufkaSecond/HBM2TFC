package net.minecraft.item.crafting;

import com.gtnewhorizon.gtnhlib.util.map.ItemStackMap;
import com.mitchej123.hodgepodge.Common;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemFishFood.FishType;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

public class FurnaceRecipes {
    private static final FurnaceRecipes smeltingBase = new FurnaceRecipes();
    private Map smeltingList = new HashMap();
    private Map experienceList = new HashMap();
    private static final String __OBFID = "CL_00000085";

    public static FurnaceRecipes smelting() {
        return smeltingBase;
    }

    private FurnaceRecipes() {
        this.smeltingList = new ItemStackMap(false);
        this.experienceList = new ItemStackMap(false);
        this.func_151393_a(Blocks.iron_ore, new ItemStack(Items.iron_ingot), 0.7F);
        this.func_151393_a(Blocks.gold_ore, new ItemStack(Items.gold_ingot), 1.0F);
        this.func_151393_a(Blocks.diamond_ore, new ItemStack(Items.diamond), 1.0F);
        this.func_151393_a(Blocks.sand, new ItemStack(Blocks.glass), 0.1F);
        this.func_151396_a(Items.porkchop, new ItemStack(Items.cooked_porkchop), 0.35F);
        this.func_151396_a(Items.beef, new ItemStack(Items.cooked_beef), 0.35F);
        this.func_151396_a(Items.chicken, new ItemStack(Items.cooked_chicken), 0.35F);
        this.func_151393_a(Blocks.cobblestone, new ItemStack(Blocks.stone), 0.1F);
        this.func_151396_a(Items.clay_ball, new ItemStack(Items.brick), 0.3F);
        this.func_151393_a(Blocks.clay, new ItemStack(Blocks.hardened_clay), 0.35F);
        this.func_151393_a(Blocks.cactus, new ItemStack(Items.dye, 1, 2), 0.2F);
        this.func_151393_a(Blocks.log, new ItemStack(Items.coal, 1, 1), 0.15F);
        this.func_151393_a(Blocks.log2, new ItemStack(Items.coal, 1, 1), 0.15F);
        this.func_151393_a(Blocks.emerald_ore, new ItemStack(Items.emerald), 1.0F);
        this.func_151396_a(Items.potato, new ItemStack(Items.baked_potato), 0.35F);
        this.func_151393_a(Blocks.netherrack, new ItemStack(Items.netherbrick), 0.1F);
        FishType[] afishtype = FishType.values();
        int i = afishtype.length;

        for(int j = 0; j < i; ++j) {
            FishType fishtype = afishtype[j];
            if (fishtype.func_150973_i()) {
                this.func_151394_a(new ItemStack(Items.fish, 1, fishtype.func_150976_a()), new ItemStack(Items.cooked_fished, 1, fishtype.func_150976_a()), 0.35F);
            }
        }

        this.func_151393_a(Blocks.coal_ore, new ItemStack(Items.coal), 0.1F);
        this.func_151393_a(Blocks.redstone_ore, new ItemStack(Items.redstone), 0.7F);
        this.func_151393_a(Blocks.lapis_ore, new ItemStack(Items.dye, 1, 4), 0.2F);
        this.func_151393_a(Blocks.quartz_ore, new ItemStack(Items.quartz), 0.2F);
    }

    public void func_151393_a(Block p_151393_1_, ItemStack p_151393_2_, float p_151393_3_) {
        this.func_151396_a(Item.getItemFromBlock(p_151393_1_), p_151393_2_, p_151393_3_);
    }

    public void func_151396_a(Item p_151396_1_, ItemStack p_151396_2_, float p_151396_3_) {
        this.func_151394_a(new ItemStack(p_151396_1_, 1, 32767), p_151396_2_, p_151396_3_);
    }

    private boolean func_151397_a(ItemStack p_151397_1_, ItemStack p_151397_2_) {
        return p_151397_2_.getItem() == p_151397_1_.getItem() && (p_151397_2_.getItemDamage() == 32767 || p_151397_2_.getItemDamage() == p_151397_1_.getItemDamage());
    }

    public Map getSmeltingList() {
        return this.smeltingList;
    }

    @Overwrite
    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinFurnaceRecipes",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    public void func_151394_a(ItemStack input, ItemStack stack, float experience) {
        if (this.getSmeltingResult(input) != null) {
            Common.log.debug("Overwriting smelting recipe for input: {} and output {} with {}", new Object[]{input, this.getSmeltingResult(input), stack});
        }

        this.smeltingList.put(input, stack);
        this.experienceList.put(stack, experience);
    }

    @Overwrite
    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinFurnaceRecipes",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    public ItemStack getSmeltingResult(ItemStack stack) {
        return (ItemStack)this.smeltingList.get(stack);
    }

    @Overwrite
    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinFurnaceRecipes",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    public float func_151398_b(ItemStack stack) {
        if (stack != null && stack.getItem() != null) {
            float exp = stack.getItem().getSmeltingExperience(stack);
            if (exp == -1.0F) {
                exp = (Float)this.experienceList.getOrDefault(stack, 0.0F);
            }

            return exp;
        } else {
            return 0.0F;
        }
    }
}
