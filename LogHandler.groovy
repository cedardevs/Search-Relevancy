// -----------------------------------------------
// This class holds all of the current search logs
// -----------------------------------------------
class LogHandler {

    LogHandler(List<SearchLog> pOldLogs = [], List<SearchLog> pSearchLogs = [], List<RequestLog> pRequestLogs = []){
        this.oldLogs         = pOldLogs
        this.searchLogs      = pSearchLogs
        this.requestLogs     = pRequestLogs
        this.oldLogCount     = pOldLogs.size()
        this.searchLogCount  = pSearchLogs.size()
        this.requestLogCount = pRequestLogs.size()
    }

    // Getters/Setters
    // ---------------

    void get_logs_from_parser(LogParser parser){
        this.oldLogs = parser.pass_old_search_logs()
        this.searchLogs = parser.pass_search_logs()
        this.requestLogs = parser.pass_request_logs()
        this.oldLogCount = this.oldLogs.size()
        this.searchLogCount = this.searchLogs.size()
        this.requestLogCount = this.requestLogs.size()
    }


    List<SearchLog> get_old_logs(){
        return this.oldLogs
    }


    List<SearchLog> get_search_logs(){
        return this.searchLogs
    }


    List<RequestLog> get_request_logs(){
        return this.requestLogs
    }


    void add_old_search_log(SearchLog log){
        this.oldLogs.add(log)
        this.oldLogCount++
    }


    void add_search_log(SearchLog log){
        this.searchLogs.add(log)
        this.searchLogCount++
    }


    void add_request_log(RequestLog log){
        this.requestLogs.add(log)
        this.requestLogCount++
    }



    // ------------------------
    // PRIVATE DATA AND METHODS
    // ------------------------

    private List<SearchLog> oldLogs
    private List<SearchLog> searchLogs
    private List<RequestLog> requestLogs
    private int oldLogCount
    private int searchLogCount
    private int requestLogCount
}
