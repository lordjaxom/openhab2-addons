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
import org.openhab.binding.maxcul.internal.device.MaxDeviceType;

/**
 * The {@link PairPingMoritzMessage} class represents the PAIR_PING MAX! radio message.
 *
 * @author Sascha Volkenandt - Initial contribution
 */
@NonNullByDefault
public class PairPingMoritzMessage extends AbstractMoritzMessage implements MoritzMessage.Incoming {

    private final int firmware;
    private final MaxDeviceType type;
    private final int testresult;
    private final String serial;

    static PairPingMoritzMessage parse(int messageId, String sourceAddress, String destAddress, String payload) {
        int firmware = Integer.parseInt(payload.substring(0, 2), 16);
        MaxDeviceType type = MaxDeviceType.findById(Integer.parseInt(payload.substring(2, 4), 16));
        int testresult = Integer.parseInt(payload.substring(4, 6), 16);
        String serial = payload.substring(6);
        return new PairPingMoritzMessage(messageId, sourceAddress, destAddress, firmware, type, testresult, serial);
    }

    private PairPingMoritzMessage(int messageId, String sourceAddress, String destAddress, int firmware,
            MaxDeviceType type, int testresult, String serial) {
        super(messageId, MoritzMessageType.PAIR_PING, sourceAddress, destAddress);
        this.firmware = firmware;
        this.type = type;
        this.testresult = testresult;
        this.serial = serial;
    }

    public int getFirmware() {
        return firmware;
    }

    public MaxDeviceType getType() {
        return type;
    }

    public int getTestresult() {
        return testresult;
    }

    public String getSerial() {
        return serial;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        sb.append("firmware: ").append(firmware);
        sb.append(" type: ").append(type);
        sb.append(" testresult: ").append(testresult);
        sb.append(" serial: ").append(serial);
    }
}
