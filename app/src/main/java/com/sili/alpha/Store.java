package com.sili.alpha;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ThreadLocalRandom;

public class Store {

    final private String PRIVATE_KEY = "privateKey";
    final private int KEY_LENGTH = 8;

    private Context context;
    private long N;

    public Store(Context context, long N) {
        this.context = context;
        this.N = N;
    }

    public void storePrivateKey(long[] pk) {
        try {
            FileOutputStream out = this.context.openFileOutput(PRIVATE_KEY, Context.MODE_PRIVATE);
            byte[] bytesPk = Utils.longsToBytes(pk);
            out.write(bytesPk);
            out.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public long[] loadPrivateKey() {
        try {
            File file = this.context.getFileStreamPath(PRIVATE_KEY);

            byte[] bytesPk = Files.readAllBytes(file.toPath());
            long[] pk = Utils.bytesToLongs(bytesPk);
            return pk;
        } catch(IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void generatePrivateKey() {
        long[] key = new long[KEY_LENGTH];

        for(int i = 0; i < KEY_LENGTH; i++) {
            long val = ThreadLocalRandom.current().nextLong(1,  N + 1);
            key[i] = val;
        }

        storePrivateKey(key);
    }

    public boolean hasPrivateKeyStored() {
        File file = this.context.getFileStreamPath(PRIVATE_KEY);
        return file.exists();
    }
}
