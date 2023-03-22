package wb.frontend

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.*
import javafx.scene.text.Font
import java.util.*


class RectangleSerializer : JsonSerializer<Rectangle>() {
    override fun serialize(value: Rectangle?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        gen?.writeStartObject()
        gen?.writeNumberField("x", value?.x ?: 0.0)
        gen?.writeNumberField("y", value?.y ?: 0.0)
        gen?.writeNumberField("layoutX", value?.layoutX ?: 0.0)
        gen?.writeNumberField("layoutY", value?.layoutY ?: 0.0)
        gen?.writeNumberField("translateX", value?.translateX ?: 0.0)
        gen?.writeNumberField("translateY", value?.translateY ?: 0.0)
        gen?.writeNumberField("width", value?.width ?: 0.0)
        gen?.writeNumberField("height", value?.height ?: 0.0)
        gen?.writeFieldName("fill")
        gen?.writeStartObject()
        val color: Color? = value?.fill as? Color
        if (color != null) {
            gen?.writeBooleanField("opaque", color.isOpaque)
            gen?.writeNumberField("hue", color.hue)
            gen?.writeNumberField("saturation", color.saturation)
            gen?.writeNumberField("brightness", color.brightness)
        }
        gen?.writeEndObject()
        gen?.writeEndObject()
    }
}

class RectangleDeserializer : JsonDeserializer<Rectangle>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): Rectangle? {
        p?.let {
            val codec = it.codec
            val node: JsonNode = codec.readTree(p)

            val x = node.get("x").asDouble()
            val y = node.get("y").asDouble()
            val layoutX = node.get("layoutX").asDouble()
            val layoutY = node.get("layoutY").asDouble()
            val translateX = node.get("translateX").asDouble()
            val translateY = node.get("translateY").asDouble()
            val width = node.get("width").asDouble()
            val height = node.get("height").asDouble()

            val fillNode = node.get("fill")
            val opaque = fillNode.get("opaque").asBoolean()
            val hue = fillNode.get("hue").asDouble()
            val saturation = fillNode.get("saturation").asDouble()
            val brightness = fillNode.get("brightness").asDouble()

            val fill = Color.hsb(hue, saturation, brightness, if (opaque) 1.0 else 0.0)
            var rectangle = Rectangle(x, y, width, height).apply {
                this.fill = fill
                this.layoutX = layoutX
                this.layoutY = layoutY
                this.translateX = translateX
                this.translateY = translateY
            }
            makeDraggable(rectangle)
            return rectangle
        }

        return null
    }
}


class CircleSerializer : JsonSerializer<Circle>() {
    override fun serialize(value: Circle?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        gen?.writeStartObject()
        gen?.writeNumberField("centerX", value?.centerX ?: 0.0)
        gen?.writeNumberField("centerY", value?.centerY ?: 0.0)
        gen?.writeNumberField("layoutX", value?.layoutX ?: 0.0)
        gen?.writeNumberField("layoutY", value?.layoutY ?: 0.0)
        gen?.writeNumberField("translateX", value?.translateX ?: 0.0)
        gen?.writeNumberField("translateY", value?.translateY ?: 0.0)
        gen?.writeNumberField("radius", value?.radius ?: 0.0)
        gen?.writeFieldName("fill")
        gen?.writeStartObject()
        val color: Color? = value?.fill as? Color
        if (color != null) {
            gen?.writeBooleanField("opaque", color.isOpaque)
            gen?.writeNumberField("hue", color.hue)
            gen?.writeNumberField("saturation", color.saturation)
            gen?.writeNumberField("brightness", color.brightness)
        }
        gen?.writeEndObject()
        gen?.writeEndObject()
    }
}

class CircleDeserializer : JsonDeserializer<Circle>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): Circle? {
        p?.let {
            val codec = it.codec
            val node: JsonNode = codec.readTree(p)

            val centerX = node.get("centerX").asDouble()
            val centerY = node.get("centerY").asDouble()
            val layoutX = node.get("layoutX").asDouble()
            val layoutY = node.get("layoutY").asDouble()
            val translateX = node.get("translateX").asDouble()
            val translateY = node.get("translateY").asDouble()
            val radius = node.get("radius").asDouble()

            val fillNode = node.get("fill")
            val opaque = fillNode.get("opaque").asBoolean()
            val hue = fillNode.get("hue").asDouble()
            val saturation = fillNode.get("saturation").asDouble()
            val brightness = fillNode.get("brightness").asDouble()

            val fill = Color.hsb(hue, saturation, brightness, if (opaque) 1.0 else 0.0)
            var shape = Circle(centerX, centerY, radius).apply {
                this.fill = fill
                this.layoutX = layoutX
                this.layoutY = layoutY
                this.translateX = translateX
                this.translateY = translateY
            }
            makeDraggable(shape)
            return shape
        }

        return null
    }
}

class PathSerializer : JsonSerializer<Path>() {
    override fun serialize(
        value: Path?,
        gen: JsonGenerator?,
        serializers: SerializerProvider?
    ) {
        gen?.writeStartObject()
        val color = value?.stroke.toString()
        val webColor = color.replace("0x", "#")
        gen?.writeStringField("color", webColor)
        gen?.writeNumberField("width", value?.strokeWidth ?: 0.0)
        gen?.writeFieldName("elements")
        gen?.writeStartArray()

        value?.elements?.forEach { element ->
            gen?.writeStartObject()
            gen?.writeFieldName("type")
            gen?.writeString(element::class.java.simpleName)
            when (element) {
                is MoveTo -> {
                    gen?.writeNumberField("x", element.x)
                    gen?.writeNumberField("y", element.y)
                }
                is LineTo -> {
                    gen?.writeNumberField("x", element.x)
                    gen?.writeNumberField("y", element.y)
                }
            }
            gen?.writeEndObject()
        }
        gen?.writeEndArray()
        gen?.writeEndObject()
    }
}


