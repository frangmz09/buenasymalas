package com.frangomez.buenasymalas.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Embedded;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PhotoDao {

    @Insert
    long insert(Photo photo);

    @Query("SELECT * FROM photo WHERE match_id = :matchId LIMIT 1")
    Photo dePartida(long matchId);

    /** Todo lo que el museo necesita para una tarjeta, en una consulta. */
    class FotoDeMuseo {
        @Embedded
        public Photo photo;

        @Embedded(prefix = "m_")
        public Match match;

        public String perdedor;
        public String alias;
    }

    @Query("SELECT f.*,"
            + " m.id AS m_id, m.played_at AS m_played_at, m.target AS m_target,"
            + " m.with_flor AS m_with_flor, m.team_size AS m_team_size,"
            + " m.score_a AS m_score_a, m.score_b AS m_score_b,"
            + " m.team_a_id AS m_team_a_id, m.team_b_id AS m_team_b_id,"
            + " m.winner_team_id AS m_winner_team_id, m.mano_player_id AS m_mano_player_id,"
            + " p.name AS perdedor, p.alias AS alias"
            + " FROM photo f"
            + " JOIN partida m ON m.id = f.match_id"
            + " JOIN player p ON p.id = f.loser_player_id"
            + " ORDER BY m.played_at DESC")
    LiveData<List<FotoDeMuseo>> observarMuseo();
}
