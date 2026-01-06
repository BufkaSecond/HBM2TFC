package com.llamalad7.mixinextras.sugar.impl.ref.generated;

import com.llamalad7.mixinextras.sugar.impl.ref.LocalRefRuntime;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;

public final class LocalIntRefImpl implements LocalIntRef {
    private int value;
    private byte state = 1;

    public int get() {
        if (this.state != 0) {
            LocalRefRuntime.checkState(this.state);
        }

        return this.value;
    }

    public void set(int var1) {
        if (this.state != 0) {
            LocalRefRuntime.checkState(this.state);
        }

        this.value = var1;
    }

    public void init(int var1) {
        this.value = var1;
        this.state = 0;
    }

    public int dispose() {
        if (this.state != 0) {
            LocalRefRuntime.checkState(this.state);
        }

        this.state = 2;
        return this.value;
    }
}
