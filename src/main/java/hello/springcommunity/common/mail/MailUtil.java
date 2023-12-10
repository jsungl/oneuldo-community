package hello.springcommunity.common.mail;

import java.util.Random;

public class MailUtil {

    /**
     * 인증번호 및 임시 비밀번호 8자리 생성 메서드
     */
    public static String createAuthKey() {
        Random random = new Random();
        StringBuffer key = new StringBuffer();

        for (int i = 0; i < 8; i++) {
            int index = random.nextInt(4);

            switch (index) {
                case 0: key.append((char) (random.nextInt(26) + 97)); break;
                case 1: key.append((char) (random.nextInt(26) + 65)); break;
                default: key.append(random.nextInt(9));
            }
        }
        return key.toString();
    }
}
