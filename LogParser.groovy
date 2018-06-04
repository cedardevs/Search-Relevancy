import com.sun.org.apache.xpath.internal.operations.Bool
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const

import java.io.File
import java.io.FileWriter
import SearchLog




// ---------------------------------------------------------------------------------
// Still to do/change
//  1. Clear logPaths and logLines after they have been used to free up memory usage
//  2. Implement calculate_common_facets() and calculate_common_pages()
//  3. Refactor get_filters() to utilize find_field_end()
// ---------------------------------------------------------------------------------





// --------------------------------------------------------------------------------------------------
// Description :
//      An object used to parse search query logs and produce usage statistics based on the log files
// --------------------------------------------------------------------------------------------------
class LogParser {

    // Data members
    // ------------
    private List<String> logPaths           // File paths to search logs
    private List<SearchLog> logs            // Individual data from the raw logs
    private List<String> logLines           // The raw (uncut) lines of the relevant logs
    private Map<String, Integer> queryMap   // Map of all queries and their occurrences
    private Map<String, Integer> filterMap  // Map of all filters and their occurrences




    // ---------------------------------------------------------
    // Description :
    //      Default constructor that initalizes all data members
    // ---------------------------------------------------------
    LogParser(){
        initialize_data_members()
    }



    // ---------------------------------------------------------------------------------------------------------------------------
    // Description :
    //      Adds an initial filepath to the object and extracts the lines from the log
    // Params :
    //      paths : a file path or paths to the logs that are to be parsed.
    // ---------------------------------------------------------------------------------------------------------------------------
    LogParser(List<String> paths) {
        int len = paths.size()
        initialize_data_members()

        for (int i = 0; i < len; i++){
            add_log_path(paths[i])
        }
        extract_lines()
        calculate_common_queries()
        calculate_common_filters()
    }





    // --------------------
    // PUBLIC CLASS METHODS
    // --------------------





    // --------------------------------------------------------
    // Description :
    //      Adds a log file to the object based.
    // Params :
    //      filePath : the path to the log in which to be added.
    // --------------------------------------------------------
    void add_log_path(String filePath) {
        this.logPaths.add(filePath)
    }


    // Getters
    // ---------------
    Map<String, Integer> get_queryMap(){
        return this.queryMap
    }

    Map<String, Integer> get_filterMap(){
        return this.filterMap
    }



    // ----------------------------------------------------------------
    // Description :
    //      Helper function to load the lines from all the current logs
    // ----------------------------------------------------------------
    void extract_lines() {

        // Loop through all queued logs
        // ----------------------------
        logPaths.each { String path ->
            File file = new File(path)

            if ( !(file.exists()) ) {
                println("${path} did not open.")
            }

            else {
                // Read each line and add only lines pertaining to 'incoming search parameters'
                // MAY NEED TO CHANGE add_incoming_line() AND parse_line() IF THE LOG FORMAT CHANGES
                // ---------------------------------------------------------------------------------
                file.eachLine('utf-8') {String line ->
                    add_incoming_line(line)
                }
            }
        }
    }



    // -----------------------------------------------------------------------------------------------
    // Description :
    //      Takes this.logs and maps all queries found to the number of times each query has been seen
    // -----------------------------------------------------------------------------------------------
    void calculate_common_queries() {
        int len = this.logs.size()

        for (int i = 0; i < len; i++) {
            String currQuery = this.logs[i].get_search_query()
            add_occurrence(this.queryMap, currQuery)
        }
        this.queryMap = this.queryMap.sort { -it.value }
    }



    // -------------------------------------------------------------------------------------
    // Description :
    //      Takes all the filters and keeps a count of how many times a filter has been seen
    // -------------------------------------------------------------------------------------
    void calculate_common_filters() {
        int j = 0;
        int len = this.logs.size()

        for (int i = 0; i < len; i++){
            int filterLen = this.logs[i].get_filters().size()

            for (j = 0; j < filterLen; j++){
                String currFilter = this.logs[i].get_filters()[j]
                add_occurrence(this.filterMap, currFilter)
            }
        }
        this.filterMap = this.filterMap.sort { -it.value }
    }



    void calculate_common_facets(){

    }

    void calculate_common_pages(){

    }




    // ---------------------
    // PRIVATE CLASS METHODS
    // ---------------------



    // --------------------------------------------------
    // Description :
    //      Initializes the data members for constructors
    // --------------------------------------------------
    private void initialize_data_members(){
        this.logPaths         = []
        this.logs             = []
        this.logLines         = []
        this.queryMap         = [:]
        this.filterMap        = [:]
    }



    // ---------------------------------------------------------------------------------------------
    // Description :
    //      Adds a line to the logLines data member if the line pertains to "incoming search params"
    // ---------------------------------------------------------------------------------------------
    void add_incoming_line(String line){

        final int INCOMING_START = 8        // The index of 'incoming' of the log after the line has been split

        String[] words = line.split()

        if (words[INCOMING_START] == "incoming") {
            this.logLines.add(line)
            parse_line(words)
        }
    }



