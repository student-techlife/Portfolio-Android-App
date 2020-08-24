package com.iiatimd.portfolioappv2.Network;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

import com.iiatimd.portfolioappv2.Entities.AccessToken;
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
    Call<AccessToken> save_user_info(@Field("name") String name, @Field("lastname") String lastname, @Field("photo") String photo);

    @GET("projects")
    Call<ProjectCall> projects();

    @GET("projects/my_projects")
    Call<ProjectCall> myProjects();

    @POST("projects/delete")
    @FormUrlEncoded
    Call<ProjectResponse> delete_project(@Field("id") int id);

    @POST("projects/create")
    @FormUrlEncoded
    Call<ProjectResponse> save_project(@Field("name") String name, @Field("website") String website, @Field("client") String client,
                                       @Field("photo") String photo, @Field("hours") String hours, @Field("desc") String desc);

    @GET("user")
    Call<User> user();
}
