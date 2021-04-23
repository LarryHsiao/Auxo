package com.larryhsiao.auxo.controller.files;

import com.larryhsiao.auxo.utils.MenuIcon;
import com.larryhsiao.auxo.utils.ShownInBrowser;
import com.larryhsiao.clotho.Source;
import javafx.scene.control.MenuItem;

import java.io.File;
import java.util.ResourceBundle;

/**
 * Source to build a menu item for browsing given file in system manager.
 */
public class BrowseFileMenuItem implements Source<MenuItem> {
    private final ResourceBundle res;
    private final File selected;

    public BrowseFileMenuItem(ResourceBundle res, File selected) {
        this.res = res;
        this.selected = selected;
    }

    @Override
    public MenuItem value() {
        final MenuItem showInBrowser = new MenuItem(res.getString("show_in_browser"));
        showInBrowser.setGraphic(new MenuIcon("/images/browse.png").value());
        showInBrowser.setOnAction(event -> {
            new Thread(() -> new ShownInBrowser(selected).fire()).start();
        });
        return showInBrowser;
    }
}
