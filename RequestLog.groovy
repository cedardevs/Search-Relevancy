// -----------------------------------------------------------
// A type of search log used when a user requests a collection
// -----------------------------------------------------------
class RequestLog {
    private Date dateTime
    private String type
    private String id

    RequestLog(Date pDateTime = new Date(), String pType = "", String pId = ""){
        this.dateTime = pDateTime
        this.type     = pType
        this.id       = pId
    }


    Date getDate(){
        return this.dateTime
    }


    String getType(){
        return this.type
    }


    String getId(){
        return this.id
    }
}
