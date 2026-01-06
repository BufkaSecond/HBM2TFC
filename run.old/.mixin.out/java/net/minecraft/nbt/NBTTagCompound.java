package net.minecraft.nbt;

import com.mitchej123.hodgepodge.util.StringPooler;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.FastEntrySet;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.nbt.NBTBase.NBTPrimitive;
import net.minecraft.nbt.NBTTagCompound.1;
import net.minecraft.nbt.NBTTagCompound.2;
import net.minecraft.util.ReportedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

public class NBTTagCompound extends NBTBase {
    private static final Logger logger = LogManager.getLogger();
    private Map tagMap = new Object2ObjectOpenHashMap();
    private static final String __OBFID = "CL_00001215";

    void write(DataOutput output) throws IOException {
        Iterator iterator = this.tagMap.keySet().iterator();

        while(iterator.hasNext()) {
            String s = (String)iterator.next();
            NBTBase nbtbase = (NBTBase)this.tagMap.get(s);
            func_150298_a(s, nbtbase, output);
        }

        output.writeByte(0);
    }

    void func_152446_a(DataInput input, int depth, NBTSizeTracker sizeTracker) throws IOException {
        if (depth > 512) {
            throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
        } else {
            this.tagMap.clear();

            byte b0;
            while((b0 = func_152447_a(input, sizeTracker)) != 0) {
                String s = func_152448_b(input, sizeTracker);
                NBTSizeTracker.readUTF(sizeTracker, s);
                NBTBase nbtbase = func_152449_a(b0, s, input, depth + 1, sizeTracker);
                this.tagMap.put(s, nbtbase);
            }

        }
    }

    public Set func_150296_c() {
        return this.tagMap.keySet();
    }

    public byte getId() {
        return 10;
    }

    public void setTag(String key, NBTBase value) {
        this.tagMap.put(key, value);
    }

    public void setByte(String key, byte value) {
        this.tagMap.put(key, new NBTTagByte(value));
    }

    public void setShort(String key, short value) {
        this.tagMap.put(key, new NBTTagShort(value));
    }

    public void setInteger(String key, int value) {
        this.tagMap.put(key, new NBTTagInt(value));
    }

    public void setLong(String key, long value) {
        this.tagMap.put(key, new NBTTagLong(value));
    }

    public void setFloat(String key, float value) {
        this.tagMap.put(key, new NBTTagFloat(value));
    }

    public void setDouble(String key, double value) {
        this.tagMap.put(key, new NBTTagDouble(value));
    }

    public void setString(String key, String value) {
        this.tagMap.put(key, new NBTTagString(value));
    }

    public void setByteArray(String key, byte[] value) {
        this.tagMap.put(key, new NBTTagByteArray(value));
    }

    public void setIntArray(String key, int[] value) {
        this.tagMap.put(key, new NBTTagIntArray(value));
    }

    public void setBoolean(String key, boolean value) {
        this.setByte(key, (byte)(value ? 1 : 0));
    }

    public NBTBase getTag(String key) {
        return (NBTBase)this.tagMap.get(key);
    }

    public byte func_150299_b(String key) {
        NBTBase nbtbase = (NBTBase)this.tagMap.get(key);
        return nbtbase != null ? nbtbase.getId() : 0;
    }

    public boolean hasKey(String key) {
        return this.tagMap.containsKey(key);
    }

    public boolean hasKey(String key, int type) {
        byte b0 = this.func_150299_b(key);
        return b0 == type ? true : (type != 99 ? false : b0 == 1 || b0 == 2 || b0 == 3 || b0 == 4 || b0 == 5 || b0 == 6);
    }

    public byte getByte(String key) {
        try {
            return !this.tagMap.containsKey(key) ? 0 : ((NBTPrimitive)this.tagMap.get(key)).func_150290_f();
        } catch (ClassCastException var3) {
            return 0;
        }
    }

    public short getShort(String key) {
        try {
            return !this.tagMap.containsKey(key) ? 0 : ((NBTPrimitive)this.tagMap.get(key)).func_150289_e();
        } catch (ClassCastException var3) {
            return 0;
        }
    }

    public int getInteger(String key) {
        try {
            return !this.tagMap.containsKey(key) ? 0 : ((NBTPrimitive)this.tagMap.get(key)).func_150287_d();
        } catch (ClassCastException var3) {
            return 0;
        }
    }

    public long getLong(String key) {
        try {
            return !this.tagMap.containsKey(key) ? 0L : ((NBTPrimitive)this.tagMap.get(key)).func_150291_c();
        } catch (ClassCastException var3) {
            return 0L;
        }
    }

    public float getFloat(String key) {
        try {
            return !this.tagMap.containsKey(key) ? 0.0F : ((NBTPrimitive)this.tagMap.get(key)).func_150288_h();
        } catch (ClassCastException var3) {
            return 0.0F;
        }
    }

    public double getDouble(String key) {
        try {
            return !this.tagMap.containsKey(key) ? 0.0D : ((NBTPrimitive)this.tagMap.get(key)).func_150286_g();
        } catch (ClassCastException var3) {
            return 0.0D;
        }
    }

    public String getString(String key) {
        try {
            return !this.tagMap.containsKey(key) ? "" : ((NBTBase)this.tagMap.get(key)).func_150285_a_();
        } catch (ClassCastException var3) {
            return "";
        }
    }

