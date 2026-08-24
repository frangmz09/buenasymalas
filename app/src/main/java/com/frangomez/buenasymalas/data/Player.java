package com.frangomez.buenasymalas.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Alguien que se sienta a la mesa. El alias es el apodo fijo que le queda cuando pierde. */
@Entity(tableName = "player")
public class Player {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    @ColumnInfo(name = "name")
    public String name = "";

    /** "El Perro". Se muestra en inicio, fin de partida, galeria y perfil. */
    @NonNull
    @ColumnInfo(name = "alias")
    public String alias = "";

    /** Color de su mitad de cancha. Lo pinta juegue de que lado juegue. */
    @ColumnInfo(name = "color")
    public int color;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    public Player() {
    }

    @androidx.room.Ignore
    public Player(@NonNull String name, @NonNull String alias, int color) {
        this.name = name;
        this.alias = alias;
        this.color = color;
        this.createdAt = System.currentTimeMillis();
    }

    /** La letra del avatar circular. */
    public String inicial() {
        return name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase();
    }
}
