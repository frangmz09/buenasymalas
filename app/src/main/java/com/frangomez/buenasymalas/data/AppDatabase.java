package com.frangomez.buenasymalas.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(
        entities = {Player.class, Team.class, TeamMember.class, Match.class, Photo.class},
        version = 1,
        exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instancia;

    public abstract PlayerDao playerDao();

    public abstract MatchDao matchDao();

    public abstract PhotoDao photoDao();

    public static AppDatabase getInstance(Context context) {
        if (instancia == null) {
            synchronized (AppDatabase.class) {
                if (instancia == null) {
                    instancia = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "buenasymalas.db")
                            .build();
                }
            }
        }
        return instancia;
    }
}
