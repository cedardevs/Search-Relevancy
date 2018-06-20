// -----------------------------------------------------
// This class is used to output log maps in a CSV format
// -----------------------------------------------------
class LogCSVWriter {

    String headerName                   // The name of the header for the map (i.e., "queries", "filters", "facets", "page")
    Map<String, Integer> outputMap      // The map in which to output to a CSV file
    String outputPath                          // The file path for which to output

    LogCSVWriter(String name = "", Map<String, Integer> map = [:], String path = ""){
        this.headerName = name
        this.outputMap  = map
        this.outputPath = path
        outputMapToFile()
    }

    // ---------------------------------------------------------------------------------
    // Description :
    //      outputs the contents of outputMap to the class's outputPath in a .CSV format
    //      creates a new file at the designated output path if the file does not exist
    // ---------------------------------------------------------------------------------
    void outputMapToFile(){
        File oFile = new File(this.outputPath)

        if ( !(oFile.exists()) ){
            oFile.createNewFile()
        }
        oFile.withWriter('utf-8') { out ->
            out.println("${this.headerName},value")
            this.outputMap.each { out.println("\"${makeCsvCompatible(it.key)}\",${it.value}") }
        }
    }


    private String makeCsvCompatible(String key) {
        String singleQuote = '"'
        String doubleQuote = '""'
        return (key.replaceAll(singleQuote, doubleQuote))
    }
}
