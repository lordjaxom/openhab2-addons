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

import static org.openhab.binding.maxcul.internal.utils.Checks.requireNonNull;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.maxcul.internal.bridge.MaxCulTransceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VersionCulCommand} class implements the workflow necessary negotiate the culfw version with the CUL.
 *
 * @author Sascha Volkenandt - Initial contribution
 */
@NonNullByDefault
public class VersionCulCommand implements CulCommand {

    private static final Duration TIMEOUT = Duration.ofSeconds(3);
    private static final Pattern RESPONSE = Pattern.compile("V (\\d+)\\.(\\d+)(?:\\.\\d+)? (.+?)(?: .+)?");
    private static final String FIRMWARE_ACUL = "a-culfw";

    private final Logger logger = LoggerFactory.getLogger(VersionCulCommand.class);

    private final MaxCulTransceiver transceiver;
    private final ScheduledExecutorService scheduler;

    private int tries = 0;
    private @Nullable ScheduledFuture<?> future;

    public VersionCulCommand(MaxCulTransceiver transceiver, ScheduledExecutorService scheduler) {
        this.transceiver = transceiver;
        this.scheduler = scheduler;
    }

    @Override
    public synchronized void start() {
        if (tries == 3) {
            logger.error("{} timeout(s) waiting for version from CUL, giving up", tries);
            transceiver.error("Cannot perform handshake with CUL!");
            return;
        }
        if (tries > 0) {
            logger.debug("{} timeout(s) waiting for version from CUL, trying again", tries);
        }
        tries++;

        transceiver.send("V");
        future = scheduler.schedule(this::start, TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public synchronized void cancel() {
        if (future != null) {
            requireNonNull(future).cancel(true);
        }
    }

    @Override
    public synchronized boolean receive(String data) {
        Matcher matcher = RESPONSE.matcher(data);
        if (!matcher.matches()) {
            return false;
        }

        cancel();

        int version = !matcher.group(3).equals(FIRMWARE_ACUL)
                ? Integer.parseInt(matcher.group(1)) * 100 + Integer.parseInt(matcher.group(2))
                : 154;
        transceiver.version(version);
        transceiver.advance();
        return true;
    }

    @Override
    public boolean similarTo(CulCommand o) {
        return o instanceof VersionCulCommand;
    }
}