class PathDeserializer : JsonDeserializer<Path>() {
    override fun deserialize(parser: JsonParser?, ctxt: DeserializationContext?): Path {
        val path = Path()
        val root = parser?.codec?.readTree<JsonNode>(parser)
        val webColor = root?.get("color").toString()
        val color = Color.web(webColor.substring(1, webColor.length - 1))
        val width = root?.get("width").toString().toDouble()
        path.strokeWidth = width
        path.stroke = color
        val elements = root?.get("elements")
        elements?.forEach { elementNode ->
            when (val type = elementNode.get("type").asText()) {
                "MoveTo" -> {
                    val x = elementNode.get("x").asDouble()
                    val y = elementNode.get("y").asDouble()
                    path.elements.add(MoveTo(x, y))
                }

                "LineTo" -> {
                    val x = elementNode.get("x").asDouble()
                    val y = elementNode.get("y").asDouble()
                    path.elements.add(LineTo(x, y))
                }
            }
        }
        println(path)
        return path
    }
}

class TextSerializer() : JsonSerializer<VBox>() {
    override fun serialize(value: VBox, gen: JsonGenerator?, serializers: SerializerProvider?) {
        gen?.writeStartObject()
        gen?.writeNumberField("layoutX", value.layoutX)
        gen?.writeNumberField("layoutY", value.layoutY)
        for (control in value.children) {
            if (control is HBox) {
                for (child in control.children) {
                    if (child is ColorPicker) {
                        val color = child.value.toString()
                        val webColor = color.replace("0x", "#")
                        gen?.writeStringField("color", webColor)
                    } else if (child is ComboBox<*>) {
                        if (child.items.size == 9) {
                            gen?.writeNumberField("size", child.value.toString().toDouble())
                        } else {
                            gen?.writeStringField("font", child.value.toString())
                        }
                    }
                }
            }
            else if (control is TextArea) {
                gen?.writeStringField("text", control.text)
            }
        }
        gen?.writeEndObject()
    }
}



class TextDeserializer() : JsonDeserializer<VBox>() {
    private var color = Color.BLACK
    private var font = "System"
    private var size = 12.0
    private fun colorToHex(color: Color): String? {
        val hex2: String
        val hex1: String = Integer.toHexString(color.hashCode()).uppercase(Locale.getDefault())
        hex2 = when (hex1.length) {
            2 -> "000000"
            3 -> String.format("00000%s", hex1.substring(0, 1))
            4 -> String.format("0000%s", hex1.substring(0, 2))
            5 -> String.format("000%s", hex1.substring(0, 3))
            6 -> String.format("00%s", hex1.substring(0, 4))
            7 -> String.format("0%s", hex1.substring(0, 5))
            else -> hex1.substring(0, 6)
        }
        return hex2
    }

    private fun setStyle(textBox: TextArea) {
        textBox.style = "-fx-font-family: ${font}; -fx-text-fill: #${colorToHex(color)}; -fx-font-size: ${size}px;"
    }

    override fun deserialize(parser: JsonParser?, ctxt: DeserializationContext?): VBox? {
        val root = parser?.codec?.readTree<JsonNode>(parser)
        val webColor = root?.get("color").toString()
        color = Color.web(webColor.substring(1, webColor.length - 1))
        font = root?.get("font").toString()
        font = font.substring(1, font.length - 1)
        size = root?.get("size").toString().toDouble()
        val layoutX = root?.get("layoutX").toString().toDouble()
        val layoutY = root?.get("layoutY").toString().toDouble()
        var text =  root?.get("text").toString()
        text = text.substring(1, text.length - 1)

        var textBox = TextArea()
        textBox.isWrapText = true
        textBox.prefRowCount = 5
        textBox.prefColumnCount = 10
        textBox.text = text
        setStyle(textBox)

        // Create font, color, and size controls
        var fontComboBox = ComboBox<String>()
        fontComboBox.items.addAll(Font.getFamilies())
        fontComboBox.selectionModel.select(font)
        var colorPicker = ColorPicker(color)
        var sizeComboBox = ComboBox<Double>()
        sizeComboBox.items.addAll(8.0, 10.0, 12.0, 14.0, 16.0, 18.0, 20.0, 22.0, 24.0)
        sizeComboBox.selectionModel.select(size)

        // Create a horizontal box to hold the controls
        var controlsBox = HBox()
        controlsBox.spacing = 10.0
        controlsBox.children.addAll(fontComboBox, colorPicker, sizeComboBox)

        var deleteButton = Button("x")
        controlsBox.children.add(deleteButton)

        var group = VBox()
        group.alignment = Pos.TOP_CENTER
        var drag = Label("drag")
        group.children.addAll(drag, controlsBox, textBox)

        drag.isVisible = false
        textBox.focusedProperty().addListener { obs, oldVal, newVal ->
            drag.isVisible = newVal
        }

        makeDraggable(drag, group)
        DragResizeMod.makeResizable(group, null);
        group.layoutX = layoutX
        group.layoutY = layoutY

        fontComboBox.setOnAction {
            font = fontComboBox.value
            setStyle(textBox)
        }
        colorPicker.setOnAction {
            color = colorPicker.value
            setStyle(textBox)
        }
        sizeComboBox.setOnAction {
            size = sizeComboBox.value
            setStyle(textBox)
        }

        // can't access canvas from here, can't remove textbox
//        deleteButton.setOnAction { canvas.children.remove(group) }
        return group
    }
}

