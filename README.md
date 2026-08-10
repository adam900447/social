# Adam — Backend

Drop the `src/main/java/adam/brooks/social/*` folders into your existing project
at the same path. Then:

1. Apply the dependency changes in `POM_ADDITIONS.md` to your `pom.xml`
2. Copy the settings from `application.properties` into your real
   `src/main/resources/application.properties` (fill in your Mongo URI and
   a real JWT secret)
3. Run the app

## What's included

| Layer | Files | Purpose |
|---|---|---|
| model/ | User, Post, Comment, Group, Message, CallLog | MongoDB documents |
| repository/ | one per model | Spring Data Mongo repositories |
| security/ | JwtUtil, JwtAuthFilter, SecurityConfig | Login tokens + route protection |
| service/ | UserService, PostService, CommentService, GroupService, ChatService | Business logic — **all ownership/blocking checks live here**, not in controllers |
| controller/ | Auth, User, Post, Comment, Group, Chat (REST) + ChatSocketController (WebSocket) | HTTP + real-time endpoints |
| config/ | WebSocketConfig, JwtHandshakeInterceptor, UserPrincipalHandshakeHandler, GlobalExceptionHandler | WebSocket auth + error handling |

## REST endpoints

**Auth**
- `POST /api/auth/register` — `{username, email, password}`
- `POST /api/auth/login` — `{usernameOrEmail, password}` → returns `{token, userId, username}`

Every other endpoint requires `Authorization: Bearer <token>`.

**Users**
- `GET /api/users/me`
- `GET /api/users/{id}`
- `POST /api/users/block/{targetUserId}`
- `POST /api/users/unblock/{targetUserId}`

**Posts**
- `POST /api/posts` — `{content, imageUrl?}`
- `GET /api/posts?page=0&size=20` — main feed
- `GET /api/posts/user/{userId}` — one user's posts
- `DELETE /api/posts/{id}` — only works if you're the author (checked server-side)
- `POST /api/posts/{id}/like` — toggles like
- `POST /api/posts/{id}/share` — reposts as a new post under your name

**Comments**
- `POST /api/posts/{postId}/comments` — `{content}`
- `GET /api/posts/{postId}/comments`
- `DELETE /api/posts/{postId}/comments/{commentId}` — only your own

**Groups**
- `POST /api/groups` — `{name, description}`
- `GET /api/groups/mine`
- `POST /api/groups/{groupId}/members/{memberId}` — owner only
- `DELETE /api/groups/{groupId}/members/{memberId}` — owner only
- `DELETE /api/groups/{groupId}` — owner only

**Chat history & deletion (REST — not real-time)**
- `GET /api/chats/history/{otherUserId}` — old 1-to-1 messages, oldest first
- `GET /api/chats/group/{groupId}/history`
- `DELETE /api/chats/message/{messageId}` — deletes **for you only**
- `DELETE /api/chats/conversation/{otherUserId}` — clears the whole chat **for you only**

## Real-time (WebSocket / STOMP)

Connect once logged in:
```js
const socket = new SockJS(`http://localhost:8080/ws?token=${jwtToken}`);
const stompClient = Stomp.over(socket);
stompClient.connect({}, () => {
  stompClient.subscribe('/user/queue/messages', msg => { /* new chat message */ });
  stompClient.subscribe('/user/queue/call', signal => { /* incoming call signal */ });
  stompClient.subscribe('/topic/group.' + groupId, msg => { /* group message */ });
});
```

Send a direct message:
```js
stompClient.send('/app/chat.send', {}, JSON.stringify({
  receiverId: 'otherUserId',
  content: 'hey!'
}));
```

Send a group message:
```js
stompClient.send('/app/chat.group.send', {}, JSON.stringify({
  groupId: 'groupId',
  content: 'hello everyone'
}));
```

## How calling actually works here

The server does **not** carry your voice/video — it only relays small
signaling messages (`/app/call.signal`) so two browsers can find each other
and set up a **direct WebRTC connection**. That's the standard approach; audio/video
never touches your Spring Boot server, which keeps it fast and cheap to run.

You still need to write the frontend WebRTC part (getUserMedia, RTCPeerConnection,
sending offer/answer/ICE candidates through `/app/call.signal`). If you want, I can
build that next — it's a separate, meaty piece of frontend JavaScript.

## Blocking behavior

- Blocking is one-directional in storage (`User.blockedUserIds`) but enforced
  **both ways** — `UserService.isBlockedEitherWay()` is checked before every
  message send and every call signal, so a blocked user can't reach you and
  you can't accidentally reach them either.

## Delete semantics — important for your UI

- **Post delete** = actually removed from the database, for everyone (only the author can do it)
- **Comment delete** = actually removed, for everyone (only the author can do it)
- **Message/conversation delete** = "delete for me" only — hides it from your
  view via the `deletedFor` array, the other person still sees their copy.
  This is intentional (matches WhatsApp/Instagram behavior) — tell me if you
  actually want a "delete for everyone" option added too, it's a small addition.
