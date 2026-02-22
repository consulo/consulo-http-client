package org.javamaster.httpclient.impl.enums;

import consulo.httpClient.localize.HttpClientLocalize;
import consulo.language.editor.completion.lookup.InsertHandler;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.ParenthesesInsertHandler;
import consulo.platform.Platform;
import consulo.project.Project;
import consulo.util.io.StreamUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.javamaster.httpclient.NlsBundle;
import org.javamaster.httpclient.impl.ui.HttpEditorTopForm;
import org.javamaster.httpclient.impl.utils.HttpUtils;
import org.javamaster.httpclient.impl.utils.VirtualFileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public enum InnerVariableEnum {
    RANDOM_ALPHABETIC("$random.alphabetic") {
        @Override
        public String typeText() {
            return NlsBundle.message("alphabetic.desc", methodName);
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            if (args.length != 1 || !(args[0] instanceof Integer)) {
                throw new IllegalArgumentException(methodName + " has wrong arguments." + typeText());
            }

            int count = (Integer) args[0];
            return RandomStringUtils.randomAlphabetic(count);
        }

        @Override
        public InsertHandler<LookupElement> insertHandler() {
            return ParenthesesInsertHandler.WITH_PARAMETERS;
        }
    },
    RANDOM_ALPHA_NUMERIC("$random.alphanumeric") {
        @Override
        public String typeText() {
            return NlsBundle.message("alphanumeric.desc", methodName);
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            if (args.length != 1 || !(args[0] instanceof Integer)) {
                throw new IllegalArgumentException(methodName + " has wrong arguments." + typeText());
            }

            int count = (Integer) args[0];
            return RandomStringUtils.randomAlphanumeric(count);
        }

        @Override
        public InsertHandler<LookupElement> insertHandler() {
            return ParenthesesInsertHandler.WITH_PARAMETERS;
        }
    },
    RANDOM_HEXADECIMAL("$random.hexadecimal") {
        @Override
        public String typeText() {
            return NlsBundle.message("hexadecimal.desc", methodName);
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            if (args.length != 1 || !(args[0] instanceof Integer)) {
                throw new IllegalArgumentException(methodName + " has wrong arguments." + typeText());
            }

            int max = (Integer) args[0];
            return Integer.toString(ThreadLocalRandom.current().nextInt(max), 16).toUpperCase();
        }

        @Override
        public InsertHandler<LookupElement> insertHandler() {
            return ParenthesesInsertHandler.WITH_PARAMETERS;
        }
    },
    RANDOM_NUMERIC("$random.numeric") {
        @Override
        public String typeText() {
            return NlsBundle.message("numeric.desc", methodName);
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            if (args.length != 1 || !(args[0] instanceof Integer)) {
                throw new IllegalArgumentException(methodName + " has wrong arguments." + typeText());
            }

            int count = (Integer) args[0];
            return RandomStringUtils.randomNumeric(count);
        }

        @Override
        public InsertHandler<LookupElement> insertHandler() {
            return ParenthesesInsertHandler.WITH_PARAMETERS;
        }
    },
    RANDOM_UUID("$random.uuid") {
        @Override
        public String typeText() {
            return NlsBundle.message("uuid.desc");
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            return UUID.randomUUID().toString().replace("-", "");
        }
    },
    RANDOM_INT("$randomInt") {
        @Override
        public String typeText() {
            return NlsBundle.message("randomInt.desc");
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            return String.valueOf(ThreadLocalRandom.current().nextInt(0, 1000));
        }
    },
    RANDOM_INTEGER("$random.integer") {
        @Override
        public String typeText() {
            return NlsBundle.message("integer.desc", methodName);
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            if (args.length != 2 || !(args[0] instanceof Integer) || !(args[1] instanceof Integer)) {
                throw new IllegalArgumentException(methodName + " has wrong arguments." + typeText());
            }

            int start = (Integer) args[0];
            int end = (Integer) args[1];
            return String.valueOf(ThreadLocalRandom.current().nextInt(start, end));
        }

        @Override
        public InsertHandler<LookupElement> insertHandler() {
            return ParenthesesInsertHandler.WITH_PARAMETERS;
        }
    },
    RANDOM_FLOAT("$random.float") {
        @Override
        public String typeText() {
            return NlsBundle.message("float.desc", methodName);
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            if (args.length != 2 || !(args[0] instanceof Integer) || !(args[1] instanceof Integer)) {
                throw new IllegalArgumentException(methodName + " has wrong arguments." + typeText());
            }

            int start = (Integer) args[0];
            int end = (Integer) args[1];
            return String.valueOf(ThreadLocalRandom.current().nextFloat((float) start, (float) end));
        }

        @Override
        public InsertHandler<LookupElement> insertHandler() {
            return ParenthesesInsertHandler.WITH_PARAMETERS;
        }
    },
    TIMESTAMP("$timestamp") {
        @Override
        public String typeText() {
            return NlsBundle.message("timestamp.desc");
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            return String.valueOf(System.currentTimeMillis());
        }
    },
    TIMESTAMP_FULL("$timestampFull") {
        @Override
        public String typeText() {
            return NlsBundle.message("timestampFull.desc", methodName);
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            if (args.length != 3 || !(args[0] instanceof Integer) || !(args[1] instanceof Integer) || !(args[2] instanceof Integer)) {
                throw new IllegalArgumentException(methodName + " has wrong arguments." + typeText());
            }

            int day = (Integer) args[0];
            int hour = (Integer) args[1];
            int minute = (Integer) args[2];

            LocalDateTime now = LocalDateTime.now().plusDays(day);
            LocalDateTime localDateTime = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), hour, minute, 0);
            ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
            Instant instant = zonedDateTime.toInstant();
            return String.valueOf(instant.toEpochMilli());
        }

        @Override
        public InsertHandler<LookupElement> insertHandler() {
            return ParenthesesInsertHandler.WITH_PARAMETERS;
        }
    },
    ISO_TIMESTAMP("$isoTimestamp") {
        @Override
        public String typeText() {
            return NlsBundle.message("isoTimestamp.desc");
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            return DateFormatUtils.format(new Date(), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", TimeZone.getDefault());
        }
    },
    DATETIME("$datetime") {
        @Override
        public String typeText() {
            return NlsBundle.message("datetime.desc");
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            return DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss", TimeZone.getDefault());
        }
    },
    TIMESTAMP_DATE("$timestampDate") {
        @Override
        public String typeText() {
            return NlsBundle.message("timestampDate.desc", methodName);
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            if (args.length != 1 || !(args[0] instanceof Integer)) {
                throw new IllegalArgumentException(methodName + " has wrong arguments." + typeText());
            }

            int count = (Integer) args[0];

            long seconds = LocalDateTime.of(LocalDate.now().plusDays(count), LocalTime.of(0, 0, 0))
                    .toEpochSecond(ZoneOffset.of("+08:00")) * 1000;
            return String.valueOf(seconds);
        }

        @Override
        public InsertHandler<LookupElement> insertHandler() {
            return ParenthesesInsertHandler.WITH_PARAMETERS;
        }
    },
    DATE("$date") {
        @Override
        public String typeText() {
            return NlsBundle.message("date.desc", methodName);
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            if (args.length != 1 || !(args[0] instanceof Integer)) {
                throw new IllegalArgumentException(methodName + " has wrong arguments." + typeText());
            }

            int count = (Integer) args[0];

            Date date = DateUtils.addDays(new Date(), count);

            return DateFormatUtils.format(date, "yyyy-MM-dd");
        }

        @Override
        public InsertHandler<LookupElement> insertHandler() {
            return ParenthesesInsertHandler.WITH_PARAMETERS;
        }
    },
    IMAGE_TO_BASE64("$imageToBase64") {
        @Override
        public String typeText() {
            return NlsBundle.message("imageToBase64.desc", methodName);
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            if (args.length != 1 || !(args[0] instanceof String)) {
                throw new IllegalArgumentException(methodName + " has wrong arguments." + typeText());
            }

            String path = (String) args[0];
            String filePath = HttpUtils.constructFilePath(path, httpFileParentPath);
            File file = new File(filePath);

            byte[] bytes = new byte[0];
            try {
                bytes = VirtualFileUtils.readNewestBytes(file);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (bytes.length == 0) {
                return "";
            }

            return Base64.getEncoder().encodeToString(bytes);
        }

        @Override
        public InsertHandler<LookupElement> insertHandler() {
            return ParenthesesInsertHandler.WITH_PARAMETERS;
        }
    },
    FILE_TO_BASE64("$fileToBase64") {
        @Override
        public String typeText() {
            return IMAGE_TO_BASE64.typeText();
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            return IMAGE_TO_BASE64.exec(httpFileParentPath, args);
        }

        @Override
        public InsertHandler<LookupElement> insertHandler() {
            return IMAGE_TO_BASE64.insertHandler();
        }
    },
    READ_STRING("$readString") {
        @Override
        public String typeText() {
            return NlsBundle.message("readString.desc", methodName);
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            if (args.length != 1 || !(args[0] instanceof String)) {
                throw new IllegalArgumentException(methodName + " has wrong arguments." + typeText());
            }

            String path = (String) args[0];
            String filePath = HttpUtils.constructFilePath(path, httpFileParentPath);
            File file = new File(filePath);

            try {
                return VirtualFileUtils.readNewestContent(file);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public InsertHandler<LookupElement> insertHandler() {
            return ParenthesesInsertHandler.WITH_PARAMETERS;
        }
    },
/*    RANDOM_ADDRESS("$random.address.full") {
        @Override
        public String typeText() {
            return NlsBundle.message("address.desc");
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            return RandomStringUtils.faker().address().fullAddress();
        }
    },
    RANDOM_BOOL("$random.bool") {
        @Override
        public String typeText() {
            return NlsBundle.message("bool.desc");
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            return String.valueOf(RandomStringUtils.faker().bool().bool());
        }
    },
    RANDOM_NAME("$random.name") {
        @Override
        public String typeText() {
            return NlsBundle.message("name.desc");
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            return RandomStringUtils.faker().name().name();
        }
    },
    RANDOM_BOOK_TITLE("$random.book.title") {
        @Override
        public String typeText() {
            return NlsBundle.message("book.desc");
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            return RandomStringUtils.faker().book().title();
        }
    },
    RANDOM_APP_NAME("$random.app.name") {
        @Override
        public String typeText() {
            return NlsBundle.message("app.desc");
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            return RandomStringUtils.faker().app().name();
        }
    },
    RANDOM_COMPANY_NAME("$random.company.name") {
        @Override
        public String typeText() {
            return NlsBundle.message("company.desc");
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            return RandomStringUtils.faker().company().name();
        }
    },
    RANDOM_HERO_NAME("$random.hero.name") {
        @Override
        public String typeText() {
            return HttpClientLocalize.heroDesc().get();
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            return RandomStringUtils.faker().superhero().name();
        }
    },
    RANDOM_NATION_NAME("$random.nation.name") {
        @Override
        public String typeText() {
            return NlsBundle.message("nation.desc");
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            return RandomStringUtils.faker().nation().capitalCity();
        }
    },
    RANDOM_UNIVERSITY_NAME("$random.university.name") {
        @Override
        public String typeText() {
            return NlsBundle.message("university.desc");
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            return RandomStringUtils.faker().university().name();
        }
    },
    RANDOM_PHONE_NUMBER("$random.phoneNumber") {
        @Override
        public String typeText() {
            return "生成电话号码";
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            return RandomStringUtils.faker().phoneNumber().phoneNumber();
        }
    },
    RANDOM_COLOR("$random.color") {
        @Override
        public String typeText() {
            return "生成颜色名称";
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            return RandomStringUtils.faker().color().name();
        }
    },
    RANDOM_EDUCATOR("$random.educator") {
        @Override
        public String typeText() {
            return "生成教育家课程";
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            return RandomStringUtils.faker().educator().course();
        }
    },
    RANDOM_HACKER("$random.hacker") {
        @Override
        public String typeText() {
            return "生成 hacker";
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            return RandomStringUtils.faker().hacker().noun();
        }
    },
    RANDOM_INTERNET("$random.internet") {
        @Override
        public String typeText() {
            return "生成 url";
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            return RandomStringUtils.faker().internet().url();
        }
    },
    RANDOM_EMAIL("$random.email") {
        @Override
        public String typeText() {
            return "生成 email";
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            return RandomStringUtils.faker().internet().emailAddress();
        }
    },
    RANDOM_BEER("$random.beer") {
        @Override
        public String typeText() {
            return "生成 beer";
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            return RandomStringUtils.faker().beer().name();
        }
    },
    RANDOM_CODE("$random.code") {
        @Override
        public String typeText() {
            return "生成 code";
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            return RandomStringUtils.faker().code().asin();
        }
    },
    RANDOM_COMMERCE("$random.commerce") {
        @Override
        public String typeText() {
            return "生成 commerce";
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            return RandomStringUtils.faker().commerce().productName();
        }
    },
    RANDOM_CRYPTO("$random.crypto") {
        @Override
        public String typeText() {
            return "生成 md5";
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            return RandomStringUtils.faker().crypto().md5();
        }
    },
    RANDOM_FINANCE("$random.finance") {
        @Override
        public String typeText() {
            return "生成 finance";
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            return RandomStringUtils.faker().finance().bic();
        }
    },
    RANDOM_ID_NUMBER("$random.idNumber") {
        @Override
        public String typeText() {
            return "生成 idNumber";
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            return RandomStringUtils.faker().idNumber().valid();
        }
    },
    RANDOM_LOREM("$random.lorem") {
        @Override
        public String typeText() {
            return "生成 lorem";
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            return RandomStringUtils.faker().lorem().word();
        }
    },
    RANDOM_ANIMAL("$random.animal") {
        @Override
        public String typeText() {
            return "生成 animal";
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            return RandomStringUtils.faker().animal().name();
        }
    },
    RANDOM_NUMBER("$random.number") {
        @Override
        public String typeText() {
            return "生成范围内数字,用法" + methodName + "(10, 1000)";
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            if (args.length != 2 || !(args[0] instanceof Integer) || !(args[1] instanceof Integer)) {
                throw new IllegalArgumentException(methodName + " has wrong arguments." + typeText());
            }

            return String.valueOf(RandomStringUtils.faker().number().numberBetween((Integer) args[0], (Integer) args[1]));
        }

        @Override
        public InsertHandler<LookupElement> insertHandler() {
            return ParenthesesInsertHandler.WITH_PARAMETERS;
        }
    },
    RANDOM_SHAKESPEARE("$random.shakespeare") {
        @Override
        public String typeText() {
            return "生成 shakespeare";
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            return RandomStringUtils.faker().shakespeare().hamletQuote();
        }
    },
    RANDOM_TEAM("$random.team") {
        @Override
        public String typeText() {
            return "生成 team";
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            return RandomStringUtils.faker().team().name();
        }
    },
    RANDOM_PROGRAMMING_LANGUAGE("$random.programmingLanguage") {
        @Override
        public String typeText() {
            return "生成 programmingLanguage";
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            return RandomStringUtils.faker().programmingLanguage().name();
        }
    },*/
    PICK("$random.pick") {
        @Override
        public String typeText() {
            return NlsBundle.message("pick.desc", methodName, methodName);
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            if (args.length == 0) {
                throw new IllegalArgumentException(methodName + " must to past arguments." + typeText());
            }

            return args[RandomUtils.insecure().randomInt(0, args.length)].toString();
        }

        @Override
        public InsertHandler<LookupElement> insertHandler() {
            return ParenthesesInsertHandler.WITH_PARAMETERS;
        }
    },
    REPEAT("$repeat") {
        @Override
        public String typeText() {
            return NlsBundle.message("repeat.desc", methodName);
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            if (args.length != 2 || !(args[0] instanceof String) || !(args[1] instanceof Integer)) {
                throw new IllegalArgumentException(methodName + " has wrong arguments." + typeText());
            }

            String str = (String) args[0];
            int times = (Integer) args[1];

            return StringUtils.repeat(str, times);
        }

        @Override
        public InsertHandler<LookupElement> insertHandler() {
            return ParenthesesInsertHandler.WITH_PARAMETERS;
        }
    },
//    EVAL("$eval") {
//        @Override
//        public String typeText() {
//            return NlsBundle.message("eval.desc", methodName);
//        }
//
//        @Override
//        public String exec(String httpFileParentPath, Object... args) {
//            if (args.length != 1 || !(args[0] instanceof String)) {
//                throw new IllegalArgumentException(methodName + " has wrong arguments." + typeText());
//            }
//
//            Context context = Context.enter();
//            try {
//                org.mozilla.javascript.Scriptable scriptableObject = context.initStandardObjects();
//                Object res = context.evaluateString(scriptableObject, (String) args[0], "dummy.js", 1, null);
//                return res.toString();
//            } finally {
//                Context.exit();
//            }
//        }
//
//        @Override
//        public InsertHandler<LookupElement> insertHandler() {
//            return ParenthesesInsertHandler.WITH_PARAMETERS;
//        }
//    },
    EXEC("$exec") {
        @Override
        public String typeText() {
            return NlsBundle.message("exec.desc", methodName);
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            if (args.length != 1 || !(args[0] instanceof String)) {
                throw new IllegalArgumentException(methodName + " has wrong arguments." + typeText());
            }

            String command;
            if (Platform.current().os().isWindows()) {
                command = "cmd /c " + args[0];
            } else {
                command = (String) args[0];
            }

            try {
                Process process = Runtime.getRuntime().exec(command);
                process.waitFor(3, TimeUnit.SECONDS);

                String msg = escapeIfNeeded(StreamUtil.readText(process.getInputStream(), Charset.forName("GBK")));

                if (msg.isEmpty()) {
                    msg = StreamUtil.readText(process.getErrorStream(), StandardCharsets.UTF_8);
                }

                msg = escapeIfNeeded(msg).substring(1, msg.length() - 1).replace("\\", "\\\\");

                return msg;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public InsertHandler<LookupElement> insertHandler() {
            return ParenthesesInsertHandler.WITH_PARAMETERS;
        }
    },
    MVN_TARGET("$mvnTarget") {
        @Override
        public String typeText() {
            return NlsBundle.message("mvnTarget.desc");
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String exec(String httpFileParentPath, Project project) {
            var triple = HttpEditorTopForm.getTriple(project);
            if (triple == null) {
                return null;
            }

            var module = triple.getThird();
            if (module == null) {
                return null;
            }

            String dirPath = module.getModuleDirPath();

            return dirPath + "/target";
        }
    },
    PROJECT_ROOT("$projectRoot") {
        @Override
        public String typeText() {
            return NlsBundle.message("projectRoot.desc");
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String exec(String httpFileParentPath, Project project) {
            return project.getBasePath();
        }
    },
    HISTORY_FOLDER("$historyFolder") {
        @Override
        public String typeText() {
            return HttpClientLocalize.historyfolderDesc().get();
        }

        @Override
        public String exec(String httpFileParentPath, Object... args) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String exec(String httpFileParentPath, Project project) {
            String basePath = project.getBasePath();
            if (basePath == null) {
                return null;
            }

            return basePath + "/.consulo/httpClient";
        }
    };

    protected final String methodName;

    InnerVariableEnum(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }

    public abstract String typeText();

    public abstract String exec(String httpFileParentPath, Object... args);

    public String exec(String httpFileParentPath, Project project) {
        return null;
    }

    public InsertHandler<LookupElement> insertHandler() {
        return null;
    }

    private static String escapeIfNeeded(String str) {
        // This method would need to be implemented based on the Kotlin extension function
        // For now, return as is
        return str;
    }

    private static final Map<String, InnerVariableEnum> map;

    static {
        map = new HashMap<>();
        for (InnerVariableEnum e : values()) {
            map.put(e.methodName, e);
        }
    }

    public static boolean isFolderEnum(InnerVariableEnum innerVariableEnum) {
        if (innerVariableEnum == null) {
            return false;
        }
        return innerVariableEnum == HISTORY_FOLDER
                || innerVariableEnum == PROJECT_ROOT
                || innerVariableEnum == MVN_TARGET;
    }

    public static InnerVariableEnum getEnum(String variable) {
        return map.get(variable);
    }
}
