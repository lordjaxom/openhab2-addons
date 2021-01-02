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
 * The {@link AckMoritzMessage} class represents the ACK MAX! radio message.
 *
 * @author Sascha Volkenandt - Initial contribution
 */
@NonNullByDefault
public class AckMoritzMessage extends AbstractMoritzMessage implements MoritzMessage.Incoming {

    static AckMoritzMessage parse(int messageId, String sourceAddress, String destAddress, String payload) {
        boolean ok = (Integer.parseInt(payload.substring(0, 2), 16) & 0x80) == 0;
        ThermostatSettings settings = ThermostatSettings.parse(payload.substring(2));
        return new AckMoritzMessage(messageId, sourceAddress, destAddress, ok, settings);
    }

    private final boolean ok;
    private final ThermostatSettings settings;

    private AckMoritzMessage(int messageId, String sourceAddress, String destAddress, boolean ok,
            ThermostatSettings settings) {
        super(messageId, MoritzMessageType.ACK, sourceAddress, destAddress);
        this.ok = ok;
        this.settings = settings;
    }

    public boolean isOk() {
        return ok;
    }

    public ThermostatSettings getSettings() {
        return settings;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        sb.append(ok ? "ok " : "nok ");
        settings.appendToString(sb);
    }
}
