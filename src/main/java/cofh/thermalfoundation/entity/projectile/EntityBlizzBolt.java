package cofh.thermalfoundation.entity.projectile;

import codechicken.lib.util.CommonUtils;
import cofh.thermalfoundation.entity.monster.EntityBlizz;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityBlizzBolt extends EntityFireball {

    protected static class DamageSourceBlizz extends EntityDamageSource {

        public DamageSourceBlizz() {

            this(null);
        }

        public DamageSourceBlizz(Entity source) {

            super("blizz", source);
        }

        public static DamageSource causeDamage(EntityBlizzBolt entityProj, Entity entitySource) {

            return (new EntityDamageSourceIndirect("blizz", entityProj, entitySource == null ? entityProj : entitySource)).setProjectile();
        }
    }

    protected static class PotionEffectBlizz extends PotionEffect {

        public PotionEffectBlizz(Potion potion, int duration, int amplifier, boolean isAmbient) {
            super(potion, duration, amplifier, isAmbient, true);
            getCurativeItems().clear();
        }

        public PotionEffectBlizz(int duration, int amplifier) {
            this(MobEffects.SLOWNESS, duration, amplifier, false);
        }

    }

    public static DamageSource blizzDamage = new DamageSourceBlizz();
    public static PotionEffect blizzEffect = new PotionEffectBlizz(5 * 20, 2);
    public static double accelMultiplier = 0.2D;

    public EntityBlizzBolt(World worldIn)
    {
        super(worldIn);
        this.setSize(0.3125F, 0.3125F);
    }

    public EntityBlizzBolt(World worldIn, EntityLivingBase shooter, double accelX, double accelY, double accelZ)
    {
        super(worldIn, shooter, accelX, accelY, accelZ);
        this.setSize(0.3125F, 0.3125F);
    }

    public EntityBlizzBolt(World worldIn, double x, double y, double z, double accelX, double accelY, double accelZ)
    {
        super(worldIn, x, y, z, accelX, accelY, accelZ);
        this.setSize(0.3125F, 0.3125F);
    }

    @Override
    protected void onImpact(RayTraceResult pos) {

        if (CommonUtils.isServerWorld(worldObj)) {
            if (pos.entityHit != null) {
                if (pos.entityHit instanceof EntityBlizz) {
                    pos.entityHit.attackEntityFrom(DamageSourceBlizz.causeDamage(this, shootingEntity), 0);
                } else {
                    if (pos.entityHit.attackEntityFrom(DamageSourceBlizz.causeDamage(this, shootingEntity), pos.entityHit.isImmuneToFire() ? 8F : 5F) && pos.entityHit instanceof EntityLivingBase) {
                        EntityLivingBase living = (EntityLivingBase) pos.entityHit;
                        living.addPotionEffect(new PotionEffect(EntityBlizzBolt.blizzEffect));
                    }
                }
            } else {
                BlockPos hitPosOffset = pos.getBlockPos().offset(pos.sideHit);

                if (worldObj.isAirBlock(hitPosOffset)) {
                    IBlockState state = worldObj.getBlockState(hitPosOffset.offset(EnumFacing.DOWN));

                    if (state.isSideSolid(worldObj, hitPosOffset.offset(EnumFacing.DOWN), EnumFacing.UP)) {
                        worldObj.setBlockState(hitPosOffset, Blocks.SNOW_LAYER.getDefaultState());
                    }
                }
            }
            for (int i = 0; i < 8; i++) {
                worldObj.spawnParticle(EnumParticleTypes.SNOWBALL, posX, posY, posZ, this.rand.nextDouble(), this.rand.nextDouble(), this.rand.nextDouble());
            }
            setDead();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getBrightnessForRender(float f) {

        return 0xF000F0;
    }

}
