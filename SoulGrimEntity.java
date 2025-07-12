package net.enderboy.soulgust.entity.soul_grim;

import net.enderboy.soulgust.content.SoulGustItems;
import net.enderboy.soulgust.util.SoulGustSoundEvents;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class SoulGrimEntity extends TameableEntity {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;
    private int attackAnimationTimeout = 0;


    public SoulGrimEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 16)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.35)
                .add(EntityAttributes.ATTACK_DAMAGE, 4)
                .add(EntityAttributes.WATER_MOVEMENT_EFFICIENCY, 0.5)
                .add(EntityAttributes.FOLLOW_RANGE, 20);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoulGustSoundEvents.SOUL_GRIM_AMBIENCE;
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource source) {
        return SoulGustSoundEvents.SOUL_GRIM_HURT;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return SoulGustSoundEvents.SOUL_GRIM_DEATH;
    }

    @Override
    protected void attackLivingEntity(LivingEntity target) {

        super.getOwner().getAttacking();
        super.getOwner().getAttacker();
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (player==this.getOwner()&&player.isSneaking()&&!player.isHolding(SoulGustItems.SOUL_GRIM)){
            player.giveItemStack(SoulGustItems.SOUL_GRIM.getDefaultStack());
            this.discard();
        }
        return super.interactMob(player, hand);
    }

    @Override
    public boolean isFireImmune() {
        return true;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new PounceAtTargetGoal(this,0.2f));
        this.goalSelector.add(2, new MeleeAttackGoal(this,1,false));
        this.goalSelector.add(4, new WanderAroundFarGoal(this, 1.0));
        this.goalSelector.add(5, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(5, new LookAroundGoal(this));
        this.goalSelector.add(3, new FollowOwnerGoal(this,1,15,2));
        this.targetSelector.add(1, new TrackOwnerAttackerGoal(this));
        this.targetSelector.add(1, new AttackWithOwnerGoal(this));
    }

    private void setupAnimationStates() {
        if (!this.isAttacking()){
            if (this.idleAnimationTimeout <= 0) {
                this.idleAnimationTimeout = 40;
                this.idleAnimationState.start(this.age);
            } else {
                --this.idleAnimationTimeout;
            }
        }else if (this.isAttacking()){
            if (this.attackAnimationTimeout <= 0) {
                this.attackAnimationTimeout = 10;
                this.attackAnimationState.start(this.age);
            } else {
                --this.attackAnimationTimeout;
            }
        }
    }



    public boolean canAttackWithOwner(LivingEntity target, LivingEntity owner) {
        return target.isAttackable();
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getWorld().isClient()) {
            this.setupAnimationStates();
        }
    }


    @Override
    public void setTarget(@Nullable LivingEntity target) {
        this.getOwner().getAttacker();
        super.setTarget(target);
    }



    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return false;
    }

    @Override
    public @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }
}
