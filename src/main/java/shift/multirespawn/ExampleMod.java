package shift.multirespawn;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import shift.multirespawn.capability.RespawnPlayerData;
import shift.multirespawn.net.PacketHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerSetSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("multirespawn")
public class ExampleMod
{
    private final static ResourceLocation KYE = new ResourceLocation("multirespawn", "respawnplayerdata");

    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public ExampleMod() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        // some preinit code
        LOGGER.info("HELLO FROM PREINIT");
        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());

        PacketHandler.init(event);
        CapabilityManager.INSTANCE.register(RespawnPlayerData.class, new RespawnPlayerData(),RespawnPlayerData::new);
        //PlayerEvent.PlayerRespawnEvent
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);

    }

    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo("examplemod", "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
    }

    private void processIMC(final InterModProcessEvent event)
    {
        // some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m->m.getMessageSupplier().get()).
                collect(Collectors.toList()));
    }
    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
        LOGGER.info("HELLO from server starting");
    }




    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            // register a new block here
            LOGGER.info("HELLO from Register Block");


        }



    }

    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.FORGE)
    public static class RegistryEvents1 {

        /**
         * リスポーン地点が登録された時のイベント
         * @param playerSetSpawnEvent
         */
        @SubscribeEvent
        public static void onplayerSetSpawnEvent(final PlayerSetSpawnEvent playerSetSpawnEvent) {
            // register a new block here
            LOGGER.info("playerSetSpawnEvent "+playerSetSpawnEvent.getNewSpawn());
            LOGGER.info("playerSetSpawnEvent "+playerSetSpawnEvent.getSpawnWorld().toString());

            RespawnPlayerData respawnPlayerData = playerSetSpawnEvent.getPlayer().getCapability(RespawnPlayerData.capability).orElseThrow(IllegalStateException::new);

            respawnPlayerData.addBlockPos(playerSetSpawnEvent.getPlayer(), playerSetSpawnEvent.getNewSpawn());

        }

        @SubscribeEvent
        public static void onEntityConstructing(AttachCapabilitiesEvent<Entity> entityAttachCapabilitiesEvent) {
            if (entityAttachCapabilitiesEvent.getObject() instanceof PlayerEntity) {
                entityAttachCapabilitiesEvent.addCapability(KYE, new RespawnPlayerData());
            }
        }

        @SubscribeEvent
        public static void onClone(PlayerEvent.Clone event) {

            PlayerEntity oldPlayer = event.getOriginal();
            oldPlayer.revive();

            oldPlayer.getCapability(RespawnPlayerData.capability).ifPresent(respawnPlayerData -> {

                event.getPlayer().getCapability(RespawnPlayerData.capability).ifPresent(
                        newRespawnPlayerData -> {
                            newRespawnPlayerData.deserializeNBT(respawnPlayerData.serializeNBT());
                        }
                );

            });

        }

        //データ同期系

        @SubscribeEvent
        /* ワールドに入った時に呼ばれるイベント。 */
        public static void onEntityJoinWorld(EntityJoinWorldEvent event) {

            if (!event.getEntity().world.isRemote && event.getEntity() instanceof ServerPlayerEntity) {

                RespawnPlayerData.loadProxyData((ServerPlayerEntity) event.getEntity());

            }

        }

        @SubscribeEvent
        /* ログインした時に呼ばれるイベント。 */
        public static void onPlayerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {

            if (!event.getPlayer().world.isRemote) {

                RespawnPlayerData.loadProxyData(event.getPlayer());

            }
        }


        @SubscribeEvent
        public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
            // プレイヤーがディメンション間を移動したときの処理

            if (!event.getPlayer().world.isRemote) {
                RespawnPlayerData.loadProxyData( event.getPlayer());

            }

        }

        @SubscribeEvent
        public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
            // プレイヤーがリスポーンした時の処理
            if (!event.getPlayer().world.isRemote) {

                RespawnPlayerData.loadProxyData((ServerPlayerEntity) event.getPlayer());

            }

        }


    }
}
