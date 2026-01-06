package com.myname.mymodid.mixins.late;

import cpw.mods.fml.common.FMLLog;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//import com.dunk.tfc.WorldGen.WorldCacheManager;

import com.dunk.tfc.WorldGen.GenLayers.Biome.GenLayerBiomeEdge;

import com.dunk.tfc.WorldGen.DataLayer;
import com.dunk.tfc.WorldGen.GenLayers.GenLayerTFC;
import com.dunk.tfc.WorldGen.Data.DataCache;
import net.minecraft.world.World;
import com.dunk.tfc.WorldGen.WorldCacheManager;
import java.util.Map;

import com.dunk.tfc.Core.TFC_Climate;
import com.dunk.tfc.Core.TFC_Core;
import net.minecraft.world.WorldProvider;


@Mixin(value=TFC_Climate.class, remap=false)
public class MixinMinecraft_Example2 {
    @Shadow
    public static Map<World, WorldCacheManager> worldPair;

    @Overwrite
    public static WorldCacheManager getCacheManager(World world)
    {
        //FMLLog.log("yo mama", Level.INFO, "PUSSY");
        if (worldPair.get(world) == null){
            worldPair.put(world, new WorldCacheManager(world));
            TFC_Core.addCDM(world);
            // return a non null value
            // return worldPair.get(world);
        }
        else{
            //if its normal than dont

        }
        return worldPair.get(world);

    }
}



/*
// Late mixin example
@Mixin(value=WorldCacheManager.class, remap=false)
public class MixinMinecraft_Example2 {
    @Shadow
    protected DataCache phCache;

    @Shadow
    protected GenLayerTFC phIndexLayer;

    @Overwrite
    public DataLayer getPHLayerAt(int x, int z)
    {
        FMLLog.log("yo mama", Level.INFO, "PUSSY");
        DataLayer dl = this.phCache.getDataLayerAt(phIndexLayer, x, z);
        return dl != null ? dl : DataLayer.PH_NEUTRAL;

        //MyMod.LOG.info("PUSSY");
    }
}

 */




/*
    @Inject(method="getPHLayerAt", at=@At("INVOKE"))
    private void init(CallbackInfo info) {
        //LOGGER.info("This line is printed by the Baubles example late mixin!");
    }
 */
