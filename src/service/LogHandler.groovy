// -----------------------------------------------
// This class holds all of the current search logs
// -----------------------------------------------
class LogHandler {

    private List<SearchLog> oldLogs
    private List<SearchLog> searchLogs
    private List<RequestLog> requestLogs


    LogHandler(List<SearchLog> pOldLogs = [], List<SearchLog> pSearchLogs = [], List<RequestLog> pRequestLogs = []){
        this.oldLogs         = pOldLogs
        this.searchLogs      = pSearchLogs
        this.requestLogs     = pRequestLogs
    }

    void getLogsFromParser(LogParser parser){
        this.oldLogs     = parser.oldSearchLogs
        this.searchLogs  = parser.searchLogs
        this.requestLogs = parser.requestLogs
        parser.initializeDataMembers()
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
}
