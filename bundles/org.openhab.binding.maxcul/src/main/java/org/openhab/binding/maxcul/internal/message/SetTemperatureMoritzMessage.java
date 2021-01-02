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
import org.openhab.binding.maxcul.internal.device.ThermostatMode;

/**
 * The {@link AckMoritzMessage} class represents the SET_TEMPERATURE MAX! radio message.
 *
 * @author Sascha Volkenandt - Initial contribution
 */
@NonNullByDefault
public class SetTemperatureMoritzMessage extends AbstractMoritzMessage
        implements MoritzMessage.Outgoing, MoritzMessage.Incoming {

    static SetTemperatureMoritzMessage parse(int messageId, String sourceAddress, String destAddress, String payload) {
        int data = Integer.parseInt(payload.substring(0, 2), 16);
        ThermostatMode thermostatMode = ThermostatMode.findById((data >> 6) & 0x03);
        double desiredTemperature = (data & 0x3f) / 2.0;
        return new SetTemperatureMoritzMessage(messageId, sourceAddress, destAddress, thermostatMode,
                desiredTemperature);
    }

    private final ThermostatMode mode;
    private final double desiredTemperature;

    public SetTemperatureMoritzMessage(String sourceAddress, String destAddress, ThermostatMode mode,
            double desiredTemperature) {
        super(MoritzMessageType.SET_TEMPERATURE, sourceAddress, destAddress);
        this.mode = mode;
        this.desiredTemperature = desiredTemperature;
    }

    private SetTemperatureMoritzMessage(int messageId, String sourceAddress, String destAddress, ThermostatMode mode,
            double desiredTemperature) {
        super(messageId, MoritzMessageType.SET_TEMPERATURE, sourceAddress, destAddress);
        this.mode = mode;
        this.desiredTemperature = desiredTemperature;
    }

    public ThermostatMode getMode() {
        return mode;
    }

    public double getDesiredTemperature() {
        return desiredTemperature;
    }

    @Override
    public String payload() {
        return String.format("%02x", (mode.getId() << 6) | (int) (desiredTemperature * 2.0));
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        sb.append("mode: ").append(mode);
        sb.append(" desired: ").append(String.format("%.1f", desiredTemperature));
    }
}
