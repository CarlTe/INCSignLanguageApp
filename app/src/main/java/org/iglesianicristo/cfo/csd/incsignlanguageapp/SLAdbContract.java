package org.iglesianicristo.cfo.csd.incsignlanguageapp;

import android.provider.BaseColumns;

public final class SLAdbContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private SLAdbContract() {}

    /* Inner class that defines the table contents */
    public static class SLAdbSLA implements BaseColumns {
        public static final String TABLE_NAME = "sla";
        public static final String COL_ID = "_id";
        public static final String COL_WORD = "word";
        public static final String COL_FILE = "file";
        public static final String COL_CAT = "cat";
        public static final String COL_FAVE = "fave";
        public static final String COL_VAR = "var";
    }

    public static final String SQL_CREATE_ENTRIES_SLA =
            "CREATE TABLE " + SLAdbSLA.TABLE_NAME + "(" +
                    SLAdbSLA.COL_ID + " INTEGER PRIMARY KEY," +
                    SLAdbSLA.COL_WORD + " TEXT," +
                    SLAdbSLA.COL_FILE + " INTEGER," +
                    SLAdbSLA.COL_CAT + " TEXT," +
                    SLAdbSLA.COL_FAVE + " INTEGER," +
                    SLAdbSLA.COL_VAR + " TEXT)";

    public static final String SQL_DELETE_ENTRIES_SLA =
            "DROP TABLE IF EXISTS " + SLAdbSLA.TABLE_NAME;


    // categories
    public static class SLAdbCAT implements BaseColumns {
        public static final String TABLE_NAME = "cat";
        public static final String COL_ID = "_id";
        public static final String COL_CAT = "cat";
    }

    public static final String SQL_CREATE_ENTRIES_CAT =
            "CREATE TABLE " + SLAdbCAT.TABLE_NAME + "(" +
                    SLAdbCAT.COL_ID + " TEXT PRIMARY KEY," +
                    SLAdbCAT.COL_CAT + " TEXT)";

    public static final String SQL_DELETE_ENTRIES_CAT =
            "DROP TABLE IF EXISTS " + SLAdbCAT.TABLE_NAME;

}
