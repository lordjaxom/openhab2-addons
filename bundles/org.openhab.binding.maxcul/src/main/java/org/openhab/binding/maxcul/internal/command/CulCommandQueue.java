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

import java.util.Deque;
import java.util.LinkedList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link CulCommandQueue} implements a thread-safe queue for {@link CulCommand}s.
 *
 * @author Sascha Volkenandt - Initial contribution
 */
@NonNullByDefault
public class CulCommandQueue {

    private final Deque<CulCommand> commandQueue = new LinkedList<>();

    private @Nullable CulCommand pendingCommand;

    public synchronized void enqueue(CulCommand command) {
        // newly added command takes precedence
        commandQueue.removeIf(command::similarTo);
        commandQueue.addLast(command);
        advance(false);
    }

    public synchronized void postpone(CulCommand command) {
        // newer similar command takes precedence
        if (commandQueue.stream().noneMatch(command::similarTo)) {
            commandQueue.addLast(command);
        }
        advance(true);
    }

    public synchronized void advance(boolean lastCommandFinished) {
        if ((lastCommandFinished || pendingCommand == null) //
                && (pendingCommand = commandQueue.pollFirst()) != null) {
            pendingCommand.start();
        }
    }

    public synchronized boolean receive(String data) {
        return pendingCommand != null && pendingCommand.receive(data);
    }

    public synchronized void clear() {
        if (pendingCommand != null) {
            pendingCommand.cancel();
            pendingCommand = null;
        }
        commandQueue.clear();
    }

    /**
     * @deprecated TODO only for debugging
     */
    @Deprecated
    public synchronized Object tracingInfo() {
        return new Object() {
            @Override
            public String toString() {
                StringBuilder sb = new StringBuilder();
                sb.append("has ").append(commandQueue.size()).append(" entries");
                if (pendingCommand != null) {
                    sb.append(", pending command is ").append(pendingCommand);
                }
                return sb.toString();
            }
        };
    }
}
