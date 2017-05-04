package com.udacity.nanodegree.popularmovies1.Objects;

import org.parceler.Parcel;

import java.util.List;

@Parcel(Parcel.Serialization.BEAN)
public class ReviewsPojo {

    private Integer id;
    private Integer page;
    private Integer total_pages;
    private Integer total_results;
    private List<ReviewPojo> results;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getTotal_pages() {
        return total_pages;
    }

    public void setTotal_pages(Integer total_pages) {
        this.total_pages = total_pages;
    }

    public Integer getTotal_results() {
        return total_results;
    }

    public void setTotal_results(Integer total_results) {
        this.total_results = total_results;
    }

    public List<ReviewPojo> getResults() {
        return results;
    }

    public void setResults(List<ReviewPojo> results) {
        this.results = results;
    }
}
