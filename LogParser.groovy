import groovy.json.JsonSlurper

// -----------------------------------------------------
// This class is used to parse log files for search logs
// -----------------------------------------------------
class LogParser {


    // ---------------------------------------------------------------------------------------------------------------------------
    // Description :
    //      Adds an initial filepath to the object and extracts the lines from the log
    // Params :
    //      paths : a file path or paths to the logs that are to be parsed.
    // ---------------------------------------------------------------------------------------------------------------------------
    LogParser(List<String> paths = "") {
        initializeDataMembers()

        paths.each {String filePath ->
            addLogPath(filePath)
        }

        readLogs()
    }


    // ----------------------------------------------------------
    // Description :
    //      Adds a log file to the object if the file path exists
    // Params :
    //      filePath : the path to the log in which to be added
    // ----------------------------------------------------------
    void addLogPath(String filePath) {
        File iFile = new File(filePath)

        if ( !(iFile.exists()) ) {
            println("${path} did not open.")
        }
        else{
            this.logPaths.add(filePath)
        }
    }


    // ------------------------------------------------------
    // Description :
    //      Reads all the currently queued logs from logPaths
    // ------------------------------------------------------
    void readLogs() {
        logPaths.each { String path ->
            File file = new File(path)

            file.eachLine('utf-8') {String line ->
                def logLine = isSearchLog(line)

                if (logLine){
                    parseLog(logLine)
                }
            }
        }
        this.logPaths = []
    }


    // ----------------------------------------------------------------
    // pass_logs functions are used to pass logs over to the LogHandler
    // THESE REMOVE ALL CURRENT LOGS FROM THEIR LISTS WHEN CALLED
    // ----------------------------------------------------------------
    List<SearchLog> passOldSearchLogs(){
        List<SearchLog> tmp = this.oldSearchLogs
        this.oldSearchLogs = []
        return tmp
    }


    List<SearchLog> passSearchLogs(){
        List<SearchLog> tmp = this.searchLogs
        this.searchLogs = []
        return tmp
    }


    List<RequestLog> passRequestLogs(){
        List<RequestLog> tmp = this.requestLogs
        this.requestLogs = []
        return tmp
    }



    // ------------------------
    // PRIVATE DATA AND METHODS
    // ------------------------

    private List<String> logPaths           // File paths to search logs
    private List<SearchLog> oldSearchLogs   // Parsed data from old formatted logs
    private List<SearchLog> searchLogs      // Parsed data from new formatted search logs
    private List<RequestLog> requestLogs    // Parsed data from new formatted request logs


    // --------------------------------------------------
    // Description :
    //      Initializes the data members for constructors
    // --------------------------------------------------
    private void initializeDataMembers(){
        this.logPaths         = []
        this.searchLogs       = []
        this.oldSearchLogs    = []
        this.requestLogs      = []
    }


    // -----------------------------------------------------------------------
    // Description :
    //      Determines if the log line pertains to search relevancy
    // Params :
    //      line : the line to determine if is valid
    // Returns :
    //      returns the split line if the line is relevant and false otherwise
    // -----------------------------------------------------------------------
    private def isSearchLog(String line){
        final int PARAM_START = 11        // The index of 'incoming' of the log after the line has been split

        String[] words = line.split()

        if ( words.size() > PARAM_START && (words[PARAM_START].contains("param") || words[PARAM_START-1] == "params:") ) {
            return words
        }
        else{
            return false
        }
    }


    // ------------------------------------------------------------------------------------------
    // Description :
    //      Determines if the line is of an old log format and performs the proper parse function
    // Params :
    //      line : the line the check format
    // ------------------------------------------------------------------------------------------
    private void parseLog(String[] line){
        final int INCOMING_START = 8

        // The old logs only specify "incoming search params:"
        // New log format is "<Method> <collection/granule> <search/ID> <param/params>"
        // -------------------------------------------------------------------------------
        if (line[INCOMING_START] != "incoming"){
            parseNewLogLine(line)
        }
        else{
            parseOldLogLine(line)
        }
    }


