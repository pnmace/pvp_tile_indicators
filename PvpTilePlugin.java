/*
 * Copyright (c) 2021, Parker Mace <https://github.com/pnmace>
 * Copyright (c) 2018, Tomas Slusny <slusnucky@gmail.com>
 * Copyright (c) 2016-2018, Adam <Adam@sigterm.info>
 * Copyright (c) 2018, Jordan Atwood <jordan.atwood423@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.pvptile;

import com.google.inject.Provides;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.events.InteractingChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;

@PluginDescriptor(
        name = "PvP Tile Indicators",
        description = "Highlights the location of your target's true tile in PvP scenarios",
        tags = {"pvp", "highlight", "overlay", "true tile"},
        enabledByDefault = false
)

public class PvpTilePlugin extends Plugin
{
    private static final Duration WAIT = Duration.ofSeconds(9);
    private Actor currentOpponent = null;
    private Instant time = Instant.now();

    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private PvpTileOverlay overlay;

    @Provides
    PvpTileConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(PvpTileConfig.class);
    }

    @Override
    protected void startUp() throws Exception
    {
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() throws Exception
    {
        currentOpponent = null;
        time = Instant.now();
        overlayManager.remove(overlay);
    }

    @Subscribe
    public void onInteractingChanged(InteractingChanged event)
    {
        if (event.getSource() != client.getLocalPlayer())
        {
            return;
        }

        Actor opponent = event.getTarget();

        if (opponent == null)
        {
            time = Instant.now();
            return;
        }

        currentOpponent = opponent;
    }

    public Actor getOpponent()
    {
        if (Duration.between(time, Instant.now()).compareTo(WAIT) > 0)
        {
            currentOpponent = null;
        }
        return currentOpponent;
    }
}
