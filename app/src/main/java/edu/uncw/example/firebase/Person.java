package edu.uncw.example.firebase;

import java.util.Date;

class Person {

    private String first;
    private String last;
    private String userId;
    private Date createdTime;

    // No-argument constructor is required to support conversion of Firestore document to POJO
    public Person() {}

    // All-argument constructor is required to support conversion of Firestore document to POJO
    public Person(String first, String last, String userId, Date createdTime) {
        this.first = first;
        this.last = last;
        this.userId = userId;
        this.createdTime = createdTime;
    }

    public String getFirst() {
        return first;
    }

    public String getLast() {
        return last;
    }

    public String getUserId() {
        return userId;
    }

    public Date getCreatedTime(){
        return createdTime;
    }
}