    // -------------------------------------------------
    // Description :
    //      Parses the new log format that contains Json
    // Params :
    //      line : the log line to parse
    // -------------------------------------------------
    private void parseNewLogLine(String[] line){
        final int SEARCH_START = 12

        Date date             = getDateFromLine(line)            // date and time of the log
        String type           = getTypeFromLine(line)            // specifies what kind of search
        String tQuery         = ""                                  // temporary string for the query
        List<String> tFilters = []                                  // temporary variable to hold filters
        Boolean tFacet        = false                               // temporary variable for the facet
        def tPage             = new Tuple2<Integer, Integer>(0,0)   // temporary variable for the page/offset

        String workingLine = line.drop(SEARCH_START).join(" ")
        def slurper = new JsonSlurper()
        def log = slurper.parseText(workingLine)

        if (log.containsKey("id")){
            RequestLog tmp = new RequestLog(date, type, log.id)
            this.requestLogs.add(tmp)
            return
        }

        if (log.containsKey("queries")) {
            tQuery = log.queries.value
        }

        if (log.containsKey("filters")) {
            /* Uncomment this for the format to be in JSON
            tFilters = log.filters.collect{it ->
                    JsonOutput.toJson(it)}
            */
            tFilters = log.filters
        }

        if (log.containsKey("facets")){
            tFacet = log.facets
        }

        if (log.containsKey("page")){
            tPage = new Tuple2<Integer, Integer>(log.page.max, log.page.offset)
        }

        SearchLog searchLog = new SearchLog(date, type, tQuery, tFilters, tFacet, tPage)
        this.searchLogs.add(searchLog)
    }

    // ---------------------------------------
    // Description :
    //      Parses the date from the log line
    // Params :
    //      line : the split log line to parse
    // ---------------------------------------
    private Date getDateFromLine(String[] line){
        String date = line[0..1].join(" ")
        Date dateTime = new Date().parse("yyyy-M-d H:m:s.ms", date)
        return dateTime
    }

    // ---------------------------------------
    // Description :
    //      Parses the type from the log line
    // Params :
    //      line : the split log line to parse
    // ---------------------------------------
    private String getTypeFromLine(String[] line){
        final int TYPE_START = 8
        return line[TYPE_START..TYPE_START+2].join(" ")
    }


    // ------------------------------------------------------------------------------------------------
    // Description :
    //      Parses the line in order to determine search queries, filters, facet value, and page/offset
    // Params :
    //      line : the log line in which to parse for its data members
    // ------------------------------------------------------------------------------------------------
    private void parseOldLogLine(String[] line) {
        final int SEARCH_START  = 11    // The index of the start of the 'incoming search params' section of the log
        final int FILTERS_START = 10    // The index increment needed to get to the start of the filters section of the log
        final int FACETS_START  = 9     // The index increment needed to get to the start of the facets section of the log
        final int FACETS_ESCAPE = 16    // The index increment needed to reach the start of the page:<max> section

        int i                 = 8                           // variable for tracking location in the log string starting at queries:[
        int j                 = i+1                         // variable for traversing the log string
        int currLevel         = 1                           // variable to keep track of what level of brackets the function is in currently
        int len               = 0                           // length of workingLine
        Date date             = getDateFromLine(line)       // date and time of the log
        String type           = "incoming search params"    // all types are the same
        String tQuery         = ""                          // temporary string for the query
        List<String> tFilters = []                          // temporary variable to hold filters
        Boolean tFacet        = false                       // temporary variable for the facet
        def tPage                                           // temporary variable for the page/offset


        String workingLine = line.drop(SEARCH_START).join(" ")
        workingLine = workingLine[1..-2]
        len = workingLine.size()

        j = findFieldEnd(workingLine, len, i)
        tQuery = "[${getQueryFromLine(workingLine, i, j)}]"

        i = j + FILTERS_START

        if ( (i+1) < len){
            j = findFieldEnd(workingLine, len, i)
            tFilters = getFiltersFromLine(workingLine, i, j)
        }

        i = j + FACETS_START
        j = i + 1
        if (j < len){
            tFacet = getFacetFromLine(workingLine, i)
            i += ( FACETS_ESCAPE + boolToInt(!tFacet) )       // The increment is different for facet = true and facet = false
        }

        j = i + 1
        if (j < len){
            tPage = getPageFromLine(workingLine, i, j)

        }
        else {
            tPage = new Tuple2<Integer, Integer>(0, 0)
        }

        // An exception needs to be checked if filters and facets doesn't appear at all in the log
        // ---------------------------------------------------------------------------------------

        if (tFilters != [] && tFilters[0][0] == ':'){
            int k = len - tFilters[0].size()
            tPage = getPageFromLine(workingLine, k, k+1)
            tFilters = []
        }
        SearchLog tmp = new SearchLog(date, type, tQuery, tFilters, tFacet, tPage)
        this.oldSearchLogs.add(tmp)
    }



