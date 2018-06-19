// --------------------------------------------------------------
// A type of search log used when a user uses the search function
// --------------------------------------------------------------
class SearchLog {

    SearchLog(Date pDateTime = new Date(), String pType = "", String pQuery = "", List<String> pFilters = [], Boolean pFacets = false, Tuple2<Integer, Integer> pPage = new Tuple2<Integer, Integer>(0,0)) {
        this.dateTime = pDateTime
        this.type = pType
        this.searchQuery = pQuery
        this.filters = pFilters
        this.facets = pFacets
        this.page = pPage
    }

    // Getters/Setters
    // -------
    Date getDate(){
        return this.dateTime
    }


    String getType(){
        return this.type
    }


    String getSearchQuery(){
        return this.searchQuery
    }


    List<String> getFilters(){
        return this.filters
    }


    Boolean getFacet(){
        return this.facets
    }


    Tuple2<Integer, Integer> getPage(){
        return this.page
    }


    void setDate(String pDateTime){
        this.dateTime = new Date().parse("yyyy-M-d H:m:s.ms", pDateTime)
    }


    void setSearchQuery(String pQuery){
        this.searchQuery = pQuery
    }


    void setFilters(List<String> pFilters){
        this.filters = pFilters
    }


    void setFacet(Boolean pFacets){
        this.facets = pFacets
    }


    void setPage(Tuple2<Integer, Integer> pPage){
        this.page = pPage
    }




    // ------------------------
    // PRIVATE DATA AND METHODS
    // ------------------------

    private Date dateTime                       // Date of the log occurred
    private String type                         // Is this a granule or collection search?
    private String searchQuery                  // The search query obtained from a log
    private List<String> filters                // A list of filters applied in a log
    private Boolean facets                      // The facet obtained from a log
    private Tuple2<Integer, Integer> page       // The max and offset of a log

}