package com.immomo.wink.listener;

import com.immomo.wink.model.DataManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.GroovyFileType;

import java.util.List;

public class FileChangeListener implements BulkFileListener {

    @Override
    public void after(@NotNull List<? extends VFileEvent> events) {
        for (VFileEvent event : events) {
            if(event.getFile().getFileType()== GroovyFileType.GROOVY_FILE_TYPE){
                DataManager.getInstance().setNeedUpdate(true);
                return;
            }
        }
    }
}
