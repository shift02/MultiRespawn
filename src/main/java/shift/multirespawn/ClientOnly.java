package shift.multirespawn;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.DistExecutor;
import shift.multirespawn.capability.RespawnPlayerData;
import shift.multirespawn.net.PlayerDataPacket;

public class ClientOnly {

    public static DistExecutor.SafeRunnable handlePlayerDataPacket(CompoundNBT data) {
        return new DistExecutor.SafeRunnable() {
            @Override
            public void run() {

                PlayerEntity player = Minecraft.getInstance().player;

                RespawnPlayerData respawnPlayerData = player.getCapability(RespawnPlayerData.capability).orElseThrow(IllegalStateException::new);
                respawnPlayerData.deserializeNBT(data.getCompound(PlayerDataPacket.PACKET_KYE));

            }
        };
    }

}
