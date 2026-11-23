package io.github.greenmc.easymute;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;

public class EasyMuteSettingsMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parentScreen -> {

            ConfigCategory.Builder categoryBuilder = ConfigCategory.createBuilder()
                    .name(Component.translatable("text.easymute.category_title"))
                    .tooltip(Component.translatable("text.easymute.category_tooltip"));

            for (SoundSource source : SoundSource.values()) {
                categoryBuilder.option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("text.easymute.setting_name", source.getName()))
                        .description(OptionDescription.of(Component.translatable("text.easymute.setting_tooltip", source.getName())))
                        .binding(
                                source == SoundSource.MASTER,
                                () -> EasyMuteConfig.categoriesToMute.getOrDefault(source, false),
                                newVal -> EasyMuteConfig.categoriesToMute.put(source, newVal)
                        )
                        .controller(TickBoxControllerBuilder::create)
                        .build());
            }

            return YetAnotherConfigLib.createBuilder()
                    .title(Component.translatable("text.easymute.mod_menu_title"))
                    .category(categoryBuilder.build())
                    .save(EasyMuteConfig::save)
                    .build()
                    .generateScreen(parentScreen);
        };
    }

}
