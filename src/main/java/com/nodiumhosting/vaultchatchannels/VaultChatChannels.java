package com.nodiumhosting.vaultchatchannels;

import com.mojang.logging.LogUtils;
import com.nodiumhosting.vaultchatchannels.command.ChatGroupCommand;
import com.nodiumhosting.vaultchatchannels.command.VaultChatCommand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod("vaultchatchannels")
public class VaultChatChannels {
    public static final Logger LOGGER = LogUtils.getLogger();

    public VaultChatChannels() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {

    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void registerCommands(RegisterCommandsEvent event) {
            VaultChatCommand.register(event.getDispatcher());
            ChatGroupCommand.register(event.getDispatcher());
        }
    }
}
