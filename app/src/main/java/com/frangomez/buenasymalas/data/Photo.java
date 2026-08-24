package com.frangomez.buenasymalas.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * La foto del perdedor.
 *
 * <p>El archivo se guarda crudo: el marcador, la fecha y el apodo se dibujan encima al
 * mostrarla. Asi, si manana cambia el apodo del perdedor, cambia en todas sus fotos.
 */
@Entity(
        tableName = "photo",
        indices = {@Index("match_id"), @Index("loser_player_id")},
        foreignKeys = {
                @ForeignKey(entity = Match.class, parentColumns = "id", childColumns = "match_id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Player.class, parentColumns = "id",
                        childColumns = "loser_player_id", onDelete = ForeignKey.CASCADE)
        })
public class Photo {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "match_id")
    public long matchId;

    @NonNull
    @ColumnInfo(name = "file_path")
    public String filePath = "";

    /** El titulo de la chicana en el museo. */
    @NonNull
    @ColumnInfo(name = "caption")
    public String caption = "";

    @ColumnInfo(name = "loser_player_id")
    public long loserPlayerId;
}
