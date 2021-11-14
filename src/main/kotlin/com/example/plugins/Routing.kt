package com.example.plugins

import com.example.smarthome.Device
import com.example.smarthome.DeviceListener
import com.example.smarthome.expose.BinaryExpose
import com.example.smarthome.expose.EnumExpose
import com.example.smarthome.expose.NumericExpose
import com.example.smarthome.util.Util
import io.ktor.routing.*
import io.ktor.http.content.*
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.*
import kotlinx.html.*

fun Application.configureRouting() {

    fun DIV.createButtonsForBinaryExpose(device: Device) {
        postForm("/devices/${device.getName() ?: device.getFriendlyName()}/set/state") {
            postButton(name = "state", classes = "btn btn-success") {
                value = "on"
                text("ON")
            }
            postButton(name = "state", classes = "btn btn-danger") {
                value = "off"
                text("OFF")
            }
        }
    }

    fun DIV.displayDevices() {
        val devices = Util.knownDevices
        devices.forEach { device ->
            val exposes = device.getSettableExposes()

            if (exposes.isNotEmpty()) {
                h1 { +(device.getName() ?: device.getFriendlyName()) }
                h3(classes = "text-uppercase, fw-lighter") { +device.getDescription() }
                div()
                {
                    exposes.forEach { exposeObject ->
                        when (exposeObject) {
                            is BinaryExpose -> {
                                createButtonsForBinaryExpose(device)
                            }
                            is NumericExpose -> {
                            }
                            is EnumExpose -> {
                            }
                        }
                    }
                }

                postForm(classes = "form-floating", action = "/devices/${device.getName() ?: device.getFriendlyName()}/set/name")
                {
                    div(classes = "input-group, row mb-3") {
                        span(classes = "input-group-text, col-sm-10, text-warning bg-dark") { text("Rename Device") }
                        textInput(name = "name")
                        submitInput()
                    }
                }
                postForm("/devices/${device.getName() ?: device.getFriendlyName()}/set/key") {
                    div(classes = "input-group, row mb-3") {
                        span(classes = "input-group-text, col-sm-10, text-warning bg-dark") { text("Key and Value") }
                        textInput(name = "key")
                        textInput(name = "value")
                        submitInput()
                    }
                }

            }
        }
    }
    routing {

        get("/") {
            call.respondHtml {
                head {
                    title { +"SMART_HOME_BY_JAKOB" }
                    link(
                        href = "https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css",
                        rel = "stylesheet"
                    )
                }

                body {
                    div("container-fluid") {
                        div("row, p-3 mb-2 bg-secondary text-white") {
                            if (Util.knownDevices.isEmpty()) {
                                h1 { +"There are no devices just yet..." }
                            } else {
                                div("col-md-auto") {
                                    displayDevices()
                                }
                            }
                        }
                        getForm("/devices") {
                            getButton(classes = "btn btn-info") { text("Add new Devices") }
                        }
                    }
                }
            }
        }
        get("/devices")
        {
            val listener = DeviceListener()
            listener.startListeningForNewDevices()
            call.respondRedirect("/")
        }

        get("/devices/{friendlyName}")
        {
            val friendlyName = call.parameters["friendlyName"]
            val device = Util.knownDevices.firstOrNull { it.getFriendlyName() == friendlyName }
            if (device != null) {
                call.respondHtml {
                    head { +"DEVICE: ${device.getDescription()}" }
                }
            } else {
                call.respondRedirect("/")
            }
        }

        route("/devices/{Oldname}/set") {
            post("/name")
            {
                val name = call.receiveParameters()["name"]
                val oldName = call.parameters["Oldname"]
                if (name != null) {
                    println("Name=$name, friendlyName=$oldName")
                    val device =
                        Util.knownDevices.firstOrNull { it.getFriendlyName() == oldName || it.getName() == oldName }
                    if (device != null) {
                        println("Updating device")
                        device.setName(name);
                        println("Updated Device with previous name=$oldName to have new name=${device.getName()}")
                    }
                }
                call.respondRedirect("/")
            }
            post("/*")
            {
                val devicename = call.parameters["Oldname"]
                val setKey = call.parameters["key"]
                val params = call.receiveParameters()
                println("GOT: params=$params and name=$devicename")
                val key: String
                val value: String
                if (setKey == "name") {

                }
                if (params["state"] != null) {
                    key = "state"
                    value = params["state"].toString()
                } else {
                    key = params["key"]!!
                    value = params["value"]!!
                }
                Util.knownDevices.firstOrNull { it.getFriendlyName() == devicename || it.getName() == devicename }
                    ?.handleSet(key, value)
                call.respondRedirect("/")
            }
        }
        // Static plugin. Try to access `/static/index.html`
        static("/static") {
            resources("static")
        }
    }
}
