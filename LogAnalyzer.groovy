// ------------------------------------------------------------
// This class is used to calculate desired statistics from logs
// ------------------------------------------------------------
class LogAnalyzer {

    LogAnalyzer(LogHandler pHandler = new LogHandler()){
        this.handler = pHandler
        initialize_maps()
    }


    void calculate_maps(){
        println(this.handler.get_old_logs().size())
        calculate_common_queries()
        calculate_common_filters()
        calculate_common_queries_with_filters()
        calculate_common_requests()
    }


    // ---------------------------------------------------------------------------------------------------
    // Description :
    //      The following calculate the times an entry has been seen in the parsed new and old log formats
    // ---------------------------------------------------------------------------------------------------
    void calculate_common_queries() {
        this.handler.get_old_logs().each { log ->
            add_occurrence(this.commonQueries, log.get_search_query())
        }
        this.handler.get_search_logs().each { log ->
            add_occurrence(this.commonQueries, log.get_search_query())
        }
        this.commonQueries = this.commonQueries.sort { -it.value }
    }


    void calculate_common_filters() {
        this.handler.get_old_logs().each { log ->
            log.get_filters().each { filter ->
                add_occurrence(this.commonFilters, filter.asType(String))
            }
        }
        this.handler.get_search_logs().each { log ->
            log.get_filters().each { filter ->
                add_occurrence(this.commonFilters, filter.asType(String))
            }
        }
        this.commonFilters = this.commonFilters.sort { -it.value }

    }


    void calculate_common_queries_with_filters(){
        this.handler.get_old_logs().each { log ->
            String queryAndFilter = "${log.get_search_query()} ${log.get_filters()}"
            add_occurrence(this.commonQueriesAndFilters, queryAndFilter)
        }
        this.handler.get_search_logs().each { log ->
            String queryAndFilter = "${log.get_search_query()} ${log.get_filters()}"
            add_occurrence(this.commonQueriesAndFilters, queryAndFilter)
        }
        this.commonQueriesAndFilters = this.commonQueriesAndFilters.sort { -it.value }
    }


    void calculate_common_requests() {
        this.handler.get_request_logs().each { log ->
            add_occurrence(this.commonRequests, log.get_id())
        }
        this.commonRequests = this.commonRequests.sort { -it.value }
    }


    // Getters
    // -------
    Map<String, Integer> get_common_queries(){
        return this.commonQueries
    }


    Map<String, Integer> get_common_filters(){
        return this.commonFilters
    }


    Map<String, Integer> get_common_query_filter(){
        return this.commonQueriesAndFilters
    }


    Map<String, Integer> get_common_requests(){
        return this.commonRequests
    }





    // ------------------------
    // PRIVATE DATA AND METHODS
    // ------------------------



    private LogHandler handler
    private Map<String, Integer> commonQueries
    private Map<String, Integer> commonFilters
    private Map<String, Integer> commonQueriesAndFilters
    private Map<String, Integer> commonRequests


    private void initialize_maps(){
        this.commonQueries           = [:]
        this.commonFilters           = [:]
        this.commonQueriesAndFilters = [:]
        this.commonRequests          = [:]
    }


    // --------------------------------------------------------------------
    // Description :
    //      Keeps a count of all the occurrences of 'line' in the given map
    //      Adds a new entry in the map if line has not been seen before
    // Params :
    //      pMap is the map for which to check if 'line' exists
    //      line is a string to add or increment the count in pMap
    // --------------------------------------------------------------------
    private void add_occurrence(Map<String, Integer> pMap, String line){
        if ( !(pMap.containsKey(line)) ){
            pMap.put( (line), 1)
        }
        else{
            pMap[line] += 1
        }
    }

}
