package io.agora.meeting.data;

import io.agora.meeting.service.body.res.RoomRes;

public class Room {
    public String roomId;
    public String roomName;
    public String channelName;
    public long startTime;

    public Room(RoomRes.Room room) {
        roomId = room.roomId;
        roomName = room.roomName;
        channelName = room.channelName;
        startTime = room.startTime;
    }
}
