package com.example.myapplication.data.enumm

sealed class FaceDetectionResult {
    object NoFace : FaceDetectionResult()
    object MultipleFaces : FaceDetectionResult()
    object SingleGoodFace : FaceDetectionResult()

    object Error : FaceDetectionResult()
}