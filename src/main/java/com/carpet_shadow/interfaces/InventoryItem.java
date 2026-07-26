package com.carpet_shadow.interfaces;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import java.util.Collection;

// FIXME [PORT 1.21.11] see PORTING_NOTES.md #6.
//  BlockEntity.readNbt(NbtCompound) doesn't exist since 1.21.6 (replaced by the internal
//  readData(ReadView) override point). This static helper is currently UNREACHABLE - the 5
//  mixins that called it (BlockDataObjectMixin, BlockItemMixin, BlockStateArgumentMixin,
//  CloneCommandMixin, FallingBlockEntityMixin) have been disabled in carpet-shadow.mixins.json
//  because their @Redirect targets are equally stale and would crash game startup
//  (defaultRequire = 1). Before re-enabling them: find the actual 1.21.11 public entry point
//  those vanilla methods now use to load a BlockEntity from a raw NbtCompound (likely wraps the
//  compound into a ReadView internally), and update both this helper and all 5 @At(target=...)
//  descriptors to match.
public interface InventoryItem {

    Collection<Inventory> carpet_shadow$getInventories();

    void carpet_shadow$addSlot(Inventory inventory, int slot);

    void carpet_shadow$removeSlot(Inventory inventory, int slot);

    static void readNbt(BlockEntity instance, NbtCompound nbt) {
        if (instance instanceof Inventory inv) {
            try {
                for (int index = 0; index < inv.size(); index++) {
                    ItemStack stack = inv.getStack(index);
                    if (((ShadowItem) (Object) stack).carpet_shadow$getShadowId() != null) {
                        ((InventoryItem) (Object) stack).carpet_shadow$removeSlot(inv, index);
                    }
                }
            }catch (Exception ignored){}

            // FIXME [PORT 1.21.11]: instance.readNbt(nbt) no longer exists - see class comment above.
            // Left uncalled for now since this whole method is currently unreachable.

            try {
                for (int index = 0; index < inv.size(); index++) {
                    ItemStack stack = inv.getStack(index);
                    if (((ShadowItem) (Object) stack).carpet_shadow$getShadowId() != null) {
                        ((InventoryItem) (Object) stack).carpet_shadow$addSlot(inv, index);
                    }
                }
            }catch (Exception ignored){}
        }
        // FIXME [PORT 1.21.11]: else-branch instance.readNbt(nbt) also removed - see class comment above.
    }
}
