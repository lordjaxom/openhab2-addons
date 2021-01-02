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
package org.openhab.binding.maxcul.internal.command;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link CulCommand} interface provides methods for implementing a command-retry-response workflow when
 * communicating with
 * the CUL and other MAX! devices.
 *
 * @author Sascha Volkenandt - Initial contribution
 */
@NonNullByDefault
public interface CulCommand {

    void start();

    void cancel();

    boolean receive(String message);

    boolean similarTo(CulCommand o);
}
