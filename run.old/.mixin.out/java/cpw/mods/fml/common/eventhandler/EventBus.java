package cpw.mods.fml.common.eventhandler;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.MapMaker;
import com.google.common.reflect.TypeToken;
import com.gtnewhorizon.gtnhlib.mixins.early.fml.EventBusAccessor;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

public class EventBus implements IEventExceptionHandler, EventBusAccessor {
    private static int maxID = 0;
    private ConcurrentHashMap<Object, ArrayList<IEventListener>> listeners;
    private Map<Object, ModContainer> listenerOwners;
    private final int busID;
    private IEventExceptionHandler exceptionHandler;

    public EventBus() {
        this.listeners = new ConcurrentHashMap();
        this.listenerOwners = (new MapMaker()).weakKeys().weakValues().makeMap();
        this.busID = maxID++;
        ListenerList.resize(this.busID + 1);
        this.exceptionHandler = this;
    }

    public EventBus(@Nonnull IEventExceptionHandler handler) {
        this();
        Preconditions.checkArgument(handler != null, "EventBus exception handler can not be null");
        this.exceptionHandler = handler;
    }

    public void register(Object target) {
        if (!this.listeners.containsKey(target)) {
            ModContainer activeModContainer = Loader.instance().activeModContainer();
            if (activeModContainer == null) {
                FMLLog.log(Level.ERROR, new Throwable(), "Unable to determine registrant mod for %s. This is a critical error and should be impossible", new Object[]{target});
                activeModContainer = Loader.instance().getMinecraftModContainer();
            }

            this.listenerOwners.put(target, activeModContainer);
            Set<? extends Class<?>> supers = TypeToken.of(target.getClass()).getTypes().rawTypes();
            Method[] var4 = target.getClass().getMethods();
            int var5 = var4.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                Method method = var4[var6];
                Iterator var8 = supers.iterator();

                while(var8.hasNext()) {
                    Class cls = (Class)var8.next();

                    try {
                        Method real = cls.getDeclaredMethod(method.getName(), method.getParameterTypes());
                        if (real.isAnnotationPresent(SubscribeEvent.class)) {
                            Class<?>[] parameterTypes = method.getParameterTypes();
                            if (parameterTypes.length != 1) {
                                throw new IllegalArgumentException("Method " + method + " has @SubscribeEvent annotation, but requires " + parameterTypes.length + " arguments.  Event handler methods must require a single argument.");
                            }

                            Class<?> eventType = parameterTypes[0];
                            if (!Event.class.isAssignableFrom(eventType)) {
                                throw new IllegalArgumentException("Method " + method + " has @SubscribeEvent annotation, but takes a argument that is not an Event " + eventType);
                            }

                            this.register(eventType, target, method, (ModContainer)activeModContainer);
                            break;
                        }
                    } catch (NoSuchMethodException var13) {
                    }
                }
            }

        }
    }

    private void register(Class<?> eventType, Object target, Method method, ModContainer owner) {
        try {
            Constructor<?> ctr = eventType.getConstructor();
            ctr.setAccessible(true);
            Event event = (Event)ctr.newInstance();
            ASMEventHandler listener = new ASMEventHandler(target, method, owner);
            event.getListenerList().register(this.busID, listener.getPriority(), listener);
            ArrayList<IEventListener> others = (ArrayList)this.listeners.get(target);
            if (others == null) {
                others = new ArrayList();
                this.listeners.put(target, others);
            }

            others.add(listener);
        } catch (Exception var9) {
            var9.printStackTrace();
        }

    }

    public void unregister(Object object) {
        this.handler$zip000$hodgepodge$removeListenerOwners(object, (CallbackInfo)null);
        ArrayList<IEventListener> list = (ArrayList)this.listeners.remove(object);
        if (list != null) {
            Iterator var3 = list.iterator();

            while(var3.hasNext()) {
                IEventListener listener = (IEventListener)var3.next();
                ListenerList.unregisterAll(this.busID, listener);
            }

        }
    }

    public boolean post(Event event) {
        IEventListener[] listeners = event.getListenerList().getListeners(this.busID);
        int index = 0;

        try {
            while(index < listeners.length) {
                listeners[index].invoke(event);
                ++index;
            }
        } catch (Throwable var5) {
            this.exceptionHandler.handleException(this, event, listeners, index, var5);
            Throwables.propagate(var5);
        }

        return event.isCancelable() ? event.isCanceled() : false;
    }

    public void handleException(EventBus bus, Event event, IEventListener[] listeners, int index, Throwable throwable) {
        FMLLog.log(Level.ERROR, throwable, "Exception caught during firing event %s:", new Object[]{event});
        FMLLog.log(Level.ERROR, "Index: %d Listeners:", new Object[]{index});

        for(int x = 0; x < listeners.length; ++x) {
            FMLLog.log(Level.ERROR, "%d: %s", new Object[]{x, listeners[x]});
        }

    }

    @MixinMerged(
        mixin = "com.mitchej123.hodgepodge.mixins.early.fml.MixinEventBus",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    private void handler$zip000$hodgepodge$removeListenerOwners(Object object, CallbackInfo ci) {
        this.listenerOwners.remove(object);
    }

    // $FF: synthetic method
    @Accessor(
        target = "listenerOwners:Ljava/util/Map;"
    )
    @MixinMerged(
        mixin = "com.gtnewhorizon.gtnhlib.mixins.early.fml.EventBusAccessor",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    public Map getListenerOwners() {
        return this.listenerOwners;
    }

    // $FF: synthetic method
    @Accessor(
        target = "listeners:Ljava/util/concurrent/ConcurrentHashMap;"
    )
    @MixinMerged(
        mixin = "com.gtnewhorizon.gtnhlib.mixins.early.fml.EventBusAccessor",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    public ConcurrentHashMap getListeners() {
        return this.listeners;
    }

    // $FF: synthetic method
    @Accessor(
        target = "busID:I"
    )
    @MixinMerged(
        mixin = "com.gtnewhorizon.gtnhlib.mixins.early.fml.EventBusAccessor",
        priority = 1000,
        sessionId = "98d213e4-85ca-4ce4-959f-84858add440a"
    )
    public int getBusID() {
        return this.busID;
    }
}
