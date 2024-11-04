let stompClientUser1 = null;
let stompClientUser2 = null;
let isRoomOwnerUser1 = false;
let isRoomOwnerUser2 = false;

function connectUser1() {
    const tokenUser1 = document.getElementById("tokenUser1").value;
    if (!tokenUser1) {
        alert("User1의 JWT 토큰을 입력하세요.");
        return;
    }
    connect("User1", tokenUser1);
}

function connectUser2() {
    const tokenUser2 = document.getElementById("tokenUser2").value;
    if (!tokenUser2) {
        alert("User2의 JWT 토큰을 입력하세요.");
        return;
    }
    connect("User2", tokenUser2);
}

function connect(user, token) {
    const socket = new WebSocket(`ws://localhost:8080/ws-chat?token=${token}`);
    const stompClient = Stomp.over(socket);

    stompClient.connect(
        {},
        (frame) => {
            console.log(`${user} Connected: ` + frame);
            showMessage(user, `${user}님이 채팅방에 연결되었습니다.`);
        },
        (error) => {
            console.error("Connection error:", error);
        }
    );

    if (user === "User1") {
        stompClientUser1 = stompClient;
    } else {
        stompClientUser2 = stompClient;
    }
}

function createRoom(user) {
    const roomId = document.getElementById(`roomId${user}`).value;
    if (!roomId) {
        alert("채팅방 ID를 입력해주세요.");
        return;
    }

    const stompClient = user === "User1" ? stompClientUser1 : stompClientUser2;
    if (stompClient && stompClient.connected) {
        // 채팅방 생성과 동시에 구독
        stompClient.subscribe(`/topic/chat-room/${roomId}`, (message) => showMessage(user, message));

        // 채팅방 생성 요청 전송
        stompClient.send(`/app/chat-room/${roomId}/create`, {});

        if (user === "User1") {
            isRoomOwnerUser1 = true;
        } else {
            isRoomOwnerUser2 = true;
        }

        showMessage(user, `채팅방 ${roomId}이 생성되고 구독이 완료되었습니다.`);
    }
}

function enterRoom(user) {
    const roomId = document.getElementById(`roomId${user}`).value;
    const stompClient = user === "User1" ? stompClientUser1 : stompClientUser2;
    if (stompClient && stompClient.connected) {
        stompClient.subscribe(`/topic/chat-room/${roomId}`, (message) => showMessage(user, message));
        stompClient.send(`/app/chat-room/${roomId}/enter`, {});
        showMessage(user, `${user}님이 채팅방에 입장하였습니다.`);
    }
}

function sendMessage(user) {
    const roomId = document.getElementById(`roomId${user}`).value;
    const messageContent = document.getElementById(`messageInput${user}`).value;
    const stompClient = user === "User1" ? stompClientUser1 : stompClientUser2;

    if (stompClient && messageContent) {
        stompClient.send(`/app/chat-room/${roomId}`, {}, JSON.stringify({ content: messageContent }));
        document.getElementById(`messageInput${user}`).value = '';
    }
}

function leaveRoom(user) {
    const roomId = document.getElementById(`roomId${user}`).value;
    const stompClient = user === "User1" ? stompClientUser1 : stompClientUser2;

    if (stompClient && stompClient.connected) {
        stompClient.send(`/app/chat-room/${roomId}/leave`, {});
        showMessage(user, `${user}님이 채팅방을 퇴실하였습니다.`);

        const isRoomOwner = user === "User1" ? isRoomOwnerUser1 : isRoomOwnerUser2;
        if (isRoomOwner) {
            stompClient.send(`/app/chat-room/${roomId}/success`, {});
            showMessage(user, `방장 권한이 다른 유저에게 승계되었습니다.`);
            if (user === "User1") isRoomOwnerUser1 = false;
            else isRoomOwnerUser2 = false;
        }
    }
}

function showMessage(user, message) {
    let messageData;
    try {
        // 메시지 body를 가져와서 JSON 파싱
        messageData = typeof message === "string" ? { content: message } : JSON.parse(message.body);
    } catch (error) {
        console.error("Invalid JSON data received:", message.body);
        return;
    }

    const content = messageData.content || "알 수 없는 메시지";
    const messageElement = document.createElement("div");
    messageElement.classList.add("chat-message", user.toLowerCase());

    const contentElement = document.createElement("div");
    contentElement.classList.add("content");
    contentElement.textContent = `${user}: ${content}`;

    messageElement.appendChild(contentElement);
    document.getElementById(`messages${user}`).appendChild(messageElement);
}

function disconnect(user) {
    const stompClient = user === "User1" ? stompClientUser1 : stompClientUser2;

    if (stompClient && stompClient.connected) {
        stompClient.disconnect(() => {
            console.log(`${user}님이 연결을 해제했습니다.`);
            showMessage(user, `${user}님이 연결을 해제했습니다.`);
        });
    }

    if (user === "User1") {
        stompClientUser1 = null;
        isRoomOwnerUser1 = false;
    } else {
        stompClientUser2 = null;
        isRoomOwnerUser2 = false;
    }
}
