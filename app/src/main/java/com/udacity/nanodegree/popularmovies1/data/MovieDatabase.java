package com.udacity.nanodegree.popularmovies1.data;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

/**
 * The database for storing the Movies
 */
@Database(version = MovieDatabase.VERSION)
public final class MovieDatabase {

    private MovieDatabase(){}

    public static final int VERSION = 2;

    @Table(MovieColumns.class) public static final String MOVIES = "movies";

}
