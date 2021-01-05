package shift.multirespawn.capability;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;
import shift.multirespawn.net.PacketHandler;
import shift.multirespawn.net.PlayerDataPacket;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RespawnPlayerData implements Capability.IStorage<RespawnPlayerData>, ICapabilitySerializable<CompoundNBT> {

    @CapabilityInject(RespawnPlayerData.class)
    public static Capability<RespawnPlayerData> capability = null;

    private LazyOptional<RespawnPlayerData> instance = LazyOptional.of(()->this);

    private Set<BlockPos> blockPosList;

    public RespawnPlayerData(){
        blockPosList = new LinkedHashSet<>();
    }

    public void addBlockPos(PlayerEntity playerEntity, BlockPos pos){
        blockPosList.add(pos);
        if(!playerEntity.getEntityWorld().isRemote() && playerEntity instanceof ServerPlayerEntity){
            //サーバーの場合はクライアントに同期用の通信をおこなう
            PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(()-> (ServerPlayerEntity) playerEntity),PlayerDataPacket.createPlayerDataPacket(this));
        }
        //System.out.printf(blockPosList.stream().map(BlockPos::toString).collect(Collectors.joining(", ")));
    }

    public static void loadProxyData(PlayerEntity playerEntity){

        if(!playerEntity.getEntityWorld().isRemote() && playerEntity instanceof ServerPlayerEntity){

            playerEntity.getCapability(capability).ifPresent(respawnPlayerData -> {
                PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(()-> (ServerPlayerEntity) playerEntity),PlayerDataPacket.createPlayerDataPacket(respawnPlayerData));
            });

        }

    }

    public Set<BlockPos> getBlockPosList() {
        return blockPosList;
    }

    @Nullable
    @Override
    public INBT writeNBT(Capability<RespawnPlayerData> capability, RespawnPlayerData instance, Direction side) {
        return instance.serializeNBT();
    }

    @Override
    public void readNBT(Capability<RespawnPlayerData> capability, RespawnPlayerData instance, Direction side, INBT nbt) {
        instance.deserializeNBT((CompoundNBT)nbt);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == capability ? instance.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundNBT serializeNBT() {

        CompoundNBT base = new CompoundNBT();
        CompoundNBT compoundNBT = new CompoundNBT();
        base.put("multirespawn",compoundNBT);

        compoundNBT.putInt("posSize", blockPosList.size());
        int count =0;
        for (BlockPos blockPos : blockPosList) {
            compoundNBT.put("pos_"+count, NBTUtil.writeBlockPos(blockPos));
            count++;
        }

        //System.out.println("serializeNBT: "+base.toString());

        return base;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {

        CompoundNBT multiRespawnNBT = nbt.getCompound("multirespawn");

        //System.out.println("deserializeNBT: "+multiRespawnNBT.toString());

        int size = multiRespawnNBT.getInt("posSize");
        for (int i = 0; i < size; i++) {
            blockPosList.add(NBTUtil.readBlockPos(multiRespawnNBT.getCompound("pos_"+i)));
        }

    }
}
