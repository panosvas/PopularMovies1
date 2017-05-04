package com.udacity.nanodegree.popularmovies1.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

import static net.simonvt.schematic.annotation.DataType.Type.INTEGER;

/**
 * The interface for the Movie Table Columns
 */
public interface MovieColumns {

    @DataType(INTEGER) @PrimaryKey
    @AutoIncrement
    String ID = "_id";

    @DataType(INTEGER)
    String MOVIE_ID = "movieId";

    @DataType(DataType.Type.TEXT) @NotNull
    String TITLE = "title";

    @DataType(DataType.Type.TEXT) @NotNull
    String SYNOPSIS = "synopsis";

    @DataType(DataType.Type.TEXT) @NotNull
    String DATE = "date";

    @DataType(DataType.Type.TEXT) @NotNull
    String RATING = "rating";

    @DataType(DataType.Type.TEXT) @NotNull
    String THUMBNAIL = "thumbnail";

}
