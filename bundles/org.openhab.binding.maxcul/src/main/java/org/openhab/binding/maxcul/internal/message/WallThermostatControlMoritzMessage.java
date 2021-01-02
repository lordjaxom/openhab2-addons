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
 * The {@link AckMoritzMessage} class represents the WALL_THERMOSTAT_CONTROL MAX! radio message.
 *
 * @author Sascha Volkenandt - Initial contribution
 */
@NonNullByDefault
public class WallThermostatControlMoritzMessage extends AbstractMoritzMessage implements MoritzMessage.Outgoing {

    private final double desiredTemperature;
    private final double measuredTemperature;

    public WallThermostatControlMoritzMessage(String sourceAddress, String destAddress, double desiredTemperature,
            double measuredTemperature) {
        super(MoritzMessageType.WALL_THERMOSTAT_CONTROL, sourceAddress, destAddress);
        this.desiredTemperature = desiredTemperature;
        this.measuredTemperature = measuredTemperature;
    }

    @Override
    public String payload() {
        int desiredTempData = (int) (desiredTemperature * 2.0);
        int measuredTempData = (int) (measuredTemperature * 10.0);
        return String.format("%04x",
                ((measuredTempData & 0x100) << 7) | (desiredTempData << 8) | (measuredTempData & 0xff));
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        sb.append("desired: ").append(String.format("%.1f", desiredTemperature));
        sb.append(" measured: ").append(String.format("%.1f", measuredTemperature));
    }
}
