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
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openhab.binding.maxcul.internal.bridge.MaxCulTransceiver;
import org.openhab.binding.maxcul.internal.test.SchedulerMockProvider;

/**
 * @author Sascha Volkenandt - Initial contribution
 */
@RunWith(MockitoJUnitRunner.class)
public class VersionCulCommandTest {

    private final SchedulerMockProvider schedulerMockProvider = new SchedulerMockProvider();

    @Mock
    private MaxCulTransceiver transceiverMock;

    private final ScheduledExecutorService schedulerMock = schedulerMockProvider.scheduler();
    private final Future<?> futureMock = schedulerMockProvider.future();

    private VersionCulCommand commandUnderTest;

    @Before
    public void before() {
        commandUnderTest = new VersionCulCommand(transceiverMock, schedulerMock);
    }

    @Test
    public void nanoCUL868_ok() {
        commandUnderTest.start();
        boolean result = commandUnderTest.receive("V 1.67 nanoCUL868");

        assertThat(result).isTrue();

        InOrder inOrder = inOrder(transceiverMock, schedulerMock, futureMock);
        verifyStartCascade(inOrder);
        verifyFinishCascade(inOrder, 167);
        verifyNoMoreInteractions(transceiverMock, schedulerMock, futureMock);
    }

    @Test
    public void aculfw_ok() {
        commandUnderTest.start();
        boolean result = commandUnderTest.receive("V 1.23.0 a-culfw Build: 123 (2019-11-06) ");

        assertThat(result).isTrue();

        InOrder inOrder = inOrder(transceiverMock, schedulerMock, futureMock);
        verifyStartCascade(inOrder);
        verifyFinishCascade(inOrder, 154);
        verifyNoMoreInteractions(transceiverMock, schedulerMock, futureMock);
    }

    @Test
    public void unknown_notOk() {
        commandUnderTest.start();
        boolean result = commandUnderTest.receive("nonsense");

        assertThat(result).isFalse();

        InOrder inOrder = inOrder(transceiverMock, schedulerMock, futureMock);
        verifyStartCascade(inOrder);
        verifyNoMoreInteractions(transceiverMock, schedulerMock, futureMock);
    }

    @Test
    public void cancel() {
        commandUnderTest.start();
        commandUnderTest.cancel();

        InOrder inOrder = inOrder(transceiverMock, schedulerMock, futureMock);
        verifyStartCascade(inOrder);
        inOrder.verify(futureMock).cancel(true);
        verifyNoMoreInteractions(transceiverMock, schedulerMock, futureMock);
    }

    @Test
    public void timeout_retryOk() {
        commandUnderTest.start();
        schedulerMockProvider.invoke();
        boolean result = commandUnderTest.receive("V 1.67 nanoCUL868");

        assertThat(result).isTrue();

        InOrder inOrder = inOrder(transceiverMock, schedulerMock, futureMock);
        verifyStartCascade(inOrder);
        verifyStartCascade(inOrder);
        verifyFinishCascade(inOrder, 167);
        verifyNoMoreInteractions(transceiverMock, schedulerMock, futureMock);
    }

    @Test
    public void timeout_failsAfterThreeTries() {
        commandUnderTest.start();
        schedulerMockProvider.invoke();
        schedulerMockProvider.invoke();
        schedulerMockProvider.invoke();

        InOrder inOrder = inOrder(transceiverMock, schedulerMock, futureMock);
        verifyStartCascade(inOrder);
        verifyStartCascade(inOrder);
        verifyStartCascade(inOrder);
        inOrder.verify(transceiverMock).error("Cannot perform handshake with CUL!");
        verifyNoMoreInteractions(transceiverMock, schedulerMock, futureMock);
    }

    private void verifyStartCascade(InOrder inOrder) {
        inOrder.verify(transceiverMock).send("V");
        inOrder.verify(schedulerMock).schedule(any(Runnable.class), anyLong(), any());
    }

    private void verifyFinishCascade(InOrder inOrder, int version) {
        inOrder.verify(futureMock).cancel(true);
        inOrder.verify(transceiverMock).version(version);
        inOrder.verify(transceiverMock).advance();
    }
}
