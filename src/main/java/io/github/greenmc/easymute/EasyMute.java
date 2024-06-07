package io.github.greenmc.easymute;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EasyMute implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("easymute");

	@Override
	public void onInitialize() {
		LOGGER.info("EasyMute initialized.");
	}

}