package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

public class NBTTagList extends NBTBase {
    public List tagList = new ArrayList();
    public byte tagType = 0;
    public static final String __OBFID = "CL_00001224";

    void write(DataOutput output) throws IOException {
        if (!this.tagList.isEmpty()) {
            this.tagType = ((NBTBase)this.tagList.get(0)).getId();
        } else {
            this.tagType = 0;
        }

        output.writeByte(this.tagType);
        output.writeInt(this.tagList.size());

        for(int i = 0; i < this.tagList.size(); ++i) {
            ((NBTBase)this.tagList.get(i)).write(output);
        }

    }

    void func_152446_a(DataInput input, int depth, NBTSizeTracker sizeTracker) throws IOException {
        if (depth > 512) {
            throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
        } else {
            sizeTracker.func_152450_a(8L);
            this.tagType = input.readByte();
            sizeTracker.func_152450_a(32L);
            int j = input.readInt();
            this.tagList = new ArrayList();

            for(int k = 0; k < j; ++k) {
                sizeTracker.func_152450_a(32L);
                NBTBase nbtbase = NBTBase.func_150284_a(this.tagType);
                nbtbase.func_152446_a(input, depth + 1, sizeTracker);
                this.tagList.add(nbtbase);
            }

        }
    }

    public byte getId() {
        return 9;
    }

    public String toString() {
        String s = "[";
        int i = 0;

        for(Iterator iterator = this.tagList.iterator(); iterator.hasNext(); ++i) {
            NBTBase nbtbase = (NBTBase)iterator.next();
            s = s + "" + i + ':' + nbtbase + ',';
        }

        return s + "]";
    }

    public void appendTag(NBTBase p_74742_1_) {
        if (this.tagType == 0) {
            this.tagType = p_74742_1_.getId();
        } else if (this.tagType != p_74742_1_.getId()) {
            System.err.println("WARNING: Adding mismatching tag types to tag list");
            return;
        }

        this.tagList.add(p_74742_1_);
    }

    public void func_150304_a(int i, NBTBase p_150304_2_) {
        if (i >= 0 && i < this.tagList.size()) {
            if (this.tagType == 0) {
                this.tagType = p_150304_2_.getId();
            } else if (this.tagType != p_150304_2_.getId()) {
                System.err.println("WARNING: Adding mismatching tag types to tag list");
                return;
            }

            this.tagList.set(i, p_150304_2_);
        } else {
            System.err.println("WARNING: index out of bounds to set tag in tag list");
        }

    }

    public NBTBase removeTag(int i) {
        return (NBTBase)this.tagList.remove(i);
    }

    public NBTTagCompound getCompoundTagAt(int i) {
        if (i >= 0 && i < this.tagList.size()) {
            NBTBase nbtbase = (NBTBase)this.tagList.get(i);
            return nbtbase.getId() == 10 ? (NBTTagCompound)nbtbase : new NBTTagCompound();
        } else {
            return new NBTTagCompound();
        }
    }

    public int[] func_150306_c(int i) {
        if (i >= 0 && i < this.tagList.size()) {
            NBTBase nbtbase = (NBTBase)this.tagList.get(i);
            return nbtbase.getId() == 11 ? ((NBTTagIntArray)nbtbase).func_150302_c() : new int[0];
        } else {
            return new int[0];
        }
    }

    public double func_150309_d(int i) {
        if (i >= 0 && i < this.tagList.size()) {
            NBTBase nbtbase = (NBTBase)this.tagList.get(i);
            return nbtbase.getId() == 6 ? ((NBTTagDouble)nbtbase).func_150286_g() : 0.0D;
        } else {
            return 0.0D;
        }
    }

    public float func_150308_e(int i) {
        if (i >= 0 && i < this.tagList.size()) {
            NBTBase nbtbase = (NBTBase)this.tagList.get(i);
            return nbtbase.getId() == 5 ? ((NBTTagFloat)nbtbase).func_150288_h() : 0.0F;
        } else {
            return 0.0F;
        }
    }

    public String getStringTagAt(int i) {
        if (i >= 0 && i < this.tagList.size()) {
            NBTBase nbtbase = (NBTBase)this.tagList.get(i);
            return nbtbase.getId() == 8 ? nbtbase.func_150285_a_() : nbtbase.toString();
        } else {
            return "";
        }
    }

    public int tagCount() {
        return this.tagList.size();
    }

    public NBTBase copy() {
        NBTTagList nbttaglist = new NBTTagList();
        nbttaglist.tagType = this.tagType;
        Iterator var10000 = this.tagList.iterator();
        this.handler$zfc000$hodgepodge$copyEnsureCapacity((CallbackInfoReturnable)null, nbttaglist);
        Iterator iterator = var10000;

        while(iterator.hasNext()) {
            NBTBase nbtbase = (NBTBase)iterator.next();
            NBTBase nbtbase1 = nbtbase.copy();
            nbttaglist.tagList.add(nbtbase1);
        }

        return nbttaglist;
    }

    public boolean equals(Object p_equals_1_) {
        if (super.equals(p_equals_1_)) {
            NBTTagList nbttaglist = (NBTTagList)p_equals_1_;
            if (this.tagType == nbttaglist.tagType) {
                return this.tagList.equals(nbttaglist.tagList);
            }
        }

        return false;
    }

    public int hashCode() {
        return super.hashCode() ^ this.tagList.hashCode();
    }

    public int func_150303_d() {
        return this.tagType;
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.minecraft.MixinNBTTagList_speedup",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    public void handler$zfc000$hodgepodge$copyEnsureCapacity(CallbackInfoReturnable<NBTBase> cir, NBTTagList newTagList) {
        List var4 = ((NBTTagList)newTagList).tagList;
        if (var4 instanceof ArrayList) {
            ArrayList list = (ArrayList)var4;
            list.ensureCapacity(this.tagList.size());
        }

    }
}
