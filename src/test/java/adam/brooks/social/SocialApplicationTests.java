package adam.brooks.social.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CallLogTest {

    private CallLog callLog;

    @BeforeEach
    void setUp() {
        callLog = new CallLog();
    }

    @Test
    void defaultType_isVoice() {
        assertEquals(CallLog.CallType.VOICE, callLog.getType());
    }

    @Test
    void defaultStatus_isMissed() {
        assertEquals(CallLog.CallStatus.MISSED, callLog.getStatus());
    }

    @Test
    void startedAt_defaultsToNowAndIsNotNull() {
        assertNotNull(callLog.getStartedAt());
        assertTrue(callLog.getStartedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void endedAt_defaultsToNull() {
        assertNull(callLog.getEndedAt());
    }

    @Test
    void id_defaultsToNull() {
        assertNull(callLog.getId());
    }

    @Test
    void settersAndGetters_workCorrectly() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 1, 1, 10, 5);

        callLog.setId("call123");
        callLog.setCallerId("user1");
        callLog.setCalleeId("user2");
        callLog.setType(CallLog.CallType.VIDEO);
        callLog.setStatus(CallLog.CallStatus.ANSWERED);
        callLog.setStartedAt(start);
        callLog.setEndedAt(end);

        assertEquals("call123", callLog.getId());
        assertEquals("user1", callLog.getCallerId());
        assertEquals("user2", callLog.getCalleeId());
        assertEquals(CallLog.CallType.VIDEO, callLog.getType());
        assertEquals(CallLog.CallStatus.ANSWERED, callLog.getStatus());
        assertEquals(start, callLog.getStartedAt());
        assertEquals(end, callLog.getEndedAt());
    }

    @Test
    void callType_enumHasExactlyVoiceAndVideo() {
        CallLog.CallType[] values = CallLog.CallType.values();
        assertEquals(2, values.length);
        assertArrayEquals(new CallLog.CallType[]{CallLog.CallType.VOICE, CallLog.CallType.VIDEO}, values);
    }

    @Test
    void callStatus_enumHasExactlyMissedAnsweredDeclined() {
        CallLog.CallStatus[] values = CallLog.CallStatus.values();
        assertEquals(3, values.length);
        assertArrayEquals(
                new CallLog.CallStatus[]{CallLog.CallStatus.MISSED, CallLog.CallStatus.ANSWERED, CallLog.CallStatus.DECLINED},
                values
        );
    }

    @Test
    void equals_and_hashCode_matchForSameFieldValues() {
        LocalDateTime fixedTime = LocalDateTime.of(2026, 1, 1, 12, 0);

        CallLog a = new CallLog();
        a.setId("1");
        a.setCallerId("caller");
        a.setCalleeId("callee");
        a.setType(CallLog.CallType.VOICE);
        a.setStatus(CallLog.CallStatus.MISSED);
        a.setStartedAt(fixedTime);
        a.setEndedAt(null);

        CallLog b = new CallLog();
        b.setId("1");
        b.setCallerId("caller");
        b.setCalleeId("callee");
        b.setType(CallLog.CallType.VOICE);
        b.setStatus(CallLog.CallStatus.MISSED);
        b.setStartedAt(fixedTime);
        b.setEndedAt(null);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_returnsFalseWhenFieldsDiffer() {
        CallLog a = new CallLog();
        a.setId("1");

        CallLog b = new CallLog();
        b.setId("2");

        assertNotEquals(a, b);
    }

    @Test
    void toString_containsClassFieldValues() {
        callLog.setId("call123");
        callLog.setCallerId("user1");

        String result = callLog.toString();

        assertTrue(result.contains("call123"));
        assertTrue(result.contains("user1"));
    }
}