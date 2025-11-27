import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class MainFrame extends JFrame {
    private EmailService emailService;
    private EmailPanel emailPanel;
    private MessagePanel messagePanel;
    private JLabel globalStatusLabel;

    private static final Dimension MIN_SIZE = new Dimension(800, 650);
    private static final Dimension MAX_SIZE = new Dimension(1200, 900);
    private static final Dimension PREFERRED_SIZE = new Dimension(1000, 700);

    // Единый шрифт для всего приложения
    private static Font mainFont;
    private static Font titleFont;
    private static Font boldFont;
    private static Font smallFont;

    public MainFrame() {
        initializeFonts();
        initializeServices();
        initializeUI();
        setupListeners();
    }

    private void initializeFonts() {
        // Пробуем использовать современные шрифты, если доступны
        String[] preferredFonts = {
                "Segoe UI",          // Windows
                "SF Pro Display",    // macOS
                "Roboto",           // Linux/Android
                "Ubuntu",           // Linux
                "Arial",            // Universal
                "DejaVu Sans",      // Universal
                "SansSerif"         // Fallback
        };

        Font baseFont = null;
        for (String fontName : preferredFonts) {
            baseFont = new Font(fontName, Font.PLAIN, 12);
            if (baseFont.getFamily().equals(fontName)) {
                System.out.println("Using font: " + fontName);
                break;
            }
        }

        // Создаем семейство шрифтов
        mainFont = baseFont.deriveFont(Font.PLAIN, 12);
        titleFont = baseFont.deriveFont(Font.BOLD, 18);
        boldFont = baseFont.deriveFont(Font.BOLD, 12);
        smallFont = baseFont.deriveFont(Font.PLAIN, 11);
    }

    public static Font getMainFont() { return mainFont; }
    public static Font getTitleFont() { return titleFont; }
    public static Font getBoldFont() { return boldFont; }
    public static Font getSmallFont() { return smallFont; }

    private void initializeServices() {
        emailService = new EmailService();
    }

    private void initializeUI() {
        setTitle("📧 Сервис временных email-адресов");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setPreferredSize(PREFERRED_SIZE);
        setMinimumSize(MIN_SIZE);
        setMaximumSize(MAX_SIZE);

        emailPanel = new EmailPanel(emailService);
        messagePanel = new MessagePanel();

        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplitPane.setTopComponent(createHeaderPanel());
        mainSplitPane.setBottomComponent(messagePanel);
        mainSplitPane.setResizeWeight(0.25);
        mainSplitPane.setDividerSize(5);
        mainSplitPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY),
                BorderFactory.createEmptyBorder(3, 5, 3, 5)
        ));
        statusPanel.setBackground(new Color(240, 240, 240));

        globalStatusLabel = new JLabel(" Готов к работе. Нажмите 'Создать временный email' для начала.");
        globalStatusLabel.setFont(smallFont);

        JLabel versionLabel = new JLabel("v1.1 | Временный Email Сервис");
        versionLabel.setFont(smallFont);
        versionLabel.setForeground(Color.GRAY);

        statusPanel.add(globalStatusLabel, BorderLayout.WEST);
        statusPanel.add(versionLabel, BorderLayout.EAST);

        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainContainer.add(mainSplitPane, BorderLayout.CENTER);
        mainContainer.add(statusPanel, BorderLayout.SOUTH);

        setContentPane(mainContainer);

        pack();
        setLocationRelativeTo(null);
        mainSplitPane.setDividerLocation(200);
        setResizable(true);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(240, 240, 240));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel titleLabel = new JLabel("Сервис временных email-адресов", JLabel.CENTER);
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(new Color(0, 100, 200));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 15, 0));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(emailPanel, BorderLayout.CENTER);

        return headerPanel;
    }

    private void setupListeners() {
        emailService.setMessageListener(new EmailService.MessageListener() {
            @Override
            public void onMessagesUpdated(List<EmailMessage> messages) {
                messagePanel.updateMessages(messages);

                SwingUtilities.invokeLater(() -> {
                    if (messages != null && !messages.isEmpty()) {
                        globalStatusLabel.setText(" Получено сообщений: " + messages.size() +
                                " | Последнее обновление: " + new java.util.Date());
                    } else {
                        globalStatusLabel.setText(" Сообщений нет | Последнее обновление: " +
                                new java.util.Date());
                    }
                });
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cleanup();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                cleanup();
            }
        });

        Timer statusTimer = new Timer(30000, e -> {
            if (emailService.getCurrentAccount() != null) {
                globalStatusLabel.setText(" Аккаунт: " +
                        emailService.getCurrentAccount().getEmail() +
                        " | Последнее обновление: " + new java.util.Date());
            }
        });
        statusTimer.start();
    }

    private void cleanup() {
        if (emailService != null) {
            emailService.stopService();
        }
        System.out.println("Приложение завершено корректно.");
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // Устанавливаем шрифты для системных компонентов
            MainFrame.initializeFontsStatic();

            UIManager.put("SplitPane.background", new Color(240, 240, 240));
            UIManager.put("Panel.background", new Color(240, 240, 240));
            UIManager.put("OptionPane.background", new Color(240, 240, 240));

        } catch (Exception e) {
            System.err.println("Ошибка установки Look and Feel: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);

                JOptionPane.showMessageDialog(mainFrame,
                        "Добро пожаловать в Сервис временных email-адресов!\n\n" +
                                "Возможности:\n" +
                                "• Создание реальных временных email адресов\n" +
                                "• Автоматическая проверка входящих сообщений\n" +
                                "• Просмотр содержимого писем\n" +
                                "• Копирование email в буфер обмена\n\n" +
                                "Для начала нажмите 'Создать временный email'",
                        "Добро пожаловать",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Ошибка запуска приложения: " + e.getMessage(),
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }

    // Статический метод для инициализации шрифтов без создания экземпляра
    private static void initializeFontsStatic() {
        String[] preferredFonts = {
                "Segoe UI",
                "SF Pro Display",
                "Roboto",
                "Ubuntu",
                "Arial",
                "DejaVu Sans",
                "SansSerif"
        };

        Font baseFont = null;
        for (String fontName : preferredFonts) {
            baseFont = new Font(fontName, Font.PLAIN, 12);
            if (baseFont.getFamily().equals(fontName)) {
                System.out.println("Using font: " + fontName);
                break;
            }
        }

        mainFont = baseFont.deriveFont(Font.PLAIN, 12);
        titleFont = baseFont.deriveFont(Font.BOLD, 18);
        boldFont = baseFont.deriveFont(Font.BOLD, 12);
        smallFont = baseFont.deriveFont(Font.PLAIN, 11);

        // Устанавливаем шрифты для системных компонентов Swing
        UIManager.put("Button.font", mainFont);
        UIManager.put("Label.font", mainFont);
        UIManager.put("TextField.font", mainFont);
        UIManager.put("TextArea.font", mainFont);
        UIManager.put("List.font", mainFont);
        UIManager.put("ComboBox.font", mainFont);
        UIManager.put("CheckBox.font", mainFont);
        UIManager.put("RadioButton.font", mainFont);
        UIManager.put("ToggleButton.font", mainFont);
        UIManager.put("ProgressBar.font", mainFont);
        UIManager.put("Viewport.font", mainFont);
        UIManager.put("TabbedPane.font", mainFont);
        UIManager.put("ScrollPane.font", mainFont);
        UIManager.put("TitledBorder.font", boldFont);
    }

    public void setGlobalStatus(String status) {
        globalStatusLabel.setText(" " + status);
    }

    public void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    public void showInfoMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Информация", JOptionPane.INFORMATION_MESSAGE);
    }
}