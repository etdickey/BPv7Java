package BPv7.utils;
import java.io.*;

/**
 * Does all the really annoying CBOR stuff
 */
public class CBORCrap {
    /*
    Not using CBOR for now.
     */
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream out = null;

//    public byte[] ObjectToByte()
//    {
//        try {
//            try {
//                out = new ObjectOutputStream(bos);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//            out.writeObject(yourObject);
//            try {
//                out.flush();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//            byte[] yourBytes = bos.toByteArray();
//        }
//    }

}