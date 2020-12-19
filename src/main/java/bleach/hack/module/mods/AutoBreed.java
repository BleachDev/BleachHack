//package bleach.hack.module.mods;
//
//import bleach.hack.module.Category;
//import bleach.hack.module.Module;
//import com.google.common.eventbus.Subscribe;
//import net.minecraft.entity.Entity;
//import net.minecraft.entity.passive.AnimalEntity;
//import net.minecraft.util.Hand;
//
//public class AutoBreed extends Module {
//    public AutoBreed() {
//        super("AutoBreed", KEY_UNBOUND, Category.WORLD, "auto breeds nearby animals");
//    }
//
//    @Subscribe
//    public void onTick() {
//        assert mc.world != null;
//        for (Entity e : mc.world.getEntities()) {
//            if (e instanceof AnimalEntity) {
//                final AnimalEntity animal = (AnimalEntity) e;
//                if (animal.getHealth() > 0) {
//                    if (!animal.isBaby() && !animal.isInLove() && mc.player.distanceTo(animal) <= 4.5f && animal.isBreedingItem(mc.player.inventory.getMainHandStack())) {
//                        mc.player.interact(animal, Hand.MAIN_HAND);
//                    }
//                }
//            }
//        }
//    }
//}