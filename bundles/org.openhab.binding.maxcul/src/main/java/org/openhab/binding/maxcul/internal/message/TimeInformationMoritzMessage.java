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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TimeInformationMoritzMessage} class represents the TIME_INFORMATION MAX! radio message.
 *
 * @author Sascha Volkenandt - Initial contribution
 */
@NonNullByDefault
public class TimeInformationMoritzMessage extends AbstractMoritzMessage
        implements MoritzMessage.Incoming, MoritzMessage.Outgoing {

    static final LocalDateTime EMPTY_DATE_TIME = LocalDateTime.of(1970, 1, 1, 0, 0);

    static TimeInformationMoritzMessage parse(int messageId, String sourceAddress, String destAddress, String payload) {
        LocalDateTime dateTime;
        if (!payload.isEmpty()) {
            int year = Integer.parseInt(payload.substring(0, 2), 16) + 2000;
            int day = Integer.parseInt(payload.substring(2, 4), 16);
            int hour = Integer.parseInt(payload.substring(4, 6), 16) & 0x1f;
            int byte4 = Integer.parseInt(payload.substring(6, 8), 16);
            int byte5 = Integer.parseInt(payload.substring(8, 10), 16);
            int minute = byte4 & 0x3f;
            int second = byte5 & 0x3f;
            int month = ((byte4 >> 6) << 2) | (byte5 >> 6); // this is just guessed
            dateTime = LocalDateTime.of(year, month, day, hour, minute, second);
        } else {
            dateTime = EMPTY_DATE_TIME;
        }
        return new TimeInformationMoritzMessage(messageId, sourceAddress, destAddress, dateTime);
    }

    private final LocalDateTime dateTime;

    TimeInformationMoritzMessage(String sourceAddress, String destAddress, LocalDateTime dateTime) {
        super(MoritzMessageType.TIME_INFORMATION, sourceAddress, destAddress);
        this.dateTime = dateTime;
    }

    public TimeInformationMoritzMessage(String sourceAddress, String destAddress) {
        this(sourceAddress, destAddress, EMPTY_DATE_TIME);
    }

    private TimeInformationMoritzMessage(int messageId, String sourceAddress, String destAddress,
            LocalDateTime dateTime) {
        super(messageId, MoritzMessageType.TIME_INFORMATION, sourceAddress, destAddress);
        this.dateTime = dateTime;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    @Override
    public String payload() {
        LocalDateTime sentDateTime = !EMPTY_DATE_TIME.equals(dateTime) ? dateTime : LocalDateTime.now();
        return String.format("%02x%02x%02x%02x%02x", sentDateTime.getYear() - 2000, sentDateTime.getDayOfMonth(),
                sentDateTime.getHour(), sentDateTime.getMinute() | ((sentDateTime.getMonthValue() & 0x0c) << 4),
                sentDateTime.getSecond() | ((sentDateTime.getMonthValue() & 0x03) << 6));
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        sb.append("time: ");
        if (!EMPTY_DATE_TIME.equals(dateTime)) {
            DateTimeFormatter.ISO_LOCAL_DATE_TIME.formatTo(dateTime, sb);
        } else {
            sb.append("empty");
        }
    }
}
