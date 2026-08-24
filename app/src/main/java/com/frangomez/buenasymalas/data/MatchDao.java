package com.frangomez.buenasymalas.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Las partidas y todo lo que se deriva de ellas.
 *
 * <p>Las stats no se guardan: se consultan. Un contador de victorias en la tabla de jugadores
 * se desincroniza en cuanto se borra una partida, y el volumen de datos acá es de decenas de
 * filas, no de millones.
 */
@Dao
public interface MatchDao {

    @Insert
    long insert(Match match);

    @Update
    void update(Match match);

    @Insert
    long insertTeam(Team team);

    @Insert
    void insertMember(TeamMember member);

    @Query("SELECT * FROM partida ORDER BY played_at DESC")
    LiveData<List<Match>> observarTodas();

    @Query("SELECT * FROM partida WHERE id = :id")
    Match porId(long id);

    /** Los jugadores de un lado, para armar el nombre del equipo en el historial. */
    @Query("SELECT p.* FROM player p"
            + " JOIN team_member tm ON tm.player_id = p.id"
            + " WHERE tm.team_id = :teamId")
    List<Player> integrantes(long teamId);

    // --- Cabeza a cabeza por jugador ---------------------------------------------------------

    /**
     * Las partidas donde los dos jugaron en lados opuestos, sin importar el formato: es lo que
     * hace que el cabeza a cabeza incluya los 2v2 y no sólo los mano a mano.
     */
    @Query("SELECT m.* FROM partida m"
            + " JOIN team_member ta ON ta.player_id = :playerA"
            + "   AND ta.team_id IN (m.team_a_id, m.team_b_id)"
            + " JOIN team_member tb ON tb.player_id = :playerB"
            + "   AND tb.team_id IN (m.team_a_id, m.team_b_id)"
            + " WHERE ta.team_id <> tb.team_id"
            + " ORDER BY m.played_at DESC")
    LiveData<List<Match>> observarCruces(long playerA, long playerB);

    @Query("SELECT m.* FROM partida m"
            + " JOIN team_member ta ON ta.player_id = :playerA"
            + "   AND ta.team_id IN (m.team_a_id, m.team_b_id)"
            + " JOIN team_member tb ON tb.player_id = :playerB"
            + "   AND tb.team_id IN (m.team_a_id, m.team_b_id)"
            + " WHERE ta.team_id <> tb.team_id"
            + " ORDER BY m.played_at DESC")
    List<Match> cruces(long playerA, long playerB);

    // --- Stats de un jugador -----------------------------------------------------------------

    @Query("SELECT COUNT(*) FROM partida m"
            + " JOIN team_member tm ON tm.player_id = :playerId"
            + "   AND tm.team_id IN (m.team_a_id, m.team_b_id)")
    int jugadas(long playerId);

    @Query("SELECT COUNT(*) FROM partida m"
            + " JOIN team_member tm ON tm.player_id = :playerId"
            + "   AND tm.team_id = m.winner_team_id")
    int ganadas(long playerId);

    /** Las partidas de un jugador, de la más nueva a la más vieja: alimenta racha y peor derrota. */
    @Query("SELECT m.* FROM partida m"
            + " JOIN team_member tm ON tm.player_id = :playerId"
            + "   AND tm.team_id IN (m.team_a_id, m.team_b_id)"
            + " ORDER BY m.played_at DESC")
    List<Match> partidasDe(long playerId);

    /** El equipo en el que jugó esa partida, para saber si ganó o perdió. */
    @Query("SELECT tm.team_id FROM team_member tm"
            + " JOIN partida m ON tm.team_id IN (m.team_a_id, m.team_b_id)"
            + " WHERE tm.player_id = :playerId AND m.id = :matchId")
    Long equipoEn(long playerId, long matchId);

    // --- Récord del equipo armado ------------------------------------------------------------

    /**
     * Cuántas veces ganó el roster A contra el roster B. Distinto del cabeza a cabeza por
     * jugador: acá cuenta la dupla exacta, no cada integrante por separado.
     */
    @Query("SELECT COUNT(*) FROM partida m"
            + " JOIN team ta ON ta.id IN (m.team_a_id, m.team_b_id) AND ta.roster_key = :rosterA"
            + " JOIN team tb ON tb.id IN (m.team_a_id, m.team_b_id) AND tb.roster_key = :rosterB"
            + " WHERE ta.id <> tb.id AND m.winner_team_id = ta.id")
    int ganadasDelRoster(String rosterA, String rosterB);

    // --- Museo -------------------------------------------------------------------------------

    @Query("SELECT COUNT(*) FROM photo")
    LiveData<Integer> derrotasDocumentadas();
}
