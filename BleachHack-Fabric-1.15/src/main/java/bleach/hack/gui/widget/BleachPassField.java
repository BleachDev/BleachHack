package bleach.hack.gui.widget;

import com.google.common.base.Predicates;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.sun.istack.internal.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class BleachPassField extends AbstractButtonWidget implements Drawable, Element {
    private final TextRenderer textRenderer;
    private String text;
    private int maxLength;
    private int focusedTicks;
    private boolean focused;
    private boolean focusUnlocked;
    private boolean editable;
    private boolean selecting;
    private int firstCharacterIndex;
    private int selectionStart;
    private int selectionEnd;
    private int editableColor;
    private int uneditableColor;
    private String suggestion;
    private Consumer<String> changedListener;
    private Predicate<String> textPredicate;
    private BiFunction<String, Integer, String> renderTextProvider;

    public BleachPassField(TextRenderer textRenderer_1, int int_1, int int_2, int int_3, int int_4, String string_1) {
        this(textRenderer_1, int_1, int_2, int_3, int_4, (BleachPassField) null, string_1);
    }

    public BleachPassField(TextRenderer textRenderer_1, int int_1, int int_2, int int_3, int int_4, @Nullable BleachPassField textFieldWidget_1, String string_1) {
        super(int_1, int_2, int_3, int_4, string_1);
        this.text = "";
        this.maxLength = 32;
        this.focused = true;
        this.focusUnlocked = true;
        this.editable = true;
        this.editableColor = 14737632;
        this.uneditableColor = 7368816;
        this.textPredicate = Predicates.alwaysTrue();
        this.renderTextProvider = (string_1x, integer_1) -> {
            return string_1x;
        };
        this.textRenderer = textRenderer_1;
        if (textFieldWidget_1 != null) {
            this.setText(textFieldWidget_1.getText());
        }

    }

    public void setChangedListener(Consumer<String> consumer_1) {
        this.changedListener = consumer_1;
    }

    public void setRenderTextProvider(BiFunction<String, Integer, String> biFunction_1) {
        this.renderTextProvider = biFunction_1;
    }

    public void tick() {
        ++this.focusedTicks;
    }

    protected String getNarrationMessage() {
        String string_1 = this.getMessage();
        return string_1.isEmpty() ? "" : I18n.translate("gui.narrate.editBox", string_1, this.text);
    }

    public String getText() {
        return this.text;
    }

    public void setText(String string_1) {
        if (this.textPredicate.test(string_1)) {
            if (string_1.length() > this.maxLength) {
                this.text = string_1.substring(0, this.maxLength);
            } else {
                this.text = string_1;
            }

            this.setCursorToEnd();
            this.setSelectionEnd(this.selectionStart);
            this.onChanged(string_1);
        }
    }

    public String getSelectedText() {
        int int_1 = this.selectionStart < this.selectionEnd ? this.selectionStart : this.selectionEnd;
        int int_2 = this.selectionStart < this.selectionEnd ? this.selectionEnd : this.selectionStart;
        return this.text.substring(int_1, int_2);
    }

    public void setTextPredicate(Predicate<String> predicate_1) {
        this.textPredicate = predicate_1;
    }

    public void write(String string_1) {
        String string_2 = "";
        String string_3 = SharedConstants.stripInvalidChars(string_1);
        int int_1 = this.selectionStart < this.selectionEnd ? this.selectionStart : this.selectionEnd;
        int int_2 = this.selectionStart < this.selectionEnd ? this.selectionEnd : this.selectionStart;
        int int_3 = this.maxLength - this.text.length() - (int_1 - int_2);
        if (!this.text.isEmpty()) {
            string_2 = string_2 + this.text.substring(0, int_1);
        }

        int int_5;
        if (int_3 < string_3.length()) {
            string_2 = string_2 + string_3.substring(0, int_3);
            int_5 = int_3;
        } else {
            string_2 = string_2 + string_3;
            int_5 = string_3.length();
        }

        if (!this.text.isEmpty() && int_2 < this.text.length()) {
            string_2 = string_2 + this.text.substring(int_2);
        }

        if (this.textPredicate.test(string_2)) {
            this.text = string_2;
            this.setSelectionStart(int_1 + int_5);
            this.setSelectionEnd(this.selectionStart);
            this.onChanged(this.text);
        }
    }

    private void onChanged(String string_1) {
        if (this.changedListener != null) {
            this.changedListener.accept(string_1);
        }

        this.nextNarration = Util.getMeasuringTimeMs() + 500L;
    }

    private void erase(int int_1) {
        if (Screen.hasControlDown()) {
            this.eraseWords(int_1);
        } else {
            this.eraseCharacters(int_1);
        }

    }

    public void eraseWords(int int_1) {
        if (!this.text.isEmpty()) {
            if (this.selectionEnd != this.selectionStart) {
                this.write("");
            } else {
                this.eraseCharacters(this.getWordSkipPosition(int_1) - this.selectionStart);
            }
        }
    }

    public void eraseCharacters(int int_1) {
        if (!this.text.isEmpty()) {
            if (this.selectionEnd != this.selectionStart) {
                this.write("");
            } else {
                boolean boolean_1 = int_1 < 0;
                int int_2 = boolean_1 ? this.selectionStart + int_1 : this.selectionStart;
                int int_3 = boolean_1 ? this.selectionStart : this.selectionStart + int_1;
                String string_1 = "";
                if (int_2 >= 0) {
                    string_1 = this.text.substring(0, int_2);
                }

                if (int_3 < this.text.length()) {
                    string_1 = string_1 + this.text.substring(int_3);
                }

                if (this.textPredicate.test(string_1)) {
                    this.text = string_1;
                    if (boolean_1) {
                        this.moveCursor(int_1);
                    }

                    this.onChanged(this.text);
                }
            }
        }
    }

    public int getWordSkipPosition(int int_1) {
        return this.getWordSkipPosition(int_1, this.getCursor());
    }

    private int getWordSkipPosition(int int_1, int int_2) {
        return this.getWordSkipPosition(int_1, int_2, true);
    }

    private int getWordSkipPosition(int int_1, int int_2, boolean boolean_1) {
        int int_3 = int_2;
        boolean boolean_2 = int_1 < 0;
        int int_4 = Math.abs(int_1);

        for (int int_5 = 0; int_5 < int_4; ++int_5) {
            if (!boolean_2) {
                int int_6 = this.text.length();
                int_3 = this.text.indexOf(32, int_3);
                if (int_3 == -1) {
                    int_3 = int_6;
                } else {
                    while (boolean_1 && int_3 < int_6 && this.text.charAt(int_3) == ' ') {
                        ++int_3;
                    }
                }
            } else {
                while (boolean_1 && int_3 > 0 && this.text.charAt(int_3 - 1) == ' ') {
                    --int_3;
                }

                while (int_3 > 0 && this.text.charAt(int_3 - 1) != ' ') {
                    --int_3;
                }
            }
        }

        return int_3;
    }

    public void moveCursor(int int_1) {
        this.setCursor(this.selectionStart + int_1);
    }

    public void setSelectionStart(int int_1) {
        this.selectionStart = MathHelper.clamp(int_1, 0, this.text.length());
    }

    public void setCursorToStart() {
        this.setCursor(0);
    }

    public void setCursorToEnd() {
        this.setCursor(this.text.length());
    }

    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (!this.isActive()) {
            return false;
        } else {
            this.selecting = Screen.hasShiftDown();
            if (Screen.isSelectAll(int_1)) {
                this.setCursorToEnd();
                this.setSelectionEnd(0);
                return true;
            } else if (Screen.isCopy(int_1)) {
                MinecraftClient.getInstance().keyboard.setClipboard(""); // Copy not allowed from password field
                return true;
            } else if (Screen.isPaste(int_1)) {
                if (this.editable) {
                    this.write(MinecraftClient.getInstance().keyboard.getClipboard());
                }

                return true;
            } else if (Screen.isCut(int_1)) {
                MinecraftClient.getInstance().keyboard.setClipboard(""); // Cut not allowed from password field
                if (this.editable) {
                    this.write("");
                }

                return true;
            } else {
                switch (int_1) {
                    case 259:
                        if (this.editable) {
                            this.selecting = false;
                            this.erase(-1);
                            this.selecting = Screen.hasShiftDown();
                        }

                        return true;
                    case 260:
                    case 264:
                    case 265:
                    case 266:
                    case 267:
                    default:
                        return false;
                    case 261:
                        if (this.editable) {
                            this.selecting = false;
                            this.erase(1);
                            this.selecting = Screen.hasShiftDown();
                        }

                        return true;
                    case 262:
                        if (Screen.hasControlDown()) {
                            this.setCursor(this.getWordSkipPosition(1));
                        } else {
                            this.moveCursor(1);
                        }

                        return true;
                    case 263:
                        if (Screen.hasControlDown()) {
                            this.setCursor(this.getWordSkipPosition(-1));
                        } else {
                            this.moveCursor(-1);
                        }

                        return true;
                    case 268:
                        this.setCursorToStart();
                        return true;
                    case 269:
                        this.setCursorToEnd();
                        return true;
                }
            }
        }
    }

    public boolean isActive() {
        return this.isVisible() && this.isFocused() && this.isEditable();
    }

    public boolean charTyped(char char_1, int int_1) {
        if (!this.isActive()) {
            return false;
        } else if (SharedConstants.isValidChar(char_1)) {
            if (this.editable) {
                this.write(Character.toString(char_1));
            }

            return true;
        } else {
            return false;
        }
    }

    public boolean mouseClicked(double double_1, double double_2, int int_1) {
        if (!this.isVisible()) {
            return false;
        } else {
            boolean boolean_1 = double_1 >= (double) this.x && double_1 < (double) (this.x + this.width) && double_2 >= (double) this.y && double_2 < (double) (this.y + this.height);
            if (this.focusUnlocked) {
                this.setSelected(boolean_1);
            }

            if (this.isFocused() && boolean_1 && int_1 == 0) {
                int int_2 = MathHelper.floor(double_1) - this.x;
                if (this.focused) {
                    int_2 -= 4;
                }

                String string_1 = this.textRenderer.trimToWidth(this.text.substring(this.firstCharacterIndex), this.getInnerWidth());
                this.setCursor(this.textRenderer.trimToWidth(string_1, int_2).length() + this.firstCharacterIndex);
                return true;
            } else {
                return false;
            }
        }
    }

    public void setSelected(boolean boolean_1) {
        super.setFocused(boolean_1);
    }

    public void renderButton(int int_1, int int_2, float float_1) {
        if (this.isVisible()) {
            if (this.hasBorder()) {
                fill(this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, -6250336);
                fill(this.x, this.y, this.x + this.width, this.y + this.height, -16777216);
            }

            int int_3 = this.editable ? this.editableColor : this.uneditableColor;
            int int_4 = this.selectionStart - this.firstCharacterIndex;
            int int_5 = this.selectionEnd - this.firstCharacterIndex;
            String string_1 = this.textRenderer.trimToWidth(this.text.substring(this.firstCharacterIndex).replaceAll(".", "\u2022"), this.getInnerWidth());
            boolean boolean_1 = int_4 >= 0 && int_4 <= string_1.length();
            boolean boolean_2 = this.isFocused() && this.focusedTicks / 6 % 2 == 0 && boolean_1;
            int int_6 = this.focused ? this.x + 4 : this.x;
            int int_7 = this.focused ? this.y + (this.height - 8) / 2 : this.y;
            int int_8 = int_6;
            if (int_5 > string_1.length()) {
                int_5 = string_1.length();
            }

            if (!string_1.isEmpty()) {
                String string_2 = boolean_1 ? string_1.substring(0, int_4) : string_1;
                int_8 = this.textRenderer.drawWithShadow((String) this.renderTextProvider.apply(string_2, this.firstCharacterIndex), (float) int_6, (float) int_7, int_3);
            }

            boolean boolean_3 = this.selectionStart < this.text.length() || this.text.length() >= this.getMaxLength();
            int int_9 = int_8;
            if (!boolean_1) {
                int_9 = int_4 > 0 ? int_6 + this.width : int_6;
            } else if (boolean_3) {
                int_9 = int_8 - 1;
                --int_8;
            }

            if (!string_1.isEmpty() && boolean_1 && int_4 < string_1.length()) {
                this.textRenderer.drawWithShadow((String) this.renderTextProvider.apply(string_1.substring(int_4), this.selectionStart), (float) int_8, (float) int_7, int_3);
            }

            if (!boolean_3 && this.suggestion != null) {
                this.textRenderer.drawWithShadow(this.suggestion, (float) (int_9 - 1), (float) int_7, -8355712);
            }

            int var10002;
            int var10003;
            if (boolean_2) {
                if (boolean_3) {
                    int var10001 = int_7 - 1;
                    var10002 = int_9 + 1;
                    var10003 = int_7 + 1;
                    this.textRenderer.getClass();
                    DrawableHelper.fill(int_9, var10001, var10002, var10003 + 9, -3092272);
                } else {
                    this.textRenderer.drawWithShadow("_", (float) int_9, (float) int_7, int_3);
                }
            }

            if (int_5 != int_4) {
                int int_10 = int_6 + this.textRenderer.getStringWidth(string_1.substring(0, int_5));
                var10002 = int_7 - 1;
                var10003 = int_10 - 1;
                int var10004 = int_7 + 1;
                this.textRenderer.getClass();
                this.drawSelectionHighlight(int_9, var10002, var10003, var10004 + 9);
            }

        }
    }

    private void drawSelectionHighlight(int int_1, int int_2, int int_3, int int_4) {
        int int_6;
        if (int_1 < int_3) {
            int_6 = int_1;
            int_1 = int_3;
            int_3 = int_6;
        }

        if (int_2 < int_4) {
            int_6 = int_2;
            int_2 = int_4;
            int_4 = int_6;
        }

        if (int_3 > this.x + this.width) {
            int_3 = this.x + this.width;
        }

        if (int_1 > this.x + this.width) {
            int_1 = this.x + this.width;
        }

        Tessellator tessellator_1 = Tessellator.getInstance();
        BufferBuilder bufferBuilder_1 = tessellator_1.getBuffer();
        RenderSystem.color4f(0.0F, 0.0F, 255.0F, 255.0F);
        RenderSystem.disableTexture();
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
        bufferBuilder_1.begin(7, VertexFormats.POSITION);
        bufferBuilder_1.vertex((double) int_1, (double) int_4, 0.0D).next();
        bufferBuilder_1.vertex((double) int_3, (double) int_4, 0.0D).next();
        bufferBuilder_1.vertex((double) int_3, (double) int_2, 0.0D).next();
        bufferBuilder_1.vertex((double) int_1, (double) int_2, 0.0D).next();
        tessellator_1.draw();
        RenderSystem.disableColorLogicOp();
        RenderSystem.enableTexture();
    }

    private int getMaxLength() {
        return this.maxLength;
    }

    public void setMaxLength(int int_1) {
        this.maxLength = int_1;
        if (this.text.length() > int_1) {
            this.text = this.text.substring(0, int_1);
            this.onChanged(this.text);
        }

    }

    public int getCursor() {
        return this.selectionStart;
    }

    public void setCursor(int int_1) {
        this.setSelectionStart(int_1);
        if (!this.selecting) {
            this.setSelectionEnd(this.selectionStart);
        }

        this.onChanged(this.text);
    }

    private boolean hasBorder() {
        return this.focused;
    }

    public void setHasBorder(boolean boolean_1) {
        this.focused = boolean_1;
    }

    public void setEditableColor(int int_1) {
        this.editableColor = int_1;
    }

    public void setUneditableColor(int int_1) {
        this.uneditableColor = int_1;
    }

    public boolean changeFocus(boolean boolean_1) {
        return this.visible && this.editable ? super.changeFocus(boolean_1) : false;
    }

    public boolean isMouseOver(double double_1, double double_2) {
        return this.visible && double_1 >= (double) this.x && double_1 < (double) (this.x + this.width) && double_2 >= (double) this.y && double_2 < (double) (this.y + this.height);
    }

    protected void onFocusedChanged(boolean boolean_1) {
        if (boolean_1) {
            this.focusedTicks = 0;
        }

    }

    private boolean isEditable() {
        return this.editable;
    }

    public void setEditable(boolean boolean_1) {
        this.editable = boolean_1;
    }

    public int getInnerWidth() {
        return this.hasBorder() ? this.width - 8 : this.width;
    }

    public void setSelectionEnd(int int_1) {
        int int_2 = this.text.length();
        this.selectionEnd = MathHelper.clamp(int_1, 0, int_2);
        if (this.textRenderer != null) {
            if (this.firstCharacterIndex > int_2) {
                this.firstCharacterIndex = int_2;
            }

            int int_3 = this.getInnerWidth();
            String string_1 = this.textRenderer.trimToWidth(this.text.substring(this.firstCharacterIndex), int_3);
            int int_4 = string_1.length() + this.firstCharacterIndex;
            if (this.selectionEnd == this.firstCharacterIndex) {
                this.firstCharacterIndex -= this.textRenderer.trimToWidth(this.text, int_3, true).length();
            }

            if (this.selectionEnd > int_4) {
                this.firstCharacterIndex += this.selectionEnd - int_4;
            } else if (this.selectionEnd <= this.firstCharacterIndex) {
                this.firstCharacterIndex -= this.firstCharacterIndex - this.selectionEnd;
            }

            this.firstCharacterIndex = MathHelper.clamp(this.firstCharacterIndex, 0, int_2);
        }

    }

    public void setFocusUnlocked(boolean boolean_1) {
        this.focusUnlocked = boolean_1;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean boolean_1) {
        this.visible = boolean_1;
    }

    public void setSuggestion(@Nullable String string_1) {
        this.suggestion = string_1;
    }

    public int getCharacterX(int int_1) {
        return int_1 > this.text.length() ? this.x : this.x + this.textRenderer.getStringWidth(this.text.substring(0, int_1));
    }

    public void setX(int int_1) {
        this.x = int_1;
    }
}
