package com.modularmc.registrate.test.gametests;

import com.modularmc.registrate.test.mod.TestMod;

import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;

@ForEachTest(groups = "registrate-runtime")
@SuppressWarnings("unused")
public class RegistrationRuntimeTests {

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Ensure block item lang reuse and fluid optional helpers behave as expected")
    public static void ensureItemAndFluidHelpers(final DynamicTest test) {
        test.onGameTest(helper -> helper.startSequence(() -> TestMod.instance().testblockitem.asStack().getItem().getDescriptionId())
                .thenExecute(blockItemDescriptionId -> {
                    helper.assertValueEqual(TestMod.instance().testblock.get().getDescriptionId(), blockItemDescriptionId, "Block item should reuse the block description key");
                    helper.assertValueEqual(true, TestMod.instance().testfluid.getBlock().isEmpty(), "Fluid entry should return an empty block optional when no sibling exists");
                    helper.assertValueEqual(true, TestMod.instance().testfluid.getBucket().isEmpty(), "Fluid entry should hide buckets when noBucket() is used");
                })
                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Ensure auxiliary registrations remain available at runtime")
    public static void ensureAuxiliaryRegistrations(final DynamicTest test) {
        test.onGameTest(helper -> helper.startSequence(() -> TestMod.instance().testmenu.getId().getPath())
                .thenExecute(menuId -> {
                    helper.assertValueEqual("testmenu", menuId, "Menu registration path");
                    helper.assertValueEqual("entity.testmod.testentity", TestMod.instance().testentity.get().getDescriptionId(), "Entity translation key");
                    helper.assertValueEqual(TestMod.instance().testentity.get(), SpawnEggItem.getType(TestMod.instance().testentitySpawnEgg.asStack()), "Spawn egg should resolve to the registered entity type");
                    helper.assertValueEqual("testitem", TestMod.instance().testduplicatename.getId().getPath(), "Duplicate-name entity registration path");
                    helper.assertValueEqual("testblockentity", TestMod.instance().testblockentity.getId().getPath(), "Standalone block entity registration path");
                    helper.assertValueEqual("testcustom", TestMod.instance().testcustom.getId().getPath(), "Custom registry entry path");
                    helper.assertValueEqual("block.testmod.magic_item_model", TestMod.instance().magicItemModelTest.asStack().getItem().getDescriptionId(), "Magic item-model block description key");
                })
                .thenSucceed());
    }
}
