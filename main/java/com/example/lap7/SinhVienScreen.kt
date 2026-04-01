package com.example.lap7

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

// 1. Cập nhật Model: Thêm trường linkAnh
data class SinhVien(
    val id: String = "",
    val ten: String = "",
    val mssv: String = "",
    val linkAnh: String = "" // Lưu URL ảnh
)

@Composable
fun SinhVienScreen(onLogout: () -> Unit) {
    val db = Firebase.firestore
    var danhSachSV by remember { mutableStateOf(listOf<SinhVien>()) }

    // Các biến lưu trữ dữ liệu nhập vào
    var ten by remember { mutableStateOf("") }
    var mssv by remember { mutableStateOf("") }
    var linkAnh by remember { mutableStateOf("") } // Biến lưu link ảnh
    var idDangSua by remember { mutableStateOf("") }

    var sinhVienXemChiTiet by remember { mutableStateOf<SinhVien?>(null) }

    // ĐỌC dữ liệu Real-time
    LaunchedEffect(Unit) {
        db.collection("SinhVien").addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            if (snapshot != null) {
                val list = snapshot.documents.map { doc ->
                    SinhVien(
                        id = doc.id,
                        ten = doc.getString("ten") ?: "",
                        mssv = doc.getString("mssv") ?: "",
                        linkAnh = doc.getString("linkAnh") ?: "" // Đọc link ảnh từ Firebase
                    )
                }
                danhSachSV = list
            }
        }
    }

    // GIAO DIỆN CHÍNH
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("QUẢN LÝ SINH VIÊN", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            Button(onClick = onLogout) { Text("Đăng xuất") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Khu vực nhập liệu
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (idDangSua.isEmpty()) "Thêm Sinh viên Mới" else "Chỉnh sửa Thông tin",
                    style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextFieldWithIcon(value = ten, onValueChange = { ten = it }, label = "Tên đầy đủ", icon = Icons.Default.AccountCircle)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextFieldWithIcon(value = mssv, onValueChange = { mssv = it }, label = "Mã số Sinh viên", icon = Icons.Default.Badge)
                Spacer(modifier = Modifier.height(8.dp))
                // Thêm ô nhập Link Ảnh
                OutlinedTextFieldWithIcon(value = linkAnh, onValueChange = { linkAnh = it }, label = "Link ảnh (URL)", icon = Icons.Default.Link)

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (ten.isNotBlank() && mssv.isNotBlank()) {
                            // Lưu thêm link ảnh lên Firebase
                            val data = hashMapOf("ten" to ten, "mssv" to mssv, "linkAnh" to linkAnh)
                            if (idDangSua.isEmpty()) {
                                db.collection("SinhVien").add(data)
                            } else {
                                db.collection("SinhVien").document(idDangSua).set(data)
                                idDangSua = ""
                            }
                            ten = ""
                            mssv = ""
                            linkAnh = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Icon(imageVector = if (idDangSua.isEmpty()) Icons.Default.Add else Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp).padding(end = 8.dp))
                    Text(if (idDangSua.isEmpty()) "THÊM MỚI" else "CẬP NHẬT")
                }
            }
        }

        Text("Danh sách Sinh viên (${danhSachSV.size})", style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp))

        // Danh sách hiển thị
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(danhSachSV) { sv ->
                SinhVienCardStyled(
                    sv = sv,
                    onClickCard = { sinhVienXemChiTiet = sv },
                    onEdit = {
                        ten = sv.ten
                        mssv = sv.mssv
                        linkAnh = sv.linkAnh // Load link ảnh để sửa
                        idDangSua = sv.id
                    },
                    onDelete = { db.collection("SinhVien").document(sv.id).delete() }
                )
            }
        }
    }

    // POPUP CHI TIẾT
    sinhVienXemChiTiet?.let { sv ->
        AlertDialog(
            onDismissRequest = { sinhVienXemChiTiet = null },
            icon = { Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Hồ sơ Chi tiết") },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    // TÍCH HỢP COIL VÀO POPUP TẠI ĐÂY
                    val imageUrl = sv.linkAnh.ifEmpty { "https://i.pravatar.cc/150?u=${sv.mssv}" }
                    Image(
                        painter = rememberAsyncImagePainter(imageUrl),
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(100.dp).clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Họ và Tên:", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Text(sv.ten.uppercase(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    Text("Mã số SV: ${sv.mssv}", style = MaterialTheme.typography.bodyLarge)
                }
            },
            confirmButton = { Button(onClick = { sinhVienXemChiTiet = null }) { Text("Đóng") } }
        )
    }
}

@Composable
fun OutlinedTextFieldWithIcon(value: String, onValueChange: (String) -> Unit, label: String, icon: ImageVector) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) },
        leadingIcon = { Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true
    )
}

@Composable
fun SinhVienCardStyled(sv: SinhVien, onClickCard: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { onClickCard() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {

            // TÍCH HỢP COIL VÀO ITEM DANH SÁCH TẠI ĐÂY
            // Nếu người dùng không nhập link, lấy đại ảnh mặc định theo MSSV cho đẹp
            val imageUrl = sv.linkAnh.ifEmpty { "https://i.pravatar.cc/150?u=${sv.mssv}" }

            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = "Avatar của ${sv.ten}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(60.dp).clip(CircleShape) // Cắt ảnh thành hình tròn
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = sv.ten.uppercase(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text(text = "MSSV: ${sv.mssv}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }

            Column(horizontalAlignment = Alignment.End) {
                TextButton(onClick = onEdit) { Text("Sửa") }
                TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Xóa") }
            }
        }
    }
}