/*
 * Copyright (c) 2018-2020 DJI
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
 */

package com.tourcoo.aircraft.ui.sample;

import android.app.Activity;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.View;

/**

 *控制与USB附件的连接。此活动侦听USB连接的操作

 *发送带有内部代码的广播，由

 *{@link OnDJIUSBAttachedReceiver}。

 */
public class DJIConnectionControlActivity extends Activity {

    public static final String ACCESSORY_ATTACHED = "com.tourcoo.aircraft.beta.sample.ACCESSORY_ATTACHED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new View(this));

        Intent usbIntent = getIntent();
        if (usbIntent != null) {
            String action = usbIntent.getAction();
            if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(action)) {
                Intent attachedIntent = new Intent();
                attachedIntent.setAction(ACCESSORY_ATTACHED);
                sendBroadcast(attachedIntent);
            }
        }

        finish();
    }
}
