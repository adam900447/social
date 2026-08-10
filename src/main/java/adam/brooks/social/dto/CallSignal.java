package adam.brooks.social.dto;

import lombok.Data;

/**
 * A generic envelope for WebRTC call signaling relayed through the server.
 * The server does NOT process call media itself — it just forwards this
 * payload from caller to callee (and back) over WebSocket. The actual
 * audio/video stream goes directly browser-to-browser via WebRTC once
 * the offer/answer/ICE exchange completes.
 */
@Data
public class CallSignal {

    private String fromUserId;
    private String toUserId;

    // OFFER, ANSWER, ICE_CANDIDATE, CALL_END, CALL_DECLINE
    private String type;

    // the actual SDP offer/answer or ICE candidate JSON, opaque to the server
    private String data;

    private String callType; // "VOICE" or "VIDEO"
}
