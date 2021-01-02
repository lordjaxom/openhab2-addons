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

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AbstractMoritzMessage} class provides basic functionality common to all MAX! radio messages.
 *
 * @author Sascha Volkenandt - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractMoritzMessage implements MoritzMessage {

    private static final AtomicInteger LAST_MESSAGE_ID = new AtomicInteger(-1);

    public static int lastMessageId() {
        return LAST_MESSAGE_ID.get();
    }

    private static int nextMessageId() {
        return LAST_MESSAGE_ID.updateAndGet(id -> id < 255 ? id + 1 : 0);
    }

    private final int messageId;
    private final MoritzMessageType messageType;
    private final String sourceAddress;
    private final String destAddress;

    protected AbstractMoritzMessage(int messageId, MoritzMessageType messageType, String sourceAddress,
            String destAddress) {
        this.messageId = messageId;
        this.messageType = messageType;
        this.sourceAddress = sourceAddress;
        this.destAddress = destAddress;
    }

    protected AbstractMoritzMessage(MoritzMessageType messageType, String sourceAddress, String destAddress) {
        this(nextMessageId(), messageType, sourceAddress, destAddress);
    }

    @Override
    public int getMessageId() {
        return messageId;
    }

    @Override
    public MoritzMessageType getMessageType() {
        return messageType;
    }

    @Override
    public String getSourceAddress() {
        return sourceAddress;
    }

    @Override
    public String getDestAddress() {
        return destAddress;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append(" id: ")
                .append(String.format("%03d", messageId)).append(" src: ").append(sourceAddress).append(" dst: ")
                .append(destAddress).append(' ');
        appendToString(sb);
        return sb.toString();
    }

    protected void appendToString(StringBuilder sb) {
        throw new UnsupportedOperationException("appendToString");
    }
}
