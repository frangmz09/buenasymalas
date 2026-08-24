package com.frangomez.buenasymalas.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PlayerDao {

    @Insert
    long insert(Player player);

    @Update
    void update(Player player);

    @Delete
    void delete(Player player);

    @Query("SELECT * FROM player ORDER BY created_at")
    LiveData<List<Player>> observarTodos();

    @Query("SELECT * FROM player ORDER BY created_at")
    List<Player> todos();

    @Query("SELECT * FROM player WHERE id = :id")
    LiveData<Player> observar(long id);

    @Query("SELECT * FROM player WHERE id = :id")
    Player porId(long id);

    @Query("SELECT COUNT(*) FROM player")
    int cuantos();
}
