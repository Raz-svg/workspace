import io.github.humbleui.skija.*;
import io.github.humbleui.skija.paragraph.*;
import io.github.humbleui.types.Rect;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class EditorApp {
    private long window;
    private int width = 1280;
    private int height = 720;

    private DirectContext skiaContext;
    private BackendRenderTarget renderTarget;
    private Surface surface;
    private Canvas canvas;
    private Paragraph paragraph;
    private FontCollection fontCollection;
    private ParagraphStyle paragraphStyle;
    private TextStyle textStyle;
    private Paint cursorPaint;
    private GapBuffer gapBuffer = new GapBuffer();

    private float margin_x = 0f;
    private float margin_y = 0f;
    private float fontSize = 24f;

    private boolean dirty;

    private void rebuildParagraph() {
        if (paragraph != null) {
            paragraph.close();
        }

        try (ParagraphBuilder builder = new ParagraphBuilder(paragraphStyle, fontCollection)) {
            builder.pushStyle(textStyle);
            builder.addText(gapBuffer.getText());
            paragraph = builder.build();
        }

        paragraph.layout(Math.max(1f, width - margin_x * 2)); // Adjust for margins
        dirty = false;
    }

    private void drawCursor() {
        float cursor_x = margin_x;
        float top = margin_y;
        float bottom = margin_y + Math.max(paragraph.getHeight(), fontSize);

        int cursor = gapBuffer.getCursorPosition();

        if (gapBuffer.length() > 0) {
            if (cursor > 0) {
                TextBox[] boxes = paragraph.getRectsForRange(
                    cursor - 1,
                    cursor,
                    RectHeightMode.TIGHT,
                    RectWidthMode.TIGHT
                );
                if (boxes.length > 0) {
                    Rect box = boxes[0].getRect();
                    if (gapBuffer.charAt(cursor - 1) == '\n') {
                        float lineHeight = Math.max(box.getBottom() - box.getTop(), fontSize);
                        cursor_x = margin_x;
                        top = margin_y + box.getBottom();
                        bottom = top + lineHeight;
                    } else {
                        cursor_x = margin_x + box.getRight();
                        top = margin_y + box.getTop();
                        bottom = margin_y + box.getBottom();
                    }
                }
            } else {
                TextBox[] boxes = paragraph.getRectsForRange(0, 1, RectHeightMode.TIGHT, RectWidthMode.TIGHT);
                if (boxes.length > 0) {
                    Rect box = boxes[0].getRect();
                    cursor_x = margin_x + box.getLeft();
                    top = margin_y + box.getTop();
                    bottom = margin_y + box.getBottom();
                }
            }
        }

        canvas.drawLine(cursor_x, top, cursor_x, bottom, cursorPaint);
    }

    public void run() {
        initWindow();
        initSkia();
        loop();
        cleanup();
    }

    private void initWindow() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(width, height, "Collaborative Editor", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create GLFW window");
        }

        glfwSetFramebufferSizeCallback(window, this::onFramebufferResize);

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        glfwSetCharCallback(window, this::onCharInput);
        glfwSetKeyCallback(window, this::onKeyInput);
        glfwSetMouseButtonCallback(window, this::onMouseButton);

        GL.createCapabilities();
    }

    private void onFramebufferResize(long win, int newW, int newH) {
        if (newW > 0 && newH > 0) {
            width = newW;
            height = newH;
            createSkiaSurface();
            dirty = true;
        }
    }

    private void onCharInput(long win, int codepoint) {
        gapBuffer.insert((char) codepoint);
        dirty = true;
    }

    private void onKeyInput(long win, int key, int scancode, int action, int mods) {
        if (action != GLFW_PRESS && action != GLFW_REPEAT) {
            return;
        }

        switch (key) {
            case GLFW_KEY_BACKSPACE:
                gapBuffer.delete();
                dirty = true;
                break;
            case GLFW_KEY_DELETE:
                gapBuffer.deleteForward();
                dirty = true;
                break;
            case GLFW_KEY_ENTER:
                gapBuffer.insert('\n');
                dirty = true;
                break;
            case GLFW_KEY_LEFT:
                gapBuffer.left();
                dirty = true;
                break;
            case GLFW_KEY_RIGHT:
                gapBuffer.right();
                dirty = true;
                break;
            default:
                break;
        }
    }

    private void onMouseButton(long win, int button, int action, int mods) {
        if (button != GLFW_MOUSE_BUTTON_LEFT || action != GLFW_PRESS || paragraph == null) {
            return;
        }

        double[] mx = new double[1];
        double[] my = new double[1];
        glfwGetCursorPos(win, mx, my);

        float localX = (float) mx[0] - margin_x;
        float localY = (float) my[0] - margin_y;

        int clickedIndex = paragraph
            .getGlyphPositionAtCoordinate(localX, localY)
            .getPosition();

        int clampedIndex = Math.max(0, Math.min(clickedIndex, gapBuffer.length()));
        gapBuffer.moveCursor(clampedIndex);
    }

    private void initSkia() {
        skiaContext = DirectContext.makeGL();
        createSkiaSurface();

        fontCollection = new FontCollection().setDefaultFontManager(FontMgr.getDefault());
        paragraphStyle = new ParagraphStyle();
        textStyle = new TextStyle().setFontSize(fontSize).setColor(0xFFFFFFFF);
        cursorPaint = new Paint().setColor(0xFFFFFFFF).setStrokeWidth(2f);

        dirty = true;
    }

    private void createSkiaSurface() {
        if (surface != null) surface.close();
        if (renderTarget != null) renderTarget.close();

        int fbId = GL11.glGetInteger(0x8CA6);

        renderTarget = BackendRenderTarget.makeGL(
            width,
            height,
            0,
            8,
            fbId,
            FramebufferFormat.GR_GL_RGBA8
        );

        surface = Surface.wrapBackendRenderTarget(
            skiaContext,
            renderTarget,
            SurfaceOrigin.BOTTOM_LEFT,
            ColorType.RGBA_8888,
            ColorSpace.getSRGB()
        );

        canvas = surface.getCanvas();
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            render();
            glfwSwapBuffers(window);
            glfwWaitEvents();
        }
    }

    private void render() {
        canvas.clear(0xFF181818);
        if (dirty) {
            rebuildParagraph();
        }
        paragraph.paint(canvas, margin_x, margin_y);
        drawCursor();
        skiaContext.flush();
    }

    private void cleanup() {
        if (paragraph != null) paragraph.close();
        if (cursorPaint != null) cursorPaint.close();
        if (textStyle != null) textStyle.close();
        if (paragraphStyle != null) paragraphStyle.close();
        if (fontCollection != null) fontCollection.close();
        if (surface != null) surface.close();
        if (renderTarget != null) renderTarget.close();
        if (skiaContext != null) skiaContext.close();

        glfwDestroyWindow(window);
        glfwTerminate();
    }

    int getWidth() {
        return width;
    }

    int getHeight() {
        return height;
    }

    public static void main(String[] args) {
        new EditorApp().run();
    }
}
