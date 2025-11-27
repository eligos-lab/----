import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EmailService {
    private TempEmailAPI emailAPI;
    private EmailAccount currentAccount;
    private ScheduledExecutorService scheduler;
    private MessageListener messageListener;

    public EmailService() {
        this.emailAPI = new TempEmailAPI();
    }

    public EmailAccount createNewEmail() {
        try {
            System.out.println("Attempting to create real temporary email...");
            currentAccount = emailAPI.createRandomEmail();

            if (currentAccount != null) {
                System.out.println("Successfully created: " + currentAccount.getEmail());
                startMessagePolling();
                return currentAccount;
            } else {
                System.err.println("Failed to create email - all services unavailable");
            }

        } catch (Exception e) {
            System.err.println("Error creating email: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public List<EmailMessage> refreshMessages() {
        if (currentAccount != null) {
            try {
                System.out.println("Refreshing REAL messages for: " + currentAccount.getEmail());
                List<EmailMessage> messages = emailAPI.getMessages(currentAccount.getEmail());

                if (messages != null) {
                    currentAccount.setMessages(messages);
                    System.out.println("Retrieved " + messages.size() + " real messages");
                    return messages;
                }
            } catch (Exception e) {
                System.err.println("Error refreshing messages: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("No current account - cannot refresh messages");
        }

        return null;
    }

    private void startMessagePolling() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }

        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            if (currentAccount != null) {
                List<EmailMessage> newMessages = refreshMessages();
                if (messageListener != null && newMessages != null) {
                    messageListener.onMessagesUpdated(newMessages);
                }
            }
        }, 0, 15, TimeUnit.SECONDS); // Проверка каждые 15 секунд
    }

    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    public void stopService() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    public EmailAccount getCurrentAccount() {
        return currentAccount;
    }

    public boolean isSimulationMode() {
        return false; // Больше нет симуляции
    }

    public interface MessageListener {
        void onMessagesUpdated(List<EmailMessage> messages);
    }
}