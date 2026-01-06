package com.myname.mymodid.mixins.late;

import com.dunk.tfc.Core.Player.BodyTempStats;
import com.dunk.tfc.Core.TFC_Core;
import com.dunk.tfc.Handlers.EntityArmorHandler;

import com.dunk.tfc.WorldGen.WorldCacheManager;
import cpw.mods.fml.common.FMLLog;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;


import java.util.*;

import com.dunk.tfc.Reference;
import com.dunk.tfc.TerraFirmaCraft;
import com.dunk.tfc.Core.TFC_Achievements;
import com.dunk.tfc.Core.TFC_Climate;
import com.dunk.tfc.Core.TFC_Core;
import com.dunk.tfc.Core.TFC_Sounds;
import com.dunk.tfc.Core.TFC_Time;
import com.dunk.tfc.Core.Player.BodyTempStats;
import com.dunk.tfc.Core.Player.FoodStatsTFC;
import com.dunk.tfc.Core.Player.InventoryPlayerTFC;
import com.dunk.tfc.Core.Player.PlayerInfo;
import com.dunk.tfc.Core.Player.PlayerManagerTFC;
import com.dunk.tfc.Core.Player.SkillStats;
import com.dunk.tfc.Entities.EntityProjectileTFC;
import com.dunk.tfc.Entities.Mobs.EntityHorseTFC;
import com.dunk.tfc.Food.ItemFoodTFC;
import com.dunk.tfc.Food.ItemMeal;
import com.dunk.tfc.Handlers.Network.AbstractPacket;
import com.dunk.tfc.Handlers.Network.ExtraItemsPacket;
import com.dunk.tfc.Handlers.Network.PlayerUpdatePacket;
import com.dunk.tfc.Items.ItemArrow;
import com.dunk.tfc.Items.ItemBloom;
import com.dunk.tfc.Items.ItemBoots;
import com.dunk.tfc.Items.ItemClothing;
import com.dunk.tfc.Items.ItemOreSmall;
import com.dunk.tfc.Items.ItemQuiver;
import com.dunk.tfc.Items.ItemSocks;
import com.dunk.tfc.Items.ItemTFCArmor;
import com.dunk.tfc.Items.ItemBlocks.ItemAnvil;
import com.dunk.tfc.Items.ItemBlocks.ItemBarrels;
import com.dunk.tfc.Items.Tools.ItemCustomBow;
import com.dunk.tfc.Items.Tools.ItemJavelin;
import com.dunk.tfc.Items.Tools.ItemSling;
import com.dunk.tfc.api.Armor;
import com.dunk.tfc.api.Food;
import com.dunk.tfc.api.TFCAttributes;
import com.dunk.tfc.api.TFCBlocks;
import com.dunk.tfc.api.TFCItems;
import com.dunk.tfc.api.TFCOptions;
import com.dunk.tfc.api.Constant.Global;
import com.dunk.tfc.api.Entities.IAnimal;
import com.dunk.tfc.api.Enums.EnumArmorBodyPart;
import com.dunk.tfc.api.Interfaces.IArmor;
import com.dunk.tfc.api.Interfaces.IAttackSpeed;
import com.dunk.tfc.api.Interfaces.IBoots;
import com.dunk.tfc.api.Interfaces.IEquipable;
import com.dunk.tfc.api.Interfaces.IEquipable.ClothingType;
import com.dunk.tfc.api.Interfaces.IEquipable.EquipType;
import com.dunk.tfc.api.Interfaces.IFood;
import com.dunk.tfc.api.Util.Helper;

import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fluids.FluidStack;
import com.hbm.items.armor.ArmorFSB;


import com.dunk.tfc.Core.TFC_Core;

import static com.dunk.tfc.Core.TFC_Core.setBodyTempStats;

@Mixin(value= TFC_Core.class, remap=false)
public class ArmorCheckMixin {
    //@Shadow
    //BodyTempStats bodyTemp;



    //@Shadow
    //LivingEvent.LivingUpdateEvent event;

    /*
    @ModifyVariable(
        method = "onEntityLivingUpdate()V",
        at = @At(value = "RETURN"),
        index = 1
    )
    private double mixin(int bodyTemp) {
        bodytemp.coldResistance = 100;
        return 100;
    }

     */

