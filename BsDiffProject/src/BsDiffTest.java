public class BsDiffTest {

    public static void main(String[] args) {

        int result = BsDiffUtil.getInstance().bsDiffFile("version1.apk", "version2.apk", "version.patch");
        System.out.println("result = " + result);

    }

}
