package net.stardust.base.sequence;

import java.util.Arrays;

import net.stardust.base.sequence.room.Room;

public class Dungeon {
    
    private Room[] rooms;

    public Dungeon(Room... rooms) {
        this.rooms = Arrays.copyOf(rooms, rooms.length);
    }

    public Room[] getRooms() {
        return rooms.clone();
    }

}
