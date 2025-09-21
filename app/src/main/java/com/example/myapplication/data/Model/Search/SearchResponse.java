package com.example.myapplication.data.Model.Search;

import com.example.myapplication.data.Model.Property.SearchProperty;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Lớp chính để parse kết quả trả về từ API Algolia
 */
public class SearchResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("results")
    private SearchResults results;

    public boolean isSuccess() {
        return success;
    }

    public SearchResults getResults() {
        return results;
    }

    /**
     * Lớp con chứa kết quả tìm kiếm
     */
    public static class SearchResults {
        @SerializedName("hits")
        private List<SearchProperty> hits;

        @SerializedName("nbHits")
        private int nbHits;

        @SerializedName("page")
        private int page;

        @SerializedName("nbPages")
        private int nbPages;

        @SerializedName("hitsPerPage")
        private int hitsPerPage;

        @SerializedName("exhaustiveNbHits")
        private boolean exhaustiveNbHits;

        @SerializedName("exhaustiveTypo")
        private boolean exhaustiveTypo;

        @SerializedName("query")
        private String query;

        @SerializedName("params")
        private String params;

        @SerializedName("processingTimeMS")
        private int processingTimeMS;

        public List<SearchProperty> getHits() {
            return hits;
        }

        public int getNbHits() {
            return nbHits;
        }

        public int getPage() {
            return page;
        }

        public int getNbPages() {
            return nbPages;
        }

        public int getHitsPerPage() {
            return hitsPerPage;
        }

        public boolean isExhaustiveNbHits() {
            return exhaustiveNbHits;
        }

        public boolean isExhaustiveTypo() {
            return exhaustiveTypo;
        }

        public String getQuery() {
            return query;
        }

        public String getParams() {
            return params;
        }

        public int getProcessingTimeMS() {
            return processingTimeMS;
        }
    }
}