    @Overwrite
    public static int[] getTemperatureResistanceFromClothes(EntityPlayer player, World world, ItemStack[] armor, ItemStack[] clothes)
    {
        int[] result = new int[] { 0, 0 };
        int numWool = 0;
        int numStraw = 0;
        float temp = TFC_Climate.getHeightAdjustedTemp(world, (int) player.posX, (int) player.posY, (int) player.posZ);

        // boolean damp = TFC_Core.isClothingDamp(null, player);
        for (ItemStack i : armor)
        {
            if (i != null && i.getItem() instanceof ItemClothing)
            {
                ItemClothing clothing = (ItemClothing) (i.getItem());
                int heat = clothing.getHeatResistance(i);
                int cool = clothing.getColdResistance(i);

                if (clothing.hasTempBoundary(i))
                {
                    if (clothing.getMinTemp(i) > temp)
                    {
                        cool = Math.min(0, cool);
                    }
                }
                // We no longer have clothing soaked
                IEquipable.ClothingType c = clothing.getClothingType();
                int wetness = i.stackTagCompound != null && i.stackTagCompound.hasKey("wetness") ? i.stackTagCompound.getInteger("wetness") : 0;
                boolean damp = wetness > 0 && wetness < 2000;
                boolean soaked = wetness >= 2000 ? ((c == ClothingType.BOOTS || c == ClothingType.SOCKS || c == ClothingType.FULLBOOTS || c == ClothingType.PANTS
                    || c == ClothingType.SKIRT || c == ClothingType.SANDALS || c == ClothingType.THINPANTS) ? true
                    : (c == ClothingType.SHIRT || c == ClothingType.COAT || c == ClothingType.HEAVYCOAT || c == ClothingType.THINCOAT || c == ClothingType.CLOTH_HAT
                    || c == ClothingType.STRAW_HAT || c == ClothingType.THINSHIRT) ? true : false) : false;

                if (soaked)
                {
                    heat = 0;
                    cool = 0;
                }
                else if (damp)
                {
                    if (heat > 0)
                    {
                        heat--;
                    }
                    else if (heat < 0)
                    {
                        heat++;
                    }
                    if (cool > 0)
                    {
                        cool--;
                    }
                    else if (cool < 0)
                    {
                        cool++;
                    }
                }
                else if (clothing.isWool())
                {
                    numWool++;
                }
                else if (clothing.isStraw() || clothing == TFCItems.grassSandals || clothing == TFCItems.leatherSandals)
                {
                    numStraw++;
                }
                result[0] += heat;
                result[1] += cool;
            }

        }
        if (clothes != null)
        {
            int counter = 0;
            for (ItemStack i : clothes)
            {
                if (i != null && i.getItem() instanceof ItemClothing)
                {
                    ItemClothing clothing = (ItemClothing) (i.getItem());
                    // damp = TFC_Core.isClothingDamp(i, player);
                    int heat = ((ItemClothing) (i.getItem())).getHeatResistance(i);
                    int cool = ((ItemClothing) (i.getItem())).getColdResistance(i);

                    //Check if the clothing item is exposed
                    boolean exposed = counter <= 4 && counter > 0 ? clothes[counter + 4] == null : true;

                    counter++;
                    if (clothing.hasTempBoundary(i))
                    {
                        if (clothing.getMinTemp(i) > temp)
                        {
                            cool = Math.min(0, cool);
                        }
                    }

                    // We no longer have clothing soaked
                    int wetness = i.stackTagCompound != null && i.stackTagCompound.hasKey("wetness") ? i.stackTagCompound.getInteger("wetness") : 0;
                    boolean damp = wetness > 0 && wetness < 2000;
                    IEquipable.ClothingType c = ((IEquipable) (i.getItem())).getClothingType();
                    boolean soaked = wetness >= 2000 ? ((c == ClothingType.BOOTS || c == ClothingType.SOCKS || c == ClothingType.FULLBOOTS || c == ClothingType.PANTS
                        || c == ClothingType.SKIRT || c == ClothingType.SANDALS || c == ClothingType.THINPANTS) ? true
                        : (c == ClothingType.SHIRT || c == ClothingType.COAT || c == ClothingType.HEAVYCOAT || c == ClothingType.THINCOAT || c == ClothingType.CLOTH_HAT
                        || c == ClothingType.STRAW_HAT || c == ClothingType.THINSHIRT) ? true : false) : false;

                    if (soaked)
                    {
                        heat = 0;
                        cool = 0;
                    }
                    else if (damp)
                    {
                        if (heat > 0)
                        {
                            heat--;
                        }
                        else if (heat < 0)
                        {
                            heat++;
                        }
                        if (cool > 0)
                        {
                            cool--;
                        }
                        else if (cool < 0)
                        {
                            cool++;
                        }
                    }
                    else if (((ItemClothing) i.getItem()).isWool())
                    {
                        numWool++;
                    }
                    else if (((ItemClothing) i.getItem()).isStraw() && (ItemClothing) i.getItem() != TFCItems.strawSocks)
                    {
                        numStraw++;
                    }
                    result[0] += heat;
                    result[1] += cool;
                }
            }
        }
        if (numWool > 1)
        {
            result[0]--;
        }
        if (numStraw > 1)
        {
            result[0] += numStraw / 2;
        }
        //this could extremely easily be rewritten as an inject statement, and probably should
        // I couldent get inject to work for whatever reason


        loop:
        {
            for (int i = 1; i < 5; i++) {
                ItemStack stack = player.getEquipmentInSlot(i);
                if (!(stack == null)) {
                    if (!(stack.getItem() instanceof ArmorFSB)) {
                        break loop;
                    }
                    //((ArmorFSB) item.getItem())
                    else if(!(((ArmorFSB) stack.getItem()).canSeal) ){
                        break loop;
                    }
                }
                else{
                    break loop;
                }
            }
            result = new int[] { 1000, 1000 };

        }







        return result;
    }

    /*
    @Inject(method = "getTemperatureResistanceFromClothes", at = @At(value = "RETURN"))
    private void mixin( CallbackInfo ci) {
        FMLLog.log("yo mama", Level.INFO, "PUSSY");
        //EntityPlayer player = (EntityPlayer) event.entityLiving;
        //BodyTempStats bodyTemp = TFC_Core.getBodyTempStats(player);
        //bodyTemp.coldResistance = 100;
        //bodyTemp.heatResistance = 100;


    }

     */




}
