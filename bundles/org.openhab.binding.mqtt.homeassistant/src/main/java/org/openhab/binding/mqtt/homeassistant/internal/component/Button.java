/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.mqtt.homeassistant.internal.component;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannelType;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractChannelConfiguration;
import org.openhab.core.thing.type.AutoUpdatePolicy;

import com.google.gson.annotations.SerializedName;

/**
 * An MQTT button, following the https://www.home-assistant.io/integrations/button.mqtt/ specification.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class Button extends AbstractComponent<Button.ChannelConfiguration> {
    public static final String BUTTON_CHANNEL_ID = "button";

    public static final String PAYLOAD_PRESS = "PRESS";

    private static final Map<String, String> COMMAND_LABELS = Map.of(PAYLOAD_PRESS, "@text/command.button.press");

    /**
     * Configuration class for MQTT component
     */
    static class ChannelConfiguration extends AbstractChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT Button");
        }

        protected @Nullable Boolean optimistic;

        @SerializedName("command_topic")
        protected @Nullable String commandTopic;

        @SerializedName("payload_press")
        protected String payloadPress = PAYLOAD_PRESS;
    }

    public Button(ComponentFactory.ComponentConfiguration componentConfiguration) {
        super(componentConfiguration, ChannelConfiguration.class);

        TextValue value = new TextValue(Map.of(), Map.of(PAYLOAD_PRESS, channelConfiguration.payloadPress), Map.of(),
                COMMAND_LABELS);

        buildChannel(BUTTON_CHANNEL_ID, ComponentChannelType.STRING, value, getName(),
                componentConfiguration.getUpdateListener())
                .commandTopic(channelConfiguration.commandTopic, channelConfiguration.isRetain(),
                        channelConfiguration.getQos())
                .withAutoUpdatePolicy(AutoUpdatePolicy.VETO).build();
        finalizeChannels();
    }
}
