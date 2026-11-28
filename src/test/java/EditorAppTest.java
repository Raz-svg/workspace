import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class EditorAppTest {

    @Test
    void editorAppInitializesWithDefaultDimensions() {
        EditorApp app = new EditorApp();
        assertNotNull(app);
        assertEquals(1280, app.getWidth());
        assertEquals(720, app.getHeight());
    }
}
