package com.modularmc.registrate.test.gametests;

import com.modularmc.registrate.test.mod.TestMod;

import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;

@ForEachTest(groups = "custom-item")
@SuppressWarnings("unused")
public class CustomItemTests {

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Ensure custom item is registered and has the expected properties")
    public static void ensureItemContent(final DynamicTest test) {
        test.onGameTest(helper -> helper.startSequence(() -> TestMod.instance().testitem.asStack())
                .thenMap(stack -> stack.getItem().getDescriptionId())
                .thenExecute(descriptionId -> helper.assertValueEqual("item.testmod.testitem", descriptionId, "Test Item translation key"))
                .thenSucceed());
    }
}
