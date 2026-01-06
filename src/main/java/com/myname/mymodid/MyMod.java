package com.myname.mymodid;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
//import com.myname.mymodid.ArmorCheck;

import com.hbm.tileentity.machine.TileEntityMachinePumpBase;
import com.dunk.tfc.api.TFCBlocks;


@Mod(modid = MyMod.MODID, version = Tags.VERSION, name = "MyMod", acceptedMinecraftVersions = "[1.7.10]", dependencies = "required-after:terrafirmacraftplus;required-after:hbm;")
public class MyMod {

    public static final String MODID = "mymodid";
    public static final Logger LOG = LogManager.getLogger(MODID);


    @SidedProxy(clientSide = "com.myname.mymodid.ClientProxy", serverSide = "com.myname.mymodid.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        proxy.init(event);

        //MinecraftForge.EVENT_BUS.register(new ArmorCheck());
        //MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventHandler
    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);

        TileEntityMachinePumpBase.validBlocks.add(TFCBlocks.stoneIgEx);
        TileEntityMachinePumpBase.validBlocks.add(TFCBlocks.stoneMM);
        TileEntityMachinePumpBase.validBlocks.add(TFCBlocks.stoneSed);

    }

    @Mod.EventHandler
    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }
}
