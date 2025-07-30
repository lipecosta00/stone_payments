package dev.ltag.stone_payments.usecases

import android.content.Context
import android.os.Build
import android.util.Log
import dev.ltag.stone_payments.Result
import stone.application.StoneStart
import stone.application.interfaces.StoneCallbackInterface
import stone.providers.ActiveApplicationProvider
import stone.user.UserModel
import stone.utils.Stone
import stone.utils.keys.StoneKeyType
import java.lang.Exception

class ActivateUsecase(
    private val context: Context,
) {
    lateinit var serial: String

    fun doActivate(appName: String, stoneCode: String, qrCodeProviderId: String?, qrCodeAuthorization: String?, callback: (Result<Boolean>) -> Unit) {

        val stoneKeys = HashMap<StoneKeyType, String>()

        if (qrCodeProviderId != null && qrCodeAuthorization != null) {
            stoneKeys[StoneKeyType.QRCODE_PROVIDERID] = qrCodeProviderId
            stoneKeys[StoneKeyType.QRCODE_AUTHORIZATION] = "Bearer $qrCodeAuthorization"
        }

        Stone.setAppName(appName);
        val userList: List<UserModel>? = StoneStart.init(context, stoneKeys)

        if (userList == null) {
            val activeApplicationProvider = ActiveApplicationProvider(context)
            activeApplicationProvider.dialogMessage = "Ativando o Stone Code"
            activeApplicationProvider.dialogTitle = "Aguarde"
            activeApplicationProvider.connectionCallback = object : StoneCallbackInterface {

                override fun onSuccess() {
                    // SDK ativado com sucesso

                    callback(Result.Success(true))
                    Log.d("SUCESSO", "SUCESSO")
                }

                override fun onError() {
                    // Ocorreu algum erro na ativação

                    Log.d("ERROR", "ERRO")
                    callback(Result.Error(Exception("Erro ao Ativar")))
                }
            }
            activeApplicationProvider.activate(stoneCode)
        } else {
            // O SDK já foi ativado.

            callback(Result.Success(true))
        }
    }

    fun getStoneInfo(): String {
        try {
            if (!StoneStart.SDKInitialized) {
                StoneStart.init(context)
            }
            val stoneCode = Stone.getUserModel(0).stoneCode
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                serial = Build.getSerial()
            } else {
                serial = Build.SERIAL
            }   
            return "$serial:$stoneCode"
        } catch (e: Exception) {
            return "FailN:FailS"
        }
    }

    fun getMerchantDocument() : String {
        if (!StoneStart.SDKInitialized) {
            StoneStart.init(context)
        }
        val document = Stone.getUserModel(0).merchantDocumentNumber
        return document
    }
}
