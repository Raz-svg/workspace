import io.github.humbleui.skija.*;
import io.github.humbleui.types.RRect;
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

    private Paint rectPaint;

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

        glfwSetFramebufferSizeCallback(window, (win, newW, newH) -> {
            if (newW > 0 && newH > 0) {
                width = newW;
                height = newH;
                createSkiaSurface();
            }
        });

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        GL.createCapabilities();
    }

    private void initSkia() {
        skiaContext = DirectContext.makeGL();
        createSkiaSurface();

        rectPaint = new Paint().setColor(0xFF89B4FA);
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
        canvas.clear(0xFF1E1E2E);
        canvas.drawRRect(RRect.makeXYWH(100, 100, 300, 180, 12), rectPaint);
        skiaContext.flush();
    }

    private void cleanup() {
        rectPaint.close();
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
