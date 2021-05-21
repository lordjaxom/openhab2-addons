/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.transform.multipress.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration that holds the profile's configuration values
 *
 * @author Sascha Volkenandt - initial contribution
 *
 */
@NonNullByDefault
public class MultiPressConfiguration {

    public String off = "OFF";

    public String on = "ON";

    public int shortDelay = 200;

    public int longDelay = 1000;
}
