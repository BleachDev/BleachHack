package bleach.hack.module.mods;

import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.RenderUtils;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpellParticle;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public class StrengthESP extends Module
{
    private final List<List<Float>> effects = Arrays.asList(Arrays.asList(0.5764706F, 0.14117648F, 0.13725491F), Arrays.asList(0.48235294F, 0.3137255F, 0.4627451F), Arrays.asList(0.4F, 0.3019608F, 0.4117647F), Arrays.asList(0.65882355F, 0.28627452F, 0.18039216F), Arrays.asList(0.6666667F, 0.34509805F, 0.1764706F), Arrays.asList(0.7137255F, 0.44705883F, 0.2F), Arrays.asList(0.5411765F, 0.3882353F, 0.43137255F), Arrays.asList(0.49019608F, 0.39215687F, 0.38431373F), Arrays.asList(0.72156864F, 0.44313726F, 0.20784314F), Arrays.asList(0.5137255F, 0.3529412F, 0.44705883F), Arrays.asList(0.4509804F, 0.3529412F, 0.39607844F), Arrays.asList(0.69803923F, 0.38039216F, 0.19607843F));
    private final HashMap<Entity, Integer> players = new HashMap<>();

    public StrengthESP()
    {
        super("StrengthESP", 0, Category.RENDER, "Shows people with strength (only works with particles on)", null);
    }

    public void onUpdate()
    {
        if (this.mc.currentScreen == null)
        {
            for (Map.Entry<Entity, Integer> entityIntegerEntry : (new HashMap<>(this.players)).entrySet())
            {
                if (entityIntegerEntry.getValue() <= 0)
                {
                    this.players.remove(entityIntegerEntry.getKey());
                } else
                {
                    this.players.replace(entityIntegerEntry.getKey(), entityIntegerEntry.getValue() - 1);
                }
            }

            if (this.mc.world.getPlayers().size() > 1)
            {
                try
                {
                    int count = 0;
                    int playerCount = 0;

                    ArrayDeque[][] deques = (ArrayDeque[][]) ReflectUtils.getField(ParticleManager.class, "fxLayers", "fxLayers").get(this.mc.effectRenderer);

                    for (ArrayDeque[] p2 : deques)
                    {
                        label82:

                        for (ArrayDeque p1 : p2)
                        {
                            Iterator iter = p1.iterator();

                            label79:

                            do
                            {
                                while (true)
                                {
                                    Particle p;
                                    do
                                    {
                                        do
                                        {
                                            if (!iter.hasNext())
                                            {
                                                continue label82;
                                            }

                                            p = (Particle) iter.next();
                                        } while (p == null);
                                    } while (!(p instanceof SpellParticle));

                                    if (count > 250)
                                    {
                                        return;
                                    }

                                    ++count;
                                    Vec3d pos = new Vec3d((Double) Objects.requireNonNull(ReflectUtils.getField(Particle.class, "posX", "posX")).get(p), (Double) Objects.requireNonNull(ReflectUtils.getField(Particle.class, "posY", "posY")).get(p), (Double) Objects.requireNonNull(ReflectUtils.getField(Particle.class, "posZ", "posZ")).get(p));

                                    for (net.minecraft.entity.player.PlayerEntity entityPlayer : this.mc.world.getPlayers())
                                    {
                                        if ( entityPlayer != this.mc.player && !this.players.containsKey(entityPlayer) && pos.distanceTo((entityPlayer).getPos()) < 2.0D && this.effects.contains(Arrays.asList(p.getRedColorF(), p.getGreenColorF(), p.getBlueColorF())))
                                        {
                                            this.players.put(entityPlayer, 10);
                                            ++playerCount;

                                            continue label79;
                                        }
                                    }
                                }
                            } while (playerCount < this.mc.world.getPlayers().size() - 1);

                            return;
                        }
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public void onRender()
    {
        for (Map.Entry<Entity, Integer> entityIntegerEntry : this.players.entrySet())
        {
            RenderUtils.drawFilledBox(entityIntegerEntry.getKey().getBoundingBox(), 1.0F, 0.0F, 0.0F, 0.3F);
        }
    }
}