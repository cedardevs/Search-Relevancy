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
    Date get_date(){
        return this.dateTime
    }


    String get_type(){
        return this.type
    }


    String get_search_query(){
        return this.searchQuery
    }


    List<String> get_filters(){
        return this.filters
    }


    Boolean get_facet(){
        return this.facets
    }


    Tuple2<Integer, Integer> get_page(){
        return this.page
    }


    void set_date(String pDateTime){
        this.dateTime = new Date().parse("yyyy-M-d H:m:s.ms", pDateTime)
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