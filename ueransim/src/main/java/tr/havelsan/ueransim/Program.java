/*
 * MIT License
 *
 * Copyright (c) 2020 ALİ GÜNGÖR
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * @author Ali Güngör (aligng1620@gmail.com)
 */

package tr.havelsan.ueransim;

import tr.havelsan.ueransim.mts.ImplicitTypedObject;
import tr.havelsan.ueransim.mts.MtsDecoder;
import tr.havelsan.ueransim.mts.MtsInitializer;
import tr.havelsan.ueransim.utils.Color;
import tr.havelsan.ueransim.utils.Console;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Program {

    public static void main(String[] args) {
        MtsInitializer.initMts();
        initLogging();

        var simContext = AppConfig.createSimContext();
        var gnbContext = AppConfig.createGnbSimContext(simContext, (ImplicitTypedObject) MtsDecoder.decode("gnb.yaml"));
    }

    private static void initLogging() {
        final String logFile = "app.log";

        Console.println(Color.YELLOW_BOLD_BRIGHT, "WARNING: All logs are written to: %s", logFile);
        Console.setStandardPrintEnabled(false);
        Console.addPrintHandler(str -> {
            final Path path = Paths.get(logFile);
            try {
                Files.write(path, str.getBytes(StandardCharsets.UTF_8),
                        Files.exists(path) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
