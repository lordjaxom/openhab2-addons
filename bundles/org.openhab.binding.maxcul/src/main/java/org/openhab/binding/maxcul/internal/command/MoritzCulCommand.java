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
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.maxcul.internal.bridge.MaxCulTransceiver;
import org.openhab.binding.maxcul.internal.message.AckMoritzMessage;
import org.openhab.binding.maxcul.internal.message.MoritzMessage;
import org.openhab.binding.maxcul.internal.message.MoritzMessageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MoritzCulCommand} class implements the workflow necessary to send and acknowledge an actual radio message
 * to another
 * MAX! device.
 *
 * @author Sascha Volkenandt - Initial contribution
 */
@NonNullByDefault
public class MoritzCulCommand implements CulCommand {

    private static final Duration TIMEOUT_CREDIT = Duration.ofSeconds(1);
    private static final Duration TIMEOUT_ACK = Duration.ofSeconds(3);
    private static final Pattern RESPONSE_CREDIT = Pattern.compile(".. +(\\d+)");

    private final Logger logger = LoggerFactory.getLogger(MoritzCulCommand.class);

    private final MaxCulTransceiver transceiver;
    private final ScheduledExecutorService scheduler;
    private final MoritzMessage.Outgoing message;

    private int tries = 0;
    private Step step = Step.CREDITS;
    private @Nullable ScheduledFuture<?> future;

    public MoritzCulCommand(MaxCulTransceiver transceiver, ScheduledExecutorService scheduler,
            MoritzMessage.Outgoing message) {
        this.transceiver = transceiver;
        this.scheduler = scheduler;
        this.message = message;
    }

    @Override
    public synchronized void start() {
        tries++;

        transceiver.send("X");
        step = Step.CREDITS;
        future = scheduler.schedule(this::timeout, TIMEOUT_CREDIT.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public synchronized void cancel() {
        if (future != null) {
            requireNonNull(future).cancel(true);
        }
    }

    @Override
    public synchronized boolean receive(String data) {
        return step.receiver.test(this, data);
    }

    @Override
    public boolean similarTo(CulCommand o) {
        if (!(o instanceof MoritzCulCommand)) {
            return false;
        }
        MoritzCulCommand other = (MoritzCulCommand) o;
        return message.getMessageType() == other.message.getMessageType()
                && message.getSourceAddress().equalsIgnoreCase(other.message.getSourceAddress())
                && message.getDestAddress().equalsIgnoreCase(other.message.getDestAddress());
    }

    private boolean credits(String data) {
        Matcher matcher = RESPONSE_CREDIT.matcher(data);
        if (!matcher.matches()) {
            return false;
        }

        cancel();

        send(Integer.parseInt(matcher.group(1)));
        return true;
    }

    private boolean ack(String data) {
        Optional<AckMoritzMessage> optionalMessage = MoritzMessageParser.parse(data) //
                .filter(AckMoritzMessage.class::isInstance) //
                .map(AckMoritzMessage.class::cast);
        if (!optionalMessage.isPresent()) {
            return false;
        }
        AckMoritzMessage incomingMessage = optionalMessage.get();
        if (incomingMessage.getMessageId() != this.message.getMessageId()) {
            logger.warn("Received unmatched ACK for message {} while waiting for {}", incomingMessage.getMessageId(),
                    message.getMessageId());
            return false;
        }

        cancel();

        logger.debug("Received matching ACK for message {}", this.message.getMessageId());
        transceiver.advance();
        return false; // dispatch message to devices in transceiver
    }

    private void send(int availableCredit) {
        String data = message.message();
        // We need 1000ms for preamble + len in bits (=hex len * 4) ms for payload. Divide by 10 to get credit10ms units
        // keep this in sync with culfw's code in clib/rf_moritz.c!
        int necessaryCredit = (int) Math.ceil((1000 + data.length() * 4) / 10.0);
        if (availableCredit < necessaryCredit) {
            int waitTime = necessaryCredit - availableCredit; // we get one credit every second
            logger.warn("Not enough credit, need {} but {} available, waiting {} s", necessaryCredit, availableCredit,
                    waitTime);
            future = scheduler.schedule(this::start, waitTime, TimeUnit.SECONDS);
            return;
        }

        logger.debug("Available credit {} is more than {} necessary", availableCredit, necessaryCredit);
        logger.debug("<<< [CKD] {}", message);

        transceiver.send("Zs" + data);
        step = Step.ACK;
        future = scheduler.schedule(this::timeout, TIMEOUT_ACK.toMillis(), TimeUnit.MILLISECONDS);
    }

    private synchronized void timeout() {
        if (tries == 10) {
            logger.warn("{} timeout(s) waiting for credit or ACK from CUL, giving up", tries);
            transceiver.advance();
            return;
        }
        if (tries > 0) {
            logger.debug("{} timeout(s) waiting for credit or ACK from CUL, trying again later", tries);
        }

        transceiver.postpone(this);
    }

    // TODO: remove
    @Override
    public String toString() {
        return "waiting for " + (step == Step.CREDITS ? "credits" : "ack");
    }

    private enum Step {

        CREDITS(MoritzCulCommand::credits),
        ACK(MoritzCulCommand::ack);

        final BiPredicate<MoritzCulCommand, String> receiver;

        Step(BiPredicate<MoritzCulCommand, String> receiver) {
            this.receiver = receiver;
        }
    }
}
