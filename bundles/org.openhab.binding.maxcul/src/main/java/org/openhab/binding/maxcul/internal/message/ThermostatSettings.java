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
 * The {@link ThermostatSettings} class represents the thermostat settings in the ACK and THERMOSTAT_STATE MAX! radio
 * messages.
 *
 * @author Sascha Volkenandt - Initial contribution
 */
@NonNullByDefault
public class ThermostatSettings {

    static ThermostatSettings parse(String payload) {
        int data = Integer.parseInt(payload.substring(0, 2), 16);
        ThermostatMode mode = ThermostatMode.findById(data & 0x03);
        boolean locked = (data & 0x20) > 0;
        boolean rfError = (data & 0x40) > 0;
        boolean batteryLow = (data & 0x80) > 0;
        int valve = Integer.parseInt(payload.substring(2, 4), 16);
        double desiredTemperature = (Integer.parseInt(payload.substring(4, 6), 16) & 0x7f) / 2.0;
        return new ThermostatSettings(mode, locked, rfError, batteryLow, valve, desiredTemperature);
    }

    private final ThermostatMode mode;
    private final boolean locked;
    private final boolean rfError;
    private final boolean batteryLow;
    private final int valve;
    private final double desiredTemperature;

    private ThermostatSettings(ThermostatMode mode, boolean locked, boolean rfError, boolean batteryLow, int valve,
            double desiredTemperature) {
        this.mode = mode;
        this.locked = locked;
        this.rfError = rfError;
        this.batteryLow = batteryLow;
        this.valve = valve;
        this.desiredTemperature = desiredTemperature;
    }

    public ThermostatMode getMode() {
        return mode;
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean isRfError() {
        return rfError;
    }

    public boolean isBatteryLow() {
        return batteryLow;
    }

    public int getValve() {
        return valve;
    }

    public double getDesiredTemperature() {
        return desiredTemperature;
    }

    protected void appendToString(StringBuilder sb) {
        sb.append("mode: ").append(mode);
        sb.append(" valve: ").append(valve);
        sb.append(" desired: ").append(String.format("%.1f", desiredTemperature));
        sb.append(rfError ? " rfError" : "");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        appendToString(sb);
        return sb.toString();
    }
}
