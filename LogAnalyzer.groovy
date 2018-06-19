// ------------------------------------------------------------
// This class is used to calculate desired statistics from logs
// ------------------------------------------------------------
class LogAnalyzer {

    LogAnalyzer(LogHandler pHandler = new LogHandler()){
        this.handler = pHandler
        initializeMaps()
    }


    void calculateMaps(){
        calculateCommonQueries()
        calculateCommonFilters()
        calculateCommonQueriesWithFilters()
        calculateCommonRequests()
    }


    // ---------------------------------------------------------------------------------------------------
    // Description :
    //      The following calculate the times an entry has been seen in the parsed new and old log formats
    // ---------------------------------------------------------------------------------------------------
    void calculateCommonQueries() {
        this.handler.getOldLogs().each { log ->
            addOccurrence(this.commonQueries, log.getSearchQuery())
        }
        this.handler.getSearchLogs().each { log ->
            addOccurrence(this.commonQueries, log.getSearchQuery())
        }
        this.commonQueries = this.commonQueries.sort { -it.value }
    }


    void calculateCommonFilters() {
        this.handler.getOldLogs().each { log ->
            log.getFilters().each { filter ->
                addOccurrence(this.commonFilters, filter.asType(String))
            }
        }
        this.handler.getSearchLogs().each { log ->
            log.getFilters().each { filter ->
                addOccurrence(this.commonFilters, filter.asType(String))
            }
        }
        this.commonFilters = this.commonFilters.sort { -it.value }

    }


    void calculateCommonQueriesWithFilters(){
        this.handler.getOldLogs().each { log ->
            String queryAndFilter = "${log.getSearchQuery()} ${log.getFilters()}"
            addOccurrence(this.commonQueriesAndFilters, queryAndFilter)
        }
        this.handler.getSearchLogs().each { log ->
            String queryAndFilter = "${log.getSearchQuery()} ${log.getFilters()}"
            addOccurrence(this.commonQueriesAndFilters, queryAndFilter)
        }
        this.commonQueriesAndFilters = this.commonQueriesAndFilters.sort { -it.value }
    }


    void calculateCommonRequests() {
        this.handler.getRequestLogs().each { log ->
            addOccurrence(this.commonRequests, log.getId())
        }
        this.commonRequests = this.commonRequests.sort { -it.value }
    }


    // Getters
    // -------
    Map<String, Integer> getCommonQueries(){
        return this.commonQueries
    }


    Map<String, Integer> getCommonFilters(){
        return this.commonFilters
    }


    Map<String, Integer> getCommonQueryFilter(){
        return this.commonQueriesAndFilters
    }


    Map<String, Integer> getCommonRequests(){
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


    private void initializeMaps(){
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
    private void addOccurrence(Map<String, Integer> pMap, String line){
        if ( !(pMap.containsKey(line)) ){
            pMap.put( (line), 1)
        }
        else{
            pMap[line] += 1
        }
    }

}
