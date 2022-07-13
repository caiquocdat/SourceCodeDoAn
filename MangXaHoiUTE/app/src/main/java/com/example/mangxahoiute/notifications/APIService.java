package com.example.mangxahoiute.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAGC9r4qw:APA91bEor4ThpB1mVMNoHKcLp8b1yvqodAMqzFzL6KPjOumBql4qGijg-RjuOvNMQoMGfnFWtdtIPDnyDZLQnA_xu5OgrMlYWA3KNJV8nqthf0zrgTAZVtB6bf_M0PO3W1TPeGA2LKjl"
    })
    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);

}
