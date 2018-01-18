package com.github.ovictorpinto.verdinho.firebase

import com.github.ovictorpinto.verdinho.util.LogHelper
import java.text.SimpleDateFormat
import java.util.*

/**
 * {
    tipo : 1, (1 mensagem, 2 sei lá)
    titulo: "Confira!",
    conteudo: "bla bla bla",
    validade: "yyyy-MM-dd HH:mm:ss",
    redireciona: 1, (0 nada, 1 site, 2 aplicativo)
    endereco: "url ou package do aplicativo"
    }
 * Created by victorpinto on 15/01/18.
 */
class MensagemTO(var tipo: Int? = null,
                 var titulo: String? = null,
                 var conteudo: String? = null,
                 var validade: Date? = null,
                 var redireciona: Int? = null,
                 var endereco: String? = null) {


    companion object {
        const val MENSAGEM = 1
        const val NAO_REDIRECIONA = 0
        const val REDIRECIONA_SITE = 1
        const val REDIRECIONA_APLICATIVO = 2
    }

    constructor(bundle: Map<String, String>) : this() {
        conteudo = bundle["conteudo"]
        titulo = bundle["titulo"]
        endereco = bundle["endereco"]

        try {
            tipo = Integer.parseInt(bundle["tipo"])

        } catch (e: Exception) {
            LogHelper.log(e)
        }

        try {
            redireciona = Integer.parseInt(bundle["redireciona"])
        } catch (e: Exception) {
            LogHelper.log(e)
        }
        try {
            validade = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(bundle["validade"])
        } catch (e: Exception) {
            LogHelper.log(e)
        }
    }
}