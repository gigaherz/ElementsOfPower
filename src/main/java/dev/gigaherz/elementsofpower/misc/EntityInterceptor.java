package dev.gigaherz.elementsofpower.misc;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Optional;

public class EntityInterceptor
{
    // Butchered from the player getMouseOver()
    public static HitResult getEntityIntercept(Player player, Level level, Vec3 start, Vec3 look, Vec3 end, HitResult mop)
    {
        double distance = end.distanceTo(start);

        if (mop.getType() != HitResult.Type.MISS)
        {
            distance = mop.getLocation().distanceTo(start);
        }

        Vec3 direction = new Vec3(
                look.x * distance,
                look.y * distance,
                look.z * distance);

        end = start.add(direction);

        Vec3 hitPosition = null;
        List<Entity> list = level.getEntities(player,
                player.getBoundingBox()
                        .expandTowards(direction.x, direction.y, direction.z)
                        .inflate(1.0, 1.0, 1.0),
                entity -> EntitySelector.NO_SPECTATORS.test(entity) && entity.isPickable());

        double distanceToEntity = distance;
        Entity pointedEntity = null;
        for (Entity entity : list)
        {
            double border = entity.getPickRadius();
            AABB bounds = entity.getBoundingBox().expandTowards(border, border, border);
            Optional<Vec3> intercept = bounds.clip(start, end);

            if (bounds.contains(start))
            {
                if (distanceToEntity >= 0.0D)
                {
                    pointedEntity = entity;
                    hitPosition = intercept.orElse(start);
                    distanceToEntity = 0.0D;
                }
            }
            else if (intercept.isPresent())
            {
                double interceptDistance = start.distanceTo(intercept.get());

                if (interceptDistance < distanceToEntity || distanceToEntity == 0.0D)
                {
                    if (entity == player.getVehicle() && !player.canRiderInteract())
                    {
                        if (distanceToEntity == 0.0D)
                        {
                            pointedEntity = entity;
                            hitPosition = intercept.get();
                        }
                    }
                    else
                    {
                        pointedEntity = entity;
                        hitPosition = intercept.get();
                        distanceToEntity = interceptDistance;
                    }
                }
            }
        }

        if (pointedEntity != null)
        {
            if (distanceToEntity < distance)
            {
                if (start.distanceTo(hitPosition) < distance)
                {
                    return new EntityHitResult(pointedEntity, hitPosition);
                }
            }
        }

        return mop;
    }

}
