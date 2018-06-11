// -----------------------------------------------------------------
// class SearchLog
// Description :
//      Used to hold the information obtained from an individual log
// -----------------------------------------------------------------
class SearchLog {
    private String type                         // Is this a granule or collection search?
    private String searchQuery                  // The search query obtained from a log
    private List<String> filters                // A list of filters applied in a log
    private Boolean facets                      // The facet obtained from a log
    private Tuple2<Integer, Integer> page       // The max and offset of a log

    // Constructor(s)
    // --------------

    SearchLog() {
        this.type = ""
        this.searchQuery = ""
        this.filters = []
        this.facets = false
        this.page = new Tuple2<Integer, Integer>(0, 0)
    }

    SearchLog(pQuery, pFilters, pFacets, pPage){
        this.searchQuery = pQuery
        this.filters = pFilters
        this.facets = pFacets
        this.page = pPage
    }

    // Getters/Setters
    // -------
    String get_search_query(){
        return this.searchQuery
    }

    List<String> get_filters(){
        return this.filters
    }

    Boolean get_facet(){
        return this.facets
    }

    def get_page(){
        return this.page
    }

    void set_search_query(String pQuery){
        this.searchQuery = pQuery
    }

    void set_filters(List<String> pFilters){
        this.filters = pFilters
    }

    void set_facet(Boolean pFacets){
        this.facets = pFacets
    }

    void set_page(Tuple2<Integer, Integer> pPage){
        this.page = pPage
    }

}