    public byte[] getByteArray(String key) {
        try {
            return !this.tagMap.containsKey(key) ? new byte[0] : ((NBTTagByteArray)this.tagMap.get(key)).func_150292_c();
        } catch (ClassCastException var3) {
            throw new ReportedException(this.createCrashReport(key, 7, var3));
        }
    }

    public int[] getIntArray(String key) {
        try {
            return !this.tagMap.containsKey(key) ? new int[0] : ((NBTTagIntArray)this.tagMap.get(key)).func_150302_c();
        } catch (ClassCastException var3) {
            throw new ReportedException(this.createCrashReport(key, 11, var3));
        }
    }

    public NBTTagCompound getCompoundTag(String key) {
        try {
            return !this.tagMap.containsKey(key) ? new NBTTagCompound() : (NBTTagCompound)this.tagMap.get(key);
        } catch (ClassCastException var3) {
            throw new ReportedException(this.createCrashReport(key, 10, var3));
        }
    }

    public NBTTagList getTagList(String key, int type) {
        try {
            if (this.func_150299_b(key) != 9) {
                return new NBTTagList();
            } else {
                NBTTagList nbttaglist = (NBTTagList)this.tagMap.get(key);
                return nbttaglist.tagCount() > 0 && nbttaglist.func_150303_d() != type ? new NBTTagList() : nbttaglist;
            }
        } catch (ClassCastException var4) {
            throw new ReportedException(this.createCrashReport(key, 9, var4));
        }
    }

    public boolean getBoolean(String key) {
        return this.getByte(key) != 0;
    }

    public void removeTag(String key) {
        this.tagMap.remove(key);
    }

    public String toString() {
        String s = "{";

        String s1;
        for(Iterator iterator = this.tagMap.keySet().iterator(); iterator.hasNext(); s = s + s1 + ':' + this.tagMap.get(s1) + ',') {
            s1 = (String)iterator.next();
        }

        return s + "}";
    }

    public boolean hasNoTags() {
        return this.tagMap.isEmpty();
    }

    private CrashReport createCrashReport(String p_82581_1_, int p_82581_2_, ClassCastException p_82581_3_) {
        CrashReport crashreport = CrashReport.makeCrashReport(p_82581_3_, "Reading NBT data");
        CrashReportCategory crashreportcategory = crashreport.makeCategoryDepth("Corrupt NBT tag", 1);
        crashreportcategory.addCrashSectionCallable("Tag type found", new 1(this, p_82581_1_));
        crashreportcategory.addCrashSectionCallable("Tag type expected", new 2(this, p_82581_2_));
        crashreportcategory.addCrashSection("Tag name", p_82581_1_);
        return crashreport;
    }

    public boolean equals(Object p_equals_1_) {
        if (super.equals(p_equals_1_)) {
            NBTTagCompound nbttagcompound = (NBTTagCompound)p_equals_1_;
            return this.tagMap.entrySet().equals(nbttagcompound.tagMap.entrySet());
        } else {
            return false;
        }
    }

    public int hashCode() {
        return super.hashCode() ^ this.tagMap.hashCode();
    }

    private static void func_150298_a(String name, NBTBase data, DataOutput output) throws IOException {
        output.writeByte(data.getId());
        if (data.getId() != 0) {
            output.writeUTF(name);
            data.write(output);
        }

    }

    private static byte func_152447_a(DataInput input, NBTSizeTracker sizeTracker) throws IOException {
        sizeTracker.func_152450_a(8L);
        return input.readByte();
    }

    private static String func_152448_b(DataInput input, NBTSizeTracker sizeTracker) throws IOException {
        return modifyExpressionValue$zfd000$hodgepodge$poolString(input.readUTF());
    }

    static NBTBase func_152449_a(byte id, String key, DataInput input, int depth, NBTSizeTracker sizeTracker) {
        sizeTracker.func_152450_a(32L);
        NBTBase nbtbase = NBTBase.func_150284_a(id);

        try {
            nbtbase.func_152446_a(input, depth, sizeTracker);
            return nbtbase;
        } catch (IOException var9) {
            CrashReport crashreport = CrashReport.makeCrashReport(var9, "Loading NBT data");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("NBT Tag");
            crashreportcategory.addCrashSection("Tag name", key);
            crashreportcategory.addCrashSection("Tag type", id);
            throw new ReportedException(crashreport);
        }
    }

    // $FF: synthetic method
    static Map access$000(NBTTagCompound x0) {
        return x0.tagMap;
    }

    @Overwrite
    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinNBTTagCompound_speedup",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    public NBTBase copy() {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        FastEntrySet entries = ((Object2ObjectOpenHashMap)this.tagMap).object2ObjectEntrySet();
        ObjectIterator fastIterator = entries.fastIterator();

        while(fastIterator.hasNext()) {
            Entry entry = (Entry)fastIterator.next();
            nbttagcompound.setTag((String)entry.getKey(), ((NBTBase)entry.getValue()).copy());
        }

        return nbttagcompound;
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinNBTTagCompound_stringPooler",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private static String modifyExpressionValue$zfd000$hodgepodge$poolString(String s) {
        return StringPooler.INSTANCE.getString(s);
    }
}
