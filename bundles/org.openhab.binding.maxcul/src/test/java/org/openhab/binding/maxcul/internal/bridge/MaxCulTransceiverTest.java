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
package org.openhab.binding.maxcul.internal.bridge;

/**
 * @author Sascha Volkenandt - Initial contribution
 *
 * @RunWith(Enclosed.class)
 *                          public class MaxCulTransceiverTest {
 * 
 *                          private static final String PORT = "serialPort";
 *                          private static final int BAUD_RATE = 38400;
 * 
 * @RunWith(MockitoJUnitRunner.class)
 *                                    public static class Lifecycle {
 * 
 * @Mock
 *       private MaxCulBridgeHandler bridgeHandlerMock;
 * 
 * @Spy
 *      private MaxCulBridgeConfiguration configuration = new MaxCulBridgeConfiguration();
 * 
 * @Mock
 *       private SerialPortManager serialPortManagerMock;
 * @Mock
 *       private SerialPortIdentifier serialPortIdentifierMock;
 * @Mock
 *       private SerialPort serialPortMock;
 * @Mock
 *       private InputStream inputStreamMock;
 * @Mock
 *       private OutputStream outputStreamMock;
 * 
 * @Mock
 *       private SerialPortDevice serialPortDeviceMock;
 * 
 * @InjectMocks
 * @Spy
 *      private MaxCulTransceiver transceiverUnderTest;
 * 
 * @Before
 *         public void setUp() {
 *         configuration.port = PORT;
 *         configuration.baudRate = BAUD_RATE;
 *         }
 * 
 * @Test
 *       public void initialize_ok() {
 *       doReturn(serialPortIdentifierMock).when(serialPortManagerMock).getIdentifier(any());
 * 
 *       boolean result = transceiverUnderTest.initialize(bridgeHandlerMock, configuration);
 * 
 *       assertThat(result).isTrue();
 * 
 *       verify(serialPortManagerMock).getIdentifier(PORT);
 *       verifyNoMoreInteractions(bridgeHandlerMock, serialPortManagerMock, serialPortIdentifierMock, serialPortMock);
 *       }
 * 
 * @Test
 *       public void connect_ok() throws Exception {
 *       BridgeImpl bridge = new BridgeImpl(THING_TYPE_BRIDGE, "theBridge");
 *       doReturn(bridge).when(bridgeHandlerMock).getThing();
 *       doReturn(inputStreamMock).when(serialPortMock).getInputStream();
 *       doReturn(outputStreamMock).when(serialPortMock).getOutputStream();
 *       doReturn(serialPortMock).when(serialPortIdentifierMock).open(any(), anyInt());
 *       doNothing().when(transceiverUnderTest).queueCommand(any());
 * 
 *       transceiverUnderTest.connect();
 * 
 *       InOrder inOrder = inOrder(bridgeHandlerMock, serialPortManagerMock, serialPortIdentifierMock, serialPortMock);
 *       inOrder.verify(bridgeHandlerMock).getThing();
 *       inOrder.verify(serialPortIdentifierMock).open("maxcul:bridge:theBridge", 2000);
 *       inOrder.verify(serialPortMock).setSerialPortParams(BAUD_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
 *       SerialPort.PARITY_NONE);
 *       inOrder.verify(serialPortMock).addEventListener(same(transceiverUnderTest));
 *       inOrder.verify(serialPortMock).notifyOnDataAvailable(true);
 *       inOrder.verify(serialPortMock).getInputStream();
 *       inOrder.verify(serialPortMock).getOutputStream();
 *       verify(transceiverUnderTest).queueCommand(argThat(command -> command instanceof VersionCulCommand));
 *       verifyNoMoreInteractions(bridgeHandlerMock, serialPortManagerMock, serialPortIdentifierMock, serialPortMock);
 *       }
 * 
 * @Test
 *       public void disconnect_ok() {
 *       transceiverUnderTest.disconnect();
 * 
 *       InOrder inOrder = inOrder(bridgeHandlerMock, serialPortMock, serialPortDeviceMock);
 *       inOrder.verify(serialPortDeviceMock).close();
 *       inOrder.verify(serialPortMock).removeEventListener();
 *       inOrder.verify(serialPortMock).close();
 *       verifyNoMoreInteractions(bridgeHandlerMock, serialPortMock, serialPortDeviceMock);
 *       }
 *       }
 * 
 * @RunWith(MockitoJUnitRunner.class)
 *                                    public static class Communication {
 * 
 * @Mock
 *       private ScheduledFuture<?> futureMock;
 * @Mock
 *       private ScheduledExecutorService schedulerMock;
 * 
 * @Mock
 *       private MaxCulBridgeHandler bridgeHandlerMock;
 * 
 * @Mock
 *       private SerialPortDevice serialPortDeviceMock;
 * 
 * @InjectMocks
 *              private MaxCulTransceiver transceiverUnderTest;
 * 
 * @Before
 *         public void setUp() {
 *         doReturn(futureMock).when(schedulerMock).schedule(any(Runnable.class), anyLong(), any());
 *         //noinspection ResultOfMethodCallIgnored
 *         doReturn(schedulerMock).when(bridgeHandlerMock).getScheduler();
 *         }
 * 
 * @Test
 *       public void sendRaw_ok() throws Exception {
 *       transceiverUnderTest.send("Hello, World!");
 * 
 *       verify(serialPortDeviceMock).write("Hello, World!");
 *       verifyNoMoreInteractions(bridgeHandlerMock, serialPortDeviceMock);
 *       }
 * 
 * @Test
 *       public void sendRaw_error() throws Exception {
 *       doThrow(new IOException()).when(serialPortDeviceMock).write(any());
 * 
 *       transceiverUnderTest.send("Hello, World!");
 * 
 *       verify(serialPortDeviceMock).write("Hello, World!");
 *       verify(bridgeHandlerMock).communicationError(eq("I/O error on serial port!"), argThat(e -> e instanceof
 *       IOException));
 *       verifyNoMoreInteractions(bridgeHandlerMock, serialPortDeviceMock);
 *       }
 * 
 * @Test
 *       public void handshakeAfterInitialize_ok() throws Exception {
 *       doReturn("V 1.67 CULMOCK", (String) null).when(serialPortDeviceMock).read();
 * 
 *       transceiverUnderTest.queueCommand(new VersionCulCommand(bridgeHandlerMock));
 *       transceiverUnderTest.serialEvent(new SerialPortEventMock());
 * 
 *       InOrder inOrder = inOrder(bridgeHandlerMock, serialPortDeviceMock);
 *       inOrder.verify(bridgeHandlerMock).sendRaw("V");
 *       //noinspection ResultOfMethodCallIgnored
 *       inOrder.verify(bridgeHandlerMock).getScheduler();
 *       inOrder.verify(serialPortDeviceMock).read();
 *       inOrder.verify(bridgeHandlerMock).receiveVersion(167);
 *       inOrder.verify(bridgeHandlerMock).handleNextCommand();
 *       inOrder.verify(serialPortDeviceMock).read();
 *       verifyNoMoreInteractions(bridgeHandlerMock, serialPortDeviceMock);
 *       }
 * 
 * @Test
 *       public void moritzCommandWithAck_ok() throws Exception {
 *       doReturn("21 900", null, "Z0E0C0202ABABAB1234560001190C2EFE", null)
 *       .when(serialPortDeviceMock).read();
 * 
 *       transceiverUnderTest.queueCommand(
 *       new MoritzCulCommand(bridgeHandlerMock,
 *       new MoritzMessageMock(12, MoritzMessageType.SET_TEMPERATURE, "ababab", "cdcdcd", "efef")));
 *       transceiverUnderTest.serialEvent(new SerialPortEventMock());
 *       transceiverUnderTest.serialEvent(new SerialPortEventMock());
 * 
 *       InOrder inOrder = inOrder(bridgeHandlerMock, serialPortDeviceMock);
 *       inOrder.verify(bridgeHandlerMock).sendRaw("X");
 *       //noinspection ResultOfMethodCallIgnored
 *       inOrder.verify(bridgeHandlerMock).getScheduler();
 *       inOrder.verify(serialPortDeviceMock).read();
 *       inOrder.verify(bridgeHandlerMock).sendRaw("Zs0C0C0040ABABABCDCDCD00EFEF");
 *       //noinspection ResultOfMethodCallIgnored
 *       inOrder.verify(bridgeHandlerMock).getScheduler();
 *       inOrder.verify(serialPortDeviceMock, times(2)).read();
 *       inOrder.verify(bridgeHandlerMock).handleNextCommand();
 *       inOrder.verify(bridgeHandlerMock).dispatch(any(AckMoritzMessage.class));
 *       inOrder.verify(serialPortDeviceMock).read();
 *       verifyNoMoreInteractions(bridgeHandlerMock, serialPortDeviceMock);
 *       }
 * 
 * @Test
 *       public void incomingMessage_ok() throws Exception {
 *       doReturn("Z0F1C0460ABABAB0000000019192E00E60A", (String) null).when(serialPortDeviceMock).read();
 * 
 *       transceiverUnderTest.serialEvent(new SerialPortEventMock());
 * 
 *       InOrder inOrder = inOrder(bridgeHandlerMock, serialPortDeviceMock);
 *       inOrder.verify(serialPortDeviceMock).read();
 *       inOrder.verify(bridgeHandlerMock).dispatch(any(ThermostatStateMoritzMessage.class));
 *       inOrder.verify(serialPortDeviceMock).read();
 *       verifyNoMoreInteractions(bridgeHandlerMock, serialPortDeviceMock);
 *       }
 *       }
 * 
 *       private static class SerialPortEventMock implements SerialPortEvent {
 * 
 * @Override
 *           public int getEventType() {
 *           return SerialPortEvent.DATA_AVAILABLE;
 *           }
 * 
 * @Override
 *           public boolean getNewValue() {
 *           return false;
 *           }
 *           }
 * 
 *           private static class MoritzMessageMock extends AbstractMoritzMessage implements MoritzMessage.Outgoing {
 * 
 *           private final String payload;
 * 
 *           MoritzMessageMock(int messageId, MoritzMessageType messageType, String sourceAddress, String destAddress,
 *           String payload) {
 *           super(messageId, messageType, sourceAddress, destAddress);
 *           this.payload = payload;
 *           }
 * 
 * @Override
 *           public String payload() {
 *           return payload;
 *           }
 *           }
 *           }
 */
