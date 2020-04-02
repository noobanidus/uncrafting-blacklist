package doomanidus.mods.uncraftingblacklist.mixins;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModClassLoader;
import net.minecraftforge.fml.common.ModContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.transformer.MixinTransformer;
import org.spongepowered.asm.mixin.transformer.Proxy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.List;

/**
 * This code original licensed under the terms of the MIT License, which is compatible with the GPL
 * Originally taken from the mod JustEnoughIDs, taken from this URL: https://github.com/DimensionalDevelopment/JustEnoughIDs/blob/2beb73d38a169899de3e17df0b66cfa84cda01b1/src/main/java/org/dimdev/jeid/mixin/init/MixinLoader.java
 * Authorship of the file is visible here: https://github.com/DimensionalDevelopment/JustEnoughIDs/commits/master/src/main/java/org/dimdev/jeid/mixin/init/MixinLoader.java
**/
@Mixin(Loader.class)
public class MixinLoader {
  @Shadow
  private List<ModContainer> mods;
  @Shadow
  private ModClassLoader modClassLoader;

  /**
   * @reason Load all mods now and load mod support mixin configs. This can't be done later
   * since constructing mods loads classes from them.
   */
  @Inject(method = "loadMods", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/common/LoadController;transition(Lnet/minecraftforge/fml/common/LoaderState;Z)V", ordinal = 1), remap = false)
  private void beforeConstructingMods(List<String> injectedModContainers, CallbackInfo ci) {
    // Add all mods to class loader
    for (ModContainer mod : mods) {
      try {
        modClassLoader.addFile(mod.getSource());
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }

    // Add and reload mixin configs
    Mixins.addConfiguration("mixins.uncraftingblacklist.json");

    @SuppressWarnings("OptionalGetWithoutIsPresent") Proxy mixinProxy = (Proxy) Launch.classLoader.getTransformers().stream().filter(transformer -> transformer instanceof Proxy).findFirst().get();
    try {
      Field transformerField = Proxy.class.getDeclaredField("transformer");
      transformerField.setAccessible(true);
      MixinTransformer transformer = (MixinTransformer) transformerField.get(mixinProxy);

      Method selectConfigsMethod = MixinTransformer.class.getDeclaredMethod("selectConfigs", MixinEnvironment.class);
      selectConfigsMethod.setAccessible(true);
      selectConfigsMethod.invoke(transformer, MixinEnvironment.getCurrentEnvironment());

      Method prepareConfigsMethod = MixinTransformer.class.getDeclaredMethod("prepareConfigs", MixinEnvironment.class);
      prepareConfigsMethod.setAccessible(true);
      prepareConfigsMethod.invoke(transformer, MixinEnvironment.getCurrentEnvironment());
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }
}
