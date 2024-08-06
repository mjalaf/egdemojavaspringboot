package com.egnamespace.eventing.egeventing;

import java.sql.Date;

public class Payload {
    // user name and lastname properties also add a constructure
    private String tableName;
    private String data;
    private Date date;


    protected Payload() {
    }

    public Payload(String tableName, String data, Date date) {
        this.tableName = tableName;
        this.data = data;
        this.date = date;   
    }   

    public String getTableName() {
        return this.tableName;
    }

    public String getData() {
        return this.data;
    }
    
    public Date getDate() {
        return this.date;
    }



}

