/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.maxcul.internal.message;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ThermostatStateMoritzMessage} class represents the THERMOSTAT_STATE MAX! radio message.
 *
 * @author Sascha Volkenandt - Initial contribution
 */
@NonNullByDefault
public class ThermostatStateMoritzMessage extends AbstractMoritzMessage implements MoritzMessage.Incoming {

    static ThermostatStateMoritzMessage parse(int messageId, String sourceAddress, String destAddress, String payload) {
        ThermostatSettings settings = ThermostatSettings.parse(payload);
        double measuredTemperature = (Integer.parseInt(payload.substring(6, 10), 16) & 0x1ff) / 10.0;
        return new ThermostatStateMoritzMessage(messageId, sourceAddress, destAddress, settings, measuredTemperature);
    }

    private ThermostatStateMoritzMessage(int messageId, String sourceAddress, String destAddress,
            ThermostatSettings settings, double measuredTemperature) {
        super(messageId, MoritzMessageType.THERMOSTAT_STATE, sourceAddress, destAddress);
        this.settings = settings;
        this.measuredTemperature = measuredTemperature;
    }

    private final ThermostatSettings settings;
    private final double measuredTemperature;

    public ThermostatSettings getSettings() {
        return settings;
    }

    public double getMeasuredTemperature() {
        return measuredTemperature;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        settings.appendToString(sb);
        sb.append(" measured: ").append(String.format("%.1f", measuredTemperature));
    }
}
