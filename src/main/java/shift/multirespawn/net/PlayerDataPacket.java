package shift.multirespawn.net;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import shift.multirespawn.ClientOnly;
import shift.multirespawn.capability.RespawnPlayerData;

import java.util.function.Supplier;

public class PlayerDataPacket {

    public static final String PACKET_KYE = "respawn_player_data_packet";

    private CompoundNBT data;

    private PlayerDataPacket(CompoundNBT data) {
        this.data = data;
    }

    public static PlayerDataPacket createPlayerDataPacket(RespawnPlayerData respawnPlayerData) {
        CompoundNBT data = new CompoundNBT();
        data.put(PACKET_KYE, respawnPlayerData.serializeNBT());

        return new PlayerDataPacket(data);
    }

    public static PlayerDataPacket decode(PacketBuffer buf) {
        return new PlayerDataPacket(buf.readCompoundTag());
    }

    public static void encode(PlayerDataPacket playerDataPacket, PacketBuffer buf) {
        buf.writeCompoundTag(playerDataPacket.data);
    }

    public static void handle(PlayerDataPacket playerDataPacket, Supplier<NetworkEvent.Context> context) {

        context.get().enqueueWork(() -> {

            DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientOnly.handlePlayerDataPacket(playerDataPacket.data));

        });
        context.get().setPacketHandled(true);

    }

}

