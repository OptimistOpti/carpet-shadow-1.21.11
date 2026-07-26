package com.carpet_shadow.mixins.general;

import com.carpet_shadow.interfaces.ShadowItem;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// FIXME [PORT 1.21.11] see PORTING_NOTES.md #4.
//  "method_331" is an intermediary (unnamed) method reference from 1.20.1 - it is NOT guaranteed to
//  point at the same method (or even exist) in 1.21.11's intermediary mappings, since loot table
//  generation/shulker-box-drop logic may have moved or been rewritten. Decompile LootTable for
//  1.21.11 and find the method that copies stacks with a set count while generating loot, then
//  re-target both the @Mixin method name and the ItemStack;copyWithCount descriptor if it changed.
@Mixin(LootTable.class)
public class LootTableMixin {

    @WrapOperation(method = "method_331", at=@At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;copyWithCount(I)Lnet/minecraft/item/ItemStack;"))
    private static ItemStack fix_survival_shulkers(ItemStack instance, int count, Operation<ItemStack> original){
        String shadowId = ((ShadowItem)(Object)instance).carpet_shadow$getShadowId();
        if (shadowId != null){
            if (instance.getCount() == count){
                return instance;
            }else if (count < instance.getMaxCount()) {
                instance.setCount(count);
                return instance;
            }
        }
        return original.call(instance, count);
    }
}
