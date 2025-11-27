import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class TempEmailAPI {
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();
    private static final Random random = new Random();

    // Токены и сессии для разных сервисов
    private String mailTMToken = null;
    private String guerrillaSID = null;

    public EmailAccount createRandomEmail() {
        System.out.println("=== Creating REAL temporary email ===");

        // Пробуем сервисы в порядке надежности
        EmailAccount account;

        // 1. Mail.tm - основной сервис
        account = createMailTMAccount();
        if (account != null) {
            System.out.println("✓ Successfully created Mail.tm account: " + account.getEmail());
            return account;
        }

        // 2. GuerrillaMail - резервный сервис
        account = createGuerrillaMailAccount();
        if (account != null) {
            System.out.println("✓ Successfully created GuerrillaMail account: " + account.getEmail());
            return account;
        }

        System.out.println("✗ All real services failed");
        return null;
    }

    /**
     * Mail.tm API - согласно документации https://docs.mail.tm/
     */
    private EmailAccount createMailTMAccount() {
        try {
            // 1. Получаем доступные домены
            String domain = getMailTMDomain();
            if (domain == null) {
                System.err.println("No Mail.tm domains available");
                return null;
            }

            // 2. Создаем случайный адрес и пароль
            String address = generateRandomUsername() + "@" + domain;
            String password = UUID.randomUUID().toString().substring(0, 16);

            // 3. Создаем аккаунт согласно API docs
            JsonObject accountData = new JsonObject();
            accountData.addProperty("address", address);
            accountData.addProperty("password", password);

            RequestBody body = RequestBody.create(
                    accountData.toString(),
                    MediaType.parse("application/json")
            );

            Request createRequest = new Request.Builder()
                    .url("https://api.mail.tm/accounts")
                    .post(body)
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "TemporaryEmailClient/1.0")
                    .build();

            Response createResponse = client.newCall(createRequest).execute();

            if (createResponse.code() == 201) {
                System.out.println("Mail.tm account created: " + address);

                // 4. Получаем токен аутентификации
                JsonObject tokenData = new JsonObject();
                tokenData.addProperty("address", address);
                tokenData.addProperty("password", password);

                RequestBody tokenBody = RequestBody.create(
                        tokenData.toString(),
                        MediaType.parse("application/json")
                );

                Request tokenRequest = new Request.Builder()
                        .url("https://api.mail.tm/token")
                        .post(tokenBody)
                        .header("Content-Type", "application/json")
                        .build();

                Response tokenResponse = client.newCall(tokenRequest).execute();

                if (tokenResponse.isSuccessful()) {
                    String tokenResponseBody = tokenResponse.body().string();
                    JsonObject tokenJson = JsonParser.parseString(tokenResponseBody).getAsJsonObject();
                    mailTMToken = tokenJson.get("token").getAsString();

                    EmailAccount account = new EmailAccount(address, password);
                    account.setPassword(mailTMToken); // Сохраняем токен
                    return account;
                }
            } else {
                System.err.println("Mail.tm account creation failed: " + createResponse.code());
                if (createResponse.body() != null) {
                    System.err.println("Response: " + createResponse.body().string());
                }
            }

        } catch (Exception e) {
            System.err.println("Mail.tm error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private String getMailTMDomain() {
        try {
            Request request = new Request.Builder()
                    .url("https://api.mail.tm/domains")
                    .header("User-Agent", "TemporaryEmailClient/1.0")
                    .build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                JsonArray domains = json.getAsJsonArray("hydra:member");

                if (domains.size() > 0) {
                    // Берем первый доступный домен
                    JsonObject domainObj = domains.get(0).getAsJsonObject();
                    return domainObj.get("domain").getAsString();
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting Mail.tm domains: " + e.getMessage());
        }
        return null;
    }

    /**
     * GuerrillaMail API - согласно документации https://www.guerrillamail.com/GuerrillaMailAPI.html
     */
    private EmailAccount createGuerrillaMailAccount() {
        try {
            // Согласно API: f=get_email_address - получаем или создаем email
            Request request = new Request.Builder()
                    .url("https://api.guerrillamail.com/ajax.php?f=get_email_address")
                    .header("User-Agent", "TemporaryEmailClient/1.0")
                    .build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String jsonResponse = response.body().string();
                JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

                // Согласно API docs, возвращает email_addr и sid_token
                if (jsonObject.has("email_addr") && jsonObject.has("sid_token")) {
                    String email = jsonObject.get("email_addr").getAsString();
                    String sid = jsonObject.get("sid_token").getAsString();

                    guerrillaSID = sid;

                    EmailAccount account = new EmailAccount(email, "");
                    account.setPassword(sid); // Сохраняем SID как идентификатор сессии
                    return account;
                }
            } else {
                System.err.println("GuerrillaMail account creation failed: " + response.code());
            }
        } catch (Exception e) {
            System.err.println("GuerrillaMail error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Получение сообщений - ТОЛЬКО реальные данные из API
     */
    public List<EmailMessage> getMessages(String email) {
        System.out.println("=== Getting REAL messages for: " + email + " ===");

        List<EmailMessage> messages = new ArrayList<>();

        // Определяем сервис по домену и используем соответствующий API
        if (email.contains("@mail.tm") || (mailTMToken != null)) {
            messages = getMailTMMessages();
        } else if (email.contains("@guerrillamail.com") || email.contains("@grr.la") || email.contains("@sharklasers.com") || (guerrillaSID != null)) {
            messages = getGuerrillaMessages();
        }

        System.out.println("Found " + messages.size() + " real messages");
        return messages;
    }

    private List<EmailMessage> getMailTMMessages() {
        List<EmailMessage> messages = new ArrayList<>();

        if (mailTMToken == null) {
            System.err.println("No Mail.tm token available");
            return messages;
        }

        try {
            // Согласно API docs: GET /messages для получения списка сообщений
            Request request = new Request.Builder()
                    .url("https://api.mail.tm/messages")
                    .header("Authorization", "Bearer " + mailTMToken)
                    .header("User-Agent", "TemporaryEmailClient/1.0")
                    .build();

            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                JsonArray messageArray = json.getAsJsonArray("hydra:member");

                System.out.println("Mail.tm API returned " + messageArray.size() + " messages");

                for (int i = 0; i < messageArray.size(); i++) {
                    JsonObject msg = messageArray.get(i).getAsJsonObject();

                    String id = msg.get("id").getAsString();
                    JsonObject fromObj = msg.get("from").getAsJsonObject();
                    String from = fromObj.get("address").getAsString();
                    String subject = msg.get("subject").getAsString();

                    // Получаем полный текст сообщения
                    String body = getMailTMFullMessage(id);

                    // Парсим дату в формате ISO 8601
                    String dateStr = msg.get("createdAt").getAsString();
                    Date date = parseISODate(dateStr);

                    messages.add(new EmailMessage(id, from, subject, body, date));
                    System.out.println("✓ Real Mail.tm message from: " + from);
                }
            } else {
                System.err.println("Mail.tm messages request failed: " + response.code());
                if (response.body() != null) {
                    System.err.println("Error response: " + response.body().string());
                }
            }

        } catch (Exception e) {
            System.err.println("Error getting Mail.tm messages: " + e.getMessage());
            e.printStackTrace();
        }

        return messages;
    }

    private String getMailTMFullMessage(String messageId) {
        if (mailTMToken == null) return "Content unavailable - no authentication";

        try {
            Request request = new Request.Builder()
                    .url("https://api.mail.tm/messages/" + messageId)
                    .header("Authorization", "Bearer " + mailTMToken)
                    .header("User-Agent", "TemporaryEmailClient/1.0")
                    .build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();

                // Согласно API, текст может быть в text или html полях
                if (json.has("text") && !json.get("text").isJsonNull()) {
                    return json.get("text").getAsString();
                } else if (json.has("html") && !json.get("html").isJsonNull()) {
                    return json.get("html").getAsString();
                } else {
                    return "No message content";
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting Mail.tm message body: " + e.getMessage());
        }
        return "Message content unavailable";
    }

    private List<EmailMessage> getGuerrillaMessages() {
        List<EmailMessage> messages = new ArrayList<>();

        if (guerrillaSID == null) {
            System.err.println("No GuerrillaMail SID available");
            return messages;
        }

        try {
            // Согласно API docs: f=get_email_list для получения списка сообщений
            Request request = new Request.Builder()
                    .url("https://api.guerrillamail.com/ajax.php?f=get_email_list&offset=0&sid_token=" + guerrillaSID)
                    .header("User-Agent", "TemporaryEmailClient/1.0")
                    .build();

            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                String jsonResponse = response.body().string();
                JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

                // Согласно API, возвращает список сообщений в поле "list"
                if (jsonObject.has("list")) {
                    JsonArray emailList = jsonObject.getAsJsonArray("list");

                    System.out.println("GuerrillaMail API returned " + emailList.size() + " messages");

                    for (int i = 0; i < emailList.size(); i++) {
                        JsonObject emailObj = emailList.get(i).getAsJsonObject();

                        String id = emailObj.get("mail_id").getAsString();
                        String from = emailObj.get("mail_from").getAsString();
                        String subject = emailObj.has("mail_subject") ?
                                emailObj.get("mail_subject").getAsString() : "No Subject";

                        // Получаем полное сообщение
                        String body = getGuerrillaFullMessage(id);

                        // Время в формате timestamp
                        long timestamp = emailObj.get("mail_timestamp").getAsLong();
                        Date date = new Date(timestamp * 1000);

                        messages.add(new EmailMessage(id, from, subject, body, date));
                        System.out.println("✓ Real GuerrillaMail message from: " + from);
                    }
                }
            } else {
                System.err.println("GuerrillaMail messages request failed: " + response.code());
            }

        } catch (Exception e) {
            System.err.println("Error getting GuerrillaMail messages: " + e.getMessage());
            e.printStackTrace();
        }

        return messages;
    }

    private String getGuerrillaFullMessage(String mailId) {
        if (guerrillaSID == null) return "Content unavailable - no authentication";

        try {
            // Согласно API docs: f=read_message для получения полного сообщения
            Request request = new Request.Builder()
                    .url("https://api.guerrillamail.com/ajax.php?f=fetch_email&email_id=" + mailId + "&sid_token=" + guerrillaSID)
                    .header("User-Agent", "TemporaryEmailClient/1.0")
                    .build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String jsonResponse = response.body().string();
                JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

                // Согласно API, текст сообщения может быть в разных полях
                if (jsonObject.has("mail_body") && !jsonObject.get("mail_body").isJsonNull()) {
                    return jsonObject.get("mail_body").getAsString();
                } else if (jsonObject.has("mail_excerpt") && !jsonObject.get("mail_excerpt").isJsonNull()) {
                    return jsonObject.get("mail_excerpt").getAsString();
                } else {
                    return "No message content";
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting GuerrillaMail message body: " + e.getMessage());
        }
        return "Message content unavailable";
    }

    /**
     * Парсинг даты в формате ISO 8601
     */
    private Date parseISODate(String dateStr) {
        try {
            // Простой парсинг для формата "2024-01-15T10:30:00.000Z"
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            return format.parse(dateStr);
        } catch (Exception e) {
            System.err.println("Error parsing date: " + dateStr);
            return new Date(); // Возвращаем текущую дату в случае ошибки
        }
    }

    /**
     * Генерация случайного имени пользователя
     */
    private String generateRandomUsername() {
        String[] adjectives = {"quick", "fast", "happy", "clever", "brave", "calm", "smart", "cool"};
        String[] nouns = {"fox", "wolf", "tiger", "eagle", "lion", "bear", "cat", "dog"};
        String[] numbers = {"2024", "123", "456", "789", "999", "111", "222", "333"};

        String adjective = adjectives[random.nextInt(adjectives.length)];
        String noun = nouns[random.nextInt(nouns.length)];
        String number = numbers[random.nextInt(numbers.length)];

        return adjective + noun + number;
    }
}