package io.github.dirtyredz.herbalwitch.client.grimoire.guide.content;

import guideme.document.block.LytSlot;
import guideme.compiler.PageCompiler;
import guideme.document.interaction.GuideTooltip;
import guideme.indices.ItemIndex;
import guideme.ui.GuideUiHost;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraftforge.client.event.ScreenEvent;
import org.joml.Vector2i;

import java.util.Optional;

/** The pinned document screen shifts wide tooltips left without clamping. Use vanilla's renderer. */
public final class GuideRecipeSlot extends LytSlot {
    private static ItemStack hovered;
    private final PageCompiler compiler;

    public GuideRecipeSlot(PageCompiler compiler, ItemStack stack) { super(stack); this.compiler = compiler; }
    public GuideRecipeSlot(PageCompiler compiler, Ingredient ingredient) { super(ingredient); this.compiler = compiler; }

    @Override
    public boolean mouseClicked(GuideUiHost host, int x, int y, int button) {
        if (button != 0) return false;
        var stack = super.getTooltip(x, y).map(GuideTooltip::getIcon).orElse(ItemStack.EMPTY);
        if (stack.isEmpty()) return false;
        var route = compiler.getIndex(ItemIndex.class).get(BuiltInRegistries.ITEM.getKey(stack.getItem()));
        if (route == null) return false;
        host.navigateTo(route);
        return true;
    }

    @Override
    public Optional<GuideTooltip> getTooltip(float x, float y) {
        super.getTooltip(x, y).ifPresent(tooltip -> hovered = tooltip.getIcon());
        return Optional.empty();
    }

    public static void beforeScreen(ScreenEvent.Render.Pre event) { hovered = null; }

    public static void afterScreen(ScreenEvent.Render.Post event) {
        ItemStack stack = hovered;
        hovered = null;
        if (stack == null || stack.isEmpty() || !(event.getScreen() instanceof GuideUiHost)) return;
        var minecraft = Minecraft.getInstance();
        var font = minecraft.font;
        int width = minecraft.getWindow().getGuiScaledWidth();
        var lines = Screen.getTooltipFromItem(minecraft, stack).stream()
                .flatMap(line -> font.split(line, Math.max(1, width - 16)).stream()).toList();
        event.getGuiGraphics().renderTooltip(font, lines, GuideRecipeSlot::position,
                event.getMouseX(), event.getMouseY());
    }

    static Vector2i position(int width, int height, int mouseX, int mouseY, int tooltipWidth, int tooltipHeight) {
        int x = mouseX + 12;
        if (x + tooltipWidth > width - 4) x = mouseX - 16 - tooltipWidth;
        return new Vector2i(Math.max(4, Math.min(x, width - tooltipWidth - 4)),
                Math.max(4, Math.min(mouseY - 12, height - tooltipHeight - 4)));
    }
}
