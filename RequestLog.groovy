class RequestLog {
    private Date dateTime
    private String type
    private String id

    RequestLog(Date pDateTime = new Date(), String pType = "", String pId = ""){
        this.dateTime = pDateTime
        this.type = pType
        this.id = pId
    }

    Date get_date(){
        return this.dateTime
    }

    String get_type(){
        return this.type
    }

    String get_id(){
        return this.id
    }
}
