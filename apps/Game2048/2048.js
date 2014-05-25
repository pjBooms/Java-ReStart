
var EventHandler = Packages.javafx.event.EventHandler;
var Direction = Packages.game2048.Direction;
var KeyCode = Packages.javafx.scene.input.KeyCode;

var handler = new EventHandler() {
    handle: function(e) {
        var keyCode = e.getCode();
        if (keyCode.equals(KeyCode.W)) {
            gameManager.move(Direction.UP);
        }
        if (keyCode.equals(KeyCode.S)) {
            gameManager.move(Direction.DOWN);
        }
        if (keyCode.equals(KeyCode.A)) {
            gameManager.move(Direction.LEFT);
        }
        if (keyCode.equals(KeyCode.D)) {
            gameManager.move(Direction.RIGHT);
        }
    }
}

var $webfx = {
    title: "2048 Game Demo",
    i18n: null,
    initWebFX : function() {
        $webfx.scene.setOnKeyPressed(handler)
    }
};
