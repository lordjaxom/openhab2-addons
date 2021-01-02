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

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.Test;
import org.openhab.binding.maxcul.internal.device.MaxDeviceType;
import org.openhab.binding.maxcul.internal.device.ThermostatMode;

/**
 * @author Sascha Volkenandt - Initial contribution
 */
public class MoritzMessageTest {

    @Test
    public void setTemperature_message() {
        SetTemperatureMoritzMessage message = new SetTemperatureMoritzMessage("ababab", "cdcdcd", ThermostatMode.MANUAL,
                21.5);
        assertThat(message.message()).matches("0B[0-9A-F]{2}0040ABABABCDCDCD006B");
    }

    @Test
    public void linkPartnerAdd_message() {
        LinkPartnerMoritzMessage message = new LinkPartnerMoritzMessage(true, "ababab", "cdcdcd", "efefef",
                MaxDeviceType.WALL_MOUNTED_THERMOSTAT);
        assertThat(message.message()).matches("0E[0-9A-F]{2}0020ABABABCDCDCD00EFEFEF03");
    }

    @Test
    public void linkPartnerRemove_message() {
        LinkPartnerMoritzMessage message = new LinkPartnerMoritzMessage(false, "ababab", "cdcdcd", "efefef",
                MaxDeviceType.WALL_MOUNTED_THERMOSTAT);
        assertThat(message.message()).matches("0E[0-9A-F]{2}0021ABABABCDCDCD00EFEFEF03");
    }

    @Test
    public void wallThermostatControl_message() {
        WallThermostatControlMoritzMessage message = new WallThermostatControlMoritzMessage("ababab", "cdcdcd", 21.5,
                23.8);
        assertThat(message.message()).matches("0C[0-9A-F]{2}0042ABABABCDCDCD002BEE");
    }

    @Test
    public void ack_parse() {
        AckMoritzMessage message = parse(AckMoritzMessage.class, "Z0E03020218F941123456000119602A2E");
        assertThat(message)
                .extracting(AckMoritzMessage::isOk, AbstractMoritzMessage::getMessageId,
                        AbstractMoritzMessage::getMessageType, AbstractMoritzMessage::getSourceAddress,
                        AbstractMoritzMessage::getDestAddress)
                .containsExactly(true, 3, MoritzMessageType.ACK, "18F941", "123456");
        assertThat(message.isOk()).isTrue();
        assertThat(message.getSettings())
                .extracting(ThermostatSettings::isLocked, ThermostatSettings::isRfError,
                        ThermostatSettings::isBatteryLow, ThermostatSettings::getValve,
                        ThermostatSettings::getDesiredTemperature, ThermostatSettings::getMode)
                .containsExactly(false, false, false, 96, 21.0, ThermostatMode.MANUAL);
    }

    @Test
    public void thermostatState_parse() {
        ThermostatStateMoritzMessage message = parse(ThermostatStateMoritzMessage.class,
                "Z0F00046018F9410000000019002A00EE2B");
        assertThat(message)
                .extracting(AbstractMoritzMessage::getMessageId, AbstractMoritzMessage::getMessageType,
                        AbstractMoritzMessage::getSourceAddress, AbstractMoritzMessage::getDestAddress)
                .containsExactly(0, MoritzMessageType.THERMOSTAT_STATE, "18F941", "000000");
        assertThat(message.getSettings())
                .extracting(ThermostatSettings::isLocked, ThermostatSettings::isRfError,
                        ThermostatSettings::isBatteryLow, ThermostatSettings::getValve,
                        ThermostatSettings::getDesiredTemperature, ThermostatSettings::getMode)
                .containsExactly(false, false, false, 0, 21.0, ThermostatMode.MANUAL);
        assertThat(message.getMeasuredTemperature()).isEqualTo(23.8);
    }

    @Test
    public void setTemperature_parse() {
        SetTemperatureMoritzMessage message = parse(SetTemperatureMoritzMessage.class, "Z0B2C0040ABABABCDCDCD006B2E");
        assertThat(message)
                .extracting(AbstractMoritzMessage::getMessageId, AbstractMoritzMessage::getMessageType,
                        AbstractMoritzMessage::getSourceAddress, AbstractMoritzMessage::getDestAddress,
                        SetTemperatureMoritzMessage::getMode, SetTemperatureMoritzMessage::getDesiredTemperature)
                .containsExactly(44, MoritzMessageType.SET_TEMPERATURE, "ABABAB", "CDCDCD", ThermostatMode.MANUAL,
                        21.5);
    }

    @Test
    public void timeInformation_message() {
        LocalDateTime dateTime = LocalDateTime.of(2020, 9, 12, 18, 6, 33);
        TimeInformationMoritzMessage message = new TimeInformationMoritzMessage("123456", "ababab", dateTime);
        assertThat(message.message()).matches("0F[0-9A-F]{2}0003123456ABABAB00140C128661");
    }

    @Test
    public void timeInformationEmpty_parse() {
        TimeInformationMoritzMessage message = parse(TimeInformationMoritzMessage.class, "Z0A000A0318F941123456000B");
        assertThat(message).extracting(AbstractMoritzMessage::getMessageId, AbstractMoritzMessage::getMessageType,
                AbstractMoritzMessage::getSourceAddress, AbstractMoritzMessage::getDestAddress,
                TimeInformationMoritzMessage::getDateTime).containsExactly(0, MoritzMessageType.TIME_INFORMATION,
                        "18F941", "123456", TimeInformationMoritzMessage.EMPTY_DATE_TIME);
    }

    @Test
    public void pairPing_parse() {
        PairPingMoritzMessage message = parse(PairPingMoritzMessage.class,
                "Z1700000018F941123456001001A04F45513034343435373312");
        assertThat(message).extracting(AbstractMoritzMessage::getMessageId, AbstractMoritzMessage::getMessageType,
                AbstractMoritzMessage::getSourceAddress, AbstractMoritzMessage::getDestAddress,
                PairPingMoritzMessage::getFirmware, PairPingMoritzMessage::getType,
                PairPingMoritzMessage::getTestresult, PairPingMoritzMessage::getSerial).containsExactly(0,
                        MoritzMessageType.PAIR_PING, "18F941", "123456", 16, MaxDeviceType.HEATING_THERMOSTAT, 160,
                        "4F455130343434353733");
    }

    @Test
    public void pairPong_message() {
        PairPongMoritzMessage message = new PairPongMoritzMessage("123456", "ababab");
        assertThat(message.message()).matches("0B[0-9A-F]{2}0001123456ABABAB0000");
    }

    private static <T extends MoritzMessage.Incoming> T parse(Class<T> clazz, String data) {
        Optional<MoritzMessage.Incoming> optionalMessage = MoritzMessageParser.parse(data);
        assertThat(optionalMessage).isPresent().get().isInstanceOf(clazz);
        return clazz.cast(optionalMessage.get());
    }
}
