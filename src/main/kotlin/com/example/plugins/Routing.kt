package com.example.plugins

import com.example.smarthome.Device
import com.example.smarthome.DeviceListener
import com.example.smarthome.expose.BinaryExpose
import com.example.smarthome.expose.EnumExpose
import com.example.smarthome.expose.ExposeObject
import com.example.smarthome.expose.NumericExpose
import com.example.smarthome.util.Util
import io.ktor.routing.*
import io.ktor.http.content.*
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.request.*
import io.ktor.response.*
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

    fun DIV.addButtonsFromList(
        device: Device,
        expose: ExposeObject,
        presets: List<String>
    ) {
        if (presets.isNotEmpty()) {
            postForm("/devices/${device.getName() ?: device.getFriendlyName()}/set/key") {
                div {
                    p {
                        presets.forEach {
                            postButton(name = expose.name, classes = "btn btn-success") {
                                value = it
                                text(it.uppercase())
                            }
                        }
                    }
                }
            }
        }
    }

    fun DIV.createStuffForNumericExpose(device: Device, expose: NumericExpose) {
        postForm("/devices/${device.getName() ?: device.getFriendlyName()}/set/key") {
            div(classes = "slidecontainer") {
                style = "width:100%"
                p { text(expose.description) }
                p { +"Current Value: ${expose.currentValue}" }
                input(type = InputType.range, classes = "slider", name = expose.name)
                {
                    min = "${expose.value_min+1}"
                    value = expose.currentValue
                    max = "${expose.value_max-1}"
                }
                submitInput()
            }
        }
    }

    fun DIV.createStuffForEnumExpose(device: Device, expose: EnumExpose)
    {
        addButtonsFromList(device, expose, expose.values)
    }

    fun DIV.addRenameOption(device: Device) {
        postForm(
            classes = "form-floating",
            action = "/devices/${device.getName() ?: device.getFriendlyName()}/set/name"
        )
        {
            div(classes = "input-group, row mb-3") {
                span(classes = "input-group-text, col-sm-10, text-warning bg-dark") { text("Rename Device") }
                textInput(name = "name")
                submitInput()
            }
        }
    }

    fun DIV.displayDevices() {
        val devices = Util.knownDevices
        devices.forEach { device ->
            val settableExposes = device.getSettableExposes()
            if (settableExposes.isNotEmpty()) {
                h1 {
                    style = """
                        font-style: italic;
                        font-weight: bolder;
                        color: #5d8ba8;
                    """.trimIndent()
                    +(device.getName() ?: device.getFriendlyName())
                }
                h3(classes = "text-uppercase, fw-lighter") { +device.getDescription() }
                div()
                {
                    settableExposes.forEach { exposeObject ->
                        h2() {
                            style = """
                               font-style: italic;
                               font-weight lighter;
                               color: #bd7126;
                            """
                            text(exposeObject.name.uppercase())
                        }
                        when (exposeObject) {
                            is BinaryExpose -> {
                                createButtonsForBinaryExpose(device)
                            }
                            is NumericExpose -> {
                                createStuffForNumericExpose(device, exposeObject)
                                if (exposeObject.presets.isNotEmpty()) {
                                    h4 {
                                        style = """
                                            font-weight: lither;
                                            font-style: fantasy;
                                            color: #bd7126;
                                        """.trimIndent()
                                        text("Preset values:")
                                    }
                                    addButtonsFromList(device, exposeObject, exposeObject.presets)
                                }
                            }
                        }
                        br()
                    }
                }
                device.enumExposes.forEach {
                    createStuffForEnumExpose(device, it)
                }
                addRenameOption(device)
                br()
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
                    style = """
                        background-color: #43574f;
                    """.trimIndent()
                    div("container-fluid") {
                        div("row, p-3 mb-2") {
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
                val params = call.receiveParameters()
                val device = Util.knownDevices.firstOrNull { it.getFriendlyName() == devicename || it.getName() == devicename }
                device?.getSettableExposes()?.forEach {
                    val key = it.name
                    val value = params[key]
                    value?.let {it2 ->
                        println("GOT EXPOSE WITH NAME: $key and VALUE IS $value")
                        when
                        {
                            (it is NumericExpose) -> it.currentValue = value
                        }
                        device.handleSet(it.name, value)
                        call.respondRedirect("/")
                    }
                }
            }
        }
        // Static plugin. Try to access `/static/index.html`
        static("/static") {
            resources("static")
        }
    }
}
