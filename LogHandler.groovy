// -----------------------------------------------
// This class holds all of the current search logs
// -----------------------------------------------
class LogHandler {

    LogHandler(List<SearchLog> pOldLogs = [], List<SearchLog> pSearchLogs = [], List<RequestLog> pRequestLogs = []){
        this.oldLogs         = pOldLogs
        this.searchLogs      = pSearchLogs
        this.requestLogs     = pRequestLogs
    }

    // Getters/Setters
    // ---------------

    void getLogsFromParser(LogParser parser){
        this.oldLogs = parser.passOldSearchLogs()
        this.searchLogs = parser.passSearchLogs()
        this.requestLogs = parser.passRequestLogs()
    }


    List<SearchLog> getOldLogs(){
        return this.oldLogs
    }


    List<SearchLog> getSearchLogs(){
        return this.searchLogs
    }


    List<RequestLog> getRequestLogs(){
        return this.requestLogs
    }


    void addOldSearchLog(SearchLog log){
        this.oldLogs.add(log)
    }


    void addSearchLog(SearchLog log){
        this.searchLogs.add(log)
    }


    void addRequestLog(RequestLog log){
        this.requestLogs.add(log)
    }



    // ------------------------
    // PRIVATE DATA AND METHODS
    // ------------------------

    private List<SearchLog> oldLogs
    private List<SearchLog> searchLogs
    private List<RequestLog> requestLogs
}
