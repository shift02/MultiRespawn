package shift.multirespawn.net;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Respawn地点を上書きするpacket
 */
public class SetRespawnPacket {

    private CompoundNBT data;

    private SetRespawnPacket(CompoundNBT data) {
        this.data = data;
    }

    public static SetRespawnPacket createSetRespawnPacket(RegistryKey<World> dimensionKey, BlockPos pos) {
        CompoundNBT data = new CompoundNBT();
        CompoundNBT compoundNBT = NBTUtil.writeBlockPos(pos);
        data.put("pos",compoundNBT);
        data.putString("dimension",dimensionKey.getLocation().toString());

        return new SetRespawnPacket(data);
    }

    public static SetRespawnPacket decode(PacketBuffer buf) {
        return new SetRespawnPacket(buf.readCompoundTag());
    }

    public static void encode(SetRespawnPacket setRespawnPacket, PacketBuffer buf) {
        buf.writeCompoundTag(setRespawnPacket.data);
    }

    public static void handle(SetRespawnPacket setRespawnPacket, Supplier<NetworkEvent.Context> context) {

        context.get().enqueueWork(() -> {

            CompoundNBT data = setRespawnPacket.data;
            RegistryKey<World> dimensionKey = RegistryKey.getOrCreateKey(Registry.WORLD_KEY,  new ResourceLocation(data.getString("dimension")));
            BlockPos pos = NBTUtil.readBlockPos(data.getCompound("pos"));

            ServerPlayerEntity sender = context.get().getSender();

            RegistryKey.getOrCreateKey(Registry.WORLD_KEY, dimensionKey.getRegistryName());


            sender.func_242111_a(dimensionKey, pos,0,true, false);

        });
        //正常なPacketの設定
        context.get().setPacketHandled(true);

    }

}

