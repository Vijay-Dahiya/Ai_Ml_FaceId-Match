package com.example.facedetectionapp

import android.Manifest.permission.CAMERA
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
//import com.google.cloud.vision.v1.ImageAnnotatorClient;
//import com.google.cloud.vision.v1.Image;
//import com.google.cloud.vision.v1.Feature;
//import com.google.cloud.vision.v1.Feature.Type;
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.common.Feature
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {

    val TAG = "Vijay"
    var selfieFace: Face? = null
    val CLOUD_VISION_API_KEY = ""
    var bitmapSelfie: Bitmap? = null

    private val REQUIRED_PERMISSIONS = arrayOf(
        CAMERA,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dispatchTakePictureIntent()
        if (REQUIRED_PERMISSIONS.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }) {
            dispatchTakePictureIntent()
        } else {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
        }
    }

    val takePicture = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null) {
            processSelfie(bitmap)
        } else {
        }
    }

    fun dispatchTakePictureIntent() {
        takePicture.launch(null)
    }

    val pickDocument = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        // Handle the Uri
        if (uri != null) {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                processIDPhoto(bitmap)
            } catch (e: Exception) {
                // Handle the error
                Toast.makeText(applicationContext, "Error opening image", Toast.LENGTH_LONG).show()
            }
        } else {
            // Handle the null case
            Toast.makeText(applicationContext, "No image selected", Toast.LENGTH_LONG).show()
        }
    }

    fun openDocument() {
        pickDocument.launch(arrayOf("image/*"))
    }

    fun processSelfie(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val detector = FaceDetection.getClient()
        bitmapSelfie = bitmap
        detector.process(image)
            .addOnSuccessListener { faces ->
                if (faces.size > 1) {
                    Toast.makeText(
                        applicationContext,
                        "More than one face detected. Please take a selfie with only your face in the picture.",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (faces.size == 1) {
                    selfieFace = faces[0]  // Store the face
                    openDocument()  // Prompt the user to upload their ID
                } else {
                    Toast.makeText(applicationContext, "No face detected. Please retake the selfie.", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Face detection failed", e)
            }
    }

    fun processIDPhoto(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val detector = FaceDetection.getClient()

        detector.process(image)
            .addOnSuccessListener { faces ->
                if (faces.size > 1) {
                    Toast.makeText(
                        applicationContext,
                        "More than one face detected in the ID. Please upload an ID with only your face.",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (faces.size == 1) {
                    val idFace = faces[0]
                    if (selfieFace != null) {
                        bitmapSelfie?.let { compareFaces(selfieFace!!,idFace, it,bitmap) }
                        // Compare faces
                        // To implement this part, you'll need a face comparison library or API.
                        // The comparison process will depend on the library or API that you choose.
                    } else {
                        // Handle the case where the selfie is not processed yet
                    }
                } else {
                    Toast.makeText(applicationContext, "No face detected in the ID. Please upload an ID with your face.", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Face detection failed", e)
            }
    }

    private fun compareFaces(selfieFace: Face, idFace: Face, originalBitmapSelfie: Bitmap,originalBitmapId: Bitmap) {
        // Convert both faces to encoded images
        val selfieBytes = convertFaceToEncodedImage(selfieFace, originalBitmapSelfie)
        val idBytes = convertFaceToEncodedImage(idFace, originalBitmapId)

        // TODO: Create a function to call Google Cloud Vision API
        val similarityScore = callGoogleCloudVisionAPI(selfieBytes, idBytes, CLOUD_VISION_API_KEY)
        if (similarityScore >= 0.8) { // Consider the faces as matching if the score is 0.8 or higher
            // The faces match
        } else {
            // The faces do not match
        }
    }

    //permission Asking code
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                // All permissions are granted
                dispatchTakePictureIntent()
            } else {
                // Some permissions are denied
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
            }
        }

    //security Addition
    private fun convertFaceToEncodedImage(face: Face, originalBitmap: Bitmap): ByteArray {
        // Get the bounding box of the face
        val boundingBox = face.boundingBox

        // Crop the face from the original bitmap
        val faceBitmap = Bitmap.createBitmap(originalBitmap, boundingBox.left, boundingBox.top, boundingBox.width(), boundingBox.height())

        // Compress the face bitmap into a ByteArray
        val stream = ByteArrayOutputStream()
        faceBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }


    //security Code
    fun convertBitmapToEncodedImage(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }

    //security Code
    fun convertBitmapToEncodedImage2(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val bytes = stream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    fun callGoogleCloudVisionAPI(selfieBytes: ByteArray, idBytes: ByteArray, apiKey: String): Double {
//        // TODO: Implement the API call
//        // This function should call the Google Cloud Vision API with the two encoded images
//        // It should return the similarity score between the two faces, as a double between 0.0 and 1.0
//
//        val client = ImageAnnotatorClient.create()
//
//        // Create an Image object for each of the encoded images.
//        val selfieImage = Image.fromBytes(selfieBytes)
//        val idImage = Image.fromBytes(idBytes)
//
//        // Create a Feature object for each of the features that you want to detect.
//        val selfieFaceFeature = Feature.newBuilder().setType(Type.FACE_DETECTION).build()
//        val idFaceFeature = Feature.newBuilder().setType(Type.FACE_DETECTION).build()
//
//        // Call the AnnotateImageWithFeatures method to request that the Cloud Vision API detect faces in the two images.
//        val request = AnnotateImageRequest.newBuilder()
//            .setImage(selfieImage)
//            .addFeatures(selfieFaceFeature)
//            .setImage(idImage)
//            .addFeatures(idFaceFeature)
//            .build()
//
//        val response = client.annotateImage(request)
//
//        // Get the similarity score between the two faces.
//        val similarityScore = response.getFaceAnnotations(0).getSimilarityScore()
//
//        // Return the similarity score.
//        return similarityScore
        return 0.0
    }

}