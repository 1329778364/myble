package com.example.myble;

public class BluetoothUtils {

    public static final int OpenBluetooth_Request_Code = 10086;

    /**
      * 字节数组转十六进制字符串
      */
    public static String bytesToHexString(byte[] src) {
        if (src == null || src.length == 0){
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hex = Integer.toHexString(v);
            if (hex.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hex);
        }
        return stringBuilder.toString();
    }

    /**
     * 将字符串转成字节数组
     */
    public static byte[] hexStringToBytes(String str) {
        byte abyte0[] = new byte[str.length() / 2];
        byte[] s11 = str.getBytes();
        for (int i1 = 0; i1 < s11.length / 2; i1++) {
            byte byte1 = s11[i1 * 2 + 1];
            byte byte0 = s11[i1 * 2];
            String s2;
            abyte0[i1] = (byte) (
                    (byte0 = (byte) (Byte.decode((new StringBuilder(String.valueOf(s2 = "0x")))
                            .append(new String(new byte[]{byte0})).toString())
                            .byteValue() << 4)) ^
                            (byte1 = Byte.decode((new StringBuilder(String.valueOf(s2)))
                                    .append(new String(new byte[]{byte1})).toString()).byteValue()));
        }
        return abyte0;
    }


}
