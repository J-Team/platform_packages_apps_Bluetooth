/*
* Copyright (C) 2013 Samsung System LSI
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.android.bluetooth.map;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.android.internal.util.FastXmlSerializer;
import java.io.UnsupportedEncodingException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import org.xmlpull.v1.XmlSerializer;

import android.util.Log;
import android.util.Xml;

public class BluetoothMapMessageListing {
    private boolean hasUnread = false;
    private static final String TAG = "BluetoothMapMessageListing";
    private List<BluetoothMapMessageListingElement> list;

    public BluetoothMapMessageListing(){
     list = new ArrayList<BluetoothMapMessageListingElement>();
    }
    public void add(BluetoothMapMessageListingElement element) {
        list.add(element);
        Log.d(TAG, "list size is  " +list.size());
        /* update info regarding whether the list contains unread messages */
        if (element.getRead().equalsIgnoreCase("no"))
        {
            hasUnread = true;
        }
    }

    /**
     * Used to fetch the number of BluetoothMapMessageListingElement elements in the list.
     * @return the number of elements in the list.
     */
    public int getCount() {
        if(list != null)
        {
            Log.d(TAG, "returning  " + list.size());
            return list.size();
        }
        Log.e(TAG, "list is null returning 0");
        return 0;
    }

    /**
     * does the list contain any unread messages
     * @return true if unread messages have been added to the list, else false
     */
    public boolean hasUnread()
    {
        return hasUnread;
    }

    /**
     * Encode the list of BluetoothMapMessageListingElement(s) into a UTF-8
     * formatted XML-string in a trimmed byte array
     *
     * @return a reference to the encoded byte array.
     * @throws UnsupportedEncodingException
     *             if UTF-8 encoding is unsupported on the platform.
     */
    public byte[] encode() throws UnsupportedEncodingException {
        Log.d(TAG, "encoding to UTF-8 format");
        XmlSerializer xmlMsgElement = new FastXmlSerializer();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputStreamWriter myOutputStreamWriter = null;
        try {
            myOutputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Failed to encode: charset=" + "UTF-8");
            return null;
        }

       try {
            String str1;
            String str2 = "<?xml version=\"1.0\"?>";
            xmlMsgElement.setOutput(myOutputStreamWriter);
            xmlMsgElement.startDocument("UTF-8", true);
            xmlMsgElement.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            xmlMsgElement.text("\n");
            xmlMsgElement.startTag(null, "MAP-msg-listing");
            xmlMsgElement.attribute(null, "version", "1.0");
            // Do the XML encoding of list
            if(list != null) {
               for (BluetoothMapMessageListingElement element : list) {
                   try {
                        element.encode(xmlMsgElement); // Append the list element
                   } catch (IllegalArgumentException e) {
                        xmlMsgElement.endTag(null, "msg");
                        Log.w(TAG, e.toString());
                   } catch (IllegalStateException e) {
                        Log.w(TAG, e.toString());
                   } catch (IOException e) {
                        Log.w(TAG, e.toString());
                   }
               }
            }
            xmlMsgElement.endTag(null, "MAP-msg-listing");
            xmlMsgElement.endDocument();

            try {
                str1 = outputStream.toString("UTF-8");
                Log.v(TAG, "Printing XML-Converted String: " + str1);
                int line1 = 0;
                line1 = str1.indexOf("\n");
                str2 += str1.substring(line1 + 1);
                if (list.size() > 0) {
                    int indxHandle = str2.indexOf("msg handle");
                    str1 = "<" + str2.substring(indxHandle);
                    str2 = str2.substring(0, (indxHandle - 1)) + str1;
                }

                return str2.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Failed to encode: charset=" + "UTF-8");
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public void sort() {
        Collections.sort(list);
    }

    public void segment(int count, int offset) {
        count = Math.min(count, list.size());
        if (offset + count <= list.size()) {
            list = list.subList(offset, offset + count);
        } else {
            if(offset > list.size()) {
               list = null;
               Log.d(TAG, "offset greater than list size. Returning null");
            } else {
               list = list.subList(offset, list.size());
            }
        }
    }
}
