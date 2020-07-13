package bleach.hack.gui.widget;

import bleach.hack.utils.FabricReflect;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;

public class TextPassFieldWidget extends TextFieldWidget {

	public TextPassFieldWidget(TextRenderer textRenderer_1, int int_1, int int_2, int int_3, int int_4, String string_1) {
		super(textRenderer_1, int_1, int_2, int_3, int_4, string_1);
	}
	
	public void renderButton(int mouseX, int mouseY, float delta) {
		String realText = getText();
		
		try { FabricReflect.getField(TextFieldWidget.class, "field_2092", "text").set(this, new String(new char[realText.length()]).replace("\0","\u2022"));
		} catch (IllegalArgumentException | IllegalAccessException e) { System.err.println("Error reflecting text"); }
		
		super.renderButton(mouseX, mouseY, delta);
		
		try { FabricReflect.getField(TextFieldWidget.class, "field_2092", "text").set(this, realText);
		} catch (IllegalArgumentException | IllegalAccessException e) { System.err.println("Error reflecting text"); }
	}
}