    // ------------------------------------------------------------------------------------------------
    // Description :
    //      Parses the line in order to determine search queries, filters, facet value, and page/offset
    // Params :
    //      line : the log line in which to parse for its data members
    // ------------------------------------------------------------------------------------------------
    private void parse_line(String[] line) {
        int i                 = 8       // variable for tracking location in the log string starting at queries:[
        int j                 = i+1     // variable for traversing the log string
        int currLevel         = 1       // variable to keep track of what level of brackets the function is in currently
        int len               = 0       // length of workingLine
        String tQuery                   // temporary string for the query
        List<String> tFilters = ["N/A"] // temporary variable to hold filters
        Boolean tFacet        = false   // temporary variable for the facet
        def tPage                       // temporary variable for the page/offset


        // After each field has been found, these are the index increments to reach the following field
        // These are a fixed distance from each field
        // --------------------------------------------------------------------------------------------
        final int SEARCH_START  = 11    // The index of the start of the 'incoming search params' section of the log
        final int FILTERS_START = 10    // The index increment needed to get to the start of the filters section of the log
        final int FACETS_START  = 9     // The index increment needed to get to the start of the facets section of the log
        final int FACETS_ESCAPE = 16    // The index increment needed to reach the start of the page:<max> section


        // Drop everything before 'incoming search params' as well as the outer brackets, and change to a string
        // MAY NEED TO CHANGE THIS AND extract_lines() IF THE LOG FORMAT CHANGES
        // -----------------------------------------------------------------------------------------------------
        String workingLine = line.drop(SEARCH_START).join(" ")
        workingLine = workingLine[1..-2]
        len = workingLine.size()

        // Get text queries
        // ----------------
        j = find_field_end(workingLine, len, i)
        tQuery = get_query_from_line(workingLine, i, j)


        // Get filters
        // -----------
        i = j + FILTERS_START

        if ( (i+1) < len){
            j = find_field_end(workingLine, len, i)
            tFilters = get_filters_from_line(workingLine, i, j)
        }


        // Get Facet
        // ---------
        i = j + FACETS_START
        j = i + 1
        if (j < len){
            tFacet = get_facet_from_line(workingLine, i)
            i += ( FACETS_ESCAPE + bool_to_int(!tFacet) )       // The increment is different for facet = true and facet = false
        }



        // get page and offset
        // -------------------
        j = i + 1
        if (j < len){
            tPage = get_page_from_line(workingLine, i, j)

        }
        else {
            tPage = new Tuple2<Integer, Integer>(0, 0)
        }

        SearchLog tmp = new SearchLog(tQuery, tFilters, tFacet, tPage)
        this.logs.add(tmp)
    }



    // --------------------------------------------------------------------------------------------------------------------------------
    // Description :
    //      A helper function for parse_line() that finds the outer most level of square brackets starting at a given index
    // Params :
    //      line : the string to parse
    //      len : the length of line
    //      startIndex : the starting index in which to traverse in the 'line' parameter (THE INDEX MUST BE A '[' CHARACTER IN 'line')
    // Return:
    //      returns the index of the character AFTER  the outer most closing (']') bracket
    // --------------------------------------------------------------------------------------------------------------------------------
    private int find_field_end(String line, int len, int startIndex){
        int j = startIndex + 1
        int currLevel = 1

        while (currLevel){
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



    // --------------------------------------------------------------------------------
    // Description :
    //      Helper function for parse_line() that gets the search query from a log line
    // Params :
    //      workingLine : the log line for which to extract the query from
    //      startIndex : the starting index of the query field in workingLine
    //      endIndex : the end index of the query field in workingLine
    // --------------------------------------------------------------------------------
    private String get_query_from_line(String workingLine, int startIndex, int endIndex){
        final int QUERIES_START = startIndex + 24       // If there is a query, then it's in a fixed position from the starting index

        // Allegedly cannot get "N/A" as a query in OneStop 2.0
        // ----------------------------------------------------
        if ( (endIndex - startIndex) == 2 ){
            return "N/A"
        }
        else{
            return workingLine[QUERIES_START..(endIndex-3)]
        }
    }



    //
    // Description :
    //      Helper function for parse_line() that gets the filter from a log line
    // Params :
    //      workingLine : the log line for which to extract the filters from
    //      startIndex : the starting index of the filters field in workingLine
    //      endIndex : the end index of the filters field in workingLine
    // --------------------------------------------------------------------------------
    private List<String> get_filters_from_line(String workingLine, int startIndex, int endIndex){
        if ( (endIndex - startIndex) == 2 ){
            return ["N/A"]
        }
        else{
            return get_filter_fields(workingLine[(startIndex+1)..(endIndex-2)])
        }
    }



    // ------------------------------------------------------------------------------------------------------
    // Description :
    //      Breaks down the filter string by its individual fields and returns a list of all fields extracted
    // Params :
    //      filterLine : the filter produced from parse_line()
    // ------------------------------------------------------------------------------------------------------
    private List<String> get_filter_fields(String filterLine) {
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



    // ----------------------------------------------------------------------
    // Description :
    //      Helper function for parse_line() to get the facet from a log line
    // Params :
    //      workingLine : the line from which to get the facet
    //      startIndex : the starting index of the facet field in workingLine
    // ----------------------------------------------------------------------
    private Boolean get_facet_from_line(String workingLine, int startIndex){
        if (workingLine[startIndex] == 't') {
            return true
        }
        else {
            return false
        }
    }



    // -----------------------------------------------
    // Description :
    //      converts a boolean to its integer value
    // Param :
    //      b : the boolean value to convert to 0 or 1
    // -----------------------------------------------
    private int bool_to_int(Boolean b){
        return b.compareTo(false)
    }



    // -----------------------------------------------------------------------------------------
    // Description :
    //      Helper function for parse_line() to get the max and offset of a page from a log line
    // Params :
    //      workingLine : the line from which to get the page max/offset
    //      startIndex : the starting index of the page field in workingLine
    //      endIndex : the end index of the page field in workingLine
    // -----------------------------------------------------------------------------------------
    private Tuple2<Integer, Integer> get_page_from_line(String workingLine, int startIndex, int endIndex){
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