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
 */

package tr.havelsan.ueransim.app.core.nodes;

import tr.havelsan.ueransim.app.api.ue.UserEquipment;
import tr.havelsan.ueransim.app.api.ue.mr.MrTask;
import tr.havelsan.ueransim.app.api.ue.timers.TimersTask;
import tr.havelsan.ueransim.app.core.UeSimContext;
import tr.havelsan.ueransim.app.core.threads.NodeLooperThread;

public class UeNode {

    public static final int TASK_TIMERS = 1;
    public static final int TASK_MR = 2;

    public static void run(UeSimContext ctx) {
        var itms = ctx.itms;

        var timersTask = new TimersTask(itms, TASK_TIMERS, ctx);
        var mrTask = new MrTask(itms, TASK_MR, ctx);

        itms.createTask(timersTask);
        itms.createTask(mrTask);

        itms.startTask(timersTask);
        itms.startTask(mrTask);

        var looperThread = new NodeLooperThread<>(ctx, UserEquipment::cycle);
        ctx.setLooperThread(looperThread);
        looperThread.start();
    }
}
