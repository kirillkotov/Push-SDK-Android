# Push-SDK-Android

Подключение SDK к проекту

        dependencies {
            implementation 'com.github.kirillkotov:Push-SDK-Android:1.0.0.45'
        }


############################################

Initialization:

        val hPlatformPushAdapterSdk =
            PushSDK(
                context = this,
                log_level = "debug",
                push_style = 1,
                basePushURL = "https://example.com/push/{version}/"
            )

Call example:

        val sdkAnswer: PushKFunAnswerGeneral = hPlatformPushAdapterSdk.push_get_message_history(7200)
        println(sdkAnswer)

############################################

if you need specific path for procedure you can override it by platform_branch parameter

Пример:

            val branchSomeValue: UrlsPlatformList = UrlsPlatformList(
                fun_pushsdk_url_device_update = "device/update/test",
                fun_pushsdk_url_registration = "device/test/registration",
                fun_pushsdk_url_revoke = "device/revoke",
                fun_pushsdk_url_get_device_all = "device/all",
                fun_pushsdk_url_message_callback = "message/callback",
                fun_pushsdk_url_message_dr = "message/dr",
                fun_pushsdk_url_mess_queue = "message/queue",
                pushsdk_url_message_history = "message/history?startDate="
            )

            val hPlatformPushAdapterSdk =
                PushSDK(
                    context = this,
                    platform_branch = branchSomeValue,
                    log_level = "debug",
                    push_style = 1,
                    basePushURL = "https://example.com/push/{version}/"
                )

############################################

Для использования процедур SDK импортируем класс (все процедуры доступны из этого единого класса):

        import com.push.android.pushsdkandroid.PushSDK

Для использования, необходимо создать экземпляр этого класса, проинициализировать его и передать ему входные параметры (один обязательный и 2 необязательных).

        PushSDK(
            context: Context,
            platform_branch: UrlsPlatformList = PushSdkParametersPublic.branchMasterValue, // необязательный параметр. Передаются данные по URL методов на которые необходимо провиженить данные.
            log_level: String = "error",  // необязательный параметр. Уровень логирования событий SDK. (debug или error)
            push_style = 1,  // стиль отображения Push сообщения
            basePushURL = "https://example.com/push/{version}/" // URL сервера сообщений
        )

Пример использования:

        val pushsdk: PushSDK = PushSDK (context = this, basePushURL = "https://example.com/push/{version}/")
        pushsdk.push_register_new("test", "1", "79193257892", "password")
        pushsdk.rewrite_msisdn("375291234567")
        pushsdk.push_clear_all_device()

############################################

Приём сообщений
Поддерживается приём сообщений в нужную часть кода приложения с помощью Broadcast Receiver.
Пример регистрации и использования:

        val mPlugInReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (PushKPushMess.message != null) {
                        val edtName: EditText = findViewById(R.id.editText2)
                        val mess: String = edtName.text.toString()
                        edtName.setText(mess + "\n" + PushKPushMess.message)
                    }
                }
            }
        override fun onStart() {
              super.onStart()
              val filter = IntentFilter()
              filter.addAction("com.push.android.pushsdkandroid.Push")
              //val receiver: MyReceiver = MyReceiver()
              registerReceiver(mPlugInReceiver, filter)
          }
          override fun onStop() {
              super.onStop()
              unregisterReceiver(mPlugInReceiver)
           }


############################################

Работа приложения в фоне
Для того чтобы приложение висело в фоне и принимало сообщения даже когда пользователь его закроет, необходимо в методе OnCreate главной формы запустить сервис, добавив следующий код:

        var intentService: Intent = Intent()
        if (Build.VERSION.SDK_INT<=25) {
                    intentService = Intent(this, PushKMessaging::class.java)
                    startService(intentService);
                }

Если push сообщение было отправлено пользователю, а приложение в это время находилось в состоянии «killed», реализована возможность проверки очереди недоставленных сообщений. В случае, если обнаружены сообщения, на которые не отправлен отчёт о доставке, эти сообщения принимаются и отправляются отчёты о доставке.
Для реализации данного функционала предлагается выполнение процедуры push_check_queue в методе onResume().


############################################

Формат ответа процедур
Определены специальные типы данных для ответов процедур. Эти типы доступны для импорта из SDK:

        import com.push.android.pushsdkandroid.core.PushKFunAnswerRegister
        import com.push.android.pushsdkandroid.core.PushKFunAnswerGeneral

Тип данных общего ответа для всех процедур:

        public data class PushKFunAnswerGeneral(
           val code: Int,
           val result: String,
           val description: String,
           val body: String
        )

Тип данных для ответа процедуры регистрации:

        public data class PushKFunAnswerRegister(
          val code: Int,
          val result: String,
          val description: String,
          val deviceId: String,
          val token: String,
          val userId: String,
          val userPhone: String,
          val createdAt: String
        )
