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
 * The {@link MoritzMessage} interface provides methods to handle MAX! radio messages.
 *
 * @author Sascha Volkenandt - Initial contribution
 */
@NonNullByDefault
public interface MoritzMessage {

    int getMessageId();

    MoritzMessageType getMessageType();

    String getSourceAddress();

    String getDestAddress();

    @NonNullByDefault
    interface Incoming extends MoritzMessage {
    }

    @NonNullByDefault
    interface Outgoing extends MoritzMessage {

        String payload();

        default String message() {
            String messageWithoutLength = String.format("%02x00%02x%s%s00%s", getMessageId(), getMessageType().getId(),
                    getSourceAddress(), getDestAddress(), payload());
            return String.format("%02x%s", messageWithoutLength.length() / 2, messageWithoutLength).toUpperCase();
        }
    }
}
