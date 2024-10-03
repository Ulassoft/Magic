// Copyright (c) 2024 Magic Tech Ltd

package fit.magic.cv.repcounter


import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import fit.magic.cv.PoseLandmarkerHelper
import com.google.mediapipe.formats.proto.LandmarkProto
import android.util.Log

class ExerciseRepCounterImpl : ExerciseRepCounter() {

    private var mytime: Long = 0

    private var oldPose=""

    var ru: Double = 0.0
    var rd: Double = 0.0
    var lu: Double = 0.0
    var ld: Double = 0.0

    //fun calculateAngle(a: MutableList<com.google.mediapipe.tasks.components.containers.NormalizedLandmark>, b: PoseLandmarker, c: PoseLandmarker): Double {
    fun calculateAngle(a: NormalizedLandmark, b: NormalizedLandmark, c: NormalizedLandmark): Double {
        // Radyan cinsinden açı hesaplama
        val radians = Math.atan2((c.y() - b.y()).toDouble(), (c.x() - b.x()).toDouble()) - Math.atan2((a.y() - b.y()).toDouble(),
            (a.x() - b.x()).toDouble()
        )

        // Radyanı dereceye çevirme
        var angle = Math.abs(radians * 180.0 / Math.PI)

        // Eğer açı 180'den büyükse, 360'dan çıkararak ayarla
        if (angle > 180.0) {
            angle = 360 - angle
        }

        return angle
    }

    fun increaseRepCount(){

        incrementRepCount()

        val repcount=getRepCount()

        if (repcount<5) {
            sendProgressUpdate((repcount/5.0f).toFloat())
        }
        else{
            sendProgressUpdate(1.0f)
        }

    }

    fun calculatePoses(resultBundle: PoseLandmarkerHelper.ResultBundle) {
        val myresult = resultBundle.results
        val poseLandmarkerResult = myresult[0]
        //val landmarks = poseLandmarkerResult.landmarks()
        //val rightHipLandmark = landmarks[23]
        //val x = rightHipLandmark.x()



        val landmarks: MutableList<MutableList<NormalizedLandmark>>? = poseLandmarkerResult.landmarks()
        val firstPoseLandmarks = landmarks?.get(0)
        if (firstPoseLandmarks != null) {

            val Node12=firstPoseLandmarks.get(12)
            val Node24=firstPoseLandmarks.get(24)
            val Node26=firstPoseLandmarks.get(26)
            val Node28=firstPoseLandmarks.get(28)
            val Node11=firstPoseLandmarks.get(11)
            val Node23=firstPoseLandmarks.get(23)
            val Node25=firstPoseLandmarks.get(25)
            val Node27=firstPoseLandmarks.get(27)

            ru= calculateAngle(Node12,Node24,Node26)
            rd=calculateAngle(Node24,Node26,Node28)
            lu=calculateAngle(Node11,Node23,Node25)
            ld=calculateAngle(Node23,Node25,Node27)
        }













        var myPose=""
        if  (ru<110  && rd <110 && lu >135  && ld<110){
            myPose="rlunges"
        }

        else if (lu<110  && ld<110 && ru >135  && rd<110){
            myPose="llunges"
        }

        else if(ru>135 &&  rd>135 && lu>135 && ld>135){
            myPose="zero"
        }

        else {
            myPose = "other"
        }



        if (myPose=="zero"){
            if(oldPose!="zero")
            {
                oldPose="zero"
                resetRepCount()
                sendProgressUpdate(0f)
            }
        }
        else if(myPose=="rlunges")
        {
            if(oldPose=="" ||oldPose=="llunges" || oldPose=="zero"){
                oldPose="rlunges"
                increaseRepCount()
            }
        }
        else if(myPose=="llunges")
        {
            if(oldPose=="" ||oldPose=="rlunges" || oldPose=="zero"){
                oldPose="llunges"
                increaseRepCount()
            }
        }
    }

    override fun setResults(resultBundle: PoseLandmarkerHelper.ResultBundle) {

        val now = System.currentTimeMillis()

        if (mytime == 0L || (now - mytime) > 500) {
            // mytime'ı güncelle
            mytime = now

            if ((resultBundle != null) ) {

                try {
                    Log.d("ProgressLog", "Pose run:")
                    calculatePoses(resultBundle)
                } catch (e: Exception) {
                    // Tüm exception'ları yakalar ve loglar
                    Log.e("ExceptionLog", "Error occurred: ${e.message}", e)
                }





            }







        }

        // process pose data in resultBundle
        //
        // use functions in base class incrementRepCount(), sendProgressUpdate(),
        // and sendFeedbackMessage() to update the UI
    }
}
