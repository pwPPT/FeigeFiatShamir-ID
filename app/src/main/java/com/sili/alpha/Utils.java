package com.sili.alpha;

import java.nio.ByteBuffer;

public class Utils {

    public static byte[] longsToBytes(long[] l) {
        ByteBuffer buf = ByteBuffer.allocate(l.length * Long.BYTES);

        for(long val : l) {
            buf = buf.putLong(val);
        }

        return buf.array();
    }

    public static long[] bytesToLongs(byte[] b) {
        ByteBuffer buf = ByteBuffer.wrap(b);
        long[] l = new long[b.length/Long.BYTES];

        for(int i = 0; i < l.length; i++) {
            l[i] = buf.getLong();
        }

        return l;
    }

//    public static CreateConnection(String URL) {
//
//    }
}
