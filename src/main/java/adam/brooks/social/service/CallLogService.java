package adam.brooks.social.service;

import adam.brooks.social.model.CallLog;
import adam.brooks.social.repository.CallLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks the lifecycle of a call (started -> answered/declined -> ended) as
 * signaling messages pass through ChatSocketController. WebRTC signaling
 * itself is stateless per-message, so this keeps a small in-memory map of
 * "which call log belongs to this pair of users right now" to correlate
 * OFFER -> ANSWER/DECLINE -> END into one record.
 */
@Service
@RequiredArgsConstructor
public class CallLogService {

    private final CallLogRepository callLogRepository;
    private final MongoTemplate mongoTemplate;

    // key: sorted "userIdA_userIdB" -> the call log currently in progress for that pair
    private final Map<String, String> activeCalls = new ConcurrentHashMap<>();

    private String pairKey(String userIdA, String userIdB) {
        return userIdA.compareTo(userIdB) < 0 ? userIdA + "_" + userIdB : userIdB + "_" + userIdA;
    }

    public void callStarted(String callerId, String calleeId, String callType) {
        CallLog log = new CallLog();
        log.setCallerId(callerId);
        log.setCalleeId(calleeId);
        log.setType("VIDEO".equalsIgnoreCase(callType) ? CallLog.CallType.VIDEO : CallLog.CallType.VOICE);
        log.setStatus(CallLog.CallStatus.MISSED); // upgraded to ANSWERED if the callee accepts
        log.setStartedAt(LocalDateTime.now());

        CallLog saved = callLogRepository.save(log);
        activeCalls.put(pairKey(callerId, calleeId), saved.getId());
    }

    public void callAnswered(String userIdA, String userIdB) {
        String logId = activeCalls.get(pairKey(userIdA, userIdB));
        if (logId == null) return;

        callLogRepository.findById(logId).ifPresent(log -> {
            log.setStatus(CallLog.CallStatus.ANSWERED);
            callLogRepository.save(log);
        });
    }

    public void callDeclined(String userIdA, String userIdB) {
        String logId = activeCalls.remove(pairKey(userIdA, userIdB));
        if (logId == null) return;

        callLogRepository.findById(logId).ifPresent(log -> {
            log.setStatus(CallLog.CallStatus.DECLINED);
            log.setEndedAt(LocalDateTime.now());
            callLogRepository.save(log);
        });
    }

    public void callEnded(String userIdA, String userIdB) {
        String logId = activeCalls.remove(pairKey(userIdA, userIdB));
        if (logId == null) return;

        callLogRepository.findById(logId).ifPresent(log -> {
            if (log.getEndedAt() == null) {
                log.setEndedAt(LocalDateTime.now());
                callLogRepository.save(log);
            }
        });
    }

    /**
     * Call history between two specific users, newest first — used to show
     * past calls interleaved with chat history on the conversation view.
     */
    public List<CallLog> getCallHistory(String userIdA, String userIdB) {
        Query query = new Query(
                new Criteria().orOperator(
                        Criteria.where("callerId").is(userIdA).and("calleeId").is(userIdB),
                        Criteria.where("callerId").is(userIdB).and("calleeId").is(userIdA)
                )
        ).with(Sort.by(Sort.Direction.DESC, "startedAt"));

        return mongoTemplate.find(query, CallLog.class);
    }
}
