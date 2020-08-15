package bleach.hack.gui.widget;

import bleach.hack.utils.FabricReflect;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class TextPassFieldWidget extends TextFieldWidget {

    public TextPassFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        super(textRenderer, x, y, width, height, text);
    }

    public void renderButton(MatrixStack matrix, int mouseX, int mouseY, float delta) {
        String realText = getText();

        try {
            FabricReflect.getField(TextFieldWidget.class, "field_2092", "text").set(this, new String(new char[realText.length()]).replace("\0", "\u2022"));
        } catch (IllegalArgumentException | IllegalAccessException e) {
            System.err.println("Error reflecting text");
        }

        super.renderButton(matrix, mouseX, mouseY, delta);

        try {
            FabricReflect.getField(TextFieldWidget.class, "field_2092", "text").set(this, realText);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            System.err.println("Error reflecting text");
        }
    }
}
