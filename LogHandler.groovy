class LogHandler {
    private List<SearchLog> oldLogs
    private List<SearchLog> searchLogs
    private List<RequestLog> requestLogs
    private int oldLogCount
    private int searchLogCount
    private int requestLogCount

    LogHandler(List<SearchLog> pOldLogs = [], List<SearchLog> pSearchLogs = [], List<RequestLog> pRequestLogs = []){
        this.oldLogs         = pOldLogs
        this.searchLogs      = pSearchLogs
        this.requestLogs     = pRequestLogs
        this.oldLogCount     = pOldLogs.size()
        this.searchLogCount  = pSearchLogs.size()
        this.requestLogCount = pRequestLogs.size()
    }

    void print_data(){
        this.oldLogs.each { SearchLog tmp ->
            println("${tmp.get_date()}\t${tmp.get_type()}\t${tmp.get_search_query()}\t${tmp.get_filters()}\t${tmp.get_facet()}\t${tmp.get_page()}")

        }
        println("Size: ${this.oldLogCount}")
        this.searchLogs.each { SearchLog tmp ->
            println("${tmp.get_date()}\t${tmp.get_type()}\t${tmp.get_search_query()}\t${tmp.get_filters()}\t${tmp.get_facet()}\t${tmp.get_page()}")

        }
        println("Size: ${this.searchLogCount}")

        this.requestLogs.each { RequestLog tmp ->
            println("${tmp.get_date()}\t${tmp.get_type()}\t${tmp.get_id()}")
        }
        println("Size: ${this.requestLogCount}")

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
}
