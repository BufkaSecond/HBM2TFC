package com.llamalad7.mixinextras.sugar.impl.ref.generated;

import com.llamalad7.mixinextras.sugar.impl.ref.LocalRefRuntime;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;

public final class LocalBooleanRefImpl implements LocalBooleanRef {
    private boolean value;
    private byte state = 1;

    public boolean get() {
        if (this.state != 0) {
            LocalRefRuntime.checkState(this.state);
        }

        return this.value;
    }

    public void set(boolean var1) {
        if (this.state != 0) {
            LocalRefRuntime.checkState(this.state);
        }

        this.value = var1;
    }

    public void init(boolean var1) {
        this.value = var1;
        this.state = 0;
    }

    public boolean dispose() {
        if (this.state != 0) {
            LocalRefRuntime.checkState(this.state);
        }

        this.state = 2;
        return this.value;
    }
}
