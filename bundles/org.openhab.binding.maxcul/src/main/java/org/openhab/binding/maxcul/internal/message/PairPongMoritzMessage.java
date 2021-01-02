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
 * The {@link PairPongMoritzMessage} class represents the PAIR_PONG MAX! radio message.
 *
 * @author Sascha Volkenandt - Initial contribution
 */
@NonNullByDefault
public class PairPongMoritzMessage extends AbstractMoritzMessage implements MoritzMessage.Outgoing {

    public PairPongMoritzMessage(String sourceAddress, String destAddress) {
        super(MoritzMessageType.PAIR_PONG, sourceAddress, destAddress);
    }

    @Override
    public String payload() {
        return "00";
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        // nothing to do
    }
}
