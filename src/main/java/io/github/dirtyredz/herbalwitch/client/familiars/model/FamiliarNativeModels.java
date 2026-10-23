package io.github.dirtyredz.herbalwitch.client.familiars.model;

import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.ambient.Bat;

/** Reset before vanilla preparation so cosmetic poses never accumulate or survive a state change. */
public final class FamiliarNativeModels {
    private FamiliarNativeModels() {}
    public static CatModel<Cat> cat(ModelPart root) { return new CatModel<>(root) {
        @Override public void prepareMobModel(Cat e,float limb,float amount,float partial){FamiliarNativePose.reset(root);super.prepareMobModel(e,limb,amount,partial);}
        @Override public void setupAnim(Cat e,float limb,float amount,float age,float yaw,float pitch){super.setupAnim(e,limb,amount,age,yaw,pitch);FamiliarNativePose.apply(root,"cat",e);}
    };}
    public static FoxModel<Fox> fox(ModelPart root) { return new FoxModel<>(root) {
        @Override public void prepareMobModel(Fox e,float limb,float amount,float partial){FamiliarNativePose.reset(root);super.prepareMobModel(e,limb,amount,partial);}
        @Override public void setupAnim(Fox e,float limb,float amount,float age,float yaw,float pitch){super.setupAnim(e,limb,amount,age,yaw,pitch);FamiliarNativePose.apply(root,"fox",e);}
    };}
    public static WolfModel<Wolf> wolf(ModelPart root) { return new WolfModel<>(root) {
        @Override public void prepareMobModel(Wolf e,float limb,float amount,float partial){FamiliarNativePose.reset(root);super.prepareMobModel(e,limb,amount,partial);}
        @Override public void setupAnim(Wolf e,float limb,float amount,float age,float yaw,float pitch){super.setupAnim(e,limb,amount,age,yaw,pitch);FamiliarNativePose.apply(root,"wolf",e);}
    };}
    public static RabbitModel<Rabbit> rabbit(ModelPart root) { return new RabbitModel<>(root) {
        @Override public void prepareMobModel(Rabbit e,float limb,float amount,float partial){FamiliarNativePose.reset(root);super.prepareMobModel(e,limb,amount,partial);}
        @Override public void setupAnim(Rabbit e,float limb,float amount,float age,float yaw,float pitch){super.setupAnim(e,limb,amount,age,yaw,pitch);FamiliarNativePose.apply(root,"rabbit",e);}
    };}
    public static BatModel bat(ModelPart root) { return new BatModel(root) {
        @Override public void prepareMobModel(Bat e,float limb,float amount,float partial){FamiliarNativePose.reset(root);super.prepareMobModel(e,limb,amount,partial);}
        @Override public void setupAnim(Bat e,float limb,float amount,float age,float yaw,float pitch){super.setupAnim(e,limb,amount,age,yaw,pitch);FamiliarNativePose.apply(root,"bat",e);}
    };}
}
