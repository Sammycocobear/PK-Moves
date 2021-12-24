package me.scb.pkmoves.pkmoves.Deflect;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;


public final class Deflect extends ChiAbility implements AddonAbility {
    private Listener listener;
    private long cooldown,time,duration;
    private double radius;
    public Deflect(Player player) {
        super(player);
        if (!this.bPlayer.canBend(this)) {
            return;
        }
        setFields();
        //gets the casters current active ability stance
        final ChiAbility stance = this.bPlayer.getStance();
        //if the caster currently has an instance of a stance
        if (stance != null) {
            //remove the stance
            stance.remove();
            //if the stance that is active is Deflect(this move)
            if (stance instanceof Deflect) {
                //set the Stance as null
                this.bPlayer.setStance(null);
                //return
                return;
            }
        }
        this.start();
        this.bPlayer.setStance(this);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_HURT, 0.5F, 2F);
    }
    private void setFields() {
        final FileConfiguration config = ConfigManager.getConfig();
        cooldown = config.getLong("ExtraAbilities.Sammycocobear.Deflect.cooldown");
        duration = config.getLong("ExtraAbilities.Sammycocobear.Deflect.duration");
        radius = config.getDouble("ExtraAbilities.Sammycocobear.Deflect.radius");
    }
    @Override
    public void progress() {
        time = System.currentTimeMillis();
        Location loc = player.getLocation();
        if (!this.bPlayer.canBendIgnoreBinds(this) || !bPlayer.hasElement(Element.CHI) || time - this.getStartTime() >= duration) {
            remove();
            return;
        }else if (player.isDead() || !player.isOnline() || GeneralMethods.isRegionProtectedFromBuild(this, loc)){
            remove();
            return;
        }else {
            for (Entity entity : GeneralMethods.getEntitiesAroundPoint(loc, radius)) {
                //if the entity were interating through is the caster then skip them and continue on to the next one
                if (entity.getUniqueId() == player.getUniqueId()) continue;
                //if the entity were interating through is a throwable entity
                if (entity instanceof Projectile) {
                    switch (entity.getType()){
                        case ARROW:
                            final Arrow arrow = (Arrow) entity;
                            //if the arrow isnt stuck to a block, AND the entity's(that is an arrow) shooter (person who shot it) is not the caster
                            if (!arrow.isInBlock() && arrow.getShooter() != player) {
                                //set the velocity of the arrow to be a inverse of its current speed
                                setVelo(arrow);
                                //play particles
                                arrow.getLocation().getWorld().spawnParticle(Particle.CLOUD, arrow.getLocation(), 5, 0, 0, 0, 0);
                            }
                            break;
                        case SNOWBALL:
                            //makes snowball variable
                            Snowball Snowball =  (Snowball) entity;
                            //if the entity's(that is an snowball) shooter (person who threw it) is not the caster
                            if (Snowball.getShooter() != player) {
                                //set the velocity of the snowball (which is an inverse of the current speed of the snowball)
                                setVelo(Snowball);
                                //play particles
                                Snowball.getLocation().getWorld().spawnParticle(Particle.SNOW_SHOVEL, Snowball.getLocation(), 5, 0, 0, 0, 0);
                            }
                    }
                }
            }
        }
    }


    public void setVelo(Projectile proj) {
        //if the projectile's shooter isnt the player
        if (proj.getShooter() != player) {
            //entity variable
            Entity entity = proj;
            //rotates the projectile to be the backwards of the current projectiles rotations, spins it in a 180
            entity.setRotation(-(proj.getLocation().getYaw()), -(proj.getLocation().getPitch()));
            //sets the projectile's velocity to the inverse of the entities current velocity in all axis's
            entity.setVelocity(new Vector(
                    -(entity.getVelocity().getX()),
                    -(entity.getVelocity().getY()),
                    -(entity.getVelocity().getZ())
            ));
        }
    }


    public void remove(){
        super.remove();
        bPlayer.addCooldown(this,cooldown);
        bPlayer.setStance(null);
    }


    @Override
    public boolean isSneakAbility() {
        return false;
    }

    @Override
    public boolean isHarmlessAbility() {
        return true;
    }

    @Override
    public long getCooldown() {
        return cooldown;
    }

    @Override
    public String getName() {
        return "Deflect";
    }

    @Override
    public Location getLocation() {
        return player.getLocation();
    }

    @Override
    public void load() {
        final FileConfiguration config = ConfigManager.getConfig();
        listener = new DeflectListener();
        ProjectKorra.plugin.getServer().getPluginManager().registerEvents(listener,ProjectKorra.plugin);
        config.addDefault("ExtraAbilities.Sammycocobear.Deflect.cooldown",0);
        config.addDefault("ExtraAbilities.Sammycocobear.Deflect.duration",10000);
        config.addDefault("ExtraAbilities.Sammycocobear.Deflect.radius", 5);
    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(listener);
    }

    @Override
    public String getAuthor() {
        return "Sammycocobear";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }
    @Override
    public String getInstructions() {
        return "If Deflect is disabled <Left Click> to enable, if it is enabled <LeftC Click> to disable";
    }

    @Override
    public String getDescription() {
        return "Chi Blocker focuse their senses into their eyes, focusing on their surroundings. If there is a Projectile near the Chi Blocker , the Chi Blocker will deflect them ";
    }
}
