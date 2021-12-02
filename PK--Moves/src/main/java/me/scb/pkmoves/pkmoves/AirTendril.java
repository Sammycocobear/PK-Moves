package me.scb.pkmoves.pkmoves;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

import java.util.*;

public final class AirTendril extends AirAbility implements AddonAbility, ComboAbility {
    private long cooldown;
    private double damage;
    private double distance;
    private Location location;
    private Vector direction;
    private Set<Entity> hurt;
    private double hitbox;
    public float radius;
    public float grow = .05f;
    public double radials = Math.PI / 16;
    private double knockback;
    public int circles;
    private double speed;
    public int helixes;
    protected int step = 0;
    private Location origin;
    static public final float PI = 3.1415927f;
    static public final float degreesToRadians = PI / 180;
    private int particles;
    public AirTendril(Player player) {
        super(player);
        if (this.bPlayer.isOnCooldown(this)) {
            return;
        }
        setFields();
        start();
    }



    private void setFields() {
        circles = ConfigManager.getConfig().getInt("ExtraAbilities.Sammycocobear.AirTendril.Circles");
        helixes = ConfigManager.getConfig().getInt("ExtraAbilities.Sammycocobear.AirTendril.Helixes");
        radius= (float)ConfigManager.getConfig().getDouble("ExtraAbilities.Sammycocobear.AirTendril.Radius");
        cooldown = ConfigManager.getConfig().getLong("ExtraAbilities.Sammycocobear.AirTendril.Cooldown");
        damage = ConfigManager.getConfig().getDouble("ExtraAbilities.Sammycocobear.AirTendril.Damage");
        distance = ConfigManager.getConfig().getDouble("ExtraAbilities.Sammycocobear.AirTendril.Distance");
        knockback = ConfigManager.getConfig().getDouble("ExtraAbilities.Sammycocobear.AirTendril.Knockback");
        hitbox = ConfigManager.getConfig().getDouble("ExtraAbilities.Sammycocobear.AirTendril.Hitbox");
        particles = ConfigManager.getConfig().getInt("ExtraAbilities.Sammycocobear.AirTendril.Particles");
        speed = ConfigManager.getConfig().getDouble("ExtraAbilities.Sammycocobear.AirTendril.Speed");
        origin = player.getLocation();
        location = player.getLocation().add(0,.2,0);
        direction = player.getLocation().getDirection().multiply(speed);
        hurt = new HashSet<>();
    }

    @Override
    public void progress() {
        if (this.player.isDead() || !this.player.isOnline()) {
            this.remove();
            return;
        } else if (GeneralMethods.isRegionProtectedFromBuild(this, location)) {
            this.remove();
            return;
        }else if (!this.bPlayer.canBendIgnoreBindsCooldowns(this)) {
            this.remove();
        } else if (this.location.distanceSquared(this.origin) >= (this.distance * this.distance)) {
            this.remove();
        }else if (location.getBlock().getType().isSolid()){
            remove();
            return;
        } else {
            for (int x = 0; x < circles; x++) {
                for (int i = 0; i < helixes; i++) {
                    double angle = step * radials + (2 * Math.PI * i / helixes);
                    Vector v = new Vector(Math.cos(angle) * radius, step * grow, Math.sin(angle) * radius);
                    rotateAroundAxisX(v, (location.getPitch() + 90) * degreesToRadians);
                    rotateAroundAxisY(v, -location.getYaw() * degreesToRadians);

                    location.add(v);
                    playAirbendingParticles(location,particles);
                    location.subtract(v);
                }
                step++;
            }
            this.location.add(this.direction);

            for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(this.location, hitbox)) {
                if (entity.getEntityId() == player.getEntityId()) continue;
                if (entity instanceof LivingEntity) {
                    if (!this.hurt.contains(entity)) {
                        DamageHandler.damageEntity(entity, this.damage, this);
                        this.hurt.add(entity);

                    }

                }
                GeneralMethods.setVelocity(this,entity,direction.clone().multiply(this.knockback));
                this.remove();
            }
        }
    }

    public static final Vector rotateAroundAxisX(Vector v, double angle) {
        double y, z, cos, sin;
        cos = Math.cos(angle);
        sin = Math.sin(angle);
        y = v.getY() * cos - v.getZ() * sin;
        z = v.getY() * sin + v.getZ() * cos;
        return v.setY(y).setZ(z);
    }

    public static final Vector rotateAroundAxisY(Vector v, double angle) {
        double x, z, cos, sin;
        cos = Math.cos(angle);
        sin = Math.sin(angle);
        x = v.getX() * cos + v.getZ() * sin;
        z = v.getX() * -sin + v.getZ() * cos;
        return v.setX(x).setZ(z);
    }

    @Override
    public void remove() {
        super.remove();
        bPlayer.addCooldown(this,cooldown);
        hurt.clear();
    }

    @Override
    public boolean isSneakAbility() {
        return false;
    }

    @Override
    public boolean isHarmlessAbility() {
        return false;
    }

    @Override
    public long getCooldown() {
        return cooldown;
    }

    @Override
    public String getName() {
        return "AirTendril";
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public void load() {
        final FileConfiguration config = ConfigManager.getConfig();
        config.addDefault("ExtraAbilities.Sammycocobear.AirTendril.Circles",2);
        config.addDefault("ExtraAbilities.Sammycocobear.AirTendril.Helixes",3);
        config.addDefault("ExtraAbilities.Sammycocobear.AirTendril.Radius",2);
        config.addDefault("ExtraAbilities.Sammycocobear.AirTendril.Cooldown",15000);
        config.addDefault("ExtraAbilities.Sammycocobear.AirTendril.Damage",2);
        config.addDefault("ExtraAbilities.Sammycocobear.AirTendril.Distance",20);
        config.addDefault("ExtraAbilities.Sammycocobear.AirTendril.Knockback",20);
        config.addDefault("ExtraAbilities.Sammycocobear.AirTendril.Hitbox",.85);
        config.addDefault("ExtraAbilities.Sammycocobear.AirTendril.Particles",20);
        config.addDefault("ExtraAbilities.Sammycocobear.AirTendril.Speed",2);

    }

    @Override
    public void stop() {

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
    public Object createNewComboInstance(Player player) {
        return new AirTendril(player);
    }


    @Override
    public String getInstructions() {
        return "AirBlast(Tap Shift)->AirBurst(Left Click)";
    }

    @Override
    public String getDescription() {
        return "Airbenders are able to manipulate air into multiple streams. If hit by these streams, it will cause massive knockback";
    }

    @Override
    public ArrayList<ComboManager.AbilityInformation> getCombination() {
        return new ArrayList(Arrays.asList(
                new ComboManager.AbilityInformation("AirBlast", ClickType.SHIFT_DOWN),
                new ComboManager.AbilityInformation("AirBlast", ClickType.SHIFT_UP),
                new ComboManager.AbilityInformation("AirBurst", ClickType.LEFT_CLICK)));
    }
}
