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
 * The {@link LinkPartnerMoritzMessage} class represents the ADD_LINK_PARTNER and REMOVE_LINK_PARTNER MAX! radio
 * messages.
 *
 * @author Sascha Volkenandt - Initial contribution
 */
@NonNullByDefault
public class LinkPartnerMoritzMessage extends AbstractMoritzMessage implements MoritzMessage.Outgoing {

    private final String linkAddress;
    private final MaxDeviceType linkDeviceType;

    public LinkPartnerMoritzMessage(boolean link, String sourceAddress, String destAddress, String linkAddress,
            MaxDeviceType linkDeviceType) {
        super(link ? MoritzMessageType.ADD_LINK_PARTNER : MoritzMessageType.REMOVE_LINK_PARTNER, sourceAddress,
                destAddress);
        this.linkAddress = linkAddress;
        this.linkDeviceType = linkDeviceType;
    }

    @Override
    public String payload() {
        return String.format("%s%02x", linkAddress, linkDeviceType.getId());
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        sb.append("link: ").append(linkAddress);
        sb.append(" type: ").append(linkDeviceType);
    }
}
