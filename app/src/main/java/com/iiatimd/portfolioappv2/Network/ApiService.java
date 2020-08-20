package com.iiatimd.portfolioappv2.Network;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import com.iiatimd.portfolioappv2.Entities.AccessToken;
import com.iiatimd.portfolioappv2.Entities.Project;
import com.iiatimd.portfolioappv2.Entities.ProjectCall;
import com.iiatimd.portfolioappv2.Entities.ProjectResponse;
import com.iiatimd.portfolioappv2.Entities.User;

public interface ApiService {

    @POST("register")
    @FormUrlEncoded
    Call<AccessToken> register(@Field("email") String email, @Field("password") String password);

    @POST("login")
    @FormUrlEncoded
    Call<AccessToken> login(@Field("email") String email, @Field("password") String password);

    @POST("refresh")
    @FormUrlEncoded
    Call<AccessToken> refresh(@Field("refresh_token") String refreshToken);

    @POST("logout")
    @FormUrlEncoded
    Call<AccessToken> logout(@Field("access_token") AccessToken accessToken);

    @POST("save_user_info")
    @FormUrlEncoded
    Call<AccessToken> save_user_info(@Field("name") String name, @Field("lastname") String lastname);

    @GET("projects")
    Call<ProjectCall> projects();

    @POST("projects/create")
    @FormUrlEncoded
    Call<ProjectResponse> save_project(@Field("name") String name, @Field("website") String website, @Field("client") String client,
                                       @Field("hours") String hours,@Field("desc") String desc);

    @GET("user")
    Call<User> user();
}
