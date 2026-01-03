package com.example.myapplication.data.remote


import com.example.myapplication.data.model.ItemResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @GET("/items")
    suspend fun getListItems(): List<ItemResponse>

    @Multipart
    @POST("/upload-and-get-image")
    suspend fun uploadData(
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part file: MultipartBody.Part // Đổi tên ở đây cho dễ đọc, quan trọng nhất vẫn là lúc tạo FormData
    ): ResponseBody
}

//Call<T> là đối tượng đại diện cho 1 request HTTP mà Retrofit tạo ra.
//T = kiểu dữ liệu server trả về
//Ví dụ:
//Server trả về Note → Call<Note>
//Server trả về List<Note> → Call<List<Note>>
//Nói ngắn gọn:
//Call = 1 cuộc gọi mạng chưa chạy
//Nó dùng để:
//Gửi request lên server
//Nhận response từ server
//Xử lý:
//thành công
//lỗi
//timeout
//cancel request
//👉 Quan trọng:
//Retrofit không gửi request ngay, mà:
//Nó tạo ra Call object
//Bạn phải ra lệnh cho Call chạy
//Call chạy như thế nào?
//Cách 1: Chạy bất đồng bộ (PHỔ BIẾN)
//api.getNotes().enqueue(object : Callback<List<Note>> {
//enqueue() → chạy BACK THREAD ✅
//Retrofit tự tạo background thread
//Network chạy KHÔNG BAO GIỜ trên UI thread
//An toàn tuyệt đối cho Android
//Nhưng lưu ý cực quan trọng:

//Callback chạy ở đâu?
//Nơi	        Thread
//Gửi request	Background thread
//onResponse()	UI thread
//onFailure()	UI thread
//
//➡️ Vì vậy bạn update UI trực tiếp được trong callback.
//Nếu gọi trên UI thread → 💥 crash
//NetworkOnMainThreadException
//Chỉ dùng khi:
//Đã ở background thread
//Worker / Executor / Thread / Coroutine
//UI Thread
//↓
//call.enqueue()
//↓
//Retrofit chuyển sang
//↓
//Background Thread (OkHttp)
//↓
//Server
//↓
//Quay về
//↓
//UI Thread → onResponse / onFailure

//Vì sao callback lại quay về UI thread?
//Vì Retrofit dùng MainThreadExecutor (Android)
//👉 để:
//Không phải runOnUiThread
//Dev dễ update UI
//Tránh lỗi crash UI
//
//6. Còn Coroutine thì sao?
//@GET("notes")
//suspend fun getNotes(): List<Note>
//
//Network chạy trên Dispatcher.IO
//Code tiếp theo chạy theo coroutine scope
//Không quay về Main tự động
//👉 Bạn phải chủ động:
//withContext(Dispatchers.Main) {
//    // update UI
//}

//Cách gọi	Network thread	Callback / code sau
//enqueue()	Background	UI thread
//execute()	Thread hiện tại	Thread hiện tại
//suspend	IO	Tùy scope

//=> Network luôn chạy background, UI chỉ nhận kết quả

//Chạy đồng bộ (KHÔNG dùng trên Android UI)
//val response = api.getNotes().execute()
//execute() → chạy trên thread bạn đang gọi ⚠️

//Tại sao Retrofit không trả dữ liệu trực tiếp mà lại trả Call?
//Vì:
//Mạng chậm
//Không biết khi nào server trả dữ liệu
//Android cấm chặn main thread
//👉 Retrofit cần:
//Cho phép chạy nền
//Cho phép hủy request
//Cho phép retry
//Cho phép bắt lỗi
//➡️ Call sinh ra để quản lý toàn bộ vòng đời của request
//Vòng đời của Call
//Call được tạo
//   ↓
//enqueue() / execute()
//   ↓
//Server xử lý
//   ↓
//onResponse() hoặc onFailure()

//So sánh với cách mới (Coroutine)
//Hiện nay hay dùng thế này hơn 👇
//@GET("notes")
//suspend fun getNotes(): List<Note>
//➡️ Không cần Call
//➡️ Retrofit tự xử lý bất đồng bộ
//Nhưng bên trong nó vẫn dùng Call, chỉ là bọc lại cho gọn
//Tóm tắt 1 câu cho nhớ
//Call là đại diện cho 1 request HTTP, cho phép Retrofit quản lý việc gửi, nhận, hủy và xử lý kết quả từ server