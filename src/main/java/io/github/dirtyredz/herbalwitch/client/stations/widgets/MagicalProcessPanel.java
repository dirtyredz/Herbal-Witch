package io.github.dirtyredz.herbalwitch.client.stations.widgets;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.stations.menu.StationMenu;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.ProcessStation;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.ProcessStatus;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.authorization.StationOperations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.function.Consumer;

/**
 * The witch's side of a station screen, shown beside it while a witch recipe is matched: its cost per
 * batch, whose batch it is, and Start, Resume and Cancel. Purely a view of the menu's synchronised data;
 * the buttons send vanilla menu-button clicks and the server decides (and says why on the action bar).
 */
public final class MagicalProcessPanel {
    public static final int WIDTH = 100;
    public static final int HEIGHT = 92;
    private static final String KEY = "screen." + HerbalWitch.MOD_ID + ".station.magic.";
    private static final int INK = 0xFFE8DCC8;
    private static final int BACK = 0xE0201420;
    private static final int EDGE = 0xFF6A4A2A;

    private final StationMenu menu;
    private Button start;
    private Button resume;
    private Button cancel;
    private int x;
    private int y;

    public MagicalProcessPanel(StationMenu menu) {
        this.menu = menu;
    }

    /** Places the panel to the right of a screen; returns its buttons for the screen to add. */
    public void init(int left, int top, int imageWidth, Consumer<Button> add) {
        x = left + imageWidth + 4;
        y = top;
        start = button(KEY + "start", 0, StationOperations.Operation.START);
        resume = button(KEY + "resume", 1, StationOperations.Operation.RESUME);
        cancel = button(KEY + "cancel", 2, StationOperations.Operation.CANCEL);
        add.accept(start);
        add.accept(resume);
        add.accept(cancel);
        update();
    }

    private Button button(String key, int row, StationOperations.Operation op) {
        return Button.builder(Component.translatable(key), b -> click(op)).bounds(x + 4, y + 44 + row * 16, WIDTH - 8, 14).build();
    }

    private void click(StationOperations.Operation op) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode != null) {
            mc.gameMode.handleInventoryButtonClick(menu.containerId, op.id());
        }
    }

    public boolean shown() {
        return menu.data(ProcessStation.DATA_MAGICAL) != 0 || menu.data(ProcessStation.DATA_OPERATOR) != 0;
    }

    /** Button visibility and state from the latest data (each container tick). */
    public void update() {
        if (start == null) {
            return;
        }
        boolean shown = shown();
        int operator = menu.data(ProcessStation.DATA_OPERATOR);
        ProcessStatus status = ProcessStatus.byOrdinal(menu.data(ProcessStation.DATA_STATUS));
        start.visible = shown;
        resume.visible = shown;
        cancel.visible = shown;
        start.active = operator == 0 && menu.data(ProcessStation.DATA_MAGICAL) != 0;
        resume.active = operator == 1 && status == ProcessStatus.NEEDS_OPERATOR;
        cancel.active = operator != 0;
    }

    public void render(GuiGraphics graphics, Font font) {
        if (!shown()) {
            return;
        }
        graphics.fill(x - 1, y - 1, x + WIDTH + 1, y + HEIGHT + 1, EDGE);
        graphics.fill(x, y, x + WIDTH, y + HEIGHT, BACK);
        int line = y + 4;
        line = write(graphics, font, Component.translatable(KEY + "title"), line);
        line = write(graphics, font, Component.translatable(KEY + "cost", menu.data(ProcessStation.DATA_ENERGY)), line);
        int operator = menu.data(ProcessStation.DATA_OPERATOR);
        write(graphics, font, Component.translatable(KEY + (operator == 0 ? "unstarted" : operator == 1 ? "yours" : "theirs")), line);
    }

    private int write(GuiGraphics graphics, Font font, Component text, int top) {
        List<FormattedCharSequence> lines = font.split(text, WIDTH - 8);
        for (FormattedCharSequence l : lines) {
            graphics.drawString(font, l, x + 4, top, INK, false);
            top += 10;
        }
        return top;
    }
}
