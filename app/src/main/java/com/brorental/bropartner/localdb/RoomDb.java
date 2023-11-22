package com.brorental.bropartner.localdb;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;

import kotlin.jvm.Volatile;

@Database(entities = StateEntity.class, exportSchema = false, version = 1)
public abstract class RoomDb extends androidx.room.RoomDatabase {
    public abstract StateDao getStateDao();
    @Volatile
    private static RoomDb db = null;
    private static String DB_NAME = "db";
    public static synchronized RoomDb getInstance(Context context) {
        if(db == null) {
//            Migration migration = new Migration(1, 2) {
//                @Override
//                public void migrate(@NonNull SupportSQLiteDatabase supportSQLiteDatabase) {
//
//                }
//            };
            db = Room.databaseBuilder(context, RoomDb.class, DB_NAME)
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }

        return db;
    }
}
