package com.waleed.gallerytosnap

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    
    private lateinit var selectButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        selectButton = findViewById(R.id.select_button)
        
        selectButton.setOnClickListener {
            if (checkPermissions()) {
                openGallery()
            } else {
                requestPermissions()
            }
        }
    }
    
    private fun checkPermissions(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    private fun requestPermissions() {
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        
        ActivityCompat.requestPermissions(this, arrayOf(permission), 100)
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 101)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            val imageUri = data.data
            imageUri?.let {
                shareToSnapchat(it)
            }
        }
    }
    
    private fun shareToSnapchat(uri: Uri) {
        try {
            // Check if Snapchat is installed
            packageManager.getPackageInfo("com.snapchat.android", 0)
            
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                setPackage("com.snapchat.android")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            
            startActivity(intent)
            Toast.makeText(this, "Opening Snapchat...", Toast.LENGTH_SHORT).show()
            
        } catch (e: PackageManager.NameNotFoundException) {
            Toast.makeText(this, "Snapchat not installed", Toast.LENGTH_LONG).show()
            // Open Play Store to install Snapchat
            val playStoreIntent = Intent(Intent.ACTION_VIEW)
            playStoreIntent.data = Uri.parse("market://details?id=com.snapchat.android")
            startActivity(playStoreIntent)
        }
    }
}