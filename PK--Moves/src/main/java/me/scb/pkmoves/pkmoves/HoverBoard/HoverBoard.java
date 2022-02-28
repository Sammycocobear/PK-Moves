package me.scb.pkmoves.pkmoves.HoverBoard;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.DamageHandler;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class HoverBoard extends ChiAbility implements AddonAbility {
    private ArmorStand stand;
    private EntityEquipment equipment;
    private final List<ArmorStand> standList = new ArrayList<>();
    private Listener listener;
    private static Particle.DustOptions blueParticleOptions = new Particle.DustOptions(Color.fromRGB(137,207,240),1.5f);
    private static Particle.DustOptions whiteParticleOptions = new Particle.DustOptions(Color.fromRGB(255,255,255),1.5f);
    private static Particle.DustOptions redParticleOptions = new Particle.DustOptions(Color.fromRGB(139,0,0) ,1.5f);
    private static Particle.DustOptions blackParticleOptions = new Particle.DustOptions(Color.fromRGB(0,0,0),1.5f);
    private final long cooldown,duration;
    private final double shootSpeed,rideSpeed;
    private final int hoverHeight;
    private final double shootRange;
    private final double explodeSize,explodeDamage,playerExplodeDamage;
    private final double jumpVelocity;
    private static boolean jump;
    private Location floorLocation,shootOrigin;
    private Vector playerJumpDirection;
    private final double shootHitbox;

    public HoverBoard(Player player) {
        super(player);
        final FileConfiguration config = ConfigManager.getConfig();
        String path = "ExtraAbilities.Sammycocobear.HoverBoard.";


        cooldown = config.getLong(path + "Cooldown");
        duration = config.getLong(path + "Duration");
        shootRange = config.getDouble(path + "ShootRange");
        playerExplodeDamage = config.getDouble(path + "PlayerExplodeDamage");
        explodeSize = config.getDouble(path + "ExplodeSize");
        hoverHeight = config.getInt(path + "HoverHeight");
        rideSpeed = config.getDouble(path +"RideSpeed");
        shootSpeed = config.getDouble(path + "ShootSpeed");
        explodeDamage = config.getDouble(path + "Damage");
        jumpVelocity = config.getDouble(path + "JumpVelocity");
        shootHitbox = config.getDouble(path + "ShootHitbox");

        if (CoreAbility.hasAbility(player,getClass()) || !bPlayer.canBend(this)) return;
        int x = 270;
        for (int i = 0; i < 3; i++) {
            stand = (ArmorStand) player.getLocation().getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
            stand.setVisible(false);
            equipment = stand.getEquipment();
            equipment.setHelmet(new ItemStack(Material.HEAVY_WEIGHTED_PRESSURE_PLATE));
            stand.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING);
            standList.add(stand);
            stand.setHeadPose(new EulerAngle(x, 0, 0));
            x -= 270;
            jump = false;
        }
        player.setAllowFlight(true);
        start();
    }


    @Override
    public void progress() {
        if (GeneralMethods.isRegionProtectedFromBuild(this, player.getLocation())) {
            remove();
            return;
        }else if (System.currentTimeMillis() - getStartTime() >= duration){
            remove();
            return;
        }else{
            if (!jump) {
                Location location = player.getLocation().subtract(0.0D, 1.35, 0.0D);
                location.add(player.getVelocity().getX() * 2.95, 0, player.getVelocity().getZ() * 2.95);
                //Don't rotate up/down
                location.setPitch(0);
                location.getWorld().playSound(location,Sound.BLOCK_BEACON_AMBIENT,.65f,1.9f);
                for (int i = 0; i < standList.size(); i++) {
                    //spawns particles on each plate
                    Location standLocation = standList.get(i).getLocation();
                    standLocation.getWorld().spawnParticle(Particle.REDSTONE, standLocation.clone().add(0, 1.35, 0), 1, 0, 0, 0, .5, new Particle.DustOptions(Color.fromRGB(137, 207, 240), 1.5f));
                    //The first armorstand created was the one infront of the player
                    if (i == 0) {
                        standList.get(i).teleport(location.clone().add(location.getDirection().multiply(.55)));
                        if (standList.get(i).getLocation().add(0, 1.5, 0).getBlock().getType().isSolid()) {
                            explode(player.getLocation(), explodeSize);
                        }
                        for (Entity entity : GeneralMethods.getEntitiesAroundPoint(standList.get(i).getLocation().add(0, 1, 0), .5)) {
                            if (entity instanceof LivingEntity && !(entity instanceof ArmorStand)) {
                                if (entity.getUniqueId() == player.getUniqueId()) continue;
                                explode(player.getLocation(), explodeSize);
                            }
                        }
                    } else if (i == 1) { //The second armorstand was the one on the player location
                        standList.get(i).teleport(location);
                    } else { // The third armorstand was the one behind the player
                        standList.get(i).teleport(location.clone().add(location.getDirection().multiply(-.55)));

                        Location loc = standList.get(i).getLocation().clone();
                        Vector pDir = loc.getDirection().multiply(-.777);
                        for (int p = 0; p < 3; p++) {
                            loc.getWorld().spawnParticle
                                    (Particle.REDSTONE, loc.clone().add(pDir).add(0, 1.35, 0), 1, .5, 0, .5, 1, blueParticleOptions);
                            loc.getWorld().spawnParticle
                                    (Particle.REDSTONE, loc.clone().add(pDir).add(0, 1.35, 0), 1, .5, 0, .5, 1, whiteParticleOptions);
                            loc.getWorld().spawnParticle
                                    (Particle.FIREWORKS_SPARK, loc.clone().add(pDir).add(0, 1.35, 0), 1, .75, 0, .75, .15);
                        }
                    }
                }

                Block standingblock = location.getBlock();
                int i = 0;
                while (i <= 255) {
                    Block block = standingblock.getRelative(BlockFace.DOWN, i);
                    if (GeneralMethods.isSolid(block) || block.isLiquid()) {
                        floorLocation = block.getLocation();
                        break;
                    }
                    ++i;
                }


                if ((int) (player.getLocation().getY() - floorLocation.getY()) > hoverHeight) {
                    player.setVelocity(new Vector(location.getDirection().getX() * rideSpeed, -.25, location.getDirection().getZ() * rideSpeed));
                } else if ((int) (player.getLocation().getY() - floorLocation.getY()) < hoverHeight) {
                    player.setVelocity(new Vector(location.getDirection().getX() * rideSpeed, .25, location.getDirection().getZ() * rideSpeed));
                } else {
                    player.setVelocity(new Vector(location.getDirection().getX() * rideSpeed, 0, location.getDirection().getZ() * rideSpeed));
                }

            } else {
                if (standList.get(0).getLocation().distanceSquared(shootOrigin) >= (shootRange * shootRange)) {
                    explode(standList.get(0).getLocation(), explodeSize);
                    return;
                }
                standList.get(0).getWorld().playSound(standList.get(0).getLocation(),Sound.BLOCK_BEACON_DEACTIVATE,.65f,1.9f);
                for (int i = 0; i < standList.size(); i++) {
                    if (GeneralMethods.isSolid(standList.get(i).getLocation().add(playerJumpDirection.multiply(1)).getBlock())) {
                        explode(standList.get(i).getLocation(), explodeSize);
                    }

                    standList.get(i).setVelocity(playerJumpDirection);
                    if (i == 0) {
                        for (Entity entity : GeneralMethods.getEntitiesAroundPoint(standList.get(i).getLocation(),shootHitbox )) {
                            if (entity.equals(player) || !(entity instanceof LivingEntity) || entity instanceof ArmorStand)
                                continue;
                            explode(standList.get(i).getLocation(), explodeSize);
                        }
                    } else if (i == 2) {
                        standList.get(i).getWorld().spawnParticle
                                (Particle.REDSTONE, standList.get(i).getLocation().add(playerJumpDirection).add(0, 1.35, 0), 3, .5, 0, .5, 1, redParticleOptions);
                        standList.get(i).getWorld().spawnParticle
                                (Particle.REDSTONE, standList.get(i).getLocation().add(playerJumpDirection).add(0, 1.35, 0), 3, .5, 0, .5, 1, blackParticleOptions);
                        standList.get(i).getWorld().spawnParticle
                                (Particle.FIREWORKS_SPARK, standList.get(i).getLocation().add(playerJumpDirection).add(0, 1.35, 0), 3, .75, 0, .75, .15);
                    }
                }

            }
        }

    }



    private void explode(Location location,double size) {
        location.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME,location,10,1,1,1,2f);
        location.getWorld().spawnParticle(Particle.SMOKE_LARGE,location,10,1,1,1,.5f);
        location.getWorld().spawnParticle(Particle.FIREWORKS_SPARK,location,10,1,1,1,.5f);
        location.getWorld().spawnParticle(Particle.EXPLOSION_LARGE,location,10,1,1,1,.5f);
        location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, .8f, 1.34f);
        for (Entity e : GeneralMethods.getEntitiesAroundPoint(location, size)) {
            if (e instanceof LivingEntity){
                if (e.getUniqueId() == player.getUniqueId()){
                    DamageHandler.damageEntity(e,playerExplodeDamage,this);
                }else {
                    DamageHandler.damageEntity(e, explodeDamage, this);
                }
            }
        }
        remove();
    }

    public void setJump(){
        Location playerLoc = player.getLocation();
        shootOrigin = playerLoc.clone();
        playerLoc.setPitch(0);
        playerJumpDirection = playerLoc.getDirection().multiply(shootSpeed);
        player.setVelocity(new Vector(playerLoc.getDirection().getX() * -jumpVelocity,.5,playerLoc.getDirection().getZ() * -jumpVelocity));
        jump = true;
    }

    public void jump(Player player) {
        HoverBoard abil = getAbility(player, HoverBoard.class);
        if (abil != null) abil.setJump();
    }

    @Override
    public void remove() {
        super.remove();
        player.setAllowFlight(false);
        bPlayer.addCooldown(this,cooldown);
        for (ArmorStand stand : standList) {
            stand.remove();
        }
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
        return cooldown ;
    }

    @Override
    public String getName() {
        return "HoverBoard";
    }

    @Override
    public Location getLocation() {
        return player.getLocation();
    }

    @Override
    public void load() {
        listener = new HoverBoardListener();
        ProjectKorra.plugin.getServer().getPluginManager().registerEvents(listener, ProjectKorra.plugin);
        String path = "ExtraAbilities.Sammycocobear.HoverBoard.";
        FileConfiguration config = ConfigManager.getConfig();
        config.addDefault(path + "JumpVelocity",1.5);
        config.addDefault(path + "Damage",2);
        config.addDefault(path + "Duration",5000);
        config.addDefault(path + "ShootRange",20);
        config.addDefault(path + "Cooldown",8000);
        config.addDefault(path + "ExplodeSize",3);
        config.addDefault(path + "PlayerExplodeDamage",3);
        config.addDefault(path + "HoverHeight",3);
        config.addDefault(path + "RideSpeed",1.5);
        config.addDefault(path + "ShootSpeed",2);
        config.addDefault(path + "ShootHitbox",1);
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


    public static boolean getStatus(){
        return jump;
    }

    @Override
    public String getInstructions() {
        return "When bound (left click) to put the hoverboard in motion, then (tap shift) to dismount your hoverboard, exploding anything in its way";
    }

    @Override
    public String getDescription() {
        return "Glide above the ground on your hoverboard to travel around on and deal explosive damage to anything in your way!";
    }


}
