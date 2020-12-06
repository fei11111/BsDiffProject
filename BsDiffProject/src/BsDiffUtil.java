public class BsDiffUtil {

    static
    {
        System.loadLibrary("BsDllProject");
    }


    private static BsDiffUtil instance = new BsDiffUtil();

    public static BsDiffUtil getInstance(){
        return instance;
    }

    public native int bsDiffFile(String oldFile,String newFile,String patchFile);

}