    // -------------------------------------------------------------------
    // The following are helper functions only for use by parseOldLogs()
    // -------------------------------------------------------------------



    // --------------------------------------------------------------------------------------------------------------------------------
    // Description :
    //      A helper function for parseLine() that finds the outer most level of square brackets starting at a given index
    // Params :
    //      line : the string to parse
    //      len : the length of line
    //      startIndex : the starting index in which to traverse in the 'line' parameter (THE INDEX MUST BE A '[' CHARACTER IN 'line')
    // Return:
    //      returns the index of the character AFTER  the outer most closing (']') bracket
    // --------------------------------------------------------------------------------------------------------------------------------
    private int findFieldEnd(String line, int len, int startIndex){
        int j = startIndex + 1
        int currLevel = 1

        while (currLevel && j < len){
            if (line[j] == '['){
                currLevel++
            }
            if (line[j] == ']'){
                currLevel--

                // Exception for a random bracket in the query text
                // ------------------------------------------------
                if ( (j+1) < len ){
                    if ( (line[j+1] != ']' && line[j+1] != ',') ){
                        currLevel++
                    }
                }
            }
            j++
        }
        return j
    }


    private String getQueryFromLine(String workingLine, int startIndex, int endIndex){
        final int QUERIES_START = startIndex + 24       // If there is a query, then it's in a fixed position from the starting index

        // Allegedly cannot get "" as a query in OneStop 2.0
        // -------------------------------------------------
        if ( (endIndex - startIndex) == 2 ){
            return ""
        }
        else{
            return workingLine[QUERIES_START..(endIndex-3)]
        }
    }


    private List<String> getFiltersFromLine(String workingLine, int startIndex, int endIndex){
        if ( (endIndex - startIndex) == 2 ){
            return []
        }
        else{
            return getFilterFields(workingLine[(startIndex+1)..(endIndex-2)])
        }
    }


    private List<String> getFilterFields(String filterLine) {
        int i, j = 1
        int currLevel = 0
        int len = filterLine.size()
        List<String> fields = [];

        // Each filter is enclosed by brackets
        // this section gets everything between the outer most level of brackets and avoids nested brackets
        // ------------------------------------------------------------------------------------------------
        while (i < len){
            currLevel = 1
            while ( currLevel && j < len){
                if (filterLine[j] == '['){
                    currLevel++
                }
                if (filterLine[j] == ']'){
                    currLevel--
                }
                j++
            }
            fields.add(filterLine[i..(j-1)])
            i = j + 2
            j = i + 1
        }
        return fields
    }


    private Boolean getFacetFromLine(String workingLine, int startIndex){
        if (workingLine[startIndex] == 't') {
            return true
        }
        else {
            return false
        }
    }


    private int boolToInt(Boolean b){
        return b.compareTo(false)
    }


    private Tuple2<Integer, Integer> getPageFromLine(String workingLine, int startIndex, int endIndex){
        final int OFFSET_START  = 9     // The start the of page:<offset> section of the log from page:<max> section
        int max, offset

        // max:<int>
        // ---------
        while( workingLine[endIndex] != ',' ){
            endIndex++
        }

        max = workingLine[startIndex..(endIndex-1)].toInteger()
        startIndex = endIndex + OFFSET_START
        endIndex = startIndex + 1

        // offset:<int>
        // ------------
        while ( workingLine[endIndex] != ']' ){
            endIndex++
        }

        offset = workingLine[startIndex..(endIndex-1)].toInteger()
        Tuple2<Integer, Integer> retTuple = new Tuple2<Integer, Integer>(max, offset)
        return retTuple
    }
}