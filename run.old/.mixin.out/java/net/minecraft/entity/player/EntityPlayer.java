package net.minecraft.entity.player;

import com.google.common.base.Charsets;
import com.llamalad7.mixinextras.sugar.impl.ref.generated.LocalIntRefImpl;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.mitchej123.hodgepodge.config.FixesConfig;
import com.mitchej123.hodgepodge.mixins.interfaces.GameRuleExt;
import com.mojang.authlib.GameProfile;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.EntityList.EntityEggInfo;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityMinecartHopper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.FoodStats;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings.GameType;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ISpecialArmor.ArmorProperties;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerFlyableFallEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

public abstract class EntityPlayer extends EntityLivingBase implements ICommandSender {
    public static final String PERSISTED_NBT_TAG = "PlayerPersisted";
    private HashMap<Integer, ChunkCoordinates> spawnChunkMap = new HashMap();
    private HashMap<Integer, Boolean> spawnForcedMap = new HashMap();
    public InventoryPlayer inventory = new InventoryPlayer(this);
    private InventoryEnderChest theInventoryEnderChest = new InventoryEnderChest();
    public Container inventoryContainer;
    public Container openContainer;
    protected FoodStats foodStats = new FoodStats();
    protected int flyToggleTimer;
    public float prevCameraYaw;
    public float cameraYaw;
    public int xpCooldown;
    public double field_71091_bM;
    public double field_71096_bN;
    public double field_71097_bO;
    public double field_71094_bP;
    public double field_71095_bQ;
    public double field_71085_bR;
    protected boolean sleeping;
    public ChunkCoordinates playerLocation;
    private int sleepTimer;
    public float field_71079_bU;
    @SideOnly(Side.CLIENT)
    public float field_71082_cx;
    public float field_71089_bV;
    private ChunkCoordinates spawnChunk;
    private boolean spawnForced;
    private ChunkCoordinates startMinecartRidingCoordinate;
    public PlayerCapabilities capabilities = new PlayerCapabilities();
    public int experienceLevel;
    public int experienceTotal;
    public float experience;
    private ItemStack itemInUse;
    public int itemInUseCount;
    protected float speedOnGround = 0.1F;
    protected float speedInAir = 0.02F;
    private int field_82249_h;
    private final GameProfile field_146106_i;
    public EntityFishHook fishEntity;
    private static final String __OBFID = "CL_00001711";
    public float eyeHeight;
    private String displayname;

