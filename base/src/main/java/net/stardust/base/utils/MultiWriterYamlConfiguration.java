package net.stardust.base.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;

import lombok.Getter;

@Getter
public class MultiWriterYamlConfiguration extends YamlConfiguration {

    private File mainFile;
    private List<File> files;

    public MultiWriterYamlConfiguration(File mainFile) {
        this.mainFile = mainFile;

        files = new ArrayList<>();
        files.add(mainFile);

        try {
            load(mainFile);
        } catch(Exception e) {
            Throwables.sendAndThrow(e);
        }
    }

    public void save() {
        for(File file : files) {
            try {
                save(file);
            } catch(IOException e) {
                Throwables.sendAndThrow(e);
            }
        }
    }

}
