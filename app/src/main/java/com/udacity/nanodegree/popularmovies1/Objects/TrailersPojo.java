package com.udacity.nanodegree.popularmovies1.Objects;

import org.parceler.Parcel;

import java.util.List;

@Parcel(Parcel.Serialization.BEAN)
public class TrailersPojo {

    private Integer id;
    private List<TrailerPojo> results;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<TrailerPojo> getResults() {
        return results;
    }

    public void setResults(List<TrailerPojo> results) {
        this.results = results;
    }
}
