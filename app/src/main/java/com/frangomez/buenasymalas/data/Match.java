package com.frangomez.buenasymalas.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Una partida terminada. */
@Entity(tableName = "partida")
public class Match {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "played_at")
    public long playedAt;

    /** 15 o 30. */
    @ColumnInfo(name = "target")
    public int target;

    @ColumnInfo(name = "with_flor")
    public boolean withFlor;

    /** Jugadores por equipo: 1, 2 o 3. */
    @ColumnInfo(name = "team_size")
    public int teamSize;

    @ColumnInfo(name = "score_a")
    public int scoreA;

    @ColumnInfo(name = "score_b")
    public int scoreB;

    @ColumnInfo(name = "team_a_id")
    public long teamAId;

    @ColumnInfo(name = "team_b_id")
    public long teamBId;

    @ColumnInfo(name = "winner_team_id")
    public long winnerTeamId;

    @ColumnInfo(name = "mano_player_id")
    public long manoPlayerId;

    /** Una paliza es ganar por la mitad del objetivo o mas. */
    public boolean esPaliza() {
        return Math.abs(scoreA - scoreB) >= target / 2;
    }
}
