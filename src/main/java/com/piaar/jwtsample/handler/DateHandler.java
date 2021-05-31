package com.piaar.jwtsample.handler;

import java.util.Calendar;
import java.util.Date;

import org.springframework.stereotype.Component;

@Component
public class DateHandler {
    public Date getCurrentDate(){
        Date date = Calendar.getInstance().getTime();
        return date;
    }
}
