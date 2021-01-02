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

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MoritzMessageParser} class provides static methods to parse incoming MAX! radio messages.
 *
 * @author Sascha Volkenandt - Initial contribution
 */
@NonNullByDefault
public class MoritzMessageParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(MoritzMessageParser.class);

    private static final Pattern MESSAGE = Pattern.compile(
            "Z([0-9A-F]{2})([0-9A-F]{2})([0-9A-F]{2})([0-9A-F]{2})([0-9A-F]{6})([0-9A-F]{6})([0-9A-F]{2})([0-9A-F]*)([0-9A-F]{2})");

    public static Optional<MoritzMessage.Incoming> parse(String message) {
        Matcher matcher = MESSAGE.matcher(message);
        if (!matcher.matches()) {
            return Optional.empty();
        }

        int length = Integer.parseInt(matcher.group(1), 16);
        if (message.length() != length * 2 + 5) { // +5 is +1 for "Z", +2 for length and +2 for rssi
            LOGGER.warn("Length mismatch in message '{}'", message);
            return Optional.empty();
        }

        int typeId = Integer.parseInt(matcher.group(4), 16);
        MoritzMessageType messageType = MoritzMessageType.findById(typeId);
        int messageId = Integer.parseInt(matcher.group(2), 16);
        String sourceAddress = matcher.group(5);
        String destAddress = matcher.group(6);
        String payload = matcher.group(8);
        return messageType.parse(messageId, sourceAddress, destAddress, payload);
    }

    private MoritzMessageParser() {
    }
}
