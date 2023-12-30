package com.wecareapp.android

object Constants {
    const val APP_NAME = "model_app"
    const val APP_PREF = "model_preffe"

    const val BASE_URL = "https://meerapp.co/"

    const val LANGUAGE = "LANGUAGE"
    const val USER_DATA = "USER_DATA"
    const val CUSTOMER_ID = "CUSTOMER_ID"
    const val CUSTOMER_TYPE = "CUSTOMER_TYPE"
    const val DEVICE_ID = "DEVICE_ID"
    const val TOKEN_ID = "TOKEN_ID"
    const val FIREBASE_TOKEN = "FIREBASE_TOKEN"
    const val MODEL_SIGNUP_SELECTION =
        "MODEL_SIGNUP_SELECTION" //1 -> Model 2 -> Voiceover 3 -> photographer 4-> camera -> rent


    const val MODEL = "Model"
    const val CUSTOMER = "Customer"
    const val VOICEOVER = "Voiceover Artist"
    const val PHOTGRAPHER = "Photographer"
    const val RENT = "Spot Rent"
    const val CAMERA = "Accessories"


    const val ACTOR_ID = "ACTOR_ID"
    const val ACTOR_TYPE = "ACTOR_TYPE"
    const val FULL_NAME = "FULL_NAME"
    const val LANGUAGES = "LANGUAGES"
    const val INTERESTS = "INTERESTS"
    const val GENDER = "GENDER"
    const val LEVEL = "LEVEL"
    const val IMAGE = "IMAGE"
    const val BIO = "BIO"
    const val HOURLY_RATE = "HOURLY_RATE"
    const val LATITUDE = "LATITUDE"
    const val LONGITUDE = "LONGITUDE"
    const val IMAGES = "images"
    const val VIDEOS = "videos"
    const val AUDIOS = "audios"
    const val CONTENT_TYPE = "contentType"
    const val MEDIA_URL = "mediaUrl"
    const val MEDIA_ID = "mediaId"
    const val IS_LOGIN = "isLogin"
    const val LOGIN_TYPE = "LoginType"
    const val TOTAL_WALLET = "TOTAL_WALLET"

    const val MESSAGE_COUNT = "MESSAGE_COUNT"
    const val NOTIFICATION_COUNT = "NOTIFICATION_COUNT"
    const val MESSAGE_TYPE = "MESSAGE_TYPE"


    const val SPLASH_TIME_OUT: Long = 3800

}
class Login {
    companion object {
        var logMessageOnOrOff = true
    }
}