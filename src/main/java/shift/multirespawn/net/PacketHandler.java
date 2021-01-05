package shift.multirespawn.net;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.ParallelDispatchEvent;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.Objects;
import java.util.Optional;

public class PacketHandler {

    /*
     * MOD固有のSimpleNetworkWrapperを取得。 文字列は他のMODと被らないようにMOD_IDを指定しておくと良い
     */
    public static final SimpleChannel INSTANCE = NetworkRegistry
            .ChannelBuilder.named(new ResourceLocation("multirespawn", "packet")).
                    clientAcceptedVersions(s -> Objects.equals(s, "1")).
                    serverAcceptedVersions(s -> Objects.equals(s, "1")).
                    networkProtocolVersion(() -> "1").simpleChannel();

    public static void init(ParallelDispatchEvent event) {

        /*
         * Messageクラスの登録。 第一引数：IMessageHandlerクラス 第二引数：送るMessageクラス
         * 第三引数：登録番号。255個まで 第四引数：ClientとServerのどちらに送るか。送り先
         */
        INSTANCE.registerMessage(0, SetRespawnPacket.class, SetRespawnPacket::encode, SetRespawnPacket::decode, SetRespawnPacket::handle);
        INSTANCE.registerMessage(1, PlayerDataPacket.class, PlayerDataPacket::encode, PlayerDataPacket::decode, PlayerDataPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        //INSTANCE.registerMessage(MessagePlayerProperties.class, MessagePlayerProperties.class, 0, Side.CLIENT);
        //INSTANCE.registerMessage(0,MessagePlayerProperties.class);
//		INSTANCE.registerMessage(ShopButtonHandler.class,PacketShopButton.class, 1, Side.SERVER);
//		INSTANCE.registerMessage(MessageGuiId.class,PacketGuiId.class, 2, Side.SERVER);
//
//		INSTANCE.registerMessage(PlayerLoginHandler.class,PacketPlayerLogin.class, 3, Side.SERVER);

    }

}