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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.openhab.binding.maxcul.internal.message.AbstractMoritzMessage.lastMessageId;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.openhab.binding.maxcul.internal.bridge.MaxCulTransceiver;
import org.openhab.binding.maxcul.internal.message.AbstractMoritzMessage;
import org.openhab.binding.maxcul.internal.message.MoritzMessage;
import org.openhab.binding.maxcul.internal.message.MoritzMessageType;
import org.openhab.binding.maxcul.internal.test.SchedulerMockProvider;

/**
 * @author Sascha Volkenandt - Initial contribution
 */
@MockitoSettings
class MoritzCulCommandTest {

    private static final String SOURCE = "ababab";
    private static final String DEST = "cdcdcd";
    private static final String PAYLOAD = "efef";

    private final SchedulerMockProvider schedulerMockProvider = new SchedulerMockProvider();

    @Mock
    private MaxCulTransceiver transceiverMock;

    private final ScheduledExecutorService schedulerMock = schedulerMockProvider.scheduler();
    private final ScheduledFuture<?> futureMock = schedulerMockProvider.future();

    @Spy
    private final MoritzMessageMock messageMock = new MoritzMessageMock();

    private MoritzCulCommand commandUnderTest;

    @BeforeEach
    void beforeEach() {
        commandUnderTest = new MoritzCulCommand(transceiverMock, schedulerMock, messageMock);
    }

    @Test
    void ok() {
        commandUnderTest.start();
        boolean creditResult = commandUnderTest.receive("21 900");
        boolean ackResult = commandUnderTest
                .receive(String.format("Z0E%02x020218F941123456000119602A2E", lastMessageId()));

        assertThat(creditResult).isTrue();
        assertThat(ackResult).isFalse();

        InOrder inOrder = inOrder(transceiverMock, schedulerMock, futureMock);
        verifyStartCascade(inOrder);
        verifyMessageCascade(inOrder);
        verifyFinishCascade(inOrder);
        verifyNoMoreInteractions(transceiverMock, schedulerMock, futureMock);
    }

    @Test
    void cancel_afterStart() {
        commandUnderTest.start();
        commandUnderTest.cancel();

        InOrder inOrder = inOrder(transceiverMock, schedulerMock, futureMock);
        verifyStartCascade(inOrder);
        inOrder.verify(futureMock).cancel(true);
        verifyNoMoreInteractions(transceiverMock, schedulerMock, futureMock);
    }

    @Test
    void cancel_afterCredit() {
        commandUnderTest.start();
        commandUnderTest.receive("21 900");
        commandUnderTest.cancel();

        InOrder inOrder = inOrder(transceiverMock, schedulerMock, futureMock);
        verifyStartCascade(inOrder);
        verifyMessageCascade(inOrder);
        inOrder.verify(futureMock).cancel(true);
        verifyNoMoreInteractions(transceiverMock, schedulerMock, futureMock);
    }

    @Test
    void creditTimeout_retryOk() {
        commandUnderTest.start();
        schedulerMockProvider.invoke();
        commandUnderTest.start();
        boolean creditResult = commandUnderTest.receive("21 900");
        boolean ackResult = commandUnderTest
                .receive(String.format("Z0E%02x020218F941123456000119602A2E", lastMessageId()));

        assertThat(creditResult).isTrue();
        assertThat(ackResult).isFalse();

        InOrder inOrder = inOrder(transceiverMock, schedulerMock, futureMock);
        verifyStartCascade(inOrder);
        inOrder.verify(transceiverMock).postpone(same(commandUnderTest));
        verifyStartCascade(inOrder);
        verifyMessageCascade(inOrder);
        verifyFinishCascade(inOrder);
        verifyNoMoreInteractions(transceiverMock, schedulerMock, futureMock);
    }

    @Test
    void ackTimeout_retryOk() {
        commandUnderTest.start();
        boolean creditResult = commandUnderTest.receive("21 900");
        schedulerMockProvider.invoke();
        commandUnderTest.start();
        boolean creditResultRetry = commandUnderTest.receive("21 900");
        boolean ackResult = commandUnderTest
                .receive(String.format("Z0E%02x020218F941123456000119602A2E", lastMessageId()));

        assertThat(creditResult).isTrue();
        assertThat(creditResultRetry).isTrue();
        assertThat(ackResult).isFalse();

        InOrder inOrder = inOrder(transceiverMock, schedulerMock, futureMock);
        verifyStartCascade(inOrder);
        verifyMessageCascade(inOrder);
        inOrder.verify(transceiverMock).postpone(same(commandUnderTest));
        verifyStartCascade(inOrder);
        verifyMessageCascade(inOrder);
        verifyFinishCascade(inOrder);
        verifyNoMoreInteractions(transceiverMock, schedulerMock, futureMock);
    }

    @Test
    void ackTimeout_failsAfterTenTries() {
        for (int i = 0; i < 10; i++) {
            commandUnderTest.start();
            commandUnderTest.receive("21 900");
            schedulerMockProvider.invoke();
        }

        InOrder inOrder = inOrder(transceiverMock, schedulerMock, futureMock);
        for (int i = 0; i < 9; i++) {
            verifyStartCascade(inOrder);
            verifyMessageCascade(inOrder);
            inOrder.verify(transceiverMock).postpone(same(commandUnderTest));
        }
        verifyStartCascade(inOrder);
        verifyMessageCascade(inOrder);
        inOrder.verify(transceiverMock).advance();
        verifyNoMoreInteractions(transceiverMock, schedulerMock, futureMock);
    }

    @Test
    void insufficientCredits_waitTime() {
        commandUnderTest.start();
        boolean creditResult = commandUnderTest.receive("21 97");
        schedulerMockProvider.invoke();

        assertThat(creditResult).isTrue();

        InOrder inOrder = inOrder(transceiverMock, schedulerMock, futureMock);
        verifyStartCascade(inOrder);
        inOrder.verify(futureMock).cancel(true);
        inOrder.verify(schedulerMock).schedule(any(Runnable.class), eq(14L), eq(TimeUnit.SECONDS));
        verifyStartCascade(inOrder);
        verifyNoMoreInteractions(transceiverMock, schedulerMock, futureMock);
    }

    private void verifyStartCascade(InOrder inOrder) {
        inOrder.verify(transceiverMock).send("X");
        inOrder.verify(schedulerMock).schedule(any(Runnable.class), anyLong(), any());
    }

    private void verifyMessageCascade(InOrder inOrder) {
        inOrder.verify(futureMock).cancel(true);
        inOrder.verify(transceiverMock).send(argThat(arg -> arg.startsWith("Zs")));
        inOrder.verify(schedulerMock).schedule(any(Runnable.class), anyLong(), any());
    }

    private void verifyFinishCascade(InOrder inOrder) {
        inOrder.verify(futureMock).cancel(true);
        inOrder.verify(transceiverMock).advance();
    }

    private static class MoritzMessageMock extends AbstractMoritzMessage implements MoritzMessage.Outgoing {

        MoritzMessageMock() {
            super(MoritzMessageType.SET_TEMPERATURE, SOURCE, DEST);
        }

        @Override
        public String payload() {
            return PAYLOAD;
        }
    }
}
