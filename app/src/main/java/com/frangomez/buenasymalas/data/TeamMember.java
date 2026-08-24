package com.frangomez.buenasymalas.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

/** Que jugador estuvo en que equipo. Es la tabla que permite el historial por jugador. */
@Entity(
        tableName = "team_member",
        primaryKeys = {"team_id", "player_id"},
        indices = {@Index("player_id")},
        foreignKeys = {
                @ForeignKey(entity = Team.class, parentColumns = "id", childColumns = "team_id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Player.class, parentColumns = "id", childColumns = "player_id",
                        onDelete = ForeignKey.CASCADE)
        })
public class TeamMember {

    @ColumnInfo(name = "team_id")
    public long teamId;

    @ColumnInfo(name = "player_id")
    public long playerId;

    public TeamMember() {
    }

    @androidx.room.Ignore
    public TeamMember(long teamId, long playerId) {
        this.teamId = teamId;
        this.playerId = playerId;
    }
}
