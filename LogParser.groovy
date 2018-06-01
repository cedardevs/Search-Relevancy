import com.sun.org.apache.xpath.internal.operations.Bool
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const

import java.io.File
import java.io.FileWriter
import SearchLog




// ---------------------------------------------------------------------------------
// Still to do/change
//  1. Clear logPaths and logLines after they have been used to free up memory usage
//  2. Implement calculate_common_facets() and calculate_common_pages()
//  3. 
// ---------------------------------------------------------------------------------





// --------------------------------------------------------------------------------------------------
// SearchEngineLog
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
    private String queryOutputFile          // The file path to output queryMap
    private String filterOutputFile         // The file path to output filterMap
    private String facetOutputFile          // The file path to output facetMap [NOT IN USE YET]
    private String pageOutputFile           // The file path to output pageMap [NOT IN USE YET]



    LogParser(){
        this.logPaths         = []
        this.logs             = []
        this.logLines         = []
        this.queryMap         = [:]
        this.filterMap        = [:]
        this.queryOutputFile  = ""
        this.filterOutputFile = ""
        this.facetOutputFile  = ""
        this.pageOutputFile   = ""
    }




    // ---------------------------------------------------------------------------------------------------------------------------
    // SearchEngineLog(paths)
    // Description :
    //      Adds an initial filepath to the object and extracts the lines from the log
    // Arguments :
    //      paths: a file path or paths to the logs that are to be parsed.
    // ---------------------------------------------------------------------------------------------------------------------------
    LogParser(List<String> paths) {
        int len = paths.size()
        this.logPaths         = []
        this.logs             = []
        this.logLines         = []
        this.queryMap         = [:]
        this.filterMap        = [:]
        this.queryOutputFile  = ""
        this.filterOutputFile = ""
        this.facetOutputFile  = ""
        this.pageOutputFile   = ""

        for (int i = 0; i < len; i++){
            add_log_path(paths[i])
        }
        extract_lines()
        calculate_common_queries()
        calculate_common_filters()
    }


    // ---------------------------------------------------------------------------------------------------------------------------
    // SearchEngineLog(paths)
    // Description :
    //      Adds an initial filepath to the object and extracts the lines from the log, and outputs the results to specified files
    // Arguments :
    //      paths: a file path or paths to the logs that are to be parsed.
    //      queryFile: the file path in which to output the results of calculate_common_queries()
    //      filterFile: the file path in which to output the results of calculate_common_filters()
    //      facetFile: the file path in which to output the results of calculate_common_facets()
    //      pageFile: the file path in which to output the results of calculate_common_pages()
    // ---------------------------------------------------------------------------------------------------------------------------
    LogParser(List<String> paths, String queryFile, String filterFile, String facetFile, String pageFile) {
        int len = paths.size()
        this.logPaths         = []
        this.logs             = []
        this.logLines         = []
        this.queryMap         = [:]
        this.filterMap        = [:]
        this.queryOutputFile  = queryFile
        this.filterOutputFile = filterFile
        this.facetOutputFile  = facetFile
        this.pageOutputFile   = pageFile

        for (int i = 0; i < len; i++){
            add_log_path(paths[i])
        }

        extract_lines()
        calculate_common_queries()
        calculate_common_filters()
        output_logs(this.queryOutputFile, this.filterOutputFile, this.facetOutputFile, this.pageOutputFile)
    }





    // --------------------
    // PUBLIC CLASS METHODS
    // --------------------





    // --------------------------------------------------------
    // void add_log_path(String filePath)
    // Description :
    //      Adds a log file to the object based.
    // Params :
    //      filePath: the path to the log in which to be added.
    // --------------------------------------------------------
    void add_log_path(String filePath) {
        this.logPaths.add(filePath)
    }


    // Setters
    // -------
    void set_queryOutputFile(String path){
        this.queryOutputFile = path
    }

    void set_filterOutputFile(String path){
        this.filterOutputFile = path
    }

    void set_facetOutputFile(String path){
        this.facetOutputFile = path
    }

    void set_pageOutputFile(String path){
        this.pageOutputFile = path
    }



    // ----------------------------------------------------------------
    // void extract_lines()
    // Description :
    //      Helper function to load the lines from all the current logs
    // ----------------------------------------------------------------
    void extract_lines() {
        int len = this.logPaths.size()

        final int INCOMING_START = 8        // The index of 'incoming' of the log after the line has been split

        // Loop through all queued logs
        // ----------------------------
        for (int i = 0; i < len; i++) {
            File file = new File(this.logPaths[i])

            if ( !(file.exists()) ) {
                println(this.logPaths[i] + " did not open.")
            }

            else {
                // Read each line and add only lines pertaining to 'incoming search parameters'
                // ----------------------------------------------------------------------------
                file.eachLine('utf-8') {String line ->
                    // Accessing this location instead of wasting time splitting the line
                    // MAY NEED TO CHANGE THIS AND parse_line() IF THE LOG FORMAT CHANGES
                    // ------------------------------------------------------------------
                    String[] words = line.split()

                    if (words[INCOMING_START] == "incoming") {
                        this.logLines.add(line)
                        parse_line(words)
                    }
                }
            }
        }
    }



    // -----------------------------------------------------------------------------------------------
    // calculate_common_queries()
    // Description :
    //      Takes this.logs and maps all queries found to the number of times each query has been seen
    // -----------------------------------------------------------------------------------------------
    void calculate_common_queries() {
        int i = 0;
        int len = this.logs.size()

        this.queryMap.put( (this.logs[0].get_search_query()), 1)

        for (i = 1; i < len; i++) {

            // If the term has not been seen before, then add it to the map and set its value to zero
            // --------------------------------------------------------------------------------------
            if ( !(this.queryMap.containsKey(this.logs[i].get_search_query())) ){
                this.queryMap.put( (this.logs[i].get_search_query()), 1)
            }
            else{
                this.queryMap[this.logs[i].get_search_query()] += 1
            }
        }

        // Sort the map by descending values
        // ---------------------------------
        this.queryMap = this.queryMap.sort { -it.value }
    }



    // -------------------------------------------------------------------------------------
    // calculate_common_filters()
    // Description :
    //      Takes all the filters and keeps a count of how many times a filter has been seen
    // -------------------------------------------------------------------------------------
    void calculate_common_filters() {
        int i, j = 0;
        int len = this.logs.size()

        this.filterMap.put( (this.logs[0].get_filters()[0]), 1)

        for (i = 0; i < len; i++){
            int filterLen = this.logs[i].get_filters().size()

            for (j = 0; j < filterLen; j++){
                if ( !(this.filterMap.containsKey(this.logs[i].get_filters()[j])) ){
                    this.filterMap.put( (this.logs[i].get_filters()[j]), 1)
                }
                else{
                    this.filterMap[this.logs[i].get_filters()[j]] += 1
                }
            }
        }
        this.filterMap = this.filterMap.sort { -it.value }
    }



    void calculate_common_facets(){

    }

    void calculate_common_pages(){

    }



    // ---------------------------------------------------------------------------------------
    // output_logs(String oFileName)
    // Description :
    //      Takes the queries from the queryMap and output the key : value to the desired file
    // Params :
    //      pQueryFile  : the file path for the query output
    //      pFilterFile : the file path for the filter output
    //      pFacetFile  : the file path for the facet output
    //      pPageFile   : the file path for the page output
    // ---------------------------------------------------------------------------------------
    void output_logs(String pQueryFile, String pFilterFile, String pFacetFile, String pPageFile) {
        int i = 0;
        int len = logs.size()
        List<String> output = []
        File qFile = new File(pQueryFile)
        File fFile = new File(pFilterFile)
        File facetFile = new File(pFacetFile)
        File pFile = new File(pPageFile)


        if ( !(qFile.exists()) ){
            qFile.createNewFile()
        }
        if ( !(fFile.exists()) ){
            fFile.createNewFile()
        }
        /*
        if ( !(facetFile.exists()) ){
            facetFile.createNewFile()
        }
        if ( !(pFile.exists()) ){
            pFile.createNewFile()
        }
        */


        qFile.withWriter('utf-8') { out ->
            out.println("Query,value")
            this.queryMap.each { out.println("\"${make_csv_compatible(it.key)}\",${it.value}") }
        }

        fFile.withWriter('utf-8') { out ->
            out.println("Filter,value")
            this.filterMap.each { out.println("\"${make_csv_compatible(it.key)}\",${it.value}") }
        }
    }




    // ---------------------
    // PRIVATE CLASS METHODS
    // ---------------------




    // ----------------------------------------------------------------------------------------------------------
    // void parse_line(String line)
    // Description :
    //      Parses the line in order to determine search queries, filters, [FILL OUT THE REST OF THE DESCRIPTION]
    // Params :
    //      line : the log line in which to parse for its data members
    // ----------------------------------------------------------------------------------------------------------
    private void parse_line(String[] line) {
        int i                 = 8       // variable for tracking location in the log string starting at queries:[
        int j                 = i+1     // variable for traversing the log string
        int currLevel         = 1       // variable to keep track of what level of brackets the function is in currently
        int len               = 0       // length of workingLine
        String tQuery                   // temporary string for the query
        List<String> tFilters = []      // temporary variable to hold filters
        Boolean tFacet        = true    // temporary variable for the facet
        def tPage                       // temporary variable for the page/offset

        final int SEARCH_START  = 11    // The start of the 'incoming search params' section of the log
        final int QUERIES_START = 32    // The start of the queries section of the log
        final int FILTERS_START = 10    // The start of the filters section of the log
        final int FACETS_START  = 9     // The start of the facets section of the log
        final int FACETS_TRUE   = 16    // Length needed to reach the end of the facets section if facets = true
        final int FACETS_FALSE  = 17    // Length needed to reach the end of the facets section if facets = false
        final int PAGE_START    = 9     // The start the of page section of the log


        // Drop everything before 'incoming search params' as well as the outer brackets, and change to a string
        // MAY NEED TO CHANGE THIS AND extract_lines() IF THE LOG FORMAT CHANGES
        // -----------------------------------------------------------------------------------------------------
        String workingLine = line.drop(SEARCH_START).join(" ")
        workingLine = workingLine[1..-2]
        len = workingLine.size()

        // get text queries
        // ----------------
        while (currLevel){
            if (workingLine[j] == '['){
                currLevel++
            }
            if (workingLine[j] == ']'){
                currLevel--

                // Exception for a random bracket in the query text
                // ------------------------------------------------
                if ( (j+1) < len ){
                    if ( (workingLine[j+1] != ']' && workingLine[j+1] != ',') ){
                        currLevel++
                    }
                }
            }
            j++
        }



        // Check to see if the text query is empty
        // ALLEGEDLY NOT NECESSARY IN ONESTOP 2.0
        // ---------------------------------------
        if ( (j - i) == 2 ){
            tQuery = "N/A"
        }
        else{
            // There's only one type, which makes the value a fixed starting position in the log
            // ---------------------------------------------------------------------------------
            tQuery = workingLine[QUERIES_START..(j-3)]
        }



        // Get filters
        // Go to the start of the filters section in the log, which is a fixed location after the search query
        // --------------------------------------------------------------------------------------------------
        i = j + FILTERS_START
        j = i + 1
        if (j < len){
            currLevel = 1

            // Get everything within the filter brackets
            // currLevel tracks whether there are additional brackets within filters
            // ---------------------------------------------------------------------
            while (currLevel){
                if (workingLine[j] == '[') {
                    currLevel++
                }
                if (workingLine[j] == ']'){
                    currLevel--
                }
                j++
            }

            if ( (j - i) == 2 ){
                tFilters.add("N/A")
            }
            else{
                // There's only one type, which makes the value a fixed position in the log
                // ------------------------------------------------------------------------
                tFilters.add(workingLine[i..(j-1)])
            }
        }
        else{
            tFilters.add("N/A")
        }

        if (tFilters[0] != "N/A"){
            tFilters = get_filters(workingLine[(i+1)..j-2])
        }



        // Get Facet
        // Go to the start of the facets section in the log, which is a fixed location after the filters
        // ---------------------------------------------------------------------------------------------
        i = j + FACETS_START
        j = i + 1
        if (j < len){
            if (workingLine[i] == 't') {
                tFacet = true
                i += FACETS_TRUE
            }
            else{
                tFacet = false
                i += FACETS_FALSE
            }
        }
        else{
            tFacet = false
        }



        // get page and offset
        // the start of the page/offset section is in a fixed location after the facets section
        // ------------------------------------------------------------------------------------
        j = i + 1
        if (j < len){
            int x, y

            // max:<int>
            // ---------
            while( workingLine[j] != ',' ){
                j++
            }

            x = workingLine[i..(j-1)].toInteger()
            i = j + PAGE_START
            j = i + 1

            // offset:<int>
            // ------------
            while ( workingLine[j] != ']' ){
                j++
            }
            y = workingLine[i..(j-1)].toInteger()

            tPage = new Tuple2<Integer, Integer>(x, y)
        }
        else {
            tPage = new Tuple2<Integer, Integer>(0, 0)
        }

        SearchLog tmp = new SearchLog(tQuery, tFilters, tFacet, tPage)
        this.logs.add(tmp)


    }



    // --------------------------------------------------------------------------------------------------------------
    // get_filters(String filterLine)
    // Description :
    //      Breaks down the filter string by its individual components and returns a list of all components extracted
    // Params :
    //      filterLine : the filter produced from parse_line()
    // --------------------------------------------------------------------------------------------------------------
    private List<String> get_filters(String filterLine) {
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



    // ----------------------------------------------------------------------------------------------------
    // make_csv_compatible(String key)
    // Description :
    //      A helper function to make a query .csv compatible by replacing single quotes with double quotes
    // Params :
    //      key : the string in which to make csv compatible
    // ----------------------------------------------------------------------------------------------------
    private String make_csv_compatible(String key) {
        return (key.replaceAll('"', '""'))
    }
}