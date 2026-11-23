package io.github.greenmc.easymute;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;

public class EasyMuteConfigScreen {

    public static Screen create(Screen parentScreen) {
        ConfigCategory.Builder categoryBuilder = ConfigCategory.createBuilder()
                .name(Component.translatable("text.easymute.category_title"))
                .tooltip(Component.translatable("text.easymute.category_tooltip"));

        for (SoundSource source : SoundSource.values()) {
            Component sourceName = Component.translatable("soundCategory." + source.getName());

            categoryBuilder.option(Option.<Boolean>createBuilder()
                    .name(sourceName)
                    .description(OptionDescription.of(Component.translatable("text.easymute.setting_tooltip", sourceName)))
                    .binding(
                            source == SoundSource.MASTER,
                            () -> EasyMuteConfig.categoriesToMute.getOrDefault(source, false),
                            newVal -> EasyMuteConfig.categoriesToMute.put(source, newVal)
                    )
                    .controller(TickBoxControllerBuilder::create)
                    .build());
        }

        ConfigCategory generalCategory = ConfigCategory.createBuilder()
                .name(Component.translatable("text.easymute.general_title"))
                .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("text.easymute.mute_in_background"))
                        .description(OptionDescription.of(Component.translatable("text.easymute.mute_in_background_tooltip")))
                        .binding(
                                false,
                                () -> EasyMuteConfig.muteInBackground,
                                newVal -> EasyMuteConfig.muteInBackground = newVal
                        )
                        .controller(TickBoxControllerBuilder::create)
                        .build())
                .build();

        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("text.easymute.mod_menu_title"))
                .category(generalCategory)
                .category(categoryBuilder.build())
                .save(EasyMuteConfig::save)
                .build()
                .generateScreen(parentScreen);
    }

}
