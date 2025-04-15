package com.chupapis.bookit.data.network

import com.chupapis.bookit.data.model.admins.ChangeUserRequest
import com.chupapis.bookit.data.model.admins.ChangeUserResponse
import com.chupapis.bookit.data.model.admins.ClientResponse
import com.chupapis.bookit.data.model.admins.UpdateBookingSeatRequest
import com.chupapis.bookit.data.model.admins.UpdateBookingSeatResponse
import com.chupapis.bookit.data.model.booking.BookingRequest
import com.chupapis.bookit.data.model.login.LoginRequest
import com.chupapis.bookit.data.model.login.LoginResponse
import com.chupapis.bookit.data.model.register.RegisterRequest
import com.chupapis.bookit.data.model.register.RegisterResponse
import com.chupapis.bookit.data.model.token.RefreshTokenRequest
import com.chupapis.bookit.data.model.token.RefreshTokenResponse
import com.chupapis.bookit.data.model.user.UserResponse
import com.chupapis.bookit.data.model.booking.BookingResponse
import com.chupapis.bookit.data.model.booking.BookingResponseChange
import com.chupapis.bookit.data.model.booking.BookingResponseGetBookingByUserID
import com.chupapis.bookit.data.model.booking.MyBookingResponse
import com.chupapis.bookit.data.model.booking.OccupiedSeatsResponse
import com.chupapis.bookit.data.model.coworking.CoworkingResponse
import com.chupapis.bookit.data.model.figure.Figure
import com.chupapis.bookit.data.model.invite.AcceptRequest
import com.chupapis.bookit.data.model.qr.CheckQrRequest
import com.chupapis.bookit.data.model.qr.CheckQrResponse
import com.chupapis.bookit.data.model.seats.FreeSeatResponse
import com.chupapis.bookit.ui.verification.PassportData
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("/users")
    suspend fun registerUser(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("/users/auth")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    @POST("/users/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<RefreshTokenResponse>

    @GET("/users/me")
    suspend fun getUserProfile(@Header("Authorization") authorization: String): Response<UserResponse>

    /*@GET("/admins/bookings")
    suspend fun getUserBookings(@Header("Authorization") authorization: String): Response<List<BookingResponse>>*/
    @GET("/admins/bookings/{user_id}")
    suspend fun getUserBookingsByUserId(
        @Path("user_id") userId: String,
        @Header("Authorization") authorization: String
    ): Response<List<BookingResponseGetBookingByUserID>>

    @GET("/booking/my/{coworking_id}")
    suspend fun getMyBookings(@Header("Authorization") authorization: String, @Path("coworking_id") coworkingId: String): Response<List<MyBookingResponse>>

    @DELETE("/users/session")
    suspend fun logoutUser(@Header("Authorization") authorization: String): Response<Unit>

    @GET("/admins/all_clients")
    suspend fun getAllClients(@Header("Authorization") authorization: String): Response<List<ClientResponse>>

    @GET("/admins/bookings/{user_id}")
    suspend fun getUserBookings(
        @Path("user_id") userId: String,
        @Header("Authorization") authorization: String
    ): Response<List<BookingResponse>>

    @PATCH("/booking/seat/{booking_id}")
    suspend fun changeBookSeat(
        @Path("booking_id") bookingId: String,
        @Body request: BookingRequest
    ): Response<BookingResponseChange>

    @GET("/booking/seat/occupied")
    suspend fun getOccupiedSeats(
        @Query("seat_uuids") seatUuids: List<String>,
        @Query("days") days: List<String>
    ): Response<List<OccupiedSeatsResponse>>


//    @GET("/coworkings/seats/{coworking_id}")
//    suspend fun getSeatsInCoworking(
//        @Path("coworking_id") coworking_id: String
//    ): Response<List<SeatResponse>>

    @GET("/booking/{coworking_id}/free_seats")
    suspend fun getFreeSeats(
        @Header("Authorization") authorization: String,
        @Path("coworking_id") coworkingId: String,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("tags") tags: List<String> = emptyList()
    ): Response<List<FreeSeatResponse>>


    @DELETE("/admins/{user_id}")
    suspend fun deleteUser(
        @Path("user_id") userId: String,
        @Header("Authorization") authorization: String
    ): Response<Unit>

    @POST("/admins/check_qr")
    suspend fun checkQr(
        @Header("Authorization") auth: String,
        @Body request: CheckQrRequest
    ): Response<CheckQrResponse>

    @POST("/booking/seat")
    suspend fun bookSeat(
        @Header("Authorization") authorization: String,
        @Body qr_data: BookingRequest
    ): Response<Unit>

    @PATCH("/admins/user/{user_id}")
    suspend fun changeUser(
        @Path("user_id") userId: String,
        @Body request: ChangeUserRequest,
        @Header("Authorization") authorization: String
    ): Response<ChangeUserResponse>

    @GET("/coworkings")
    suspend fun getCoworkings(
        @Header("Authorization") authorization: String
    ): Response<List<CoworkingResponse>>

    @PATCH("/booking/seat/{booking_id}")
    suspend fun updateSeatBookingReservation(
        @Header("Authorization") authorization: String,
        @Path("booking_id") bookingId: String,
        @Body request: UpdateBookingSeatRequest
    ): Response<UpdateBookingSeatResponse>

    @DELETE("/booking/{booking_id}")
    suspend fun deleteBookingSeat(
        @Header("Authorization") authorization: String,
        @Path("booking_id") bookingId: String
    ): Response<Unit>

    @GET("coworkings/{coworking_id}/tables")
    suspend fun getTables(
        @Path("coworking_id") coworkingId: String
    ): Response<List<Figure>>


    @POST("/users/add_passport")
    suspend fun savePassportData(
        @Header("Authorization") authorization: String,
        @Body passportData: PassportData
    ): Response<Unit>

    @Multipart
    @POST("/coworkings/create")
    suspend fun createCoworking(
        @Header("Authorization") authorization: String,
        @Part("title") title: RequestBody,
        @Part("address") address: RequestBody,
        @Part("tz_offset") tzOffset: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<String>

    @POST("/booking/invite")
    suspend fun acceptInvite(
        @Header("Authorization") authorization: String,
        @Body request: AcceptRequest
    ): Response<Unit>
}