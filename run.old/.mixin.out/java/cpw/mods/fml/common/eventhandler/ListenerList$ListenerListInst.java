package cpw.mods.fml.common.eventhandler;

import cpw.mods.fml.common.eventhandler.ListenerList.1;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

class ListenerList$ListenerListInst {
    private boolean rebuild;
    private IEventListener[] listeners;
    private ArrayList<ArrayList<IEventListener>> priorities;
    private ListenerList$ListenerListInst parent;
    // $FF: synthetic field
    final ListenerList this$0;
    @Unique
    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.fml.MixinListenerListInst",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private static final IEventListener DUMMY_EVENT_LISTENER = (event) -> {
    };

    private ListenerList$ListenerListInst(ListenerList var1) {
        this.this$0 = var1;
        this.rebuild = true;
        int count = EventPriority.values().length;
        this.priorities = new ArrayList(count);

        for(int x = 0; x < count; ++x) {
            this.priorities.add(new ArrayList());
        }

    }

    public void dispose() {
        Iterator var1 = this.priorities.iterator();

        while(var1.hasNext()) {
            ArrayList<IEventListener> listeners = (ArrayList)var1.next();
            listeners.clear();
        }

        this.priorities.clear();
        this.parent = null;
        this.listeners = null;
    }

    private ListenerList$ListenerListInst(ListenerList var1, ListenerList$ListenerListInst parent) {
        this(var1);
        this.parent = parent;
    }

    public ArrayList<IEventListener> getListeners(EventPriority priority) {
        ArrayList<IEventListener> ret = new ArrayList((Collection)this.priorities.get(priority.ordinal()));
        if (this.parent != null) {
            ret.addAll(this.parent.getListeners(priority));
        }

        return ret;
    }

    public IEventListener[] getListeners() {
        if (this.shouldRebuild()) {
            this.buildCache();
        }

        return this.listeners;
    }

    protected boolean shouldRebuild() {
        return this.rebuild || this.parent != null && this.parent.shouldRebuild();
    }

    private void buildCache() {
        if (this.parent != null && this.parent.shouldRebuild()) {
            this.parent.buildCache();
        }

        ArrayList<IEventListener> ret = new ArrayList();
        EventPriority[] var2 = EventPriority.values();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            EventPriority value = var2[var4];
            List<IEventListener> listeners = this.getListeners(value);
            if (listeners.size() > 0) {
                ret.add(value);
                ret.addAll(listeners);
            }
        }

        this.listeners = (IEventListener[])ret.toArray(new IEventListener[ret.size()]);
        this.rebuild = false;
    }

    public void register(EventPriority priority, IEventListener listener) {
        ((ArrayList)this.priorities.get(priority.ordinal())).add(listener);
        this.rebuild = true;
    }

    public void unregister(IEventListener listener) {
        Iterator var2 = this.priorities.iterator();

        while(var2.hasNext()) {
            ArrayList<IEventListener> list = (ArrayList)var2.next();
            if (list.remove(listener)) {
                this.rebuild = true;
            }
        }

        this.handler$zio000$hodgepodge$fixUnregisterMemLeak(listener, (CallbackInfo)null);
    }

    // $FF: synthetic method
    ListenerList$ListenerListInst(ListenerList x0, ListenerList$ListenerListInst x1, 1 x2) {
        this(x0, x1);
    }

    // $FF: synthetic method
    ListenerList$ListenerListInst(ListenerList x0, 1 x1) {
        this(x0);
    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.fml.MixinListenerListInst",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private void handler$zio000$hodgepodge$fixUnregisterMemLeak(IEventListener object, CallbackInfo ci) {
        int index = ArrayUtils.indexOf(this.listeners, object);
        if (index != -1) {
            this.listeners[index] = DUMMY_EVENT_LISTENER;
        }
    }
}
