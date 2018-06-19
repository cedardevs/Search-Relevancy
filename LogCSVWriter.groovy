// -----------------------------------------------------
// This class is used to output log maps in a CSV format
// -----------------------------------------------------
class LogCSVWriter {

    LogCSVWriter(String name = "", Map<String, Integer> map = [:], String path = ""){
        set_header_name(name)
        set_map(map)
        set_output_path(path)
        output_map_to_file()
    }


    // Getters/Setters
    // ---------------
    String get_header_name(){
        return this.headerName
    }


    Map<String, Integer> get_map(){
        return this.outputMap
    }


    String get_output_path(){
        return this.outputPath
    }


    void set_header_name(String name){
        this.headerName = name
    }


    void set_map(Map<String, Integer> map){
        this.outputMap = map
    }


    void set_output_path(String path){
        this.outputPath = path
    }


    // ---------------------------------------------------------------------------------
    // Description :
    //      outputs the contents of outputMap to the class's outputPath in a .CSV format
    //      creates a new file at the designated output path if the file does not exist
    // ---------------------------------------------------------------------------------
    void output_map_to_file(){
        File oFile = new File(this.outputPath)

        if ( !(oFile.exists()) ){
            oFile.createNewFile()
        }

        oFile.withWriter('utf-8') { out ->
            out.println("${this.headerName},value")
            this.outputMap.each { out.println("\"${make_csv_compatible(it.key)}\",${it.value}") }
        }
    }



    // --------------------------------
    // PRIVATE DATA MEMBERS AND METHODS
    // --------------------------------

    private String headerName                   // The name of the header for the map (i.e., "queries", "filters", "facets", "page")
    private Map<String, Integer> outputMap      // The map in which to output to a CSV file
    private outputPath                          // The file path for which to output


    // -------------------------------------------------------------------------------------------------
    // Description :
    //      A helper function to make text .csv compatible by replacing single quotes with double quotes
    // Params :
    //      key : the string in which to make csv compatible
    // -------------------------------------------------------------------------------------------------
    private String make_csv_compatible(String key) {
        String singleQuote = '"'
        String doubleQuote = '""'
        return (key.replaceAll(singleQuote, doubleQuote))
    }
}