    public EntityPlayer(World p_i45324_1_, GameProfile p_i45324_2_) {
        super(p_i45324_1_);
        this.entityUniqueID = func_146094_a(p_i45324_2_);
        this.field_146106_i = p_i45324_2_;
        this.inventoryContainer = new ContainerPlayer(this.inventory, !p_i45324_1_.isRemote, this);
        this.openContainer = this.inventoryContainer;
        this.yOffset = 1.62F;
        ChunkCoordinates chunkcoordinates = p_i45324_1_.getSpawnPoint();
        this.setLocationAndAngles((double)chunkcoordinates.posX + 0.5D, (double)(chunkcoordinates.posY + 1), (double)chunkcoordinates.posZ + 0.5D, 0.0F, 0.0F);
        this.field_70741_aB = 180.0F;
        this.fireResistance = 20;
        this.eyeHeight = this.getDefaultEyeHeight();
    }

    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(1.0D);
    }

    protected void entityInit() {
        super.entityInit();
        this.dataWatcher.addObject(16, (byte)0);
        this.dataWatcher.addObject(17, 0.0F);
        this.dataWatcher.addObject(18, 0);
    }

    @SideOnly(Side.CLIENT)
    public ItemStack getItemInUse() {
        return this.itemInUse;
    }

    @SideOnly(Side.CLIENT)
    public int getItemInUseCount() {
        return this.itemInUseCount;
    }

    public boolean isUsingItem() {
        return this.itemInUse != null;
    }

    @SideOnly(Side.CLIENT)
    public int getItemInUseDuration() {
        return this.isUsingItem() ? this.itemInUse.getMaxItemUseDuration() - this.itemInUseCount : 0;
    }

    public void stopUsingItem() {
        if (this.itemInUse != null && !ForgeEventFactory.onUseItemStop(this, this.itemInUse, this.itemInUseCount)) {
            this.itemInUse.onPlayerStoppedUsing(this.worldObj, this, this.itemInUseCount);
        }

        this.clearItemInUse();
    }

    public void clearItemInUse() {
        this.itemInUse = null;
        this.itemInUseCount = 0;
        if (!this.worldObj.isRemote) {
            this.setEating(false);
        }

    }

    public boolean isBlocking() {
        return this.isUsingItem() && this.itemInUse.getItem().getItemUseAction(this.itemInUse) == EnumAction.block;
    }

    public void onUpdate() {
        FMLCommonHandler.instance().onPlayerPreTick(this);
        if (this.itemInUse != null) {
            ItemStack itemstack = this.inventory.getCurrentItem();
            if (itemstack == this.itemInUse) {
                this.itemInUseCount = ForgeEventFactory.onItemUseTick(this, this.itemInUse, this.itemInUseCount);
                if (this.itemInUseCount <= 0) {
                    this.onItemUseFinish();
                } else {
                    this.itemInUse.getItem().onUsingTick(this.itemInUse, this, this.itemInUseCount);
                    if (this.itemInUseCount <= 25 && this.itemInUseCount % 4 == 0) {
                        this.updateItemUse(itemstack, 5);
                    }

                    if (--this.itemInUseCount == 0 && !this.worldObj.isRemote) {
                        this.onItemUseFinish();
                    }
                }
            } else {
                this.clearItemInUse();
            }
        }

        if (this.xpCooldown > 0) {
            --this.xpCooldown;
        }

        if (this.isPlayerSleeping()) {
            ++this.sleepTimer;
            if (this.sleepTimer > 100) {
                this.sleepTimer = 100;
            }

            if (!this.worldObj.isRemote) {
                if (!this.isInBed()) {
                    this.wakeUpPlayer(true, true, false);
                } else if (this.worldObj.isDaytime()) {
                    this.wakeUpPlayer(false, true, true);
                }
            }
        } else if (this.sleepTimer > 0) {
            ++this.sleepTimer;
            if (this.sleepTimer >= 110) {
                this.sleepTimer = 0;
            }
        }

        super.onUpdate();
        if (!this.worldObj.isRemote && this.openContainer != null && !ForgeHooks.canInteractWith(this, this.openContainer)) {
            this.closeScreen();
            this.openContainer = this.inventoryContainer;
        }

        if (this.isBurning() && this.capabilities.disableDamage) {
            this.extinguish();
        }

        this.field_71091_bM = this.field_71094_bP;
        this.field_71096_bN = this.field_71095_bQ;
        this.field_71097_bO = this.field_71085_bR;
        double d3 = this.posX - this.field_71094_bP;
        double d0 = this.posY - this.field_71095_bQ;
        double d1 = this.posZ - this.field_71085_bR;
        double d2 = 10.0D;
        if (d3 > d2) {
            this.field_71091_bM = this.field_71094_bP = this.posX;
        }

        if (d1 > d2) {
            this.field_71097_bO = this.field_71085_bR = this.posZ;
        }

        if (d0 > d2) {
            this.field_71096_bN = this.field_71095_bQ = this.posY;
        }

        if (d3 < -d2) {
            this.field_71091_bM = this.field_71094_bP = this.posX;
        }

        if (d1 < -d2) {
            this.field_71097_bO = this.field_71085_bR = this.posZ;
        }

        if (d0 < -d2) {
            this.field_71096_bN = this.field_71095_bQ = this.posY;
        }

        this.field_71094_bP += d3 * 0.25D;
        this.field_71085_bR += d1 * 0.25D;
        this.field_71095_bQ += d0 * 0.25D;
        if (this.ridingEntity == null) {
            this.startMinecartRidingCoordinate = null;
        }

        if (!this.worldObj.isRemote) {
            this.foodStats.onUpdate(this);
            this.addStat(StatList.minutesPlayedStat, 1);
        }

        FMLCommonHandler.instance().onPlayerPostTick(this);
    }

    public int getMaxInPortalTime() {
        return this.capabilities.disableDamage ? 0 : 80;
    }

    protected String getSwimSound() {
        return "game.player.swim";
    }

    protected String getSplashSound() {
        return "game.player.swim.splash";
    }

    public int getPortalCooldown() {
        return 10;
    }

    public void playSound(String name, float volume, float pitch) {
        this.worldObj.playSoundToNearExcept(this, name, volume, pitch);
    }

    protected void updateItemUse(ItemStack itemStackIn, int p_71010_2_) {
        if (itemStackIn.getItemUseAction() == EnumAction.drink) {
            this.playSound("random.drink", 0.5F, this.worldObj.rand.nextFloat() * 0.1F + 0.9F);
        }

        if (itemStackIn.getItemUseAction() == EnumAction.eat) {
            for(int j = 0; j < p_71010_2_; ++j) {
                Vec3 vec3 = Vec3.createVectorHelper(((double)this.rand.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
                vec3.rotateAroundX(-this.rotationPitch * 3.1415927F / 180.0F);
                vec3.rotateAroundY(-this.rotationYaw * 3.1415927F / 180.0F);
                Vec3 vec31 = Vec3.createVectorHelper(((double)this.rand.nextFloat() - 0.5D) * 0.3D, (double)(-this.rand.nextFloat()) * 0.6D - 0.3D, 0.6D);
                vec31.rotateAroundX(-this.rotationPitch * 3.1415927F / 180.0F);
                vec31.rotateAroundY(-this.rotationYaw * 3.1415927F / 180.0F);
                vec31 = vec31.addVector(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ);
                String s = "iconcrack_" + Item.getIdFromItem(itemStackIn.getItem());
                if (itemStackIn.getHasSubtypes()) {
                    s = s + "_" + itemStackIn.getItemDamage();
                }

                this.worldObj.spawnParticle(s, vec31.xCoord, vec31.yCoord, vec31.zCoord, vec3.xCoord, vec3.yCoord + 0.05D, vec3.zCoord);
            }

            this.playSound("random.eat", 0.5F + 0.5F * (float)this.rand.nextInt(2), (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
        }

    }

    protected void onItemUseFinish() {
        if (this.itemInUse != null) {
            this.updateItemUse(this.itemInUse, 16);
            int i = this.itemInUse.stackSize;
            ItemStack itemstack = this.itemInUse.onFoodEaten(this.worldObj, this);
            itemstack = ForgeEventFactory.onItemUseFinish(this, this.itemInUse, this.itemInUseCount, itemstack);
            if (itemstack != this.itemInUse || itemstack != null && itemstack.stackSize != i) {
                this.inventory.mainInventory[this.inventory.currentItem] = itemstack;
                if (itemstack != null && itemstack.stackSize == 0) {
                    this.inventory.mainInventory[this.inventory.currentItem] = null;
                }
            }

            this.clearItemInUse();
        }

    }

    @SideOnly(Side.CLIENT)
    public void handleHealthUpdate(byte p_70103_1_) {
        if (p_70103_1_ == 9) {
            this.onItemUseFinish();
        } else {
            super.handleHealthUpdate(p_70103_1_);
        }

    }

    protected boolean isMovementBlocked() {
        return this.getHealth() <= 0.0F || this.isPlayerSleeping();
    }

    public void closeScreen() {
        this.openContainer = this.inventoryContainer;
    }

    public void mountEntity(Entity entityIn) {
        if (this.ridingEntity != null && entityIn == null) {
            if (!this.worldObj.isRemote) {
                this.dismountEntity(this.ridingEntity);
            }

            if (this.ridingEntity != null) {
                this.ridingEntity.riddenByEntity = null;
            }

            this.ridingEntity = null;
        } else {
            super.mountEntity(entityIn);
        }

    }

    public void updateRidden() {
        if (!this.worldObj.isRemote && this.isSneaking()) {
            this.mountEntity((Entity)null);
            this.setSneaking(false);
        } else {
            double d0 = this.posX;
            double d1 = this.posY;
            double d2 = this.posZ;
            float f = this.rotationYaw;
            float f1 = this.rotationPitch;
            super.updateRidden();
            this.prevCameraYaw = this.cameraYaw;
            this.cameraYaw = 0.0F;
            this.addMountedMovementStat(this.posX - d0, this.posY - d1, this.posZ - d2);
            if (this.ridingEntity instanceof EntityLivingBase && ((EntityLivingBase)this.ridingEntity).shouldRiderFaceForward(this)) {
                this.rotationPitch = f1;
                this.rotationYaw = f;
                this.renderYawOffset = ((EntityLivingBase)this.ridingEntity).renderYawOffset;
            }
        }

    }

    @SideOnly(Side.CLIENT)
    public void preparePlayerToSpawn() {
        this.yOffset = 1.62F;
        this.setSize(0.6F, 1.8F);
        super.preparePlayerToSpawn();
        this.setHealth(this.getMaxHealth());
        this.deathTime = 0;
    }

    protected void updateEntityActionState() {
        super.updateEntityActionState();
        this.updateArmSwingProgress();
    }

    public void onLivingUpdate() {
        LocalIntRefImpl sharedRef8 = new LocalIntRefImpl();
        sharedRef8.init(0);
        if (this.flyToggleTimer > 0) {
            --this.flyToggleTimer;
        }

        if (this.worldObj.difficultySetting == EnumDifficulty.PEACEFUL && this.getHealth() < this.getMaxHealth() && this.worldObj.getGameRules().getGameRuleBooleanValue("naturalRegeneration") && this.ticksExisted % 20 * 12 == 0) {
            this.heal(1.0F);
        }

        this.inventory.decrementAnimations();
        this.prevCameraYaw = this.cameraYaw;
        super.onLivingUpdate();
        IAttributeInstance iattributeinstance = this.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
        if (!this.worldObj.isRemote) {
            iattributeinstance.setBaseValue((double)this.capabilities.getWalkSpeed());
        }

        this.jumpMovementFactor = this.speedInAir;
        if (this.isSprinting()) {
            this.jumpMovementFactor = (float)((double)this.jumpMovementFactor + (double)this.speedInAir * 0.3D);
        }

        this.setAIMoveSpeed((float)iattributeinstance.getAttributeValue());
        float f = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
        float f1 = (float)Math.atan(-this.motionY * 0.20000000298023224D) * 15.0F;
        if (f > 0.1F) {
            f = 0.1F;
        }

        if (!this.onGround || this.getHealth() <= 0.0F) {
            f = 0.0F;
        }

        if (this.onGround || this.getHealth() <= 0.0F) {
            f1 = 0.0F;
        }

        this.cameraYaw += (f - this.cameraYaw) * 0.4F;
        this.cameraPitch += (f1 - this.cameraPitch) * 0.8F;
        if (this.getHealth() > 0.0F) {
            AxisAlignedBB axisalignedbb = null;
            if (this.ridingEntity != null && !this.ridingEntity.isDead) {
                axisalignedbb = this.boundingBox.func_111270_a(this.ridingEntity.boundingBox).expand(1.0D, 0.0D, 1.0D);
            } else {
                axisalignedbb = this.boundingBox.expand(1.0D, 0.5D, 1.0D);
            }

            List list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, axisalignedbb);
            this.handler$zca000$hodgepodge$resetItemCounter((CallbackInfo)null, sharedRef8);
            if (list != null) {
                for(int i = 0; i < list.size(); ++i) {
                    Entity entity = (Entity)list.get(i);
                    if (!entity.isDead) {
                        this.redirect$zca000$hodgepodge$throttleItemPickupEvent(this, entity, sharedRef8);
                    }
                }
            }
        }

    }

    private void collideWithPlayer(Entity p_71044_1_) {
        p_71044_1_.onCollideWithPlayer(this);
    }

    public int getScore() {
        return this.dataWatcher.getWatchableObjectInt(18);
    }

    public void setScore(int p_85040_1_) {
        this.dataWatcher.updateObject(18, p_85040_1_);
    }

    public void addScore(int p_85039_1_) {
        int j = this.getScore();
        this.dataWatcher.updateObject(18, j + p_85039_1_);
    }

    public void onDeath(DamageSource p_70645_1_) {
        if (!ForgeHooks.onLivingDeath(this, p_70645_1_)) {
            super.onDeath(p_70645_1_);
            this.setSize(0.2F, 0.2F);
            this.setPosition(this.posX, this.posY, this.posZ);
            this.motionY = 0.10000000149011612D;
            this.captureDrops = true;
            this.capturedDrops.clear();
            if (this.getCommandSenderName().equals("Notch")) {
                this.func_146097_a(new ItemStack(Items.apple, 1), true, false);
            }

            if (!this.worldObj.getGameRules().getGameRuleBooleanValue("keepInventory")) {
                this.inventory.dropAllItems();
            }

            this.captureDrops = false;
            if (!this.worldObj.isRemote) {
                PlayerDropsEvent event = new PlayerDropsEvent(this, p_70645_1_, this.capturedDrops, this.recentlyHit > 0);
                if (!MinecraftForge.EVENT_BUS.post(event)) {
                    Iterator var3 = this.capturedDrops.iterator();

                    while(var3.hasNext()) {
                        EntityItem item = (EntityItem)var3.next();
                        this.joinEntityItemWithWorld(item);
                    }
                }
            }

            if (p_70645_1_ != null) {
                this.motionX = (double)(-MathHelper.cos((this.attackedAtYaw + this.rotationYaw) * 3.1415927F / 180.0F) * 0.1F);
                this.motionZ = (double)(-MathHelper.sin((this.attackedAtYaw + this.rotationYaw) * 3.1415927F / 180.0F) * 0.1F);
            } else {
                this.motionX = this.motionZ = 0.0D;
            }

            this.yOffset = 0.1F;
            this.addStat(StatList.deathsStat, 1);
        }
    }

    protected String getHurtSound() {
        return "game.player.hurt";
    }

    protected String getDeathSound() {
        return "game.player.die";
    }

    public void addToPlayerScore(Entity entityIn, int amount) {
        this.addScore(amount);
        Collection collection = this.getWorldScoreboard().func_96520_a(IScoreObjectiveCriteria.totalKillCount);
        if (entityIn instanceof EntityPlayer) {
            this.addStat(StatList.playerKillsStat, 1);
            collection.addAll(this.getWorldScoreboard().func_96520_a(IScoreObjectiveCriteria.playerKillCount));
        } else {
            this.addStat(StatList.mobKillsStat, 1);
        }

        Iterator iterator = collection.iterator();

        while(iterator.hasNext()) {
            ScoreObjective scoreobjective = (ScoreObjective)iterator.next();
            Score score = this.getWorldScoreboard().func_96529_a(this.getCommandSenderName(), scoreobjective);
            score.func_96648_a();
        }

    }

    public EntityItem dropOneItem(boolean p_71040_1_) {
        ItemStack stack = this.inventory.getCurrentItem();
        if (stack == null) {
            return null;
        } else if (!stack.getItem().onDroppedByPlayer(stack, this)) {
            return null;
        } else {
            int count = p_71040_1_ && this.inventory.getCurrentItem() != null ? this.inventory.getCurrentItem().stackSize : 1;
            return ForgeHooks.onPlayerTossEvent(this, this.inventory.decrStackSize(this.inventory.currentItem, count), true);
        }
    }

    public EntityItem dropPlayerItemWithRandomChoice(ItemStack itemStackIn, boolean p_71019_2_) {
        boolean injectorAllocatedLocal3 = false;
        return ForgeHooks.onPlayerTossEvent(this, itemStackIn, this.modify$zcb000$hodgepodge$passThrower(injectorAllocatedLocal3, p_71019_2_));
    }

    public EntityItem func_146097_a(ItemStack p_146097_1_, boolean p_146097_2_, boolean p_146097_3_) {
        if (p_146097_1_ == null) {
            return null;
        } else if (p_146097_1_.stackSize == 0) {
            return null;
        } else {
            EntityItem entityitem = new EntityItem(this.worldObj, this.posX, this.posY - 0.30000001192092896D + (double)this.getEyeHeight(), this.posZ, p_146097_1_);
            entityitem.delayBeforeCanPickup = 40;
            if (p_146097_3_) {
                entityitem.func_145799_b(this.getCommandSenderName());
            }

            float f = 0.1F;
            float f1;
            if (p_146097_2_) {
                f1 = this.rand.nextFloat() * 0.5F;
                float f2 = this.rand.nextFloat() * 3.1415927F * 2.0F;
                entityitem.motionX = (double)(-MathHelper.sin(f2) * f1);
                entityitem.motionZ = (double)(MathHelper.cos(f2) * f1);
                entityitem.motionY = 0.20000000298023224D;
            } else {
                f = 0.3F;
                entityitem.motionX = (double)(-MathHelper.sin(this.rotationYaw / 180.0F * 3.1415927F) * MathHelper.cos(this.rotationPitch / 180.0F * 3.1415927F) * f);
                entityitem.motionZ = (double)(MathHelper.cos(this.rotationYaw / 180.0F * 3.1415927F) * MathHelper.cos(this.rotationPitch / 180.0F * 3.1415927F) * f);
                entityitem.motionY = (double)(-MathHelper.sin(this.rotationPitch / 180.0F * 3.1415927F) * f + 0.1F);
                f = 0.02F;
                f1 = this.rand.nextFloat() * 3.1415927F * 2.0F;
                f *= this.rand.nextFloat();
                entityitem.motionX += Math.cos((double)f1) * (double)f;
                entityitem.motionY += (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F);
                entityitem.motionZ += Math.sin((double)f1) * (double)f;
            }

            this.joinEntityItemWithWorld(entityitem);
            this.addStat(StatList.dropStat, 1);
            return entityitem;
        }
    }

    public void joinEntityItemWithWorld(EntityItem p_71012_1_) {
        if (this.captureDrops) {
            this.capturedDrops.add(p_71012_1_);
        } else {
            this.worldObj.spawnEntityInWorld(p_71012_1_);
        }
    }

    /** @deprecated */
    @Deprecated
    public float getCurrentPlayerStrVsBlock(Block p_146096_1_, boolean p_146096_2_) {
        return this.getBreakSpeed(p_146096_1_, p_146096_2_, 0, 0, -1, 0);
    }

    /** @deprecated */
    @Deprecated
    public float getBreakSpeed(Block p_146096_1_, boolean p_146096_2_, int meta) {
        return this.getBreakSpeed(p_146096_1_, p_146096_2_, meta, 0, -1, 0);
    }

    public float getBreakSpeed(Block p_146096_1_, boolean p_146096_2_, int meta, int x, int y, int z) {
        ItemStack stack = this.inventory.getCurrentItem();
        float f = stack == null ? 1.0F : stack.getItem().getDigSpeed(stack, p_146096_1_, meta);
        if (f > 1.0F) {
            int i = EnchantmentHelper.getEfficiencyModifier(this);
            ItemStack itemstack = this.inventory.getCurrentItem();
            if (i > 0 && itemstack != null) {
                float f1 = (float)(i * i + 1);
                boolean canHarvest = ForgeHooks.canToolHarvestBlock(p_146096_1_, meta, itemstack);
                if (!canHarvest && f <= 1.0F) {
                    f += f1 * 0.08F;
                } else {
                    f += f1;
                }
            }
        }

        if (this.isPotionActive(Potion.digSpeed)) {
            f *= 1.0F + (float)(this.getActivePotionEffect(Potion.digSpeed).getAmplifier() + 1) * 0.2F;
        }

        if (this.isPotionActive(Potion.digSlowdown)) {
            f *= 1.0F - (float)(this.getActivePotionEffect(Potion.digSlowdown).getAmplifier() + 1) * 0.2F;
        }

        if (this.isInsideOfMaterial(Material.water) && !EnchantmentHelper.getAquaAffinityModifier(this)) {
            f /= 5.0F;
        }

        if (!this.onGround) {
            f /= 5.0F;
        }

        f = ForgeEventFactory.getBreakSpeed(this, p_146096_1_, meta, f, x, y, z);
        return f < 0.0F ? 0.0F : f;
    }

    public boolean canHarvestBlock(Block p_146099_1_) {
        return ForgeEventFactory.doPlayerHarvestCheck(this, p_146099_1_, this.inventory.func_146025_b(p_146099_1_));
    }

    public void readEntityFromNBT(NBTTagCompound tagCompund) {
        super.readEntityFromNBT(tagCompund);
        this.entityUniqueID = func_146094_a(this.field_146106_i);
        NBTTagList nbttaglist = tagCompund.getTagList("Inventory", 10);
        this.inventory.readFromNBT(nbttaglist);
        this.inventory.currentItem = tagCompund.getInteger("SelectedItemSlot");
        this.sleeping = tagCompund.getBoolean("Sleeping");
        this.sleepTimer = tagCompund.getShort("SleepTimer");
        this.experience = tagCompund.getFloat("XpP");
        this.experienceLevel = tagCompund.getInteger("XpLevel");
        this.experienceTotal = tagCompund.getInteger("XpTotal");
        this.setScore(tagCompund.getInteger("Score"));
        if (this.sleeping) {
            this.playerLocation = new ChunkCoordinates(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ));
            this.wakeUpPlayer(true, true, false);
        }

        if (tagCompund.hasKey("SpawnX", 99) && tagCompund.hasKey("SpawnY", 99) && tagCompund.hasKey("SpawnZ", 99)) {
            this.spawnChunk = new ChunkCoordinates(tagCompund.getInteger("SpawnX"), tagCompund.getInteger("SpawnY"), tagCompund.getInteger("SpawnZ"));
            this.spawnForced = tagCompund.getBoolean("SpawnForced");
        }

        NBTTagList spawnlist = null;
        spawnlist = tagCompund.getTagList("Spawns", 10);

        for(int i = 0; i < spawnlist.tagCount(); ++i) {
            NBTTagCompound spawndata = spawnlist.getCompoundTagAt(i);
            int spawndim = spawndata.getInteger("Dim");
            this.spawnChunkMap.put(spawndim, new ChunkCoordinates(spawndata.getInteger("SpawnX"), spawndata.getInteger("SpawnY"), spawndata.getInteger("SpawnZ")));
            this.spawnForcedMap.put(spawndim, spawndata.getBoolean("SpawnForced"));
        }

        this.foodStats.readNBT(tagCompund);
        this.capabilities.readCapabilitiesFromNBT(tagCompund);
        if (tagCompund.hasKey("EnderItems", 9)) {
            NBTTagList nbttaglist1 = tagCompund.getTagList("EnderItems", 10);
            this.theInventoryEnderChest.loadInventoryFromNBT(nbttaglist1);
        }

    }

    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setTag("Inventory", this.inventory.writeToNBT(new NBTTagList()));
        tagCompound.setInteger("SelectedItemSlot", this.inventory.currentItem);
        tagCompound.setBoolean("Sleeping", this.sleeping);
        tagCompound.setShort("SleepTimer", (short)this.sleepTimer);
        tagCompound.setFloat("XpP", this.experience);
        tagCompound.setInteger("XpLevel", this.experienceLevel);
        tagCompound.setInteger("XpTotal", this.experienceTotal);
        tagCompound.setInteger("Score", this.getScore());
        if (this.spawnChunk != null) {
            tagCompound.setInteger("SpawnX", this.spawnChunk.posX);
            tagCompound.setInteger("SpawnY", this.spawnChunk.posY);
            tagCompound.setInteger("SpawnZ", this.spawnChunk.posZ);
            tagCompound.setBoolean("SpawnForced", this.spawnForced);
        }

        NBTTagList spawnlist = new NBTTagList();
        Iterator var3 = this.spawnChunkMap.entrySet().iterator();

        while(var3.hasNext()) {
            Entry<Integer, ChunkCoordinates> entry = (Entry)var3.next();
            ChunkCoordinates spawn = (ChunkCoordinates)entry.getValue();
            if (spawn != null) {
                Boolean forced = (Boolean)this.spawnForcedMap.get(entry.getKey());
                if (forced == null) {
                    forced = false;
                }

                NBTTagCompound spawndata = new NBTTagCompound();
                spawndata.setInteger("Dim", (Integer)entry.getKey());
                spawndata.setInteger("SpawnX", spawn.posX);
                spawndata.setInteger("SpawnY", spawn.posY);
                spawndata.setInteger("SpawnZ", spawn.posZ);
                spawndata.setBoolean("SpawnForced", forced);
                spawnlist.appendTag(spawndata);
            }
        }

        tagCompound.setTag("Spawns", spawnlist);
        this.foodStats.writeNBT(tagCompound);
        this.capabilities.writeCapabilitiesToNBT(tagCompound);
        tagCompound.setTag("EnderItems", this.theInventoryEnderChest.saveInventoryToNBT());
    }

    public void displayGUIChest(IInventory p_71007_1_) {
    }

    public void func_146093_a(TileEntityHopper p_146093_1_) {
    }

    public void displayGUIHopperMinecart(EntityMinecartHopper p_96125_1_) {
    }

    public void displayGUIHorse(EntityHorse p_110298_1_, IInventory p_110298_2_) {
    }

    public void displayGUIEnchantment(int p_71002_1_, int p_71002_2_, int p_71002_3_, String p_71002_4_) {
    }

    public void displayGUIAnvil(int p_82244_1_, int p_82244_2_, int p_82244_3_) {
    }

    public void displayGUIWorkbench(int p_71058_1_, int p_71058_2_, int p_71058_3_) {
    }

    public float getEyeHeight() {
        return this.eyeHeight;
    }

    protected void resetHeight() {
        this.yOffset = 1.62F;
    }

    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (ForgeHooks.onLivingAttack(this, source, amount)) {
            return false;
        } else if (this.isEntityInvulnerable()) {
            return false;
        } else if (this.capabilities.disableDamage && !source.canHarmInCreative()) {
            return false;
        } else {
            this.entityAge = 0;
            if (this.getHealth() <= 0.0F) {
                return false;
            } else {
                if (this.isPlayerSleeping() && !this.worldObj.isRemote) {
                    this.wakeUpPlayer(true, true, false);
                }

                if (source.isDifficultyScaled()) {
                    if (this.worldObj.difficultySetting == EnumDifficulty.PEACEFUL) {
                        amount = 0.0F;
                    }

                    if (this.worldObj.difficultySetting == EnumDifficulty.EASY) {
                        amount = amount / 2.0F + 1.0F;
                    }

                    if (this.worldObj.difficultySetting == EnumDifficulty.HARD) {
                        amount = amount * 3.0F / 2.0F;
                    }
                }

                if (amount == 0.0F) {
                    return false;
                } else {
                    Entity entity = source.getEntity();
                    if (entity instanceof EntityArrow && ((EntityArrow)entity).shootingEntity != null) {
                        entity = ((EntityArrow)entity).shootingEntity;
                    }

                    this.addStat(StatList.damageTakenStat, Math.round(amount * 10.0F));
                    return super.attackEntityFrom(source, amount);
                }
            }
        }
    }

    public boolean canAttackPlayer(EntityPlayer p_96122_1_) {
        Team team = this.getTeam();
        Team team1 = p_96122_1_.getTeam();
        return team == null ? true : (!team.isSameTeam(team1) ? true : team.getAllowFriendlyFire());
    }

    protected void damageArmor(float p_70675_1_) {
        this.inventory.damageArmor(p_70675_1_);
    }

    public int getTotalArmorValue() {
        return this.inventory.getTotalArmorValue();
    }

    public float getArmorVisibility() {
        int i = 0;
        ItemStack[] aitemstack = this.inventory.armorInventory;
        int j = aitemstack.length;

        for(int k = 0; k < j; ++k) {
            ItemStack itemstack = aitemstack[k];
            if (itemstack != null) {
                ++i;
            }
        }

        return (float)i / (float)this.inventory.armorInventory.length;
    }

    protected void damageEntity(DamageSource p_70665_1_, float p_70665_2_) {
        if (!this.isEntityInvulnerable()) {
            p_70665_2_ = ForgeHooks.onLivingHurt(this, p_70665_1_, p_70665_2_);
            if (p_70665_2_ <= 0.0F) {
                return;
            }

            if (!p_70665_1_.isUnblockable() && this.isBlocking() && p_70665_2_ > 0.0F) {
                p_70665_2_ = (1.0F + p_70665_2_) * 0.5F;
            }

            p_70665_2_ = ArmorProperties.ApplyArmor(this, this.inventory.armorInventory, p_70665_1_, (double)p_70665_2_);
            if (p_70665_2_ <= 0.0F) {
                return;
            }

            p_70665_2_ = this.applyPotionDamageCalculations(p_70665_1_, p_70665_2_);
            float f1 = p_70665_2_;
            p_70665_2_ = Math.max(p_70665_2_ - this.getAbsorptionAmount(), 0.0F);
            this.setAbsorptionAmount(this.getAbsorptionAmount() - (f1 - p_70665_2_));
            if (p_70665_2_ != 0.0F) {
                this.addExhaustion(p_70665_1_.getHungerDamage());
                float f2 = this.getHealth();
                this.setHealth(this.getHealth() - p_70665_2_);
                this.func_110142_aN().func_94547_a(p_70665_1_, f2, p_70665_2_);
            }
        }

    }

    public void func_146101_a(TileEntityFurnace p_146101_1_) {
    }

    public void func_146102_a(TileEntityDispenser p_146102_1_) {
    }

    public void func_146100_a(TileEntity p_146100_1_) {
    }

    public void func_146095_a(CommandBlockLogic p_146095_1_) {
    }

    public void func_146098_a(TileEntityBrewingStand p_146098_1_) {
    }

    public void func_146104_a(TileEntityBeacon p_146104_1_) {
    }

    public void displayGUIMerchant(IMerchant p_71030_1_, String p_71030_2_) {
    }

    public void displayGUIBook(ItemStack p_71048_1_) {
    }

    public boolean interactWith(Entity p_70998_1_) {
        if (MinecraftForge.EVENT_BUS.post(new EntityInteractEvent(this, p_70998_1_))) {
            return false;
        } else {
            ItemStack itemstack = this.getCurrentEquippedItem();
            ItemStack itemstack1 = itemstack != null ? itemstack.copy() : null;
            if (!p_70998_1_.interactFirst(this)) {
                if (itemstack != null && p_70998_1_ instanceof EntityLivingBase) {
                    if (this.capabilities.isCreativeMode) {
                        itemstack = itemstack1;
                    }

                    if (itemstack.interactWithEntity(this, (EntityLivingBase)p_70998_1_)) {
                        if (itemstack.stackSize <= 0 && !this.capabilities.isCreativeMode) {
                            this.destroyCurrentEquippedItem();
                        }

                        return true;
                    }
                }

                return false;
            } else {
                if (itemstack != null && itemstack == this.getCurrentEquippedItem()) {
                    if (itemstack.stackSize <= 0 && !this.capabilities.isCreativeMode) {
                        this.destroyCurrentEquippedItem();
                    } else if (itemstack.stackSize < itemstack1.stackSize && this.capabilities.isCreativeMode) {
                        itemstack.stackSize = itemstack1.stackSize;
                    }
                }

                return true;
            }
        }
    }

    public ItemStack getCurrentEquippedItem() {
        return this.inventory.getCurrentItem();
    }

    public void destroyCurrentEquippedItem() {
        ItemStack orig = this.getCurrentEquippedItem();
        this.inventory.setInventorySlotContents(this.inventory.currentItem, (ItemStack)null);
        MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(this, orig));
    }

    public double getYOffset() {
        return (double)(this.yOffset - 0.5F);
    }

    public void attackTargetEntityWithCurrentItem(Entity targetEntity) {
        if (!MinecraftForge.EVENT_BUS.post(new AttackEntityEvent(this, targetEntity))) {
            ItemStack stack = this.getCurrentEquippedItem();
            if (stack == null || !stack.getItem().onLeftClickEntity(stack, this, targetEntity)) {
                if (targetEntity.canAttackWithItem() && !targetEntity.hitByEntity(this)) {
                    float f = (float)this.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
                    int i = 0;
                    float f1 = 0.0F;
                    if (targetEntity instanceof EntityLivingBase) {
                        f1 = EnchantmentHelper.getEnchantmentModifierLiving(this, (EntityLivingBase)targetEntity);
                        i += EnchantmentHelper.getKnockbackModifier(this, (EntityLivingBase)targetEntity);
                    }

                    if (this.isSprinting()) {
                        ++i;
                    }

                    if (f > 0.0F || f1 > 0.0F) {
                        boolean flag = this.fallDistance > 0.0F && !this.onGround && !this.isOnLadder() && !this.isInWater() && !this.isPotionActive(Potion.blindness) && this.ridingEntity == null && targetEntity instanceof EntityLivingBase;
                        if (flag && f > 0.0F) {
                            f *= 1.5F;
                        }

                        f += f1;
                        boolean flag1 = false;
                        int j = EnchantmentHelper.getFireAspectModifier(this);
                        if (targetEntity instanceof EntityLivingBase && j > 0 && !targetEntity.isBurning()) {
                            flag1 = true;
                            targetEntity.setFire(1);
                        }

                        boolean flag2 = targetEntity.attackEntityFrom(DamageSource.causePlayerDamage(this), f);
                        if (flag2) {
                            if (i > 0) {
                                targetEntity.addVelocity((double)(-MathHelper.sin(this.rotationYaw * 3.1415927F / 180.0F) * (float)i * 0.5F), 0.1D, (double)(MathHelper.cos(this.rotationYaw * 3.1415927F / 180.0F) * (float)i * 0.5F));
                                this.motionX *= 0.6D;
                                this.motionZ *= 0.6D;
                                this.setSprinting(false);
                            }

                            if (flag) {
                                this.onCriticalHit(targetEntity);
                            }

                            if (f1 > 0.0F) {
                                this.onEnchantmentCritical(targetEntity);
                            }

                            if (f >= 18.0F) {
                                this.triggerAchievement(AchievementList.overkill);
                            }

                            this.setLastAttacker(targetEntity);
                            if (targetEntity instanceof EntityLivingBase) {
                                EnchantmentHelper.func_151384_a((EntityLivingBase)targetEntity, this);
                            }

                            EnchantmentHelper.func_151385_b(this, targetEntity);
                            ItemStack itemstack = this.getCurrentEquippedItem();
                            Object object = targetEntity;
                            if (targetEntity instanceof EntityDragonPart) {
                                IEntityMultiPart ientitymultipart = ((EntityDragonPart)targetEntity).entityDragonObj;
                                if (ientitymultipart != null && ientitymultipart instanceof EntityLivingBase) {
                                    object = (EntityLivingBase)ientitymultipart;
                                }
                            }

                            if (itemstack != null && object instanceof EntityLivingBase) {
                                itemstack.hitEntity((EntityLivingBase)object, this);
                                if (itemstack.stackSize <= 0) {
                                    this.destroyCurrentEquippedItem();
                                }
                            }

                            if (targetEntity instanceof EntityLivingBase) {
                                this.addStat(StatList.damageDealtStat, Math.round(f * 10.0F));
                                if (j > 0) {
                                    targetEntity.setFire(j * 4);
                                }
                            }

                            this.addExhaustion(0.3F);
                        } else if (flag1) {
                            targetEntity.extinguish();
                        }
                    }
                }

            }
        }
    }

    public void onCriticalHit(Entity p_71009_1_) {
    }

    public void onEnchantmentCritical(Entity p_71047_1_) {
    }

    @SideOnly(Side.CLIENT)
    public void respawnPlayer() {
    }

    public void setDead() {
        super.setDead();
        this.inventoryContainer.onContainerClosed(this);
        if (this.openContainer != null) {
            this.openContainer.onContainerClosed(this);
        }

    }

    public boolean isEntityInsideOpaqueBlock() {
        return !this.sleeping && super.isEntityInsideOpaqueBlock();
    }

    public GameProfile getGameProfile() {
        return this.field_146106_i;
    }

    public net.minecraft.entity.player.EntityPlayer.EnumStatus sleepInBedAt(int x, int y, int z) {
        PlayerSleepInBedEvent event = new PlayerSleepInBedEvent(this, x, y, z);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.result != null) {
            return event.result;
        } else {
            if (!this.worldObj.isRemote) {
                if (this.isPlayerSleeping() || !this.isEntityAlive()) {
                    return net.minecraft.entity.player.EntityPlayer.EnumStatus.OTHER_PROBLEM;
                }

                if (!this.worldObj.provider.isSurfaceWorld()) {
                    return net.minecraft.entity.player.EntityPlayer.EnumStatus.NOT_POSSIBLE_HERE;
                }

                if (this.worldObj.isDaytime()) {
                    return net.minecraft.entity.player.EntityPlayer.EnumStatus.NOT_POSSIBLE_NOW;
                }

                if (Math.abs(this.posX - (double)x) > 3.0D || Math.abs(this.posY - (double)y) > 2.0D || Math.abs(this.posZ - (double)z) > 3.0D) {
                    return net.minecraft.entity.player.EntityPlayer.EnumStatus.TOO_FAR_AWAY;
                }

                double d0 = 8.0D;
                double d1 = 5.0D;
                List list = this.worldObj.getEntitiesWithinAABB(EntityMob.class, AxisAlignedBB.getBoundingBox((double)x - d0, (double)y - d1, (double)z - d0, (double)x + d0, (double)y + d1, (double)z + d0));
                if (!list.isEmpty()) {
                    return net.minecraft.entity.player.EntityPlayer.EnumStatus.NOT_SAFE;
                }
            }

            if (this.isRiding()) {
                this.mountEntity((Entity)null);
            }

            this.setSize(0.2F, 0.2F);
            this.yOffset = 0.2F;
            if (this.worldObj.blockExists(x, y, z)) {
                int l = this.worldObj.getBlock(x, y, z).getBedDirection(this.worldObj, x, y, z);
                float f1 = 0.5F;
                float f = 0.5F;
                switch(l) {
                case 0:
                    f = 0.9F;
                    break;
                case 1:
                    f1 = 0.1F;
                    break;
                case 2:
                    f = 0.1F;
                    break;
                case 3:
                    f1 = 0.9F;
                }

                this.func_71013_b(l);
                this.setPosition((double)((float)x + f1), (double)((float)y + 0.9375F), (double)((float)z + f));
            } else {
                this.setPosition((double)((float)x + 0.5F), (double)((float)y + 0.9375F), (double)((float)z + 0.5F));
            }

            this.sleeping = true;
            this.sleepTimer = 0;
            this.playerLocation = new ChunkCoordinates(x, y, z);
            this.motionX = this.motionZ = this.motionY = 0.0D;
            if (!this.worldObj.isRemote) {
                this.worldObj.updateAllPlayersSleepingFlag();
            }

            return net.minecraft.entity.player.EntityPlayer.EnumStatus.OK;
        }
    }

    private void func_71013_b(int p_71013_1_) {
        this.field_71079_bU = 0.0F;
        this.field_71089_bV = 0.0F;
        switch(p_71013_1_) {
        case 0:
            this.field_71089_bV = -1.8F;
            break;
        case 1:
            this.field_71079_bU = 1.8F;
            break;
        case 2:
            this.field_71089_bV = 1.8F;
            break;
        case 3:
            this.field_71079_bU = -1.8F;
        }

    }

    public void wakeUpPlayer(boolean p_70999_1_, boolean updateWorldFlag, boolean setSpawn) {
        MinecraftForge.EVENT_BUS.post(new PlayerWakeUpEvent(this, p_70999_1_, updateWorldFlag, setSpawn));
        this.setSize(0.6F, 1.8F);
        this.resetHeight();
        ChunkCoordinates chunkcoordinates = this.playerLocation;
        ChunkCoordinates chunkcoordinates1 = this.playerLocation;
        Block block = chunkcoordinates == null ? null : this.worldObj.getBlock(chunkcoordinates.posX, chunkcoordinates.posY, chunkcoordinates.posZ);
        if (chunkcoordinates != null && block.isBed(this.worldObj, chunkcoordinates.posX, chunkcoordinates.posY, chunkcoordinates.posZ, this)) {
            block.setBedOccupied(this.worldObj, chunkcoordinates.posX, chunkcoordinates.posY, chunkcoordinates.posZ, this, false);
            chunkcoordinates1 = block.getBedSpawnPosition(this.worldObj, chunkcoordinates.posX, chunkcoordinates.posY, chunkcoordinates.posZ, this);
            if (chunkcoordinates1 == null) {
                chunkcoordinates1 = new ChunkCoordinates(chunkcoordinates.posX, chunkcoordinates.posY + 1, chunkcoordinates.posZ);
            }

            this.setPosition((double)((float)chunkcoordinates1.posX + 0.5F), (double)((float)chunkcoordinates1.posY + this.yOffset + 0.1F), (double)((float)chunkcoordinates1.posZ + 0.5F));
        }

        this.sleeping = false;
        if (!this.worldObj.isRemote && updateWorldFlag) {
            this.worldObj.updateAllPlayersSleepingFlag();
        }

        if (p_70999_1_) {
            this.sleepTimer = 0;
        } else {
            this.sleepTimer = 100;
        }

        if (setSpawn) {
            this.setSpawnChunk(this.playerLocation, false);
        }

    }

    private boolean isInBed() {
        return this.worldObj.getBlock(this.playerLocation.posX, this.playerLocation.posY, this.playerLocation.posZ).isBed(this.worldObj, this.playerLocation.posX, this.playerLocation.posY, this.playerLocation.posZ, this);
    }

    public static ChunkCoordinates verifyRespawnCoordinates(World p_71056_0_, ChunkCoordinates p_71056_1_, boolean p_71056_2_) {
        IChunkProvider ichunkprovider = p_71056_0_.getChunkProvider();
        ichunkprovider.loadChunk(p_71056_1_.posX - 3 >> 4, p_71056_1_.posZ - 3 >> 4);
        ichunkprovider.loadChunk(p_71056_1_.posX + 3 >> 4, p_71056_1_.posZ - 3 >> 4);
        ichunkprovider.loadChunk(p_71056_1_.posX - 3 >> 4, p_71056_1_.posZ + 3 >> 4);
        ichunkprovider.loadChunk(p_71056_1_.posX + 3 >> 4, p_71056_1_.posZ + 3 >> 4);
        if (p_71056_0_.getBlock(p_71056_1_.posX, p_71056_1_.posY, p_71056_1_.posZ).isBed(p_71056_0_, p_71056_1_.posX, p_71056_1_.posY, p_71056_1_.posZ, (EntityLivingBase)null)) {
            ChunkCoordinates chunkcoordinates1 = p_71056_0_.getBlock(p_71056_1_.posX, p_71056_1_.posY, p_71056_1_.posZ).getBedSpawnPosition(p_71056_0_, p_71056_1_.posX, p_71056_1_.posY, p_71056_1_.posZ, (EntityPlayer)null);
            return chunkcoordinates1;
        } else {
            Material material = p_71056_0_.getBlock(p_71056_1_.posX, p_71056_1_.posY, p_71056_1_.posZ).getMaterial();
            Material material1 = p_71056_0_.getBlock(p_71056_1_.posX, p_71056_1_.posY + 1, p_71056_1_.posZ).getMaterial();
            boolean flag1 = !material.isSolid() && !material.isLiquid();
            boolean flag2 = !material1.isSolid() && !material1.isLiquid();
            return p_71056_2_ && flag1 && flag2 ? p_71056_1_ : null;
        }
    }

    @SideOnly(Side.CLIENT)
    public float getBedOrientationInDegrees() {
        if (this.playerLocation != null) {
            int x = this.playerLocation.posX;
            int y = this.playerLocation.posY;
            int z = this.playerLocation.posZ;
            int j = this.worldObj.getBlock(x, y, z).getBedDirection(this.worldObj, x, y, z);
            switch(j) {
            case 0:
                return 90.0F;
            case 1:
                return 0.0F;
            case 2:
                return 270.0F;
            case 3:
                return 180.0F;
            }
        }

        return 0.0F;
    }

    public boolean isPlayerSleeping() {
        return this.sleeping;
    }

    public boolean isPlayerFullyAsleep() {
        return this.sleeping && this.sleepTimer >= 100;
    }

    @SideOnly(Side.CLIENT)
    public int getSleepTimer() {
        return this.sleepTimer;
    }

    @SideOnly(Side.CLIENT)
    protected boolean getHideCape(int p_82241_1_) {
        return (this.dataWatcher.getWatchableObjectByte(16) & 1 << p_82241_1_) != 0;
    }

    protected void setHideCape(int p_82239_1_, boolean p_82239_2_) {
        byte b0 = this.dataWatcher.getWatchableObjectByte(16);
        if (p_82239_2_) {
            this.dataWatcher.updateObject(16, (byte)(b0 | 1 << p_82239_1_));
        } else {
            this.dataWatcher.updateObject(16, (byte)(b0 & ~(1 << p_82239_1_)));
        }

    }

    public void addChatComponentMessage(IChatComponent p_146105_1_) {
    }

    /** @deprecated */
    @Deprecated
    public ChunkCoordinates getBedLocation() {
        return this.getBedLocation(this.dimension);
    }

    /** @deprecated */
    @Deprecated
    public boolean isSpawnForced() {
        return this.isSpawnForced(this.dimension);
    }

    public void setSpawnChunk(ChunkCoordinates p_71063_1_, boolean p_71063_2_) {
        if (this.dimension != 0) {
            this.setSpawnChunk(p_71063_1_, p_71063_2_, this.dimension);
        } else {
            if (p_71063_1_ != null) {
                this.spawnChunk = new ChunkCoordinates(p_71063_1_);
                this.spawnForced = p_71063_2_;
            } else {
                this.spawnChunk = null;
                this.spawnForced = false;
            }

        }
    }

    public void triggerAchievement(StatBase p_71029_1_) {
        this.addStat(p_71029_1_, 1);
    }

    public void addStat(StatBase p_71064_1_, int p_71064_2_) {
    }

    public void jump() {
        super.jump();
        this.addStat(StatList.jumpStat, 1);
        if (this.isSprinting()) {
            this.addExhaustion(0.8F);
        } else {
            this.addExhaustion(0.2F);
        }

    }

    public void moveEntityWithHeading(float p_70612_1_, float p_70612_2_) {
        double d0 = this.posX;
        double d1 = this.posY;
        double d2 = this.posZ;
        if (this.capabilities.isFlying && this.ridingEntity == null) {
            double d3 = this.motionY;
            float f2 = this.jumpMovementFactor;
            this.jumpMovementFactor = this.capabilities.getFlySpeed();
            super.moveEntityWithHeading(p_70612_1_, p_70612_2_);
            this.motionY = d3 * 0.6D;
            this.jumpMovementFactor = f2;
        } else {
            super.moveEntityWithHeading(p_70612_1_, p_70612_2_);
        }

        this.addMovementStat(this.posX - d0, this.posY - d1, this.posZ - d2);
    }

    public float getAIMoveSpeed() {
        return (float)this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue();
    }

    public void addMovementStat(double p_71000_1_, double p_71000_3_, double p_71000_5_) {
        if (this.ridingEntity == null) {
            int i;
            if (this.isInsideOfMaterial(Material.water)) {
                i = Math.round(MathHelper.sqrt_double(p_71000_1_ * p_71000_1_ + p_71000_3_ * p_71000_3_ + p_71000_5_ * p_71000_5_) * 100.0F);
                if (i > 0) {
                    this.addStat(StatList.distanceDoveStat, i);
                    this.addExhaustion(0.015F * (float)i * 0.01F);
                }
            } else if (this.isInWater()) {
                i = Math.round(MathHelper.sqrt_double(p_71000_1_ * p_71000_1_ + p_71000_5_ * p_71000_5_) * 100.0F);
                if (i > 0) {
                    this.addStat(StatList.distanceSwumStat, i);
                    this.addExhaustion(0.015F * (float)i * 0.01F);
                }
            } else if (this.isOnLadder()) {
                if (p_71000_3_ > 0.0D) {
                    this.addStat(StatList.distanceClimbedStat, (int)Math.round(p_71000_3_ * 100.0D));
                }
            } else if (this.onGround) {
                i = Math.round(MathHelper.sqrt_double(p_71000_1_ * p_71000_1_ + p_71000_5_ * p_71000_5_) * 100.0F);
                if (i > 0) {
                    this.addStat(StatList.distanceWalkedStat, i);
                    if (this.isSprinting()) {
                        this.addExhaustion(0.099999994F * (float)i * 0.01F);
                    } else {
                        this.addExhaustion(0.01F * (float)i * 0.01F);
                    }
                }
            } else {
                i = Math.round(MathHelper.sqrt_double(p_71000_1_ * p_71000_1_ + p_71000_5_ * p_71000_5_) * 100.0F);
                if (i > 25) {
                    this.addStat(StatList.distanceFlownStat, i);
                }
            }
        }

    }

    private void addMountedMovementStat(double p_71015_1_, double p_71015_3_, double p_71015_5_) {
        if (this.ridingEntity != null) {
            int i = Math.round(MathHelper.sqrt_double(p_71015_1_ * p_71015_1_ + p_71015_3_ * p_71015_3_ + p_71015_5_ * p_71015_5_) * 100.0F);
            if (i > 0) {
                if (this.ridingEntity instanceof EntityMinecart) {
                    this.addStat(StatList.distanceByMinecartStat, i);
                    if (this.startMinecartRidingCoordinate == null) {
                        this.startMinecartRidingCoordinate = new ChunkCoordinates(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ));
                    } else if ((double)this.startMinecartRidingCoordinate.getDistanceSquared(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ)) >= 1000000.0D) {
                        this.addStat(AchievementList.onARail, 1);
                    }
                } else if (this.ridingEntity instanceof EntityBoat) {
                    this.addStat(StatList.distanceByBoatStat, i);
                } else if (this.ridingEntity instanceof EntityPig) {
                    this.addStat(StatList.distanceByPigStat, i);
                } else if (this.ridingEntity instanceof EntityHorse) {
                    this.addStat(StatList.field_151185_q, i);
                }
            }
        }

    }

    protected void fall(float distance) {
        if (!this.capabilities.allowFlying) {
            if (distance >= 2.0F) {
                this.addStat(StatList.distanceFallenStat, (int)Math.round((double)distance * 100.0D));
            }

            super.fall(distance);
        } else {
            MinecraftForge.EVENT_BUS.post(new PlayerFlyableFallEvent(this, distance));
        }

    }

    protected String func_146067_o(int p_146067_1_) {
        return p_146067_1_ > 4 ? "game.player.hurt.fall.big" : "game.player.hurt.fall.small";
    }

    public void onKillEntity(EntityLivingBase entityLivingIn) {
        if (entityLivingIn instanceof IMob) {
            this.triggerAchievement(AchievementList.killEnemy);
        }

        int i = EntityList.getEntityID(entityLivingIn);
        EntityEggInfo entityegginfo = (EntityEggInfo)EntityList.entityEggs.get(i);
        if (entityegginfo != null) {
            this.addStat(entityegginfo.field_151512_d, 1);
        }

    }

    public void setInWeb() {
        if (!this.capabilities.isFlying) {
            super.setInWeb();
        }

    }

    @SideOnly(Side.CLIENT)
    public IIcon getItemIcon(ItemStack itemStackIn, int p_70620_2_) {
        super.getItemIcon(itemStackIn, p_70620_2_);
        IIcon iicon;
        if (itemStackIn.getItem() == Items.fishing_rod && this.fishEntity != null) {
            iicon = Items.fishing_rod.func_94597_g();
        } else {
            if (this.itemInUse != null && itemStackIn.getItem() == Items.bow) {
                int j = itemStackIn.getMaxItemUseDuration() - this.itemInUseCount;
                if (j >= 18) {
                    return Items.bow.getItemIconForUseDuration(2);
                }

                if (j > 13) {
                    return Items.bow.getItemIconForUseDuration(1);
                }

                if (j > 0) {
                    return Items.bow.getItemIconForUseDuration(0);
                }
            }

            iicon = itemStackIn.getItem().getIcon(itemStackIn, p_70620_2_, this, this.itemInUse, this.itemInUseCount);
        }

        return iicon;
    }

    public ItemStack getCurrentArmor(int p_82169_1_) {
        return this.inventory.armorItemInSlot(p_82169_1_);
    }

    public void addExperience(int p_71023_1_) {
        this.addScore(p_71023_1_);
        int j = Integer.MAX_VALUE - this.experienceTotal;
        if (p_71023_1_ > j) {
            p_71023_1_ = j;
        }

        this.experience += (float)p_71023_1_ / (float)this.xpBarCap();

        for(this.experienceTotal += p_71023_1_; this.experience >= 1.0F; this.experience /= (float)this.xpBarCap()) {
            this.experience = (this.experience - 1.0F) * (float)this.xpBarCap();
            this.addExperienceLevel(1);
        }

    }

    public void addExperienceLevel(int p_82242_1_) {
        this.experienceLevel += p_82242_1_;
        if (this.experienceLevel < 0) {
            this.experienceLevel = 0;
            this.experience = 0.0F;
            this.experienceTotal = 0;
        }

        if (p_82242_1_ > 0 && this.experienceLevel % 5 == 0 && (float)this.field_82249_h < (float)this.ticksExisted - 100.0F) {
            float f = this.experienceLevel > 30 ? 1.0F : (float)this.experienceLevel / 30.0F;
            this.worldObj.playSoundAtEntity(this, "random.levelup", f * 0.75F, 1.0F);
            this.field_82249_h = this.ticksExisted;
        }

    }

    public int xpBarCap() {
        return this.experienceLevel >= 30 ? 62 + (this.experienceLevel - 30) * 7 : (this.experienceLevel >= 15 ? 17 + (this.experienceLevel - 15) * 3 : 17);
    }

    public void addExhaustion(float p_71020_1_) {
        if (!this.capabilities.disableDamage && !this.worldObj.isRemote) {
            FoodStats injectorAllocatedLocal2 = this.foodStats;
            if (this.wrapWithCondition$zja000$hodgepodge$hungerRule(injectorAllocatedLocal2, p_71020_1_)) {
                injectorAllocatedLocal2.addExhaustion(p_71020_1_);
            }
        }

    }

    public FoodStats getFoodStats() {
        return this.foodStats;
    }

    public boolean canEat(boolean p_71043_1_) {
        return (p_71043_1_ || this.foodStats.needFood()) && !this.capabilities.disableDamage;
    }

    public boolean shouldHeal() {
        return this.getHealth() > 0.0F && this.getHealth() < this.getMaxHealth();
    }

    public void setItemInUse(ItemStack p_71008_1_, int p_71008_2_) {
        if (p_71008_1_ != this.itemInUse) {
            p_71008_2_ = ForgeEventFactory.onItemUseStart(this, p_71008_1_, p_71008_2_);
            if (p_71008_2_ <= 0) {
                return;
            }

            this.itemInUse = p_71008_1_;
            this.itemInUseCount = p_71008_2_;
            if (!this.worldObj.isRemote) {
                this.setEating(true);
            }
        }

    }

    public boolean isCurrentToolAdventureModeExempt(int p_82246_1_, int p_82246_2_, int p_82246_3_) {
        if (this.capabilities.allowEdit) {
            return true;
        } else {
            Block block = this.worldObj.getBlock(p_82246_1_, p_82246_2_, p_82246_3_);
            if (block.getMaterial() != Material.air) {
                if (block.getMaterial().isAdventureModeExempt()) {
                    return true;
                }

                if (this.getCurrentEquippedItem() != null) {
                    ItemStack itemstack = this.getCurrentEquippedItem();
                    if (itemstack.func_150998_b(block) || itemstack.func_150997_a(block) > 1.0F) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public boolean canPlayerEdit(int p_82247_1_, int p_82247_2_, int p_82247_3_, int p_82247_4_, ItemStack p_82247_5_) {
        return this.capabilities.allowEdit ? true : (p_82247_5_ != null ? p_82247_5_.canEditBlocks() : false);
    }

    protected int getExperiencePoints(EntityPlayer p_70693_1_) {
        if (this.worldObj.getGameRules().getGameRuleBooleanValue("keepInventory")) {
            return 0;
        } else {
            int i = this.experienceLevel * 7;
            return i > 100 ? 100 : i;
        }
    }

    protected boolean isPlayer() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public boolean getAlwaysRenderNameTagForRender() {
        return true;
    }

    public void clonePlayer(EntityPlayer p_71049_1_, boolean p_71049_2_) {
        if (p_71049_2_) {
            this.inventory.copyInventory(p_71049_1_.inventory);
            this.setHealth(p_71049_1_.getHealth());
            this.foodStats = p_71049_1_.foodStats;
            this.experienceLevel = p_71049_1_.experienceLevel;
            this.experienceTotal = p_71049_1_.experienceTotal;
            this.experience = p_71049_1_.experience;
            this.setScore(p_71049_1_.getScore());
            this.teleportDirection = p_71049_1_.teleportDirection;
            this.extendedProperties = p_71049_1_.extendedProperties;
            Iterator var3 = this.extendedProperties.values().iterator();

            while(var3.hasNext()) {
                IExtendedEntityProperties p = (IExtendedEntityProperties)var3.next();
                p.init(this, this.worldObj);
            }
        } else if (this.worldObj.getGameRules().getGameRuleBooleanValue("keepInventory")) {
            this.inventory.copyInventory(p_71049_1_.inventory);
            this.experienceLevel = p_71049_1_.experienceLevel;
            this.experienceTotal = p_71049_1_.experienceTotal;
            this.experience = p_71049_1_.experience;
            this.setScore(p_71049_1_.getScore());
        }

        this.theInventoryEnderChest = p_71049_1_.theInventoryEnderChest;
        this.spawnChunkMap = p_71049_1_.spawnChunkMap;
        this.spawnForcedMap = p_71049_1_.spawnForcedMap;
        NBTTagCompound old = p_71049_1_.getEntityData();
        if (old.hasKey("PlayerPersisted")) {
            this.getEntityData().setTag("PlayerPersisted", old.getCompoundTag("PlayerPersisted"));
        }

        MinecraftForge.EVENT_BUS.post(new Clone(this, p_71049_1_, !p_71049_2_));
    }

    protected boolean canTriggerWalking() {
        return !this.capabilities.isFlying;
    }

    public void sendPlayerAbilities() {
    }

    public void setGameType(GameType gameType) {
    }

    public String getCommandSenderName() {
        return this.field_146106_i.getName();
    }

    public World getEntityWorld() {
        return this.worldObj;
    }

    public InventoryEnderChest getInventoryEnderChest() {
        return this.theInventoryEnderChest;
    }

    public ItemStack getEquipmentInSlot(int p_71124_1_) {
        return p_71124_1_ == 0 ? this.inventory.getCurrentItem() : this.inventory.armorInventory[p_71124_1_ - 1];
    }

    public ItemStack getHeldItem() {
        return this.inventory.getCurrentItem();
    }

    public void setCurrentItemOrArmor(int slotIn, ItemStack itemStackIn) {
        if (slotIn == 0) {
            this.inventory.mainInventory[this.inventory.currentItem] = itemStackIn;
        } else {
            this.inventory.armorInventory[slotIn - 1] = itemStackIn;
        }

    }

    @SideOnly(Side.CLIENT)
    public boolean isInvisibleToPlayer(EntityPlayer player) {
        if (!this.isInvisible()) {
            return false;
        } else {
            Team team = this.getTeam();
            return team == null || player == null || player.getTeam() != team || !team.func_98297_h();
        }
    }

    public ItemStack[] getLastActiveItems() {
        return this.inventory.armorInventory;
    }

    @SideOnly(Side.CLIENT)
    public boolean getHideCape() {
        return this.getHideCape(1);
    }

    public boolean isPushedByWater() {
        return !this.capabilities.isFlying;
    }

    public Scoreboard getWorldScoreboard() {
        return this.worldObj.getScoreboard();
    }

    public Team getTeam() {
        return this.getWorldScoreboard().getPlayersTeam(this.getCommandSenderName());
    }

    public IChatComponent func_145748_c_() {
        ChatComponentText chatcomponenttext = new ChatComponentText(ScorePlayerTeam.formatPlayerName(this.getTeam(), this.getDisplayName()));
        chatcomponenttext.getChatStyle().setChatClickEvent(new ClickEvent(Action.SUGGEST_COMMAND, "/msg " + this.getCommandSenderName() + " "));
        return chatcomponenttext;
    }

    public void setAbsorptionAmount(float p_110149_1_) {
        if (p_110149_1_ < 0.0F) {
            p_110149_1_ = 0.0F;
        }

        this.getDataWatcher().updateObject(17, p_110149_1_);
    }

    public float getAbsorptionAmount() {
        return this.getDataWatcher().getWatchableObjectFloat(17);
    }

    public static UUID func_146094_a(GameProfile p_146094_0_) {
        UUID uuid = p_146094_0_.getId();
        if (uuid == null) {
            uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + p_146094_0_.getName()).getBytes(Charsets.UTF_8));
        }

        return uuid;
    }

    public void openGui(Object mod, int modGuiId, World world, int x, int y, int z) {
        FMLNetworkHandler.openGui(this, mod, modGuiId, world, x, y, z);
    }

    @SideOnly(Side.CLIENT)
    public Vec3 getPosition(float par1) {
        if (par1 == 1.0F) {
            return Vec3.createVectorHelper(this.posX, this.posY + (double)(this.getEyeHeight() - this.getDefaultEyeHeight()), this.posZ);
        } else {
            double d0 = this.prevPosX + (this.posX - this.prevPosX) * (double)par1;
            double d1 = this.prevPosY + (this.posY - this.prevPosY) * (double)par1 + (double)(this.getEyeHeight() - this.getDefaultEyeHeight());
            double d2 = this.prevPosZ + (this.posZ - this.prevPosZ) * (double)par1;
            return Vec3.createVectorHelper(d0, d1, d2);
        }
    }

    public ChunkCoordinates getBedLocation(int dimension) {
        return dimension == 0 ? this.spawnChunk : (ChunkCoordinates)this.spawnChunkMap.get(dimension);
    }

    public boolean isSpawnForced(int dimension) {
        if (dimension == 0) {
            return this.spawnForced;
        } else {
            Boolean forced = (Boolean)this.spawnForcedMap.get(dimension);
            return forced == null ? false : forced;
        }
    }

    public void setSpawnChunk(ChunkCoordinates chunkCoordinates, boolean forced, int dimension) {
        if (dimension == 0) {
            if (chunkCoordinates != null) {
                this.spawnChunk = new ChunkCoordinates(chunkCoordinates);
                this.spawnForced = forced;
            } else {
                this.spawnChunk = null;
                this.spawnForced = false;
            }

        } else {
            if (chunkCoordinates != null) {
                this.spawnChunkMap.put(dimension, new ChunkCoordinates(chunkCoordinates));
                this.spawnForcedMap.put(dimension, forced);
            } else {
                this.spawnChunkMap.remove(dimension);
                this.spawnForcedMap.remove(dimension);
            }

        }
    }

    public float getDefaultEyeHeight() {
        return 0.12F;
    }

    public String getDisplayName() {
        if (this.displayname == null) {
            this.displayname = ForgeEventFactory.getPlayerDisplayName(this, this.getCommandSenderName());
        }

        return this.displayname;
    }

    public void refreshDisplayName() {
        this.displayname = ForgeEventFactory.getPlayerDisplayName(this, this.getCommandSenderName());
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinEntityPlayer_ThrottlePickup",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private void handler$zca000$hodgepodge$resetItemCounter(CallbackInfo ci, LocalIntRef itemEntityCounter) {
        itemEntityCounter.set(0);
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinEntityPlayer_ThrottlePickup",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private void redirect$zca000$hodgepodge$throttleItemPickupEvent(EntityPlayer instance, Entity entity, LocalIntRef itemEntityCounter) {
        if (entity instanceof EntityItem) {
            if (itemEntityCounter.get() < FixesConfig.itemStacksPickedUpPerTick) {
                this.collideWithPlayer(entity);
            }

            itemEntityCounter.set(itemEntityCounter.get() + 1);
        } else {
            this.collideWithPlayer(entity);
        }
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinEntityPlayer_ItemThrower",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private boolean modify$zcb000$hodgepodge$passThrower(boolean original, boolean addThrowerTag) {
        return addThrowerTag;
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinEntityPlayer_HungerRule",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private boolean wrapWithCondition$zja000$hodgepodge$hungerRule(FoodStats foodStats, float amount) {
        return !((GameRuleExt)this.worldObj.getGameRules()).hodgepodge$isHungerDisabled();
    }
}
