/*
 * Copyright (C) 2017 Good Sign
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.bleachhack.util.doom.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Resource IO to automate read/write on configuration/resources
 *
 * @author Good Sign
 */
public class ResourceIO {

    private final Path file;
    private final Charset charset = Charset.forName("US-ASCII");

    public ResourceIO(final File file) {
        this.file = file.toPath();
    }

    public ResourceIO(final Path file) {
        this.file = file;
    }

    public ResourceIO(final String path) {
        this.file = FileSystems.getDefault().getPath(path);
    }

    public boolean exists() {
        return Files.exists(file);
    }

    public boolean readLines(final Consumer<String> lineConsumer) {
        if (Files.exists(file)) {
            try (BufferedReader reader = Files.newBufferedReader(file, charset)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lineConsumer.accept(line);
                }
                
                return true;
            } catch (IOException x) {
                System.err.format("IOException: %s%n", x);
                return false;
            }
        }

        return false;
    }

    public boolean writeLines(final Supplier<String> lineSupplier, final OpenOption... options) {
        try (BufferedWriter writer = Files.newBufferedWriter(file, charset, options)) {
            String line;
            while ((line = lineSupplier.get()) != null) {
                writer.write(line, 0, line.length());
                writer.newLine();
            }
            
            return true;
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
            return false;
        }
    }
    
    public String getFileame() {
        return file.toString();
    }
}
