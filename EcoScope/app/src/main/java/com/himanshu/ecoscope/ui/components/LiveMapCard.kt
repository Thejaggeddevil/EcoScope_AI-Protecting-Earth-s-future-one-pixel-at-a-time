//package com.himanshu.ecoscope.ui.components
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.unit.dp
//
//@Composable
//fun LiveMapCard(onConnect: () -> Unit) {
//    Card(
//        shape = RoundedCornerShape(12.dp),
//        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(100.dp)
//            .clickable { onConnect() }
//    ) {
//        Box(
//            contentAlignment = Alignment.Center,
//            modifier = Modifier.fillMaxSize()
//        ) {
//            Text("📡 Connect to Live Map", color = Color(0xFF00FFAA), style = MaterialTheme.typography.bodyLarge)
//        }
//    }
//}
