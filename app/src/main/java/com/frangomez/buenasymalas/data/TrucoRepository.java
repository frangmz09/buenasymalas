package com.frangomez.buenasymalas.data;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Única puerta a los datos. Las lecturas salen como {@link LiveData} —Room las actualiza sola—
 * y las escrituras van a un hilo propio, uno solo, para que no haya dos transacciones
 * peleándose por la misma partida.
 */
public class TrucoRepository {

    private static volatile TrucoRepository instancia;

    private final AppDatabase db;
    private final PlayerDao playerDao;
    private final MatchDao matchDao;
    private final PhotoDao photoDao;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    private TrucoRepository(Context context) {
        db = AppDatabase.getInstance(context);
        playerDao = db.playerDao();
        matchDao = db.matchDao();
        photoDao = db.photoDao();
    }

    public static TrucoRepository getInstance(Context context) {
        if (instancia == null) {
            synchronized (TrucoRepository.class) {
                if (instancia == null) {
                    instancia = new TrucoRepository(context);
                }
            }
        }
        return instancia;
    }

    public void ejecutar(Runnable tarea) {
        io.execute(tarea);
    }

    // --- Jugadores ---------------------------------------------------------------------------

    public LiveData<List<Player>> observarJugadores() {
        return playerDao.observarTodos();
    }

    public LiveData<Player> observarJugador(long id) {
        return playerDao.observar(id);
    }

    /** Lectura directa, para el hilo de io: la usa el marcador al armar los equipos. */
    public Player jugador(long id) {
        return playerDao.porId(id);
    }

    public void guardarJugador(Player player) {
        io.execute(() -> {
            if (player.id == 0) {
                playerDao.insert(player);
            } else {
                playerDao.update(player);
            }
        });
    }

    // --- Partidas ----------------------------------------------------------------------------

    public LiveData<List<Match>> observarPartidas() {
        return matchDao.observarTodas();
    }

    public LiveData<List<Match>> observarCruces(long playerA, long playerB) {
        return matchDao.observarCruces(playerA, playerB);
    }

    /**
     * Guarda una partida terminada con sus dos equipos.
     *
     * <p>Va en una transacción porque un equipo sin partida, o una partida sin ganador, deja el
     * historial mintiendo. Los ids de equipo sólo existen después de insertarlos, y ellos a su
     * vez necesitan el id de la partida: por eso la partida se inserta primero y se completa
     * con un update dentro de la misma transacción.
     */
    public void guardarPartida(Match match, List<Long> jugadoresA, List<Long> jugadoresB,
                               boolean ganoA, Runnable alTerminar) {
        io.execute(() -> {
            db.runInTransaction(() -> {
                long matchId = matchDao.insert(match);

                long teamAId = crearEquipo(matchId, jugadoresA);
                long teamBId = crearEquipo(matchId, jugadoresB);

                match.id = matchId;
                match.teamAId = teamAId;
                match.teamBId = teamBId;
                match.winnerTeamId = ganoA ? teamAId : teamBId;
                matchDao.update(match);
            });
            if (alTerminar != null) {
                alTerminar.run();
            }
        });
    }

    private long crearEquipo(long matchId, List<Long> jugadores) {
        long teamId = matchDao.insertTeam(new Team(matchId, Team.rosterKeyDe(jugadores)));
        for (long playerId : jugadores) {
            matchDao.insertMember(new TeamMember(teamId, playerId));
        }
        return teamId;
    }

    // --- Stats derivadas ---------------------------------------------------------------------

    /** Cuántas ganó cada uno de los dos, contando sólo las partidas en que se enfrentaron. */
    public int[] recordEntre(long playerA, long playerB) {
        int ganoA = 0;
        int ganoB = 0;
        for (Match m : matchDao.cruces(playerA, playerB)) {
            Long equipoDeA = matchDao.equipoEn(playerA, m.id);
            if (equipoDeA != null && equipoDeA == m.winnerTeamId) {
                ganoA++;
            } else {
                ganoB++;
            }
        }
        return new int[]{ganoA, ganoB};
    }

    /** Récord de la dupla exacta contra la dupla exacta. */
    public int[] recordEntreRosters(String rosterA, String rosterB) {
        return new int[]{
                matchDao.ganadasDelRoster(rosterA, rosterB),
                matchDao.ganadasDelRoster(rosterB, rosterA)
        };
    }

    public int jugadas(long playerId) {
        return matchDao.jugadas(playerId);
    }

    public int ganadas(long playerId) {
        return matchDao.ganadas(playerId);
    }

    /**
     * Racha actual: cuántas seguidas ganó (positivo) o perdió (negativo) desde la última vez
     * que cambió el resultado. Se recorre en Java porque SQLite sin window functions no ayuda
     * y son decenas de filas.
     */
    public int racha(long playerId) {
        List<Match> partidas = matchDao.partidasDe(playerId);
        int racha = 0;
        Boolean ganoLaPrimera = null;
        for (Match m : partidas) {
            Long equipo = matchDao.equipoEn(playerId, m.id);
            boolean gano = equipo != null && equipo == m.winnerTeamId;
            if (ganoLaPrimera == null) {
                ganoLaPrimera = gano;
            } else if (gano != ganoLaPrimera) {
                break;
            }
            racha++;
        }
        if (ganoLaPrimera == null) {
            return 0;
        }
        return ganoLaPrimera ? racha : -racha;
    }

    /** La derrota con mayor diferencia de puntos, o null si todavía no perdió ninguna. */
    public Match peorDerrota(long playerId) {
        Match peor = null;
        int mayorDiferencia = -1;
        for (Match m : matchDao.partidasDe(playerId)) {
            Long equipo = matchDao.equipoEn(playerId, m.id);
            if (equipo == null || equipo == m.winnerTeamId) {
                continue;
            }
            int diferencia = Math.abs(m.scoreA - m.scoreB);
            if (diferencia > mayorDiferencia) {
                mayorDiferencia = diferencia;
                peor = m;
            }
        }
        return peor;
    }

    public List<Player> integrantes(long teamId) {
        return matchDao.integrantes(teamId);
    }

    // --- Fotos -------------------------------------------------------------------------------

    public LiveData<List<PhotoDao.FotoDeMuseo>> observarMuseo() {
        return photoDao.observarMuseo();
    }

    public LiveData<Integer> derrotasDocumentadas() {
        return matchDao.derrotasDocumentadas();
    }

    public void guardarFoto(Photo photo) {
        io.execute(() -> photoDao.insert(photo));
    }
}
