package com.example.lap7

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) } // Đổi giữa Đăng nhập & Đăng ký

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isLoginMode) "ĐĂNG NHẬP" else "ĐĂNG KÝ TÀI KHOẢN",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mật khẩu") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            modifier = Modifier.fillMaxWidth().height(50.dp),
            onClick = {
                if (email.isBlank() || password.isBlank()) return@Button

                if (isLoginMode) {
                    // LOGIC ĐĂNG NHẬP
                    auth.signInWithEmailAndPassword(email.trim(), password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                if (user != null && user.isEmailVerified) {
                                    onAuthSuccess()
                                } else {
                                    Toast.makeText(context, "Vui lòng xác thực Email trước!", Toast.LENGTH_LONG).show()
                                    auth.signOut()
                                }
                            } else {
                                Toast.makeText(context, "Lỗi: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    // LOGIC ĐĂNG KÝ
                    // Đổi email thành email.trim()
                    auth.createUserWithEmailAndPassword(email.trim(), password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                auth.currentUser?.sendEmailVerification()
                                Toast.makeText(context, "Đã gửi mail xác thực. Hãy kiểm tra hộp thư!", Toast.LENGTH_LONG).show()
                                isLoginMode = true // Chuyển về đăng nhập
                            } else {
                                Toast.makeText(context, "Lỗi: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        ) {
            Text(if (isLoginMode) "ĐĂNG NHẬP" else "ĐĂNG KÝ")
        }

        TextButton(onClick = { isLoginMode = !isLoginMode }) {
            Text(if (isLoginMode) "Chưa có tài khoản? Đăng ký ngay" else "Đã có tài khoản? Đăng nhập")
        }
    }
}