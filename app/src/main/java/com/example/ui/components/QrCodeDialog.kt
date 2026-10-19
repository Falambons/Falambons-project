package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.util.FileShareHelper
import com.example.util.QrCodeGenerator

val QR_COLOR_PRESETS = listOf(
    Pair("Cyan Neon", Color(0xFF00E5FF)),
    Pair("Neon Pink", Color(0xFFFF007F)),
    Pair("Lime Green", Color(0xFF00FF66)),
    Pair("Pure White", Color(0xFFFFFFFF)),
    Pair("Cyber Gold", Color(0xFFFFD700))
)

@Composable
fun QrCodeDialog(
    initialText: String,
    title: String = "Custom QR Barcode File",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var qrContent by remember { mutableStateOf(initialText) }
    var selectedColorIndex by remember { mutableIntStateOf(0) }
    var centerLogoText by remember { mutableStateOf("FLB") }
    var showCenterLogo by remember { mutableStateOf(true) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(qrContent, selectedColorIndex, centerLogoText, showCenterLogo) {
        val fgColor = QR_COLOR_PRESETS[selectedColorIndex].second.toArgb()
        qrBitmap = QrCodeGenerator.generateCustomQr(
            content = qrContent,
            foregroundColor = fgColor,
            backgroundColor = android.graphics.Color.rgb(10, 10, 16),
            centerLogoText = centerLogoText,
            showCenterLogo = showCenterLogo
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // QR Code Image Display
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0A0A10))
                        .border(2.dp, QR_COLOR_PRESETS[selectedColorIndex].second, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    qrBitmap?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "QR Code Result",
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Custom Color Selector
                Text(
                    text = "Warna Custom QR:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    QR_COLOR_PRESETS.forEachIndexed { index, pair ->
                        val isSelected = index == selectedColorIndex
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(pair.second)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorIndex = index }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Center Logo Customizer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Logo Tengah Custom:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Switch(
                        checked = showCenterLogo,
                        onCheckedChange = { showCenterLogo = it }
                    )
                }

                if (showCenterLogo) {
                    OutlinedTextField(
                        value = centerLogoText,
                        onValueChange = { centerLogoText = it.take(4) },
                        label = { Text("Teks Logo (Maks 4 Huruf, misal: FLB, CODE)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_qr_logo")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Share QR Button
                Button(
                    onClick = {
                        qrBitmap?.let { bmp ->
                            val file = QrCodeGenerator.saveQrToFile(context, bmp, "falambons_code_qr")
                            FileShareHelper.shareFile(context, file, "image/png")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_share_qr"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bagikan Gambar QR Code", fontWeight = FontWeight.Bold, color = Color.Black)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Credit by : falambons developer",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
